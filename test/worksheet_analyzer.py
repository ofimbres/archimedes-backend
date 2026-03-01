#!/usr/bin/env python3
"""
Comprehensive Worksheet Analysis Tool

Analyzes ALL worksheets in the directory to determine:
1. Common patterns across different worksheet types
2. Different generation algorithms used
3. Scalable approaches for implementation
"""

import os
import re
import json
from pathlib import Path
from typing import Dict, List, Set, Tuple, Any
from collections import defaultdict, Counter
from dataclasses import dataclass, asdict
import glob


@dataclass
class WorksheetInfo:
    """Information extracted from a worksheet"""
    file_name: str
    worksheet_id: str
    subject_code: str
    number: str
    file_size: int
    line_count: int
    has_formulas: bool
    formula_count: int
    has_lookup_tables: bool
    iterations: int
    last_cell: str
    first_input: str
    zoom_level: float
    unique_patterns: List[str]


class WorksheetAnalyzer:
    """Analyzes all worksheets to find patterns and determine best approach"""
    
    def __init__(self, worksheets_dir: str):
        self.worksheets_dir = Path(worksheets_dir)
        self.worksheet_info: List[WorksheetInfo] = []
        self.patterns = defaultdict(list)
        
    def analyze_all_worksheets(self) -> Dict[str, Any]:
        """Analyze all worksheets and return comprehensive report"""
        print("🔍 Analyzing all worksheets...")
        
        # Get all HTML files
        html_files = list(self.worksheets_dir.glob("*.html"))
        print(f"📄 Found {len(html_files)} worksheet files")
        
        for html_file in html_files:
            try:
                info = self._analyze_single_worksheet(html_file)
                if info:
                    self.worksheet_info.append(info)
                    self._categorize_patterns(info)
            except Exception as e:
                print(f"❌ Error analyzing {html_file.name}: {e}")
        
        return self._generate_report()
    
    def _analyze_single_worksheet(self, html_file: Path) -> WorksheetInfo:
        """Analyze a single worksheet file"""
        with open(html_file, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        # Extract basic info
        file_name = html_file.name
        worksheet_id = html_file.stem
        subject_code = worksheet_id[:2]
        number = worksheet_id[2:]
        
        # File metrics
        file_size = html_file.stat().st_size
        line_count = content.count('\n')
        
        # Extract JavaScript variables
        iterations = self._extract_js_var(content, 'iterations', 4)
        last_cell = self._extract_js_var(content, 'lastCell', '')
        first_input = self._extract_js_var(content, 'firstInput', '')
        zoom_level = self._extract_zoom_level(content)
        
        # Formula analysis
        formula_pattern = r"<object id='f\w+' formula='([^']+)'></object>"
        formulas = re.findall(formula_pattern, content)
        has_formulas = len(formulas) > 0
        formula_count = len(formulas)
        
        # Check for lookup tables (VLOOKUP usage)
        has_lookup_tables = 'VLOOKUP' in content
        
        # Identify unique patterns
        unique_patterns = self._identify_patterns(content)
        
        return WorksheetInfo(
            file_name=file_name,
            worksheet_id=worksheet_id,
            subject_code=subject_code,
            number=number,
            file_size=file_size,
            line_count=line_count,
            has_formulas=has_formulas,
            formula_count=formula_count,
            has_lookup_tables=has_lookup_tables,
            iterations=iterations,
            last_cell=last_cell,
            first_input=first_input,
            zoom_level=zoom_level,
            unique_patterns=unique_patterns
        )
    
    def _extract_js_var(self, content: str, var_name: str, default: Any) -> Any:
        """Extract JavaScript variable value"""
        pattern = rf"var {var_name} = ['\"]*([^;'\"]+)['\"]*;"
        match = re.search(pattern, content)
        if match:
            value = match.group(1)
            # Try to convert to appropriate type
            try:
                if value.isdigit():
                    return int(value)
                elif value.replace('.', '').isdigit():
                    return float(value)
                else:
                    return value.strip('\'"')
            except:
                return value
        return default
    
    def _extract_zoom_level(self, content: str) -> float:
        """Extract zoom level from CSS"""
        zoom_pattern = r"zoom:([0-9.]+);"
        match = re.search(zoom_pattern, content)
        return float(match.group(1)) if match else 1.0
    
    def _identify_patterns(self, content: str) -> List[str]:
        """Identify unique patterns in the worksheet"""
        patterns = []
        
        # Check for specific formula patterns
        if 'RAND()' in content:
            patterns.append('random_generation')
        if 'VLOOKUP' in content:
            patterns.append('lookup_tables')
        if 'INT(' in content and 'RAND' in content:
            patterns.append('integer_random')
        if 'LCG' in content or '9821' in content:
            patterns.append('linear_congruential')
        if 'TODAY()' in content:
            patterns.append('date_based')
        if re.search(r'H60.*1000', content):
            patterns.append('student_id_seeding')
        if 'SUM(' in content:
            patterns.append('auto_grading')
        if re.search(r'if\([^)]+==""\)', content):
            patterns.append('conditional_logic')
        
        return patterns
    
    def _categorize_patterns(self, info: WorksheetInfo):
        """Categorize worksheet by its patterns"""
        for pattern in info.unique_patterns:
            self.patterns[pattern].append(info.worksheet_id)
    
    def _generate_report(self) -> Dict[str, Any]:
        """Generate comprehensive analysis report"""
        # Subject analysis
        subjects = Counter(info.subject_code for info in self.worksheet_info)
        
        # Size analysis
        sizes = [info.file_size for info in self.worksheet_info]
        avg_size = sum(sizes) / len(sizes) if sizes else 0
        
        # Formula analysis
        formula_counts = [info.formula_count for info in self.worksheet_info]
        avg_formulas = sum(formula_counts) / len(formula_counts) if formula_counts else 0
        
        # Pattern analysis
        pattern_stats = {
            pattern: len(worksheets) 
            for pattern, worksheets in self.patterns.items()
        }
        
        # Generation type classification
        generation_types = self._classify_generation_types()
        
        return {
            "summary": {
                "total_worksheets": len(self.worksheet_info),
                "subjects": dict(subjects),
                "avg_file_size_kb": round(avg_size / 1024, 2),
                "avg_formulas_per_worksheet": round(avg_formulas, 1),
                "worksheets_with_formulas": sum(1 for info in self.worksheet_info if info.has_formulas),
                "worksheets_with_lookup_tables": sum(1 for info in self.worksheet_info if info.has_lookup_tables)
            },
            "patterns": pattern_stats,
            "generation_types": generation_types,
            "subject_breakdown": self._analyze_by_subject(),
            "recommendations": self._generate_recommendations()
        }
    
    def _classify_generation_types(self) -> Dict[str, List[str]]:
        """Classify worksheets by their generation approach"""
        classification = defaultdict(list)
        
        for info in self.worksheet_info:
            if 'linear_congruential' in info.unique_patterns:
                classification['LCG_based'].append(info.worksheet_id)
            elif 'student_id_seeding' in info.unique_patterns:
                classification['ID_seeded'].append(info.worksheet_id)
            elif 'random_generation' in info.unique_patterns:
                classification['random_based'].append(info.worksheet_id)
            elif info.has_lookup_tables:
                classification['lookup_table_based'].append(info.worksheet_id)
            else:
                classification['static_or_unknown'].append(info.worksheet_id)
        
        return dict(classification)
    
    def _analyze_by_subject(self) -> Dict[str, Dict[str, Any]]:
        """Analyze patterns by subject"""
        by_subject = defaultdict(lambda: {
            'count': 0,
            'avg_formulas': 0,
            'common_patterns': [],
            'examples': []
        })
        
        for info in self.worksheet_info:
            subject = info.subject_code
            by_subject[subject]['count'] += 1
            by_subject[subject]['examples'].append(info.worksheet_id)
            
            # Track formulas
            if not by_subject[subject]['avg_formulas']:
                by_subject[subject]['avg_formulas'] = info.formula_count
            else:
                by_subject[subject]['avg_formulas'] = (
                    by_subject[subject]['avg_formulas'] + info.formula_count
                ) / 2
        
        return dict(by_subject)
    
    def _generate_recommendations(self) -> List[str]:
        """Generate recommendations for implementation approach"""
        recommendations = []
        
        # Count worksheets by type
        lcg_count = len(self.patterns.get('linear_congruential', []))
        lookup_count = len(self.patterns.get('lookup_tables', []))
        total = len(self.worksheet_info)
        
        if lcg_count > total * 0.5:
            recommendations.append(
                f"🎯 RECOMMENDED: LCG-based approach - {lcg_count}/{total} worksheets use Linear Congruential Generator"
            )
        
        if lookup_count > total * 0.7:
            recommendations.append(
                f"📊 Extract lookup tables - {lookup_count}/{total} worksheets use VLOOKUP"
            )
        
        if len(self.patterns.get('student_id_seeding', [])) > total * 0.8:
            recommendations.append(
                "🔐 Student ID seeding is universal - implement consistent hashing"
            )
        
        # Subject-specific recommendations
        subjects = Counter(info.subject_code for info in self.worksheet_info)
        largest_subject = subjects.most_common(1)[0]
        recommendations.append(
            f"🏆 Start with {largest_subject[0]} subject ({largest_subject[1]} worksheets) for highest ROI"
        )
        
        return recommendations


def main():
    """Run comprehensive analysis"""
    worksheets_dir = "/Users/oscarfimbres/git/archimedes-backend/mini-quizzes"
    
    analyzer = WorksheetAnalyzer(worksheets_dir)
    report = analyzer.analyze_all_worksheets()
    
    # Print report
    print("\n" + "="*60)
    print("📊 COMPREHENSIVE WORKSHEET ANALYSIS REPORT")
    print("="*60)
    
    # Summary
    summary = report['summary']
    print(f"\n📈 SUMMARY:")
    print(f"   Total Worksheets: {summary['total_worksheets']}")
    print(f"   Average File Size: {summary['avg_file_size_kb']} KB")
    print(f"   Worksheets with Formulas: {summary['worksheets_with_formulas']}")
    print(f"   Worksheets with Lookup Tables: {summary['worksheets_with_lookup_tables']}")
    print(f"   Average Formulas per Worksheet: {summary['avg_formulas_per_worksheet']}")
    
    # Top subjects
    print(f"\n🎯 TOP SUBJECTS:")
    for subject, count in sorted(summary['subjects'].items(), key=lambda x: x[1], reverse=True)[:10]:
        print(f"   {subject}: {count} worksheets")
    
    # Pattern analysis
    print(f"\n🔍 PATTERN ANALYSIS:")
    for pattern, count in sorted(report['patterns'].items(), key=lambda x: x[1], reverse=True):
        print(f"   {pattern}: {count} worksheets")
    
    # Generation types
    print(f"\n⚙️  GENERATION TYPES:")
    for gen_type, worksheets in report['generation_types'].items():
        print(f"   {gen_type}: {len(worksheets)} worksheets")
        if len(worksheets) <= 5:
            print(f"      Examples: {', '.join(worksheets)}")
        else:
            print(f"      Examples: {', '.join(worksheets[:5])}...")
    
    # Recommendations
    print(f"\n🚀 RECOMMENDATIONS:")
    for rec in report['recommendations']:
        print(f"   {rec}")
    
    # Save detailed report
    with open("worksheet_analysis_report.json", "w") as f:
        json.dump(report, f, indent=2)
    print(f"\n💾 Detailed report saved to: worksheet_analysis_report.json")


if __name__ == "__main__":
    main()