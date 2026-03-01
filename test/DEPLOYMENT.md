# 🚀 Production Deployment Guide

## Quick Production Deployment

### Prerequisites
- Docker & Docker Compose installed
- SSL certificates (for HTTPS)
- Production database credentials
- AWS credentials configured

### 1. Configure Environment
```bash
# Copy and configure production environment
cp .env.production .env
# Edit .env with your production values
```

### 2. Deploy
```bash
# Quick deployment
./deploy.sh

# With specific image tag
./deploy.sh v1.0.0

# With custom env file
./deploy.sh latest .env.custom
```

### 3. Production Checklist

#### Security
- [ ] Update SSL certificates in `nginx/ssl/`
- [ ] Configure firewall (only 80/443 open)
- [ ] Set strong database passwords
- [ ] Configure AWS IAM with minimal permissions
- [ ] Enable CORS restrictions in production

#### Database
- [ ] Configure PostgreSQL with production settings
- [ ] Set up automated backups
- [ ] Configure connection pooling
- [ ] Run database migrations

#### Monitoring
- [ ] Set up log aggregation (ELK stack)
- [ ] Configure Sentry for error tracking  
- [ ] Set up Prometheus/Grafana monitoring
- [ ] Configure health check alerts

#### Performance
- [ ] Configure Redis for caching
- [ ] Set up CDN for static assets
- [ ] Tune Uvicorn worker count
- [ ] Configure database connection pool

### 4. Production Architecture

```
Internet → Nginx (SSL/Rate Limiting) → FastAPI App → PostgreSQL
                                    → Redis (Cache)
```

### 5. Scaling Options

#### Horizontal Scaling
```yaml
# In docker-compose.production.yml
archimedes-app:
  deploy:
    replicas: 3
```

#### Load Balancer
```nginx
upstream archimedes_backend {
    server archimedes-app-1:8001;
    server archimedes-app-2:8001;
    server archimedes-app-3:8001;
}
```

### 6. Maintenance Commands

```bash
# View logs
docker-compose -f docker-compose.production.yml logs -f

# Database backup
docker exec archimedes-postgres pg_dump -U archimedes archimedes_prod > backup.sql

# Update application
git pull
./deploy.sh v1.1.0

# Rolling restart
docker-compose -f docker-compose.production.yml restart archimedes-app

# Scale up
docker-compose -f docker-compose.production.yml up -d --scale archimedes-app=3
```

### 7. Troubleshooting

#### Health Check Failed
```bash
# Check app logs
docker logs archimedes-app

# Check database connection
docker exec archimedes-app curl http://localhost:8001/api/v1/health/status
```

#### Performance Issues
```bash
# Monitor resource usage
docker stats

# Check database performance
docker exec archimedes-postgres pg_stat_activity
```

#### SSL Issues
```bash
# Verify certificates
openssl x509 -in nginx/ssl/cert.pem -text -noout

# Test SSL configuration
curl -I https://your-domain.com
```

### 8. Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection | `postgresql+asyncpg://user:pass@host:5432/db` |
| `COGNITO_USER_POOL_ID` | AWS Cognito Pool ID | `us-west-2_abc123456` |
| `AWS_ACCESS_KEY_ID` | AWS Access Key | `AKIAIOSFODNN7EXAMPLE` |
| `UVICORN_WORKERS` | Worker processes | `4` |
| `LOG_LEVEL` | Logging level | `info` |

### 9. Performance Tuning

#### Database
```sql
-- PostgreSQL tuning
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
```

#### Application
```python
# In config.py
DB_POOL_SIZE = 20
DB_POOL_OVERFLOW = 10
UVICORN_WORKERS = 4  # CPU cores
```

### 10. Backup Strategy

#### Automated Daily Backups
```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
docker exec archimedes-postgres pg_dump -U archimedes archimedes_prod | \
  gzip > backups/backup_$DATE.sql.gz
```

#### S3 Backup Upload
```bash
# Upload to S3
aws s3 cp backups/backup_$DATE.sql.gz s3://your-backup-bucket/
```