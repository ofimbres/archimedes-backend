"""Subtopic SQLAlchemy model - topic_id -> topics."""

from sqlalchemy import String, Integer, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, Mapped, mapped_column
from sqlalchemy import UniqueConstraint
import uuid
from typing import TYPE_CHECKING

from app.database import Base

if TYPE_CHECKING:
    from app.models.activity import Activity


class Subtopic(Base):
    """Subtopic model - belongs to a topic; activities reference subtopic_id."""

    __tablename__ = "subtopics"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
    )
    topic_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("topics.id", ondelete="CASCADE"),
        nullable=False,
    )
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    display_order: Mapped[int | None] = mapped_column(Integer, nullable=True)

    __table_args__ = (
        UniqueConstraint("topic_id", "name", name="uq_subtopic_topic_name"),
        {"extend_existing": True},
    )

    topic = relationship("Topic", back_populates="subtopics")
    activities: Mapped[list["Activity"]] = relationship(
        "Activity",
        back_populates="subtopic",
    )

    def __repr__(self) -> str:
        return (
            f"<Subtopic(id={self.id}, name='{self.name}', "
            f"topic_id={self.topic_id})>"
        )
