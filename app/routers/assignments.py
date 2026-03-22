"""Assignments API router - create and list assignments for courses."""

from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.dependencies.auth import (
    CurrentUserClaims,
    get_current_user_claims,
)
from app.services.assignment_service import AssignmentService
from app.models.student import Student
from app.services.course_assignments_access import (
    assert_teacher_or_admin_assignment_progress,
    resolve_course_assignments_list_access,
)
from app.schemas.assignment import (
    AssignmentCreate,
    AssignmentResponse,
    AssignmentListResponse,
    AssignmentCompletionCreate,
    AssignmentCompletionResponse,
    AssignmentCompletionListResponse,
    AssignmentProgressResponse,
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
            activity=(
                ActivityResponse.from_activity(assignment.activity)
                if assignment.activity
                else None
            ),
            course_name=(
                assignment.course.course_name if assignment.course else None
            ),
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
    claims: CurrentUserClaims = Depends(get_current_user_claims),
):
    """List assignments for a course.

    **Authorization:** Bearer (Cognito **ID** or **access** token). Caller must be
    an enrolled active **student** in the course, the course-owning **teacher**, or
    a configured platform **admin**.

    For students, each assignment includes nested **activity** (``activity_id``,
    ``description``, ``content_url`` when configured) and ``my_completed_at`` /
    ``my_score`` when applicable.
    """
    viewer_student_id = await resolve_course_assignments_list_access(
        db, course_id, claims
    )
    service = AssignmentService(db)
    return await service.list_by_course(
        course_id,
        viewer_student_id=viewer_student_id,
    )


@router.get("/teachers/{teacher_id}", response_model=AssignmentListResponse)
async def list_teacher_assignments(
    teacher_id: UUID,
    db: AsyncSession = Depends(get_db),
):
    """List all assignments allocated by a teacher (across their courses)."""
    service = AssignmentService(db)
    return await service.list_by_teacher(teacher_id)


@router.get(
    "/{assignment_id}/completions",
    response_model=AssignmentCompletionListResponse,
)
async def list_assignment_completions(
    assignment_id: UUID,
    db: AsyncSession = Depends(get_db),
):
    """List students who have completed this assignment."""
    service = AssignmentService(db)
    try:
        return await service.list_completions_for_assignment(assignment_id)
    except ValueError as e:
        if "not found" in str(e).lower():
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=str(e),
            )
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


@router.get("/{assignment_id}/progress", response_model=AssignmentProgressResponse)
async def get_assignment_progress(
    assignment_id: UUID,
    db: AsyncSession = Depends(get_db),
    claims: CurrentUserClaims = Depends(get_current_user_claims),
):
    """
    Per-student status for the assignment roster (pending / completed / past_due),
    optional score and completed_at.

    **Authorization:** Bearer. Course-owning **teacher** or platform **admin** only.
    """
    await assert_teacher_or_admin_assignment_progress(db, assignment_id, claims)
    service = AssignmentService(db)
    try:
        return await service.get_assignment_progress(assignment_id)
    except ValueError as e:
        msg = str(e)
        if "Assignment not found" in msg:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=msg,
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=msg,
        )


@router.post(
    "/{assignment_id}/completions",
    response_model=AssignmentCompletionResponse,
    status_code=201,
)
async def record_assignment_completion(
    assignment_id: UUID,
    data: AssignmentCompletionCreate,
    db: AsyncSession = Depends(get_db),
    claims: CurrentUserClaims = Depends(get_current_user_claims),
):
    """Record that a student completed an assignment.

    Caller must present ``Authorization: Bearer`` for a Cognito user linked to
    a **student** row; ``student_id`` in the body must match that profile.
    The student must be enrolled in the assignment's course.
    """
    linked = await db.execute(
        select(Student.id).where(Student.cognito_user_id == claims.sub)
    )
    linked_student_id = linked.scalar_one_or_none()
    if linked_student_id is None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Authenticated user is not linked to a student profile",
        )
    if linked_student_id != data.student_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="student_id does not match authenticated student",
        )
    service = AssignmentService(db)
    try:
        return await service.record_completion(assignment_id, data)
    except ValueError as e:
        msg = str(e)
        if "Assignment not found" in msg:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=msg)
        if "not enrolled" in msg:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=msg)
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=msg)


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
