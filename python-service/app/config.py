"""
Configuration management - so much cleaner than Java's application.yml mess!
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings with automatic environment variable loading"""

    # Database
    database_url: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/archimedes"

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

    class Config:
        env_file = ".env"
        case_sensitive = False


# Global settings instance
settings = Settings()
