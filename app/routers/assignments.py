"""Assignments API router - create and list assignments for courses."""

from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.services.assignment_service import AssignmentService
from app.schemas.assignment import (
    AssignmentCreate,
    AssignmentResponse,
    AssignmentListResponse,
)
from app.schemas.activity import ActivityResponse

router = APIRouter(
    prefix="/assignments",
    tags=["Assignments"],
)


@router.post("", response_model=AssignmentResponse, status_code=201)
async def create_assignment(
    data: AssignmentCreate,
    db: AsyncSession = Depends(get_db),
):
    """
    Create an assignment (teacher assigns an activity to a course).
    teacher_id must be the course owner; frontend gets it from GET /auth/me.
    """
    service = AssignmentService(db)
    try:
        assignment = await service.create_assignment(data, data.teacher_id)
        # Refetch with activity and course for response
        assignment = await service.get_assignment(assignment.id)
        return AssignmentResponse(
            id=assignment.id,
            course_id=assignment.course_id,
            activity_id=assignment.activity_id,
            assigned_by=assignment.assigned_by,
            due_date=assignment.due_date,
            title_override=assignment.title_override,
            created_at=assignment.created_at,
            activity=ActivityResponse.from_activity(assignment.activity) if assignment.activity else None,
            course_name=assignment.course.course_name if assignment.course else None,
        )
    except ValueError as e:
        msg = str(e)
        if "Course not found" in msg:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=msg,
            )
        if "Activity not found" in msg:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=msg,
            )
        if "does not own" in msg:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=msg,
            )
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=msg)


@router.get("/courses/{course_id}", response_model=AssignmentListResponse)
async def list_course_assignments(
    course_id: UUID,
    db: AsyncSession = Depends(get_db),
):
    """List assignments for a course (teacher or enrolled student)."""
    service = AssignmentService(db)
    return await service.list_by_course(course_id)


@router.get("/{assignment_id}", response_model=AssignmentResponse)
async def get_assignment(
    assignment_id: UUID,
    db: AsyncSession = Depends(get_db),
):
    """Get a single assignment by id."""
    service = AssignmentService(db)
    assignment = await service.get_assignment(assignment_id)
    if not assignment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assignment not found",
        )
    return AssignmentResponse(
        id=assignment.id,
        course_id=assignment.course_id,
        activity_id=assignment.activity_id,
        assigned_by=assignment.assigned_by,
        due_date=assignment.due_date,
        title_override=assignment.title_override,
        created_at=assignment.created_at,
        activity=ActivityResponse.from_activity(assignment.activity) if assignment.activity else None,
        course_name=assignment.course.course_name if assignment.course else None,
    )
