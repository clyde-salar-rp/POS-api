#!/bin/bash

# Validate CloudFormation template and show detailed errors

echo "🔍 Validating CloudFormation template..."
echo ""

aws cloudformation validate-template \
  --template-body file://ecs-deployment.yaml \
  --region ap-southeast-2

if [ $? -ne 0 ]; then
  echo ""
  echo "❌ Validation failed. Common issues:"
  echo "  1. YAML syntax errors"
  echo "  2. Invalid resource references"
  echo "  3. Missing file: ecs-deployment.yaml"
  echo ""
  echo "Checking if file exists..."
  if [ -f "ecs-deployment.yaml" ]; then
    echo "✅ File exists"
    echo ""
    echo "Let's check YAML syntax..."
    python3 -c "import yaml; yaml.safe_load(open('ecs-deployment.yaml'))" 2>&1
  else
    echo "❌ File ecs-deployment.yaml not found!"
    echo "   Please make sure the file is in the current directory."
  fi
else
  echo ""
  echo "✅ Template is valid!"
fi