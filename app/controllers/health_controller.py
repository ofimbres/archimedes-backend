"""
Health controller - Handle health check HTTP operations
"""
from typing import Dict
from fastapi import HTTPException
from app.controllers.base_controller import BaseController
from app.services.health_service import HealthService


class HealthController(BaseController):
    """Controller for health check operations"""

    @classmethod
    async def basic_health_check(cls) -> Dict:
        """Basic health check"""
        try:
            return cls.success_response(
                {
                    "status": "healthy",
                    "service": "archimedes-python",
                    "message": "🐍 Python service running smoothly!"
                },
                "Service is healthy"
            )
        except Exception as e:
            raise cls.handle_server_error(f"Health check failed: {str(e)}")

    @classmethod
    async def detailed_health_check(cls) -> Dict:
        """Detailed health check with system metrics"""
        try:
            health_status = await HealthService.get_health_status()
            return cls.success_response(
                health_status,
                "Detailed health status retrieved successfully"
            )
        except Exception as e:
            raise cls.handle_server_error(
                f"Detailed health check failed: {str(e)}")

    @classmethod
    async def readiness_check(cls) -> Dict:
        """Readiness probe for Kubernetes"""
        try:
            # Check if all critical services are ready
            db_status = await HealthService._check_database()

            if db_status.get("status") == "connected":
                return cls.success_response(
                    {"ready": True, "database": db_status},
                    "Service is ready"
                )
            else:
                raise HTTPException(
                    status_code=503,
                    detail=cls.error_response(
                        "Service not ready - database unavailable",
                        "NOT_READY"
                    )
                )
        except HTTPException:
            raise
        except Exception as e:
            raise HTTPException(
                status_code=503,
                detail=cls.error_response(
                    f"Readiness check failed: {str(e)}",
                    "NOT_READY"
                )
            )

    @classmethod
    async def liveness_check(cls) -> Dict:
        """Liveness probe for Kubernetes"""
        try:
            # Simple check to see if the service is alive
            return cls.success_response(
                {"alive": True},
                "Service is alive"
            )
        except Exception as e:
            raise HTTPException(
                status_code=503,
                detail=cls.error_response(
                    f"Liveness check failed: {str(e)}",
                    "NOT_ALIVE"
                )
            )
