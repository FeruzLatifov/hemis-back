#!/bin/bash
# Get active student IDs for both systems
# Usage: bash ./.scripts/get_active_student_ids.sh

# Configuration
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"
NEW_USERNAME="otm401"
OLD_USERNAME="otm351"
NEW_PASSWORD="XCZDAb7qvGTXxz"
OLD_PASSWORD="XCZDAb7qvGTXxz"
CLIENT_ID="client"
CLIENT_SECRET="secret"

echo "=== Getting active student IDs ==="
echo ""

# Get NEW-HEMIS token and find active student
echo "🔍 NEW-HEMIS (otm401):"
NEW_TOKEN=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$NEW_USERNAME&password=$NEW_PASSWORD" | jq -r '.access_token')

if [ ! -z "$NEW_TOKEN" ]; then
    NEW_STUDENTS=$(curl -s "$NEW_BASE/app/rest/v2/entities/hemishe_EStudent?view=_local&limit=20&returnNulls=false" \
      -H "Authorization: Bearer $NEW_TOKEN")

    NEW_ACTIVE=$(echo "$NEW_STUDENTS" | jq -r '.[] | select(.active == true) | .id' | head -1)

    if [ ! -z "$NEW_ACTIVE" ]; then
        echo "  ✅ Active student ID: $NEW_ACTIVE"
        echo "  newStudentId=\"$NEW_ACTIVE\""
    else
        echo "  ⚠️ Active talaba topilmadi"
    fi
else
    echo "  ❌ Token olinmadi"
fi

echo ""

# Get OLD-HEMIS token and find active student
echo "🔍 OLD-HEMIS (otm351):"
OLD_TOKEN=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$OLD_USERNAME&password=$OLD_PASSWORD" | jq -r '.access_token')

if [ ! -z "$OLD_TOKEN" ]; then
    OLD_STUDENTS=$(curl -s "$OLD_BASE/app/rest/v2/entities/hemishe_EStudent?view=_local&limit=20&returnNulls=false" \
      -H "Authorization: Bearer $OLD_TOKEN")

    OLD_ACTIVE=$(echo "$OLD_STUDENTS" | jq -r '.[] | select(.active == true) | .id' | head -1)

    if [ ! -z "$OLD_ACTIVE" ]; then
        echo "  ✅ Active student ID: $OLD_ACTIVE"
        echo "  oldStudentId=\"$OLD_ACTIVE\""
    else
        echo "  ⚠️ Active talaba topilmadi"
    fi
else
    echo "  ❌ Token olinmadi"
fi

echo ""
echo "=== endpoint_tester.html da qo'yish uchun ==="
echo "NEW: <input id=\"newStudentId\" value=\"$NEW_ACTIVE\">"
echo "OLD: <input id=\"oldStudentId\" value=\"$OLD_ACTIVE\">"
