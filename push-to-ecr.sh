#!/bin/bash
# ==========================================
# 🐳 AWS ECR Docker Build & Push Script
# ==========================================
# Automated Docker image build and push to AWS ECR

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Configuration
AWS_ACCOUNT_ID="928483661641"
AWS_REGION="us-west-2"
ECR_REPOSITORY="dev-archimedes-backend-ecr-repository"
IMAGE_NAME="dev-archimedes-backend-ecr-repository"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"

# Parse command line arguments
IMAGE_TAG="${1:-latest}"
ENVIRONMENT="${2:-dev}"

echo -e "${BLUE}🐳 AWS ECR Docker Deployment${NC}"
echo -e "${BLUE}=============================${NC}"
echo -e "${PURPLE}📋 Configuration:${NC}"
echo -e "   • AWS Account: ${AWS_ACCOUNT_ID}"
echo -e "   • Region: ${AWS_REGION}"
echo -e "   • Repository: ${ECR_REPOSITORY}"
echo -e "   • Image Tag: ${IMAGE_TAG}"
echo -e "   • Environment: ${ENVIRONMENT}"
echo ""

# Check prerequisites
echo -e "${BLUE}🔍 Checking prerequisites...${NC}"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed. Please install it first.${NC}"
    echo -e "${YELLOW}💡 Install: brew install awscli${NC}"
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity > /dev/null 2>&1; then
    echo -e "${RED}❌ AWS credentials not configured or invalid.${NC}"
    echo -e "${YELLOW}💡 Run: aws configure${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites check passed${NC}"

# ECR Login
echo -e "${BLUE}🔐 Authenticating with AWS ECR...${NC}"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ ECR authentication successful${NC}"
else
    echo -e "${RED}❌ ECR authentication failed${NC}"
    exit 1
fi

# Build Docker image
echo -e "${BLUE}🔨 Building Docker image...${NC}"
echo -e "${YELLOW}   Building: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"

# Add build timestamp and git commit for tracking
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

docker build \
    --build-arg BUILD_DATE="${BUILD_DATE}" \
    --build-arg GIT_COMMIT="${GIT_COMMIT}" \
    --build-arg ENVIRONMENT="${ENVIRONMENT}" \
    -t ${IMAGE_NAME}:${IMAGE_TAG} \
    .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Docker image built successfully${NC}"
else
    echo -e "${RED}❌ Docker build failed${NC}"
    exit 1
fi

# Tag image for ECR
echo -e "${BLUE}🏷️  Tagging image for ECR...${NC}"
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_URI}:${IMAGE_TAG}

# Also tag as latest if not already latest
if [ "${IMAGE_TAG}" != "latest" ]; then
    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_URI}:latest
    echo -e "${YELLOW}   Tagged as both ${IMAGE_TAG} and latest${NC}"
fi

echo -e "${GREEN}✅ Image tagged successfully${NC}"

# Push to ECR
echo -e "${BLUE}📤 Pushing image to ECR...${NC}"
echo -e "${YELLOW}   Pushing: ${ECR_URI}:${IMAGE_TAG}${NC}"

docker push ${ECR_URI}:${IMAGE_TAG}

# Push latest tag if created
if [ "${IMAGE_TAG}" != "latest" ]; then
    echo -e "${YELLOW}   Pushing: ${ECR_URI}:latest${NC}"
    docker push ${ECR_URI}:latest
fi

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Image pushed to ECR successfully${NC}"
else
    echo -e "${RED}❌ Failed to push image to ECR${NC}"
    exit 1
fi

# Display image information
echo -e "${BLUE}📊 Image Information:${NC}"
IMAGE_SIZE=$(docker images ${IMAGE_NAME}:${IMAGE_TAG} --format "table {{.Size}}" | tail -n1)
echo -e "   • Local Image: ${IMAGE_NAME}:${IMAGE_TAG}"
echo -e "   • ECR URI: ${ECR_URI}:${IMAGE_TAG}"
echo -e "   • Image Size: ${IMAGE_SIZE}"
echo -e "   • Build Date: ${BUILD_DATE}"
echo -e "   • Git Commit: ${GIT_COMMIT}"

# Get ECR image details
echo -e "${BLUE}🔍 ECR Repository Status:${NC}"
aws ecr describe-images \
    --repository-name ${ECR_REPOSITORY} \
    --region ${AWS_REGION} \
    --image-ids imageTag=${IMAGE_TAG} \
    --query 'imageDetails[0].[imagePushedAt,imageSizeInBytes]' \
    --output table 2>/dev/null || echo -e "${YELLOW}   Image details not immediately available${NC}"

# Clean up local images (optional)
read -p "🧹 Clean up local Docker images? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}🧹 Cleaning up local images...${NC}"
    docker rmi ${IMAGE_NAME}:${IMAGE_TAG} 2>/dev/null || true
    docker rmi ${ECR_URI}:${IMAGE_TAG} 2>/dev/null || true
    if [ "${IMAGE_TAG}" != "latest" ]; then
        docker rmi ${ECR_URI}:latest 2>/dev/null || true
    fi
    echo -e "${GREEN}✅ Local images cleaned up${NC}"
fi

echo ""
echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
echo -e "${BLUE}📋 Next Steps:${NC}"
echo -e "   • Update your ECS task definition with: ${ECR_URI}:${IMAGE_TAG}"
echo -e "   • Deploy to ECS service or update Lambda function"
echo -e "   • Monitor application logs and health checks"

echo ""
echo -e "${YELLOW}💡 Useful Commands:${NC}"
echo -e "   • List ECR images: aws ecr list-images --repository-name ${ECR_REPOSITORY} --region ${AWS_REGION}"
echo -e "   • Pull image: docker pull ${ECR_URI}:${IMAGE_TAG}"
echo -e "   • Run locally: docker run -p 8001:8001 ${ECR_URI}:${IMAGE_TAG}"