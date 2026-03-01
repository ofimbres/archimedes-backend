#!/bin/bash
# ==========================================
# 🚀 Quick ECR Push Script
# ==========================================
# Simple script for quick development pushes

set -e

# Configuration
AWS_ACCOUNT_ID="928483661641"
AWS_REGION="us-west-2"
ECR_REPOSITORY="dev-archimedes-backend-ecr-repository"
IMAGE_NAME="dev-archimedes-backend-ecr-repository"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"

# Get image tag from argument or use latest
IMAGE_TAG="${1:-latest}"

echo "🐳 Quick ECR Push - ${IMAGE_TAG}"
echo "================================"

# ECR Login
echo "🔐 ECR Login..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Build
echo "🔨 Building..."
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .

# Tag
echo "🏷️ Tagging..."
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_URI}:${IMAGE_TAG}

# Push
echo "📤 Pushing..."
docker push ${ECR_URI}:${IMAGE_TAG}

echo "✅ Done! Image pushed to:"
echo "   ${ECR_URI}:${IMAGE_TAG}"