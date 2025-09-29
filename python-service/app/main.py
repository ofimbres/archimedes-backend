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
from contextlib import asynccontextmanager

from .database import engine, Base
from .routers import health
from .config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    # Startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
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

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router, tags=["Health"])


@app.get("/")
async def root():
    """Welcome message - so much cleaner than Java!"""
    return {
        "message": "🐍 Archimedes Python Service",
        "description": "Clean, modern Python - no Java verbosity!",
        "docs": "/docs",
        "status": "running"
    }