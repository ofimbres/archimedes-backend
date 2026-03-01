# ==========================================
# 🐍 Archimedes Python Service - Production Dockerfile
# ==========================================
# Multi-stage build for optimized production image
# Clean, modern Python deployment (much better than Java containers!)

# ==========================================
# Stage 1: Builder - Install dependencies and compile
# ==========================================
FROM python:3.11-slim as builder

# Set build arguments
ARG DEBIAN_FRONTEND=noninteractive
ARG BUILD_DATE
ARG GIT_COMMIT
ARG ENVIRONMENT=production

# Install system dependencies needed for building
RUN apt-get update && apt-get install -y \
    build-essential \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Create virtual environment
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Copy requirements first (Docker layer caching)
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --upgrade pip setuptools wheel && \
    pip install --no-cache-dir -r requirements.txt

# ==========================================
# Stage 2: Runtime - Minimal production image
# ==========================================
FROM python:3.11-slim as runtime

# Set build arguments for runtime stage
ARG BUILD_DATE
ARG GIT_COMMIT
ARG ENVIRONMENT=production

# Set production environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PATH="/opt/venv/bin:$PATH" \
    ENVIRONMENT=${ENVIRONMENT} \
    BUILD_DATE=${BUILD_DATE} \
    GIT_COMMIT=${GIT_COMMIT}

# Install only runtime system dependencies
RUN apt-get update && apt-get install -y \
    --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Create non-root user for security
RUN groupadd -r appuser && \
    useradd -r -g appuser -d /app -s /sbin/nologin -c "App User" appuser

# Copy virtual environment from builder stage
COPY --from=builder /opt/venv /opt/venv

# Set working directory
WORKDIR /app

# Copy application code
COPY --chown=appuser:appuser . .

# Create necessary directories and set permissions
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8001/api/v1/health/ || exit 1

# Production command - Use Uvicorn with multiple workers
CMD ["uvicorn", "app.main:app", \
     "--host", "0.0.0.0", \
     "--port", "8001", \
     "--workers", "4", \
     "--worker-class", "uvicorn.workers.UvicornWorker", \
     "--access-log", \
     "--log-level", "info"]