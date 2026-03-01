#!/usr/bin/env python3
"""
FastAPI Worksheet Service

Integrates the worksheet generator with FastAPI to create a scalable
worksheet generation and scoring service.
"""

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel
from typing import Dict, List, Optional, Any
import uvicorn
from datetime import datetime
import json

from worksheet_generator import WorksheetGenerator, Worksheet, Question

# FastAPI app
app = FastAPI(
    title="Math Worksheet Service",
    description="Generate infinite math worksheets with automatic scoring",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global generator instance
generator = WorksheetGenerator()

# Pydantic models
class WorksheetRequest(BaseModel):
    student_id: str
    worksheet_id: str = "EX02"

class AnswerSubmission(BaseModel):
    student_id: str
    worksheet_id: str = "EX02"
    answers: Dict[int, float]
    session_id: Optional[str] = None
    student_name: Optional[str] = None

class QuestionResponse(BaseModel):
    number: int
    expression: str
    description: str
    x_value: float

class WorksheetResponse(BaseModel):
    worksheet_id: str
    title: str
    student_id: str
    quiz_number: int
    questions: List[QuestionResponse]
    session_id: str

class ScoreResponse(BaseModel):
    student_id: str
    worksheet_id: str
    quiz_number: int
    session_id: Optional[str]
    score: Dict[str, Any]
    feedback: List[Dict[str, Any]]
    timestamp: str


@app.get("/", response_class=HTMLResponse)
async def home():
    """Home page with API documentation"""
    return """
    <html>
        <head>
            <title>Math Worksheet API</title>
            <style>
                body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                .endpoint { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }
                .method { color: white; padding: 4px 8px; border-radius: 3px; font-weight: bold; }
                .get { background: #28a745; }
                .post { background: #007bff; }
                code { background: #f8f9fa; padding: 2px 4px; border-radius: 3px; }
            </style>
        </head>
        <body>
            <h1>🧮 Math Worksheet API</h1>
            <p>Generate infinite variations of math worksheets with automatic scoring!</p>
            
            <h2>🚀 Key Features</h2>
            <ul>
                <li><strong>Infinite Variations</strong>: Each student ID generates different problems</li>
                <li><strong>Consistent Results</strong>: Same student always gets same worksheet</li>
                <li><strong>Automatic Scoring</strong>: Instant feedback and grading</li>
                <li><strong>No Storage Required</strong>: Problems generated algorithmically</li>
            </ul>
            
            <h2>📡 API Endpoints</h2>
            
            <div class="endpoint">
                <span class="method get">GET</span> <code>/worksheet/{worksheet_id}</code>
                <p>Generate a worksheet for a student</p>
                <strong>Query Parameters:</strong> <code>student_id</code> (required)
            </div>
            
            <div class="endpoint">
                <span class="method post">POST</span> <code>/worksheet/{worksheet_id}/submit</code>
                <p>Submit answers and get score</p>
                <strong>Body:</strong> <code>{"student_id": "123", "answers": {"1": 7, "2": 5}}</code>
            </div>
            
            <div class="endpoint">
                <span class="method get">GET</span> <code>/student/{student_id}/worksheets</code>
                <p>List available worksheets for a student</p>
            </div>
            
            <div class="endpoint">
                <span class="method get">GET</span> <code>/health</code>
                <p>API health check</p>
            </div>
            
            <h2>📋 Example Usage</h2>
            <pre><code>
# Get a worksheet
curl "http://localhost:8000/worksheet/EX02?student_id=1000"

# Submit answers
curl -X POST "http://localhost:8000/worksheet/EX02/submit" \\
     -H "Content-Type: application/json" \\
     -d '{"student_id": "1000", "answers": {"1": 7, "2": 5, "3": 8}}'
            </code></pre>
            
            <p><a href="/docs">📚 View Interactive API Documentation</a></p>
        </body>
    </html>
    """

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat(),
        "generator_ready": True
    }

@app.get("/worksheet/{worksheet_id}", response_model=WorksheetResponse)
async def get_worksheet(
    worksheet_id: str,
    student_id: str
):
    """Generate a worksheet for a student"""
    try:
        # Generate worksheet
        worksheet = generator.generate_worksheet(student_id, worksheet_id)
        
        # Convert to response format
        questions = [
            QuestionResponse(
                number=q.number,
                expression=q.expression,
                description=q.description,
                x_value=q.x_value
            )
            for q in worksheet.questions
        ]
        
        # Generate session ID
        session_id = f"ws_{student_id}_{worksheet_id}_{worksheet.quiz_number}"
        
        return WorksheetResponse(
            worksheet_id=worksheet.worksheet_id,
            title=worksheet.title,
            student_id=worksheet.student_id,
            quiz_number=worksheet.quiz_number,
            questions=questions,
            session_id=session_id
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/worksheet/{worksheet_id}/submit", response_model=ScoreResponse)
async def submit_worksheet(
    worksheet_id: str,
    submission: AnswerSubmission
):
    """Submit worksheet answers and get score"""
    try:
        # Generate the same worksheet to get correct answers
        worksheet = generator.generate_worksheet(submission.student_id, worksheet_id)
        
        # Calculate score
        result = generator.calculate_score(worksheet, submission.answers)
        
        # Generate session ID if not provided
        session_id = submission.session_id or f"ws_{submission.student_id}_{worksheet_id}_{worksheet.quiz_number}"
        
        return ScoreResponse(
            student_id=result["student_id"],
            worksheet_id=result["worksheet_id"],
            quiz_number=result["quiz_number"],
            session_id=session_id,
            score=result["score"],
            feedback=result["feedback"],
            timestamp=datetime.utcnow().isoformat()
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/student/{student_id}/worksheets")
async def get_student_worksheets(student_id: str):
    """Get available worksheets for a student"""
    # In a full implementation, this would list all available worksheet types
    available_worksheets = [
        {
            "id": "EX02", 
            "title": "Expressions & Equations",
            "description": "Translate word problems into mathematical expressions",
            "subject": "Algebra",
            "difficulty": "Basic"
        },
        # Add more as you implement them
    ]
    
    # Show what each would generate for this student
    worksheets_with_preview = []
    for ws_info in available_worksheets:
        try:
            worksheet = generator.generate_worksheet(student_id, ws_info["id"])
            preview = {
                **ws_info,
                "quiz_number": worksheet.quiz_number,
                "question_count": len(worksheet.questions),
                "sample_question": worksheet.questions[0].expression if worksheet.questions else None
            }
            worksheets_with_preview.append(preview)
        except:
            # If generation fails, still show basic info
            worksheets_with_preview.append(ws_info)
    
    return {
        "student_id": student_id,
        "available_worksheets": worksheets_with_preview
    }

@app.get("/worksheet/{worksheet_id}/demo")
async def demo_worksheet(worksheet_id: str):
    """Demo endpoint showing different students get different problems"""
    demo_students = ["1000", "1001", "2000", "alice", "bob"]
    demos = []
    
    for student_id in demo_students:
        try:
            worksheet = generator.generate_worksheet(student_id, worksheet_id)
            demos.append({
                "student_id": student_id,
                "quiz_number": worksheet.quiz_number,
                "sample_questions": [
                    {
                        "number": q.number,
                        "expression": q.expression,
                        "x_value": q.x_value,
                        "answer": q.correct_answer
                    }
                    for q in worksheet.questions[:3]  # First 3 questions
                ]
            })
        except Exception as e:
            demos.append({
                "student_id": student_id,
                "error": str(e)
            })
    
    return {
        "worksheet_id": worksheet_id,
        "description": "Different problems generated for each student ID",
        "demos": demos
    }

# Integration endpoint for external systems
@app.post("/webhook/score")
async def score_webhook(
    submission: AnswerSubmission,
    webhook_url: Optional[str] = None
):
    """Score worksheet and optionally send to external webhook"""
    # Score the worksheet
    score_response = await submit_worksheet(submission.worksheet_id, submission)
    
    # If webhook URL provided, send results there
    if webhook_url:
        try:
            import httpx
            async with httpx.AsyncClient() as client:
                await client.post(webhook_url, json=score_response.dict())
        except Exception as e:
            # Log error but don't fail the scoring
            pass
    
    return score_response

# Add more endpoints as needed...

if __name__ == "__main__":
    print("🚀 Starting Math Worksheet Service...")
    print("📚 API Documentation: http://localhost:8000/docs")
    print("🏠 Home Page: http://localhost:8000")
    
    uvicorn.run(
        "worksheet_service:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )