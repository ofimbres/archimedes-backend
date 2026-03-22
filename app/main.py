"""
Archimedes Python Microservice
A clean, modern alternative to Java verbosity!

This service handles:
- Sequential ID generation (much cleaner than Java!)
- Analytics and reporting
- Real-time features
"""

from urllib.parse import urlparse

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

def _origin_from_base_url(base: str) -> str | None:
    """scheme://host[:port] for CORS Allow-Origin (no path)."""
    u = (base or "").strip()
    if not u:
        return None
    parsed = urlparse(u if "://" in u else f"//{u}", scheme="http")
    if not parsed.netloc:
        parsed = urlparse(u)
    if not parsed.scheme or not parsed.netloc:
        return None
    host = parsed.netloc.split("@")[-1]
    return f"{parsed.scheme}://{host}"


def _origins_http_and_https_for_host(base: str) -> list[str]:
    """
    CloudFront may serve the same quiz on http and https; the browser Origin uses
    the page's actual scheme. Allow both so preflight matches either.
    """
    canonical = _origin_from_base_url(base)
    if not canonical:
        return []
    parsed = urlparse(canonical)
    hostport = parsed.netloc.split("@")[-1]
    if not hostport:
        return [canonical]
    return [f"http://{hostport}", f"https://{hostport}"]


def _cors_allow_origins() -> list[str]:
    """
    Explicit origins only (required with Authorization + allow_credentials).

    Merges: CORS_ORIGINS, FRONTEND_URL, and the origin of MINIQUIZ_BASE_URL /
    S3_MINI_QUIZ_BASE_URL so CloudFront miniquiz fetch() preflight succeeds without
    duplicating the CDN in env when it matches the content host.
    """
    seen: set[str] = set()
    ordered: list[str] = []

    def add(origin: str | None) -> None:
        if not origin:
            return
        o = origin.strip().rstrip("/")
        if not o or o in seen:
            return
        seen.add(o)
        ordered.append(o)

    for part in (settings.cors_origins or "").split(","):
        add(part.strip())
    add(_origin_from_base_url(settings.frontend_url))
    for mo in _origins_http_and_https_for_host(settings.miniquiz_base_url):
        add(mo)

    if not ordered:
        add("http://localhost:3000")
    return ordered


# CORS: explicit Allow-Origin per request origin (not "*"); preflight must list
# POST, OPTIONS and headers the miniquiz uses (Authorization, Content-Type).
_CORS_METHODS = ["GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
_CORS_HEADERS = ["Authorization", "Content-Type", "Accept"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=_cors_allow_origins(),
    allow_credentials=True,
    allow_methods=_CORS_METHODS,
    allow_headers=_CORS_HEADERS,
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
