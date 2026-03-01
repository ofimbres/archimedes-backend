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