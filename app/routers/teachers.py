"""Teacher API router with CRUD operations."""

from typing import Optional
from uuid import UUID
from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.services.teacher_service import TeacherService
from app.schemas.teacher import (
    TeacherCreate,
    TeacherUpdate,
    TeacherResponse,
    TeacherListResponse
)


router = APIRouter(
    prefix="/teachers",
    tags=["teachers"]
)


@router.post("/", response_model=TeacherResponse, status_code=201)
async def create_teacher(
    teacher_data: TeacherCreate,
    db: AsyncSession = Depends(get_db)
):
    """Create a new teacher."""
    service = TeacherService(db)
    try:
        teacher = await service.create_teacher(teacher_data)
        return TeacherResponse.model_validate(teacher)
    except ValueError as e:
        if "School not found" in str(e):
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="School not found"
            )
        elif "email already exists" in str(e):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Teacher with this email already exists"
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )


@router.get("/{teacher_id}", response_model=TeacherResponse)
async def get_teacher(
    teacher_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Get teacher by ID."""
    service = TeacherService(db)
    teacher = await service.get_teacher_by_id(teacher_id)
    if not teacher:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Teacher not found"
        )
    return TeacherResponse.model_validate(teacher)


@router.get("/", response_model=TeacherListResponse)
async def get_teachers(
    school_id: Optional[UUID] = Query(
        None, description="Filter by school ID"
    ),
    is_active: Optional[bool] = Query(
        None, description="Filter by active status"
    ),
    page: int = Query(1, ge=1, description="Page number"),
    size: int = Query(10, ge=1, le=100, description="Page size"),
    db: AsyncSession = Depends(get_db)
):
    """Get paginated list of teachers with optional filters."""
    if page < 1 or size < 1 or size > 100:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid pagination parameters"
        )
    
    service = TeacherService(db)
    return await service.get_teachers(
        school_id=school_id,
        is_active=is_active,
        page=page,
        size=size
    )


@router.put("/{teacher_id}", response_model=TeacherResponse)
async def update_teacher(
    teacher_id: UUID,
    teacher_data: TeacherUpdate,
    db: AsyncSession = Depends(get_db)
):
    """Update teacher information."""
    service = TeacherService(db)
    try:
        teacher = await service.update_teacher(teacher_id, teacher_data)
        if not teacher:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Teacher not found"
            )
        return TeacherResponse.model_validate(teacher)
    except ValueError as e:
        if "email already exists" in str(e):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Teacher with this email already exists"
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )


@router.delete("/{teacher_id}")
async def delete_teacher(
    teacher_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Delete a teacher."""
    service = TeacherService(db)
    success = await service.delete_teacher(teacher_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Teacher not found"
        )
    return {"message": "Teacher deleted successfully"}


@router.get("/school/{school_id}", response_model=TeacherListResponse)
async def get_teachers_by_school(
    school_id: UUID,
    page: int = Query(1, ge=1, description="Page number"),
    size: int = Query(10, ge=1, le=100, description="Page size"),
    db: AsyncSession = Depends(get_db)
):
    """Get teachers for a specific school."""
    if page < 1 or size < 1 or size > 100:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid pagination parameters"
        )

    service = TeacherService(db)
    return await service.get_teachers_by_school(
        school_id=school_id,
        page=page,
        size=size
    )
