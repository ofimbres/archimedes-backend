"""Enrollment business logic and database operations."""

from typing import Optional
from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, and_
from sqlalchemy.orm import selectinload

from app.models.enrollment import Enrollment
from app.models.course import Course
from app.models.student import Student
from app.schemas.enrollment import (
    EnrollmentCreate,
    EnrollmentCreateDirect,
    EnrollmentUpdate,
    EnrollmentResponse,
    EnrollmentListResponse
)


class EnrollmentService:
    """Service class for enrollment business logic."""

    def __init__(self, db: AsyncSession):
        """Initialize with database session."""
        self.db = db

    async def enroll_with_join_code(
        self,
        student_id: UUID,
        enrollment_data: EnrollmentCreate
    ) -> Enrollment:
        """Enroll a student using a join code."""
        # Find the course by join code
        course_result = await self.db.execute(
            select(Course).where(
                and_(
                    Course.join_code == enrollment_data.join_code,
                    Course.is_active.is_(True)
                )
            )
        )
        course = course_result.scalar_one_or_none()
        if not course:
            raise ValueError("Invalid join code or course is not active")

        # Check if student is already enrolled
        existing_enrollment = await self.db.execute(
            select(Enrollment).where(
                and_(
                    Enrollment.student_id == student_id,
                    Enrollment.class_id == course.id
                )
            )
        )
        if existing_enrollment.scalar_one_or_none():
            raise ValueError("Student is already enrolled in this course")

        # Create enrollment
        enrollment = Enrollment(
            student_id=student_id,
            class_id=course.id,
            enrollment_status="active",
            is_active=True
        )

        self.db.add(enrollment)
        await self.db.commit()
        await self.db.refresh(enrollment)

        return enrollment

    async def create_direct_enrollment(
        self,
        enrollment_data: EnrollmentCreateDirect
    ) -> Enrollment:
        """Create a direct enrollment (admin use)."""
        # Verify student exists
        student_result = await self.db.execute(
            select(Student).where(Student.id == enrollment_data.student_id)
        )
        if not student_result.scalar_one_or_none():
            raise ValueError("Student not found")

        # Verify course exists
        course_result = await self.db.execute(
            select(Course).where(Course.id == enrollment_data.course_id)
        )
        if not course_result.scalar_one_or_none():
            raise ValueError("Course not found")

        # Check if enrollment already exists
        existing_enrollment = await self.db.execute(
            select(Enrollment).where(
                and_(
                    Enrollment.student_id == enrollment_data.student_id,
                    Enrollment.class_id == enrollment_data.course_id
                )
            )
        )
        if existing_enrollment.scalar_one_or_none():
            raise ValueError("Student is already enrolled in this course")

        # Create enrollment
        enrollment = Enrollment(
            student_id=enrollment_data.student_id,
            class_id=enrollment_data.course_id,
            enrollment_status=enrollment_data.enrollment_status,
            is_active=True
        )

        self.db.add(enrollment)
        await self.db.commit()
        await self.db.refresh(enrollment)

        return enrollment

    async def get_enrollment_by_id(
        self, enrollment_id: UUID
    ) -> Optional[Enrollment]:
        """Get enrollment by ID with relationships."""
        result = await self.db.execute(
            select(Enrollment)
            .options(
                selectinload(Enrollment.student),
                selectinload(Enrollment.course).selectinload(Course.teacher)
            )
            .where(Enrollment.id == enrollment_id)
        )
        return result.scalar_one_or_none()

    async def get_student_enrollments(
        self,
        student_id: UUID,
        is_active: Optional[bool] = None,
        page: int = 1,
        size: int = 10
    ) -> EnrollmentListResponse:
        """Get enrollments for a specific student."""
        query = (
            select(Enrollment)
            .options(
                selectinload(Enrollment.student),
                selectinload(Enrollment.course).selectinload(Course.teacher)
            )
            .where(Enrollment.student_id == student_id)
        )

        if is_active is not None:
            query = query.where(Enrollment.is_active == is_active)

        # Get total count
        count_query = (
            select(func.count(Enrollment.id))
            .where(Enrollment.student_id == student_id)
        )
        if is_active is not None:
            count_query = count_query.where(Enrollment.is_active == is_active)

        total_result = await self.db.execute(count_query)
        total = total_result.scalar()

        # Apply pagination
        offset = (page - 1) * size
        query = query.offset(offset).limit(size)

        result = await self.db.execute(query)
        enrollments = result.scalars().all()

        total_pages = (total + size - 1) // size

        # Build response with nested data
        enrollment_responses = []
        for enrollment in enrollments:
            response = EnrollmentResponse.model_validate(enrollment)
            response.student_name = enrollment.student.full_name
            response.course_name = enrollment.course.class_name
            response.teacher_name = enrollment.course.teacher.full_name
            response.teacher_id = enrollment.course.teacher_id
            response.course_id = enrollment.class_id  # Alias for consistency
            enrollment_responses.append(response)

        return EnrollmentListResponse(
            enrollments=enrollment_responses,
            total=total,
            page=page,
            size=size,
            total_pages=total_pages
        )

    async def get_course_enrollments(
        self,
        course_id: UUID,
        is_active: Optional[bool] = None,
        page: int = 1,
        size: int = 10
    ) -> EnrollmentListResponse:
        """Get enrollments for a specific course."""
        query = (
            select(Enrollment)
            .options(selectinload(Enrollment.student))
            .where(Enrollment.class_id == course_id)
        )

        if is_active is not None:
            query = query.where(Enrollment.is_active == is_active)

        # Get total count
        count_query = (
            select(func.count(Enrollment.id))
            .where(Enrollment.class_id == course_id)
        )
        if is_active is not None:
            count_query = count_query.where(Enrollment.is_active == is_active)

        total_result = await self.db.execute(count_query)
        total = total_result.scalar()

        # Apply pagination
        offset = (page - 1) * size
        query = query.offset(offset).limit(size)

        result = await self.db.execute(query)
        enrollments = result.scalars().all()

        total_pages = (total + size - 1) // size

        # Build response with nested data
        enrollment_responses = []
        for enrollment in enrollments:
            response = EnrollmentResponse.model_validate(enrollment)
            response.student_name = enrollment.student.full_name
            enrollment_responses.append(response)

        return EnrollmentListResponse(
            enrollments=enrollment_responses,
            total=total,
            page=page,
            size=size,
            total_pages=total_pages
        )

    async def update_enrollment(
        self,
        enrollment_id: UUID,
        enrollment_data: EnrollmentUpdate
    ) -> Optional[Enrollment]:
        """Update enrollment information."""
        enrollment = await self.get_enrollment_by_id(enrollment_id)
        if not enrollment:
            return None

        # Update fields
        update_data = enrollment_data.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(enrollment, field, value)

        await self.db.commit()
        await self.db.refresh(enrollment)
        return enrollment

    async def drop_enrollment(
        self, enrollment_id: UUID
    ) -> Optional[Enrollment]:
        """Drop an enrollment (set to inactive and status to dropped)."""
        enrollment = await self.get_enrollment_by_id(enrollment_id)
        if not enrollment:
            return None

        enrollment.is_active = False
        enrollment.enrollment_status = "dropped"

        await self.db.commit()
        await self.db.refresh(enrollment)
        return enrollment

    async def delete_enrollment(self, enrollment_id: UUID) -> bool:
        """Permanently delete an enrollment."""
        enrollment = await self.get_enrollment_by_id(enrollment_id)
        if not enrollment:
            return False

        await self.db.delete(enrollment)
        await self.db.commit()
        return True
