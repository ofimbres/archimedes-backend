"""
Configuration management - so much cleaner than Java's application.yml mess!
"""

from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    """Application settings with automatic environment variable loading"""

    # Database
    database_url: str = (
        "postgresql+asyncpg://postgres:postgres@localhost:5432/archimedes"
    )

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        # Ensure asyncpg driver is used for PostgreSQL
        if self.database_url.startswith('postgresql://'):
            self.database_url = self.database_url.replace(
                'postgresql://', 'postgresql+asyncpg://'
            )
        elif self.database_url.startswith('postgres://'):
            self.database_url = self.database_url.replace(
                'postgres://', 'postgresql+asyncpg://'
            )

    # API
    api_host: str = "0.0.0.0"
    api_port: int = 8001

    # Environment
    environment: str = "development"
    debug: bool = True

    # Logging
    log_level: str = "INFO"

    # AWS Cognito
    aws_region: str = "us-west-2"
    cognito_user_pool_id: str = ""
    cognito_client_id: str = ""
    # Hosted UI domain, e.g. archimedes-dev.auth.us-east-1.amazoncognito.com
    cognito_domain: str = ""
    # Optional; required for server-side code exchange if app client is confidential
    cognito_client_secret: str = ""
    # Public URL of this backend (for OAuth redirect_uri). From infra: BACKEND_URL.
    backend_public_url: str = Field(
        default="http://localhost:8001", validation_alias="BACKEND_URL")
    aws_access_key_id: str = ""
    aws_secret_access_key: str = ""

    # Admin (not shown in UI; for /me and admin-only routes)
    admin_emails: str = ""  # Comma-separated emails, e.g. admin@example.com
    admin_cognito_ids: str = ""  # Comma-separated Cognito sub values

    # Worksheet File Service
    worksheets_use_s3: bool = False
    worksheets_local_path: str = "/workspace/deploy/mini-quizzes"
    worksheets_s3_bucket: str = ""
    worksheets_s3_prefix: str = "worksheets/"

    class Config:
        env_file = ".env"
        case_sensitive = False
        extra = "ignore"  # Ignore extra fields from .env file


# Global settings instance
settings = Settings()
