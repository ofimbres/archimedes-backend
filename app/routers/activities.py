"""Activities API router - search catalog by topic/subtopic."""

from typing import Optional

from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.services.activity_service import ActivityService
from app.schemas.activity import ActivityListResponse, ActivityResponse, TopicSummary

router = APIRouter(
    prefix="/activities",
    tags=["Activities"],
)


@router.get("", response_model=ActivityListResponse)
async def list_activities(
    topic: Optional[str] = Query(
        None, description="Filter by topic (e.g. Algebra)",
    ),
    subtopic: Optional[str] = Query(None, description="Filter by subtopic"),
    activity_type: str = Query(
        "miniquiz",
        description="Activity type (miniquiz, exercise, etc.)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """List activities (catalog) with optional filters. Search by topic and/or subtopic."""
    service = ActivityService(db)
    return await service.list_activities(
        topic=topic,
        subtopic=subtopic,
        activity_type=activity_type,
    )


@router.get("/topics", response_model=list[TopicSummary])
async def list_topics(db: AsyncSession = Depends(get_db)):
    """List topics and their subtopics from the taxonomy (for filter UIs)."""
    service = ActivityService(db)
    summary = await service.get_topics_summary()
    return [TopicSummary(**s) for s in summary]


@router.get("/{activity_id}", response_model=ActivityResponse)
async def get_activity(
    activity_id: str,
    db: AsyncSession = Depends(get_db),
):
    """Get a single activity by activity_id (exercise id). Returns 404 if not found."""
    service = ActivityService(db)
    activity = await service.get_activity_by_id(activity_id, load_taxonomy=True)
    if not activity:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Activity not found",
        )
    return ActivityResponse.from_activity(activity)
