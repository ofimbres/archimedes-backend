# Use Python 3.11 slim image
FROM mcr.microsoft.com/devcontainers/python:3.13

# Install PostgreSQL client tools
RUN apt-get update && apt-get install -y \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Set up working directory
WORKDIR /workspace

# Copy requirements file
COPY requirements.txt .

# Install Python dependencies
RUN pip3 --disable-pip-version-check --no-cache-dir install -r requirements.txt