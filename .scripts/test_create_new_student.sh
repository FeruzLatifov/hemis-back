#!/bin/bash
# Create new student and show generated ID
# Usage: bash ./.scripts/test_create_new_student.sh

# Configuration
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"
NEW_USERNAME="otm401"
OLD_USERNAME="otm351"
NEW_PASSWORD="XCZDAb7qvGTXxz"
OLD_PASSWORD="XCZDAb7qvGTXxz"
CLIENT_ID="client"
CLIENT_SECRET="secret"

echo "=== Yangi talaba yaratish (POST /services/student/id) ==="
echo ""

# Generate random test data
RANDOM_PINFL="$(shuf -i 10000000000000-99999999999999 -n 1)"
RANDOM_SERIAL="AA$(shuf -i 1000000-9999999 -n 1)"

echo "📝 Test ma'lumotlari:"
echo "  PINFL: $RANDOM_PINFL"
echo "  Serial: $RANDOM_SERIAL"
echo "  Citizenship: 11 (O'zbekiston)"
echo "  Gender: 12 (Ayol)"
echo "  Education Type: 11 (Bakalavr)"
echo "  Education Form: 11 (Kunduzgi)"
echo "  Year: 2024"
echo ""

# Get NEW-HEMIS token
echo "🔑 NEW-HEMIS token olish..."
NEW_TOKEN=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$NEW_USERNAME&password=$NEW_PASSWORD" | jq -r '.access_token')

if [ -z "$NEW_TOKEN" ] || [ "$NEW_TOKEN" == "null" ]; then
    echo "❌ NEW-HEMIS token olinmadi!"
    exit 1
fi

echo "✅ Token olindi: ${NEW_TOKEN:0:30}..."
echo ""

# Create student in NEW-HEMIS
echo "🆕 NEW-HEMIS: Yangi talaba yaratilmoqda..."
NEW_DATA=$(cat <<EOF
{
  "data": {
    "citizenship": "11",
    "pinfl": "$RANDOM_PINFL",
    "serial": "$RANDOM_SERIAL",
    "year": "2024",
    "education_type": "11",
    "education_form": "11",
    "gender": "12"
  }
}
EOF
)

NEW_RESPONSE=$(curl -s -X POST "$NEW_BASE/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $NEW_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$NEW_DATA")

echo "$NEW_RESPONSE" > /tmp/new_created_student.json

echo "Response:"
echo "$NEW_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$NEW_RESPONSE"
echo ""

# Extract student ID
NEW_STUDENT_ID=$(echo "$NEW_RESPONSE" | jq -r '.data.id // empty')
NEW_SUCCESS=$(echo "$NEW_RESPONSE" | jq -r '.success // false')

if [ "$NEW_SUCCESS" == "true" ] && [ ! -z "$NEW_STUDENT_ID" ]; then
    echo "✅ ✅ ✅ NEW-HEMIS: Talaba muvaffaqiyatli yaratildi!"
    echo "   📌 Student ID: $NEW_STUDENT_ID"
    echo "   📝 PINFL: $RANDOM_PINFL"
    echo "   📄 Serial: $RANDOM_SERIAL"
    echo "   📁 Code: $(echo "$NEW_RESPONSE" | jq -r '.data.code // "N/A"')"
else
    echo "⚠️ NEW-HEMIS: Talaba yaratilmadi"
    NEW_MESSAGE=$(echo "$NEW_RESPONSE" | jq -r '.message // "Unknown error"')
    echo "   Xabar: $NEW_MESSAGE"
fi

echo ""
echo "========================================"
echo ""

# Get OLD-HEMIS token
echo "🔑 OLD-HEMIS token olish..."
OLD_TOKEN=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$OLD_USERNAME&password=$OLD_PASSWORD" | jq -r '.access_token')

if [ -z "$OLD_TOKEN" ] || [ "$OLD_TOKEN" == "null" ]; then
    echo "❌ OLD-HEMIS token olinmadi!"
    exit 1
fi

echo "✅ Token olindi: ${OLD_TOKEN:0:30}..."
echo ""

# Create student in OLD-HEMIS (with same data)
echo "🏛️  OLD-HEMIS: Yangi talaba yaratilmoqda (bir xil ma'lumotlar)..."
OLD_DATA=$(cat <<EOF
{
  "data": {
    "citizenship": "11",
    "pinfl": "$RANDOM_PINFL",
    "serial": "$RANDOM_SERIAL",
    "year": "2024",
    "education_type": "11",
    "education_form": "11",
    "gender": "12"
  }
}
EOF
)

OLD_RESPONSE=$(curl -s -X POST "$OLD_BASE/app/rest/v2/services/student/id" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$OLD_DATA")

echo "$OLD_RESPONSE" > /tmp/old_created_student.json

echo "Response:"
echo "$OLD_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$OLD_RESPONSE"
echo ""

# Extract student ID
OLD_STUDENT_ID=$(echo "$OLD_RESPONSE" | jq -r '.data.id // .student.id // empty')
OLD_SUCCESS=$(echo "$OLD_RESPONSE" | jq -r '.success // false')

if [ "$OLD_SUCCESS" == "true" ] && [ ! -z "$OLD_STUDENT_ID" ]; then
    echo "✅ ✅ ✅ OLD-HEMIS: Talaba muvaffaqiyatli yaratildi!"
    echo "   📌 Student ID: $OLD_STUDENT_ID"
    echo "   📝 PINFL: $RANDOM_PINFL"
    echo "   📄 Serial: $RANDOM_SERIAL"
    echo "   📁 Code: $(echo "$OLD_RESPONSE" | jq -r '.data.code // .student.code // "N/A"')"
else
    echo "⚠️ OLD-HEMIS: Talaba yaratilmadi"
    OLD_MESSAGE=$(echo "$OLD_RESPONSE" | jq -r '.message // "Unknown error"')
    echo "   Xabar: $OLD_MESSAGE"

    # Check if student already exists
    if [[ "$OLD_MESSAGE" == *"active"* ]]; then
        echo ""
        echo "💡 Bu PINFL bilan talaba allaqachon mavjud"
        OLD_STUDENT_ID=$(echo "$OLD_RESPONSE" | jq -r '.student.id // empty')
        if [ ! -z "$OLD_STUDENT_ID" ]; then
            echo "   Mavjud talaba ID: $OLD_STUDENT_ID"
        fi
    fi
fi

echo ""
echo "========================================"
echo "📊 XULOSA"
echo "========================================"
echo ""

if [ "$NEW_SUCCESS" == "true" ]; then
    echo "🆕 NEW-HEMIS: ✅ Yaratildi - ID: $NEW_STUDENT_ID"
else
    echo "🆕 NEW-HEMIS: ❌ Yaratilmadi"
fi

if [ "$OLD_SUCCESS" == "true" ]; then
    echo "🏛️  OLD-HEMIS: ✅ Yaratildi - ID: $OLD_STUDENT_ID"
else
    echo "🏛️  OLD-HEMIS: ❌ Yaratilmadi"
fi

echo ""
echo "📁 Saqlangan fayllar:"
echo "  - NEW: /tmp/new_created_student.json"
echo "  - OLD: /tmp/old_created_student.json"
echo ""
echo "✨ Test tugadi!"
