# ==========================================
# 🐍 Archimedes Python Service - Makefile
# ==========================================
# Easy command management for development and deployment

# Configuration
IMAGE_NAME = dev-archimedes-backend-ecr-repository
ECR_URI = 928483661641.dkr.ecr.us-west-2.amazonaws.com/dev-archimedes-backend-ecr-repository
AWS_REGION = us-west-2

# Default target
.DEFAULT_GOAL := help

# Colors
BLUE = \033[0;34m
GREEN = \033[0;32m
YELLOW = \033[1;33m
RED = \033[0;31m
NC = \033[0m # No Color

.PHONY: help build push quick-push dev run test clean docker-clean ecr-login

help: ## Show this help message
	@echo "$(BLUE)🐍 Archimedes Python Service Commands$(NC)"
	@echo "$(BLUE)=====================================$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "$(YELLOW)%-15s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build Docker image locally
	@echo "$(BLUE)🔨 Building Docker image...$(NC)"
	docker build -t $(IMAGE_NAME):latest .
	@echo "$(GREEN)✅ Build complete!$(NC)"

ecr-login: ## Login to AWS ECR
	@echo "$(BLUE)🔐 Logging into AWS ECR...$(NC)"
	aws ecr get-login-password --region $(AWS_REGION) | docker login --username AWS --password-stdin 928483661641.dkr.ecr.$(AWS_REGION).amazonaws.com
	@echo "$(GREEN)✅ ECR login successful!$(NC)"

push: ecr-login build ## Build and push to ECR (full process)
	@echo "$(BLUE)📤 Running full ECR push process...$(NC)"
	./push-to-ecr.sh

quick-push: ## Quick build and push to ECR
	@echo "$(BLUE)🚀 Quick push to ECR...$(NC)"
	./quick-push.sh

push-tag: ## Push with specific tag (usage: make push-tag TAG=v1.0.0)
	@echo "$(BLUE)📤 Pushing with tag: $(TAG)$(NC)"
	./push-to-ecr.sh $(TAG)

dev: ## Run development server locally
	@echo "$(BLUE)🚀 Starting development server...$(NC)"
	uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload

run: build ## Run Docker container locally
	@echo "$(BLUE)🐳 Running Docker container...$(NC)"
	docker run -p 8001:8001 --env-file .env $(IMAGE_NAME):latest

test: ## Run tests
	@echo "$(BLUE)🧪 Running tests...$(NC)"
	pytest -v

test-docker: build ## Run tests in Docker
	@echo "$(BLUE)🧪 Running tests in Docker...$(NC)"
	docker run --rm $(IMAGE_NAME):latest pytest -v

clean: ## Clean up local Docker images
	@echo "$(BLUE)🧹 Cleaning up Docker images...$(NC)"
	docker rmi $(IMAGE_NAME):latest 2>/dev/null || true
	@echo "$(GREEN)✅ Cleanup complete!$(NC)"

docker-clean: ## Clean up all Docker resources
	@echo "$(BLUE)🧹 Deep cleaning Docker resources...$(NC)"
	docker system prune -f
	docker image prune -f
	@echo "$(GREEN)✅ Deep cleanup complete!$(NC)"

logs: ## View application logs (if running in Docker)
	@echo "$(BLUE)📋 Viewing application logs...$(NC)"
	docker logs -f archimedes-app 2>/dev/null || echo "$(RED)Container not running$(NC)"

health: ## Check application health
	@echo "$(BLUE)🏥 Checking application health...$(NC)"
	curl -f http://localhost:8001/api/v1/health/ || echo "$(RED)Health check failed$(NC)"

deploy-prod: ## Deploy to production
	@echo "$(BLUE)🚀 Deploying to production...$(NC)"
	./deploy.sh

stop: ## Stop running containers
	@echo "$(BLUE)🛑 Stopping containers...$(NC)"
	docker-compose -f docker-compose.production.yml down 2>/dev/null || \
	docker stop archimedes-app 2>/dev/null || echo "$(YELLOW)No containers to stop$(NC)"

restart: stop run ## Restart the application

install: ## Install Python dependencies locally
	@echo "$(BLUE)📦 Installing Python dependencies...$(NC)"
	pip install -r requirements.txt
	@echo "$(GREEN)✅ Dependencies installed!$(NC)"

format: ## Format code with black
	@echo "$(BLUE)✨ Formatting code...$(NC)"
	black app/ --line-length 88
	@echo "$(GREEN)✅ Code formatted!$(NC)"

lint: ## Lint code with ruff
	@echo "$(BLUE)🔍 Linting code...$(NC)"
	ruff app/
	@echo "$(GREEN)✅ Linting complete!$(NC)"

type-check: ## Type check with mypy
	@echo "$(BLUE)🔎 Type checking...$(NC)"
	mypy app/
	@echo "$(GREEN)✅ Type checking complete!$(NC)"

qa: format lint type-check test ## Run all quality assurance checks

# Development workflow
dev-setup: install ## Set up development environment
	@echo "$(BLUE)🛠️ Setting up development environment...$(NC)"
	cp .env.example .env 2>/dev/null || true
	@echo "$(GREEN)✅ Development environment ready!$(NC)"
	@echo "$(YELLOW)💡 Don't forget to configure your .env file$(NC)"

# CI/CD helpers
ci-build: build ## Build for CI/CD
	@echo "$(GREEN)✅ CI build complete$(NC)"

ci-test: test ## Test for CI/CD
	@echo "$(GREEN)✅ CI tests complete$(NC)"

ci-push: push ## Push for CI/CD
	@echo "$(GREEN)✅ CI push complete$(NC)"