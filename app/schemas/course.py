"""Pydantic schemas for course validation and serialization."""

from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from datetime import datetime
from uuid import UUID


class CourseBase(BaseModel):
    """Base course schema with common fields."""
    class_name: str = Field(..., min_length=1, max_length=100)
    subject: str = Field("General", min_length=1, max_length=50)
    academic_year: str = Field("2024-25", max_length=10)
    semester: str = Field("Fall", max_length=20)


class CourseCreate(BaseModel):
    """Schema for creating a course. School is derived from the teacher."""
    teacher_id: UUID = Field(..., description="Teacher ID (school from their profile)")
    class_name: str = Field(..., min_length=1, max_length=100, description="Course name")
    subject: str = Field("General", min_length=1, max_length=50)
    academic_year: str = Field("2024-25", max_length=10)
    semester: str = Field("Fall", max_length=20)


class CourseUpdate(BaseModel):
    """Schema for updating a course (all optional fields)."""
    class_name: Optional[str] = Field(None, min_length=1, max_length=100)
    subject: Optional[str] = Field(None, min_length=1, max_length=50)
    academic_year: Optional[str] = Field(None, max_length=10)
    semester: Optional[str] = Field(None, max_length=20)
    is_active: Optional[bool] = None


class CourseResponse(CourseBase):
    """Schema for course responses."""
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    school_id: UUID
    teacher_id: UUID
    join_code: str
    is_active: bool
    created_at: datetime
    updated_at: datetime

    # Nested objects (optional, for detailed responses)
    teacher_name: Optional[str] = None
    school_name: Optional[str] = None
    enrolled_count: Optional[int] = None


class CourseListResponse(BaseModel):
    """Schema for paginated course list responses."""
    courses: list[CourseResponse]
    total: int
    page: int
    size: int
    total_pages: int
