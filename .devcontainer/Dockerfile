# Use Python 3.13 with Debian Bookworm (latest Python + stable Debian)
FROM mcr.microsoft.com/devcontainers/python:3.12

# Install PostgreSQL client tools and AWS CLI
RUN apt-get update && apt-get install -y \
    postgresql-client \
    awscli \
    && rm -rf /var/lib/apt/lists/*

# Set up working directory
WORKDIR /workspace

# Copy requirements file
COPY requirements.txt .

# Install Python dependencies
RUN pip3 --disable-pip-version-check --no-cache-dir install -r requirements.txt