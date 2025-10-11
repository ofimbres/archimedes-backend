"""Enrollment SQLAlchemy model for student-class relationships."""

from sqlalchemy import String, ForeignKey, DateTime, Boolean
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from datetime import datetime
import uuid
from typing import Any

from app.database import Base


class Enrollment(Base):
    """Enrollment model for many-to-many student-class relationship."""

    __tablename__ = "enrollments"

    # Primary key
    id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    # Foreign keys
    student_id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("students.id", ondelete="CASCADE"),
        nullable=False
    )
    class_id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("classes.id", ondelete="CASCADE"),
        nullable=False
    )

    # Enrollment details
    enrolled_at: Mapped[datetime] = mapped_column(
        DateTime,
        default=datetime.utcnow,
        nullable=False
    )
    enrollment_status: Mapped[str] = mapped_column(
        String(20),
        default='active',
        nullable=False
    )
    is_active: Mapped[bool] = mapped_column(
        Boolean,
        default=True,
        nullable=False
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
    student = relationship("Student")
    course = relationship("Course")

    # Unique constraint to prevent duplicate enrollments
    __table_args__ = (
        {'extend_existing': True}
    )

    def __repr__(self) -> str:
        """String representation of enrollment."""
        return (
            f"<Enrollment(id={self.id}, student_id={self.student_id}, "
            f"class_id={self.class_id}, status={self.enrollment_status})>"
        )
