"""
Pydantic schemas for Student operations.

These schemas handle request/response validation and serialization.
"""

from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel, Field, validator, EmailStr


class StudentBase(BaseModel):
    """Base student schema with common fields."""
    
    first_name: str = Field(
        ...,
        min_length=1,
        max_length=50,
        description="Student's first name"
    )
    last_name: str = Field(
        ...,
        min_length=1,
        max_length=50,
        description="Student's last name"
    )
    email: EmailStr = Field(..., description="Student's email address")
    username: str = Field(
        ...,
        min_length=3,
        max_length=50,
        description="Student's username"
    )

    @validator('first_name', 'last_name')
    def validate_names(cls, v: str) -> str:
        """Validate name fields are not just whitespace."""
        if not v or not v.strip():
            raise ValueError('Name cannot be empty or just whitespace')
        return v.strip().title()

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


class StudentCreate(StudentBase):
    """Schema for creating a new student."""
    
    school_id: UUID = Field(..., description="UUID of the school")
    cognito_user_id: Optional[str] = Field(
        None, description="Cognito sub for OAuth users; links identity to this record"
    )

    @validator('school_id')
    def validate_school_id(cls, v: UUID) -> UUID:
        """Validate school_id is a proper UUID."""
        if not v:
            raise ValueError('School ID is required')
        return v


class StudentUpdate(BaseModel):
    """Schema for updating an existing student."""
    
    first_name: Optional[str] = Field(
        None,
        min_length=1,
        max_length=50,
        description="Student's first name"
    )
    last_name: Optional[str] = Field(
        None,
        min_length=1,
        max_length=50,
        description="Student's last name"
    )
    email: Optional[EmailStr] = Field(
        None,
        description="Student's email address"
    )
    username: Optional[str] = Field(
        None,
        min_length=3,
        max_length=50,
        description="Student's username"
    )
    is_active: Optional[bool] = Field(
        None,
        description="Whether the student is active"
    )

    @validator('first_name', 'last_name')
    def validate_names(cls, v: Optional[str]) -> Optional[str]:
        """Validate name fields are not just whitespace."""
        if v is not None:
            if not v or not v.strip():
                raise ValueError('Name cannot be empty or just whitespace')
            return v.strip().title()
        return v

    @validator('username')
    def validate_username(cls, v: Optional[str]) -> Optional[str]:
        """Validate username format."""
        if v is not None:
            v = v.strip().lower()
            if not v.replace('_', '').replace('.', '').isalnum():
                raise ValueError(
                    'Username can only contain letters, numbers, '
                    'dots, and underscores'
                )
            return v
        return v


class StudentResponse(StudentBase):
    """Schema for student responses."""
    
    id: UUID = Field(..., description="Student's unique ID")
    school_id: UUID = Field(..., description="School's unique ID")
    full_name: str = Field(..., description="Student's full name")
    is_active: bool = Field(..., description="Whether student is active")
    created_at: datetime = Field(..., description="When student was created")
    updated_at: datetime = Field(
        ..., description="When student was last updated"
    )

    class Config:
        """Pydantic configuration."""
        from_attributes = True
        json_encoders = {
            datetime: lambda v: v.isoformat(),
            UUID: lambda v: str(v)
        }


class StudentList(BaseModel):
    """Schema for paginated student lists."""
    
    students: list[StudentResponse] = Field(
        ...,
        description="List of students"
    )
    total: int = Field(..., description="Total number of students")
    page: int = Field(..., description="Current page number")
    size: int = Field(..., description="Page size")
    pages: int = Field(..., description="Total number of pages")
