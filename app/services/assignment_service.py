"""Assignment business logic and database operations."""

from typing import Optional
from uuid import UUID

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload

from app.models.assignment import Assignment
from app.models.assignment_completion import AssignmentCompletion
from app.models.course import Course
from app.models.activity import Activity
from app.models.subtopic import Subtopic
from app.models.enrollment import Enrollment
from app.schemas.assignment import (
    AssignmentCreate,
    AssignmentResponse,
    AssignmentListResponse,
    AssignmentCompletionCreate,
    AssignmentCompletionResponse,
    AssignmentCompletionListResponse,
    AssignmentProgressResponse,
    AssignmentStudentStatus,
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

    async def list_by_course_for_teacher(
        self,
        course_id: UUID,
        teacher_id: UUID,
        include_activity: bool = True,
    ) -> AssignmentListResponse:
        """List assignments for a course only if the teacher owns that course."""
        course_result = await self.db.execute(
            select(Course).where(Course.id == course_id)
        )
        course = course_result.scalar_one_or_none()
        if not course:
            raise ValueError("Course not found")
        if course.teacher_id != teacher_id:
            raise ValueError("Teacher does not own this course")
        return await self.list_by_course(course_id, include_activity=include_activity)

    async def list_by_teacher(
        self,
        teacher_id: UUID,
        include_activity: bool = True,
    ) -> AssignmentListResponse:
        """List all assignments allocated by a teacher (across their courses)."""
        query = (
            select(Assignment)
            .where(Assignment.assigned_by == teacher_id)
            .order_by(Assignment.created_at.desc())
        )
        if include_activity:
            query = query.options(
                selectinload(Assignment.activity)
                .selectinload(Activity.subtopic)
                .selectinload(Subtopic.topic),
                selectinload(Assignment.course),
            )
        else:
            query = query.options(selectinload(Assignment.course))
        result = await self.db.execute(query)
        assignments = result.scalars().all()

        items = []
        for a in assignments:
            course_name = a.course.course_name if a.course else None
            items.append(
                AssignmentResponse(
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
            )
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

    async def record_completion(
        self,
        assignment_id: UUID,
        data: AssignmentCompletionCreate,
    ) -> AssignmentCompletionResponse:
        """Record that a student completed an assignment.

        Student must be enrolled in the assignment's course.
        """
        assignment = await self.get_assignment(assignment_id)
        if not assignment:
            raise ValueError("Assignment not found")

        # Ensure student is enrolled in the course (active)
        enrollment_result = await self.db.execute(
            select(Enrollment).where(
                Enrollment.student_id == data.student_id,
                Enrollment.course_id == assignment.course_id,
                Enrollment.enrollment_status == "active",
                Enrollment.is_active.is_(True),
            )
        )
        if not enrollment_result.scalar_one_or_none():
            raise ValueError("Student is not enrolled in this course")

        existing = await self.db.execute(
            select(AssignmentCompletion)
            .options(selectinload(AssignmentCompletion.student))
            .where(
                AssignmentCompletion.assignment_id == assignment_id,
                AssignmentCompletion.student_id == data.student_id,
            )
        )
        completion = existing.scalar_one_or_none()
        if completion:
            if data.score is not None:
                completion.score = data.score
                await self.db.commit()
                await self.db.refresh(completion)
            student_name = None
            if completion.student:
                student_name = getattr(completion.student, "full_name", None) or (
                    f"{completion.student.first_name} {completion.student.last_name}"
                )
            return AssignmentCompletionResponse(
                id=completion.id,
                student_id=completion.student_id,
                assignment_id=completion.assignment_id,
                completed_at=completion.completed_at,
                score=float(completion.score) if completion.score is not None else None,
                student_name=student_name,
            )

        completion = AssignmentCompletion(
            student_id=data.student_id,
            assignment_id=assignment_id,
            score=data.score,
        )
        self.db.add(completion)
        await self.db.commit()
        await self.db.refresh(completion)
        # Reload with student for name
        result = await self.db.execute(
            select(AssignmentCompletion)
            .options(selectinload(AssignmentCompletion.student))
            .where(AssignmentCompletion.id == completion.id)
        )
        completion = result.scalar_one_or_none() or completion
        student_name = None
        if completion and completion.student:
            student_name = getattr(completion.student, "full_name", None) or (
                f"{completion.student.first_name} {completion.student.last_name}"
            )
        return AssignmentCompletionResponse(
            id=completion.id,
            student_id=completion.student_id,
            assignment_id=completion.assignment_id,
            completed_at=completion.completed_at,
            score=float(completion.score) if completion.score is not None else None,
            student_name=student_name,
        )

    async def list_completions_for_assignment(
        self,
        assignment_id: UUID,
    ) -> AssignmentCompletionListResponse:
        """List all students who completed an assignment (with student names)."""
        assignment = await self.get_assignment(assignment_id)
        if not assignment:
            raise ValueError("Assignment not found")

        result = await self.db.execute(
            select(AssignmentCompletion)
            .options(selectinload(AssignmentCompletion.student))
            .where(AssignmentCompletion.assignment_id == assignment_id)
            .order_by(AssignmentCompletion.completed_at.desc())
        )
        completions = result.scalars().all()
        items = []
        for c in completions:
            student_name = None
            if c.student:
                student_name = getattr(c.student, "full_name", None) or (
                    f"{c.student.first_name} {c.student.last_name}"
                )
            items.append(
                AssignmentCompletionResponse(
                    id=c.id,
                    student_id=c.student_id,
                    assignment_id=c.assignment_id,
                    completed_at=c.completed_at,
                    score=float(c.score) if c.score is not None else None,
                    student_name=student_name,
                )
            )
        return AssignmentCompletionListResponse(completions=items, total=len(items))

    async def get_assignment_progress(
        self,
        assignment_id: UUID,
    ) -> AssignmentProgressResponse:
        """Return per-student status for an assignment (completed / pending / past_due)."""
        assignment = await self.get_assignment(assignment_id)
        if not assignment:
            raise ValueError("Assignment not found")

        # Load all active enrollments for the course with students
        result = await self.db.execute(
            select(Enrollment)
            .options(selectinload(Enrollment.student))
            .where(
                Enrollment.course_id == assignment.course_id,
                Enrollment.is_active.is_(True),
            )
        )
        enrollments = result.scalars().all()

        # Load all completions for this assignment
        result = await self.db.execute(
            select(AssignmentCompletion).where(
                AssignmentCompletion.assignment_id == assignment_id,
            )
        )
        completions = result.scalars().all()
        completion_by_student: dict[UUID, AssignmentCompletion] = {
            c.student_id: c for c in completions
        }

        from datetime import datetime, timezone

        now = datetime.now(timezone.utc)
        due_date = assignment.due_date

        students: list[AssignmentStudentStatus] = []

        for enrollment in enrollments:
            student = enrollment.student
            if not student:
                # Should not happen, but skip defensively
                continue

            student_id = student.id
            full_name = getattr(student, "full_name", None) or (
                f"{student.first_name} {student.last_name}"
            )

            completion = completion_by_student.get(student_id)
            if completion:
                status: str = "completed"
                score_val = (
                    float(completion.score)
                    if getattr(completion, "score", None) is not None
                    else None
                )
                completed_at = completion.completed_at
            else:
                # No completion; decide pending vs past_due
                if due_date and now > due_date:
                    status = "past_due"
                else:
                    status = "pending"
                score_val = None
                completed_at = None

            students.append(
                AssignmentStudentStatus(
                    student_id=student_id,
                    student_name=full_name,
                    status=status,  # type: ignore[arg-type]
                    score=score_val,
                    completed_at=completed_at,
                )
            )

        return AssignmentProgressResponse(
            assignment_id=assignment.id,
            course_id=assignment.course_id,
            students=students,
            total=len(students),
        )
