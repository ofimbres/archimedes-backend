#!/usr/bin/env python3
"""
HTML Worksheet Embedding Analysis
Analyzing how WebWorksheet HTML files can be embedded and whether scores can be extracted.
"""

import json
from typing import Dict, Any, List

def analyze_worksheet_embedding():
    """Analyze the feasibility of embedding WebWorksheet HTML files"""
    
    analysis = {
        "embedding_feasibility": {
            "can_embed": True,
            "challenges": [],
            "solutions": [],
            "score_extraction": "possible_with_modifications"
        },
        "technical_details": {},
        "recommendations": []
    }
    
    # RENDERING ANALYSIS
    analysis["technical_details"]["rendering"] = {
        "javascript_dependencies": [
            "http://webworksheet.com/release/030701/webworksheet.js",
            "http://webworksheet.com/release/030701/webworksheetUpdate.js", 
            "m4u_extended.js (local file)"
        ],
        "external_dependencies": "Relies on webworksheet.com servers",
        "css_styling": "Self-contained with embedded styles",
        "dynamic_generation": "Uses LCG algorithm for problem generation",
        "student_interaction": "Input fields with onChange events"
    }
    
    # EMBEDDING CHALLENGES
    analysis["embedding_feasibility"]["challenges"] = [
        "External JavaScript dependencies from webworksheet.com",
        "Global JavaScript variables may conflict with parent page",
        "Form submission tries to email results (not modern web app friendly)",
        "CSS styles may conflict with parent page styling",
        "No built-in API for programmatic score extraction",
        "Hard-coded student ID input (J60) and submission logic"
    ]
    
    # SOLUTIONS
    analysis["embedding_feasibility"]["solutions"] = [
        "Use iframe to sandbox the worksheet (isolates JS/CSS conflicts)", 
        "Download and serve webworksheet.js files locally",
        "Override submitForm() function to capture scores instead of emailing",
        "Add CSS scoping or iframe to prevent style conflicts",
        "Create JavaScript bridge for parent-child communication",
        "Modify HTML to disable email submission and enable score extraction"
    ]
    
    # SCORE EXTRACTION ANALYSIS
    analysis["technical_details"]["score_extraction"] = {
        "current_mechanism": "Email submission with Grade: P60 attachment",
        "grade_calculation": "Performed by webworksheet.js calculate() functions",
        "student_answers": "Stored in input fields with IDs like iG65, iO65, etc.",
        "correct_answers": "Calculated dynamically using VLOOKUP formulas",
        "extraction_methods": [
            "JavaScript DOM access to input field values",
            "Override submitForm() to intercept submission data", 
            "Access calculated grade in cell P60",
            "Monitor onChange events to track student progress"
        ]
    }
    
    # RECOMMENDATIONS
    analysis["recommendations"] = [
        "Use iframe embedding for safety and isolation",
        "Create a wrapper script to extract scores via postMessage API",
        "Host webworksheet.js files locally for reliability",
        "Implement score extraction via DOM manipulation",
        "Build a modern API wrapper around the legacy worksheets"
    ]
    
    return analysis

def create_embedding_demo():
    """Create a demo HTML page showing how to embed and extract scores"""
    
    demo_html = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WebWorksheet Embedding Demo</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .worksheet-frame { width: 100%; height: 800px; border: 2px solid #ccc; margin: 20px 0; }
        .controls { background: #f0f0f0; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .score-display { background: #e8f5e8; padding: 10px; margin: 10px 0; border-radius: 5px; }
        button { background: #007acc; color: white; border: none; padding: 10px 20px; margin: 5px; border-radius: 3px; cursor: pointer; }
        button:hover { background: #005999; }
        .status { margin: 10px 0; padding: 10px; border-radius: 3px; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .warning { background: #fff3cd; color: #856404; border: 1px solid #ffeaa7; }
        .info { background: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🧮 WebWorksheet Embedding Demo</h1>
        
        <div class="status info">
            <strong>Status:</strong> Demonstrating how to embed AL01 worksheet and extract scores
        </div>
        
        <div class="controls">
            <h3>📋 Student Information</h3>
            <label>Student ID: <input type="text" id="studentId" value="1000" /></label>
            <button onclick="loadWorksheet()">Load Worksheet</button>
            <button onclick="extractScore()">Extract Score</button>
            <button onclick="resetWorksheet()">Reset</button>
        </div>
        
        <div class="score-display">
            <h3>📊 Score Information</h3>
            <div id="scoreInfo">No score extracted yet</div>
            <div id="studentAnswers">Student answers will appear here</div>
        </div>
        
        <!-- Embedded Worksheet -->
        <iframe 
            id="worksheetFrame" 
            class="worksheet-frame"
            src=""
            title="WebWorksheet AL01"
            sandbox="allow-scripts allow-same-origin allow-forms">
        </iframe>
        
        <div class="status warning">
            <strong>Note:</strong> This demo shows the concept. Actual implementation requires:
            <ul>
                <li>Hosting webworksheet.js files locally (they're currently external)</li>
                <li>Modifying the worksheet HTML to disable email submission</li>
                <li>Adding score extraction JavaScript</li>
                <li>Implementing proper iframe communication</li>
            </ul>
        </div>
    </div>

    <script>
        // Demo JavaScript for worksheet interaction
        
        function loadWorksheet() {
            const studentId = document.getElementById('studentId').value;
            const frame = document.getElementById('worksheetFrame');
            
            // In real implementation, you'd pass the student ID to the worksheet
            // For now, just load the static HTML file
            frame.src = 'mini-quizzes/AL01.html';
            
            updateStatus('Loading worksheet for student ' + studentId + '...', 'info');
            
            // Listen for worksheet load
            frame.onload = function() {
                updateStatus('Worksheet loaded successfully!', 'success');
                setupScoreExtraction();
            };
        }
        
        function setupScoreExtraction() {
            // In a real implementation, you would:
            // 1. Inject score extraction JavaScript into the iframe
            // 2. Override the submitForm() function
            // 3. Set up postMessage communication
            
            const frame = document.getElementById('worksheetFrame');
            
            try {
                // This would work if same-origin and no sandbox restrictions
                const frameDoc = frame.contentDocument || frame.contentWindow.document;
                
                // Add score extraction to the worksheet
                const script = frameDoc.createElement('script');
                script.textContent = `
                    // Override submitForm to capture scores instead of emailing
                    window.originalSubmitForm = window.submitForm;
                    window.submitForm = function(buttonId) {
                        // Extract student answers
                        const answers = extractStudentAnswers();
                        const score = calculateScore();
                        
                        // Send to parent window
                        window.parent.postMessage({
                            type: 'scoreData',
                            data: { answers, score }
                        }, '*');
                        
                        return false; // Prevent original email submission
                    };
                    
                    function extractStudentAnswers() {
                        const answers = {};
                        const inputs = document.querySelectorAll('input[id^="i"]');
                        inputs.forEach(input => {
                            if (input.value.trim()) {
                                answers[input.id] = input.value.trim();
                            }
                        });
                        return answers;
                    }
                    
                    function calculateScore() {
                        // Access the grade cell (P60) that webworksheet calculates
                        const gradeCell = document.getElementById('P60');
                        return gradeCell ? gradeCell.textContent : 'Unknown';
                    }
                `;
                frameDoc.head.appendChild(script);
                
                updateStatus('Score extraction setup complete!', 'success');
                
            } catch (error) {
                updateStatus('Cannot access iframe content (CORS/sandbox restrictions)', 'warning');
                console.log('Iframe access error:', error);
            }
        }
        
        function extractScore() {
            const frame = document.getElementById('worksheetFrame');
            
            try {
                const frameDoc = frame.contentDocument || frame.contentWindow.document;
                
                // Extract current student answers
                const answers = {};
                const inputs = frameDoc.querySelectorAll('input[id^="i"]');
                inputs.forEach(input => {
                    if (input.value && input.value.trim()) {
                        answers[input.id] = input.value.trim();
                    }
                });
                
                // Try to get calculated score
                const gradeCell = frameDoc.getElementById('P60');
                const score = gradeCell ? gradeCell.textContent : 'Not calculated yet';
                
                // Display results
                displayScore(answers, score);
                
            } catch (error) {
                updateStatus('Cannot extract score due to iframe restrictions', 'warning');
                
                // Show alternative approach
                displayScore({
                    'simulated': 'Due to CORS restrictions, showing mock data',
                    'iG65': '15',
                    'iG66': '23', 
                    'iG67': '8'
                }, '75%');
            }
        }
        
        function displayScore(answers, score) {
            const scoreInfo = document.getElementById('scoreInfo');
            const studentAnswers = document.getElementById('studentAnswers');
            
            scoreInfo.innerHTML = `
                <strong>Current Score:</strong> ${score}<br>
                <strong>Questions Answered:</strong> ${Object.keys(answers).length}
            `;
            
            studentAnswers.innerHTML = `
                <strong>Student Answers:</strong><br>
                ${Object.entries(answers).map(([id, value]) => 
                    `${id}: ${value}`
                ).join('<br>')}
            `;
            
            updateStatus('Score extracted successfully!', 'success');
        }
        
        function resetWorksheet() {
            const frame = document.getElementById('worksheetFrame');
            frame.src = '';
            document.getElementById('scoreInfo').textContent = 'No score extracted yet';
            document.getElementById('studentAnswers').textContent = 'Student answers will appear here';
            updateStatus('Worksheet reset', 'info');
        }
        
        function updateStatus(message, type) {
            // Update status display (you could add a status div)
            console.log(`[${type.toUpperCase()}] ${message}`);
        }
        
        // Listen for messages from worksheet iframe
        window.addEventListener('message', function(event) {
            if (event.data.type === 'scoreData') {
                displayScore(event.data.data.answers, event.data.data.score);
            }
        });
        
        // Auto-load worksheet on page load
        window.addEventListener('load', function() {
            setTimeout(loadWorksheet, 1000);
        });
    </script>
</body>
</html>"""
    
    return demo_html

def create_score_extractor_js():
    """Create JavaScript code for extracting scores from embedded worksheets"""
    
    js_code = """
// WebWorksheet Score Extractor
// Inject this into embedded worksheets to extract scores

(function() {
    'use strict';
    
    // Wait for WebWorksheet to load
    function waitForWebWorksheet(callback) {
        if (typeof calculate !== 'undefined' && typeof submitForm !== 'undefined') {
            callback();
        } else {
            setTimeout(() => waitForWebWorksheet(callback), 100);
        }
    }
    
    waitForWebWorksheet(function() {
        console.log('WebWorksheet Score Extractor loaded');
        
        // Override submit function to capture scores
        window.originalSubmitForm = window.submitForm;
        window.submitForm = function(buttonId) {
            const scoreData = extractCompleteScore();
            
            // Send to parent window
            if (window.parent !== window) {
                window.parent.postMessage({
                    type: 'worksheetSubmitted',
                    data: scoreData
                }, '*');
            }
            
            // Also store locally
            localStorage.setItem('worksheetScore', JSON.stringify(scoreData));
            
            // Prevent email submission in embedded mode
            return false;
        };
        
        // Extract all score data
        function extractCompleteScore() {
            const data = {
                studentId: getStudentId(),
                worksheetId: getWorksheetId(),
                answers: extractAnswers(),
                score: extractScore(),
                timestamp: new Date().toISOString(),
                questions: extractQuestions()
            };
            
            return data;
        }
        
        function getStudentId() {
            // Student ID is typically in input iJ60
            const studentInput = document.getElementById('iJ60');
            return studentInput ? studentInput.value : 'unknown';
        }
        
        function getWorksheetId() {
            const title = document.title || 'unknown';
            return title;
        }
        
        function extractAnswers() {
            const answers = {};
            
            // Find all input fields that start with 'i' (student answer fields)
            const inputs = document.querySelectorAll('input[id^="i"]');
            inputs.forEach(input => {
                if (input.value && input.value.trim() && input.id !== 'iZZ99') {
                    answers[input.id] = {
                        value: input.value.trim(),
                        cellRef: input.id.replace('i', ''),
                        isNumeric: !isNaN(parseFloat(input.value))
                    };
                }
            });
            
            return answers;
        }
        
        function extractScore() {
            // Score is typically calculated in cell P60
            const gradeCell = document.getElementById('P60');
            if (gradeCell) {
                return {
                    percentage: gradeCell.textContent || gradeCell.value,
                    calculated: true
                };
            }
            
            // Manual calculation if no grade cell
            return {
                percentage: 'Not calculated',
                calculated: false
            };
        }
        
        function extractQuestions() {
            const questions = [];
            
            // Extract generated questions from visible cells
            // This would need to be customized based on worksheet layout
            const questionCells = document.querySelectorAll('td[id^="B"]:not([style*="display:none"])');
            
            questionCells.forEach(cell => {
                if (cell.textContent && cell.textContent.trim()) {
                    questions.push({
                        cellId: cell.id,
                        text: cell.textContent.trim()
                    });
                }
            });
            
            return questions;
        }
        
        // Auto-extract score when student answers change
        function setupAutoExtraction() {
            const inputs = document.querySelectorAll('input[id^="i"]');
            inputs.forEach(input => {
                input.addEventListener('change', function() {
                    // Debounce score extraction
                    clearTimeout(window.scoreExtractionTimeout);
                    window.scoreExtractionTimeout = setTimeout(function() {
                        const scoreData = extractCompleteScore();
                        
                        // Send progress update to parent
                        if (window.parent !== window) {
                            window.parent.postMessage({
                                type: 'progressUpdate',
                                data: scoreData
                            }, '*');
                        }
                    }, 500);
                });
            });
        }
        
        setupAutoExtraction();
        
        // Expose extraction functions globally
        window.worksheetExtractor = {
            extractScore: extractCompleteScore,
            getAnswers: extractAnswers,
            getStudentId: getStudentId
        };
        
        console.log('Score extractor ready');
    });
})();
"""
    
    return js_code

if __name__ == "__main__":
    print("🔍 WebWorksheet Embedding Analysis")
    print("=" * 50)
    
    analysis = analyze_worksheet_embedding()
    
    print("\n📋 EMBEDDING FEASIBILITY")
    print(f"Can Embed: {analysis['embedding_feasibility']['can_embed']}")
    print(f"Score Extraction: {analysis['embedding_feasibility']['score_extraction']}")
    
    print(f"\n⚠️ CHALLENGES ({len(analysis['embedding_feasibility']['challenges'])} issues)")
    for i, challenge in enumerate(analysis['embedding_feasibility']['challenges'], 1):
        print(f"  {i}. {challenge}")
    
    print(f"\n✅ SOLUTIONS ({len(analysis['embedding_feasibility']['solutions'])} approaches)")
    for i, solution in enumerate(analysis['embedding_feasibility']['solutions'], 1):
        print(f"  {i}. {solution}")
    
    print(f"\n🎯 SCORE EXTRACTION METHODS")
    for method in analysis['technical_details']['score_extraction']['extraction_methods']:
        print(f"  • {method}")
    
    print(f"\n💡 RECOMMENDATIONS")
    for i, rec in enumerate(analysis['recommendations'], 1):
        print(f"  {i}. {rec}")
    
    print(f"\n🚀 CONCLUSION")
    print("✅ YES - WebWorksheet HTML files CAN be embedded")
    print("✅ YES - Scores CAN be extracted with proper setup")
    print("⚠️ REQUIRES - JavaScript modifications and iframe communication")
    print("🎯 BEST APPROACH - Iframe embedding + postMessage API")
    
    # Save analysis to file
    with open('/Users/oscarfimbres/git/archimedes-backend/embedding_analysis.json', 'w') as f:
        json.dump(analysis, f, indent=2)
    
    print(f"\n📁 Analysis saved to embedding_analysis.json")