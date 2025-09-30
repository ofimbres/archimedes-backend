"""
School SQLAlchemy model for Archimedes Education Platform.

This model represents schools as the central hub for multi-tenant organization.
"""

from sqlalchemy import Column, String, DateTime, Boolean
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import uuid

from ..database import Base


class School(Base):
    """School model representing educational institutions.

    Schools are the top-level entity in the multi-tenant architecture.
    Each school has a unique code for user-friendly URLs.
    """

    __tablename__ = "schools"

    id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True
    )
    code = Column(
        String(10),
        unique=True,
        nullable=False,
        index=True
    )
    name = Column(String(200), nullable=False, index=True)
    address = Column(String(500), nullable=True)
    city = Column(String(100), nullable=True)
    state = Column(String(50), nullable=True)
    zip_code = Column(String(20), nullable=True)
    phone = Column(String(20), nullable=True)
    email = Column(String(255), nullable=True)
    website = Column(String(200), nullable=True)
    is_active = Column(Boolean, default=True, nullable=False, index=True)
    created_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False
    )
    updated_at = Column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False
    )

    # Relationships
    students = relationship(
        "Student",
        back_populates="school",
        cascade="all, delete-orphan"
    )
    # TODO: Add these relationships when Teacher and Class models are created
    # teachers = relationship(
    #     "Teacher",
    #     back_populates="school",
    #     cascade="all, delete-orphan"
    # )
    # classes = relationship(
    #     "Class",
    #     back_populates="school",
    #     cascade="all, delete-orphan"
    # )

    def __repr__(self) -> str:
        """String representation of School."""
        return (
            f"<School(id={self.id}, code='{self.code}', "
            f"name='{self.name}')>"
        )
