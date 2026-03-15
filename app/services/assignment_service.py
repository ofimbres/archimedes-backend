"""Assignment business logic and database operations."""

from typing import Optional
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload

from app.models.assignment import Assignment
from app.models.course import Course
from app.models.activity import Activity
from app.models.subtopic import Subtopic
from app.schemas.assignment import (
    AssignmentCreate,
    AssignmentResponse,
    AssignmentListResponse,
)
from app.schemas.activity import ActivityResponse


class AssignmentService:
    """Service for assignment operations (create, list by course)."""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def create_assignment(
        self,
        data: AssignmentCreate,
        teacher_id: UUID,
    ) -> Assignment:
        """Create an assignment. Teacher must own the course."""
        course_result = await self.db.execute(
            select(Course).where(Course.id == data.course_id)
        )
        course = course_result.scalar_one_or_none()
        if not course:
            raise ValueError("Course not found")
        if course.teacher_id != teacher_id:
            raise ValueError("Teacher does not own this course")

        activity_result = await self.db.execute(
            select(Activity).where(Activity.activity_id == data.activity_id)
        )
        if not activity_result.scalar_one_or_none():
            raise ValueError("Activity not found")

        assignment = Assignment(
            course_id=data.course_id,
            activity_id=data.activity_id,
            assigned_by=teacher_id,
            due_date=data.due_date,
            title_override=data.title_override,
        )
        self.db.add(assignment)
        await self.db.commit()
        await self.db.refresh(assignment)
        return assignment

    async def list_by_course(
        self,
        course_id: UUID,
        include_activity: bool = True,
    ) -> AssignmentListResponse:
        """List assignments for a course."""
        query = (
            select(Assignment)
            .where(Assignment.course_id == course_id)
            .order_by(Assignment.created_at.desc())
        )
        if include_activity:
            query = query.options(
                selectinload(Assignment.activity)
                .selectinload(Activity.subtopic)
                .selectinload(Subtopic.topic),
            )
        result = await self.db.execute(query)
        assignments = result.scalars().all()

        course_result = await self.db.execute(
            select(Course).where(Course.id == course_id)
        )
        course = course_result.scalar_one_or_none()
        course_name = course.course_name if course else None

        items = []
        for a in assignments:
            resp = AssignmentResponse(
                id=a.id,
                course_id=a.course_id,
                activity_id=a.activity_id,
                assigned_by=a.assigned_by,
                due_date=a.due_date,
                title_override=a.title_override,
                created_at=a.created_at,
                activity=(
                    ActivityResponse.from_activity(a.activity)
                    if a.activity
                    else None
                ),
                course_name=course_name,
            )
            items.append(resp)

        return AssignmentListResponse(assignments=items, total=len(items))

    async def get_assignment(self, assignment_id: UUID) -> Optional[Assignment]:
        """Get a single assignment by id (activity loaded with subtopic and topic)."""
        result = await self.db.execute(
            select(Assignment)
            .options(
                selectinload(Assignment.activity)
                .selectinload(Activity.subtopic)
                .selectinload(Subtopic.topic),
                selectinload(Assignment.course),
            )
            .where(Assignment.id == assignment_id)
        )
        return result.scalar_one_or_none()

    async def delete_assignment(
        self,
        assignment_id: UUID,
        teacher_id: UUID,
    ) -> bool:
        """Delete an assignment. Teacher must own the course."""
        assignment = await self.get_assignment(assignment_id)
        if not assignment:
            return False
        if assignment.assigned_by != teacher_id:
            raise ValueError(
                "Not allowed to delete this assignment",
            )
        await self.db.delete(assignment)
        await self.db.commit()
        return True
