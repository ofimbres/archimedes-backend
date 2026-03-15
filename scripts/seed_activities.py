#!/usr/bin/env python3
"""
Seed taxonomy (topics, subtopics) and activities from CSV files.

Idempotent: only inserts when each table is empty. Run from project root:
  python scripts/seed_activities.py

Use --force to clear topics/subtopics/activities and re-seed (e.g. after a
failed run left activities empty).
"""

import argparse
import asyncio
import sys
from pathlib import Path

from sqlalchemy import text

from app.database import AsyncSessionLocal
from app.services.activity_service import ActivityService

# Resolve CSV paths relative to project root (parent of scripts/)
_PROJECT_ROOT = Path(__file__).resolve().parent.parent
TOPICS_CSV = _PROJECT_ROOT / "docs" / "topics.csv"
ACTIVITIES_CSV = _PROJECT_ROOT / "docs" / "miniquiz-activities.csv"


async def main() -> int:
    parser = argparse.ArgumentParser(description="Seed activities taxonomy and catalog")
    parser.add_argument(
        "--force",
        action="store_true",
        help="Truncate topics, subtopics, activities and re-seed",
    )
    args = parser.parse_args()

    if args.force:
        async with AsyncSessionLocal() as db:
            await db.execute(text("TRUNCATE activities, subtopics, topics CASCADE"))
            await db.commit()
        print("Cleared activities, subtopics, topics.")

    topics_path = TOPICS_CSV if TOPICS_CSV.exists() else None
    activities_path = ACTIVITIES_CSV if ACTIVITIES_CSV.exists() else None
    if args.force and (not topics_path or not activities_path):
        print(
            "Warning: CSV files not found. topics:", TOPICS_CSV, "exists:",
            TOPICS_CSV.exists(), "activities:", ACTIVITIES_CSV, "exists:",
            ACTIVITIES_CSV.exists(),
        )
    async with AsyncSessionLocal() as db:
        service = ActivityService(db)
        counts = await service.seed_all_if_empty(
            force=args.force,
            topics_csv_path=topics_path,
            activities_csv_path=activities_path,
        )

    if any(counts.values()):
        print(
            f"Seeded: topics={counts['topics']}, subtopics={counts['subtopics']}, "
            f"activities={counts['activities']}"
        )
    else:
        print("No seeding needed (tables already have data).")
    return 0


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))
