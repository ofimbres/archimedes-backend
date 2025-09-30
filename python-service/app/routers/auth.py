"""Authentication router for user registration and management."""

from typing import Any

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.schemas.auth import UserRegistrationRequest, UserRegistrationResponse
from app.services.user_registration_service import UserRegistrationService

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
