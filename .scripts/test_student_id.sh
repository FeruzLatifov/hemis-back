#!/bin/bash
# Test student/id endpoint on both systems (new-hemis vs old-hemis)
# Usage: ./.scripts/test_student_id.sh
#
# IMPORTANT: Both systems must use SAME user to compare correctly!
# User determines university, so different users = different universities = different results

# Configuration - use same user for both!
USERNAME="${TEST_USER:-otm351}"
PASSWORD="${TEST_PASS:-XCZDAb7qvGTXxz}"

# Get fresh tokens
echo "=== Getting tokens (user: $USERNAME) ==="
TOKEN=$(curl -s -X POST "http://localhost:8081/app/rest/v2/oauth/token" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$USERNAME&password=$PASSWORD" | jq -r '.access_token')

OLD_TOKEN=$(curl -s -X POST "http://localhost:8082/app/rest/v2/oauth/token" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$USERNAME&password=$PASSWORD" | jq -r '.access_token')

echo "New token: ${TOKEN:0:30}..."
echo "Old token: ${OLD_TOKEN:0:30}..."
echo ""

DATA='{"data":{"citizenship":"11","pinfl":"31507976020031","serial":"AA6970877","year":"2024","education_type":"11","education_form":"11"}}'

echo "=== NEW-HEMIS: POST /app/rest/v2/services/student/id ==="
curl -s -X POST "http://localhost:8081/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$DATA" > /tmp/new_student_id.json

python3 -m json.tool /tmp/new_student_id.json 2>/dev/null || cat /tmp/new_student_id.json
echo ""

echo "=== OLD-HEMIS: POST /app/rest/v2/services/student/id ==="
curl -s -X POST "http://localhost:8082/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$DATA" > /tmp/old_student_id.json

python3 -m json.tool /tmp/old_student_id.json 2>/dev/null || cat /tmp/old_student_id.json
