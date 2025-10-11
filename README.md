# Archimedes Python Microservice
# A clean, modern Python service to complement the Java backend

## Setup
```bash
cd python-service
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
```

## Architecture
- **FastAPI** for modern, fast API development
- **SQLAlchemy** for clean ORM (much simpler than JPA!)
- **Pydantic** for automatic data validation
- **Async/await** for high performance
- **PostgreSQL** shared with Java backend

## Services
This Python service will handle:
- User ID generation (cleaner implementation)
- Analytics and reporting
- Real-time features
- Background tasks

## API Endpoints
- `GET /health` - Health check
- `POST /generate/student-id` - Generate sequential student ID
- `POST /generate/teacher-id` - Generate sequential teacher ID
- `GET /analytics/school/{school_id}` - School analytics

## Benefits over Java
- 🚀 **10x less verbose** than Java/Spring Boot
- 🔥 **Modern async/await** patterns
- 📝 **Automatic API docs** with FastAPI
- 🎯 **Simple, readable code**
- ⚡ **Fast development** cycle