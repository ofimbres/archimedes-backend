"""Course API router for teacher course management operations."""

from typing import Optional
from uuid import UUID
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.services.course_service import CourseService
from app.schemas.course import (
    CourseCreate,
    CourseUpdate,
    CourseResponse,
    CourseListResponse
)

router = APIRouter(
    prefix="/courses",
    tags=["courses"]
)


@router.post("/", response_model=CourseResponse, status_code=201)
async def create_course(
    course_data: CourseCreate,
    db: AsyncSession = Depends(get_db)
):
    """Create a new course."""
    service = CourseService(db)
    try:
        course = await service.create_course(course_data)
        return CourseResponse.model_validate(course)
    except ValueError as e:
        if "Teacher not found" in str(e):
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Teacher not found"
            )
        elif "same school" in str(e):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Teacher and course must belong to the same school"
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )


@router.get("/{course_id}", response_model=CourseResponse)
async def get_course(
    course_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Get course by ID."""
    service = CourseService(db)
    course = await service.get_course_by_id(course_id)
    if not course:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Course not found"
        )
    return CourseResponse.model_validate(course)


@router.get("/join-code/{join_code}", response_model=CourseResponse)
async def get_course_by_join_code(
    join_code: str,
    db: AsyncSession = Depends(get_db)
):
    """Get course by join code."""
    service = CourseService(db)
    course = await service.get_course_by_join_code(join_code)
    if not course:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Course not found"
        )
    return CourseResponse.model_validate(course)


@router.get("/teacher/{teacher_id}", response_model=CourseListResponse)
async def get_teacher_courses(
    teacher_id: UUID,
    is_active: Optional[bool] = Query(
        None, description="Filter by active status"
    ),
    page: int = Query(1, ge=1, description="Page number"),
    size: int = Query(10, ge=1, le=100, description="Page size"),
    db: AsyncSession = Depends(get_db)
):
    """Get courses for a specific teacher."""
    if page < 1 or size < 1 or size > 100:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid pagination parameters"
        )

    service = CourseService(db)
    return await service.get_teacher_courses(
        teacher_id=teacher_id,
        is_active=is_active,
        page=page,
        size=size
    )


@router.put("/{course_id}", response_model=CourseResponse)
async def update_course(
    course_id: UUID,
    course_data: CourseUpdate,
    db: AsyncSession = Depends(get_db)
):
    """Update course information."""
    service = CourseService(db)
    course = await service.update_course(course_id, course_data)
    if not course:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Course not found"
        )
    return CourseResponse.model_validate(course)


@router.delete("/{course_id}")
async def delete_course(
    course_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Delete a course."""
    service = CourseService(db)
    success = await service.delete_course(course_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Course not found"
        )
    return {"message": "Course deleted successfully"}
