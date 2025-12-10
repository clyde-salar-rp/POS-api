#!/bin/bash

# Complete deployment script for ECS
set -e

AWS_REGION="ap-southeast-2"
ECR_IMAGE_URI="493800112837.dkr.ecr.ap-southeast-2.amazonaws.com/discount-repo:latest"
STACK_NAME="discount-api-stack"
ENVIRONMENT="production"

echo "════════════════════════════════════════════════════"
echo "🚀 Complete ECS Deployment"
echo "════════════════════════════════════════════════════"
echo "Region: ${AWS_REGION}"
echo "Image: ${ECR_IMAGE_URI}"
echo "Stack: ${STACK_NAME}"
echo "Environment: ${ENVIRONMENT}"
echo "════════════════════════════════════════════════════"
echo ""

# Step 1: Verify ECR image exists
echo "📦 Step 1: Verifying ECR image..."
if aws ecr describe-images \
  --repository-name discount-repo \
  --image-ids imageTag=latest \
  --region ${AWS_REGION} >/dev/null 2>&1; then
  echo "✅ ECR image found"

  # Show when image was pushed
  IMAGE_PUSHED=$(aws ecr describe-images \
    --repository-name discount-repo \
    --image-ids imageTag=latest \
    --region ${AWS_REGION} \
    --query 'imageDetails[0].imagePushedAt' \
    --output text)
  echo "   Pushed at: ${IMAGE_PUSHED}"
else
  echo "❌ ECR image not found. Please push your image first:"
  echo "   ./push-to-ecr.sh"
  exit 1
fi

echo ""

# Step 2: Validate CloudFormation template
echo "🔍 Step 2: Validating CloudFormation template..."
if aws cloudformation validate-template \
  --template-body file://ecs-deployment.yaml \
  --region ${AWS_REGION} >/dev/null 2>&1; then
  echo "✅ Template is valid"
else
  echo "❌ Template validation failed"
  exit 1
fi

echo ""

# Step 3: Deploy CloudFormation stack
echo "☁️  Step 3: Deploying CloudFormation stack..."
echo "   This will take 5-10 minutes..."
echo ""

aws cloudformation deploy \
  --template-file ecs-deployment.yaml \
  --stack-name ${STACK_NAME} \
  --parameter-overrides \
      ImageUri=${ECR_IMAGE_URI} \
      Environment=${ENVIRONMENT} \
  --capabilities CAPABILITY_NAMED_IAM \
  --region ${AWS_REGION}

if [ $? -eq 0 ]; then
  echo ""
  echo "✅ Stack deployed successfully!"
else
  echo ""
  echo "❌ Stack deployment failed"
  echo "Check the CloudFormation console for details:"
  echo "https://ap-southeast-2.console.aws.amazon.com/cloudformation/home?region=ap-southeast-2#/stacks"
  exit 1
fi

echo ""

# Step 4: Get stack outputs
echo "📋 Step 4: Getting deployment information..."
echo ""

LOAD_BALANCER_DNS=$(aws cloudformation describe-stacks \
  --stack-name ${STACK_NAME} \
  --region ${AWS_REGION} \
  --query 'Stacks[0].Outputs[?OutputKey==`LoadBalancerDNS`].OutputValue' \
  --output text)

CLUSTER_NAME=$(aws cloudformation describe-stacks \
  --stack-name ${STACK_NAME} \
  --region ${AWS_REGION} \
  --query 'Stacks[0].Outputs[?OutputKey==`ClusterName`].OutputValue' \
  --output text)

SERVICE_NAME=$(aws cloudformation describe-stacks \
  --stack-name ${STACK_NAME} \
  --region ${AWS_REGION} \
  --query 'Stacks[0].Outputs[?OutputKey==`ServiceName`].OutputValue' \
  --output text)

LOG_GROUP=$(aws cloudformation describe-stacks \
  --stack-name ${STACK_NAME} \
  --region ${AWS_REGION} \
  --query 'Stacks[0].Outputs[?OutputKey==`LogGroupName`].OutputValue' \
  --output text)

echo "════════════════════════════════════════════════════"
echo "🎉 Deployment Complete!"
echo "════════════════════════════════════════════════════"
echo ""
echo "📍 Your API Endpoints:"
echo "   Health Check: http://${LOAD_BALANCER_DNS}/api/hello"
echo "   Discount API: http://${LOAD_BALANCER_DNS}/discount"
echo ""
echo "🔧 AWS Resources:"
echo "   Cluster: ${CLUSTER_NAME}"
echo "   Service: ${SERVICE_NAME}"
echo "   Log Group: ${LOG_GROUP}"
echo ""
echo "════════════════════════════════════════════════════"
echo ""

# ✨ NEW STEP: Force ECS to pull new image
echo "🔄 Step 5: Forcing ECS to pull new image..."
echo "   This ensures the latest code is deployed..."
echo ""

aws ecs update-service \
  --cluster ${CLUSTER_NAME} \
  --service ${SERVICE_NAME} \
  --force-new-deployment \
  --region ${AWS_REGION} \
  --no-cli-pager

echo "✅ Force deployment initiated!"
echo ""

# Step 6: Wait for service to be stable
echo "⏳ Step 6: Waiting for ECS service to be healthy..."
echo "   This may take 3-5 minutes..."
echo ""

aws ecs wait services-stable \
  --cluster ${CLUSTER_NAME} \
  --services ${SERVICE_NAME} \
  --region ${AWS_REGION}

if [ $? -eq 0 ]; then
  echo "✅ Service is stable and healthy!"
else
  echo "⚠️  Service may not be fully healthy yet. Check the status below."
fi

echo ""

# Step 7: Test the endpoint
echo "🧪 Step 7: Testing the API endpoint..."
echo ""

MAX_ATTEMPTS=10
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
  echo "Attempt ${ATTEMPT}/${MAX_ATTEMPTS}..."

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://${LOAD_BALANCER_DNS}/api/hello --connect-timeout 10 2>/dev/null)

  if [ "$HTTP_CODE" = "200" ]; then
    echo ""
    echo "✅ API is responding! (HTTP ${HTTP_CODE})"
    echo ""
    echo "Response:"
    curl -s http://${LOAD_BALANCER_DNS}/api/hello | jq . 2>/dev/null || curl -s http://${LOAD_BALANCER_DNS}/api/hello
    echo ""
    break
  else
    echo "   Status: HTTP ${HTTP_CODE} (waiting...)"
    if [ $ATTEMPT -lt $MAX_ATTEMPTS ]; then
      sleep 30
    fi
  fi

  ATTEMPT=$((ATTEMPT + 1))
done

if [ "$HTTP_CODE" != "200" ]; then
  echo ""
  echo "⚠️  API is not responding yet. This is normal during initial deployment."
  echo "   Please wait a few more minutes and try:"
  echo "   curl http://${LOAD_BALANCER_DNS}/api/hello"
fi

echo ""

# Step 8: Test discount endpoint with NEW rules
echo "🧪 Step 8: Testing discount rules..."
echo ""

DISCOUNT_RESPONSE=$(curl -s -X POST http://${LOAD_BALANCER_DNS}/discount \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "upc": "999999937551",
        "description": "Medium Polar Pop",
        "price": 0.89,
        "quantity": 3,
        "category": "BEVERAGE"
      }
    ]
  }')

echo "Response:"
echo "${DISCOUNT_RESPONSE}" | jq . 2>/dev/null || echo "${DISCOUNT_RESPONSE}"
echo ""

# Check if new rules are active
if echo "${DISCOUNT_RESPONSE}" | grep -q "BUY_2_GET_1"; then
  echo "✅ NEW DISCOUNT RULES ARE ACTIVE!"
else
  echo "⚠️  Old discount rules still active or no discount applied"
  echo "   This might take a few more minutes to update"
fi

echo ""
echo "════════════════════════════════════════════════════"
echo "📚 Useful Commands:"
echo "════════════════════════════════════════════════════"
echo ""
echo "Test your API:"
echo "  curl http://${LOAD_BALANCER_DNS}/api/hello"
echo ""
echo "Test discount endpoint:"
echo "  curl -X POST http://${LOAD_BALANCER_DNS}/discount \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"items\":[{\"upc\":\"999999937551\",\"description\":\"Medium Polar Pop\",\"price\":0.89,\"quantity\":3,\"category\":\"BEVERAGE\"}]}'"
echo ""
echo "View logs:"
echo "  aws logs tail ${LOG_GROUP} --follow --region ${AWS_REGION}"
echo ""
echo "Check service status:"
echo "  aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${SERVICE_NAME} --region ${AWS_REGION}"
echo ""
echo "List running tasks:"
echo "  aws ecs list-tasks --cluster ${CLUSTER_NAME} --service-name ${SERVICE_NAME} --region ${AWS_REGION}"
echo ""
echo "Force update (if needed):"
echo "  aws ecs update-service --cluster ${CLUSTER_NAME} --service ${SERVICE_NAME} --force-new-deployment --region ${AWS_REGION}"
echo ""
echo "AWS Console Links:"
echo "  ECS Cluster: https://ap-southeast-2.console.aws.amazon.com/ecs/home?region=ap-southeast-2#/clusters/${CLUSTER_NAME}/services"
echo "  Load Balancer: https://ap-southeast-2.console.aws.amazon.com/ec2/home?region=ap-southeast-2#LoadBalancers:"
echo "  CloudWatch Logs: https://ap-southeast-2.console.aws.amazon.com/cloudwatch/home?region=ap-southeast-2#logsV2:log-groups/log-group/${LOG_GROUP}"
echo ""
echo "════════════════════════════════════════════════════"
echo ""
echo "💡 Tips:"
echo "  - Your API is now running on AWS ECS with Fargate"
echo "  - It has 2 tasks for high availability"
echo "  - The load balancer distributes traffic automatically"
echo "  - Logs are stored in CloudWatch for 7 days"
echo "  - Health checks run every 30 seconds"
echo ""
echo "To update your deployment:"
echo "  1. Build and push new image: ./push-to-ecr.sh"
echo "  2. Redeploy: ./deploy-complete.sh (now with force deployment!)"
echo ""
echo "To delete everything:"
echo "  aws cloudformation delete-stack --stack-name ${STACK_NAME} --region ${AWS_REGION}"
echo ""
echo "════════════════════════════════════════════════════"