#!/bin/bash
# test_student_put_random.sh - PUT /entities/hemishe_EStudent/{id} test with random data
# Usage: ./test_student_put_random.sh

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"
OLD_USERNAME="otm351"
OLD_PASSWORD="XCZDAb7qvGTXxz"
NEW_USERNAME="otm401"
NEW_PASSWORD="XCZDAb7qvGTXxz"
CLIENT_ID="client"
CLIENT_SECRET="secret"

# Student IDs from config
NEW_STUDENT_ID="003528c8-0936-ffce-8f84-858cab6b70ef"
OLD_STUDENT_ID="6ae56e25-26f6-e7df-ac7e-0fe94e9520fb"

# Output files
OLD_RESPONSE="/tmp/old_put_response.json"
NEW_RESPONSE="/tmp/new_put_response.json"
OLD_TOKEN_FILE="/tmp/old_token.txt"
NEW_TOKEN_FILE="/tmp/new_token.txt"
NEW_VERIFY="/tmp/new_verify.json"
OLD_VERIFY="/tmp/old_verify.json"

# Function to print colored output
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Generate random test data
RANDOM_PHONE="+99890$(shuf -i 1000000-9999999 -n 1)"
RANDOM_EMAIL="student$(shuf -i 1000-9999 -n 1)@hemis.uz"
RANDOM_ADDRESS="Test manzil $(shuf -i 1-100 -n 1)"

# Print configuration
print_header "PUT ENDPOINT TEST - RANDOM DATA"
echo "New-Hemis: $NEW_BASE"
echo "Old-Hemis: $OLD_BASE"
echo "New Student ID: $NEW_STUDENT_ID"
echo "Old Student ID: $OLD_STUDENT_ID"
echo ""
print_info "Random test ma'lumotlari:"
echo "  Phone:   $RANDOM_PHONE"
echo "  Email:   $RANDOM_EMAIL"
echo "  Address: $RANDOM_ADDRESS"
echo ""

# Step 1: Get token from old-hemis
print_info "1️⃣ Getting token from Old-Hemis (otm351)..."
OLD_TOKEN_RESPONSE=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$OLD_USERNAME&password=$OLD_PASSWORD")

OLD_TOKEN=$(echo "$OLD_TOKEN_RESPONSE" | jq -r '.access_token')

if [ "$OLD_TOKEN" == "null" ] || [ -z "$OLD_TOKEN" ]; then
    print_error "Old-Hemis token olishda xatolik!"
    echo "$OLD_TOKEN_RESPONSE" | jq '.'
    exit 1
fi

echo "$OLD_TOKEN" > "$OLD_TOKEN_FILE"
print_success "Old-Hemis token olindi: ${OLD_TOKEN:0:30}..."

# Step 2: Get token from new-hemis
print_info "2️⃣ Getting token from New-Hemis (otm401)..."
NEW_TOKEN_RESPONSE=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&username=$NEW_USERNAME&password=$NEW_PASSWORD")

NEW_TOKEN=$(echo "$NEW_TOKEN_RESPONSE" | jq -r '.access_token')

if [ "$NEW_TOKEN" == "null" ] || [ -z "$NEW_TOKEN" ]; then
    print_error "New-Hemis token olishda xatolik!"
    echo "$NEW_TOKEN_RESPONSE" | jq '.'
    exit 1
fi

echo "$NEW_TOKEN" > "$NEW_TOKEN_FILE"
print_success "New-Hemis token olindi: ${NEW_TOKEN:0:30}..."

echo ""

# Step 3: Test new-hemis PUT
print_header "🆕 NEW-HEMIS: PUT /entities/hemishe_EStudent/$NEW_STUDENT_ID"
HTTP_CODE=$(curl -s -o "$NEW_RESPONSE" -w "%{http_code}" -X PUT "$NEW_BASE/app/rest/v2/entities/hemishe_EStudent/$NEW_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $NEW_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$RANDOM_PHONE\", \"email\": \"$RANDOM_EMAIL\", \"address\": \"$RANDOM_ADDRESS\"}")

if [ "$HTTP_CODE" == "200" ]; then
    print_success "HTTP $HTTP_CODE - OK"
    cat "$NEW_RESPONSE" | jq '.'
else
    print_error "HTTP $HTTP_CODE - ERROR"
    cat "$NEW_RESPONSE" | jq '.' 2>/dev/null || cat "$NEW_RESPONSE"
fi

echo ""

# Step 4: Verify changes - GET after PUT
print_header "🔍 NEW-HEMIS: GET (verify changes)"
curl -s "$NEW_BASE/app/rest/v2/entities/hemishe_EStudent/$NEW_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $NEW_TOKEN" > "$NEW_VERIFY"

print_info "Yangilangan ma'lumotlar:"
cat "$NEW_VERIFY" | jq '{phone, email, address}'

echo ""

# Step 5: Test old-hemis PUT (for comparison)
print_header "🏛️  OLD-HEMIS: PUT /entities/hemishe_EStudent/$OLD_STUDENT_ID"
HTTP_CODE=$(curl -s -o "$OLD_RESPONSE" -w "%{http_code}" -X PUT "$OLD_BASE/app/rest/v2/entities/hemishe_EStudent/$OLD_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $OLD_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"phone\": \"$RANDOM_PHONE\", \"email\": \"$RANDOM_EMAIL\", \"address\": \"$RANDOM_ADDRESS\"}")

if [ "$HTTP_CODE" == "200" ]; then
    print_success "HTTP $HTTP_CODE - OK"
    cat "$OLD_RESPONSE" | jq '.'
else
    print_error "HTTP $HTTP_CODE - ERROR"
    cat "$OLD_RESPONSE" | jq '.' 2>/dev/null || cat "$OLD_RESPONSE"
fi

echo ""

# Step 6: Verify old-hemis changes
print_header "🔍 OLD-HEMIS: GET (verify changes)"
curl -s "$OLD_BASE/app/rest/v2/entities/hemishe_EStudent/$OLD_STUDENT_ID?returnNulls=false" \
  -H "Authorization: Bearer $OLD_TOKEN" > "$OLD_VERIFY"

print_info "Yangilangan ma'lumotlar:"
cat "$OLD_VERIFY" | jq '{phone, email, address}'

echo ""

# Step 7: Compare response formats
print_header "📊 COMPARISON RESULT"

# Check if both responses are valid JSON
if ! jq empty "$NEW_RESPONSE" 2>/dev/null; then
    print_error "New-Hemis response is not valid JSON!"
    exit 1
fi

if ! jq empty "$OLD_RESPONSE" 2>/dev/null; then
    print_error "Old-Hemis response is not valid JSON!"
    exit 1
fi

# Count fields
NEW_FIELD_COUNT=$(cat "$NEW_RESPONSE" | jq 'keys | length')
OLD_FIELD_COUNT=$(cat "$OLD_RESPONSE" | jq 'keys | length')

print_info "NEW-HEMIS field count: $NEW_FIELD_COUNT"
print_info "OLD-HEMIS field count: $OLD_FIELD_COUNT"

# Check if _entityName exists (CUBA format check)
NEW_ENTITY_NAME=$(cat "$NEW_RESPONSE" | jq -r '._entityName // "missing"')
OLD_ENTITY_NAME=$(cat "$OLD_RESPONSE" | jq -r '._entityName // "missing"')

if [ "$NEW_ENTITY_NAME" == "hemishe_EStudent" ] && [ "$OLD_ENTITY_NAME" == "hemishe_EStudent" ]; then
    print_success "✅ Response format CUBA compatible (_entityName present)"
else
    print_warning "⚠️  _entityName field check failed"
fi

echo ""
print_success "✅ PUT endpoint test completed!"
print_info "Test ma'lumotlari:"
echo "  Phone:   $RANDOM_PHONE"
echo "  Email:   $RANDOM_EMAIL"
echo "  Address: $RANDOM_ADDRESS"
echo ""
print_info "Saved responses:"
echo "  - New PUT: $NEW_RESPONSE"
echo "  - Old PUT: $OLD_RESPONSE"
echo "  - New Verify: $NEW_VERIFY"
echo "  - Old Verify: $OLD_VERIFY"
