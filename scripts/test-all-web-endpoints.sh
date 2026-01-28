#!/bin/bash

###############################################################################
# HEMIS Backend - Complete /api/v1/web/* E2E Test Suite
#
# Purpose: End-to-End testing of ALL /api/v1/web/* endpoints
#
# Tests:
#   1. Authentication (login, refresh, token validation)
#   2. Dashboard (stats, cache)
#   3. Menu (multi-language, versioned cache)
#   4. I18n (messages, cache, statistics)
#   5. Translation Admin (CRUD, cache clear)
#   6. Faculty Registry (groups, by-university, dictionaries)
#   7. University Registry (list, by-code, dictionaries)
#
# Usage:
#   bash scripts/test-all-web-endpoints.sh
#
# Exit Codes:
#   0 = All tests passed
#   1 = One or more tests failed
#
# Author: Senior Architect
# Date: 2025-11-13
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8081}"
USERNAME="${TEST_USERNAME:-admin}"
PASSWORD="${TEST_PASSWORD:-admin}"

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Output directory
TEST_OUTPUT_DIR="/tmp/hemis-e2e-tests"
mkdir -p "$TEST_OUTPUT_DIR"

###############################################################################
# Helper Functions
###############################################################################

print_header() {
    echo ""
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_test() {
    ((TOTAL_TESTS++))
    echo -e "${BLUE}► Test $TOTAL_TESTS: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ PASS: $1${NC}"
    ((PASSED_TESTS++))
}

print_failure() {
    echo -e "${RED}❌ FAIL: $1${NC}"
    echo -e "${RED}   Details: $2${NC}"
    ((FAILED_TESTS++))
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

test_endpoint() {
    local test_name="$1"
    local url="$2"
    local method="${3:-GET}"
    local auth="${4:-true}"
    local expected_status="${5:-200}"

    print_test "$test_name"

    local auth_header=""
    if [[ "$auth" == "true" ]]; then
        auth_header="Authorization: Bearer $ACCESS_TOKEN"
    fi

    local response
    local http_status

    if [[ "$method" == "GET" ]]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
            -H "$auth_header" \
            "$BASE_URL$url")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
            -X "$method" \
            -H "Content-Type: application/json" \
            -H "$auth_header" \
            "$BASE_URL$url")
    fi

    http_status=$(echo "$response" | grep "HTTP_STATUS" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS/d')

    if [[ "$http_status" == "$expected_status" ]]; then
        print_success "$test_name (HTTP $http_status)"
        return 0
    else
        print_failure "$test_name" "Expected HTTP $expected_status, got $http_status"
        echo "$body" > "$TEST_OUTPUT_DIR/failed_${TOTAL_TESTS}.txt"
        return 1
    fi
}

###############################################################################
# Main Test Suite
###############################################################################

print_header "HEMIS BACKEND - COMPLETE /api/v1/web/* E2E TEST SUITE"
echo "Base URL: $BASE_URL"
echo "Test Time: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

###############################################################################
# TEST CATEGORY 1: AUTHENTICATION
###############################################################################

print_header "1. AUTHENTICATION API (/api/v1/web/auth/*)"

# Test 1.1: Login
print_test "Login with valid credentials"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/web/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

echo "$LOGIN_RESPONSE" > "$TEST_OUTPUT_DIR/login_response.json"

ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('accessToken', ''))" 2>/dev/null)
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('refreshToken', ''))" 2>/dev/null)

if [[ -n "$ACCESS_TOKEN" ]]; then
    print_success "Login successful, access token received"
    print_info "Token length: ${#ACCESS_TOKEN} chars"
else
    print_failure "Login failed" "No access token in response"
    exit 1
fi

# Test 1.2: Refresh Token
if [[ -n "$REFRESH_TOKEN" ]]; then
    print_test "Refresh token"
    REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/web/auth/refresh" \
        -H "Content-Type: application/json" \
        -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")

    NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('accessToken', ''))" 2>/dev/null)

    if [[ -n "$NEW_ACCESS_TOKEN" ]]; then
        print_success "Refresh token successful"
    else
        print_failure "Refresh token failed" "No new access token"
    fi
fi

# Test 1.3: Invalid credentials
print_test "Login with invalid credentials (should fail)"
INVALID_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$BASE_URL/api/v1/web/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong_password"}')

INVALID_STATUS=$(echo "$INVALID_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)

if [[ "$INVALID_STATUS" == "401" ]]; then
    print_success "Invalid credentials correctly rejected (401)"
else
    print_failure "Invalid credentials" "Expected 401, got $INVALID_STATUS"
fi

###############################################################################
# TEST CATEGORY 2: DASHBOARD
###############################################################################

print_header "2. DASHBOARD API (/api/v1/web/dashboard/*)"

test_endpoint "Get dashboard statistics" "/api/v1/web/dashboard/stats" "GET" "true" "200"

# Verify response structure
STATS_RESPONSE=$(curl -s -H "Authorization: Bearer $ACCESS_TOKEN" "$BASE_URL/api/v1/web/dashboard/stats")
echo "$STATS_RESPONSE" > "$TEST_OUTPUT_DIR/dashboard_stats.json"

HAS_OVERVIEW=$(echo "$STATS_RESPONSE" | grep -o "overview" | wc -l)
if [[ "$HAS_OVERVIEW" -gt 0 ]]; then
    print_success "Dashboard response contains overview data"
else
    print_failure "Dashboard response" "Missing overview field"
fi

###############################################################################
# TEST CATEGORY 3: MENU
###############################################################################

print_header "3. MENU API (/api/v1/web/menu)"

LANGUAGES=("uz-UZ" "oz-UZ" "ru-RU" "en-US")

for LANG in "${LANGUAGES[@]}"; do
    test_endpoint "Menu API - $LANG" "/api/v1/web/menu?locale=$LANG" "GET" "true" "200"
done

###############################################################################
# TEST CATEGORY 4: I18N (INTERNATIONALIZATION)
###############################################################################

print_header "4. I18N API (/api/v1/web/i18n/*)"

for LANG in "${LANGUAGES[@]}"; do
    test_endpoint "I18n messages - $LANG" "/api/v1/web/i18n/messages?lang=$LANG" "GET" "true" "200"
done

###############################################################################
# TEST CATEGORY 5: TRANSLATION ADMIN
###############################################################################

print_header "5. TRANSLATION ADMIN API (/api/v1/web/system/translation/*)"

test_endpoint "Get translation statistics" "/api/v1/web/system/translation/stats" "GET" "true" "200"

# Test cache clear
print_test "Clear translation cache"
CLEAR_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    -X POST "$BASE_URL/api/v1/web/system/translation/cache/clear" \
    -H "Authorization: Bearer $ACCESS_TOKEN")

CLEAR_STATUS=$(echo "$CLEAR_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)

if [[ "$CLEAR_STATUS" == "200" ]]; then
    print_success "Cache clear successful"
else
    print_failure "Cache clear failed" "HTTP status: $CLEAR_STATUS"
fi

###############################################################################
# TEST CATEGORY 6: FACULTY REGISTRY
###############################################################################

print_header "6. FACULTY REGISTRY API (/api/v1/web/registry/faculties/*)"

test_endpoint "Get faculty groups" "/api/v1/web/registry/faculties/groups" "GET" "true" "200"
test_endpoint "Get faculties by university" "/api/v1/web/registry/faculties/by-university/00001" "GET" "true" "200"
test_endpoint "Get faculty dictionaries" "/api/v1/web/registry/faculties/dictionaries" "GET" "true" "200"

###############################################################################
# TEST CATEGORY 7: UNIVERSITY REGISTRY
###############################################################################

print_header "7. UNIVERSITY REGISTRY API (/api/v1/web/registry/universities/*)"

test_endpoint "Get universities list" "/api/v1/web/registry/universities" "GET" "true" "200"
test_endpoint "Get university by code" "/api/v1/web/registry/universities/00001" "GET" "true" "200"
test_endpoint "Get university dictionaries" "/api/v1/web/registry/universities/dictionaries" "GET" "true" "200"

###############################################################################
# TEST CATEGORY 8: AUTHENTICATION VALIDATION
###############################################################################

print_header "8. AUTHENTICATION VALIDATION"

# Test unauthorized access
test_endpoint "Dashboard without auth (should fail)" "/api/v1/web/dashboard/stats" "GET" "false" "401"
test_endpoint "Menu without auth (should fail)" "/api/v1/web/menu?locale=uz-UZ" "GET" "false" "401"
test_endpoint "I18n without auth (should fail)" "/api/v1/web/i18n/messages?lang=uz-UZ" "GET" "false" "401"

###############################################################################
# TEST SUMMARY
###############################################################################

print_header "TEST SUMMARY"

echo ""
echo "Total Tests:  $TOTAL_TESTS"
echo -e "${GREEN}Passed:       $PASSED_TESTS${NC}"
echo -e "${RED}Failed:       $FAILED_TESTS${NC}"
echo ""

PASS_PERCENTAGE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
echo "Pass Rate: $PASS_PERCENTAGE%"
echo ""

if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✅ ALL TESTS PASSED - READY FOR PRODUCTION${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Test artifacts saved to: $TEST_OUTPUT_DIR"
    exit 0
else
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}❌ SOME TESTS FAILED - DO NOT DEPLOY${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Test artifacts saved to: $TEST_OUTPUT_DIR"
    echo "Check failed test outputs for details."
    exit 1
fi
