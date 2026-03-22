"""Authorization helpers for course-scoped assignment lists."""

from typing import Optional
from uuid import UUID

from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.dependencies.auth import CurrentUserClaims, claims_is_platform_admin
from sqlalchemy.orm import selectinload

from app.models.assignment import Assignment
from app.models.course import Course
from app.models.enrollment import Enrollment
from app.models.student import Student
from app.models.teacher import Teacher


async def resolve_course_assignments_list_access(
    db: AsyncSession,
    course_id: UUID,
    claims: CurrentUserClaims,
) -> Optional[UUID]:
    """
    Who may call GET /assignments/courses/{course_id}:

    - Enrolled active student -> returns that student's UUID (for my_* fields).
    - Course owner (teacher) -> returns None.
    - Platform admin -> returns None.
    - Otherwise -> 403.
    """
    if claims_is_platform_admin(claims):
        course_row = await db.execute(select(Course.id).where(Course.id == course_id))
        if course_row.scalar_one_or_none() is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Course not found",
            )
        return None

    sid = (
        await db.execute(
            select(Student.id).where(Student.cognito_user_id == claims.sub)
        )
    ).scalar_one_or_none()
    if sid is not None:
        enr = await db.execute(
            select(Enrollment.id).where(
                Enrollment.student_id == sid,
                Enrollment.course_id == course_id,
                Enrollment.enrollment_status == "active",
                Enrollment.is_active.is_(True),
            )
        )
        if enr.scalar_one_or_none() is None:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not enrolled in this course",
            )
        return sid

    tid = (
        await db.execute(
            select(Teacher.id).where(Teacher.cognito_user_id == claims.sub)
        )
    ).scalar_one_or_none()
    if tid is not None:
        course_result = await db.execute(
            select(Course).where(Course.id == course_id)
        )
        course = course_result.scalar_one_or_none()
        if course is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Course not found",
            )
        if course.teacher_id != tid:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not allowed to view assignments for this course",
            )
        return None

    raise HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail="Not allowed to view assignments for this course",
    )


async def assert_teacher_or_admin_assignment_progress(
    db: AsyncSession,
    assignment_id: UUID,
    claims: CurrentUserClaims,
) -> None:
    """Teacher who owns the assignment's course, or platform admin, may view progress."""
    if claims_is_platform_admin(claims):
        a = await db.execute(select(Assignment.id).where(Assignment.id == assignment_id))
        if a.scalar_one_or_none() is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Assignment not found",
            )
        return

    tid = (
        await db.execute(
            select(Teacher.id).where(Teacher.cognito_user_id == claims.sub)
        )
    ).scalar_one_or_none()
    if tid is None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Teacher or admin access required",
        )

    result = await db.execute(
        select(Assignment)
        .options(selectinload(Assignment.course))
        .where(Assignment.id == assignment_id)
    )
    assignment = result.scalar_one_or_none()
    if assignment is None or assignment.course is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Assignment not found",
        )
    if assignment.course.teacher_id != tid:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not allowed to view progress for this assignment",
        )
