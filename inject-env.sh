#!/bin/bash

ENV_FILE=$1

if [[ -z "$ENV_FILE" ]]; then
  echo "❌ Please provide an env file: ./inject-env.sh .env.test"
  exit 1
fi

echo "✅ Loading secrets from $ENV_FILE..."
export $(grep -v '^#' $ENV_FILE | xargs)

echo "🚀 Starting Docker Compose..."
docker-compose -f docker-compose.yml up --build
