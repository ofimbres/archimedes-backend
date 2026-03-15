"""Teacher SQLAlchemy model with computed full name."""

from sqlalchemy import String, ForeignKey, DateTime
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from sqlalchemy.ext.hybrid import hybrid_property
from datetime import datetime
import uuid
from typing import TYPE_CHECKING

from app.database import Base

if TYPE_CHECKING:
    from app.models.school import School
    from app.models.assignment import Assignment


class Teacher(Base):
    """Teacher model with computed full name column."""

    __tablename__ = "teachers"

    # Primary key
    id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    # Required fields
    first_name: Mapped[str] = mapped_column(String(50), nullable=False)
    last_name: Mapped[str] = mapped_column(String(50), nullable=False)
    email: Mapped[str] = mapped_column(
        String(100), nullable=False, unique=True
    )
    username: Mapped[str] = mapped_column(
        String(50), nullable=False, unique=True
    )
    cognito_user_id: Mapped[str | None] = mapped_column(
        String(255), unique=True, nullable=True, index=True,
        comment="Cognito sub for OAuth users; links to Cognito identity",
    )

    # Foreign key to school
    school_id: Mapped[UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("schools.id"),
        nullable=False
    )

    # Optional fields
    max_classes: Mapped[int | None] = mapped_column(
        nullable=True, default=6
    )
    is_active: Mapped[bool] = mapped_column(
        nullable=False, default=True
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
    school: Mapped["School"] = relationship(
        "School", back_populates="teachers"
    )
    courses = relationship(
        "Course", back_populates="teacher", cascade="all, delete-orphan"
    )
    assignments: Mapped[list["Assignment"]] = relationship(
        "Assignment",
        back_populates="teacher",
    )

    @hybrid_property
    def full_name(self) -> str:
        """Computed full name property."""
        return f"{self.first_name} {self.last_name}"

    def __repr__(self) -> str:
        """String representation of teacher."""
        return (
            f"<Teacher(id={self.id}, email={self.email}, "
            f"full_name={self.full_name})>"
        )
