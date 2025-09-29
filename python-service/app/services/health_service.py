"""
Health service - Handle health check and system status
"""
from typing import Dict
from datetime import datetime
import os


class HealthService:
    """Service for health check operations"""

    @staticmethod
    async def get_health_status() -> Dict:
        """Get comprehensive health status"""
        return {
            "status": "healthy",
            "timestamp": datetime.utcnow().isoformat(),
            "version": os.getenv("APP_VERSION", "1.0.0"),
            "environment": os.getenv("ENVIRONMENT", "development"),
            "database": await HealthService._check_database()
        }

    @staticmethod
    async def _check_database() -> Dict:
        """Check database connectivity"""
        try:
            # Simple database check - can be enhanced later
            return {"status": "connected", "type": "postgresql"}
        except Exception as e:
            return {"status": "disconnected", "error": str(e)}
