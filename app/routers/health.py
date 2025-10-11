"""
Health check router - simple and clean!
"""

from fastapi import APIRouter, status
from app.services.health_service import HealthService

router = APIRouter(prefix="/health", tags=["health"])


@router.get("/", status_code=status.HTTP_200_OK)
async def health_check():
    """Basic health check endpoint"""
    return {
        "status": "healthy",
        "service": "archimedes-python",
        "message": "🐍 Python service running smoothly!"
    }


@router.get("/status", status_code=status.HTTP_200_OK)
async def health_status():
    """Health status with service dependencies"""
    return await HealthService.get_health_status()
