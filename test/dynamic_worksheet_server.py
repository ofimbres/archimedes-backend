#!/usr/bin/env python3
"""
Dynamic Worksheet Server - Generate worksheets with student IDs
This solves the "fixed ID" problem by generating personalized HTML on-the-fly
"""

from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
import re
import os
from typing import Dict, Any

app = FastAPI(title="Dynamic Worksheet Generator")

# Serve static files
app.mount("/static", StaticFiles(directory="mini-quizzes"), name="static")

def inject_student_id_into_worksheet(html_content: str, student_id: str) -> str:
    """Inject student ID into worksheet HTML and trigger calculations"""
    
    # 1. Replace the student ID input field default value
    html_content = re.sub(
        r"(<input id='iJ60'[^>]*?)(\s*/>)",
        rf'\1 value="{student_id}"\2',
        html_content
    )
    
    # 2. Also set the data-default attribute if it exists
    html_content = re.sub(
        r"(data-default=')[^']*(')",
        rf'\1{student_id}\2',
        html_content
    )
    
    # 3. Add JavaScript to ensure the worksheet calculates after loading
    calculation_script = f"""
<script>
// Ensure student ID is set and calculations are triggered
window.addEventListener('load', function() {{
    console.log('Setting student ID: {student_id}');
    
    // Set the student ID in the input field
    const studentInput = document.getElementById('iJ60');
    if (studentInput) {{
        studentInput.value = '{student_id}';
        
        // Trigger the change event to start calculations
        if (typeof calculate !== 'undefined') {{
            console.log('Triggering worksheet calculation...');
            calculate('J60');
        }}
        
        // Also trigger change event manually
        const changeEvent = new Event('change', {{ bubbles: true }});
        studentInput.dispatchEvent(changeEvent);
        
        console.log('Student ID set and calculations triggered');
    }}
}});
</script>
"""
    
    # Insert the script before closing </body> tag
    html_content = html_content.replace('</body>', calculation_script + '</body>')
    
    return html_content

@app.get("/")
async def root():
    """API documentation"""
    return {
        "message": "Dynamic Worksheet Generator API",
        "endpoints": {
            "/worksheet/{worksheet_id}/{student_id}": "Get personalized worksheet",
            "/demo": "Demo page",
            "/static/{filename}": "Static worksheet files"
        },
        "examples": {
            "student_1000": "/worksheet/AL01/1000",
            "student_1001": "/worksheet/AL01/1001", 
            "demo": "/demo"
        }
    }

@app.get("/worksheet/{worksheet_id}/{student_id}", response_class=HTMLResponse)
async def get_personalized_worksheet(worksheet_id: str, student_id: str):
    """Generate personalized worksheet for specific student"""
    
    # Find the worksheet file
    worksheet_file = f"mini-quizzes/{worksheet_id}.html"
    
    if not os.path.exists(worksheet_file):
        raise HTTPException(status_code=404, detail=f"Worksheet {worksheet_id} not found")
    
    # Read the original worksheet
    try:
        with open(worksheet_file, 'r', encoding='utf-8') as f:
            original_html = f.read()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error reading worksheet: {str(e)}")
    
    # Inject student ID and personalization
    personalized_html = inject_student_id_into_worksheet(original_html, student_id)
    
    # Add header comment
    header_comment = f"""<!-- 
PERSONALIZED WORKSHEET
Student ID: {student_id}
Worksheet: {worksheet_id}
Generated: {__import__('datetime').datetime.now().isoformat()}
-->

"""
    
    return HTMLResponse(content=header_comment + personalized_html)

@app.get("/demo", response_class=HTMLResponse)
async def demo_page():
    """Interactive demo page"""
    
    demo_html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🧮 Dynamic Worksheet Demo</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .worksheet-frame { width: 100%; height: 600px; border: 2px solid #ccc; margin: 20px 0; }
        .controls { background: #f0f0f0; padding: 15px; margin: 10px 0; border-radius: 5px; }
        button { background: #007acc; color: white; border: none; padding: 10px 20px; margin: 5px; border-radius: 3px; cursor: pointer; }
        button:hover { background: #005999; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; padding: 10px; margin: 10px 0; border-radius: 3px; }
        .comparison { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧮 Dynamic Student Worksheet Demo</h1>
        
        <div class="success">
            <strong>✅ SOLUTION:</strong> Each student gets a different URL with their ID pre-injected!
        </div>
        
        <div class="controls">
            <h3>🎯 Try Different Students</h3>
            <button onclick="loadStudent('1000')">Student 1000</button>
            <button onclick="loadStudent('1001')">Student 1001</button>
            <button onclick="loadStudent('2000')">Student 2000</button>
            <button onclick="loadStudent('1234')">Student 1234</button>
            <br><br>
            <label>Custom Student: <input type="text" id="customId" placeholder="Enter student ID" /></label>
            <button onclick="loadCustomStudent()">Load Worksheet</button>
        </div>
        
        <div id="currentInfo" style="margin: 20px 0; padding: 10px; background: #e8f4f8; border-radius: 5px;">
            <strong>Current:</strong> No student loaded
        </div>
        
        <iframe id="worksheetFrame" class="worksheet-frame" src="" title="Student Worksheet"></iframe>
        
        <div class="comparison">
            <div>
                <h4>🎓 Student 1000</h4>
                <iframe src="/worksheet/AL01/1000" style="width: 100%; height: 400px; border: 1px solid #ccc;"></iframe>
            </div>
            <div>
                <h4>👩‍🎓 Student 1001</h4>
                <iframe src="/worksheet/AL01/1001" style="width: 100%; height: 400px; border: 1px solid #ccc;"></iframe>
            </div>
        </div>
        
        <div style="background: #fff3cd; padding: 15px; margin: 20px 0; border-radius: 5px;">
            <h4>🎯 How It Works:</h4>
            <ol>
                <li><strong>URL-based IDs:</strong> Each student gets a unique URL like <code>/worksheet/AL01/1000</code></li>
                <li><strong>Server-side injection:</strong> Student ID is injected into HTML before sending</li>
                <li><strong>Auto-calculation:</strong> JavaScript automatically triggers worksheet recalculation</li>
                <li><strong>Different questions:</strong> LCG algorithm generates different problems per student</li>
            </ol>
        </div>
    </div>

    <script>
        function loadStudent(studentId) {
            const frame = document.getElementById('worksheetFrame');
            const url = `/worksheet/AL01/${studentId}`;
            frame.src = url;
            
            document.getElementById('currentInfo').innerHTML = 
                `<strong>Current:</strong> Student ${studentId} | URL: <a href="${url}" target="_blank">${url}</a>`;
        }
        
        function loadCustomStudent() {
            const studentId = document.getElementById('customId').value.trim();
            if (!studentId) {
                alert('Please enter a student ID');
                return;
            }
            loadStudent(studentId);
        }
        
        // Auto-load student 1000 on page load
        window.addEventListener('load', function() {
            loadStudent('1000');
        });
    </script>
</body>
</html>"""
    
    return HTMLResponse(content=demo_html)

# Add CORS middleware if needed
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

if __name__ == "__main__":
    import uvicorn
    print("🚀 Starting Dynamic Worksheet Server...")
    print("📋 Available endpoints:")
    print("   • http://localhost:8001/demo - Interactive demo")
    print("   • http://localhost:8001/worksheet/AL01/1000 - Student 1000's worksheet")
    print("   • http://localhost:8001/worksheet/AL01/1001 - Student 1001's worksheet")
    print("   • http://localhost:8001/ - API documentation")
    
    uvicorn.run(app, host="0.0.0.0", port=8001)