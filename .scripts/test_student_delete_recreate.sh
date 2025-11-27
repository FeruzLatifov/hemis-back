#!/bin/bash
# Test DELETE and recreate workflow
# Usage: ./.scripts/test_student_delete_recreate.sh

# Student IDs to delete and recreate
NEW_STUDENT_ID="0d738e89-a9a9-9b9c-22d7-19912228b063"
OLD_STUDENT_ID="486cb676-d84b-0572-6d09-a82d9838f75d"

# Configuration
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"
NEW_USERNAME="otm401"
OLD_USERNAME="otm351"
NEW_PASSWORD="XCZDAb7qvGTXxz"
OLD_PASSWORD="XCZDAb7qvGTXxz"
CLIENT_ID="client"
CLIENT_SECRET="secret"

# Get tokens
echo "=== Getting tokens ==="
NEW_TOKEN=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$NEW_USERNAME&password=$NEW_PASSWORD" | jq -r '.access_token')

OLD_TOKEN=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$OLD_USERNAME&password=$OLD_PASSWORD" | jq -r '.access_token')

echo "New token: ${NEW_TOKEN:0:30}..."
echo "Old token: ${OLD_TOKEN:0:30}..."
echo ""

# Step 1: Use known test data for recreation (same parameters for both systems)
echo "=== Step 1: Prepare test data for recreation ==="
# Use the same test data that worked in test_student_id.sh
PINFL="31507976020031"
SERIAL="AA6970877"
CITIZENSHIP="11"
GENDER="12"
EDU_TYPE="11"
EDU_FORM="11"
YEAR="2024"

echo "Test parameters: pinfl=$PINFL, serial=$SERIAL, citizenship=$CITIZENSHIP, gender=$GENDER, eduType=$EDU_TYPE, eduForm=$EDU_FORM"
echo ""

# Step 2: DELETE students
echo "=== Step 2: DELETE students ==="
echo "NEW-HEMIS: DELETE $NEW_STUDENT_ID"
curl -s -X DELETE "$NEW_BASE/app/rest/v2/entities/hemishe_EStudent/$NEW_STUDENT_ID" \
  -H "Authorization: Bearer $NEW_TOKEN" > /tmp/new_delete_response.json

cat /tmp/new_delete_response.json
echo ""

echo "OLD-HEMIS: DELETE $OLD_STUDENT_ID"
curl -s -X DELETE "$OLD_BASE/app/rest/v2/entities/hemishe_EStudent/$OLD_STUDENT_ID" \
  -H "Authorization: Bearer $OLD_TOKEN" > /tmp/old_delete_response.json

cat /tmp/old_delete_response.json
echo ""

# Step 3: Recreate students with POST /services/student/id
echo "=== Step 3: Recreate students (POST /services/student/id) ==="

echo "NEW-HEMIS: Creating student with test parameters"
NEW_DATA=$(cat <<EOF
{
  "data": {
    "citizenship": "$CITIZENSHIP",
    "pinfl": "$PINFL",
    "serial": "$SERIAL",
    "year": "$YEAR",
    "education_type": "$EDU_TYPE",
    "education_form": "$EDU_FORM",
    "gender": "$GENDER"
  }
}
EOF
)

curl -s -X POST "$NEW_BASE/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $NEW_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$NEW_DATA" > /tmp/new_recreated_student.json

echo "New student created:"
cat /tmp/new_recreated_student.json | jq '.'
echo ""

echo "OLD-HEMIS: Creating student with test parameters"
OLD_DATA=$(cat <<EOF
{
  "data": {
    "citizenship": "$CITIZENSHIP",
    "pinfl": "$PINFL",
    "serial": "$SERIAL",
    "year": "$YEAR",
    "education_type": "$EDU_TYPE",
    "education_form": "$EDU_FORM",
    "gender": "$GENDER"
  }
}
EOF
)

curl -s -X POST "$OLD_BASE/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$OLD_DATA" > /tmp/old_recreated_student.json

echo "Old student created:"
cat /tmp/old_recreated_student.json | jq '.'
echo ""

# Step 4: Compare results
echo "=== Step 4: Comparison ==="
echo "NEW-HEMIS:"
echo "  Old ID: $NEW_STUDENT_ID"
echo "  New ID: $(cat /tmp/new_recreated_student.json | jq -r '.data.id // "ERROR"')"
echo ""
echo "OLD-HEMIS:"
echo "  Old ID: $OLD_STUDENT_ID"
echo "  New ID: $(cat /tmp/old_recreated_student.json | jq -r '.data.id // "ERROR"')"
echo ""

echo "=== Test completed ==="
