"""
Configuration management - so much cleaner than Java's application.yml mess!
"""

from pydantic import AliasChoices, Field, model_validator
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings with automatic environment variable loading"""

    # Database — local/dev: set DATABASE_URL (single string). ECS/RDS IAM: set USE_IAM_AUTH=true
    # and DB_HOST, DB_PORT, DB_NAME, DB_IAM_USER; omit or ignore DATABASE_URL.
    database_url: str = Field(
        default="",
        validation_alias=AliasChoices("DATABASE_URL", "database_url"),
    )
    use_iam_auth: bool = Field(
        default=False,
        validation_alias=AliasChoices("USE_IAM_AUTH", "use_iam_auth"),
    )
    db_host: str = Field(default="", validation_alias="DB_HOST")
    db_port: int = Field(default=5432, validation_alias="DB_PORT")
    db_name: str = Field(
        default="",
        validation_alias=AliasChoices("DB_NAME", "DBNAME"),
    )
    db_iam_user: str = Field(default="", validation_alias="DB_IAM_USER")

    @model_validator(mode="after")
    def _normalize_database_config(self) -> "Settings":
        if self.use_iam_auth:
            if not self.db_host or not self.db_name or not self.db_iam_user:
                raise ValueError(
                    "USE_IAM_AUTH requires DB_HOST, DB_NAME, and DB_IAM_USER"
                )
            return self
        url = (self.database_url or "").strip()
        if not url:
            url = "postgresql+asyncpg://postgres:postgres@localhost:5432/archimedes"
        if url.startswith("postgresql://"):
            url = url.replace("postgresql://", "postgresql+asyncpg://", 1)
        elif url.startswith("postgres://"):
            url = url.replace("postgres://", "postgresql+asyncpg://", 1)
        object.__setattr__(self, "database_url", url)
        return self

    # API
    api_host: str = "0.0.0.0"
    api_port: int = 8001

    # Environment
    environment: str = "development"
    debug: bool = True

    # Logging
    log_level: str = "INFO"

    # AWS Cognito / RDS IAM auth
    aws_region: str = Field(
        default="us-west-2",
        validation_alias=AliasChoices("AWS_REGION", "aws_region"),
    )
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
