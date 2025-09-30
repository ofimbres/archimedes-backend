"""
Student API router for Archimedes Education Platform.

This router handles all student-related endpoints with proper validation,
error handling, and response formatting.
"""

from typing import Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from ..database import get_db
from ..schemas.student import (
    StudentCreate,
    StudentUpdate,
    StudentResponse,
    StudentList
)
from ..services.student_service import StudentService

router = APIRouter(prefix="/students", tags=["Students"])


@router.post(
    "/",
    response_model=StudentResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create a new student",
    description="Create a new student in the system with validation."
)
async def create_student(
    student_data: StudentCreate,
    db: AsyncSession = Depends(get_db)
) -> StudentResponse:
    """Create a new student.

    Args:
        student_data: Student creation data
        db: Database session dependency

    Returns:
        Created student response

    Raises:
        409: If email or username already exists
        404: If school not found
        400: If validation fails
    """
    service = StudentService(db)
    return await service.create_student(student_data)


@router.get(
    "/{student_id}",
    response_model=StudentResponse,
    summary="Get student by ID",
    description="Retrieve a specific student by their UUID."
)
async def get_student(
    student_id: UUID,
    db: AsyncSession = Depends(get_db)
) -> StudentResponse:
    """Get a student by ID.

    Args:
        student_id: UUID of the student
        db: Database session dependency

    Returns:
        Student response

    Raises:
        404: If student not found
    """
    service = StudentService(db)
    student = await service.get_student(student_id)

    if not student:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found"
        )

    return student


@router.get(
    "/school/{school_id}",
    response_model=StudentList,
    summary="Get students by school",
    description="Get paginated list of students for a specific school."
)
async def get_students_by_school(
    school_id: UUID,
    page: int = Query(1, ge=1, description="Page number (1-based)"),
    size: int = Query(50, ge=1, le=100, description="Items per page"),
    search: Optional[str] = Query(
        None, description="Search term for name, email, or username"
    ),
    active_only: bool = Query(
        True, description="Include only active students"
    ),
    db: AsyncSession = Depends(get_db)
) -> StudentList:
    """Get students for a specific school with pagination and search.

    Args:
        school_id: UUID of the school
        page: Page number (1-based)
        size: Items per page (max 100)
        search: Search term for filtering
        active_only: Whether to include only active students
        db: Database session dependency

    Returns:
        Paginated list of students
    """
    service = StudentService(db)
    result = await service.get_students_by_school(
        school_id=school_id,
        page=page,
        size=size,
        search=search,
        active_only=active_only
    )

    return StudentList(**result)


@router.put(
    "/{student_id}",
    response_model=StudentResponse,
    summary="Update student",
    description="Update an existing student's information."
)
async def update_student(
    student_id: UUID,
    student_data: StudentUpdate,
    db: AsyncSession = Depends(get_db)
) -> StudentResponse:
    """Update a student.

    Args:
        student_id: UUID of the student
        student_data: Updated student data
        db: Database session dependency

    Returns:
        Updated student response

    Raises:
        404: If student not found
        409: If email or username conflicts
        400: If validation fails
    """
    service = StudentService(db)
    student = await service.update_student(student_id, student_data)

    if not student:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found"
        )

    return student


@router.delete(
    "/{student_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Soft delete student",
    description="Soft delete a student (sets is_active to False)."
)
async def delete_student(
    student_id: UUID,
    db: AsyncSession = Depends(get_db)
) -> None:
    """Soft delete a student.

    Args:
        student_id: UUID of the student
        db: Database session dependency

    Raises:
        404: If student not found
    """
    service = StudentService(db)
    deleted = await service.delete_student(student_id)

    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found"
        )


@router.delete(
    "/{student_id}/permanent",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Permanently delete student",
    description="Permanently delete a student from the database."
)
async def permanently_delete_student(
    student_id: UUID,
    db: AsyncSession = Depends(get_db)
) -> None:
    """Permanently delete a student.

    Args:
        student_id: UUID of the student
        db: Database session dependency

    Raises:
        404: If student not found
    """
    service = StudentService(db)
    deleted = await service.permanently_delete_student(student_id)

    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Student not found"
        )
