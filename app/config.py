"""
Configuration management - so much cleaner than Java's application.yml mess!
"""

from pydantic_settings import BaseSettings
from pydantic import Field, AliasChoices


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
    # OAuth callback URL (must match Cognito app client exactly).
    oauth_callback_uri: str = Field(
        default="http://localhost:8001/api/v1/auth/callback",
        validation_alias="OAUTH_CALLBACK_URI",
    )
    # Frontend URL for OAuth callback redirect (custom auth UI). When set, backend redirects
    # to {FRONTEND_URL}/auth/callback#access_token=... instead of returning HTML/JSON.
    frontend_url: str = Field(
        default="http://localhost:3000",
        validation_alias="FRONTEND_URL",
    )
    # Comma-separated CORS origins. If empty, FRONTEND_URL is used.
    cors_origins: str = Field(
        default="",
        validation_alias=AliasChoices("CORS_ORIGINS", "CORS_ORIGIN"),
    )
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

    # Miniquiz / exercise content base URL. Activity content URL = {miniquiz_base_url}/{activity_id}.html
    # e.g. http://d21jyw7vfrv0n9.cloudfront.net → http://d21jyw7vfrv0n9.cloudfront.net/EX01.html; no trailing slash.
    miniquiz_base_url: str = Field(
        default="",
        validation_alias=AliasChoices("S3_MINI_QUIZ_BASE_URL", "MINIQUIZ_BASE_URL"),
    )

    class Config:
        env_file = ".env"
        case_sensitive = False
        extra = "ignore"  # Ignore extra fields from .env file


# Global settings instance
settings = Settings()
