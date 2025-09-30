"""
AWS Cognito service for user authentication.
"""

import boto3
from typing import Dict, Any
from botocore.exceptions import ClientError
from fastapi import HTTPException, status

from ..config import settings


class CognitoService:
    """Service for AWS Cognito operations."""

    def __init__(self):
        """Initialize Cognito client."""
        self.client = boto3.client(
            'cognito-idp',
            region_name=settings.aws_region
            # boto3 auto-discovers AWS credentials from environment variables
        )
        self.user_pool_id = settings.cognito_user_pool_id
        self.client_id = settings.cognito_client_id

    async def register_user(
        self,
        username: str,
        email: str,
        password: str,
        given_name: str,
        family_name: str,
        user_type: str
    ) -> Dict[str, Any]:
        """Register a new user in Cognito.

        Args:
            username: Username for login
            email: User's email address
            password: User's password
            given_name: User's first name
            family_name: User's last name
            user_type: Type of user (students/teachers)

        Returns:
            Dictionary with Cognito user details

        Raises:
            HTTPException: If registration fails
        """
        try:
            # Create user in Cognito
            response = self.client.admin_create_user(
                UserPoolId=self.user_pool_id,
                Username=username,
                UserAttributes=[
                    {'Name': 'email', 'Value': email},
                    {'Name': 'email_verified', 'Value': 'true'},
                    {'Name': 'given_name', 'Value': given_name},
                    {'Name': 'family_name', 'Value': family_name},
                    # {'Name': 'custom:user_type', 'Value': user_type}
                ],
                TemporaryPassword=password,
                MessageAction='SUPPRESS'  # Don't send welcome email
            )

            # Set permanent password
            self.client.admin_set_user_password(
                UserPoolId=self.user_pool_id,
                Username=username,
                Password=password,
                Permanent=True
            )

            return {
                'cognito_user_id': response['User']['Username'],
                'user_status': response['User']['UserStatus'],
                'requires_verification': False
            }

        except ClientError as e:
            error_code = e.response['Error']['Code']
            error_message = e.response['Error']['Message']

            if error_code == 'UsernameExistsException':
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail="Username already exists"
                )
            elif error_code == 'InvalidPasswordException':
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Password does not meet requirements"
                )
            elif error_code == 'InvalidParameterException':
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Invalid parameter: {error_message}"
                )
            else:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Cognito registration failed: {error_message}"
                )

    async def delete_user(self, username: str) -> bool:
        """Delete a user from Cognito.

        Args:
            username: Username to delete

        Returns:
            True if user was deleted, False if user not found
        """
        try:
            self.client.admin_delete_user(
                UserPoolId=self.user_pool_id,
                Username=username
            )
            return True
        except ClientError as e:
            if e.response['Error']['Code'] == 'UserNotFoundException':
                return False
            raise

    async def user_exists(self, username: str) -> bool:
        """Check if a user exists in Cognito.

        Args:
            username: Username to check

        Returns:
            True if user exists, False otherwise
        """
        try:
            self.client.admin_get_user(
                UserPoolId=self.user_pool_id,
                Username=username
            )
            return True
        except ClientError as e:
            if e.response['Error']['Code'] == 'UserNotFoundException':
                return False
            raise
