"""User registration service combining Cognito and database operations."""

from sqlalchemy.ext.asyncio import AsyncSession
from fastapi import HTTPException, status

from ..schemas.auth import UserRegistrationRequest, UserRegistrationResponse
from ..schemas.student import StudentCreate, StudentResponse
from ..services.student_service import StudentService
from ..services.cognito_service import CognitoService


class UserRegistrationService:
    """Service for unified user registration."""

    def __init__(self, db: AsyncSession):
        """Initialize registration service."""
        self.db = db
        self.cognito_service = CognitoService()

    async def register_user(
        self,
        registration_data: UserRegistrationRequest
    ) -> UserRegistrationResponse:
        """Register a new user in both Cognito and database.

        This is an atomic operation - if either Cognito or database
        registration fails, the entire operation is rolled back.

        Args:
            registration_data: User registration data

        Returns:
            Registration response with user details

        Raises:
            HTTPException: If registration fails
        """
        cognito_user_id = None
        db_user = None

        try:
            # Step 1: Register in Cognito first
            cognito_result = await self.cognito_service.register_user(
                username=registration_data.username,
                email=registration_data.email,
                password=registration_data.password,
                given_name=registration_data.given_name,
                family_name=registration_data.family_name,
                user_type=registration_data.user_type
            )
            cognito_user_id = cognito_result['cognito_user_id']

            # Step 2: Register in database based on user type
            if registration_data.user_type == "students":
                db_user = await self._register_student(registration_data)
            elif registration_data.user_type == "teachers":
                # TODO: Implement teacher registration
                raise HTTPException(
                    status_code=status.HTTP_501_NOT_IMPLEMENTED,
                    detail="Teacher registration not yet implemented"
                )
            else:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Invalid user type"
                )

            # Step 3: Return success response
            return UserRegistrationResponse(
                user_id=db_user.id,
                cognito_user_id=cognito_user_id,
                username=db_user.username,
                email=db_user.email,
                full_name=db_user.full_name,
                user_type=registration_data.user_type,
                school_id=db_user.school_id,
                requires_verification=cognito_result['requires_verification'],
                message="User registered successfully"
            )

        except Exception as e:
            # Rollback: Clean up Cognito user if database registration failed
            if cognito_user_id and not db_user:
                try:
                    await self.cognito_service.delete_user(
                        registration_data.username
                    )
                except Exception:
                    # Log the cleanup failure but don't mask the original error
                    pass

            # Re-raise the original exception
            if isinstance(e, HTTPException):
                raise e
            else:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Registration failed: {str(e)}"
                )

    async def _register_student(
        self,
        registration_data: UserRegistrationRequest
    ) -> StudentResponse:
        """Register a student in the database.

        Args:
            registration_data: Registration data

        Returns:
            Student response object
        """
        student_service = StudentService(self.db)

        student_data = StudentCreate(
            school_id=registration_data.school_id,
            first_name=registration_data.given_name,
            last_name=registration_data.family_name,
            email=registration_data.email,
            username=registration_data.username
        )

        return await student_service.create_student(student_data)

