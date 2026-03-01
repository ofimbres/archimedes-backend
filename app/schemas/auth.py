"""Authentication schemas for user registration and login."""

from typing import Literal, Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field, EmailStr, model_validator, validator


class UserRegistrationRequest(BaseModel):
    """Schema for user registration request."""

    username: str = Field(
        ...,
        min_length=3,
        max_length=50,
        description="Username for login"
    )
    email: EmailStr = Field(..., description="User's email address")
    password: str = Field(
        ...,
        min_length=8,
        description="Password (min 8 characters)"
    )
    given_name: str = Field(
        ...,
        min_length=1,
        max_length=50,
        description="User's first name",
        alias="givenName"
    )
    family_name: str = Field(
        ...,
        min_length=1,
        max_length=50,
        description="User's last name",
        alias="familyName"
    )
    school_id: UUID = Field(
        ...,
        description="UUID of the school",
        alias="schoolId"
    )
    user_type: Literal["students", "teachers"] = Field(
        ...,
        description="Type of user to register",
        alias="userType"
    )

    @validator('username')
    def validate_username(cls, v: str) -> str:
        """Validate username format."""
        v = v.strip().lower()
        if not v.replace('_', '').replace('.', '').isalnum():
            raise ValueError(
                'Username can only contain letters, numbers, '
                'dots, and underscores'
            )
        return v

    @validator('password')
    def validate_password(cls, v: str) -> str:
        """Validate password strength."""
        if len(v) < 8:
            raise ValueError('Password must be at least 8 characters long')

        # Check for at least one uppercase, lowercase, digit, and special char
        has_upper = any(c.isupper() for c in v)
        has_lower = any(c.islower() for c in v)
        has_digit = any(c.isdigit() for c in v)
        has_special = any(c in '!@#$%^&*()_+-=[]{}|;:,.<>?' for c in v)

        if not all([has_upper, has_lower, has_digit, has_special]):
            raise ValueError(
                'Password must contain uppercase, lowercase, '
                'digit, and special character'
            )

        return v

    model_config = ConfigDict(populate_by_name=True)


class UserRegistrationResponse(BaseModel):
    """Schema for user registration response."""

    user_id: UUID = Field(..., description="Database user ID")
    cognito_user_id: str = Field(..., description="Cognito user ID")
    username: str = Field(..., description="Username")
    email: str = Field(..., description="Email address")
    full_name: str = Field(..., description="Full name")
    user_type: str = Field(..., description="User type")
    school_id: UUID = Field(..., description="School ID")
    requires_verification: bool = Field(
        ...,
        description="Whether email verification is required"
    )
    message: str = Field(..., description="Registration status message")

    class Config:
        """Pydantic configuration."""
        json_encoders = {
            UUID: lambda v: str(v)
        }


class UserLoginRequest(BaseModel):
    """Schema for user login request."""

    username: str = Field(
        ...,
        min_length=3,
        max_length=50,
        description="Username or email"
    )
    password: str = Field(
        ...,
        min_length=1,
        description="User password"
    )


class UserLoginResponse(BaseModel):
    """Schema for user login response."""

    access_token: str = Field(..., description="JWT access token")
    token_type: str = Field(default="Bearer", description="Token type")
    expires_in: int = Field(
        ..., description="Token expiration time in seconds"
    )
    refresh_token: Optional[str] = Field(None, description="Refresh token")
    id_token: Optional[str] = Field(None, description="OIDC id_token (e.g. from OAuth/Google)")
    user: dict = Field(..., description="User information")

    class Config:
        """Pydantic configuration."""
        json_encoders = {
            UUID: lambda v: str(v)
        }


class TokenRefreshRequest(BaseModel):
    """Schema for token refresh request."""

    refresh_token: str = Field(..., description="Refresh token")


class LogoutRequest(BaseModel):
    """Schema for logout request."""

    access_token: str = Field(..., description="Access token to invalidate")


class MeResponse(BaseModel):
    """Current user profile from JWT: either a linked DB profile or none (complete profile needed)."""

    user_type: Optional[Literal["students", "teachers", "admin"]] = Field(
        None, description="Which profile or role exists"
    )
    profile: Optional[dict] = Field(
        None, description="Student or teacher record; null if not yet completed"
    )

    class Config:
        json_encoders = {UUID: lambda v: str(v)}


class CompleteProfileRequest(BaseModel):
    """Request to create student/teacher from OAuth identity (e.g. after Google sign-in).
    Students: send either joinCode (to join a class) or schoolId (no class). Teachers: schoolId required.
    """

    school_id: Optional[UUID] = Field(
        None, alias="schoolId", description="School to link to (required for teachers; optional for students if joinCode set)"
    )
    join_code: Optional[str] = Field(
        None, alias="joinCode", min_length=5, max_length=10,
        description="Class join code for students; resolves course and school, enrolls in course"
    )
    user_type: Literal["students", "teachers"] = Field(
        ..., alias="userType", description="Register as student or teacher"
    )

    model_config = ConfigDict(populate_by_name=True)

    @model_validator(mode="after")
    def validate_student_teacher_fields(self):
        """Student: exactly one of join_code or school_id. Teacher: school_id required."""
        user_type = self.user_type
        school_id = self.school_id
        join_code = self.join_code and str(self.join_code).strip()
        if user_type == "teachers":
            if not school_id:
                raise ValueError("Teachers must provide schoolId")
            return self
        if user_type == "students":
            has_code = bool(join_code)
            has_school = school_id is not None
            if has_code and has_school:
                raise ValueError("Students must provide either joinCode or schoolId, not both")
            if not has_code and not has_school:
                raise ValueError("Students must provide either joinCode or schoolId")
            return self
        return self


class MeUpdateRequest(BaseModel):
    """Optional personal info update for the current user (student or teacher)."""

    first_name: Optional[str] = Field(None, min_length=1, max_length=50, alias="firstName")
    last_name: Optional[str] = Field(None, min_length=1, max_length=50, alias="lastName")

    model_config = ConfigDict(populate_by_name=True)
