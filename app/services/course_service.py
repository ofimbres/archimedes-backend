"""Course business logic and database operations."""

import random
import string
from typing import Optional
from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from sqlalchemy.orm import selectinload

from app.models.course import Course
from app.models.teacher import Teacher

from app.schemas.course import (
    CourseCreate,
    CourseUpdate,
    CourseResponse,
    CourseListResponse
)


class CourseService:
    """Service class for course business logic."""

    def __init__(self, db: AsyncSession):
        """Initialize with database session."""
        self.db = db

    async def _generate_unique_join_code(self) -> str:
        """Generate a unique join code for the course."""
        while True:
            # Generate a 5-character alphanumeric code
            code = ''.join(random.choices(
                string.ascii_uppercase + string.digits, k=5
            ))

            # Check if it already exists
            result = await self.db.execute(
                select(Course).where(Course.join_code == code)
            )
            if not result.scalar_one_or_none():
                return code

    async def create_course(self, course_data: CourseCreate) -> Course:
        """Create a new course."""
        # Verify teacher exists
        teacher_result = await self.db.execute(
            select(Teacher).where(Teacher.id == course_data.teacher_id)
        )
        teacher = teacher_result.scalar_one_or_none()
        if not teacher:
            raise ValueError("Teacher not found")

        # Verify school exists and matches teacher's school
        if teacher.school_id != course_data.school_id:
            raise ValueError(
                "Teacher and course must belong to the same school"
            )

        # Generate unique join code
        join_code = await self._generate_unique_join_code()

        # Create course
        course_dict = course_data.model_dump()
        course_dict['join_code'] = join_code
        course_dict['is_active'] = True

        course = Course(**course_dict)
        self.db.add(course)
        await self.db.commit()
        await self.db.refresh(course)

        return course

    async def create_default_course(
        self,
        teacher_id: UUID,
        school_id: UUID,
        course_number: int = 1
    ) -> Course:
        """Create a default course for a new teacher."""
        default_course = CourseCreate(
            school_id=school_id,
            teacher_id=teacher_id,
            class_name=f"Course {course_number}",
            subject="General",
            academic_year="2024-25",
            semester="Fall"
        )
        return await self.create_course(default_course)

    async def get_course_by_id(self, course_id: UUID) -> Optional[Course]:
        """Get course by ID with relationships."""
        result = await self.db.execute(
            select(Course)
            .options(
                selectinload(Course.teacher),
                selectinload(Course.school)
            )
            .where(Course.id == course_id)
        )
        return result.scalar_one_or_none()

    async def get_course_by_join_code(
        self, join_code: str
    ) -> Optional[Course]:
        """Get course by join code."""
        result = await self.db.execute(
            select(Course)
            .options(
                selectinload(Course.teacher),
                selectinload(Course.school)
            )
            .where(Course.join_code == join_code)
        )
        return result.scalar_one_or_none()

    async def get_teacher_courses(
        self,
        teacher_id: UUID,
        is_active: Optional[bool] = None,
        page: int = 1,
        size: int = 10
    ) -> CourseListResponse:
        """Get courses for a specific teacher."""
        query = (
            select(Course)
            .options(selectinload(Course.school))
            .where(Course.teacher_id == teacher_id)
        )

        if is_active is not None:
            query = query.where(Course.is_active == is_active)

        # Get total count
        count_query = (
            select(func.count(Course.id))
            .where(Course.teacher_id == teacher_id)
        )
        if is_active is not None:
            count_query = count_query.where(Course.is_active == is_active)

        total_result = await self.db.execute(count_query)
        total = total_result.scalar()

        # Apply pagination
        offset = (page - 1) * size
        query = query.offset(offset).limit(size)

        result = await self.db.execute(query)
        courses = result.scalars().all()

        total_pages = (total + size - 1) // size

        return CourseListResponse(
            courses=[CourseResponse.model_validate(c) for c in courses],
            total=total,
            page=page,
            size=size,
            total_pages=total_pages
        )

    async def update_course(
        self,
        course_id: UUID,
        course_data: CourseUpdate
    ) -> Optional[Course]:
        """Update course information."""
        course = await self.get_course_by_id(course_id)
        if not course:
            return None

        # Update fields
        update_data = course_data.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(course, field, value)

        await self.db.commit()
        await self.db.refresh(course)
        return course

    async def delete_course(self, course_id: UUID) -> bool:
        """Delete a course."""
        course = await self.get_course_by_id(course_id)
        if not course:
            return False

        await self.db.delete(course)
        await self.db.commit()
        return True
