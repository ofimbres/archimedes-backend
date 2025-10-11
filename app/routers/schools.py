"""School router for managing educational institutions."""

from typing import Any
from uuid import UUID

from fastapi import APIRouter, HTTPException, status, Query, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db

from app.schemas.school import (
    SchoolCreate,
    SchoolUpdate,
    SchoolResponse,
    SchoolListResponse
)
from app.services.school_service import SchoolService

router = APIRouter(
    prefix="/schools",
    tags=["Schools"]
)


@router.post(
    "/",
    response_model=SchoolResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create a new school",
    description="Create a new educational institution"
)
async def create_school(
    school_data: SchoolCreate,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Create a new school.

    Args:
        school_data: School creation data

    Returns:
        SchoolResponse: The created school information

    Raises:
        HTTPException: If creation fails
    """
    try:
        school_service = SchoolService()
        return await school_service.create_school(school_data, db)
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to create school: {str(e)}"
        )


@router.get(
    "/{school_id}",
    response_model=SchoolResponse,
    summary="Get school by ID",
    description="Retrieve a specific school by its ID"
)
async def get_school(
    school_id: UUID,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Get a school by ID.

    Args:
        school_id: UUID of the school to retrieve

    Returns:
        SchoolResponse: The school information

    Raises:
        HTTPException: If school not found
    """
    try:
        school_service = SchoolService()
        school = await school_service.get_school_by_id(school_id, db)
        if not school:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="School not found"
            )
        return school
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve school: {str(e)}"
        )


@router.get(
    "/",
    response_model=SchoolListResponse,
    summary="List all schools",
    description="Retrieve a paginated list of all schools"
)
async def get_schools(
    page: int = Query(1, ge=1, description="Page number"),
    size: int = Query(10, ge=1, le=100, description="Page size"),
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Get a paginated list of schools.

    Args:
        page: Page number (1-based)
        size: Number of schools per page (1-100)

    Returns:
        SchoolListResponse: Paginated list of schools
    """
    try:
        school_service = SchoolService()
        return await school_service.get_schools(db, page=page, size=size)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to retrieve schools: {str(e)}"
        )


@router.put(
    "/{school_id}",
    response_model=SchoolResponse,
    summary="Update school",
    description="Update an existing school's information"
)
async def update_school(
    school_id: UUID,
    school_data: SchoolUpdate,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Update a school.

    Args:
        school_id: UUID of the school to update
        school_data: Updated school data

    Returns:
        SchoolResponse: The updated school information

    Raises:
        HTTPException: If school not found or update fails
    """
    try:
        school_service = SchoolService()
        school = await school_service.update_school(db, school_id, school_data)
        if not school:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="School not found"
            )
        return school
    except HTTPException:
        raise
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to update school: {str(e)}"
        )


@router.delete(
    "/{school_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete school",
    description="Delete a school (soft delete)"
)
async def delete_school(
    school_id: UUID,
    db: AsyncSession = Depends(get_db)
) -> None:
    """
    Delete a school (soft delete).

    Args:
        school_id: UUID of the school to delete

    Raises:
        HTTPException: If school not found or deletion fails
    """
    try:
        school_service = SchoolService()
        success = await school_service.delete_school(db, school_id)
        if not success:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="School not found"
            )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to delete school: {str(e)}"
        )
