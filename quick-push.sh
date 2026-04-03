#!/bin/bash
# ==========================================
# 🚀 Quick ECR Push Script
# ==========================================
# Simple script for quick development pushes (linux/arm64 for Graviton ECS).

set -e

# Configuration
AWS_ACCOUNT_ID="928483661641"
AWS_REGION="us-west-2"
ECR_REPOSITORY="dev-archimedes-backend"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"
BUILD_PLATFORM="${BUILD_PLATFORM:-linux/arm64}"

# Get image tag from argument or use latest
IMAGE_TAG="${1:-latest}"

echo "🐳 Quick ECR Push - ${IMAGE_TAG} (${BUILD_PLATFORM})"
echo "================================"

# ECR Login
echo "🔐 ECR Login..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

echo "🔨 Buildx build + push..."
docker buildx build \
  --platform "${BUILD_PLATFORM}" \
  --build-arg BUILD_DATE="${BUILD_DATE}" \
  --build-arg GIT_COMMIT="${GIT_COMMIT}" \
  --build-arg ENVIRONMENT=dev \
  -t "${ECR_URI}:${IMAGE_TAG}" \
  --push \
  .

echo "✅ Done! Image pushed to:"
echo "   ${ECR_URI}:${IMAGE_TAG}"
