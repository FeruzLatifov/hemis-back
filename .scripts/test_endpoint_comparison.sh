#!/bin/bash
# test_endpoint_comparison.sh - Old-hemis vs Yangi hemis endpoint solishtirish
# Usage: ./test_endpoint_comparison.sh "/app/rest/v2/userInfo"

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration (default values from database)
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"
USERNAME="feruz"
PASSWORD="BvZzXW6oQxEEte"
CLIENT_ID="client"
CLIENT_SECRET="secret"

# Default test PINFL (from e_student.passport_pin in hemis_401)
DEFAULT_PINFL="61902025630068"

# Output files
OLD_RESPONSE="/tmp/old_response.json"
NEW_RESPONSE="/tmp/new_response.json"
OLD_TOKEN_FILE="/tmp/old_token.txt"
NEW_TOKEN_FILE="/tmp/new_token.txt"

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

# Check if endpoint is provided
if [ -z "$1" ]; then
    print_error "Endpoint URL kiritilmagan!"
    echo "Usage: $0 <endpoint_url>"
    echo "Example: $0 /app/rest/v2/userInfo"
    echo "Example: $0 \"/app/rest/v2/services/pass/data?pinfl=31503776560016\""
    exit 1
fi

ENDPOINT="$1"

# Print configuration
print_header "HEMIS API ENDPOINT COMPARISON TESTER"
echo "Endpoint: $ENDPOINT"
echo "Old-Hemis: $OLD_BASE"
echo "New-Hemis: $NEW_BASE"
echo "Username: $USERNAME"
echo ""

# Special handling for OAuth token endpoint
if [[ "$ENDPOINT" == *"/oauth/token"* ]]; then
    print_warning "⚠️  OAuth Token endpoint - ushbu endpoint tokenni qaytaradi, token talab qilmaydi"
    print_info "Test qilish: grant_type=password bilan POST request"
    echo ""

    # Test old-hemis OAuth token endpoint
    print_header "🏛️  OLD-HEMIS RESPONSE (port 8082)"
    OLD_TOKEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$OLD_BASE$ENDPOINT" \
      -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password&username=$USERNAME&password=$PASSWORD")

    OLD_HTTP_CODE=$(echo "$OLD_TOKEN_RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
    OLD_TOKEN_JSON=$(echo "$OLD_TOKEN_RESPONSE" | sed '/HTTP_CODE:/d')

    echo "$OLD_TOKEN_JSON" > "$OLD_RESPONSE"

    if [ "$OLD_HTTP_CODE" == "200" ]; then
        print_success "HTTP $OLD_HTTP_CODE - OK"
        echo "$OLD_TOKEN_JSON" | jq '.'
    else
        print_error "HTTP $OLD_HTTP_CODE - ERROR"
        echo "$OLD_TOKEN_JSON" | jq '.' 2>/dev/null || echo "$OLD_TOKEN_JSON"
    fi

    echo ""

    # Test new-hemis OAuth token endpoint
    print_header "🆕 NEW-HEMIS RESPONSE (port 8081)"
    NEW_TOKEN_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$NEW_BASE$ENDPOINT" \
      -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password&username=$USERNAME&password=$PASSWORD")

    NEW_HTTP_CODE=$(echo "$NEW_TOKEN_RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
    NEW_TOKEN_JSON=$(echo "$NEW_TOKEN_RESPONSE" | sed '/HTTP_CODE:/d')

    echo "$NEW_TOKEN_JSON" > "$NEW_RESPONSE"

    if [ "$NEW_HTTP_CODE" == "200" ]; then
        print_success "HTTP $NEW_HTTP_CODE - OK"
        echo "$NEW_TOKEN_JSON" | jq '.'
    else
        print_error "HTTP $NEW_HTTP_CODE - ERROR"
        echo "$NEW_TOKEN_JSON" | jq '.' 2>/dev/null || echo "$NEW_TOKEN_JSON"
    fi

    echo ""

    # Compare OAuth token responses
    print_header "📊 COMPARISON RESULT"

    if [ "$OLD_HTTP_CODE" != "200" ]; then
        print_warning "⚠️  OLD-HEMIS DA XATOLIK!"
        print_info "Old-Hemis HTTP code: $OLD_HTTP_CODE"
        print_success "✅ NATIJA: Bu endpointni PORT qilish KERAK EMAS!"
        exit 0
    fi

    if [ "$NEW_HTTP_CODE" != "200" ]; then
        print_error "❌ NEW-HEMIS DA XATOLIK!"
        print_info "New-Hemis HTTP code: $NEW_HTTP_CODE"
        print_warning "⚠️  NATIJA: Yangi hemis da xatolik bor - tuzatish kerak!"
        exit 1
    fi

    # Check if both return access_token
    OLD_ACCESS_TOKEN=$(echo "$OLD_TOKEN_JSON" | jq -r '.access_token')
    NEW_ACCESS_TOKEN=$(echo "$NEW_TOKEN_JSON" | jq -r '.access_token')

    if [ "$OLD_ACCESS_TOKEN" != "null" ] && [ "$NEW_ACCESS_TOKEN" != "null" ]; then
        print_success "✅ ✅ TOKEN MUVAFFAQIYATLI OLINDI!"
        print_info "Old-Hemis token: ${OLD_ACCESS_TOKEN:0:30}..."
        print_info "New-Hemis token: ${NEW_ACCESS_TOKEN:0:30}..."
        print_success "✅ NATIJA: OAuth token endpoint ishlayapti!"
        exit 0
    else
        print_error "❌ Token olinmadi!"
        exit 1
    fi
fi

# Step 1: Get token from old-hemis (for regular endpoints)
print_info "1️⃣ Getting token from Old-Hemis (port 8082)..."
OLD_TOKEN_RESPONSE=$(curl -s -X POST "$OLD_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -F "grant_type=password" \
  -F "username=$USERNAME" \
  -F "password=$PASSWORD")

OLD_TOKEN=$(echo "$OLD_TOKEN_RESPONSE" | jq -r '.access_token')

if [ "$OLD_TOKEN" == "null" ] || [ -z "$OLD_TOKEN" ]; then
    print_error "Old-Hemis token olishda xatolik!"
    echo "$OLD_TOKEN_RESPONSE" | jq '.'
    exit 1
fi

echo "$OLD_TOKEN" > "$OLD_TOKEN_FILE"
print_success "Old-Hemis token olindi: ${OLD_TOKEN:0:30}..."

# Step 2: Get token from new-hemis
print_info "2️⃣ Getting token from New-Hemis (port 8081)..."
NEW_TOKEN_RESPONSE=$(curl -s -X POST "$NEW_BASE/app/rest/v2/oauth/token" \
  -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
  -F "grant_type=password" \
  -F "username=$USERNAME" \
  -F "password=$PASSWORD")

NEW_TOKEN=$(echo "$NEW_TOKEN_RESPONSE" | jq -r '.access_token')

if [ "$NEW_TOKEN" == "null" ] || [ -z "$NEW_TOKEN" ]; then
    print_error "New-Hemis token olishda xatolik!"
    echo "$NEW_TOKEN_RESPONSE" | jq '.'
    exit 1
fi

echo "$NEW_TOKEN" > "$NEW_TOKEN_FILE"
print_success "New-Hemis token olindi: ${NEW_TOKEN:0:30}..."

echo ""

# Step 3: Test old-hemis endpoint
print_header "🏛️  OLD-HEMIS RESPONSE (port 8082)"
HTTP_CODE=$(curl -s -o "$OLD_RESPONSE" -w "%{http_code}" "$OLD_BASE$ENDPOINT" \
  -H "Authorization: Bearer $OLD_TOKEN")

if [ "$HTTP_CODE" == "200" ]; then
    print_success "HTTP $HTTP_CODE - OK"
    cat "$OLD_RESPONSE" | jq '.'
else
    print_error "HTTP $HTTP_CODE - ERROR"
    cat "$OLD_RESPONSE" | jq '.' 2>/dev/null || cat "$OLD_RESPONSE"
fi

echo ""

# Step 4: Test new-hemis endpoint
print_header "🆕 NEW-HEMIS RESPONSE (port 8081)"
HTTP_CODE=$(curl -s -o "$NEW_RESPONSE" -w "%{http_code}" "$NEW_BASE$ENDPOINT" \
  -H "Authorization: Bearer $NEW_TOKEN")

if [ "$HTTP_CODE" == "200" ]; then
    print_success "HTTP $HTTP_CODE - OK"
    cat "$NEW_RESPONSE" | jq '.'
else
    print_error "HTTP $HTTP_CODE - ERROR"
    cat "$NEW_RESPONSE" | jq '.' 2>/dev/null || cat "$NEW_RESPONSE"
fi

echo ""

# Step 5: Compare responses
print_header "📊 COMPARISON RESULT"

# Check if both responses are valid JSON
if ! jq empty "$OLD_RESPONSE" 2>/dev/null; then
    print_error "Old-Hemis response is not valid JSON!"
    exit 1
fi

if ! jq empty "$NEW_RESPONSE" 2>/dev/null; then
    print_error "New-Hemis response is not valid JSON!"
    exit 1
fi

# Check if old-hemis returned error (don't port non-working endpoints!)
OLD_HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$OLD_BASE$ENDPOINT" -H "Authorization: Bearer $OLD_TOKEN" 2>/dev/null)

if [ "$OLD_HTTP_CODE" != "200" ]; then
    print_warning "⚠️  OLD-HEMIS DA HAM XATOLIK!"
    echo ""
    print_info "Old-Hemis HTTP code: $OLD_HTTP_CODE"
    print_info "Bu endpoint old-hemis da ishlamaydi yoki mavjud emas."
    echo ""
    print_success "✅ NATIJA: Bu endpointni PORT qilish KERAK EMAS!"
    print_info "Sabab: Old-hemis da ham ishlamagan endpointlarni ko'chirish kerak emas."
    echo ""
    print_info "Saved responses:"
    echo "  - Old: $OLD_RESPONSE"
    echo "  - New: $NEW_RESPONSE"
    echo ""
    print_info "📝 Tavsiya: Agar bu endpoint kerak bo'lsa, avval old-hemis da tuzating."
    exit 0
fi

# Compare using diff (only if old-hemis returned 200 OK)
if diff <(jq -S '.' "$OLD_RESPONSE") <(jq -S '.' "$NEW_RESPONSE") > /dev/null; then
    print_success "Responses are 100% IDENTICAL! ✅"
    print_success "PORT muvaffaqiyatli - javoblar bir xil!"
    echo ""
    print_info "Saved responses:"
    echo "  - Old: $OLD_RESPONSE"
    echo "  - New: $NEW_RESPONSE"
    exit 0
else
    print_warning "DIFFERENCES FOUND! ⚠️"
    echo ""
    echo "Farqlar:"
    diff <(jq -S '.' "$OLD_RESPONSE") <(jq -S '.' "$NEW_RESPONSE") || true
    echo ""
    print_error "Controller tuzatish kerak!"
    print_info "Saved responses:"
    echo "  - Old: $OLD_RESPONSE"
    echo "  - New: $NEW_RESPONSE"
    exit 1
fi
