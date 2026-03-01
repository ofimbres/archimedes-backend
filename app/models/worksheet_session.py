"""
Worksheet Session Model - Student worksheet completion tracking
"""
from sqlalchemy import Column, String, DateTime, Float, JSON, Boolean, Integer
from sqlalchemy.sql import func
from datetime import datetime
import uuid

from ..database import Base


class WorksheetSession(Base):
    """
    Tracks student worksheet sessions - simple and focused
    """
    __tablename__ = "worksheet_sessions"

    # Primary key
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    
    # Student and worksheet identification
    student_id = Column(String, nullable=False, index=True)
    worksheet_id = Column(String, nullable=False)  # e.g. "AL01", "CM05"
    
    # Session tracking
    started_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    completed_at = Column(DateTime, nullable=True)
    is_completed = Column(Boolean, default=False)
    
    # Results
    score = Column(Float, nullable=True)  # Percentage score (0-100)
    total_questions = Column(Integer, nullable=True)
    correct_answers = Column(Integer, nullable=True)
    
    # Data storage
    student_answers = Column(JSON, nullable=True)  # Student's submitted answers
    correct_answers_data = Column(JSON, nullable=True)  # Correct answers for review
    completed_html_content = Column(String, nullable=True)  # Full HTML snapshot of completed worksheet
    
    # Metadata
    time_taken_seconds = Column(Integer, nullable=True)
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    def __repr__(self):
        return f"<WorksheetSession {self.student_id}:{self.worksheet_id} - {self.score}%>"

    @property
    def is_in_progress(self):
        """Check if session is still in progress"""
        return not self.is_completed and self.completed_at is None

    def calculate_score(self, correct_count: int, total_count: int):
        """Calculate and set the score"""
        if total_count > 0:
            self.score = round((correct_count / total_count) * 100, 2)
            self.correct_answers = correct_count
            self.total_questions = total_count
        else:
            self.score = 0.0
            self.correct_answers = 0
            self.total_questions = 0

    def complete_session(self):
        """Mark session as completed"""
        self.completed_at = datetime.utcnow()
        self.is_completed = True
        
        # Calculate time taken
        if self.started_at:
            time_diff = self.completed_at - self.started_at
            self.time_taken_seconds = int(time_diff.total_seconds())