#!/usr/bin/env python3
"""
Phase 1: Enhanced Universal Generator

Improvements:
1. Extract real lookup tables from HTML worksheets  
2. Better expression parsing with error handling
3. Handle edge cases and malformed data
4. Comprehensive validation and fallbacks
"""

import re
import json
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple
from dataclasses import dataclass
import logging
from datetime import datetime
import ast
import operator


# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@dataclass
class ParsedWorksheetData:
    """Enhanced worksheet data with validation"""
    worksheet_id: str
    subject: str
    title: str
    lookup_table: Dict[int, Dict[str, Any]]
    question_count: int
    extraction_quality: str  # 'high', 'medium', 'low'
    errors: List[str]


class EnhancedTableExtractor:
    """Improved table extraction from HTML worksheets"""
    
    def __init__(self):
        self.expression_patterns = [
            r'[Xx][\+\-\*/\(\)0-9\s]+',  # Contains X with math operations
            r'[\+\-\*/\(\)0-9\s]*[Xx][\+\-\*/\(\)0-9\s]*',  # X anywhere in math
            r'\([^)]*[Xx][^)]*\)',  # X in parentheses
            r'[0-9]+[\*\/][Xx]',  # Number times/divided by X
            r'[Xx][\*\/][0-9]+'   # X times/divided by number
        ]
        
        self.description_keywords = [
            'number', 'times', 'sum', 'difference', 'product', 'quotient',
            'more than', 'less than', 'increased by', 'decreased by',
            'diminished', 'third', 'half', 'twice', 'thrice'
        ]
    
    def extract_from_worksheet(self, html_file: Path) -> ParsedWorksheetData:
        """Extract enhanced data from single worksheet"""
        errors = []
        
        try:
            with open(html_file, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
        except Exception as e:
            errors.append(f"File read error: {e}")
            return self._create_empty_data(html_file.stem, errors)
        
        # Extract basic info
        worksheet_id = html_file.stem
        subject_code = worksheet_id[:2] if len(worksheet_id) >= 2 else "UN"
        subject = self._get_subject_name(subject_code)
        
        # Extract table data using multiple methods
        lookup_table = {}
        methods_tried = []
        
        # Method 1: Parse HTML table structure
        try:
            table1 = self._extract_html_table_method(content)
            if table1:
                lookup_table.update(table1)
                methods_tried.append("html_table")
        except Exception as e:
            errors.append(f"HTML table method failed: {e}")
        
        # Method 2: Parse cell data patterns  
        try:
            table2 = self._extract_cell_pattern_method(content)
            if table2:
                lookup_table.update(table2)
                methods_tried.append("cell_patterns")
        except Exception as e:
            errors.append(f"Cell pattern method failed: {e}")
        
        # Method 3: Parse formula references
        try:
            table3 = self._extract_formula_reference_method(content)
            if table3:
                lookup_table.update(table3)
                methods_tried.append("formula_refs")
        except Exception as e:
            errors.append(f"Formula reference method failed: {e}")
        
        # Determine extraction quality
        quality = self._assess_extraction_quality(lookup_table, methods_tried, errors)
        
        return ParsedWorksheetData(
            worksheet_id=worksheet_id,
            subject=subject,
            title=f"{subject} - {worksheet_id}",
            lookup_table=lookup_table,
            question_count=min(len(lookup_table), 10),
            extraction_quality=quality,
            errors=errors
        )
    
    def _extract_html_table_method(self, content: str) -> Dict[int, Dict[str, Any]]:
        """Method 1: Extract from HTML table structure"""
        lookup_table = {}
        
        # Find table rows with data
        row_pattern = r'<tr id=[\'"]ROW(\d+)[\'"][^>]*>.*?</tr>'
        rows = re.findall(row_pattern, content, re.DOTALL)
        
        for row_num_str in rows:
            try:
                row_num = int(row_num_str)
                # Extract cells from this row
                row_start = content.find(f'<tr id=\'ROW{row_num}\'')
                row_end = content.find('</tr>', row_start)
                if row_start == -1 or row_end == -1:
                    continue
                    
                row_content = content[row_start:row_end + 5]
                cells = self._extract_cells_from_row(row_content)
                
                # Look for expression patterns in cells
                expression = self._find_expression_in_cells(cells)
                description = self._find_description_in_cells(cells)
                
                if expression:
                    lookup_table[len(lookup_table) + 1] = {
                        'expression': expression,
                        'description': description or f"Expression: {expression}",
                        'x_value': self._generate_x_value(len(lookup_table) + 1),
                        'source_row': row_num
                    }
                    
            except Exception:
                continue
                
        return lookup_table
    
    def _extract_cell_pattern_method(self, content: str) -> Dict[int, Dict[str, Any]]:
        """Method 2: Extract using cell data patterns"""
        lookup_table = {}
        
        # Find all cell contents
        cell_pattern = r'<td[^>]*>([^<]+)</td>'
        all_cells = re.findall(cell_pattern, content)
        
        expressions = []
        descriptions = []
        
        for cell_content in all_cells:
            cell_content = cell_content.strip()
            if not cell_content:
                continue
                
            # Check if it's an expression
            if self._is_mathematical_expression(cell_content):
                expressions.append(cell_content)
            
            # Check if it's a description
            elif self._is_description(cell_content):
                descriptions.append(cell_content)
        
        # Pair expressions with descriptions
        for i, expr in enumerate(expressions[:10]):  # Limit to 10
            desc = descriptions[i] if i < len(descriptions) else f"Expression: {expr}"
            
            lookup_table[i + 1] = {
                'expression': expr,
                'description': desc,
                'x_value': self._generate_x_value(i + 1),
                'source_method': 'cell_patterns'
            }
            
        return lookup_table
    
    def _extract_formula_reference_method(self, content: str) -> Dict[int, Dict[str, Any]]:
        """Method 3: Extract from VLOOKUP formula references"""
        lookup_table = {}
        
        # Find VLOOKUP formulas that reference data ranges
        vlookup_pattern = r'VLOOKUP\([^,]+,"([A-Z]\d+:[A-Z]\d+)",(\d+),0\)'
        matches = re.findall(vlookup_pattern, content)
        
        for range_ref, col_num in matches:
            try:
                # Parse the range (e.g., "D2:I51")
                start_cell, end_cell = range_ref.split(':')
                start_col, start_row = self._parse_cell_ref(start_cell)
                end_col, end_row = self._parse_cell_ref(end_cell)
                
                # Extract data from this range
                range_data = self._extract_range_data(content, start_row, end_row)
                
                for idx, row_data in enumerate(range_data):
                    if idx >= 10:  # Limit to 10
                        break
                        
                    if 'expression' in row_data:
                        lookup_table[len(lookup_table) + 1] = {
                            'expression': row_data['expression'],
                            'description': row_data.get('description', ''),
                            'x_value': row_data.get('x_value', 1),
                            'source_method': 'vlookup_range'
                        }
                        
            except Exception:
                continue
                
        return lookup_table
    
    def _extract_cells_from_row(self, row_content: str) -> List[str]:
        """Extract all cell contents from a table row"""
        cell_pattern = r'<td[^>]*>([^<]*)</td>'
        return [cell.strip() for cell in re.findall(cell_pattern, row_content)]
    
    def _find_expression_in_cells(self, cells: List[str]) -> Optional[str]:
        """Find mathematical expression in cell list"""
        for cell in cells:
            if self._is_mathematical_expression(cell):
                return self._clean_expression(cell)
        return None
    
    def _find_description_in_cells(self, cells: List[str]) -> Optional[str]:
        """Find description text in cell list"""
        for cell in cells:
            if self._is_description(cell):
                return cell
        return None
    
    def _is_mathematical_expression(self, text: str) -> bool:
        """Check if text is a mathematical expression"""
        if not text or len(text) > 50:  # Too long to be an expression
            return False
            
        # Must contain X and mathematical operators
        has_x = 'X' in text.upper()
        has_math = any(op in text for op in ['+', '-', '*', '/', '(', ')'])
        
        # Check against expression patterns
        pattern_match = any(re.search(pattern, text, re.IGNORECASE) for pattern in self.expression_patterns)
        
        return has_x and (has_math or pattern_match)
    
    def _is_description(self, text: str) -> bool:
        """Check if text is a description"""
        if not text or len(text) < 5:
            return False
            
        # Contains description keywords
        text_lower = text.lower()
        keyword_match = any(keyword in text_lower for keyword in self.description_keywords)
        
        # Reasonable length and structure
        reasonable_length = 10 <= len(text) <= 100
        has_spaces = ' ' in text
        
        return keyword_match and reasonable_length and has_spaces
    
    def _clean_expression(self, expr: str) -> str:
        """Clean and standardize mathematical expression"""
        # Remove extra spaces
        expr = ' '.join(expr.split())
        
        # Standardize operators
        expr = expr.replace('×', '*').replace('÷', '/')
        expr = expr.replace(' * ', '*').replace(' / ', '/')
        expr = expr.replace(' + ', '+').replace(' - ', '-')
        
        # Handle common patterns
        expr = re.sub(r'(\d)\s*\*\s*([Xx])', r'\1*\2', expr)  # "3 * X" -> "3*X"
        expr = re.sub(r'([Xx])\s*\*\s*(\d)', r'\1*\2', expr)  # "X * 3" -> "X*3"
        expr = re.sub(r'(\d)\s*([Xx])', r'\1*\2', expr)        # "3 X" -> "3*X"
        
        return expr
    
    def _generate_x_value(self, index: int) -> float:
        """Generate varied X values for testing"""
        x_values = [1, 2, 3, 4, 5, 0, -1, 6, 8, 10, 12, 15, 18, 20, 22, 24, 0.5, 1.5, 2.5, 3.5]
        return x_values[(index - 1) % len(x_values)]
    
    def _parse_cell_ref(self, cell_ref: str) -> Tuple[str, int]:
        """Parse cell reference like 'D2' into column and row"""
        match = re.match(r'([A-Z]+)(\d+)', cell_ref)
        if match:
            return match.group(1), int(match.group(2))
        return 'A', 1
    
    def _extract_range_data(self, content: str, start_row: int, end_row: int) -> List[Dict[str, Any]]:
        """Extract data from a specific row range"""
        range_data = []
        
        for row_num in range(start_row, min(end_row + 1, start_row + 20)):  # Limit range
            row_pattern = f'<tr id=[\'"]ROW{row_num}[\'"][^>]*>.*?</tr>'
            row_match = re.search(row_pattern, content, re.DOTALL)
            
            if row_match:
                row_content = row_match.group(0)
                cells = self._extract_cells_from_row(row_content)
                
                expression = self._find_expression_in_cells(cells)
                description = self._find_description_in_cells(cells)
                
                if expression:
                    range_data.append({
                        'expression': expression,
                        'description': description,
                        'x_value': self._generate_x_value(len(range_data) + 1)
                    })
                    
        return range_data
    
    def _assess_extraction_quality(self, lookup_table: Dict, methods_tried: List[str], errors: List[str]) -> str:
        """Assess the quality of extraction"""
        if len(lookup_table) >= 8 and len(errors) == 0:
            return 'high'
        elif len(lookup_table) >= 5 and len(errors) <= 2:
            return 'medium'
        else:
            return 'low'
    
    def _get_subject_name(self, subject_code: str) -> str:
        """Get full subject name from code"""
        subjects = {
            'AL': 'Algebra', 'AN': 'Analysis', 'AR': 'Arithmetic', 'BS': 'Basic Skills',
            'CF': 'Fractions', 'CI': 'Circle Geometry', 'CM': 'Measurement', 'DE': 'Decimals',
            'EQ': 'Equations', 'EX': 'Expressions', 'FA': 'Functions & Algebra', 'FR': 'Fractions & Ratios',
            'GE': 'Geometry', 'IN': 'Inequalities', 'ME': 'Measurement', 'MN': 'Money',
            'OO': 'Operations', 'PE': 'Percentages', 'PP': 'Probability', 'PR': 'Proportions',
            'PT': 'Pythagorean Theorem', 'PY': 'Python/Programming', 'RO': 'Rotations',
            'SA': 'Statistics & Analysis', 'SE': 'Sequences', 'SF': 'Surface Area',
            'TS': 'Trigonometry', 'TT': 'Transformations', 'VO': 'Volume', 'WN': 'Whole Numbers'
        }
        return subjects.get(subject_code, subject_code)
    
    def _create_empty_data(self, worksheet_id: str, errors: List[str]) -> ParsedWorksheetData:
        """Create empty data structure for failed extractions"""
        return ParsedWorksheetData(
            worksheet_id=worksheet_id,
            subject="Unknown",
            title=f"Unknown - {worksheet_id}",
            lookup_table={},
            question_count=0,
            extraction_quality='failed',
            errors=errors
        )


class SafeExpressionEvaluator:
    """Enhanced expression evaluation with safety and error handling"""
    
    def __init__(self):
        # Safe operators for AST evaluation
        self.safe_operators = {
            ast.Add: operator.add,
            ast.Sub: operator.sub,
            ast.Mult: operator.mul,
            ast.Div: operator.truediv,
            ast.USub: operator.neg,
            ast.UAdd: operator.pos,
        }
    
    def evaluate_expression(self, expression: str, x_value: float) -> Tuple[float, Optional[str]]:
        """Safely evaluate expression, return (result, error)"""
        try:
            # Clean and prepare expression
            cleaned_expr = self._prepare_expression(expression, x_value)
            
            # Try AST evaluation first (safest)
            try:
                result = self._ast_evaluate(cleaned_expr)
                return round(result, 3), None
            except:
                pass
            
            # Fallback to eval with restrictions
            try:
                result = self._restricted_eval(cleaned_expr)
                return round(result, 3), None
            except Exception as e:
                return 0.0, f"Evaluation failed: {str(e)}"
                
        except Exception as e:
            return 0.0, f"Expression preparation failed: {str(e)}"
    
    def _prepare_expression(self, expression: str, x_value: float) -> str:
        """Clean and prepare expression for evaluation"""
        # Substitute X with value
        expr = expression.replace('X', str(x_value)).replace('x', str(x_value))
        
        # Clean common issues
        expr = expr.replace('×', '*').replace('÷', '/')
        expr = expr.replace(' ', '')  # Remove spaces
        
        # Handle implicit multiplication
        expr = re.sub(r'(\d)\(', r'\1*(', expr)  # "3(" -> "3*("
        expr = re.sub(r'\)(\d)', r')*\1', expr)  # ")3" -> ")*3"
        
        # Validate characters
        allowed_chars = set('0123456789+-*/().')
        if not all(c in allowed_chars for c in expr):
            raise ValueError(f"Invalid characters in expression: {expr}")
        
        return expr
    
    def _ast_evaluate(self, expr: str) -> float:
        """Evaluate using AST (safest method)"""
        try:
            node = ast.parse(expr, mode='eval')
            return self._ast_eval_node(node.body)
        except:
            raise ValueError("AST evaluation failed")
    
    def _ast_eval_node(self, node):
        """Recursively evaluate AST node"""
        if isinstance(node, ast.Constant):  # Python 3.8+
            return node.value
        elif isinstance(node, ast.Num):  # Python < 3.8
            return node.n
        elif isinstance(node, ast.BinOp):
            left = self._ast_eval_node(node.left)
            right = self._ast_eval_node(node.right)
            op = self.safe_operators.get(type(node.op))
            if op:
                return op(left, right)
            else:
                raise ValueError(f"Unsupported operation: {type(node.op)}")
        elif isinstance(node, ast.UnaryOp):
            operand = self._ast_eval_node(node.operand)
            op = self.safe_operators.get(type(node.op))
            if op:
                return op(operand)
            else:
                raise ValueError(f"Unsupported unary operation: {type(node.op)}")
        else:
            raise ValueError(f"Unsupported node type: {type(node)}")
    
    def _restricted_eval(self, expr: str) -> float:
        """Fallback evaluation with restrictions"""
        # Very restricted globals/locals
        safe_dict = {
            "__builtins__": {},
            "__name__": "__main__",
            "__doc__": None,
        }
        
        return eval(expr, safe_dict, {})


class EnhancedUniversalGenerator:
    """Enhanced universal generator with improved extraction and evaluation"""
    
    def __init__(self, worksheets_dir: str):
        self.worksheets_dir = Path(worksheets_dir)
        self.extractor = EnhancedTableExtractor()
        self.evaluator = SafeExpressionEvaluator()
        self.worksheet_cache = {}
        
        # Quality tracking
        self.extraction_stats = {
            'total_processed': 0,
            'high_quality': 0,
            'medium_quality': 0,
            'low_quality': 0,
            'failed': 0
        }
    
    def extract_worksheet_data(self, worksheet_id: str) -> ParsedWorksheetData:
        """Extract data with enhanced error handling"""
        if worksheet_id in self.worksheet_cache:
            return self.worksheet_cache[worksheet_id]
        
        html_file = self.worksheets_dir / f"{worksheet_id}.html"
        if not html_file.exists():
            raise FileNotFoundError(f"Worksheet {worksheet_id} not found")
        
        # Extract with enhanced methods
        data = self.extractor.extract_from_worksheet(html_file)
        
        # Update stats
        self.extraction_stats['total_processed'] += 1
        if data.extraction_quality == 'high':
            self.extraction_stats['high_quality'] += 1
        elif data.extraction_quality == 'medium':
            self.extraction_stats['medium_quality'] += 1
        elif data.extraction_quality == 'low':
            self.extraction_stats['low_quality'] += 1
        else:
            self.extraction_stats['failed'] += 1
        
        # Log quality
        logger.info(f"Extracted {worksheet_id}: {data.extraction_quality} quality, {len(data.lookup_table)} questions")
        
        if data.errors:
            logger.warning(f"Errors in {worksheet_id}: {data.errors}")
        
        self.worksheet_cache[worksheet_id] = data
        return data
    
    def generate_worksheet(self, student_id: str, worksheet_id: str) -> Dict[str, Any]:
        """Generate worksheet with enhanced error handling"""
        try:
            # Extract worksheet data
            ws_data = self.extract_worksheet_data(worksheet_id)
            
            if len(ws_data.lookup_table) == 0:
                logger.warning(f"No lookup table data for {worksheet_id}, using fallback")
                # Could implement fallback generation here
                return self._create_fallback_worksheet(student_id, worksheet_id, ws_data)
            
            # Generate questions
            quiz_number = self._generate_quiz_number(student_id)
            questions = self._generate_questions(quiz_number, ws_data)
            
            return {
                'worksheet_id': worksheet_id,
                'title': ws_data.title,
                'subject': ws_data.subject,
                'student_id': student_id,
                'quiz_number': quiz_number,
                'questions': questions,
                'max_score': 100,
                'extraction_quality': ws_data.extraction_quality,
                'question_count': len(questions)
            }
            
        except Exception as e:
            logger.error(f"Error generating {worksheet_id} for {student_id}: {e}")
            return self._create_error_worksheet(student_id, worksheet_id, str(e))
    
    def _generate_questions(self, quiz_number: int, ws_data: ParsedWorksheetData) -> List[Dict[str, Any]]:
        """Generate questions with enhanced evaluation"""
        questions = []
        
        # LCG sequence
        seed = quiz_number / 1000.0
        
        for i in range(min(ws_data.question_count, 10)):
            try:
                # LCG step
                seed = self._lcg_step(seed)
                
                # Select from lookup table
                table_size = len(ws_data.lookup_table)
                if table_size == 0:
                    continue
                
                index = (int(seed * table_size) % table_size) + 1
                
                # Get the actual keys (may not be sequential)
                table_keys = list(ws_data.lookup_table.keys())
                if not table_keys:
                    continue
                    
                key_index = (index - 1) % len(table_keys)
                selected_key = table_keys[key_index]
                
                entry = ws_data.lookup_table[selected_key]
                
                # Evaluate with enhanced evaluator
                answer, eval_error = self.evaluator.evaluate_expression(
                    entry['expression'], 
                    entry['x_value']
                )
                
                question_data = {
                    'number': i + 1,
                    'expression': entry['expression'],
                    'description': entry['description'],
                    'x_value': entry['x_value'],
                    'correct_answer': answer
                }
                
                if eval_error:
                    question_data['evaluation_error'] = eval_error
                    logger.warning(f"Evaluation error in {ws_data.worksheet_id} Q{i+1}: {eval_error}")
                
                questions.append(question_data)
                
            except Exception as e:
                logger.error(f"Error generating question {i+1} for {ws_data.worksheet_id}: {e}")
                continue
        
        return questions
    
    def _lcg_step(self, seed: float) -> float:
        """Linear congruential generator step"""
        result = (9821 * seed + 0.211327)
        return result - int(result)
    
    def _generate_quiz_number(self, student_id: str) -> int:
        """Generate quiz number from student ID"""
        try:
            id_num = int(student_id)
        except ValueError:
            id_num = abs(hash(student_id)) % 10000
            
        day_of_year = datetime.now().timetuple().tm_yday
        return (id_num - (id_num // 1000) * 1000) + day_of_year
    
    def _create_fallback_worksheet(self, student_id: str, worksheet_id: str, ws_data: ParsedWorksheetData) -> Dict[str, Any]:
        """Create fallback worksheet when extraction fails"""
        return {
            'worksheet_id': worksheet_id,
            'title': f"Fallback - {worksheet_id}",
            'subject': ws_data.subject,
            'student_id': student_id,
            'quiz_number': self._generate_quiz_number(student_id),
            'questions': [],
            'max_score': 0,
            'extraction_quality': 'failed',
            'note': 'Could not extract questions from this worksheet'
        }
    
    def _create_error_worksheet(self, student_id: str, worksheet_id: str, error: str) -> Dict[str, Any]:
        """Create error worksheet"""
        return {
            'worksheet_id': worksheet_id,
            'title': f"Error - {worksheet_id}",
            'subject': 'Unknown',
            'student_id': student_id,
            'quiz_number': 0,
            'questions': [],
            'max_score': 0,
            'extraction_quality': 'error',
            'error': error
        }
    
    def get_extraction_stats(self) -> Dict[str, Any]:
        """Get extraction quality statistics"""
        total = self.extraction_stats['total_processed']
        if total == 0:
            return self.extraction_stats
        
        return {
            **self.extraction_stats,
            'success_rate': round((total - self.extraction_stats['failed']) / total * 100, 1),
            'high_quality_rate': round(self.extraction_stats['high_quality'] / total * 100, 1)
        }


# Test the enhanced generator
def main():
    """Test enhanced generator on multiple worksheets"""
    print("🔧 Testing Enhanced Universal Generator")
    print("="*60)
    
    generator = EnhancedUniversalGenerator("/Users/oscarfimbres/git/archimedes-backend/mini-quizzes")
    
    # Test on various worksheet types
    test_worksheets = ["AL01", "DE01", "EX02", "PP01", "GE01", "FR01", "CM01", "VO01"]
    student_id = "1000"
    
    successful = 0
    total = len(test_worksheets)
    
    for worksheet_id in test_worksheets:
        try:
            print(f"\n📝 Testing {worksheet_id}...")
            worksheet = generator.generate_worksheet(student_id, worksheet_id)
            
            print(f"   Subject: {worksheet['subject']}")
            print(f"   Quality: {worksheet['extraction_quality']}")
            print(f"   Questions: {len(worksheet['questions'])}")
            
            if worksheet['questions']:
                q1 = worksheet['questions'][0]
                print(f"   Sample: {q1['expression']} when x={q1['x_value']} → {q1['correct_answer']}")
                successful += 1
            else:
                print("   ⚠️  No questions generated")
                
        except Exception as e:
            print(f"   ❌ Error: {e}")
    
    # Show statistics
    print(f"\n📊 EXTRACTION STATISTICS:")
    stats = generator.get_extraction_stats()
    for key, value in stats.items():
        print(f"   {key}: {value}")
    
    print(f"\n✅ Successfully generated {successful}/{total} worksheets")
    print(f"🚀 Enhanced generator ready for Phase 2!")


if __name__ == "__main__":
    main()