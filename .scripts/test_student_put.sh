#!/bin/bash
# Test PUT /entities/hemishe_EStudent/{id} on both systems (new-hemis vs old-hemis)
# Usage: ./.scripts/test_student_put.sh

# Student IDs from config
NEW_STUDENT_ID="003528c8-0936-ffce-8f84-858cab6b70ef"
OLD_STUDENT_ID="6ae56e25-26f6-e7df-ac7e-0fe94e9520fb"

# Generate random test data
RANDOM_PHONE="+99890$(shuf -i 1000000-9999999 -n 1)"
RANDOM_EMAIL="student$(shuf -i 1000-9999 -n 1)@hemis.uz"
RANDOM_ADDRESS="Test manzil $(shuf -i 1-100 -n 1)"

echo "=== Random test ma'lumotlari ==="
echo "Phone: $RANDOM_PHONE"
echo "Email: $RANDOM_EMAIL"
echo "Address: $RANDOM_ADDRESS"
echo ""

# Get fresh tokens
echo "=== Tokenlarni olish ==="
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

# Test NEW-HEMIS PUT
echo "=== NEW-HEMIS: PUT /entities/hemishe_EStudent/$NEW_STUDENT_ID ==="
curl -s -X PUT "http://localhost:8081/app/rest/v2/entities/hemishe_EStudent/$NEW_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $NEW_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$RANDOM_PHONE\", \"email\": \"$RANDOM_EMAIL\", \"address\": \"$RANDOM_ADDRESS\"}" > /tmp/new_put_response.json

echo "Response:"
python3 -m json.tool /tmp/new_put_response.json 2>/dev/null || cat /tmp/new_put_response.json
echo ""

# Verify changes - GET after PUT
echo "=== NEW-HEMIS: GET after PUT (verify) ==="
curl -s "http://localhost:8081/app/rest/v2/entities/hemishe_EStudent/$NEW_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $NEW_TOKEN" > /tmp/new_verify.json

echo "Yangilangan ma'lumotlar:"
cat /tmp/new_verify.json | jq '{phone, email, address}' 2>/dev/null || cat /tmp/new_verify.json
echo ""

# Test OLD-HEMIS PUT (for comparison)
echo "=== OLD-HEMIS: PUT /entities/hemishe_EStudent/$OLD_STUDENT_ID ==="
curl -s -X PUT "http://localhost:8082/app/rest/v2/entities/hemishe_EStudent/$OLD_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$RANDOM_PHONE\", \"email\": \"$RANDOM_EMAIL\", \"address\": \"$RANDOM_ADDRESS\"}" > /tmp/old_put_response.json

echo "Response:"
python3 -m json.tool /tmp/old_put_response.json 2>/dev/null || cat /tmp/old_put_response.json
echo ""

# Compare response formats
echo "=== Response Format Comparison ==="
echo "NEW-HEMIS field count: $(cat /tmp/new_put_response.json | jq 'keys | length' 2>/dev/null || echo '0')"
echo "OLD-HEMIS field count: $(cat /tmp/old_put_response.json | jq 'keys | length' 2>/dev/null || echo '0')"
echo ""

echo "=== Test tugadi ==="
