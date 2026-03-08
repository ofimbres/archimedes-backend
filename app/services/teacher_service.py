"""Teacher business logic and database operations."""

from typing import Optional
from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from sqlalchemy.orm import selectinload

from app.models.teacher import Teacher
from app.models.school import School
from app.schemas.teacher import (
    TeacherCreate,
    TeacherUpdate,
    TeacherResponse,
    TeacherListResponse
)
from app.services.course_service import CourseService


class TeacherService:
    """Service class for teacher business logic."""

    def __init__(self, db: AsyncSession):
        """Initialize with database session."""
        self.db = db

    async def create_teacher(self, teacher_data: TeacherCreate) -> Teacher:
        """Create a new teacher."""
        # Verify school exists
        school_result = await self.db.execute(
            select(School).where(School.id == teacher_data.school_id)
        )
        school = school_result.scalar_one_or_none()
        if not school:
            raise ValueError("School not found")

        # Check if email already exists
        email_result = await self.db.execute(
            select(Teacher).where(Teacher.email == teacher_data.email)
        )
        if email_result.scalar_one_or_none():
            raise ValueError("Teacher with this email already exists")

        # Check if username already exists
        username_result = await self.db.execute(
            select(Teacher).where(Teacher.username == teacher_data.username)
        )
        if username_result.scalar_one_or_none():
            raise ValueError("Teacher with this username already exists")

        # Create teacher
        teacher = Teacher(**teacher_data.model_dump())
        self.db.add(teacher)
        await self.db.commit()
        await self.db.refresh(teacher)

        # Create default courses based on max_classes
        await self._create_default_courses(teacher)

        return teacher

    async def _create_default_courses(self, teacher: Teacher) -> None:
        """Create default courses for a new teacher."""
        course_service = CourseService(self.db)

        # Create courses up to max_classes limit (default 6)
        max_courses = teacher.max_classes or 6

        for i in range(1, max_courses + 1):
            try:
                await course_service.create_default_course(
                    teacher_id=teacher.id,
                    course_number=i
                )
            except Exception:
                # If course creation fails, continue with others
                # This prevents failure of teacher creation
                continue

    async def get_teacher_by_id(self, teacher_id: UUID) -> Optional[Teacher]:
        """Get teacher by ID with school and courses relationship."""
        result = await self.db.execute(
            select(Teacher)
            .options(
                selectinload(Teacher.school),
                selectinload(Teacher.courses)
            )
            .where(Teacher.id == teacher_id)
        )
        return result.scalar_one_or_none()

    async def get_teacher_by_email(self, email: str) -> Optional[Teacher]:
        """Get teacher by email."""
        result = await self.db.execute(
            select(Teacher).where(Teacher.email == email)
        )
        return result.scalar_one_or_none()

    async def get_teacher_by_username(self, username: str) -> Optional[Teacher]:
        """Get teacher by username."""
        result = await self.db.execute(
            select(Teacher).where(Teacher.username == username)
        )
        return result.scalar_one_or_none()

    async def get_teachers(
        self,
        school_id: Optional[UUID] = None,
        is_active: Optional[bool] = None,
        page: int = 1,
        size: int = 10
    ) -> TeacherListResponse:
        """Get paginated list of teachers with optional filters."""
        # Build base query
        query = select(Teacher).options(selectinload(Teacher.school))

        # Apply filters
        if school_id:
            query = query.where(Teacher.school_id == school_id)
        if is_active is not None:
            query = query.where(Teacher.is_active == is_active)

        # Get total count
        count_query = select(func.count(Teacher.id))
        if school_id:
            count_query = count_query.where(Teacher.school_id == school_id)
        if is_active is not None:
            count_query = count_query.where(Teacher.is_active == is_active)

        total_result = await self.db.execute(count_query)
        total = total_result.scalar()

        # Apply pagination
        offset = (page - 1) * size
        query = query.offset(offset).limit(size)

        # Execute query
        result = await self.db.execute(query)
        teachers = result.scalars().all()

        # Calculate total pages
        total_pages = (total + size - 1) // size

        return TeacherListResponse(
            teachers=[TeacherResponse.model_validate(t) for t in teachers],
            total=total,
            page=page,
            size=size,
            total_pages=total_pages
        )

    async def update_teacher(
        self,
        teacher_id: UUID,
        teacher_data: TeacherUpdate
    ) -> Optional[Teacher]:
        """Update teacher information."""
        teacher = await self.get_teacher_by_id(teacher_id)
        if not teacher:
            return None

        # Check for email conflicts if email is being updated
        if teacher_data.email and teacher_data.email != teacher.email:
            existing_teacher = await self.get_teacher_by_email(
                teacher_data.email
            )
            if existing_teacher:
                raise ValueError("Teacher with this email already exists")

        # Check for username conflicts if username is being updated
        if teacher_data.username and teacher_data.username != teacher.username:
            existing_teacher = await self.get_teacher_by_username(
                teacher_data.username
            )
            if existing_teacher:
                raise ValueError("Teacher with this username already exists")

        # Update fields
        update_data = teacher_data.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(teacher, field, value)

        await self.db.commit()
        await self.db.refresh(teacher)
        return teacher

    async def delete_teacher(self, teacher_id: UUID) -> bool:
        """Delete a teacher."""
        teacher = await self.get_teacher_by_id(teacher_id)
        if not teacher:
            return False

        await self.db.delete(teacher)
        await self.db.commit()
        return True

    async def get_teachers_by_school(
        self,
        school_id: UUID,
        page: int = 1,
        size: int = 10
    ) -> TeacherListResponse:
        """Get teachers for a specific school."""
        return await self.get_teachers(
            school_id=school_id,
            page=page,
            size=size
        )
