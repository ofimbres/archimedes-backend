"""Pydantic schemas for teacher validation and serialization."""

from pydantic import BaseModel, Field, EmailStr, ConfigDict
from typing import Optional
from datetime import datetime
from uuid import UUID


class TeacherBase(BaseModel):
    """Base teacher schema with common fields."""
    first_name: str = Field(..., min_length=1, max_length=50)
    last_name: str = Field(..., min_length=1, max_length=50)
    email: EmailStr
    username: str = Field(..., min_length=1, max_length=50)


class TeacherCreate(TeacherBase):
    """Schema for creating a teacher."""
    school_id: UUID = Field(..., description="ID of the school")
    cognito_user_id: Optional[str] = Field(
        None, description="Cognito sub for OAuth users; links identity to this record"
    )


class TeacherUpdate(BaseModel):
    """Schema for updating a teacher (all optional fields)."""
    first_name: Optional[str] = Field(None, min_length=1, max_length=50)
    last_name: Optional[str] = Field(None, min_length=1, max_length=50)
    email: Optional[EmailStr] = None
    username: Optional[str] = Field(None, min_length=1, max_length=50)
    max_classes: Optional[int] = Field(None, ge=1, le=20)
    is_active: Optional[bool] = None


class TeacherResponse(TeacherBase):
    """Schema for teacher responses."""
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    school_id: UUID
    full_name: str
    max_classes: int
    is_active: bool
    created_at: datetime
    updated_at: datetime


class TeacherListResponse(BaseModel):
    """Schema for paginated teacher list responses."""
    teachers: list[TeacherResponse]
    total: int
    page: int
    size: int
    total_pages: int
