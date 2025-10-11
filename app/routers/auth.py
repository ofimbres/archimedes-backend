"""Authentication router for user registration and management."""

from typing import Any

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.schemas.auth import (
    UserRegistrationRequest,
    UserRegistrationResponse,
    UserLoginRequest,
    UserLoginResponse,
    TokenRefreshRequest,
    LogoutRequest
)
from app.services.user_registration_service import UserRegistrationService
from app.services.cognito_service import CognitoService

router = APIRouter(
    prefix="/auth",
    tags=["Authentication"]
)


@router.post(
    "/register",
    response_model=UserRegistrationResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Register a new user",
    description="Register a new user in both AWS Cognito and the database"
)
async def register_user(
    request: UserRegistrationRequest,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Register a new user in both AWS Cognito and the database.

    This endpoint creates a user account in AWS Cognito for authentication
    and creates the corresponding user record in the database.

    Args:
        request: User registration data including credentials and profile info

    Returns:
        UserRegistrationResponse: The created user information

    Raises:
        HTTPException: If registration fails
    """
    try:
        registration_service = UserRegistrationService(db)
        return await registration_service.register_user(request)

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )


@router.post(
    "/login",
    response_model=UserLoginResponse,
    summary="User login",
    description="Authenticate user with username/email and password"
)
async def login(
    request: UserLoginRequest,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Authenticate a user and return access tokens.

    Args:
        request: Login credentials
        db: Database session dependency

    Returns:
        UserLoginResponse: Access tokens and user information

    Raises:
        HTTPException: If authentication fails
    """
    try:
        cognito_service = CognitoService()
        auth_result = await cognito_service.authenticate_user(
            username=request.username,
            password=request.password
        )

        # TODO: Optionally fetch additional user data from database
        # based on auth_result['user_info']['username']

        return UserLoginResponse(
            access_token=auth_result['access_token'],
            token_type=auth_result['token_type'],
            expires_in=auth_result['expires_in'],
            refresh_token=auth_result.get('refresh_token'),
            user=auth_result['user_info']
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Login failed: {str(e)}"
        )


@router.post(
    "/refresh",
    response_model=UserLoginResponse,
    summary="Refresh access token",
    description="Get a new access token using a refresh token"
)
async def refresh_token(
    request: TokenRefreshRequest
) -> Any:
    """
    Refresh an access token using a refresh token.

    Args:
        request: Refresh token request

    Returns:
        UserLoginResponse: New access token

    Raises:
        HTTPException: If token refresh fails
    """
    try:
        cognito_service = CognitoService()
        auth_result = await cognito_service.refresh_access_token(
            refresh_token=request.refresh_token
        )

        return UserLoginResponse(
            access_token=auth_result['access_token'],
            token_type=auth_result['token_type'],
            expires_in=auth_result['expires_in'],
            refresh_token=None,  # Refresh tokens are not returned on refresh
            user={}  # User info not available during refresh
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Token refresh failed: {str(e)}"
        )


@router.post(
    "/logout",
    summary="User logout",
    description="Logout user and invalidate access token"
)
async def logout(
    request: LogoutRequest
) -> Any:
    """
    Logout a user by invalidating their access token.

    Args:
        request: Logout request with access token

    Returns:
        Success message

    Raises:
        HTTPException: If logout fails
    """
    try:
        cognito_service = CognitoService()
        await cognito_service.logout_user(request.access_token)

        return {"message": "Successfully logged out"}

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Logout failed: {str(e)}"
        )
