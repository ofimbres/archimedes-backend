# Development Cleanup Script

This script cleans up development data from both AWS Cognito and the PostgreSQL database.

## ⚠️ WARNING
**This script will DELETE ALL DATA! Use only in development environments!**

## Usage

### Basic cleanup (truncate tables only)
```bash
cd /workspace
python scripts/dev_cleanup.py
```

### Force cleanup (no confirmation prompt)
```bash
python scripts/dev_cleanup.py --force
```

### Full reset (drop and recreate schema)
```bash
python scripts/dev_cleanup.py --reset-schema
```

### Force full reset
```bash
python scripts/dev_cleanup.py --reset-schema --force
```

## What it does

1. **Cognito Cleanup**: Deletes all users from the AWS Cognito User Pool
2. **Database Cleanup**: 
   - Default: Truncates all tables (keeps schema)
   - With `--reset-schema`: Drops and recreates all tables

## Safety Features

- ✅ Prevents running in production environment
- ✅ Requires confirmation unless `--force` is used
- ✅ Shows what will be deleted before running
- ✅ Handles errors gracefully

## Requirements

- AWS credentials configured (via `.env` file or AWS CLI)
- Database connection configured
- Development environment only

## Example Output

```
============================================================
🧹 ARCHIMEDES DEVELOPMENT CLEANUP SCRIPT
============================================================
🚀 Starting full cleanup...
   AWS Region: us-west-2
   Cognito User Pool: us-west-2_qNjGNCmga
   Database: localhost:5432/archimedes
   ⚠️  This will DELETE ALL DATA!

⚠️  Are you sure you want to delete ALL data? (type 'yes' to confirm): yes

🧹 Cleaning up Cognito users...
  ✅ Deleted Cognito user: oscar.fimbres
  ✅ Deleted Cognito user: jane.smith
🎉 Deleted 2 Cognito users

🧹 Cleaning up database tables...
  ✅ Cleared 5 records from students
  ✅ Cleared 2 records from schools
🎉 Cleared 2 tables

🎉 Cleanup complete!
   Cognito users deleted: 2
   Database: Cleaned
```