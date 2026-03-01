"""
Worksheet Service - Business logic for worksheet operations
"""
import logging
import re
from typing import Optional, Dict, Any, List, Tuple
from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
import random
import uuid

from ..models.worksheet_session import WorksheetSession
from ..schemas.worksheet import (
    SessionCreateRequest, SessionCreateResponse, SubmitAnswersRequest,
    SessionResult, SessionResultWithDetails, WorksheetInfo, TopicSummary
)
from .data_loader import data_loader
from .file_service import file_service

logger = logging.getLogger(__name__)


class WorksheetService:
    """
    Core business logic for worksheet operations
    Handles session management, scoring, and data processing
    """

    def __init__(self):
        self.data_loader = data_loader
        self.file_service = file_service

    async def get_topics_summary(self) -> List[TopicSummary]:
        """Get all available topics with worksheet information"""
        try:
            topic_summaries = self.data_loader.get_topic_summary()

            result = []
            for summary in topic_summaries:
                worksheets = []
                for ws_data in summary['worksheets']:
                    worksheet = WorksheetInfo(
                        id=ws_data['id'],
                        title=ws_data['title'],
                        topic=ws_data['topic'],
                        subtopic=ws_data['subtopic'],
                        description=ws_data.get('description', '')
                    )
                    worksheets.append(worksheet)

                topic = TopicSummary(
                    category=summary['category'],
                    name=summary['name'],
                    worksheet_count=summary['worksheet_count'],
                    worksheets=worksheets
                )
                result.append(topic)

            return result

        except Exception as e:
            logger.error(f"Error getting topics summary: {e}")
            return []

    async def get_worksheets_by_topic(self, topic_code: str) -> List[WorksheetInfo]:
        """Get all worksheets for a specific topic"""
        try:
            ws_data_list = self.data_loader.get_worksheets_by_topic(topic_code)

            worksheets = []
            for ws_data in ws_data_list:
                worksheet = WorksheetInfo(
                    id=ws_data['id'],
                    title=ws_data['title'],
                    topic=ws_data['topic'],
                    subtopic=ws_data['subtopic'],
                    description=ws_data.get('description', '')
                )
                worksheets.append(worksheet)

            return worksheets

        except Exception as e:
            logger.error(
                f"Error getting worksheets for topic {topic_code}: {e}")
            return []

    async def get_worksheet_with_grade(
        self, worksheet_id: str, student_id: str, session_id: str, db: AsyncSession
    ) -> Optional[str]:
        """
        Get completed worksheet content from stored HTML snapshot
        """
        try:
            # Get the session to check if it's completed and get stored content
            result = await db.execute(
                select(WorksheetSession).where(
                    WorksheetSession.id == session_id,
                    WorksheetSession.student_id == student_id
                )
            )
            session = result.scalar_one_or_none()

            if not session or not session.is_completed:
                logger.warning(
                    f"Session {session_id} not found or not completed")
                return None

            # Use stored HTML snapshot if available (preferred method)
            if session.completed_html_content:
                logger.info(f"Returning stored HTML snapshot for session {session_id}")
                return session.completed_html_content

            # Fallback: reconstruct from original worksheet (legacy method)
            logger.warning(f"No HTML snapshot found for session {session_id}, using fallback reconstruction")
            return await self._reconstruct_completed_worksheet(worksheet_id, student_id, session)

        except Exception as e:
            logger.error(f"Error getting worksheet with grade: {e}")
            return None
    
    async def _reconstruct_completed_worksheet(
        self, worksheet_id: str, student_id: str, session: WorksheetSession
    ) -> Optional[str]:
        """
        Legacy method: Reconstruct completed worksheet by injecting grade into original
        """
        try:
            # Get worksheet content
            content = await self.get_worksheet_content(worksheet_id, student_id)
            if not content:
                return None

            # Inject grade into the data-grade-field
            grade_display = f"{session.score}%"

            # Add JavaScript to populate the grade field
            grade_script = f"""
            <script type="text/javascript">
            // Populate grade field when page loads
            function populateGradeField() {{
                try {{
                    var gradeField = document.querySelector('[data-grade-field]');
                    if (gradeField) {{
                        gradeField.value = '{grade_display}';
                        gradeField.style.backgroundColor = '#90ee90';
                        gradeField.style.fontWeight = 'bold';
                        gradeField.readOnly = true;
                        console.log('Set grade field:', gradeField.id, '{grade_display}');
                    }}
                    
                    // Also try common grade field IDs
                    var gradeFieldById = document.getElementById('iP60');
                    if (gradeFieldById) {{
                        gradeFieldById.value = '{grade_display}';
                        gradeFieldById.style.backgroundColor = '#90ee90';
                        gradeFieldById.style.fontWeight = 'bold';
                        gradeFieldById.readOnly = true;
                        console.log('Set grade by ID: iP60');
                    }}
                    
                    // Disable all input fields to show as completed
                    var inputs = document.querySelectorAll('input[type="text"]');
                    inputs.forEach(function(input) {{
                        if (!input.hasAttribute('data-name-field') && 
                            !input.hasAttribute('data-id-field') && 
                            !input.hasAttribute('data-grade-field')) {{
                            input.readOnly = true;
                            input.style.backgroundColor = '#f0f0f0';
                        }}
                    }});
                    
                }} catch(e) {{
                    console.warn('Error populating grade field:', e);
                }}
            }}
            
            // Run when page loads
            if (document.readyState === 'loading') {{
                document.addEventListener('DOMContentLoaded', populateGradeField);
            }} else {{
                populateGradeField();
            }}
            
            // Also run after a delay to ensure WebWorksheet is loaded
            setTimeout(populateGradeField, 1500);
            </script>
            """

            # Insert script before closing </head> tag
            head_end = content.rfind('</head>')
            if head_end != -1:
                content = content[:head_end] + \
                    grade_script + content[head_end:]

            # Add completion banner
            completion_banner = f"""
            <div style="background: #4caf50; color: white; border-radius: 5px; padding: 15px; margin: 10px 0; text-align: center; font-family: Arial;">
                <h3 style="margin: 0;">✅ Worksheet Completed!</h3>
                <p style="margin: 5px 0;">Final Score: <strong>{grade_display}</strong></p>
                <p style="margin: 5px 0; font-size: 12px;">Completed: {session.completed_at.strftime('%Y-%m-%d %H:%M UTC') if session.completed_at else 'N/A'}</p>
            </div>
            """

            # Insert banner after the personalization header
            if 'Worksheet for Student' in content:
                content = content.replace(
                    '</div>', f'</div>{completion_banner}', 1)

            logger.debug(
                f"Generated worksheet with grade for session {session_id}: {grade_display}")
            return content

        except Exception as e:
            logger.error(f"Error getting worksheet with grade: {e}")
            return None

    async def get_worksheet_content(self, worksheet_id: str, student_id: Optional[str] = None) -> Optional[str]:
        """
        Get worksheet HTML content, potentially personalized for student
        """
        try:
            # Get base content
            content = await self.file_service.get_worksheet_content(worksheet_id)

            if not content:
                logger.warning(f"Worksheet content not found: {worksheet_id}")
                return None

            # Personalize content if student ID provided
            if student_id:
                content = await self._personalize_worksheet_content(
                    content, worksheet_id, student_id
                )

            return content

        except Exception as e:
            logger.error(
                f"Error getting worksheet content {worksheet_id}: {e}")
            return None

    async def _personalize_worksheet_content(
        self, content: str, worksheet_id: str, student_id: str
    ) -> str:
        """
        Personalize worksheet content based on student ID
        Populates WebWorksheet data attributes for proper student data integration
        """
        try:
            # Create deterministic seed from student_id and worksheet_id
            seed = hash(f"{student_id}_{worksheet_id}") % (2**31)
            random.seed(seed)

            # Generate student name from ID (simple mapping for demo)
            student_name = f"Student {student_id}"

            # Method 1: Find data-name-field and data-id-field containers and update their inputs
            # For data-name-field (student name)
            name_field_pattern = r'(<td[^>]*data-name-field[^>]*>.*?<input[^>]*?)(\s+data-default=\'[^\']*\')?([^>]*>)'
            content = re.sub(
                name_field_pattern, rf'\1 value="{student_name}"\3', content, flags=re.DOTALL)

            # For data-id-field (student ID) - override the data-default value
            id_field_pattern = r'(<td[^>]*data-id-field[^>]*>.*?<input[^>]*?)(\s+data-default=\'[^\']*\')?([^>]*>)'
            content = re.sub(
                id_field_pattern, rf'\1 value="{student_id}" data-default="{student_id}"\3', content, flags=re.DOTALL)

            # Add a subtle personalization notice
            personalization_header = f"""
            <div style="background: #e3f2fd; border-left: 4px solid #2196f3; padding: 8px; margin: 10px 0; font-family: Arial; font-size: 12px;">
                <strong>📚 Worksheet for Student {student_id}</strong> | Worksheet: {worksheet_id}
            </div>
            """

            # Add postMessage communication script
            communication_script = f"""
            <script type="text/javascript">
            console.log('🚀 Communication script loaded for student {student_id}');
            
            // Communication with parent window
            function notifyParent(type, data) {{
                try {{
                    window.parent.postMessage({{ type: type, ...data }}, '*');
                    console.log('Sent message to parent:', type, data);
                }} catch(e) {{
                    console.warn('Could not send message to parent:', e);
                }}
            }}
            
            // Monitor data-grade-field for changes
            function setupGradeMonitoring() {{
                const gradeField = document.querySelector('[data-grade-field]');
                if (gradeField) {{
                    console.log('Found grade field:', gradeField.id, gradeField.tagName);
                    
                    // Monitor for content changes (since it's a td element, not input)
                    const observer = new MutationObserver(function(mutations) {{
                        mutations.forEach(function(mutation) {{
                            if (mutation.type === 'childList' || mutation.type === 'characterData') {{
                                const grade = gradeField.textContent || gradeField.innerText;
                                if (grade && grade.trim().length > 0) {{
                                    console.log('Grade detected via mutation:', grade);
                                    notifyParent('grade-updated', {{ grade: grade.trim() }});
                                }}
                            }}
                        }});
                    }});
                    
                    observer.observe(gradeField, {{ 
                        childList: true, 
                        subtree: true, 
                        characterData: true 
                    }});
                    
                    // Also periodically check for grade changes
                    let lastGrade = '';
                    const gradeChecker = setInterval(function() {{
                        const currentGrade = gradeField.textContent || gradeField.innerText || '';
                        if (currentGrade.trim() !== lastGrade.trim() && currentGrade.trim().length > 0) {{
                            lastGrade = currentGrade.trim();
                            console.log('Grade detected via polling:', lastGrade);
                            notifyParent('grade-updated', {{ grade: lastGrade }});
                        }}
                    }}, 1000);
                    
                    // Stop polling after 30 seconds to avoid memory leak
                    setTimeout(function() {{
                        clearInterval(gradeChecker);
                        console.log('Stopped grade polling');
                    }}, 30000);
                }}
                
                // Look for submit buttons and forms - comprehensive selector
                const submitButtons = document.querySelectorAll('input[type="submit"], button[type="submit"], input[type="button"][value*="submit" i], input[value*="submit" i], button[onclick*="submit" i], input[onclick*="submit" i], button[onClick*="submit"], input[onClick*="submit"]');
                console.log('Found', submitButtons.length, 'submit buttons');
                submitButtons.forEach(function(button) {{
                    console.log('Found submit button:', button.id, button.type, button.value, button.getAttribute('onClick') || button.getAttribute('onclick'));
                    button.addEventListener('click', function(e) {{
                        console.log('Submit button clicked:', this.id, 'notifying parent...');
                        setTimeout(function() {{
                            // Collect answers from inputs - improved collection
                            const answers = {{}};
                            const allInputs = document.querySelectorAll('input[type="text"], input[type="number"], textarea');
                            console.log('Found', allInputs.length, 'input fields total');
                            
                            allInputs.forEach(function(input, index) {{
                                // Skip student data fields
                                const isStudentField = input.parentElement?.hasAttribute('data-name-field') || 
                                                     input.parentElement?.hasAttribute('data-id-field') ||
                                                     input.parentElement?.hasAttribute('data-grade-field');
                                
                                if (!isStudentField && input.value && input.value.trim().length > 0) {{
                                    const fieldId = input.id || input.name || 'field_' + (index + 1);
                                    answers[fieldId] = input.value.trim();
                                    console.log('Collected answer:', fieldId, '=', input.value.trim());
                                }}
                            }});
                            
                            // Check if grade field has a value
                            const gradeField = document.querySelector('[data-grade-field]');
                            const grade = gradeField ? (gradeField.textContent || gradeField.innerText || '').trim() : null;
                            
                            // Capture completed worksheet using WebWorksheet's static form
                            function captureCompletedWorksheet() {{
                                try {{
                                    // Use WebWorksheet's built-in static form function
                                    if (typeof window.createStaticForm === 'function') {{
                                        console.log('Creating static form using WebWorksheet function');
                                        const htmlContent = window.createStaticForm(true); // Get static readonly form HTML directly
                                        
                                        if (htmlContent && htmlContent.length > 0) {{
                                            if (htmlContent.length < 1000000) {{ // 1MB limit
                                                console.log('Captured static form HTML:', htmlContent.length, 'bytes');
                                                return htmlContent;
                                            }} else {{
                                                console.warn('Static form HTML too large:', htmlContent.length, 'bytes');
                                            }}
                                        }} else {{
                                            console.warn('createStaticForm returned empty content');
                                        }}
                                    }} else {{
                                        console.log('createStaticForm not available, using regular HTML capture');
                                        const htmlContent = document.documentElement.outerHTML;
                                        if (htmlContent.length < 1000000) {{
                                            return htmlContent;
                                        }}
                                    }}
                                }} catch(e) {{
                                    console.warn('Error creating static form:', e);
                                }}
                                return null;
                            }}
                            
                            const completedHtml = captureCompletedWorksheet();
                            
                            console.log('Sending worksheet data:', {{ 
                                answerCount: Object.keys(answers).length, 
                                answers: answers, 
                                grade: grade, 
                                htmlSize: completedHtml ? completedHtml.length : 0 
                            }});
                            notifyParent('worksheet-submitted', {{ 
                                answers: answers, 
                                grade: grade,
                                completedHtml: completedHtml
                            }});
                        }}, 500); // Increased delay to allow for form processing
                    }});
                }});
                
                // Also try to intercept the submitForm function if it exists
                if (typeof window.submitForm === 'function') {{
                    const originalSubmitForm = window.submitForm;
                    window.submitForm = function(buttonId) {{
                        console.log('submitForm called with button:', buttonId);
                        const result = originalSubmitForm.call(this, buttonId);
                        
                        // After submit processing, collect data
                        setTimeout(function() {{
                            const answers = {{}};
                            const inputs = document.querySelectorAll('input[type="text"]');
                            inputs.forEach(function(input, index) {{
                                if (input.value && input.value.trim().length > 0) {{
                                    const isStudentField = input.parentElement?.hasAttribute('data-name-field') || 
                                                         input.parentElement?.hasAttribute('data-id-field');
                                    if (!isStudentField) {{
                                        answers[input.id || 'q' + (index + 1)] = input.value.trim();
                                    }}
                                }}
                            }});
                            
                            const gradeField = document.querySelector('[data-grade-field]');
                            const grade = gradeField ? (gradeField.textContent || gradeField.innerText || '').trim() : null;
                            
                            // Capture complete HTML content  
                            const completedHtml = document.documentElement.outerHTML;
                            
                            console.log('submitForm processed, sending data with HTML:', {{ answers: answers, grade: grade, htmlSize: completedHtml.length }});
                            notifyParent('worksheet-submitted', {{ 
                                answers: answers, 
                                grade: grade,
                                completedHtml: completedHtml
                            }});
                        }}, 1000); // Longer delay for custom submit processing
                        
                        return result;
                    }};
                    console.log('Intercepted submitForm function');
                }} else {{
                    console.log('submitForm function not found, trying later...');
                    // Try to find submitForm function after a delay
                    setTimeout(function() {{
                        if (typeof window.submitForm === 'function') {{
                            const originalSubmitForm = window.submitForm;
                            window.submitForm = function(buttonId) {{
                                console.log('submitForm called (delayed hook) with button:', buttonId);
                                const result = originalSubmitForm.call(this, buttonId);
                                
                                setTimeout(function() {{
                                    const answers = {{}};
                                    const inputs = document.querySelectorAll('input[type="text"]');
                                    inputs.forEach(function(input, index) {{
                                        if (input.value && input.value.trim().length > 0) {{
                                            const isStudentField = input.parentElement?.hasAttribute('data-name-field') || 
                                                                 input.parentElement?.hasAttribute('data-id-field');
                                            if (!isStudentField) {{
                                                answers[input.id || 'q' + (index + 1)] = input.value.trim();
                                            }}
                                        }}
                                    }});
                                    
                                    const gradeField = document.querySelector('[data-grade-field]');
                                    const grade = gradeField ? (gradeField.textContent || gradeField.innerText || '').trim() : null;
                                    
                                    // Capture complete HTML content
                                    const completedHtml = document.documentElement.outerHTML;
                                    
                                    console.log('submitForm processed (delayed), sending data with HTML:', {{ answers: answers, grade: grade, htmlSize: completedHtml.length }});
                                    notifyParent('worksheet-submitted', {{ 
                                        answers: answers, 
                                        grade: grade,
                                        completedHtml: completedHtml
                                    }});
                                }}, 1000);
                                
                                return result;
                            }};
                            console.log('Intercepted submitForm function (delayed hook)');
                        }} else {{
                            console.warn('submitForm function still not found after delay');
                        }}
                    }}, 2000);
                }};
                
                console.log('Grade and submission monitoring setup complete');
            }}
            
            // Setup monitoring when page is ready
            if (document.readyState === 'loading') {{
                document.addEventListener('DOMContentLoaded', setupGradeMonitoring);
            }} else {{
                setupGradeMonitoring();
            }}
            
            // Additional setup after a delay (for WebWorksheet.js)
            setTimeout(setupGradeMonitoring, 1000);
            </script>
            """

            # Insert the header and communication script after the opening body tag
            script_injected = False
            if '<body>' in content:
                content = content.replace(
                    '<body>', f'<body>{personalization_header}{communication_script}')
                script_injected = True
                logger.debug(
                    f"Injected script after <body> tag for {worksheet_id}")
            elif '<BODY>' in content:
                content = content.replace(
                    '<BODY>', f'<BODY>{personalization_header}{communication_script}')
                script_injected = True
                logger.debug(
                    f"Injected script after <BODY> tag for {worksheet_id}")
            else:
                # Fallback: inject after <html> tag or at the beginning
                if '<html>' in content:
                    content = content.replace(
                        '<html>', f'<html>{personalization_header}{communication_script}')
                    script_injected = True
                    logger.debug(
                        f"Injected script after <html> tag for {worksheet_id}")
                elif '<HTML>' in content:
                    content = content.replace(
                        '<HTML>', f'<HTML>{personalization_header}{communication_script}')
                    script_injected = True
                    logger.debug(
                        f"Injected script after <HTML> tag for {worksheet_id}")
                else:
                    # Last resort: prepend to content
                    content = f'{personalization_header}{communication_script}{content}'
                    script_injected = True
                    logger.debug(
                        f"Prepended script to content for {worksheet_id}")

            if not script_injected:
                logger.warning(
                    f"Could not inject communication script for {worksheet_id}")

            # Find and replace randomizable elements for variety
            def replace_random(match):
                try:
                    range_str = match.group(1)
                    if '-' in range_str:
                        min_val, max_val = map(int, range_str.split('-'))
                        return str(random.randint(min_val, max_val))
                    else:
                        return str(random.randint(1, int(range_str)))
                except:
                    return match.group(0)  # Return original if error

            # Replace random number patterns (if any exist)
            content = re.sub(r'{{random:(\d+-\d+|\d+)}}',
                             replace_random, content)

            logger.debug(
                f"Personalized worksheet {worksheet_id} for student {student_id}")
            return content

        except Exception as e:
            logger.error(f"Error personalizing worksheet content: {e}")
            return content  # Return unpersonalized content as fallback

    async def create_session(
        self, request: SessionCreateRequest, db: AsyncSession
    ) -> Optional[SessionCreateResponse]:
        """Create a new worksheet session"""
        try:
            # Validate worksheet exists
            worksheet_info = self.data_loader.get_worksheet_info(
                request.worksheet_id)
            if not worksheet_info:
                logger.warning(f"Worksheet not found: {request.worksheet_id}")
                return None

            # Check if content is available
            content_exists = await self.file_service.check_worksheet_exists(request.worksheet_id)
            if not content_exists:
                logger.warning(
                    f"Worksheet content not available: {request.worksheet_id}")
                return None

            # Create new session
            session = WorksheetSession(
                student_id=request.student_id,
                worksheet_id=request.worksheet_id.upper(),
                started_at=datetime.utcnow()
            )

            db.add(session)
            await db.commit()
            await db.refresh(session)

            response = SessionCreateResponse(
                session_id=session.id,
                student_id=session.student_id,
                worksheet_id=session.worksheet_id,
                started_at=session.started_at,
                message=f"Session created for worksheet {session.worksheet_id}"
            )

            logger.info(
                f"Created session {session.id} for student {request.student_id}")
            return response

        except Exception as e:
            logger.error(f"Error creating session: {e}")
            await db.rollback()
            return None

    async def submit_answers(
        self, session_id: str, request: SubmitAnswersRequest, db: AsyncSession
    ) -> Optional[SessionResult]:
        """Submit answers and calculate score"""
        try:
            # Get session
            result = await db.execute(
                select(WorksheetSession).where(
                    WorksheetSession.id == session_id)
            )
            session = result.scalar_one_or_none()

            if not session:
                logger.warning(f"Session not found: {session_id}")
                return None

            if session.is_completed:
                logger.warning(f"Session already completed: {session_id}")
                return None

            # Store answers and completed HTML snapshot
            session.student_answers = request.answers
            
            # Store complete HTML content if provided
            if hasattr(request, 'completed_html') and request.completed_html:
                session.completed_html_content = request.completed_html
                logger.info(f"Stored completed HTML content ({len(request.completed_html)} chars) for session {session.id}")

            # Check if grade was provided directly from worksheet
            if hasattr(request, 'grade') and request.grade:
                # Parse grade from worksheet (e.g., "85%" or "17/20" or "85")
                grade_str = str(request.grade).strip()
                logger.info(f"Received grade from worksheet: {grade_str}")

                # Parse different grade formats
                if '%' in grade_str:
                    # Format: "85%"
                    score = int(float(grade_str.replace('%', '')))
                elif '/' in grade_str:
                    # Format: "17/20"
                    parts = grade_str.split('/')
                    if len(parts) == 2:
                        correct = float(parts[0])
                        total = float(parts[1])
                        score = int((correct / total) *
                                    100) if total > 0 else 0
                    else:
                        score = 0
                else:
                    # Assume it's already a percentage number
                    try:
                        score = int(float(grade_str))
                    except:
                        score = 0

                # Calculate correct answers from score and total questions
                total_questions = len(
                    request.answers) if request.answers else 1
                correct_answers = int(
                    (score * total_questions) / 100) if score > 0 else 0

                session.score = score
                session.correct_answers = correct_answers
                session.total_questions = total_questions
                session.correct_answers_data = {
                    "source": f"WebWorksheet grade: {grade_str}"}

                logger.info(
                    f"Using WebWorksheet grade: {score}% ({correct_answers}/{total_questions})")
            else:
                # Fallback to manual calculation if no grade provided
                score_result = await self._calculate_score(
                    session.worksheet_id, request.answers
                )

                if score_result:
                    correct_count, total_count, correct_answers_data = score_result
                    session.calculate_score(correct_count, total_count)
                    session.correct_answers_data = correct_answers_data

            # Set time taken if provided
            if request.time_taken_seconds:
                session.time_taken_seconds = request.time_taken_seconds

            # Complete session
            session.complete_session()

            await db.commit()
            await db.refresh(session)

            # Return result
            result = SessionResult(
                session_id=session.id,
                student_id=session.student_id,
                worksheet_id=session.worksheet_id,
                score=session.score,
                correct_answers=session.correct_answers,
                total_questions=session.total_questions,
                time_taken_seconds=session.time_taken_seconds,
                started_at=session.started_at,
                completed_at=session.completed_at,
                is_completed=session.is_completed
            )

            logger.info(
                f"Submitted answers for session {session_id}, score: {session.score}%")
            return result

        except Exception as e:
            logger.error(f"Error submitting answers: {e}")
            await db.rollback()
            return None

    async def _calculate_score(
        self, worksheet_id: str, student_answers: Dict[str, Any]
    ) -> Optional[Tuple[int, int, Dict[str, Any]]]:
        """
        Calculate score by analyzing worksheet content and student answers
        Returns: (correct_count, total_count, correct_answers_data)
        """

    async def _calculate_score(
        self, worksheet_id: str, student_answers: Dict[str, Any]
    ) -> Optional[Tuple[int, int, Dict[str, Any]]]:
        """
        Calculate score by analyzing worksheet content and student answers
        Returns: (correct_count, total_count, correct_answers_data)
        """
        try:
            # Demo scoring system - more realistic scoring that gives credit for reasonable answers
            logger.debug(
                f"Calculating score for {worksheet_id} with answers: {student_answers}")

            if not student_answers:
                return 0, 1, {}

            total_questions = len(student_answers)
            correct_count = 0
            correct_answers_data = {}

            # More realistic scoring logic
            for question_id, answer in student_answers.items():
                answer_str = str(answer).strip()

                # Skip empty answers
                if not answer_str:
                    correct_answers_data[question_id] = "No answer provided"
                    continue

                # Generate a "correct" answer based on question ID and worksheet
                seed = hash(f"{worksheet_id}_{question_id}") % 100

                # Give credit for any reasonable answer (this is a demo after all)
                is_correct = False

                if worksheet_id.startswith('AL'):  # Algebra
                    # For algebra, accept any number between 1-50
                    try:
                        student_num = float(answer_str)
                        if 1 <= student_num <= 50:
                            is_correct = True
                            correct_answers_data[question_id] = f"✓ {answer_str} (algebra answer)"
                        else:
                            correct_answers_data[question_id] = f"Expected 1-50, got {answer_str}"
                    except:
                        # Non-numeric algebra answer
                        if len(answer_str) >= 1:
                            is_correct = True  # Give credit for effort
                            correct_answers_data[question_id] = f"✓ {answer_str} (text answer)"

                elif worksheet_id.startswith('AR'):  # Arithmetic
                    # For arithmetic, accept any reasonable number
                    try:
                        student_num = float(answer_str)
                        if -1000 <= student_num <= 1000:
                            is_correct = True
                            correct_answers_data[question_id] = f"✓ {answer_str} (arithmetic)"
                        else:
                            correct_answers_data[question_id] = f"Out of range: {answer_str}"
                    except:
                        # Give partial credit for any text answer
                        if len(answer_str) >= 1:
                            is_correct = True
                            correct_answers_data[question_id] = f"✓ {answer_str} (text answer)"

                elif worksheet_id.startswith('FR'):  # Fractions
                    # Accept fraction-like answers
                    if '/' in answer_str or any(char.isdigit() for char in answer_str):
                        is_correct = True
                        correct_answers_data[question_id] = f"✓ {answer_str} (fraction)"
                    else:
                        correct_answers_data[question_id] = f"Expected fraction, got: {answer_str}"

                else:  # Generic/Other worksheets (like EX02)
                    # Be very generous - any non-empty answer gets credit
                    if len(answer_str.strip()) >= 1:
                        is_correct = True
                        correct_answers_data[question_id] = f"✓ {answer_str} (good effort)"
                    else:
                        correct_answers_data[question_id] = "Empty answer"

                if is_correct:
                    correct_count += 1

            # Ensure at least some credit for effort (minimum 30% if they tried)
            if correct_count == 0 and total_questions > 0:
                non_empty_answers = sum(
                    1 for ans in student_answers.values() if str(ans).strip())
                if non_empty_answers > 0:
                    # 30% minimum for effort
                    correct_count = max(1, int(total_questions * 0.3))
                    logger.debug(
                        f"Giving minimum credit for effort: {correct_count}/{total_questions}")

            logger.info(
                f"Realistic score for {worksheet_id}: {correct_count}/{total_questions} (answers: {list(student_answers.keys())})")
            return correct_count, total_questions, correct_answers_data

        except Exception as e:
            logger.error(f"Error calculating score: {e}")
            # Fallback to generous scoring
            total_questions = len(student_answers) if student_answers else 1
            correct_count = max(1, int(total_questions * 0.7))  # 70% fallback
            correct_answers_data = {
                f"q{i+1}": "demo_answer" for i in range(total_questions)}
            return correct_count, total_questions, correct_answers_data

    async def get_session_result(
        self, session_id: str, db: AsyncSession, include_details: bool = False
    ) -> Optional[SessionResult]:
        """Get session results"""
        try:
            result = await db.execute(
                select(WorksheetSession).where(
                    WorksheetSession.id == session_id)
            )
            session = result.scalar_one_or_none()

            if not session:
                return None

            if include_details:
                return SessionResultWithDetails(
                    session_id=session.id,
                    student_id=session.student_id,
                    worksheet_id=session.worksheet_id,
                    score=session.score,
                    correct_answers=session.correct_answers,
                    total_questions=session.total_questions,
                    time_taken_seconds=session.time_taken_seconds,
                    started_at=session.started_at,
                    completed_at=session.completed_at,
                    is_completed=session.is_completed,
                    student_answers=session.student_answers,
                    correct_answers_data=session.correct_answers_data
                )
            else:
                return SessionResult(
                    session_id=session.id,
                    student_id=session.student_id,
                    worksheet_id=session.worksheet_id,
                    score=session.score,
                    correct_answers=session.correct_answers,
                    total_questions=session.total_questions,
                    time_taken_seconds=session.time_taken_seconds,
                    started_at=session.started_at,
                    completed_at=session.completed_at,
                    is_completed=session.is_completed
                )

        except Exception as e:
            logger.error(f"Error getting session result: {e}")
            return None

    async def get_student_progress(
        self, student_id: str, db: AsyncSession
    ) -> Optional[Dict[str, Any]]:
        """Get student's overall progress"""
        try:
            # Get session statistics
            total_result = await db.execute(
                select(func.count(WorksheetSession.id))
                .where(WorksheetSession.student_id == student_id)
            )
            total_sessions = total_result.scalar() or 0

            completed_result = await db.execute(
                select(func.count(WorksheetSession.id))
                .where(
                    WorksheetSession.student_id == student_id,
                    WorksheetSession.is_completed == True
                )
            )
            completed_sessions = completed_result.scalar() or 0

            # Get average score
            avg_score_result = await db.execute(
                select(func.avg(WorksheetSession.score))
                .where(
                    WorksheetSession.student_id == student_id,
                    WorksheetSession.is_completed == True,
                    WorksheetSession.score.isnot(None)
                )
            )
            avg_score = avg_score_result.scalar()

            # Get topics attempted
            topics_result = await db.execute(
                select(WorksheetSession.worksheet_id)
                .where(WorksheetSession.student_id == student_id)
                .distinct()
            )
            worksheet_ids = topics_result.scalars().all()
            topics_attempted = list(set(
                ws_id[:2] for ws_id in worksheet_ids if len(ws_id) >= 2
            ))

            # Get last session date
            last_session_result = await db.execute(
                select(func.max(WorksheetSession.started_at))
                .where(WorksheetSession.student_id == student_id)
            )
            last_session_date = last_session_result.scalar()

            return {
                "student_id": student_id,
                "total_sessions": total_sessions,
                "completed_sessions": completed_sessions,
                "average_score": round(avg_score, 2) if avg_score else None,
                "topics_attempted": sorted(topics_attempted),
                "last_session_date": last_session_date
            }

        except Exception as e:
            logger.error(f"Error getting student progress: {e}")
            return None


# Global instance
worksheet_service = WorksheetService()
