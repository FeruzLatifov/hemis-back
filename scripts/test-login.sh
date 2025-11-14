#!/bin/bash
#
# Login API Test
# Maqsad: Login endpoint ishlaganligini tekshirish
#

BASE_URL="${BASE_URL:-http://localhost:8081}"
PASSED=0
FAILED=0

echo "==================================="
echo "🧪 LOGIN API TEST"
echo "==================================="
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Admin login (NEW system)
echo "Test 1: Admin login (NEW system)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/app/rest/v2/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -d "grant_type=password&username=admin&password=admin")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    TOKEN=$(echo "$BODY" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        echo "✅ PASS: Admin login successful"
        echo "   Token: ${TOKEN:0:50}..."
        PASSED=$((PASSED+1))
    else
        echo "❌ FAIL: Token not found in response"
        echo "   Response: $BODY"
        FAILED=$((FAILED+1))
    fi
else
    echo "❌ FAIL: HTTP $HTTP_CODE"
    echo "   Response: $BODY"
    FAILED=$((FAILED+1))
fi

# Test 2: Invalid credentials
echo ""
echo "Test 2: Invalid credentials"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST $BASE_URL/app/rest/v2/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -d "grant_type=password&username=admin&password=wrongpassword")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "400" ]; then
    echo "✅ PASS: Invalid credentials rejected (HTTP $HTTP_CODE)"
    PASSED=$((PASSED+1))
else
    echo "❌ FAIL: Expected 401/400, got HTTP $HTTP_CODE"
    FAILED=$((FAILED+1))
fi

# Test 3: Token expiration field
echo ""
echo "Test 3: Token response format"
RESPONSE=$(curl -s -X POST $BASE_URL/app/rest/v2/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -d "grant_type=password&username=admin&password=admin")

if echo "$RESPONSE" | grep -q "expires_in"; then
    EXPIRES=$(echo "$RESPONSE" | grep -o '"expires_in":[0-9]*' | cut -d':' -f2)
    echo "✅ PASS: expires_in field present ($EXPIRES seconds)"
    PASSED=$((PASSED+1))
else
    echo "❌ FAIL: expires_in field missing"
    echo "   Response: $RESPONSE"
    FAILED=$((FAILED+1))
fi

# Test 4: Refresh token field
echo ""
echo "Test 4: Refresh token presence"
if echo "$RESPONSE" | grep -q "refresh_token"; then
    echo "✅ PASS: refresh_token field present"
    PASSED=$((PASSED+1))
else
    echo "❌ FAIL: refresh_token field missing"
    echo "   Response: $RESPONSE"
    FAILED=$((FAILED+1))
fi

# Summary
echo ""
echo "==================================="
echo "SUMMARY:"
echo "  ✅ Passed: $PASSED/4"
echo "  ❌ Failed: $FAILED/4"
echo "==================================="

# Exit code
if [ $FAILED -eq 0 ]; then
    echo "🎉 ALL TESTS PASSED!"
    exit 0
else
    echo "⚠️  SOME TESTS FAILED!"
    exit 1
fi
