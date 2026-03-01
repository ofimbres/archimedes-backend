#!/bin/bash
# ==========================================
# 🐍 Archimedes Python Service - Production Deployment Script
# ==========================================
# Automated deployment script for production environments

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="archimedes-python"
IMAGE_TAG="${1:-latest}"
ENV_FILE="${2:-.env.production}"

echo -e "${BLUE}🐍 Archimedes Python Service - Production Deployment${NC}"
echo -e "${BLUE}=================================================${NC}"

# Check if .env.production exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}❌ Environment file $ENV_FILE not found!${NC}"
    echo -e "${YELLOW}💡 Copy .env.production template and configure your values${NC}"
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Build the Docker image
echo -e "${BLUE}🔨 Building Docker image...${NC}"
docker build -t ${APP_NAME}:${IMAGE_TAG} .

# Tag as latest
docker tag ${APP_NAME}:${IMAGE_TAG} ${APP_NAME}:latest

echo -e "${GREEN}✅ Docker image built successfully${NC}"

# Stop existing containers
echo -e "${BLUE}🛑 Stopping existing containers...${NC}"
docker-compose -f docker-compose.production.yml down || true

# Remove old containers and volumes (optional - uncomment if needed)
# echo -e "${YELLOW}🧹 Cleaning up old containers...${NC}"
# docker system prune -f

# Start new deployment
echo -e "${BLUE}🚀 Starting production deployment...${NC}"
docker-compose -f docker-compose.production.yml up -d

# Wait for services to be healthy
echo -e "${BLUE}⏳ Waiting for services to be healthy...${NC}"
sleep 30

# Check service health
echo -e "${BLUE}🏥 Checking service health...${NC}"
if curl -f http://localhost:8001/api/v1/health/ > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Application is healthy and running!${NC}"
else
    echo -e "${RED}❌ Application health check failed${NC}"
    echo -e "${YELLOW}📋 Checking logs...${NC}"
    docker-compose -f docker-compose.production.yml logs --tail=20 archimedes-app
    exit 1
fi

# Display running services
echo -e "${BLUE}📊 Running services:${NC}"
docker-compose -f docker-compose.production.yml ps

echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
echo -e "${BLUE}📱 Application URLs:${NC}"
echo -e "   • API: http://localhost:8001"
echo -e "   • Health: http://localhost:8001/api/v1/health/"
echo -e "   • Docs: http://localhost:8001/docs"
echo -e "   • Database: localhost:5432"

echo -e "${YELLOW}💡 Useful commands:${NC}"
echo -e "   • View logs: docker-compose -f docker-compose.production.yml logs -f"
echo -e "   • Stop services: docker-compose -f docker-compose.production.yml down"
echo -e "   • Restart app: docker-compose -f docker-compose.production.yml restart archimedes-app"