"""Activity SQLAlchemy model - catalog of assignable items (e.g. miniquiz)."""

from sqlalchemy import String, DateTime, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from datetime import datetime
import uuid
from typing import TYPE_CHECKING

from app.database import Base

if TYPE_CHECKING:
    from app.models.assignment import Assignment
    from app.models.subtopic import Subtopic


class Activity(Base):
    """Activity model - catalog of assignable items (miniquiz, exercise, etc.)."""

    __tablename__ = "activities"

    activity_id: Mapped[str] = mapped_column(String(20), primary_key=True)
    subtopic_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("subtopics.id", ondelete="RESTRICT"),
        nullable=False,
    )
    description: Mapped[str | None] = mapped_column(String(500), nullable=True)
    activity_type: Mapped[str] = mapped_column(
        String(50), nullable=False, default="miniquiz",
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime,
        default=datetime.utcnow,
        nullable=False,
    )

    subtopic = relationship("Subtopic", back_populates="activities")
    assignments: Mapped[list["Assignment"]] = relationship(
        "Assignment",
        back_populates="activity",
    )

    def __repr__(self) -> str:
        return f"<Activity(activity_id='{self.activity_id}', subtopic_id={self.subtopic_id})>"
