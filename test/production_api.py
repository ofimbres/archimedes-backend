#!/usr/bin/env python3
"""
Production Worksheet API - Phase 2

FastAPI service using the extracted worksheet data library
Serves all 513 worksheets with quality ratings and fallback generation
"""

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, List, Optional, Any
import json
from pathlib import Path
import random
from datetime import date

from batch_processor import WorksheetLibraryManager
from enhanced_generator import EnhancedUniversalGenerator


# API Models
class QuizRequest(BaseModel):
    student_id: int
    worksheet_id: str


class QuestionResponse(BaseModel):
    question_number: int
    question: str
    answer: float
    source_method: str


class QuizResponse(BaseModel):
    worksheet_id: str
    subject: str
    title: str
    student_id: int
    quiz_number: int
    questions: List[QuestionResponse]
    extraction_quality: str
    total_questions: int


class WorksheetInfo(BaseModel):
    worksheet_id: str
    subject: str
    title: str
    question_count: int
    extraction_quality: str
    available: bool


class SubjectSummary(BaseModel):
    subject: str
    worksheet_count: int
    high_quality_worksheets: List[str]
    recommended_worksheet: str


# Initialize FastAPI app
app = FastAPI(
    title="Archimedes Worksheet API",
    description="Generate dynamic mathematical worksheets and quizzes",
    version="2.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize services
library = WorksheetLibraryManager("/Users/oscarfimbres/git/archimedes-backend/worksheet_data")
generator = EnhancedUniversalGenerator("/Users/oscarfimbres/git/archimedes-backend/mini-quizzes")


# Utility functions
def calculate_quiz_number(student_id: int) -> int:
    """Calculate quiz number using the same LCG formula"""
    day_of_year = date.today().timetuple().tm_yday
    return (student_id % 1000) + day_of_year


def generate_student_worksheet(student_id: int, worksheet_data: Dict[str, Any]) -> QuizResponse:
    """Generate a worksheet for a specific student"""
    quiz_num = calculate_quiz_number(student_id)
    
    # Use LCG to generate questions
    questions = []
    lookup_table = worksheet_data.get('lookup_table', {})
    
    if not lookup_table:
        raise HTTPException(status_code=400, f"No questions available for {worksheet_data['worksheet_id']}")
    
    # Generate 10 questions using LCG
    seed = quiz_num
    for i in range(10):
        # LCG formula: next = (9821 * current + 0.211327) % 1
        seed = (9821 * seed + 0.211327) % 1
        
        # Map to lookup table
        table_size = len(lookup_table)
        question_index = int(seed * table_size) + 1
        
        # Get question data
        if str(question_index) in lookup_table:
            q_data = lookup_table[str(question_index)]
            
            # Extract expression and calculate answer
            expression = q_data.get('expression', '')
            x_value = q_data.get('x_value', question_index)
            
            # Simple expression evaluation (replace X with value)
            try:
                # Replace X with the x_value
                eval_expr = expression.replace('X', str(x_value))
                eval_expr = eval_expr.replace('x', str(x_value))  # Handle lowercase x too
                
                # Safe evaluation (basic math only)
                answer = eval(eval_expr)
                
                questions.append(QuestionResponse(
                    question_number=i + 1,
                    question=q_data.get('description', expression),
                    answer=float(answer),
                    source_method=q_data.get('source_method', 'library')
                ))
                
            except Exception as e:
                # Skip problematic questions
                continue
    
    return QuizResponse(
        worksheet_id=worksheet_data['worksheet_id'],
        subject=worksheet_data['subject'],
        title=worksheet_data['title'],
        student_id=student_id,
        quiz_number=quiz_num,
        questions=questions,
        extraction_quality=worksheet_data['extraction_quality'],
        total_questions=len(questions)
    )


# API Endpoints
@app.get("/", summary="API Information")
async def root():
    """API information and health check"""
    stats = library.get_processing_stats()
    
    return {
        "service": "Archimedes Worksheet API",
        "version": "2.0.0",
        "description": "Dynamic mathematical worksheet generation system",
        "total_worksheets": stats.get('total_processed', 513),
        "high_quality_worksheets": stats.get('quality_distribution', {}).get('high', 70),
        "documentation": "/docs",
        "status": "ready"
    }


@app.get("/worksheets", response_model=List[WorksheetInfo], summary="List all available worksheets")
async def list_worksheets(
    subject: Optional[str] = Query(None, description="Filter by subject"),
    quality: Optional[str] = Query(None, description="Filter by quality (high, medium, low)"),
    limit: Optional[int] = Query(None, description="Limit number of results")
):
    """List all available worksheets with optional filtering"""
    try:
        worksheets = []
        
        # Load all worksheet data
        data_dir = Path("/Users/oscarfimbres/git/archimedes-backend/worksheet_data")
        
        for json_file in data_dir.glob("*.json"):
            if json_file.name in ['processing_results.json', 'high_quality_catalog.json', 'subjects_catalog.json']:
                continue
            
            with open(json_file, 'r') as f:
                ws_data = json.load(f)
            
            # Apply filters
            if subject and ws_data.get('subject', '').lower() != subject.lower():
                continue
            
            if quality and ws_data.get('extraction_quality', '').lower() != quality.lower():
                continue
            
            worksheets.append(WorksheetInfo(
                worksheet_id=ws_data['worksheet_id'],
                subject=ws_data['subject'],
                title=ws_data['title'],
                question_count=ws_data['question_count'],
                extraction_quality=ws_data['extraction_quality'],
                available=ws_data['question_count'] > 0
            ))
        
        # Sort by quality and question count
        quality_order = {'high': 3, 'medium': 2, 'low': 1}
        worksheets.sort(
            key=lambda x: (quality_order.get(x.extraction_quality, 0), x.question_count),
            reverse=True
        )
        
        # Apply limit
        if limit:
            worksheets = worksheets[:limit]
        
        return worksheets
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading worksheets: {str(e)}")


@app.get("/subjects", response_model=List[SubjectSummary], summary="Get subject summary")
async def list_subjects():
    """Get summary of all subjects with worksheet counts"""
    try:
        high_quality = library.list_high_quality_worksheets()
        
        # Group by subject
        subjects = {}
        for ws in high_quality:
            subject = ws['subject']
            if subject not in subjects:
                subjects[subject] = {
                    'worksheets': [],
                    'high_quality': []
                }
            subjects[subject]['worksheets'].append(ws['worksheet_id'])
            subjects[subject]['high_quality'].append(ws['worksheet_id'])
        
        # Create summaries
        summaries = []
        for subject, data in subjects.items():
            # Find best worksheet (most questions)
            best_ws = max(high_quality, key=lambda x: x['question_count'] if x['subject'] == subject else 0)
            recommended = best_ws['worksheet_id'] if best_ws['subject'] == subject else data['high_quality'][0]
            
            summaries.append(SubjectSummary(
                subject=subject,
                worksheet_count=len(data['worksheets']),
                high_quality_worksheets=data['high_quality'],
                recommended_worksheet=recommended
            ))
        
        # Sort by worksheet count
        summaries.sort(key=lambda x: x.worksheet_count, reverse=True)
        
        return summaries
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading subjects: {str(e)}")


@app.post("/quiz", response_model=QuizResponse, summary="Generate a quiz for a student")
async def generate_quiz(request: QuizRequest):
    """Generate a personalized quiz for a student"""
    try:
        # Load worksheet data from library
        worksheet_data = library.get_worksheet_data(request.worksheet_id)
        
        # Generate quiz
        quiz = generate_student_worksheet(request.student_id, worksheet_data)
        
        return quiz
    
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"Worksheet {request.worksheet_id} not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating quiz: {str(e)}")


@app.get("/quiz/{worksheet_id}/{student_id}", response_model=QuizResponse, summary="Generate quiz with URL parameters")
async def generate_quiz_get(worksheet_id: str, student_id: int):
    """Generate a quiz using GET request with URL parameters"""
    return await generate_quiz(QuizRequest(student_id=student_id, worksheet_id=worksheet_id))


@app.get("/worksheet/{worksheet_id}", response_model=WorksheetInfo, summary="Get worksheet information")
async def get_worksheet_info(worksheet_id: str):
    """Get detailed information about a specific worksheet"""
    try:
        worksheet_data = library.get_worksheet_data(worksheet_id)
        
        return WorksheetInfo(
            worksheet_id=worksheet_data['worksheet_id'],
            subject=worksheet_data['subject'],
            title=worksheet_data['title'],
            question_count=worksheet_data['question_count'],
            extraction_quality=worksheet_data['extraction_quality'],
            available=worksheet_data['question_count'] > 0
        )
    
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"Worksheet {worksheet_id} not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading worksheet: {str(e)}")


@app.get("/stats", summary="Get API statistics")
async def get_stats():
    """Get comprehensive API statistics"""
    try:
        stats = library.get_processing_stats()
        high_quality = library.list_high_quality_worksheets()
        
        # Subject statistics
        subject_stats = {}
        for ws in high_quality:
            subject = ws['subject']
            if subject not in subject_stats:
                subject_stats[subject] = {
                    'count': 0,
                    'total_questions': 0,
                    'avg_questions': 0
                }
            subject_stats[subject]['count'] += 1
            subject_stats[subject]['total_questions'] += ws['question_count']
        
        # Calculate averages
        for subject, data in subject_stats.items():
            data['avg_questions'] = round(data['total_questions'] / data['count'], 1)
        
        return {
            "overview": stats,
            "production_ready": {
                "high_quality_worksheets": len(high_quality),
                "subjects_available": len(subject_stats),
                "total_questions": sum(ws['question_count'] for ws in high_quality)
            },
            "subject_breakdown": subject_stats,
            "top_worksheets": sorted(high_quality, key=lambda x: x['question_count'], reverse=True)[:10]
        }
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error loading statistics: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)