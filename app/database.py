"""
Database configuration - SQLAlchemy is SO much cleaner than JPA!
"""

import os
import ssl

import boto3
import asyncpg
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase

from .config import settings


def _rds_ssl_context() -> ssl.SSLContext:
    """
    RDS uses Amazon CAs; the OS trust store alone often fails verification.
    Load the AWS RDS global bundle on top of the default context (do not use cafile=
    alone on create_default_context — it can still mis-handle full chains with asyncpg).
    """
    ctx = ssl.create_default_context()
    candidates = [
        os.environ.get("RDS_SSL_CA_BUNDLE") or "",
        "/etc/ssl/rds/global-bundle.pem",
        "/app/docker/rds-global-bundle.pem",
    ]
    for path in candidates:
        if path and os.path.isfile(path):
            ctx.load_verify_locations(cafile=path)
            return ctx
    return ctx


def _create_async_engine():
    """URL mode for local/dev; IAM token per new connection for RDS on ECS."""
    if settings.use_iam_auth:
        host = settings.db_host
        port = settings.db_port
        dbname = settings.db_name
        iam_user = settings.db_iam_user
        region = settings.aws_region
        ssl_ctx = _rds_ssl_context()

        async def async_creator():
            rds = boto3.client("rds", region_name=region)
            token = rds.generate_db_auth_token(
                DBHostname=host,
                Port=int(port),
                DBUsername=iam_user,
                Region=region,
            )
            return await asyncpg.connect(
                host=host,
                port=int(port),
                user=iam_user,
                password=token,
                database=dbname,
                ssl=ssl_ctx,
            )

        return create_async_engine(
            "postgresql+asyncpg://",
            async_creator=async_creator,
            echo=settings.debug,
            future=True,
        )

    return create_async_engine(
        settings.database_url,
        echo=settings.debug,
        future=True,
    )


engine = _create_async_engine()

AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,
)


class Base(DeclarativeBase):
    """Base class for all models - so much simpler than JPA entities!"""
    pass


async def get_db():
    """Dependency to get database session"""
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()
