"""
Student service layer for business logic.

This service handles all student-related operations with proper validation
and error handling.
"""

import math
from typing import Optional
from uuid import UUID

from sqlalchemy import select, func, or_
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.exc import IntegrityError
from fastapi import HTTPException, status

from ..models.student import Student
from ..schemas.student import StudentCreate, StudentUpdate, StudentResponse


class StudentService:
    """Service layer for student operations."""

    def __init__(self, db: AsyncSession):
        """Initialize service with database session."""
        self.db = db

    async def create_student(
        self, student_data: StudentCreate
    ) -> StudentResponse:
        """Create a new student with validation.

        Args:
            student_data: Student creation data

        Returns:
            Created student response

        Raises:
            HTTPException: If validation fails or email/username exists
        """
        # Check if email already exists
        if await self._email_exists(student_data.email):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email address is already registered"
            )

        # Check if username already exists
        if await self._username_exists(student_data.username):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Username is already taken"
            )

        # Create student
        try:
            # Create student explicitly without computed fields
            student = Student(
                school_id=student_data.school_id,
                first_name=student_data.first_name,
                last_name=student_data.last_name,
                email=student_data.email,
                username=student_data.username,
                cognito_user_id=getattr(
                    student_data, "cognito_user_id", None
                ),
            )
            self.db.add(student)
            await self.db.commit()
            await self.db.refresh(student)

            return StudentResponse.from_orm(student)
        except IntegrityError as e:
            await self.db.rollback()
            if "school_id" in str(e):
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="School not found"
                )
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Failed to create student due to database constraints"
            )

    async def get_student(self, student_id: UUID) -> Optional[StudentResponse]:
        """Get a student by ID.

        Args:
            student_id: UUID of the student

        Returns:
            Student if found, None otherwise
        """
        stmt = select(Student).where(Student.id == student_id)
        result = await self.db.execute(stmt)
        student = result.scalar_one_or_none()

        if student:
            return StudentResponse.from_orm(student)
        return None

    async def get_students_by_school(
        self,
        school_id: UUID,
        page: int = 1,
        size: int = 50,
        search: Optional[str] = None,
        active_only: bool = True
    ) -> dict:
        """Get students for a specific school with pagination.

        Args:
            school_id: UUID of the school
            page: Page number (1-based)
            size: Items per page
            search: Search term for name or email
            active_only: Whether to include only active students

        Returns:
            Dictionary with students list and pagination info
        """
        # Base query
        stmt = select(Student).where(Student.school_id == school_id)

        # Add active filter
        if active_only:
            stmt = stmt.where(Student.is_active.is_(True))

        # Add search filter
        if search:
            search_term = f"%{search.lower()}%"
            stmt = stmt.where(
                or_(
                    func.lower(Student.full_name).contains(search_term),
                    func.lower(Student.email).contains(search_term),
                    func.lower(Student.username).contains(search_term)
                )
            )

        # Get total count
        count_stmt = select(func.count(Student.id)).select_from(
            stmt.subquery()
        )
        total_result = await self.db.execute(count_stmt)
        total = total_result.scalar()

        # Add pagination
        offset = (page - 1) * size
        stmt = stmt.order_by(Student.full_name).offset(offset).limit(size)

        # Execute query
        result = await self.db.execute(stmt)
        students = result.scalars().all()

        return {
            "students": [StudentResponse.from_orm(s) for s in students],
            "total": total,
            "page": page,
            "size": size,
            "pages": math.ceil(total / size) if total > 0 else 0
        }

    async def update_student(
        self,
        student_id: UUID,
        student_data: StudentUpdate
    ) -> Optional[StudentResponse]:
        """Update a student.

        Args:
            student_id: UUID of the student
            student_data: Updated student data

        Returns:
            Updated student if found, None otherwise

        Raises:
            HTTPException: If email/username conflicts exist
        """
        # Get existing student
        stmt = select(Student).where(Student.id == student_id)
        result = await self.db.execute(stmt)
        student = result.scalar_one_or_none()

        if not student:
            return None

        # Check for email conflicts (if email is being updated)
        if (student_data.email and
                student_data.email != student.email and
                await self._email_exists(student_data.email)):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Email address is already registered"
            )

        # Check for username conflicts (if username is being updated)
        if (student_data.username and
                student_data.username != student.username and
                await self._username_exists(student_data.username)):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Username is already taken"
            )

        # Update fields
        update_data = student_data.dict(exclude_unset=True)
        for field, value in update_data.items():
            setattr(student, field, value)

        try:
            await self.db.commit()
            await self.db.refresh(student)
            return StudentResponse.from_orm(student)
        except IntegrityError:
            await self.db.rollback()
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Failed to update student due to database constraints"
            )

    async def delete_student(self, student_id: UUID) -> bool:
        """Soft delete a student (set is_active = False).

        Args:
            student_id: UUID of the student

        Returns:
            True if student was found and deleted, False otherwise
        """
        stmt = select(Student).where(Student.id == student_id)
        result = await self.db.execute(stmt)
        student = result.scalar_one_or_none()

        if not student:
            return False

        student.is_active = False
        await self.db.commit()
        return True

    async def permanently_delete_student(self, student_id: UUID) -> bool:
        """Permanently delete a student from database.

        Args:
            student_id: UUID of the student

        Returns:
            True if student was found and deleted, False otherwise
        """
        stmt = select(Student).where(Student.id == student_id)
        result = await self.db.execute(stmt)
        student = result.scalar_one_or_none()

        if not student:
            return False

        await self.db.delete(student)
        await self.db.commit()
        return True

    async def _email_exists(self, email: str) -> bool:
        """Check if email is already registered.

        Args:
            email: Email to check

        Returns:
            True if email exists, False otherwise
        """
        stmt = select(Student).where(
            func.lower(Student.email) == email.lower())
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none() is not None

    async def _username_exists(self, username: str) -> bool:
        """Check if username is already taken.

        Args:
            username: Username to check

        Returns:
            True if username exists, False otherwise
        """
        stmt = select(Student).where(
            func.lower(Student.username) == username.lower()
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none() is not None
