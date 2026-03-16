"""
Student SQLAlchemy model for Archimedes Education Platform.

This model represents students scoped to individual schools.
"""

from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, Computed
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import uuid

from ..database import Base


class Student(Base):
    """Student model representing students in the education platform.

    Students are scoped to schools and have globally unique emails.
    The full_name is a computed column in the database.
    """

    __tablename__ = "students"

    id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        index=True
    )
    school_id = Column(
        UUID(as_uuid=True),
        ForeignKey("schools.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )
    first_name = Column(String(50), nullable=False)
    last_name = Column(String(50), nullable=False)
    # full_name is computed in PostgreSQL as (first_name || ' ' || last_name)
    full_name = Column(String(255), Computed("first_name || ' ' || last_name"))
    email = Column(String(100), unique=True, nullable=False, index=True)
    username = Column(String(50), unique=True, nullable=False)
    cognito_user_id = Column(
        String(255), unique=True, nullable=True, index=True,
        comment="Cognito sub for OAuth users; links to Cognito identity",
    )
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
    school = relationship("School", back_populates="students")
    courses = relationship(
        "Course",
        secondary="enrollments",
        back_populates="students"
    )
    assignment_completions = relationship(
        "AssignmentCompletion",
        back_populates="student",
        cascade="all, delete-orphan",
    )

    def __repr__(self) -> str:
        """String representation of Student."""
        return (
            f"<Student(id={self.id}, name='{self.full_name}', "
            f"email='{self.email}')>"
        )
