# Multi-stage image for FastAPI (ECS / ECR). Build from repo root.
FROM python:3.11-slim AS builder

ARG DEBIAN_FRONTEND=noninteractive
ARG BUILD_DATE
ARG GIT_COMMIT
ARG ENVIRONMENT=production

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gcc \
    && rm -rf /var/lib/apt/lists/*

RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip setuptools wheel && \
    pip install --no-cache-dir -r requirements.txt

FROM python:3.11-slim AS runtime

ARG BUILD_DATE
ARG GIT_COMMIT
ARG ENVIRONMENT=production

ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PATH="/opt/venv/bin:$PATH" \
    ENVIRONMENT=${ENVIRONMENT} \
    BUILD_DATE=${BUILD_DATE} \
    GIT_COMMIT=${GIT_COMMIT}

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# RDS TLS: vendored AWS global bundle (avoids build-time fetch + guarantees file exists).
RUN mkdir -p /etc/ssl/rds
COPY docker/rds-global-bundle.pem /etc/ssl/rds/global-bundle.pem
RUN chmod 644 /etc/ssl/rds/global-bundle.pem

RUN groupadd -r appuser && \
    useradd -r -g appuser -d /app -s /sbin/nologin appuser

COPY --from=builder /opt/venv /opt/venv

WORKDIR /app
COPY --chown=appuser:appuser . .
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

USER appuser
EXPOSE 8001

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8001/api/v1/health/ || exit 1

# Single worker fits small ECS tasks (e.g. t4g.micro); --proxy-headers for ALB.
CMD ["uvicorn", "app.main:app", \
     "--host", "0.0.0.0", \
     "--port", "8001", \
     "--proxy-headers", \
     "--forwarded-allow-ips", "*", \
     "--access-log", \
     "--log-level", "info"]
