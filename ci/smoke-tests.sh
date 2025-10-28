#!/bin/bash

# =====================================================
# HEMIS Smoke Tests
# =====================================================
# Purpose: Verify production deployment
# Usage: ./ci/smoke-tests.sh <base-url> [jwt-token]
# Example: ./ci/smoke-tests.sh http://localhost:8080
# =====================================================

set -e

BASE_URL="${1:-http://localhost:8080}"
JWT_TOKEN="${2:-}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

PASSED=0
FAILED=0

log_info() {
    echo -e "${GREEN}[PASS]${NC} $1"
    PASSED=$((PASSED + 1))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
    FAILED=$((FAILED + 1))
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# =====================================================
# Helper Functions
# =====================================================

http_get() {
    local url="$1"
    local expected_status="${2:-200}"

    if [ -n "$JWT_TOKEN" ]; then
        response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $JWT_TOKEN" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" "$url")
    fi

    body=$(echo "$response" | head -n -1)
    status=$(echo "$response" | tail -n 1)

    if [ "$status" = "$expected_status" ]; then
        echo "$body"
        return 0
    else
        echo "Expected $expected_status, got $status"
        return 1
    fi
}

# =====================================================
# Smoke Tests
# =====================================================

echo ""
echo "======================================================"
echo "  HEMIS Smoke Tests"
echo "======================================================"
echo ""
echo "Target: $BASE_URL"
echo "JWT Token: ${JWT_TOKEN:+Provided}"
echo ""

# =====================================================
# Test 1: Health Check
# =====================================================

echo "Test 1: Health Check"
if result=$(http_get "$BASE_URL/actuator/health" 200); then
    if echo "$result" | grep -q '"status":"UP"'; then
        log_info "Health check: UP"
    else
        log_error "Health check: status not UP"
    fi
else
    log_error "Health check endpoint failed"
fi
echo ""

# =====================================================
# Test 2: Application Info
# =====================================================

echo "Test 2: Application Info"
if result=$(http_get "$BASE_URL/actuator/info" 200); then
    log_info "Application info accessible"
else
    log_error "Application info endpoint failed"
fi
echo ""

# =====================================================
# Test 3: Database Connectivity
# =====================================================

echo "Test 3: Database Connectivity (via health check)"
if result=$(http_get "$BASE_URL/actuator/health" 200); then
    if echo "$result" | grep -q '"db".*"status":"UP"'; then
        log_info "Database connection: UP"
    else
        log_warn "Database status not found in health check"
    fi
else
    log_error "Cannot verify database connectivity"
fi
echo ""

# =====================================================
# Test 4: Students API (Authentication Required)
# =====================================================

echo "Test 4: Students API (GET /app/rest/v2/students)"
if [ -z "$JWT_TOKEN" ]; then
    log_warn "JWT token not provided - expecting 401 Unauthorized"
    if result=$(http_get "$BASE_URL/app/rest/v2/students" 401 2>/dev/null); then
        log_info "Authentication required (401) - correct behavior"
    else
        # Try without expecting 401
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL/app/rest/v2/students")
        status=$(echo "$response" | tail -n 1)
        if [ "$status" = "401" ]; then
            log_info "Authentication required (401) - correct behavior"
        else
            log_error "Expected 401, got $status (authentication may not be enforced)"
        fi
    fi
else
    log_info "JWT token provided - testing authenticated access"
    if result=$(http_get "$BASE_URL/app/rest/v2/students?page=0&size=1" 200); then
        if echo "$result" | grep -q '"success":true'; then
            log_info "Students API accessible with JWT"
            if echo "$result" | grep -q '"content"'; then
                log_info "Response contains pagination data"
            fi
        else
            log_error "Students API response format incorrect"
        fi
    else
        log_error "Students API request failed"
    fi
fi
echo ""

# =====================================================
# Test 5: Legacy JSON Field Names
# =====================================================

echo "Test 5: Legacy JSON Field Names"
if [ -n "$JWT_TOKEN" ]; then
    if result=$(http_get "$BASE_URL/app/rest/v2/students?page=0&size=1" 200); then
        # Check for underscore-prefixed fields
        if echo "$result" | grep -q '"_university"'; then
            log_info "Legacy field '_university' preserved"
        else
            log_warn "Field '_university' not found (may be no data)"
        fi

        if echo "$result" | grep -q '"_student_status"'; then
            log_info "Legacy field '_student_status' preserved"
        else
            log_warn "Field '_student_status' not found (may be no data)"
        fi
    fi
else
    log_warn "JWT token required to verify legacy field names"
fi
echo ""

# =====================================================
# Test 6: NO DELETE Endpoint (NDG)
# =====================================================

echo "Test 6: DELETE Endpoint (should return 405)"
response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/app/rest/v2/students/00000000-0000-0000-0000-000000000000")
status=$(echo "$response" | tail -n 1)

if [ "$status" = "405" ]; then
    log_info "DELETE endpoint blocked (405 Method Not Allowed) - NDG enforced"
elif [ "$status" = "401" ]; then
    log_info "DELETE endpoint requires auth (401) - will be blocked by security"
else
    log_error "DELETE endpoint returned unexpected status: $status"
fi
echo ""

# =====================================================
# Test 7: CORS Headers
# =====================================================

echo "Test 7: CORS Configuration"
response=$(curl -s -I -H "Origin: http://example.com" "$BASE_URL/actuator/health")

if echo "$response" | grep -qi "Access-Control-Allow-Origin"; then
    log_info "CORS headers present"
else
    log_warn "CORS headers not found (may not be required for actuator)"
fi
echo ""

# =====================================================
# Test 8: Response Time
# =====================================================

echo "Test 8: Response Time"
start_time=$(date +%s%N)
http_get "$BASE_URL/actuator/health" 200 > /dev/null
end_time=$(date +%s%N)
duration_ms=$(( (end_time - start_time) / 1000000 ))

if [ "$duration_ms" -lt 1000 ]; then
    log_info "Response time: ${duration_ms}ms (< 1s)"
elif [ "$duration_ms" -lt 3000 ]; then
    log_warn "Response time: ${duration_ms}ms (1-3s, acceptable)"
else
    log_error "Response time: ${duration_ms}ms (> 3s, too slow)"
fi
echo ""

# =====================================================
# Test 9: Error Handling
# =====================================================

echo "Test 9: Error Handling (404 Not Found)"
if [ -n "$JWT_TOKEN" ]; then
    response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $JWT_TOKEN" \
        "$BASE_URL/app/rest/v2/students/00000000-0000-0000-0000-000000000000")
    body=$(echo "$response" | head -n -1)
    status=$(echo "$response" | tail -n 1)

    if [ "$status" = "404" ]; then
        if echo "$body" | grep -q '"status":404'; then
            log_info "404 error properly formatted"
        else
            log_warn "404 response format may be non-standard"
        fi
    else
        log_warn "Expected 404 for non-existent student, got $status"
    fi
else
    log_warn "JWT token required to test error handling"
fi
echo ""

# =====================================================
# Test 10: Environment Configuration
# =====================================================

echo "Test 10: Environment Configuration"
if result=$(http_get "$BASE_URL/actuator/health" 200); then
    log_info "Application responding (environment configured correctly)"
else
    log_error "Application not responding (check environment variables)"
fi
echo ""

# =====================================================
# Summary
# =====================================================

echo "======================================================"
echo "  Smoke Tests Summary"
echo "======================================================"
echo ""
echo "Passed: $PASSED"
echo "Failed: $FAILED"
echo ""

if [ "$FAILED" -eq 0 ]; then
    echo -e "${GREEN}✅ ALL SMOKE TESTS PASSED${NC}"
    echo ""
    echo "Production deployment verified successfully!"
    echo ""
    exit 0
else
    echo -e "${RED}❌ SOME SMOKE TESTS FAILED${NC}"
    echo ""
    echo "Review failed tests above and fix issues before proceeding."
    echo ""
    exit 1
fi
