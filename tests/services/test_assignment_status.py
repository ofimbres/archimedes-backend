from datetime import datetime, timedelta, timezone
from pathlib import Path
import sys

sys.path.append(str(Path(__file__).resolve().parents[2]))
from app.services.assignment_service import _student_work_status


def test_returns_late_completed_when_finished_after_due_date() -> None:
    due_date = datetime(2026, 4, 20, 18, 0, tzinfo=timezone.utc)
    completed_at = due_date + timedelta(seconds=1)
    now = due_date + timedelta(days=1)

    status = _student_work_status(
        has_completion=True,
        completed_at=completed_at,
        due_date=due_date,
        now=now,
    )

    assert status == "late_completed"


def test_returns_completed_when_finished_on_due_date_boundary() -> None:
    due_date = datetime(2026, 4, 20, 18, 0, tzinfo=timezone.utc)
    completed_at = due_date
    now = due_date + timedelta(days=1)

    status = _student_work_status(
        has_completion=True,
        completed_at=completed_at,
        due_date=due_date,
        now=now,
    )

    assert status == "completed"


def test_returns_past_due_when_not_completed_and_due_passed() -> None:
    due_date = datetime(2026, 4, 20, 18, 0, tzinfo=timezone.utc)
    now = due_date + timedelta(seconds=1)

    status = _student_work_status(
        has_completion=False,
        completed_at=None,
        due_date=due_date,
        now=now,
    )

    assert status == "past_due"


def test_returns_pending_when_due_date_is_not_set() -> None:
    now = datetime.now(timezone.utc)

    status = _student_work_status(
        has_completion=False,
        completed_at=None,
        due_date=None,
        now=now,
    )

    assert status == "pending"
