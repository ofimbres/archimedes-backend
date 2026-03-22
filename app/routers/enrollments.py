"""Enrollment API router for student course enrollment operations."""

from typing import Optional
from uuid import UUID
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.dependencies.auth import (
    CurrentUserClaims,
    claims_is_platform_admin,
    get_current_user_claims,
)
from app.models.student import Student
from app.services.enrollment_service import EnrollmentService
from sqlalchemy import select
from app.schemas.enrollment import (
    EnrollmentCreate,
    EnrollmentCreateDirect,
    EnrollmentUpdate,
    EnrollmentResponse,
    EnrollmentListResponse
)

router = APIRouter(
    prefix="/enrollments",
    tags=["enrollments"]
)


@router.post("/join", response_model=EnrollmentResponse, status_code=201)
async def enroll_with_join_code(
    student_id: UUID,
    enrollment_data: EnrollmentCreate,
    db: AsyncSession = Depends(get_db),
    claims: CurrentUserClaims = Depends(get_current_user_claims),
):
    """Enroll a student in a course using a join code.

    **Authorization:** Bearer. Query ``student_id`` must match the authenticated
    student's profile (Cognito ``sub`` → ``students.cognito_user_id``).
    Body: ``{ "join_code": "..." }`` only.
    """
    linked = (
        await db.execute(
            select(Student.id).where(Student.cognito_user_id == claims.sub)
        )
    ).scalar_one_or_none()
    if linked is None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Authenticated user is not linked to a student profile",
        )
    if linked != student_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="student_id must match authenticated student",
        )
    service = EnrollmentService(db)
    try:
        enrollment = await service.enroll_with_join_code(
            student_id, enrollment_data
        )
        return EnrollmentResponse.model_validate(enrollment)
    except ValueError as e:
        if "Invalid join code" in str(e):
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Invalid join code or course is not active"
            )
        elif "already enrolled" in str(e):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Student is already enrolled in this course"
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )
    except Exception as e:
        # Catch all other exceptions for debugging
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.post("/direct", response_model=EnrollmentResponse, status_code=201)
async def create_direct_enrollment(
    enrollment_data: EnrollmentCreateDirect,
    db: AsyncSession = Depends(get_db)
):
    """Create a direct enrollment (admin use)."""
    service = EnrollmentService(db)
    try:
        enrollment = await service.create_direct_enrollment(enrollment_data)
        return EnrollmentResponse.model_validate(enrollment)
    except ValueError as e:
        if "not found" in str(e):
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=str(e)
            )
        elif "already enrolled" in str(e):
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail="Student is already enrolled in this course"
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=str(e)
            )


@router.get("/{enrollment_id}", response_model=EnrollmentResponse)
async def get_enrollment(
    enrollment_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Get enrollment by ID."""
    service = EnrollmentService(db)
    enrollment = await service.get_enrollment_by_id(enrollment_id)
    if not enrollment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Enrollment not found"
        )
    return EnrollmentResponse.model_validate(enrollment)


@router.get("/student/{student_id}", response_model=EnrollmentListResponse)
async def get_student_enrollments(
    student_id: UUID,
    is_active: Optional[bool] = Query(
        None, description="Filter by active status"
    ),
    page: int = Query(1, ge=1, description="Page number"),
    size: int = Query(10, ge=1, le=100, description="Page size"),
    db: AsyncSession = Depends(get_db),
    claims: CurrentUserClaims = Depends(get_current_user_claims),
):
    """Get enrollments for a specific student.

    **Authorization:** Bearer. Callers may only read their own ``student_id`` unless
    they are a configured platform admin.
    """
    if not claims_is_platform_admin(claims):
        linked = (
            await db.execute(
                select(Student.id).where(Student.cognito_user_id == claims.sub)
            )
        ).scalar_one_or_none()
        if linked is None or linked != student_id:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not allowed to view enrollments for this student",
            )
    if page < 1 or size < 1 or size > 100:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid pagination parameters"
        )

    service = EnrollmentService(db)
    return await service.get_student_enrollments(
        student_id=student_id,
        is_active=is_active,
        page=page,
        size=size
    )


@router.get("/course/{course_id}", response_model=EnrollmentListResponse)
async def get_course_enrollments(
    course_id: UUID,
    is_active: Optional[bool] = Query(
        None, description="Filter by active status"
    ),
    page: int = Query(1, ge=1, description="Page number"),
    size: int = Query(10, ge=1, le=100, description="Page size"),
    db: AsyncSession = Depends(get_db)
):
    """Get enrollments for a specific course."""
    if page < 1 or size < 1 or size > 100:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid pagination parameters"
        )

    service = EnrollmentService(db)
    return await service.get_course_enrollments(
        course_id=course_id,
        is_active=is_active,
        page=page,
        size=size
    )


@router.put("/{enrollment_id}", response_model=EnrollmentResponse)
async def update_enrollment(
    enrollment_id: UUID,
    enrollment_data: EnrollmentUpdate,
    db: AsyncSession = Depends(get_db)
):
    """Update enrollment information."""
    service = EnrollmentService(db)
    enrollment = await service.update_enrollment(
        enrollment_id, enrollment_data
    )
    if not enrollment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Enrollment not found"
        )
    return EnrollmentResponse.model_validate(enrollment)


@router.post("/{enrollment_id}/drop", response_model=EnrollmentResponse)
async def drop_enrollment(
    enrollment_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Drop an enrollment (set to inactive and status to dropped)."""
    service = EnrollmentService(db)
    enrollment = await service.drop_enrollment(enrollment_id)
    if not enrollment:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Enrollment not found"
        )
    return EnrollmentResponse.model_validate(enrollment)


@router.delete("/{enrollment_id}")
async def delete_enrollment(
    enrollment_id: UUID,
    db: AsyncSession = Depends(get_db)
):
    """Permanently delete an enrollment."""
    service = EnrollmentService(db)
    success = await service.delete_enrollment(enrollment_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Enrollment not found"
        )
    return {"message": "Enrollment deleted successfully"}
