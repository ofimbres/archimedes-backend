"""AssignmentCompletion model - records when a student completes an assignment."""

from sqlalchemy import ForeignKey, DateTime, Numeric, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from datetime import datetime
import uuid

from app.database import Base


class AssignmentCompletion(Base):
    """One record per student per assignment when a student completes it."""

    __tablename__ = "assignment_completions"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )
    student_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("students.id", ondelete="CASCADE"),
        nullable=False,
    )
    assignment_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("assignments.id", ondelete="CASCADE"),
        nullable=False,
    )
    completed_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=datetime.utcnow,
        nullable=False,
    )
    score: Mapped[float | None] = mapped_column(Numeric(5, 2), nullable=True)

    __table_args__ = (
        UniqueConstraint(
            "student_id",
            "assignment_id",
            name="uq_completion_student_assignment",
        ),
        {"extend_existing": True},
    )

    assignment = relationship("Assignment", back_populates="completions")
    student = relationship("Student", back_populates="assignment_completions")

    def __repr__(self) -> str:
        return (
            f"<AssignmentCompletion(id={self.id}, student_id={self.student_id}, "
            f"assignment_id={self.assignment_id})>"
        )
