#!/bin/bash

# Create the directory if it doesn't exist
mkdir -p /Users/robertwohl/IdeaProjects/IntegrationTesting/src/main/webapp/dashboard/public

# Copy the metrics.csv file to the public directory
cp /Users/robertwohl/IdeaProjects/IntegrationTesting/build/reports/metrics/metrics.csv /Users/robertwohl/IdeaProjects/IntegrationTesting/src/main/webapp/dashboard/public/metrics.csv

echo "Metrics file copied successfully!"