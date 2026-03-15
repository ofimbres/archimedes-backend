"""Pydantic schemas for assignment validation and serialization."""

from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from datetime import datetime
from uuid import UUID

from app.schemas.activity import ActivityResponse


class AssignmentCreate(BaseModel):
    """Schema for creating an assignment."""

    course_id: UUID = Field(..., description="Course to assign the activity to")
    activity_id: str = Field(..., min_length=1, max_length=20)
    teacher_id: UUID = Field(..., description="Teacher creating the assignment (must own the course)")
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
