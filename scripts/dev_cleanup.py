#!/usr/bin/env python3
"""
Development cleanup script for Archimedes Education Platform.

This script cleans up:
1. All users from AWS Cognito User Pool
2. All records from database tables
3. Resets the database to a clean state

⚠️  WARNING: This script will DELETE ALL DATA! Use only in development!
"""

import asyncio
import boto3
from botocore.exceptions import ClientError
from sqlalchemy import text


from app.config import settings
from app.database import AsyncSessionLocal, engine, Base


class DevCleanupService:
    """Service for cleaning up development data."""

    def __init__(self):
        """Initialize cleanup service."""
        self.cognito_client = boto3.client(
            'cognito-idp',
            region_name=settings.aws_region
        )

    async def delete_all_cognito_users(self) -> int:
        """
        Delete all users from Cognito User Pool.

        Returns:
            Number of users deleted
        """
        print("🧹 Cleaning up Cognito users...")

        try:
            users_deleted = 0
            paginator = self.cognito_client.get_paginator('list_users')

            for page in paginator.paginate(
                UserPoolId=settings.cognito_user_pool_id
            ):
                users = page.get('Users', [])

                for user in users:
                    username = user['Username']
                    try:
                        self.cognito_client.admin_delete_user(
                            UserPoolId=settings.cognito_user_pool_id,
                            Username=username
                        )
                        users_deleted += 1
                        print(f"  ✅ Deleted Cognito user: {username}")
                    except ClientError as e:
                        print(f"  ❌ Failed to delete user {username}: {e}")

            print(f"🎉 Deleted {users_deleted} Cognito users")
            return users_deleted

        except ClientError as e:
            print(f"❌ Error listing Cognito users: {e}")
            return 0

    async def truncate_all_tables(self) -> None:
        """
        Truncate all application tables in the database.

        ⚠️ WARNING: This will delete ALL data!
        """
        print("🧹 Cleaning up database tables...")

        async with AsyncSessionLocal() as session:
            try:
                # Disable foreign key checks temporarily
                await session.execute(
                    text("SET session_replication_role = replica;")
                )

                # Get all table names from our models
                table_names = [
                    table.name for table in Base.metadata.tables.values()
                ]

                tables_cleared = 0
                for table_name in table_names:
                    try:
                        result = await session.execute(
                            text(f"SELECT COUNT(*) FROM {table_name}")
                        )
                        count = result.scalar()

                        if count > 0:
                            await session.execute(
                                text(f"TRUNCATE TABLE {table_name} CASCADE")
                            )
                            tables_cleared += 1
                            print(
                                f"  ✅ Cleared {count} records from {table_name}")
                        else:
                            print(
                                f"  ℹ️  Table {table_name} was already empty")

                    except Exception as e:
                        print(f"  ❌ Failed to clear table {table_name}: {e}")

                # Re-enable foreign key checks
                await session.execute(
                    text("SET session_replication_role = DEFAULT;")
                )

                await session.commit()
                print(f"🎉 Cleared {tables_cleared} tables")

            except Exception as e:
                await session.rollback()
                print(f"❌ Error clearing database: {e}")
                raise

    async def reset_database_schema(self) -> None:
        """
        Drop and recreate all tables.

        ⚠️ WARNING: This will delete ALL data and recreate the schema!
        """
        print("🔄 Resetting database schema...")

        try:
            # Drop all tables
            async with engine.begin() as conn:
                await conn.run_sync(Base.metadata.drop_all)
                print("  ✅ Dropped all tables")

                # Recreate all tables
                await conn.run_sync(Base.metadata.create_all)
                print("  ✅ Recreated all tables")

            print("🎉 Database schema reset complete")

        except Exception as e:
            print(f"❌ Error resetting database schema: {e}")
            raise

    async def cleanup_all(self, reset_schema: bool = False) -> None:
        """
        Clean up everything: Cognito users and database.

        Args:
            reset_schema: If True, drop and recreate database schema
        """
        print("🚀 Starting full cleanup...")
        print(f"   AWS Region: {settings.aws_region}")
        print(f"   Cognito User Pool: {settings.cognito_user_pool_id}")
        if settings.use_iam_auth:
            print(f"   Database: {settings.db_host}:{settings.db_port}/{settings.db_name} (IAM)")
        else:
            print(f"   Database: {settings.database_url.split('@')[-1]}")
        print("   ⚠️  This will DELETE ALL DATA!")

        # Clean up Cognito users
        cognito_deleted = await self.delete_all_cognito_users()

        # Clean up database
        if reset_schema:
            await self.reset_database_schema()
        else:
            await self.truncate_all_tables()

        print("\n🎉 Cleanup complete!")
        print(f"   Cognito users deleted: {cognito_deleted}")
        print("   Database: Cleaned")


async def main():
    """Main entry point for the cleanup script."""
    import sys

    print("=" * 60)
    print("🧹 ARCHIMEDES DEVELOPMENT CLEANUP SCRIPT")
    print("=" * 60)

    # Safety check
    if settings.environment.lower() == 'production':
        print("❌ ERROR: Cannot run cleanup script in production environment!")
        sys.exit(1)

    cleanup_service = DevCleanupService()

    # Parse command line arguments
    reset_schema = '--reset-schema' in sys.argv

    if reset_schema:
        print("⚠️  SCHEMA RESET MODE: Will drop and recreate all tables")

    # Ask for confirmation
    if '--force' not in sys.argv:
        confirm = input(
            "\n⚠️  Are you sure you want to delete ALL data? (type 'yes' to confirm): "
        )
        if confirm.lower() != 'yes':
            print("❌ Cleanup cancelled")
            sys.exit(0)

    try:
        await cleanup_service.cleanup_all(reset_schema=reset_schema)
    except Exception as e:
        print(f"\n❌ Cleanup failed: {e}")
        sys.exit(1)


if __name__ == "__main__":
    asyncio.run(main())
