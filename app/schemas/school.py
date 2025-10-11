"""Pydantic schemas for school operations."""

from typing import Optional, List
from uuid import UUID
from datetime import datetime

from pydantic import BaseModel, Field, EmailStr


class SchoolBase(BaseModel):
    """Base school schema with common fields."""
    
    name: str = Field(
        ..., min_length=1, max_length=200, description="School name"
    )
    code: str = Field(
        ..., min_length=1, max_length=10, description="School code"
    )
    address: Optional[str] = Field(
        None, max_length=500, description="School address"
    )
    city: Optional[str] = Field(
        None, max_length=100, description="City"
    )
    state: Optional[str] = Field(
        None, max_length=50, description="State/Province"
    )
    zip_code: Optional[str] = Field(
        None, max_length=20, description="ZIP/Postal code"
    )
    phone: Optional[str] = Field(
        None, max_length=20, description="Phone number"
    )
    email: Optional[EmailStr] = Field(None, description="School email")
    website: Optional[str] = Field(
        None, max_length=200, description="School website"
    )


class SchoolCreate(SchoolBase):
    """Schema for creating a new school."""
    pass


class SchoolUpdate(BaseModel):
    """Schema for updating an existing school."""
    
    name: Optional[str] = Field(None, min_length=1, max_length=200)
    code: Optional[str] = Field(None, min_length=1, max_length=10)
    address: Optional[str] = Field(None, max_length=500)
    city: Optional[str] = Field(None, max_length=100)
    state: Optional[str] = Field(None, max_length=50)
    zip_code: Optional[str] = Field(None, max_length=20)
    phone: Optional[str] = Field(None, max_length=20)
    email: Optional[EmailStr] = None
    website: Optional[str] = Field(None, max_length=200)
    is_active: Optional[bool] = None


class SchoolResponse(SchoolBase):
    """Schema for school response."""
    
    id: UUID
    is_active: bool
    created_at: datetime
    updated_at: datetime
    
    class Config:
        from_attributes = True


class SchoolListResponse(BaseModel):
    """Schema for paginated school list response."""
    
    schools: List[SchoolResponse]
    total: int
    page: int
    size: int
    pages: int
