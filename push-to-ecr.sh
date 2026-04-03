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
ECR_REPOSITORY="dev-archimedes-backend"
IMAGE_NAME="dev-archimedes-backend"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"
# Match ECS on Graviton (t4g); override with BUILD_PLATFORM=linux/amd64 if needed.
BUILD_PLATFORM="${BUILD_PLATFORM:-linux/arm64}"

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
echo -e "   • Platform: ${BUILD_PLATFORM}"
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

# Build and push (buildx: correct arch for Graviton ECS; --push loads no local tag)
echo -e "${BLUE}🔨 Building and pushing (${BUILD_PLATFORM})...${NC}"
echo -e "${YELLOW}   Tags: ${ECR_URI}:${IMAGE_TAG}${NC}"

BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

TAG_ARGS=( -t "${ECR_URI}:${IMAGE_TAG}" )
if [ "${IMAGE_TAG}" != "latest" ]; then
    TAG_ARGS+=( -t "${ECR_URI}:latest" )
    echo -e "${YELLOW}   Also tagging: ${ECR_URI}:latest${NC}"
fi

if ! docker buildx version &>/dev/null; then
    echo -e "${RED}❌ docker buildx not available. Install Docker Buildx or update Docker Desktop.${NC}"
    exit 1
fi

docker buildx build \
    --platform "${BUILD_PLATFORM}" \
    --build-arg BUILD_DATE="${BUILD_DATE}" \
    --build-arg GIT_COMMIT="${GIT_COMMIT}" \
    --build-arg ENVIRONMENT="${ENVIRONMENT}" \
    "${TAG_ARGS[@]}" \
    --push \
    .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Image built and pushed to ECR${NC}"
else
    echo -e "${RED}❌ docker buildx build --push failed${NC}"
    exit 1
fi

# Display image information
echo -e "${BLUE}📊 Image Information:${NC}"
echo -e "   • Platform: ${BUILD_PLATFORM}"
echo -e "   • ECR URI: ${ECR_URI}:${IMAGE_TAG}"
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