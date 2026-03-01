"""
Data Loader Service - Loads topics and activities from CSV files into memory cache
"""
import csv
import json
import os
from typing import Dict, List, Optional
from pathlib import Path
import logging
from collections import defaultdict

logger = logging.getLogger(__name__)


class DataLoader:
    """
    Loads and caches worksheet metadata from CSV files
    Simple in-memory cache for fast API responses
    """

    def __init__(self, data_path: str = "/workspace/deploy"):
        self.data_path = Path(data_path)
        self.topics_cache = {}
        self.activities_cache = {}
        self.worksheets_cache = {}
        self.loaded = False

    async def load_all_data(self) -> bool:
        """Load all data from CSV files into memory"""
        try:
            await self._load_topics()
            await self._load_activities()
            await self._organize_worksheets()
            self.loaded = True
            logger.info("Successfully loaded worksheet data from CSV files")
            return True
        except Exception as e:
            logger.error(f"Failed to load data: {e}")
            self.loaded = False
            return False

    async def _load_topics(self):
        """Load topics from topics.csv"""
        topics_file = self.data_path / "topics.csv"
        if not topics_file.exists():
            logger.warning(f"Topics file not found: {topics_file}")
            return

        topics = defaultdict(set)

        with open(topics_file, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                topic = row.get('TOPIC', '').strip()
                subtopic = row.get('SUBTOPIC', '').strip()

                if topic and subtopic:
                    topics[topic].add(subtopic)

        # Convert to regular dict with lists
        self.topics_cache = {
            topic: list(subtopics)
            for topic, subtopics in topics.items()
        }

        logger.info(f"Loaded {len(self.topics_cache)} topics")

    async def _load_activities(self):
        """Load activities from miniquiz-activities.csv"""
        activities_file = self.data_path / "miniquiz-activities.csv"
        if not activities_file.exists():
            logger.warning(f"Activities file not found: {activities_file}")
            return

        activities = []

        with open(activities_file, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                activity = {
                    'topic': row.get('TOPIC', '').strip(),
                    'subtopic': row.get('SUBTOPIC', '').strip(),
                    'activity_id': row.get('ACTIVITY_ID', '').strip(),
                    'description': row.get('ACTIVITY_DESCRIPTION', '').strip()
                }

                if activity['activity_id']:
                    activities.append(activity)
                    # Index by activity_id for quick lookup
                    self.activities_cache[activity['activity_id']] = activity

        logger.info(f"Loaded {len(activities)} activities")

    async def _organize_worksheets(self):
        """Organize worksheets by topic category"""
        # Get available worksheet files
        worksheets_dir = self.data_path / "mini-quizzes"
        if not worksheets_dir.exists():
            logger.warning(f"Worksheets directory not found: {worksheets_dir}")
            return

        # Group worksheets by topic prefix
        topic_groups = defaultdict(list)

        for html_file in worksheets_dir.glob("*.html"):
            worksheet_id = html_file.stem  # filename without extension

            # Extract topic prefix (e.g., AL from AL01)
            if len(worksheet_id) >= 2:
                topic_code = ''.join(c for c in worksheet_id if c.isalpha())

                # Get activity info if available
                activity_info = self.activities_cache.get(worksheet_id, {})

                worksheet_info = {
                    'id': worksheet_id,
                    'title': activity_info.get('description', f'Worksheet {worksheet_id}'),
                    'topic': activity_info.get('topic', 'Unknown Topic'),
                    'subtopic': activity_info.get('subtopic', 'Unknown Subtopic'),
                    'description': activity_info.get('description', ''),
                    'topic_code': topic_code,
                    'file_path': str(html_file)
                }

                topic_groups[topic_code].append(worksheet_info)

        # Sort worksheets within each topic
        for topic_code in topic_groups:
            topic_groups[topic_code].sort(key=lambda x: x['id'])

        self.worksheets_cache = dict(topic_groups)

        total_worksheets = sum(len(worksheets)
                               for worksheets in topic_groups.values())
        logger.info(
            f"Organized {total_worksheets} worksheets into {len(topic_groups)} topic groups")

    def get_topics(self) -> Dict[str, List[str]]:
        """Get all available topics"""
        if not self.loaded:
            return {}
        return self.topics_cache.copy()

    def get_topic_summary(self) -> List[Dict]:
        """Get summary of all topics with worksheet counts"""
        if not self.loaded:
            return []

        # Create mapping of topic codes to readable names
        topic_names = {
            'AL': 'Algebra',
            'AR': 'Arithmetic Operations', 
            'AN': 'Algebra & Number Theory',
            'BS': 'Basic Skills',
            'CF': 'Common Factors',
            'CI': 'Circle Geometry',
            'CM': 'Calculus & Advanced Math',
            'DE': 'Decimals',
            'EQ': 'Equations',
            'EX': 'Expressions',
            'FA': 'Factors',
            'FR': 'Fractions',
            'GE': 'Geometry',
            'IN': 'Integers',
            'IQ': 'Inequalities',
            'ME': 'Measurement',
            'MN': 'Mixed Numbers',
            'OO': 'Order of Operations',
            'PE': 'Percentages',
            'PP': 'Proportions',
            'PR': 'Probability & Statistics',
            'PT': 'Powers of Ten',
            'PY': 'Pythagorean Theorem',
            'RO': 'Roman Numerals',
            'SA': 'Surface Area',
            'SE': 'Sequences',
            'SF': 'Similar Figures',
            'TS': 'Test Preparation',
            'TT': 'Times Tables',
            'VO': 'Volume',
            'WN': 'Whole Numbers'
        }

        summaries = []
        for topic_code, worksheets in self.worksheets_cache.items():
            # Get readable topic name from mapping
            topic_name = topic_names.get(topic_code, f"{topic_code} Topic")
            
            # If we still don't have a good name, try from first worksheet
            if topic_name == f"{topic_code} Topic" and worksheets:
                worksheet_topic = worksheets[0].get('topic', '').strip()
                if worksheet_topic and worksheet_topic != 'Unknown Topic':
                    topic_name = worksheet_topic

            summary = {
                'category': topic_code,
                'name': topic_name,
                'worksheet_count': len(worksheets),
                'worksheets': worksheets
            }
            summaries.append(summary)

        # Sort by topic code
        summaries.sort(key=lambda x: x['category'])
        return summaries

    def get_worksheets_by_topic(self, topic_code: str) -> List[Dict]:
        """Get all worksheets for a specific topic"""
        if not self.loaded:
            return []

        topic_code = topic_code.upper()
        return self.worksheets_cache.get(topic_code, [])

    def get_worksheet_info(self, worksheet_id: str) -> Optional[Dict]:
        """Get information about a specific worksheet"""
        if not self.loaded:
            return None

        worksheet_id = worksheet_id.upper()

        # Search through all topics
        for worksheets in self.worksheets_cache.values():
            for worksheet in worksheets:
                if worksheet['id'] == worksheet_id:
                    return worksheet

        return None

    def get_worksheet_file_path(self, worksheet_id: str) -> Optional[str]:
        """Get the file path for a worksheet"""
        worksheet_info = self.get_worksheet_info(worksheet_id)
        if worksheet_info:
            return worksheet_info.get('file_path')
        return None

    def is_loaded(self) -> bool:
        """Check if data has been loaded"""
        return self.loaded

    async def reload_data(self) -> bool:
        """Reload all data from CSV files"""
        self.topics_cache.clear()
        self.activities_cache.clear()
        self.worksheets_cache.clear()
        self.loaded = False

        return await self.load_all_data()


# Global instance
data_loader = DataLoader()
