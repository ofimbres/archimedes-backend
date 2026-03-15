"""Assignment SQLAlchemy model - teacher assigns activity to course."""

from sqlalchemy import String, ForeignKey, DateTime, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from datetime import datetime
import uuid
from typing import TYPE_CHECKING

from app.database import Base

if TYPE_CHECKING:
    from app.models.course import Course
    from app.models.teacher import Teacher
    from app.models.activity import Activity


class Assignment(Base):
    """Assignment model - teacher assigns an activity to a course."""

    __tablename__ = "assignments"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )
    course_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("courses.id", ondelete="CASCADE"),
        nullable=False,
    )
    activity_id: Mapped[str] = mapped_column(
        String(20),
        ForeignKey("activities.activity_id", ondelete="CASCADE"),
        nullable=False,
    )
    assigned_by: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("teachers.id", ondelete="CASCADE"),
        nullable=False,
    )
    due_date: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )
    title_override: Mapped[str | None] = mapped_column(String(200), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=datetime.utcnow,
        nullable=False,
    )

    __table_args__ = (
        UniqueConstraint("course_id", "activity_id", name="uq_assignment_course_activity"),
        {"extend_existing": True},
    )

    course = relationship("Course", back_populates="assignments")
    teacher = relationship("Teacher", back_populates="assignments")
    activity = relationship("Activity", back_populates="assignments")

    def __repr__(self) -> str:
        return (
            f"<Assignment(id={self.id}, course_id={self.course_id}, "
            f"activity_id='{self.activity_id}')>"
        )
