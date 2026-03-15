"""Topic SQLAlchemy model - taxonomy for activities."""

from sqlalchemy import String, Integer
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
import uuid
from typing import TYPE_CHECKING

from app.database import Base

if TYPE_CHECKING:
    from app.models.subtopic import Subtopic


class Topic(Base):
    """Topic model - top-level category (e.g. Algebra, Arithmetic Operations)."""

    __tablename__ = "topics"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )
    name: Mapped[str] = mapped_column(String(200), unique=True, nullable=False)
    display_order: Mapped[int | None] = mapped_column(Integer, nullable=True)

    subtopics: Mapped[list["Subtopic"]] = relationship(
        "Subtopic",
        back_populates="topic",
        cascade="all, delete-orphan",
    )

    def __repr__(self) -> str:
        return f"<Topic(id={self.id}, name='{self.name}')>"
