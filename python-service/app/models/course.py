"""Class SQLAlchemy model for teacher courses."""

from sqlalchemy import String, ForeignKey, DateTime, Boolean
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from datetime import datetime
import uuid
from typing import TYPE_CHECKING

from app.database import Base

if TYPE_CHECKING:
    from app.models.school import School
    from app.models.teacher import Teacher
    from app.models.student import Student


class Course(Base):
    """Course model representing a teacher's class."""

    __tablename__ = "classes"

    # Primary key
    id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    # Foreign keys
    school_id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("schools.id"),
        nullable=False
    )
    teacher_id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("teachers.id"),
        nullable=False
    )

    # Required fields
    class_name: Mapped[str] = mapped_column(String(100), nullable=False)
    subject: Mapped[str] = mapped_column(String(50), nullable=False)
    join_code: Mapped[str] = mapped_column(
        String(10), nullable=False, unique=True
    )

    # Optional fields with defaults
    academic_year: Mapped[str] = mapped_column(
        String(10), nullable=False, default='2024-25'
    )
    semester: Mapped[str] = mapped_column(
        String(20), nullable=False, default='Fall'
    )
    is_active: Mapped[bool] = mapped_column(
        Boolean, nullable=False, default=True
    )

    # Timestamps
    created_at: Mapped[datetime] = mapped_column(
        DateTime,
        default=datetime.utcnow,
        nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime,
        default=datetime.utcnow,
        onupdate=datetime.utcnow,
        nullable=False
    )

    # Relationships
    school: Mapped["School"] = relationship("School", back_populates="courses")
    teacher: Mapped["Teacher"] = relationship(
        "Teacher", back_populates="courses"
    )
    students: Mapped[list["Student"]] = relationship(
        "Student",
        secondary="enrollments",
        back_populates="courses"
    )

    def __repr__(self) -> str:
        """String representation of course."""
        return (
            f"<Course(id={self.id}, name='{self.class_name}', "
            f"join_code='{self.join_code}')>"
        )
