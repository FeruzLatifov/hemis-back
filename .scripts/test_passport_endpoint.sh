#!/bin/bash
# test_passport_endpoint.sh - Passport endpoint to'liq workflow test (captcha bilan)
# Usage: ./test_passport_endpoint.sh [pinfl] [seriaNumber]

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Configuration
OLD_BASE="http://localhost:8082"
NEW_BASE="http://localhost:8081"
USERNAME="feruz"
PASSWORD="BvZzXW6oQxEEte"
CLIENT_ID="client"
CLIENT_SECRET="secret"

# Default values from database (e_student.passport_pin)
DEFAULT_PINFL="61902025630068"
DEFAULT_SERIA="AC2455764"

# Input parameters
PINFL="${1:-$DEFAULT_PINFL}"
SERIA="${2:-$DEFAULT_SERIA}"

# Output files
OLD_TOKEN_FILE="/tmp/old_passport_token.txt"
NEW_TOKEN_FILE="/tmp/new_passport_token.txt"
OLD_CAPTCHA_FILE="/tmp/old_passport_captcha.json"
NEW_CAPTCHA_FILE="/tmp/new_passport_captcha.json"
OLD_PASSPORT_FILE="/tmp/old_passport_response.json"
NEW_PASSPORT_FILE="/tmp/new_passport_response.json"

# Function to print colored output
print_header() {
    echo -e "${CYAN}============================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}============================================${NC}"
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

print_step() {
    echo -e "${MAGENTA}$1${NC}"
}

# Function to get token
get_token() {
    local BASE_URL=$1
    local SYSTEM_NAME=$2
    local OUTPUT_FILE=$3

    print_step "\n📝 Step 1: Getting token from $SYSTEM_NAME..."

    local RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "$BASE_URL/app/rest/v2/oauth/token" \
      -H "Authorization: Basic $(echo -n "$CLIENT_ID:$CLIENT_SECRET" | base64)" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password&username=$USERNAME&password=$PASSWORD")

    local HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
    local JSON=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

    if [ "$HTTP_CODE" == "200" ]; then
        local TOKEN=$(echo "$JSON" | jq -r '.access_token')
        echo "$TOKEN" > "$OUTPUT_FILE"
        print_success "Token received: ${TOKEN:0:50}..."
        return 0
    else
        print_error "Failed to get token (HTTP $HTTP_CODE)"
        echo "$JSON" | jq '.' 2>/dev/null || echo "$JSON"
        return 1
    fi
}

# Function to get captcha
get_captcha() {
    local BASE_URL=$1
    local SYSTEM_NAME=$2
    local TOKEN_FILE=$3
    local OUTPUT_FILE=$4

    print_step "\n🎨 Step 2: Getting captcha from $SYSTEM_NAME..."

    local TOKEN=$(cat "$TOKEN_FILE")
    local RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
      "$BASE_URL/app/rest/v2/services/captcha/getNumericCaptcha" \
      -H "Authorization: Bearer $TOKEN")

    local HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
    local JSON=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

    if [ "$HTTP_CODE" == "200" ]; then
        echo "$JSON" > "$OUTPUT_FILE"
        local CAPTCHA_ID=$(echo "$JSON" | jq -r '.id')
        print_success "Captcha ID: $CAPTCHA_ID"
        print_info "Captcha image: base64 PNG available"
        return 0
    else
        print_error "Failed to get captcha (HTTP $HTTP_CODE)"
        echo "$JSON" | jq '.' 2>/dev/null || echo "$JSON"
        return 1
    fi
}

# Function to test passport endpoint
test_passport() {
    local BASE_URL=$1
    local SYSTEM_NAME=$2
    local TOKEN_FILE=$3
    local CAPTCHA_FILE=$4
    local OUTPUT_FILE=$5
    local CAPTCHA_VALUE=$6

    print_step "\n🔍 Step 3: Testing passport endpoint on $SYSTEM_NAME..."

    local TOKEN=$(cat "$TOKEN_FILE")
    local CAPTCHA_ID=$(cat "$CAPTCHA_FILE" | jq -r '.id')

    print_info "PINFL: $PINFL"
    print_info "Seria: $SERIA"
    print_info "Captcha ID: $CAPTCHA_ID"
    print_info "Captcha Value: $CAPTCHA_VALUE"

    local RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
      "$BASE_URL/app/rest/v2/services/passport-data/getDataBySN?pinfl=$PINFL&seriaNumber=$SERIA&captchaId=$CAPTCHA_ID&captchaValue=$CAPTCHA_VALUE" \
      -H "Authorization: Bearer $TOKEN")

    local HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d':' -f2)
    local JSON=$(echo "$RESPONSE" | sed '/HTTP_CODE:/d')

    echo "$JSON" > "$OUTPUT_FILE"

    if [ "$HTTP_CODE" == "200" ]; then
        local SUCCESS=$(echo "$JSON" | jq -r '.success')
        if [ "$SUCCESS" == "true" ]; then
            print_success "HTTP $HTTP_CODE - Passport data retrieved successfully!"
            echo "$JSON" | jq '{success, data: .data.data[0] | {pinfl: .current_pinpp, name: .name_latin, surname: .sur_name_latin, document: .document}}'
        else
            print_warning "HTTP $HTTP_CODE - Request succeeded but data retrieval failed"
            local ERROR=$(echo "$JSON" | jq -r '.data')
            echo "Error: $ERROR"
        fi
    elif [ "$HTTP_CODE" == "400" ]; then
        print_warning "HTTP $HTTP_CODE - Bad Request (likely invalid captcha)"
        echo "$JSON" | jq '.'
    else
        print_error "HTTP $HTTP_CODE - Error"
        echo "$JSON" | jq '.' 2>/dev/null || echo "$JSON"
    fi

    return 0
}

# Function to compare responses
compare_responses() {
    print_header "📊 RESPONSE COMPARISON"

    if [ ! -f "$OLD_PASSPORT_FILE" ] || [ ! -f "$NEW_PASSPORT_FILE" ]; then
        print_warning "Cannot compare - one or both response files missing"
        return 1
    fi

    echo -e "\n${BLUE}🏛️  Old-hemis response:${NC}"
    cat "$OLD_PASSPORT_FILE" | jq '.'

    echo -e "\n${BLUE}🆕 New-hemis response:${NC}"
    cat "$NEW_PASSPORT_FILE" | jq '.'

    echo -e "\n${BLUE}Comparison:${NC}"

    # Compare success field
    local OLD_SUCCESS=$(cat "$OLD_PASSPORT_FILE" | jq -r '.success')
    local NEW_SUCCESS=$(cat "$NEW_PASSPORT_FILE" | jq -r '.success')

    if [ "$OLD_SUCCESS" == "$NEW_SUCCESS" ]; then
        print_success "Success field matches: $OLD_SUCCESS"
    else
        print_error "Success field differs: old=$OLD_SUCCESS, new=$NEW_SUCCESS"
    fi

    # If both succeeded, compare data
    if [ "$OLD_SUCCESS" == "true" ] && [ "$NEW_SUCCESS" == "true" ]; then
        local OLD_PINFL=$(cat "$OLD_PASSPORT_FILE" | jq -r '.data.data[0].current_pinpp')
        local NEW_PINFL=$(cat "$NEW_PASSPORT_FILE" | jq -r '.data.data[0].current_pinpp')

        if [ "$OLD_PINFL" == "$NEW_PINFL" ]; then
            print_success "PINFL matches: $OLD_PINFL"
        else
            print_error "PINFL differs: old=$OLD_PINFL, new=$NEW_PINFL"
        fi
    fi

    # If both failed, compare error messages
    if [ "$OLD_SUCCESS" == "false" ] && [ "$NEW_SUCCESS" == "false" ]; then
        local OLD_ERROR=$(cat "$OLD_PASSPORT_FILE" | jq -r '.data')
        local NEW_ERROR=$(cat "$NEW_PASSPORT_FILE" | jq -r '.data')

        if [ "$OLD_ERROR" == "$NEW_ERROR" ]; then
            print_success "Error messages match: $OLD_ERROR"
        else
            print_warning "Error messages differ: old='$OLD_ERROR', new='$NEW_ERROR'"
        fi
    fi
}

# Main execution
clear
print_header "PASSPORT ENDPOINT WORKFLOW TEST"
echo "PINFL: $PINFL"
echo "Passport Seria: $SERIA"
echo ""
print_warning "⚠️  This test uses WRONG captcha value (00000) to demonstrate validation"
print_info "💡 For successful test, you need to manually read captcha image and provide correct value"
echo ""

# Test old-hemis
print_header "🏛️  OLD-HEMIS WORKFLOW (port 8082)"
if timeout 3 curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    print_success "Server is running"

    if get_token "$OLD_BASE" "old-hemis" "$OLD_TOKEN_FILE"; then
        if get_captcha "$OLD_BASE" "old-hemis" "$OLD_TOKEN_FILE" "$OLD_CAPTCHA_FILE"; then
            test_passport "$OLD_BASE" "old-hemis" "$OLD_TOKEN_FILE" "$OLD_CAPTCHA_FILE" "$OLD_PASSPORT_FILE" "00000"
        fi
    fi
else
    print_error "Old-hemis server is not running on port 8082"
    print_info "Start it with: cd /home/adm1n/startup/old-hemis && ./run.sh"
fi

# Test new-hemis
print_header "🆕 NEW-HEMIS WORKFLOW (port 8081)"
if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    print_success "Server is running"

    if get_token "$NEW_BASE" "new-hemis" "$NEW_TOKEN_FILE"; then
        if get_captcha "$NEW_BASE" "new-hemis" "$NEW_TOKEN_FILE" "$NEW_CAPTCHA_FILE"; then
            test_passport "$NEW_BASE" "new-hemis" "$NEW_TOKEN_FILE" "$NEW_CAPTCHA_FILE" "$NEW_PASSPORT_FILE" "00000"
        fi
    fi
else
    print_error "New-hemis server is not running on port 8081"
    print_info "Start it with: cd /home/adm1n/startup/hemis-back && ./gradlew :app:bootRun"
fi

# Compare results
echo ""
compare_responses

# Print summary
print_header "📝 TEST SUMMARY"
echo "✅ Workflow tested:"
echo "   1. Token authentication"
echo "   2. Captcha generation"
echo "   3. Passport endpoint with captcha validation"
echo ""
echo "📂 Output files:"
echo "   Old-hemis response: $OLD_PASSPORT_FILE"
echo "   New-hemis response: $NEW_PASSPORT_FILE"
echo ""
print_info "💡 To test with CORRECT captcha value:"
echo "   1. Check captcha image in $OLD_CAPTCHA_FILE or $NEW_CAPTCHA_FILE"
echo "   2. Decode base64 image: cat $NEW_CAPTCHA_FILE | jq -r '.image' | base64 -d > /tmp/captcha.png"
echo "   3. View image: xdg-open /tmp/captcha.png (or use endpoint_tester.html)"
echo "   4. Re-run with correct value: $0 $PINFL $SERIA"
echo ""
print_success "Test completed! ✨"
