"""
Worksheet Router - API endpoints for student worksheet workflow
"""
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import HTMLResponse
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Optional
import logging

from ..database import get_db
from ..schemas.worksheet import (
    TopicSummary, WorksheetInfo, SessionCreateRequest, SessionCreateResponse,
    SubmitAnswersRequest, SessionResult, WorksheetContent, APIResponse
)
from ..services.worksheet_service import worksheet_service
from ..services.data_loader import data_loader

logger = logging.getLogger(__name__)

router = APIRouter()


@router.on_event("startup")
async def startup_event():
    """Load worksheet data on startup"""
    logger.info("Loading worksheet data...")
    success = await data_loader.load_all_data()
    if success:
        logger.info("Worksheet data loaded successfully")
    else:
        logger.error("Failed to load worksheet data")


@router.get("/topics", response_model=List[TopicSummary])
async def get_topics():
    """
    Get all available topic categories with their worksheets
    
    Returns organized topics like:
    - Algebra (AL): AL01, AL02, AL03...
    - Arithmetic (AR): AR01, AR02...
    - Calculus (CM): CM01, CM02...
    """
    try:
        topics = await worksheet_service.get_topics_summary()
        return topics
    except Exception as e:
        logger.error(f"Error getting topics: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to load topics"
        )


@router.get("/topics/{topic_code}/worksheets", response_model=List[WorksheetInfo])
async def get_worksheets_by_topic(topic_code: str):
    """
    Get all worksheets for a specific topic
    
    Args:
        topic_code: Topic category code (AL, AR, CM, etc.)
    """
    try:
        topic_code = topic_code.upper()
        worksheets = await worksheet_service.get_worksheets_by_topic(topic_code)
        
        if not worksheets:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"No worksheets found for topic '{topic_code}'"
            )
        
        return worksheets
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting worksheets for topic {topic_code}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to load worksheets"
        )


@router.get("/worksheets/{worksheet_id}/content")
async def get_worksheet_content(
    worksheet_id: str, 
    student_id: Optional[str] = None
):
    """
    Get worksheet HTML content
    
    Args:
        worksheet_id: Worksheet identifier (e.g., AL01, CM05)
        student_id: Optional student ID for personalization
    
    Returns HTML content ready to display in frontend
    """
    try:
        worksheet_id = worksheet_id.upper()
        
        # Check if worksheet exists
        worksheet_info = data_loader.get_worksheet_info(worksheet_id)
        if not worksheet_info:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Worksheet '{worksheet_id}' not found"
            )
        
        # Get content
        content = await worksheet_service.get_worksheet_content(worksheet_id, student_id)
        if not content:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Worksheet content not available for '{worksheet_id}'"
            )
        
        return HTMLResponse(
            content=content,
            media_type="text/html"
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting worksheet content {worksheet_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to load worksheet content"
        )



@router.get("/worksheets/{worksheet_id}/completed")
async def get_completed_worksheet(
    worksheet_id: str,
    student_id: str,
    session_id: str,
    db: AsyncSession = Depends(get_db)
):
    """
    Get completed worksheet with grade populated
    
    This endpoint returns the worksheet HTML with the student's grade
    displayed in the data-grade-field and all inputs made read-only.
    """
    try:
        content = await worksheet_service.get_worksheet_with_grade(
            worksheet_id, student_id, session_id, db
        )
        
        if not content:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Completed worksheet not found for session '{session_id}'"
            )
        
        return HTMLResponse(
            content=content,
            media_type="text/html"
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting completed worksheet {worksheet_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to load completed worksheet"
        )


@router.post("/sessions", response_model=SessionCreateResponse)
async def create_worksheet_session(
    request: SessionCreateRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    Create a new worksheet session for a student
    
    This starts a new attempt at a worksheet and returns a session ID
    for tracking progress and submitting answers.
    """
    try:
        session = await worksheet_service.create_session(request, db)
        
        if not session:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Failed to create session for worksheet '{request.worksheet_id}'"
            )
        
        return session
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error creating session: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create worksheet session"
        )


@router.post("/sessions/{session_id}/submit", response_model=SessionResult)
async def submit_worksheet_answers(
    session_id: str,
    request: SubmitAnswersRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    Submit answers for a worksheet session
    
    Calculates score and completes the session.
    """
    try:
        result = await worksheet_service.submit_answers(session_id, request, db)
        
        if not result:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Failed to submit answers for session '{session_id}'"
            )
        
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error submitting answers for session {session_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to submit answers"
        )


@router.get("/sessions/{session_id}/results", response_model=SessionResult)
async def get_session_results(
    session_id: str,
    include_details: bool = False,
    db: AsyncSession = Depends(get_db)
):
    """
    Get results for a completed worksheet session
    
    Args:
        session_id: Session identifier
        include_details: Include student answers and correct answers
    """
    try:
        result = await worksheet_service.get_session_result(session_id, db, include_details)
        
        if not result:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Session '{session_id}' not found"
            )
        
        return result
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting session results {session_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to get session results"
        )


@router.get("/students/{student_id}/progress")
async def get_student_progress(
    student_id: str,
    db: AsyncSession = Depends(get_db)
):
    """
    Get overall progress summary for a student
    
    Returns statistics about completed worksheets, average score, etc.
    """
    try:
        progress = await worksheet_service.get_student_progress(student_id, db)
        
        if progress is None:
            # Return empty progress for new student
            progress = {
                "student_id": student_id,
                "total_sessions": 0,
                "completed_sessions": 0,
                "average_score": None,
                "topics_attempted": [],
                "last_session_date": None
            }
        
        return APIResponse(
            success=True,
            message="Student progress retrieved",
            data=progress
        )
        
    except Exception as e:
        logger.error(f"Error getting student progress {student_id}: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to get student progress"
        )


@router.get("/health")
async def worksheet_health_check():
    """
    Health check for worksheet service
    """
    try:
        # Check if data is loaded
        data_loaded = data_loader.is_loaded()
        
        # Get storage info
        storage_info = worksheet_service.file_service.get_storage_info()
        
        return {
            "status": "healthy",
            "data_loaded": data_loaded,
            "storage": storage_info,
            "timestamp": "2024-12-25T00:00:00Z"
        }
        
    except Exception as e:
        logger.error(f"Health check error: {e}")
        return {
            "status": "unhealthy",
            "error": str(e),
            "timestamp": "2024-12-25T00:00:00Z"
        }


@router.post("/admin/reload-data")
async def reload_worksheet_data():
    """
    Admin endpoint to reload worksheet data from CSV files
    """
    try:
        success = await data_loader.reload_data()
        
        if success:
            return APIResponse(
                success=True,
                message="Worksheet data reloaded successfully"
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to reload worksheet data"
            )
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error reloading data: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to reload data"
        )