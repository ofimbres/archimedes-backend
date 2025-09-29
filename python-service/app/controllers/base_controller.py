"""
Base controller - Common functionality for all controllers
"""
from abc import ABC
from typing import Dict, Any
from fastapi import HTTPException, status
from pydantic import ValidationError


class BaseController(ABC):
    """Base controller with common HTTP handling operations"""
    
    @staticmethod
    def success_response(data: Any, message: str = "Success") -> Dict:
        """Standard success response format"""
        return {
            "success": True,
            "message": message,
            "data": data
        }
    
    @staticmethod
    def error_response(message: str, error_code: str = "INTERNAL_ERROR") -> Dict:
        """Standard error response format"""
        return {
            "success": False,
            "message": message,
            "error_code": error_code,
            "data": None
        }
    
    @staticmethod
    def paginated_response(data: list, page: int, limit: int, total: int) -> Dict:
        """Standard paginated response format"""
        return {
            "success": True,
            "data": data,
            "pagination": {
                "page": page,
                "limit": limit,
                "total": total,
                "pages": (total + limit - 1) // limit,
                "has_next": page * limit < total,
                "has_prev": page > 1
            }
        }
    
    @staticmethod
    def handle_validation_error(error: ValidationError) -> HTTPException:
        """Handle Pydantic validation errors"""
        return HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=BaseController.error_response(
                "Validation failed", 
                "VALIDATION_ERROR"
            )
        )
    
    @staticmethod
    def handle_not_found(resource: str = "Resource") -> HTTPException:
        """Handle not found errors"""
        return HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=BaseController.error_response(
                f"{resource} not found",
                "NOT_FOUND"
            )
        )
    
    @staticmethod
    def handle_server_error(message: str = "Internal server error") -> HTTPException:
        """Handle internal server errors"""
        return HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=BaseController.error_response(
                message,
                "INTERNAL_ERROR"
            )
        )
