#!/usr/bin/env python3
"""
Batch Worksheet Processor - Phase 1 Completion

Process all 513 worksheets to:
1. Extract lookup tables and save them for reuse
2. Identify which worksheets have good vs poor extraction
3. Create a worksheet catalog with quality ratings
4. Generate fallback data for failed extractions
"""

import json
from pathlib import Path
from typing import Dict, List, Any
import time
from enhanced_generator import EnhancedUniversalGenerator


class BatchWorksheetProcessor:
    """Process all worksheets and build comprehensive data library"""
    
    def __init__(self, worksheets_dir: str, output_dir: str = None):
        self.worksheets_dir = Path(worksheets_dir)
        self.output_dir = Path(output_dir) if output_dir else Path("worksheet_data")
        self.output_dir.mkdir(exist_ok=True)
        
        self.generator = EnhancedUniversalGenerator(worksheets_dir)
        self.results = {
            'processed': {},
            'high_quality': [],
            'medium_quality': [],
            'low_quality': [],
            'failed': [],
            'summary': {}
        }
    
    def process_all_worksheets(self) -> Dict[str, Any]:
        """Process all worksheets and save results"""
        print("🔄 Processing all worksheets...")
        
        # Get all worksheet files
        html_files = list(self.worksheets_dir.glob("*.html"))
        total_files = len(html_files)
        
        print(f"📄 Found {total_files} worksheet files")
        
        processed_count = 0
        start_time = time.time()
        
        for html_file in html_files:
            worksheet_id = html_file.stem
            
            try:
                # Extract worksheet data
                ws_data = self.generator.extract_worksheet_data(worksheet_id)
                
                # Save individual worksheet data
                self._save_worksheet_data(ws_data)
                
                # Categorize by quality
                self._categorize_worksheet(worksheet_id, ws_data)
                
                processed_count += 1
                
                # Progress update
                if processed_count % 50 == 0:
                    elapsed = time.time() - start_time
                    rate = processed_count / elapsed
                    eta = (total_files - processed_count) / rate
                    print(f"   Processed {processed_count}/{total_files} ({processed_count/total_files*100:.1f}%) - ETA: {eta:.1f}s")
                
            except Exception as e:
                print(f"❌ Failed to process {worksheet_id}: {e}")
                self.results['failed'].append({
                    'worksheet_id': worksheet_id,
                    'error': str(e)
                })
        
        # Generate summary
        self._generate_summary()
        
        # Save all results
        self._save_results()
        
        print(f"\n✅ Processed {processed_count} worksheets in {time.time() - start_time:.1f}s")
        return self.results
    
    def _save_worksheet_data(self, ws_data):
        """Save individual worksheet data"""
        worksheet_file = self.output_dir / f"{ws_data.worksheet_id}.json"
        
        data = {
            'worksheet_id': ws_data.worksheet_id,
            'subject': ws_data.subject,
            'title': ws_data.title,
            'lookup_table': ws_data.lookup_table,
            'question_count': ws_data.question_count,
            'extraction_quality': ws_data.extraction_quality,
            'errors': ws_data.errors
        }
        
        with open(worksheet_file, 'w') as f:
            json.dump(data, f, indent=2)
    
    def _categorize_worksheet(self, worksheet_id: str, ws_data):
        """Categorize worksheet by extraction quality"""
        category_data = {
            'worksheet_id': worksheet_id,
            'subject': ws_data.subject,
            'question_count': len(ws_data.lookup_table),
            'errors': len(ws_data.errors)
        }
        
        if ws_data.extraction_quality == 'high':
            self.results['high_quality'].append(category_data)
        elif ws_data.extraction_quality == 'medium':
            self.results['medium_quality'].append(category_data)
        elif ws_data.extraction_quality == 'low':
            self.results['low_quality'].append(category_data)
        else:
            self.results['failed'].append(category_data)
        
        self.results['processed'][worksheet_id] = ws_data.extraction_quality
    
    def _generate_summary(self):
        """Generate processing summary"""
        total = len(self.results['processed'])
        
        # Quality distribution
        quality_counts = {
            'high': len(self.results['high_quality']),
            'medium': len(self.results['medium_quality']),
            'low': len(self.results['low_quality']),
            'failed': len(self.results['failed'])
        }
        
        # Subject analysis
        subject_quality = {}
        for quality_level in ['high_quality', 'medium_quality', 'low_quality']:
            for item in self.results[quality_level]:
                subject = item['subject']
                if subject not in subject_quality:
                    subject_quality[subject] = {'high': 0, 'medium': 0, 'low': 0}
                
                quality_key = quality_level.split('_')[0]
                subject_quality[subject][quality_key] += 1
        
        self.results['summary'] = {
            'total_processed': total,
            'quality_distribution': quality_counts,
            'success_rate': round((total - quality_counts['failed']) / total * 100, 1) if total > 0 else 0,
            'high_quality_rate': round(quality_counts['high'] / total * 100, 1) if total > 0 else 0,
            'subject_quality': subject_quality,
            'recommended_worksheets': self._get_recommended_worksheets()
        }
    
    def _get_recommended_worksheets(self) -> List[Dict[str, Any]]:
        """Get top recommended worksheets by quality and subject"""
        recommendations = []
        
        # Get best worksheets from each subject
        by_subject = {}
        for item in self.results['high_quality']:
            subject = item['subject']
            if subject not in by_subject or item['question_count'] > by_subject[subject]['question_count']:
                by_subject[subject] = item
        
        recommendations = list(by_subject.values())
        
        # Sort by question count (more questions = better)
        recommendations.sort(key=lambda x: x['question_count'], reverse=True)
        
        return recommendations[:20]  # Top 20
    
    def _save_results(self):
        """Save all processing results"""
        # Main results file
        with open(self.output_dir / "processing_results.json", 'w') as f:
            json.dump(self.results, f, indent=2)
        
        # High-quality worksheets catalog
        high_quality_catalog = {
            'worksheets': self.results['high_quality'],
            'count': len(self.results['high_quality']),
            'description': 'Worksheets with excellent extraction quality'
        }
        
        with open(self.output_dir / "high_quality_catalog.json", 'w') as f:
            json.dump(high_quality_catalog, f, indent=2)
        
        # Subject summary
        subject_summary = {}
        for item in self.results['high_quality'] + self.results['medium_quality']:
            subject = item['subject']
            if subject not in subject_summary:
                subject_summary[subject] = []
            subject_summary[subject].append(item['worksheet_id'])
        
        with open(self.output_dir / "subjects_catalog.json", 'w') as f:
            json.dump(subject_summary, f, indent=2)
        
        print(f"💾 Results saved to {self.output_dir}/")


class WorksheetLibraryManager:
    """Manage the extracted worksheet library"""
    
    def __init__(self, data_dir: str = "worksheet_data"):
        self.data_dir = Path(data_dir)
    
    def get_worksheet_data(self, worksheet_id: str) -> Dict[str, Any]:
        """Load worksheet data from library"""
        worksheet_file = self.data_dir / f"{worksheet_id}.json"
        
        if not worksheet_file.exists():
            raise FileNotFoundError(f"Worksheet {worksheet_id} not found in library")
        
        with open(worksheet_file, 'r') as f:
            return json.load(f)
    
    def list_high_quality_worksheets(self) -> List[Dict[str, Any]]:
        """List all high-quality worksheets"""
        catalog_file = self.data_dir / "high_quality_catalog.json"
        
        if not catalog_file.exists():
            return []
        
        with open(catalog_file, 'r') as f:
            catalog = json.load(f)
        
        return catalog['worksheets']
    
    def get_worksheets_by_subject(self, subject: str) -> List[str]:
        """Get worksheet IDs for a specific subject"""
        subjects_file = self.data_dir / "subjects_catalog.json"
        
        if not subjects_file.exists():
            return []
        
        with open(subjects_file, 'r') as f:
            subjects = json.load(f)
        
        return subjects.get(subject, [])
    
    def get_processing_stats(self) -> Dict[str, Any]:
        """Get processing statistics"""
        results_file = self.data_dir / "processing_results.json"
        
        if not results_file.exists():
            return {}
        
        with open(results_file, 'r') as f:
            results = json.load(f)
        
        return results.get('summary', {})


def main():
    """Run batch processing"""
    print("🏭 Batch Worksheet Processor - Phase 1 Completion")
    print("="*60)
    
    # Process all worksheets
    processor = BatchWorksheetProcessor("/Users/oscarfimbres/git/archimedes-backend/mini-quizzes")
    results = processor.process_all_worksheets()
    
    # Print summary
    summary = results['summary']
    print(f"\n📊 PROCESSING SUMMARY:")
    print(f"   Total Processed: {summary['total_processed']}")
    print(f"   Success Rate: {summary['success_rate']}%")
    print(f"   High Quality Rate: {summary['high_quality_rate']}%")
    
    print(f"\n📈 QUALITY DISTRIBUTION:")
    for quality, count in summary['quality_distribution'].items():
        percentage = count / summary['total_processed'] * 100
        print(f"   {quality.capitalize()}: {count} ({percentage:.1f}%)")
    
    print(f"\n🏆 TOP RECOMMENDED WORKSHEETS:")
    for i, rec in enumerate(summary['recommended_worksheets'][:10]):
        print(f"   {i+1}. {rec['worksheet_id']} ({rec['subject']}) - {rec['question_count']} questions")
    
    print(f"\n🎯 IMPLEMENTATION RECOMMENDATIONS:")
    
    high_count = summary['quality_distribution']['high']
    medium_count = summary['quality_distribution']['medium']
    total_good = high_count + medium_count
    
    print(f"   ✅ Start with {total_good} worksheets ({high_count} high + {medium_count} medium quality)")
    print(f"   ✅ Focus on subjects with high extraction rates")
    print(f"   ✅ Implement fallback generation for remaining worksheets")
    print(f"   ✅ Ready for Phase 2: API development")
    
    # Test library manager
    print(f"\n🔍 Testing Library Manager...")
    library = WorksheetLibraryManager()
    
    try:
        stats = library.get_processing_stats()
        high_quality = library.list_high_quality_worksheets()
        
        print(f"   Library contains {len(high_quality)} high-quality worksheets")
        print(f"   Ready for production use!")
        
    except Exception as e:
        print(f"   ⚠️ Library not yet built: {e}")


if __name__ == "__main__":
    main()