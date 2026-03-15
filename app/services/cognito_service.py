"""
AWS Cognito service for user authentication.
Supports both username/password and Hosted UI (e.g. Sign in with Google).
"""

import base64
import json
from typing import Dict, Any, Optional
from urllib.parse import quote, urlencode

import boto3
import httpx
from botocore.exceptions import ClientError
from fastapi import HTTPException, status

from ..config import settings


def _query_quote(s: str, safe: str = "", encoding: str | None = None, errors: str | None = None) -> str:
    """Quote for query string but leave '+' unencoded (Cognito expects scope=openid+email+profile)."""
    return quote(s, safe="+")


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
        self.domain = (settings.cognito_domain or "").rstrip("/")
        self.client_secret = settings.cognito_client_secret or None

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

    async def authenticate_user(
        self,
        username: str,
        password: str
    ) -> Dict[str, Any]:
        """Authenticate a user with username and password.

        Args:
            username: Username or email
            password: User's password

        Returns:
            Dictionary with authentication tokens and user info

        Raises:
            HTTPException: If authentication fails
        """
        try:
            # Use admin_initiate_auth for server-side authentication
            response = self.client.admin_initiate_auth(
                UserPoolId=self.user_pool_id,
                ClientId=self.client_id,
                AuthFlow='ADMIN_NO_SRP_AUTH',
                AuthParameters={
                    'USERNAME': username,
                    'PASSWORD': password
                }
            )

            # Extract tokens from response
            auth_result = response['AuthenticationResult']

            # Get user details
            user_info = self.client.admin_get_user(
                UserPoolId=self.user_pool_id,
                Username=username
            )

            # Extract user attributes
            user_attributes = {}
            for attr in user_info['UserAttributes']:
                user_attributes[attr['Name']] = attr['Value']

            return {
                'access_token': auth_result['AccessToken'],
                'refresh_token': auth_result.get('RefreshToken'),
                'id_token': auth_result.get('IdToken'),
                'token_type': auth_result.get('TokenType', 'Bearer'),
                'expires_in': auth_result.get('ExpiresIn', 3600),
                'user_info': {
                    'username': user_info['Username'],
                    'email': user_attributes.get('email'),
                    'given_name': user_attributes.get('given_name'),
                    'family_name': user_attributes.get('family_name'),
                    'user_type': user_attributes.get('custom:user_type'),
                    'user_status': user_info['UserStatus']
                }
            }

        except ClientError as e:
            error_code = e.response['Error']['Code']
            error_message = e.response['Error']['Message']

            if error_code == 'NotAuthorizedException':
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid username or password"
                )
            elif error_code == 'UserNotConfirmedException':
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="User account is not confirmed"
                )
            elif error_code == 'UserNotFoundException':
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid username or password"
                )
            else:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Authentication failed: {error_message}"
                )

    async def refresh_access_token(self, refresh_token: str) -> Dict[str, Any]:
        """Refresh an access token using a refresh token.

        Args:
            refresh_token: The refresh token

        Returns:
            Dictionary with new access token

        Raises:
            HTTPException: If token refresh fails
        """
        try:
            response = self.client.admin_initiate_auth(
                UserPoolId=self.user_pool_id,
                ClientId=self.client_id,
                AuthFlow='REFRESH_TOKEN_AUTH',
                AuthParameters={
                    'REFRESH_TOKEN': refresh_token
                }
            )

            auth_result = response['AuthenticationResult']

            return {
                'access_token': auth_result['AccessToken'],
                'id_token': auth_result.get('IdToken'),
                'token_type': auth_result.get('TokenType', 'Bearer'),
                'expires_in': auth_result.get('ExpiresIn', 3600)
            }

        except ClientError as e:
            error_code = e.response['Error']['Code']
            error_message = e.response['Error']['Message']

            if error_code == 'NotAuthorizedException':
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid or expired refresh token"
                )
            else:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Token refresh failed: {error_message}"
                )

    async def logout_user(self, access_token: str) -> bool:
        """Logout a user by invalidating their access token.

        Args:
            access_token: The access token to invalidate

        Returns:
            True if logout successful

        Raises:
            HTTPException: If logout fails
        """
        try:
            self.client.global_sign_out(
                AccessToken=access_token
            )
            return True

        except ClientError as e:
            error_code = e.response['Error']['Code']
            error_message = e.response['Error']['Message']

            if error_code == 'NotAuthorizedException':
                # Token is already invalid, consider it a successful logout
                return True
            else:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Logout failed: {error_message}"
                )

    def get_authorization_url(
        self,
        redirect_uri: str,
        state: Optional[str] = None,
        identity_provider: Optional[str] = "Google",
        scope: str = "openid email profile",
        prompt: Optional[str] = None,
    ) -> str:
        """Build Cognito Hosted UI authorize URL (e.g. Sign in with Google).

        Args:
            redirect_uri: Callback URL (must match one configured in Cognito app client).
            state: Optional state for CSRF protection.
            identity_provider: IdP name in Cognito (e.g. 'Google'). None = show Cognito login.
            scope: OAuth scopes (default openid email profile).
            prompt: Optional OIDC prompt (e.g. 'select_account' to show account picker).

        Returns:
            Full URL to redirect the user to.
        """
        if not self.domain:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Cognito Hosted UI is not configured (COGNITO_DOMAIN)"
            )
        params = {
            "response_type": "code",
            "client_id": self.client_id,
            "redirect_uri": redirect_uri,
            "scope": scope.replace(" ", "+"),
        }
        if state:
            params["state"] = state
        if identity_provider:
            params["identity_provider"] = identity_provider
        if prompt:
            params["prompt"] = prompt
        # Use quote with safe='+' so scope stays as openid+email+profile (Cognito expects literal +)
        return f"https://{self.domain}/oauth2/authorize?{urlencode(params, quote_via=_query_quote)}"

    async def exchange_code_for_tokens(
        self,
        code: str,
        redirect_uri: str,
    ) -> Dict[str, Any]:
        """Exchange authorization code for tokens (OAuth2 code flow).

        Args:
            code: Authorization code from Cognito callback.
            redirect_uri: Same redirect_uri used in the authorize request.

        Returns:
            Dict with access_token, id_token, refresh_token, expires_in, user_info.
        """
        if not self.domain:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Cognito Hosted UI is not configured (COGNITO_DOMAIN)"
            )
        token_url = f"https://{self.domain}/oauth2/token"
        body = {
            "grant_type": "authorization_code",
            "code": code,
            "redirect_uri": redirect_uri,
            "client_id": self.client_id,
        }
        if self.client_secret:
            body["client_secret"] = self.client_secret
        headers = {"Content-Type": "application/x-www-form-urlencoded"}
        async with httpx.AsyncClient() as client:
            response = await client.post(
                token_url,
                data=body,
                headers=headers,
            )
        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Token exchange failed: {response.text or response.status_code}"
            )
        data = response.json()
        id_token = data.get("id_token") or ""
        user_info = self._decode_id_token_claims(id_token)
        return {
            "access_token": data.get("access_token", ""),
            "id_token": id_token,
            "refresh_token": data.get("refresh_token"),
            "token_type": data.get("token_type", "Bearer"),
            "expires_in": data.get("expires_in", 3600),
            "user_info": user_info,
        }

    @staticmethod
    def _decode_id_token_claims(id_token: str) -> Dict[str, Any]:
        """Decode JWT payload for user_info (no verification; token from our Cognito token exchange)."""
        if not id_token or "." not in id_token:
            return {}
        try:
            payload_b64 = id_token.split(".")[1]
            padding = 4 - len(payload_b64) % 4
            if padding != 4:
                payload_b64 += "=" * padding
            raw = base64.urlsafe_b64decode(payload_b64)
            claims = json.loads(raw)
            return {
                "username": claims.get("cognito:username") or claims.get("sub", ""),
                "email": claims.get("email"),
                "given_name": claims.get("given_name"),
                "family_name": claims.get("family_name"),
                "sub": claims.get("sub"),
            }
        except Exception:
            return {}
