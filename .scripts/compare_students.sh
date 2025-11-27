#!/bin/bash
# Compare student entity responses between new-hemis and old-hemis
# Usage: ./.scripts/compare_students.sh

NEW_STUDENT_ID="003528c8-0936-ffce-8f84-858cab6b70ef"
OLD_STUDENT_ID="6ae56e25-26f6-e7df-ac7e-0fe94e9520fb"

# Get tokens
echo "=== Getting tokens ==="
NEW_TOKEN=$(curl -s -X POST "http://localhost:8081/app/rest/v2/oauth/token" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=otm401&password=XCZDAb7qvGTXxz" | jq -r '.access_token')

OLD_TOKEN=$(curl -s -X POST "http://localhost:8082/app/rest/v2/oauth/token" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=otm351&password=XCZDAb7qvGTXxz" | jq -r '.access_token')

echo "New token: ${NEW_TOKEN:0:30}..."
echo "Old token: ${OLD_TOKEN:0:30}..."
echo ""

# Get new hemis student
echo "=== NEW-HEMIS: GET /entities/hemishe_EStudent/$NEW_STUDENT_ID ==="
curl -s "http://localhost:8081/app/rest/v2/entities/hemishe_EStudent/$NEW_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $NEW_TOKEN" > /tmp/new_student_entity.json

echo "Response saved to /tmp/new_student_entity.json"
echo "Field count:" $(cat /tmp/new_student_entity.json | jq 'keys | length')
echo ""

# Get old hemis student
echo "=== OLD-HEMIS: GET /entities/hemishe_EStudent/$OLD_STUDENT_ID ==="
curl -s "http://localhost:8082/app/rest/v2/entities/hemishe_EStudent/$OLD_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $OLD_TOKEN" > /tmp/old_student_entity.json

echo "Response saved to /tmp/old_student_entity.json"
echo "Field count:" $(cat /tmp/old_student_entity.json | jq 'keys | length')
echo ""

# Compare fields
echo "=== FIELD COMPARISON ==="
echo ""
echo "NEW-HEMIS fields:"
cat /tmp/new_student_entity.json | jq -r 'keys | sort | .[]' > /tmp/new_fields.txt
cat /tmp/new_fields.txt
echo ""

echo "OLD-HEMIS fields:"
cat /tmp/old_student_entity.json | jq -r 'keys | sort | .[]' > /tmp/old_fields.txt
cat /tmp/old_fields.txt
echo ""

echo "=== ONLY in NEW-HEMIS (missing in OLD) ==="
comm -23 /tmp/new_fields.txt /tmp/old_fields.txt
echo ""

echo "=== ONLY in OLD-HEMIS (missing in NEW) ==="
comm -13 /tmp/new_fields.txt /tmp/old_fields.txt
echo ""

echo "=== Sample values comparison ==="
echo "NEW-HEMIS sample:"
cat /tmp/new_student_entity.json | jq '{
  id,
  firstname,
  lastname,
  statusOrderName,
  statusOrderDate,
  statusOrderNumber,
  statusOrderCategory,
  roommateCount
}'

echo ""
echo "OLD-HEMIS sample:"
cat /tmp/old_student_entity.json | jq '{
  id,
  firstname,
  lastname,
  statusOrderName,
  statusOrderDate,
  statusOrderNumber,
  statusOrderCategory,
  roommateCount
}'
