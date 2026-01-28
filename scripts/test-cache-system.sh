#!/bin/bash

###############################################################################
# HEMIS Backend - Versioned Cache System Integration Test
#
# Purpose: Automated testing of cache implementation
#
# Tests:
#   1. Backend health check
#   2. Login & Token management (access + refresh tokens)
#   3. Menu API with different languages
#   4. Cache versioning (L1 Caffeine + L2 Redis)
#   5. Cache invalidation (version increment + Pub/Sub)
#   6. Translation API response
#   7. Multi-language switching
#
# Usage:
#   bash scripts/test-cache-system.sh
#
# Exit Codes:
#   0 = All tests passed
#   1 = One or more tests failed
#
# Author: Senior Architect
# Date: 2025-11-13
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test configuration
BASE_URL="${BASE_URL:-http://localhost:8081}"
USERNAME="${TEST_USERNAME:-admin}"
PASSWORD="${TEST_PASSWORD:-admin}"

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Output directory for test results
TEST_OUTPUT_DIR="/tmp/hemis-cache-tests"
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

# Run a test and check result
run_test() {
    ((TOTAL_TESTS++))
    local test_name="$1"
    local test_command="$2"
    local expected_pattern="$3"

    print_test "$test_name"

    local result
    if result=$(eval "$test_command" 2>&1); then
        if [[ -z "$expected_pattern" ]] || echo "$result" | grep -q "$expected_pattern"; then
            print_success "$test_name"
            return 0
        else
            print_failure "$test_name" "Expected pattern not found: $expected_pattern"
            echo "$result" > "$TEST_OUTPUT_DIR/test_${TOTAL_TESTS}_output.txt"
            return 1
        fi
    else
        print_failure "$test_name" "Command failed: $test_command"
        echo "$result" > "$TEST_OUTPUT_DIR/test_${TOTAL_TESTS}_output.txt"
        return 1
    fi
}

###############################################################################
# Test Suite
###############################################################################

print_header "HEMIS BACKEND - VERSIONED CACHE SYSTEM TEST SUITE"
echo "Base URL: $BASE_URL"
echo "Test Time: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

###############################################################################
# Test 1: Backend Health Check
###############################################################################

print_header "1. BACKEND HEALTH CHECK"

run_test "Backend is running" \
    "curl -s -f $BASE_URL/actuator/health" \
    "UP"

###############################################################################
# Test 2: Login & Token Management
###############################################################################

print_header "2. LOGIN & TOKEN MANAGEMENT"

# Login and get access token
print_test "Login with credentials"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/web/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

# Save full response
echo "$LOGIN_RESPONSE" > "$TEST_OUTPUT_DIR/login_response.json"

# Extract tokens
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('accessToken', ''))" 2>/dev/null)
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('refreshToken', ''))" 2>/dev/null)
EXPIRES_IN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('expiresIn', ''))" 2>/dev/null)

if [[ -n "$ACCESS_TOKEN" ]]; then
    ((PASSED_TESTS++))
    print_success "Login successful, access token received"
    print_info "Token length: ${#ACCESS_TOKEN} chars"
    print_info "Expires in: ${EXPIRES_IN}s"
    echo "$ACCESS_TOKEN" > "$TEST_OUTPUT_DIR/access_token.txt"
else
    ((FAILED_TESTS++))
    print_failure "Login failed" "No access token in response"
    echo "Exiting - cannot continue without token"
    exit 1
fi

# Test refresh token
if [[ -n "$REFRESH_TOKEN" ]]; then
    print_test "Refresh token received"
    print_success "Refresh token present"
    print_info "Refresh token length: ${#REFRESH_TOKEN} chars"
    echo "$REFRESH_TOKEN" > "$TEST_OUTPUT_DIR/refresh_token.txt"
else
    print_failure "Refresh token missing" "No refreshToken in response"
fi

###############################################################################
# Test 3: Menu API (Multi-Language)
###############################################################################

print_header "3. MENU API - MULTI-LANGUAGE TEST"

LANGUAGES=("uz-UZ" "oz-UZ" "ru-RU" "en-US")

for LANG in "${LANGUAGES[@]}"; do
    print_test "Menu API - Language: $LANG"

    MENU_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        "$BASE_URL/api/v1/web/menu?locale=$LANG" \
        -H "Authorization: Bearer $ACCESS_TOKEN")

    HTTP_STATUS=$(echo "$MENU_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
    MENU_BODY=$(echo "$MENU_RESPONSE" | sed '/HTTP_STATUS/d')

    echo "$MENU_BODY" > "$TEST_OUTPUT_DIR/menu_${LANG}.json"

    if [[ "$HTTP_STATUS" == "200" ]]; then
        # Check if response contains menu items
        MENU_COUNT=$(echo "$MENU_BODY" | python3 -c "import sys, json; d=json.load(sys.stdin); print(len(d) if isinstance(d, list) else 0)" 2>/dev/null)

        if [[ "$MENU_COUNT" -gt 0 ]]; then
            print_success "Menu API - $LANG (${MENU_COUNT} items)"
        else
            print_failure "Menu API - $LANG" "Empty menu response"
        fi
    else
        print_failure "Menu API - $LANG" "HTTP Status: $HTTP_STATUS"
    fi
done

###############################################################################
# Test 4: Redis Cache Verification
###############################################################################

print_header "4. REDIS CACHE VERIFICATION"

# Check if redis-cli is available
if ! command -v redis-cli &> /dev/null; then
    print_info "redis-cli not found - skipping Redis tests"
else
    # Test 4.1: Check cache version key
    print_test "Cache version key exists"
    CACHE_VERSION=$(redis-cli GET "cache:version:i18n" 2>/dev/null)

    if [[ -n "$CACHE_VERSION" ]]; then
        print_success "Cache version: $CACHE_VERSION"
        echo "$CACHE_VERSION" > "$TEST_OUTPUT_DIR/cache_version.txt"
    else
        print_failure "Cache version key not found" "cache:version:i18n is empty"
    fi

    # Test 4.2: Check versioned cache keys
    print_test "Versioned cache keys exist"
    CACHE_KEYS=$(redis-cli KEYS "i18n:v*:messages:*" 2>/dev/null | wc -l)

    if [[ "$CACHE_KEYS" -gt 0 ]]; then
        print_success "Found $CACHE_KEYS versioned cache keys"
        redis-cli KEYS "i18n:v*:messages:*" > "$TEST_OUTPUT_DIR/cache_keys.txt"
    else
        print_failure "No versioned cache keys found" "Expected i18n:v*:messages:* keys"
    fi

    # Test 4.3: Check cache key format
    print_test "Cache key format (i18n:v{N}:messages:{lang})"
    SAMPLE_KEY=$(redis-cli KEYS "i18n:v*:messages:uz-UZ" 2>/dev/null | head -1)

    if [[ "$SAMPLE_KEY" =~ ^i18n:v[0-9]+:messages:uz-UZ$ ]]; then
        print_success "Cache key format correct: $SAMPLE_KEY"
    else
        print_failure "Cache key format incorrect" "Expected: i18n:v{N}:messages:uz-UZ, Got: $SAMPLE_KEY"
    fi

    # Test 4.4: Check cache TTL
    print_test "Cache TTL verification"
    if [[ -n "$SAMPLE_KEY" ]]; then
        CACHE_TTL=$(redis-cli TTL "$SAMPLE_KEY" 2>/dev/null)

        # TTL should be > 0 and <= 1800 (30 minutes)
        if [[ "$CACHE_TTL" -gt 0 ]] && [[ "$CACHE_TTL" -le 1800 ]]; then
            print_success "Cache TTL: ${CACHE_TTL}s (≤30 min)"
        else
            print_failure "Cache TTL out of range" "Expected: 0-1800s, Got: ${CACHE_TTL}s"
        fi
    else
        print_failure "Cache TTL check skipped" "No sample key found"
    fi
fi

###############################################################################
# Test 5: Translation API
###############################################################################

print_header "5. TRANSLATION API TEST"

print_test "Get all messages (uz-UZ)"
I18N_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    "$BASE_URL/api/v1/web/i18n/messages?lang=uz-UZ" \
    -H "Authorization: Bearer $ACCESS_TOKEN")

HTTP_STATUS=$(echo "$I18N_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
I18N_BODY=$(echo "$I18N_RESPONSE" | sed '/HTTP_STATUS/d')

echo "$I18N_BODY" > "$TEST_OUTPUT_DIR/i18n_messages.json"

if [[ "$HTTP_STATUS" == "200" ]]; then
    # Check if response contains data
    MESSAGE_COUNT=$(echo "$I18N_BODY" | python3 -c "import sys, json; d=json.load(sys.stdin); print(len(d.get('data', {})))" 2>/dev/null)

    if [[ "$MESSAGE_COUNT" -gt 0 ]]; then
        print_success "Translation API - ${MESSAGE_COUNT} messages"
    else
        print_failure "Translation API" "Empty messages response"
    fi
else
    print_failure "Translation API" "HTTP Status: $HTTP_STATUS"
fi

###############################################################################
# Test 6: Cache Invalidation Test
###############################################################################

print_header "6. CACHE INVALIDATION TEST"

if command -v redis-cli &> /dev/null; then
    # Get current version
    print_test "Get current cache version"
    VERSION_BEFORE=$(redis-cli GET "cache:version:i18n" 2>/dev/null)
    print_success "Version before: $VERSION_BEFORE"

    # Trigger cache invalidation via API
    print_test "Trigger cache invalidation"
    CLEAR_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "$BASE_URL/api/v1/web/system/translation/cache/clear" \
        -H "Authorization: Bearer $ACCESS_TOKEN")

    HTTP_STATUS=$(echo "$CLEAR_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
    CLEAR_BODY=$(echo "$CLEAR_RESPONSE" | sed '/HTTP_STATUS/d')

    echo "$CLEAR_BODY" > "$TEST_OUTPUT_DIR/cache_clear_response.json"

    if [[ "$HTTP_STATUS" == "200" ]]; then
        print_success "Cache invalidation triggered"

        # Wait a moment for invalidation to complete
        sleep 1

        # Check if version incremented
        print_test "Verify version increment"
        VERSION_AFTER=$(redis-cli GET "cache:version:i18n" 2>/dev/null)

        if [[ "$VERSION_AFTER" -gt "$VERSION_BEFORE" ]]; then
            print_success "Version incremented: $VERSION_BEFORE → $VERSION_AFTER"
        else
            print_failure "Version not incremented" "Before: $VERSION_BEFORE, After: $VERSION_AFTER"
        fi
    else
        print_failure "Cache invalidation failed" "HTTP Status: $HTTP_STATUS"
    fi
else
    print_info "redis-cli not available - skipping invalidation test"
fi

###############################################################################
# Test 7: Translation Statistics
###############################################################################

print_header "7. TRANSLATION STATISTICS"

print_test "Get translation statistics"
STATS_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
    "$BASE_URL/api/v1/web/system/translation/stats" \
    -H "Authorization: Bearer $ACCESS_TOKEN")

HTTP_STATUS=$(echo "$STATS_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
STATS_BODY=$(echo "$STATS_RESPONSE" | sed '/HTTP_STATUS/d')

echo "$STATS_BODY" > "$TEST_OUTPUT_DIR/translation_stats.json"

if [[ "$HTTP_STATUS" == "200" ]]; then
    # Check if cache stats include version info
    HAS_VERSION=$(echo "$STATS_BODY" | grep -o "currentVersion" | wc -l)

    if [[ "$HAS_VERSION" -gt 0 ]]; then
        print_success "Statistics include cache version info"

        # Extract and display key stats
        CURRENT_VERSION=$(echo "$STATS_BODY" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('cache', {}).get('currentVersion', 'N/A'))" 2>/dev/null)
        print_info "Current version from stats: $CURRENT_VERSION"
    else
        print_failure "Statistics missing version info" "currentVersion field not found"
    fi
else
    print_failure "Statistics API" "HTTP Status: $HTTP_STATUS"
fi

###############################################################################
# Test 8: Caffeine L1 Cache Statistics
###############################################################################

print_header "8. CAFFEINE L1 CACHE STATISTICS"

print_test "Get Caffeine cache statistics"

# Get stats from the statistics endpoint
CAFFEINE_STATS=$(echo "$STATS_BODY" | python3 -c "import sys, json; d=json.load(sys.stdin); print(json.dumps(d.get('cacheStatistics', {}), indent=2))" 2>/dev/null)

if [[ -n "$CAFFEINE_STATS" ]] && [[ "$CAFFEINE_STATS" != "{}" ]]; then
    print_success "Caffeine L1 statistics available"
    echo "$CAFFEINE_STATS" > "$TEST_OUTPUT_DIR/caffeine_stats.json"

    # Check if stats contain expected fields
    HAS_HIT_RATE=$(echo "$CAFFEINE_STATS" | grep -o "hitRate" | wc -l)
    if [[ "$HAS_HIT_RATE" -gt 0 ]]; then
        print_info "Hit rate metrics present"
    fi
else
    print_failure "Caffeine statistics missing" "cacheStatistics field empty or missing"
fi

###############################################################################
# Test Summary
###############################################################################

print_header "TEST SUMMARY"

echo ""
echo "Total Tests:  $TOTAL_TESTS"
echo -e "${GREEN}Passed:       $PASSED_TESTS${NC}"
echo -e "${RED}Failed:       $FAILED_TESTS${NC}"
echo ""

if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}✅ ALL TESTS PASSED${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Test artifacts saved to: $TEST_OUTPUT_DIR"
    exit 0
else
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}❌ SOME TESTS FAILED${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Test artifacts saved to: $TEST_OUTPUT_DIR"
    echo "Check failed test outputs for details."
    exit 1
fi
