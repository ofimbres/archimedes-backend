"""
Database configuration - SQLAlchemy is SO much cleaner than JPA!
"""

import boto3
import asyncpg
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase

from .config import settings


def _create_async_engine():
    """URL mode for local/dev; IAM token per new connection for RDS on ECS."""
    if settings.use_iam_auth:
        host = settings.db_host
        port = settings.db_port
        dbname = settings.db_name
        iam_user = settings.db_iam_user
        region = settings.aws_region

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
                ssl=True,
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
