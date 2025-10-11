"""Schemas package for Archimedes Education Platform."""

from .student import (
    StudentBase,
    StudentCreate,
    StudentUpdate,
    StudentResponse,
    StudentList
)

__all__ = [
    "StudentBase",
    "StudentCreate",
    "StudentUpdate",
    "StudentResponse",
    "StudentList"
]
