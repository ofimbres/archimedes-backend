"""Activity catalog business logic and database operations."""

import csv
from pathlib import Path
from typing import Optional

from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from sqlalchemy.orm import selectinload

from app.models.activity import Activity
from app.models.topic import Topic
from app.models.subtopic import Subtopic
from app.schemas.activity import ActivityResponse, ActivityListResponse


# CSV paths: prefer docs/ in repo for seeding
TOPICS_CSV_PATHS = [
    Path("/workspace/docs/topics.csv"),
    Path(__file__).resolve().parent.parent.parent / "docs" / "topics.csv",
]

ACTIVITIES_CSV_PATHS = [
    Path("/workspace/docs/miniquiz-activities.csv"),
    Path(__file__).resolve().parent.parent.parent
    / "docs"
    / "miniquiz-activities.csv",
]


class ActivityService:
    """Service for activity catalog (search, list, seed). Taxonomy: topics -> subtopics -> activities."""

    def __init__(self, db: AsyncSession):
        self.db = db

    async def list_activities(
        self,
        topic: Optional[str] = None,
        subtopic: Optional[str] = None,
        activity_type: str = "miniquiz",
    ) -> ActivityListResponse:
        """List activities with optional filters by topic name, subtopic name, type. Joins taxonomy."""
        query = (
            select(Activity)
            .join(Activity.subtopic)
            .join(Subtopic.topic)
            .where(Activity.activity_type == activity_type)
        )
        if topic:
            query = query.where(Topic.name == topic)
        if subtopic:
            query = query.where(Subtopic.name == subtopic)
        query = query.order_by(Topic.name, Subtopic.name, Activity.activity_id)
        query = query.options(
            selectinload(Activity.subtopic).selectinload(Subtopic.topic),
        )
        result = await self.db.execute(query)
        activities = result.scalars().unique().all()
        total = len(activities)
        return ActivityListResponse(
            activities=[ActivityResponse.from_activity(a) for a in activities],
            total=total,
        )

    async def get_activity_by_id(
        self, activity_id: str, load_taxonomy: bool = True
    ) -> Optional[Activity]:
        """Get a single activity by activity_id. Optionally load subtopic and topic for response."""
        query = select(Activity).where(Activity.activity_id == activity_id)
        if load_taxonomy:
            query = query.options(
                selectinload(Activity.subtopic).selectinload(Subtopic.topic),
            )
        result = await self.db.execute(query)
        return result.scalar_one_or_none()

    async def get_topics_summary(self) -> list[dict]:
        """Return topics with their subtopics from taxonomy tables (optionally only those with activities)."""
        query = (
            select(Topic)
            .order_by(Topic.display_order, Topic.name)
        )
        result = await self.db.execute(
            query.options(selectinload(Topic.subtopics)),
        )
        topics = result.scalars().unique().all()
        return [
            {
                "topic": t.name,
                "subtopics": sorted(s.name for s in t.subtopics),
            }
            for t in topics
        ]

    async def count_topics(self) -> int:
        """Return number of topics."""
        result = await self.db.execute(select(func.count(Topic.id)))
        return result.scalar() or 0

    async def count_subtopics(self) -> int:
        """Return number of subtopics."""
        result = await self.db.execute(select(func.count(Subtopic.id)))
        return result.scalar() or 0

    async def count_activities(self) -> int:
        """Return total number of activities in the catalog."""
        result = await self.db.execute(select(func.count(Activity.activity_id)))
        return result.scalar() or 0

    def _resolve_subtopic_id(self, topic_name: str, subtopic_name: str) -> Optional[str]:
        """Sync helper: resolve (topic_name, subtopic_name) to subtopic id from current session state. Not used in async seed."""
        return None

    async def seed_topics(
        self, csv_path: Optional[Path] = None, force: bool = False
    ) -> int:
        """Seed topics from topics.csv if table is empty (or force=True). Returns number inserted."""
        if not force and await self.count_topics() > 0:
            return 0
        path = csv_path or next((p for p in TOPICS_CSV_PATHS if p.exists()), None)
        if not path or not path.exists():
            return 0
        seen: set[str] = set()
        inserted = 0
        path = path.resolve()
        with open(path, encoding="utf-8-sig") as f:
            reader = csv.DictReader(f)
            for row in reader:
                name = (row.get("TOPIC") or "").strip()
                if not name or name in seen:
                    continue
                seen.add(name)
                self.db.add(Topic(name=name))
                inserted += 1
        if inserted:
            await self.db.commit()
        return inserted

    async def seed_subtopics(
        self, csv_path: Optional[Path] = None, force: bool = False
    ) -> int:
        """Seed subtopics from topics.csv if table is empty (or force=True). Requires topics first."""
        if not force and await self.count_subtopics() > 0:
            return 0
        path = csv_path or next((p for p in TOPICS_CSV_PATHS if p.exists()), None)
        if not path or not path.exists():
            return 0
        # Load topic name -> id
        result = await self.db.execute(select(Topic.id, Topic.name))
        topic_by_name = {row.name: row.id for row in result.all()}
        if not topic_by_name:
            return 0
        inserted = 0
        seen: set[tuple[str, str]] = set()
        path = path.resolve() if hasattr(path, "resolve") else path
        with open(path, encoding="utf-8-sig") as f:
            reader = csv.DictReader(f)
            for row in reader:
                topic_name = (row.get("TOPIC") or "").strip()
                subtopic_name = (row.get("SUBTOPIC") or "").strip()
                if not topic_name or not subtopic_name:
                    continue
                key = (topic_name, subtopic_name)
                if key in seen:
                    continue
                seen.add(key)
                topic_id = topic_by_name.get(topic_name)
                if not topic_id:
                    continue
                self.db.add(Subtopic(topic_id=topic_id, name=subtopic_name))
                inserted += 1
        if inserted:
            await self.db.commit()
        return inserted

    async def seed_activities(
        self, csv_path: Optional[Path] = None, force: bool = False
    ) -> int:
        """
        Seed activities from miniquiz-activities.csv if table is empty (or force=True).
        Requires topics and subtopics to be seeded first. Resolves (TOPIC, SUBTOPIC) to subtopic_id.
        """
        if not force and await self.count_activities() > 0:
            return 0
        path = csv_path or next((p for p in ACTIVITIES_CSV_PATHS if p.exists()), None)
        if not path or not path.exists():
            return 0
        # Build (topic_name, subtopic_name) -> subtopic_id
        result = await self.db.execute(
            select(Subtopic.id, Subtopic.name, Topic.name).join(Subtopic.topic),
        )
        subtopic_id_by_pair: dict[tuple[str, str], str] = {}
        for row in result.all():
            subtopic_id_by_pair[(row[2], row[1])] = str(row[0])
        if not subtopic_id_by_pair:
            return 0
        inserted = 0
        seen_activity_ids: set[str] = set()
        path = path.resolve() if hasattr(path, "resolve") else path
        with open(path, encoding="utf-8-sig") as f:
            reader = csv.DictReader(f)
            for row in reader:
                aid = (row.get("ACTIVITY_ID") or "").strip()
                if not aid or aid in seen_activity_ids:
                    continue
                topic_name = (row.get("TOPIC") or "").strip()
                subtopic_name = (row.get("SUBTOPIC") or "").strip()
                description = (row.get("ACTIVITY_DESCRIPTION") or "").strip()
                key = (topic_name, subtopic_name)
                subtopic_id = subtopic_id_by_pair.get(key)
                if not subtopic_id:
                    continue
                from uuid import UUID
                seen_activity_ids.add(aid)
                activity = Activity(
                    activity_id=aid,
                    subtopic_id=UUID(subtopic_id),
                    description=description or None,
                    activity_type="miniquiz",
                )
                self.db.add(activity)
                inserted += 1
        if inserted:
            await self.db.commit()
        return inserted

    async def seed_all_if_empty(
        self,
        force: bool = False,
        *,
        topics_csv_path: Optional[Path] = None,
        activities_csv_path: Optional[Path] = None,
    ) -> dict[str, int]:
        """Run topic -> subtopic -> activity seed in order (if each table empty, or force=True). Returns counts."""
        t = await self.seed_topics(csv_path=topics_csv_path, force=force)
        s = await self.seed_subtopics(csv_path=topics_csv_path, force=force)
        a = await self.seed_activities(csv_path=activities_csv_path, force=force)
        return {"topics": t, "subtopics": s, "activities": a}
