#!/bin/bash

# Push Docker image to AWS ECR
set -e

# Configuration (matches your deploy-complete.sh)
AWS_REGION="ap-southeast-2"
AWS_ACCOUNT_ID="493800112837"
ECR_REPOSITORY="discount-repo"
IMAGE_TAG="latest"
ECR_IMAGE_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}:${IMAGE_TAG}"

echo "════════════════════════════════════════════════════"
echo "🐳 Building and Pushing to AWS ECR"
echo "════════════════════════════════════════════════════"
echo "Region: ${AWS_REGION}"
echo "Repository: ${ECR_REPOSITORY}"
echo "Image URI: ${ECR_IMAGE_URI}"
echo "════════════════════════════════════════════════════"
echo ""

# Step 1: Run tests
echo "🧪 Step 1: Running tests..."
./gradlew clean test

if [ $? -eq 0 ]; then
  echo "✅ All tests passed!"
else
  echo "❌ Tests failed! Fix issues before deploying."
  exit 1
fi

echo ""

# Step 2: Build the application
echo "🔨 Step 2: Building application..."
./gradlew build -x test

if [ $? -eq 0 ]; then
  echo "✅ Application built successfully!"
else
  echo "❌ Build failed!"
  exit 1
fi

echo ""

# Step 3: Build Docker image for linux/amd64 (AWS Fargate platform)
echo "🐳 Step 3: Building Docker image for linux/amd64..."
docker build --platform linux/amd64 -t ${ECR_REPOSITORY}:${IMAGE_TAG} .

if [ $? -eq 0 ]; then
  echo "✅ Docker image built successfully!"
else
  echo "❌ Docker build failed!"
  exit 1
fi

echo ""

# Step 4: Authenticate Docker to AWS ECR
echo "🔐 Step 4: Authenticating to AWS ECR..."
aws ecr get-login-password --region ${AWS_REGION} | \
  docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

if [ $? -eq 0 ]; then
  echo "✅ Successfully authenticated to ECR!"
else
  echo "❌ ECR authentication failed!"
  echo "Make sure you have AWS credentials configured:"
  echo "  aws configure"
  exit 1
fi

echo ""

# Step 5: Tag image for ECR
echo "🏷️  Step 5: Tagging image for ECR..."
docker tag ${ECR_REPOSITORY}:${IMAGE_TAG} ${ECR_IMAGE_URI}
echo "✅ Image tagged as: ${ECR_IMAGE_URI}"

echo ""

# Step 6: Push to ECR
echo "📤 Step 6: Pushing image to ECR..."
echo "   This may take a few minutes..."
docker push ${ECR_IMAGE_URI}

if [ $? -eq 0 ]; then
  echo "✅ Image pushed successfully!"
else
  echo "❌ Push to ECR failed!"
  exit 1
fi

echo ""

# Step 7: Verify image in ECR
echo "🔍 Step 7: Verifying image in ECR..."
IMAGE_DETAILS=$(aws ecr describe-images \
  --repository-name ${ECR_REPOSITORY} \
  --image-ids imageTag=${IMAGE_TAG} \
  --region ${AWS_REGION} \
  --query 'imageDetails[0].[imagePushedAt,imageSizeInBytes]' \
  --output text 2>/dev/null)

if [ $? -eq 0 ]; then
  echo "✅ Image verified in ECR!"
  echo "   Pushed at: $(echo ${IMAGE_DETAILS} | awk '{print $1}')"

  # Calculate image size in MB
  SIZE_BYTES=$(echo ${IMAGE_DETAILS} | awk '{print $2}')
  SIZE_MB=$(echo "scale=2; ${SIZE_BYTES}/1024/1024" | bc)
  echo "   Size: ${SIZE_MB} MB"
else
  echo "⚠️  Could not verify image (this might be okay)"
fi

echo ""
echo "════════════════════════════════════════════════════"
echo "🎉 Push Complete!"
echo "════════════════════════════════════════════════════"
echo ""
echo "Image URI: ${ECR_IMAGE_URI}"
echo ""
echo "Next steps:"
echo "  1. Deploy to ECS:"
echo "     ./deploy-complete.sh"
echo ""
echo "  2. Or force update existing service:"
echo "     aws ecs update-service \\"
echo "       --cluster <cluster-name> \\"
echo "       --service <service-name> \\"
echo "       --force-new-deployment \\"
echo "       --region ${AWS_REGION}"
echo ""
echo "════════════════════════════════════════════════════"