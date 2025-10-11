"""Business logic service for school operations."""

import math
from typing import Optional
from uuid import UUID

from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.school import School
from app.schemas.school import (
    SchoolCreate,
    SchoolUpdate,
    SchoolResponse,
    SchoolListResponse
)


class SchoolService:
    """Service class for school business logic."""

    async def create_school(
        self, school_data: SchoolCreate, session: AsyncSession
    ) -> SchoolResponse:
        """
        Create a new school.

        Args:
            school_data: School creation data

        Returns:
            SchoolResponse: The created school

        Raises:
            ValueError: If school code already exists
        """
        # Check if school code already exists
        existing_school = await session.execute(
            select(School).where(School.code == school_data.code)
        )
        if existing_school.scalar_one_or_none():
            raise ValueError(
                f"School code '{school_data.code}' already exists"
            )

        # Create new school
        school = School(
            name=school_data.name,
            code=school_data.code,
            address=school_data.address,
            city=school_data.city,
            state=school_data.state,
            zip_code=school_data.zip_code,
            phone=school_data.phone,
            email=school_data.email,
            website=school_data.website
        )

        session.add(school)
        await session.commit()
        await session.refresh(school)

        return SchoolResponse.model_validate(school)

    async def get_school_by_id(
        self, school_id: UUID, session: AsyncSession
    ) -> Optional[SchoolResponse]:
        """
        Get a school by ID.

        Args:
            school_id: UUID of the school

        Returns:
            SchoolResponse or None if not found
        """
        result = await session.execute(
            select(School).where(
                School.id == school_id,
                School.is_active == True  # noqa: E712
            )
        )
        school = result.scalar_one_or_none()

        if school:
            return SchoolResponse.model_validate(school)
        return None

    async def get_schools(
        self,
        session: AsyncSession,
        page: int = 1,
        size: int = 10
    ) -> SchoolListResponse:
        """
        Get paginated list of schools.

        Args:
            page: Page number (1-based)
            size: Number of schools per page

        Returns:
            SchoolListResponse: Paginated school list
        """
        # Get total count
        count_result = await session.execute(
            select(func.count(School.id)).where(
                School.is_active == True  # noqa: E712
            )
        )
        total = count_result.scalar()

        # Get schools for current page
        offset = (page - 1) * size
        result = await session.execute(
            select(School)
            .where(School.is_active == True)  # noqa: E712
            .order_by(School.name)
            .offset(offset)
            .limit(size)
        )
        schools = result.scalars().all()

        # Calculate total pages
        pages = math.ceil(total / size) if total > 0 else 1

        school_responses = [
            SchoolResponse.model_validate(school) for school in schools
        ]

        return SchoolListResponse(
            schools=school_responses,
            total=total,
            page=page,
            size=size,
            pages=pages
        )

    async def update_school(
        self,
        session: AsyncSession,
        school_id: UUID,
        school_data: SchoolUpdate
    ) -> Optional[SchoolResponse]:
        """
        Update a school.

        Args:
            school_id: UUID of the school to update
            school_data: Updated school data

        Returns:
            SchoolResponse or None if not found

        Raises:
            ValueError: If school code already exists for another school
        """
        # Get existing school
        result = await session.execute(
            select(School).where(
                School.id == school_id,
                School.is_active == True  # noqa: E712
            )
        )
        school = result.scalar_one_or_none()

        if not school:
            return None

        # Check if new code already exists (if code is being updated)
        if school_data.code and school_data.code != school.code:
            existing_school = await session.execute(
                select(School).where(
                    School.code == school_data.code,
                    School.id != school_id
                )
            )
            if existing_school.scalar_one_or_none():
                raise ValueError(
                    f"School code '{school_data.code}' already exists"
                )

        # Update fields
        update_data = school_data.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(school, field, value)

        await session.commit()
        await session.refresh(school)

        return SchoolResponse.model_validate(school)

    async def delete_school(
        self, session: AsyncSession, school_id: UUID
    ) -> bool:
        """
        Soft delete a school.

        Args:
            school_id: UUID of the school to delete

        Returns:
            bool: True if deleted, False if not found
        """
        result = await session.execute(
            select(School).where(
                School.id == school_id,
                School.is_active == True  # noqa: E712
            )
        )
        school = result.scalar_one_or_none()

        if not school:
            return False

        school.is_active = False
        await session.commit()

        return True
