#!/usr/bin/env python3
"""
Worksheet Data Decoder - Extract and decode actual data from HTML worksheets
Understanding the encoding patterns used in the lookup tables.
"""

from bs4 import BeautifulSoup
import re
from typing import Dict, List, Tuple, Any
import json

def extract_vlookup_data(file_path: str) -> Dict[str, Any]:
    """Extract and decode VLOOKUP data from worksheet HTML"""
    
    with open(file_path, 'r', encoding='utf-8') as f:
        soup = BeautifulSoup(f.read(), 'html.parser')
    
    result = {
        'file_name': file_path.split('/')[-1],
        'vlookup_formulas': [],
        'lookup_tables': {},
        'decoded_expressions': [],
        'extraction_quality': 'unknown'
    }
    
    # 1. Extract VLOOKUP formulas
    vlookup_objects = soup.find_all('object', attrs={'formula': re.compile(r'VLOOKUP')})
    for obj in vlookup_objects:
        formula = obj.get('formula', '')
        if 'VLOOKUP' in formula:
            result['vlookup_formulas'].append({
                'id': obj.get('id', ''),
                'formula': formula
            })
    
    # 2. Extract lookup table ranges from formulas
    lookup_ranges = set()
    for formula_obj in result['vlookup_formulas']:
        formula = formula_obj['formula']
        # Extract range like "K1:N50"
        range_match = re.search(r'"([A-Z]+\d+:[A-Z]+\d+)"', formula)
        if range_match:
            lookup_ranges.add(range_match.group(1))
    
    # 3. Extract data from lookup ranges
    for range_spec in lookup_ranges:
        table_data = extract_range_data(soup, range_spec)
        result['lookup_tables'][range_spec] = table_data
        
        # 4. Decode expressions from the table data
        decoded = decode_expressions(table_data, range_spec)
        result['decoded_expressions'].extend(decoded)
    
    # 5. Assess quality
    num_expressions = len(result['decoded_expressions'])
    num_errors = sum(1 for expr in result['decoded_expressions'] if 'error' in expr)
    
    if num_expressions >= 8 and num_errors == 0:
        result['extraction_quality'] = 'high'
    elif num_expressions >= 5 and num_errors <= 2:
        result['extraction_quality'] = 'medium'
    else:
        result['extraction_quality'] = 'low'
    
    return result

def extract_range_data(soup: BeautifulSoup, range_spec: str) -> List[Dict[str, str]]:
    """Extract data from a specific range like K1:N50"""
    
    # Parse range specification
    start_cell, end_cell = range_spec.split(':')
    start_col = re.match(r'([A-Z]+)', start_cell).group(1)
    start_row = int(re.match(r'[A-Z]+(\d+)', start_cell).group(1))
    end_col = re.match(r'([A-Z]+)', end_cell).group(1)
    end_row = int(re.match(r'[A-Z]+(\d+)', end_cell).group(1))
    
    # Generate column letters
    def col_to_num(col):
        result = 0
        for char in col:
            result = result * 26 + (ord(char) - ord('A') + 1)
        return result
    
    def num_to_col(num):
        result = ""
        while num > 0:
            num -= 1
            result = chr(ord('A') + (num % 26)) + result
            num //= 26
        return result
    
    start_col_num = col_to_num(start_col)
    end_col_num = col_to_num(end_col)
    
    table_data = []
    
    for row in range(start_row, end_row + 1):
        row_data = {'row': row}
        for col_num in range(start_col_num, end_col_num + 1):
            col_letter = num_to_col(col_num)
            cell_id = f"{col_letter}{row}"
            
            # Find the cell in HTML (including hidden ones)
            cell = soup.find('td', id=cell_id)
            value = ""
            if cell and cell.get_text(strip=True):
                value = cell.get_text(strip=True)
            
            row_data[col_letter] = value
        
        # Only include rows with some data
        if any(row_data[col] for col in row_data if col != 'row'):
            table_data.append(row_data)
    
    return table_data

def decode_expressions(table_data: List[Dict[str, str]], range_spec: str) -> List[Dict[str, Any]]:
    """Decode numerical data into mathematical expressions"""
    
    expressions = []
    
    # Determine the structure based on the range
    # Most common patterns: K=index, L=coefficient, M=constant/x_coeff
    
    for row_data in table_data:
        if not row_data:
            continue
            
        try:
            # Extract key values
            k_val = row_data.get('K', '').strip()
            l_val = row_data.get('L', '').strip()
            m_val = row_data.get('M', '').strip()
            n_val = row_data.get('N', '').strip()
            
            if not k_val or not l_val or not m_val:
                continue
            
            index = int(k_val)
            
            # Try different decoding patterns
            decoded_expr = None
            description = ""
            
            # Pattern 1: L*X + M (linear expressions)
            if l_val.isdigit() and m_val.isdigit():
                l_coeff = int(l_val)
                m_const = int(m_val)
                
                if l_coeff == 1:
                    decoded_expr = f"X + {m_const}" if m_const > 0 else f"X - {abs(m_const)}"
                else:
                    if m_const > 0:
                        decoded_expr = f"{l_coeff}*X + {m_const}"
                    else:
                        decoded_expr = f"{l_coeff}*X - {abs(m_const)}"
                
                description = f"Linear expression: {decoded_expr}"
            
            # Pattern 2: Coefficient pairs for different operations
            elif l_val.isdigit() and m_val.isdigit():
                l_num = int(l_val)
                m_num = int(m_val)
                
                # Try to detect arithmetic patterns
                if l_num <= 10 and m_num <= 10:
                    # Simple multiplication/addition
                    decoded_expr = f"{l_num} × {m_num}"
                    description = f"Arithmetic: {l_num} times {m_num} = {l_num * m_num}"
                else:
                    # Larger numbers might be coefficients
                    decoded_expr = f"{l_num}*X + {m_num}"
                    description = f"Expression with X: {decoded_expr}"
            
            if decoded_expr:
                expressions.append({
                    'index': index,
                    'expression': decoded_expr,
                    'description': description,
                    'raw_data': {
                        'K': k_val,
                        'L': l_val,
                        'M': m_val,
                        'N': n_val
                    }
                })
            
        except (ValueError, TypeError) as e:
            expressions.append({
                'index': row_data.get('K', 'unknown'),
                'error': f"Decoding failed: {str(e)}",
                'raw_data': row_data
            })
    
    return expressions

def test_decoder():
    """Test the decoder on a known worksheet"""
    
    test_file = "/Users/oscarfimbres/git/archimedes-backend/mini-quizzes/AL01.html"
    result = extract_vlookup_data(test_file)
    
    print(f"=== WORKSHEET DATA DECODER TEST ===")
    print(f"File: {result['file_name']}")
    print(f"Quality: {result['extraction_quality']}")
    print(f"VLOOKUP formulas found: {len(result['vlookup_formulas'])}")
    print(f"Lookup tables: {list(result['lookup_tables'].keys())}")
    print(f"Decoded expressions: {len(result['decoded_expressions'])}")
    
    print(f"\n=== SAMPLE VLOOKUP FORMULAS ===")
    for formula in result['vlookup_formulas'][:3]:
        print(f"- {formula['id']}: {formula['formula']}")
    
    print(f"\n=== SAMPLE DECODED EXPRESSIONS ===")
    for expr in result['decoded_expressions'][:10]:
        if 'error' not in expr:
            print(f"Row {expr['index']:2}: {expr['expression']:15} | {expr['description']}")
        else:
            print(f"Row {expr['index']:2}: ERROR - {expr['error']}")
    
    print(f"\n=== LOOKUP TABLE SAMPLE ===")
    for range_name, table in result['lookup_tables'].items():
        print(f"Range {range_name}: {len(table)} rows")
        for row in table[:5]:
            print(f"  Row {row['row']}: K={row.get('K','')} L={row.get('L','')} M={row.get('M','')} N={row.get('N','')}")
        if len(table) > 5:
            print(f"  ... and {len(table)-5} more rows")
        break
    
    return result

if __name__ == "__main__":
    result = test_decoder()