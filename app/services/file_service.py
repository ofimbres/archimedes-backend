"""
File Service - Handles worksheet file access (local or S3)
Uses environment variables for configuration
"""
import os
import aiofiles
import logging
from pathlib import Path
from typing import Optional, Dict, Any
import boto3
from botocore.exceptions import ClientError, NoCredentialsError

logger = logging.getLogger(__name__)


class FileService:
    """
    Handles worksheet file operations with configurable storage backend
    Supports both local filesystem and S3 storage
    """

    def __init__(self):
        # Configuration from environment variables
        self.use_s3 = os.getenv('WORKSHEETS_USE_S3', 'false').lower() == 'true'
        self.local_path = os.getenv(
            'WORKSHEETS_LOCAL_PATH', '/workspace/deploy/mini-quizzes')

        # S3 configuration
        self.s3_bucket = os.getenv('WORKSHEETS_S3_BUCKET', '')
        self.s3_prefix = os.getenv('WORKSHEETS_S3_PREFIX', 'worksheets/')
        self.s3_region = os.getenv('AWS_REGION', 'us-east-1')

        # Initialize S3 client if needed
        self.s3_client = None
        if self.use_s3:
            self._init_s3_client()

        logger.info(
            f"FileService initialized - Use S3: {self.use_s3}, Local path: {self.local_path}")

    def _init_s3_client(self):
        """Initialize S3 client with credentials from environment"""
        try:
            # Credentials from environment variables
            aws_access_key = os.getenv('AWS_ACCESS_KEY_ID')
            aws_secret_key = os.getenv('AWS_SECRET_ACCESS_KEY')

            if aws_access_key and aws_secret_key:
                self.s3_client = boto3.client(
                    's3',
                    aws_access_key_id=aws_access_key,
                    aws_secret_access_key=aws_secret_key,
                    region_name=self.s3_region
                )
            else:
                # Use default credential chain (IAM roles, etc.)
                self.s3_client = boto3.client('s3', region_name=self.s3_region)

            logger.info("S3 client initialized successfully")

        except Exception as e:
            logger.error(f"Failed to initialize S3 client: {e}")
            self.s3_client = None

    async def get_worksheet_content(self, worksheet_id: str) -> Optional[str]:
        """
        Get worksheet HTML content from storage
        """
        try:
            if self.use_s3 and self.s3_client:
                return await self._get_from_s3(worksheet_id)
            else:
                return await self._get_from_local(worksheet_id)

        except Exception as e:
            logger.error(f"Failed to get worksheet {worksheet_id}: {e}")
            return None

    async def _get_from_local(self, worksheet_id: str) -> Optional[str]:
        """Get worksheet content from local filesystem"""
        file_path = Path(self.local_path) / f"{worksheet_id}.html"

        if not file_path.exists():
            logger.warning(f"Worksheet file not found: {file_path}")
            return None

        try:
            async with aiofiles.open(file_path, 'r', encoding='utf-8') as file:
                content = await file.read()
                logger.debug(
                    f"Loaded worksheet {worksheet_id} from local file")
                return content

        except Exception as e:
            logger.error(f"Error reading local file {file_path}: {e}")
            return None

    async def _get_from_s3(self, worksheet_id: str) -> Optional[str]:
        """Get worksheet content from S3"""
        if not self.s3_client or not self.s3_bucket:
            logger.error("S3 client or bucket not configured")
            return None

        s3_key = f"{self.s3_prefix}{worksheet_id}.html"

        try:
            response = self.s3_client.get_object(
                Bucket=self.s3_bucket, Key=s3_key)
            content = response['Body'].read().decode('utf-8')
            logger.debug(f"Loaded worksheet {worksheet_id} from S3")
            return content

        except ClientError as e:
            error_code = e.response['Error']['Code']
            if error_code == 'NoSuchKey':
                logger.warning(f"Worksheet not found in S3: {s3_key}")
            else:
                logger.error(f"S3 error for {s3_key}: {e}")
            return None
        except Exception as e:
            logger.error(f"Unexpected error accessing S3: {e}")
            return None

    async def check_worksheet_exists(self, worksheet_id: str) -> bool:
        """Check if a worksheet file exists"""
        if self.use_s3 and self.s3_client:
            return await self._check_s3_exists(worksheet_id)
        else:
            return await self._check_local_exists(worksheet_id)

    async def _check_local_exists(self, worksheet_id: str) -> bool:
        """Check if worksheet exists in local filesystem"""
        file_path = Path(self.local_path) / f"{worksheet_id}.html"
        return file_path.exists()

    async def _check_s3_exists(self, worksheet_id: str) -> bool:
        """Check if worksheet exists in S3"""
        if not self.s3_client or not self.s3_bucket:
            return False

        s3_key = f"{self.s3_prefix}{worksheet_id}.html"

        try:
            self.s3_client.head_object(Bucket=self.s3_bucket, Key=s3_key)
            return True
        except ClientError as e:
            error_code = e.response['Error']['Code']
            if error_code == '404':
                return False
            else:
                logger.error(f"Error checking S3 object existence: {e}")
                return False

    async def list_available_worksheets(self) -> list[str]:
        """List all available worksheet IDs"""
        if self.use_s3 and self.s3_client:
            return await self._list_s3_worksheets()
        else:
            return await self._list_local_worksheets()

    async def _list_local_worksheets(self) -> list[str]:
        """List worksheets in local directory"""
        try:
            worksheets_dir = Path(self.local_path)
            if not worksheets_dir.exists():
                return []

            worksheet_ids = []
            for html_file in worksheets_dir.glob("*.html"):
                worksheet_ids.append(html_file.stem)

            return sorted(worksheet_ids)

        except Exception as e:
            logger.error(f"Error listing local worksheets: {e}")
            return []

    async def _list_s3_worksheets(self) -> list[str]:
        """List worksheets in S3 bucket"""
        if not self.s3_client or not self.s3_bucket:
            return []

        try:
            response = self.s3_client.list_objects_v2(
                Bucket=self.s3_bucket,
                Prefix=self.s3_prefix
            )

            worksheet_ids = []
            for obj in response.get('Contents', []):
                key = obj['Key']
                if key.endswith('.html'):
                    # Extract worksheet ID from S3 key
                    filename = key.split('/')[-1]
                    worksheet_id = filename.replace('.html', '')
                    worksheet_ids.append(worksheet_id)

            return sorted(worksheet_ids)

        except Exception as e:
            logger.error(f"Error listing S3 worksheets: {e}")
            return []

    def get_storage_info(self) -> Dict[str, Any]:
        """Get information about current storage configuration"""
        info = {
            'storage_type': 'S3' if self.use_s3 else 'Local',
            'local_path': self.local_path,
        }

        if self.use_s3:
            info.update({
                's3_bucket': self.s3_bucket,
                's3_prefix': self.s3_prefix,
                's3_region': self.s3_region,
                's3_client_ready': self.s3_client is not None
            })

        return info


# Global instance
file_service = FileService()
