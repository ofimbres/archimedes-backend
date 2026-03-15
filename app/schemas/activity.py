"""Pydantic schemas for activity (catalog) validation and serialization."""

from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from app.models.activity import Activity


class ActivityResponse(BaseModel):
    """Schema for activity list/detail responses. topic/subtopic from joined taxonomy."""

    model_config = ConfigDict(from_attributes=True)

    activity_id: str
    topic: str
    subtopic: str
    description: str | None
    activity_type: str
    created_at: datetime
    topic_id: str | None = None
    subtopic_id: str | None = None
    # Content URL: {miniquiz_base_url}/{activity_id}.html (e.g. CloudFront or S3 website)
    content_url: str | None = None

    @classmethod
    def from_activity(cls, activity: "Activity") -> "ActivityResponse":
        """Build response from Activity with subtopic and subtopic.topic loaded."""
        from app.config import settings

        topic_name = activity.subtopic.topic.name if activity.subtopic else ""
        subtopic_name = activity.subtopic.name if activity.subtopic else ""
        base = (settings.miniquiz_base_url or "").strip().rstrip("/")
        content_url = (
            f"{base}/{activity.activity_id}.html" if base and activity.activity_id else None
        )
        return cls(
            activity_id=activity.activity_id,
            topic=topic_name,
            subtopic=subtopic_name,
            description=activity.description,
            activity_type=activity.activity_type,
            created_at=activity.created_at,
            topic_id=str(activity.subtopic.topic.id) if activity.subtopic and activity.subtopic.topic else None,
            subtopic_id=str(activity.subtopic_id) if activity.subtopic_id else None,
            content_url=content_url,
        )


class ActivityListResponse(BaseModel):
    """Schema for paginated activity list."""

    activities: list[ActivityResponse]
    total: int


class TopicSummary(BaseModel):
    """Schema for distinct topic (and optional subtopics) for filter UIs."""

    topic: str
    subtopics: list[str] = Field(default_factory=list)
