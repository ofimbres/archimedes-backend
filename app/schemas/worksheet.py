"""
Worksheet API Schemas - Request/Response models
"""
from pydantic import BaseModel, Field, validator
from typing import Optional, Dict, Any, List
from datetime import datetime
from enum import Enum


class TopicCategory(str, Enum):
    """Available worksheet topic categories"""
    ALGEBRA = "AL"
    ARITHMETIC = "AR"
    CALCULUS = "CM"
    GEOMETRY = "DE"
    FRACTIONS = "FR"
    EQUATIONS = "EQ"
    EXPRESSIONS = "EX"
    ANALYSIS = "AN"
    BASICS = "BS"
    CONFIDENCE = "CF"
    CIRCLE = "CI"
    FUNCTIONS = "FA"


class WorksheetInfo(BaseModel):
    """Information about a single worksheet"""
    id: str = Field(..., description="Worksheet ID (e.g., AL01)")
    title: str = Field(..., description="Human-readable title")
    topic: str = Field(..., description="Topic category")
    subtopic: str = Field(..., description="Subtopic name")
    description: Optional[str] = Field(
        None, description="Activity description")
    difficulty: Optional[str] = Field("Medium", description="Difficulty level")

    @validator('id')
    def validate_worksheet_id(cls, v):
        if not v or len(v) < 3:
            raise ValueError('Worksheet ID must be at least 3 characters')
        return v.upper()


class TopicSummary(BaseModel):
    """Summary of a topic with available worksheets"""
    category: str = Field(...,
                          description="Topic category code (AL, AR, etc.)")
    name: str = Field(..., description="Full topic name")
    worksheet_count: int = Field(...,
                                 description="Number of available worksheets")
    worksheets: List[WorksheetInfo] = Field(...,
                                            description="List of worksheets")


class SessionCreateRequest(BaseModel):
    """Request to create a new worksheet session"""
    student_id: str = Field(..., description="Student identifier")
    worksheet_id: str = Field(..., description="Worksheet to work on")

    @validator('student_id')
    def validate_student_id(cls, v):
        if not v or len(v.strip()) == 0:
            raise ValueError('Student ID is required')
        return v.strip()

    @validator('worksheet_id')
    def validate_worksheet_id(cls, v):
        return v.upper() if v else v


class SessionCreateResponse(BaseModel):
    """Response when creating a new session"""
    session_id: str = Field(..., description="Created session ID")
    student_id: str = Field(..., description="Student ID")
    worksheet_id: str = Field(..., description="Worksheet ID")
    started_at: datetime = Field(..., description="Session start time")
    message: str = Field(..., description="Success message")


class SubmitAnswersRequest(BaseModel):
    """Request to submit worksheet answers"""
    answers: Dict[str, Any] = Field(...,
                                    description="Student answers (question_id -> answer)")
    time_taken_seconds: Optional[int] = Field(
        None, description="Time taken in seconds")
    grade: Optional[str] = Field(
        None, description="Grade from worksheet (e.g., '85%', '17/20', '85')")
    completed_html: Optional[str] = Field(
        None, description="Complete HTML content of the finished worksheet")

    @validator('answers')
    def validate_answers(cls, v):
        if not isinstance(v, dict):
            raise ValueError('Answers must be a dictionary')
        return v


class SessionResult(BaseModel):
    """Worksheet session results"""
    session_id: str
    student_id: str
    worksheet_id: str
    score: Optional[float] = Field(
        None, description="Score percentage (0-100)")
    correct_answers: Optional[int] = None
    total_questions: Optional[int] = None
    time_taken_seconds: Optional[int] = None
    started_at: datetime
    completed_at: Optional[datetime] = None
    is_completed: bool = False


class SessionResultWithDetails(SessionResult):
    """Extended session results with answer details"""
    student_answers: Optional[Dict[str, Any]] = None
    correct_answers_data: Optional[Dict[str, Any]] = None
    feedback: Optional[str] = None


class WorksheetContent(BaseModel):
    """Worksheet HTML content response"""
    worksheet_id: str
    content: str = Field(..., description="HTML content of the worksheet")
    content_type: str = Field(default="text/html")
    personalized: bool = Field(
        default=False, description="Whether content is personalized for student")


class StudentProgress(BaseModel):
    """Student's overall progress summary"""
    student_id: str
    total_sessions: int
    completed_sessions: int
    average_score: Optional[float] = None
    topics_attempted: List[str] = []
    last_session_date: Optional[datetime] = None


class APIResponse(BaseModel):
    """Standard API response wrapper"""
    success: bool = True
    message: str = "Success"
    data: Optional[Any] = None
    error: Optional[str] = None
