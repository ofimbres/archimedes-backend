"""Pydantic schemas for enrollment validation and serialization."""

from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from datetime import datetime
from uuid import UUID


class EnrollmentBase(BaseModel):
    """Base enrollment schema with common fields."""
    enrollment_status: str = Field(
        "active",
        description="Status of enrollment (active, dropped, completed)"
    )


class EnrollmentCreate(BaseModel):
    """Schema for creating an enrollment via join code."""
    join_code: str = Field(..., min_length=5, max_length=10)


class EnrollmentCreateDirect(BaseModel):
    """Schema for direct enrollment (admin use)."""
    student_id: UUID = Field(..., description="ID of the student")
    course_id: UUID = Field(..., description="ID of the course")
    enrollment_status: str = Field(
        "active",
        description="Status of enrollment"
    )


class EnrollmentUpdate(BaseModel):
    """Schema for updating an enrollment."""
    enrollment_status: Optional[str] = Field(
        None,
        description="Status of enrollment (active, dropped, completed)"
    )
    is_active: Optional[bool] = None


class EnrollmentResponse(BaseModel):
    """Schema for enrollment responses."""
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    student_id: UUID
    course_id: UUID
    enrolled_at: datetime
    enrollment_status: str
    is_active: bool
    created_at: datetime
    updated_at: datetime

    # Nested objects (optional, for detailed responses)
    student_name: Optional[str] = None
    student_email: Optional[str] = None
    course_name: Optional[str] = None
    teacher_name: Optional[str] = None
    teacher_id: Optional[UUID] = None


class EnrollmentListResponse(BaseModel):
    """Schema for paginated enrollment list responses."""
    enrollments: list[EnrollmentResponse]
    total: int
    page: int
    size: int
    total_pages: int


class CourseEnrollmentSummary(BaseModel):
    """Schema for course enrollment summary."""
    course_id: UUID
    course_name: str
    subject: str
    teacher_name: str
    total_enrolled: int
    active_enrolled: int
    join_code: str


class StudentEnrollmentSummary(BaseModel):
    """Schema for student enrollment summary."""
    student_id: UUID
    student_name: str
    email: str
    total_courses: int
    active_courses: int
