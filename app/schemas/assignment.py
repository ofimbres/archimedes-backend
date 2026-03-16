"""Pydantic schemas for assignment validation and serialization."""

from pydantic import BaseModel, Field, ConfigDict
from typing import Optional, Literal
from datetime import datetime
from uuid import UUID

from app.schemas.activity import ActivityResponse


class AssignmentCreate(BaseModel):
    """Schema for creating an assignment."""

    course_id: UUID = Field(
        ...,
        description="Course to assign the activity to",
    )
    activity_id: str = Field(..., min_length=1, max_length=20)
    teacher_id: UUID = Field(
        ...,
        description="Teacher creating the assignment (must own the course)",
    )
    due_date: Optional[datetime] = None
    title_override: Optional[str] = Field(None, max_length=200)


class AssignmentResponse(BaseModel):
    """Schema for assignment responses."""

    model_config = ConfigDict(from_attributes=True)

    id: UUID
    course_id: UUID
    activity_id: str
    assigned_by: UUID
    due_date: Optional[datetime] = None
    title_override: Optional[str] = None
    created_at: datetime

    activity: Optional[ActivityResponse] = None
    course_name: Optional[str] = None


class AssignmentListResponse(BaseModel):
    """Schema for assignment list by course."""

    assignments: list[AssignmentResponse]
    total: int


# ---------------------------------------------------------------------------
# Assignment completions (students who completed an assignment)
# ---------------------------------------------------------------------------


class AssignmentCompletionCreate(BaseModel):
    """Schema for recording that a student completed an assignment."""

    student_id: UUID = Field(
        ...,
        description="Student who completed (must be enrolled in the course)",
    )
    score: Optional[float] = Field(
        None, ge=0, le=100, description="Optional score 0-100"
    )


class AssignmentCompletionResponse(BaseModel):
    """Single completion record with optional student name."""

    model_config = ConfigDict(from_attributes=True)

    id: UUID
    student_id: UUID
    assignment_id: UUID
    completed_at: datetime
    score: Optional[float] = None
    student_name: Optional[str] = None


class AssignmentCompletionListResponse(BaseModel):
    """List of completions for an assignment."""

    completions: list[AssignmentCompletionResponse]
    total: int


class AssignmentStudentStatus(BaseModel):
    """Per-student status for an assignment within a course."""

    student_id: UUID
    student_name: str
    status: Literal["completed", "pending", "past_due"]
    score: Optional[float] = None
    completed_at: Optional[datetime] = None


class AssignmentProgressResponse(BaseModel):
    """Overall progress for an assignment across enrolled students."""

    assignment_id: UUID
    course_id: UUID
    students: list[AssignmentStudentStatus]
    total: int
