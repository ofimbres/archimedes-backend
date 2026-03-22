"""
Archimedes Python Microservice
A clean, modern alternative to Java verbosity!

This service handles:
- Sequential ID generation (much cleaner than Java!)
- Analytics and reporting
- Real-time features
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from contextlib import asynccontextmanager

from .config import settings
from .database import engine, Base
from .routers import (
    health,
    students,
    auth,
    schools,
    teachers,
    enrollments,
    courses,
    worksheets,
    activities,
    assignments,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    # Seed taxonomy and activities: topics -> subtopics -> activities (if each table empty)
    from .database import AsyncSessionLocal
    from .services.activity_service import ActivityService
    async with AsyncSessionLocal() as db:
        service = ActivityService(db)
        counts = await service.seed_all_if_empty()
        if any(counts.values()):
            print(f"Seeded taxonomy/activities: {counts}")

    yield

    # Shutdown
    await engine.dispose()


# Create FastAPI app with automatic docs
app = FastAPI(
    title="Archimedes Python Service",
    description="Clean, modern Python microservice - no Java verbosity!",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

def _cors_allow_origins() -> list[str]:
    raw = (settings.cors_origins or "").strip()
    if raw:
        return [o.strip() for o in raw.split(",") if o.strip()]
    fu = (settings.frontend_url or "").strip().rstrip("/")
    return [fu] if fu else ["http://localhost:3000"]


# CORS: explicit origins (needed with credentials=True; cannot use "*")
app.add_middleware(
    CORSMiddleware,
    allow_origins=_cors_allow_origins(),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["Authorization", "Content-Type", "Accept", "*"],
)

# Include routers
app.include_router(health.router, prefix="/api/v1", tags=["Health"])
app.include_router(students.router, prefix="/api/v1", tags=["Students"])
app.include_router(schools.router, prefix="/api/v1", tags=["Schools"])
app.include_router(auth.router, prefix="/api/v1", tags=["Authentication"])
app.include_router(teachers.router, prefix="/api/v1", tags=["Teachers"])
app.include_router(enrollments.router, prefix="/api/v1", tags=["Enrollments"])
app.include_router(courses.router, prefix="/api/v1", tags=["Courses"])
app.include_router(worksheets.router, prefix="/api/v1", tags=["Worksheets"])
app.include_router(activities.router, prefix="/api/v1", tags=["Activities"])
app.include_router(assignments.router, prefix="/api/v1", tags=["Assignments"])


@app.get("/")
async def root():
    """Welcome message - so much cleaner than Java!"""
    return {
        "message": "🐍 Archimedes Python Service",
        "description": "Clean, modern Python - no Java verbosity!",
        "docs": "/docs",
        "login": "/login",
        "status": "running"
    }


@app.get("/login", response_class=HTMLResponse)
async def login_page():
    """Simple login page with Sign in with Google (Cognito Hosted UI). No frontend app required."""
    return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sign in – Archimedes</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 24rem; margin: 4rem auto; padding: 0 1rem; text-align: center; }
    h1 { font-size: 1.5rem; margin-bottom: 0.5rem; }
    p { color: #555; margin-bottom: 1.5rem; }
    a.button {
      display: inline-block;
      padding: 0.75rem 1.5rem;
      background: #1a73e8;
      color: #fff;
      text-decoration: none;
      border-radius: 6px;
      font-weight: 500;
    }
    a.button:hover { background: #1557b0; }
    .links { margin-top: 2rem; font-size: 0.875rem; }
    .links a { color: #0066cc; }
  </style>
</head>
<body>
  <h1>Archimedes</h1>
  <p>Sign in with your account</p>
  <a href="/api/v1/auth/oauth/redirect?prompt=select_account" class="button">Sign in with Google</a>
  <p class="links"><a href="/">API</a> · <a href="/docs">Docs</a></p>
</body>
</html>"""
