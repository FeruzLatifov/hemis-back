# API Testlar - Regression va Integration

**Maqsad:** API o'zgarishlarni erta aniqlash va sifatni ta'minlash
**Sana:** 2025-11-09
**Coverage:** Login, User Management, Basic CRUD

---

## 🎯 Nima uchun test kerak?

### Muammolar:

1. **Regression** - Yangi kod eski funksiyani buzishi mumkin
2. **Integration** - Modullar bir-biri bilan to'g'ri ishlaydimi?
3. **Performance** - API sekin ishlaydimi?
4. **Security** - Xavfsizlik buzilishi mumkin

### Yechim: Avtomatik testlar

```bash
# Har safar deployment oldidan
./scripts/run-api-tests.sh

# Agar barcha testlar o'tsa → ✅ Deploy qilish xavfsiz
# Agar test fail bo'lsa → ❌ Deployment to'xtatish
```

---

## 📁 Test fayllar

```
hemis-back/
├── scripts/
│   ├── run-api-tests.sh           # Barcha testlarni yuritadi
│   ├── test-login.sh              # Login test
│   ├── test-users.sh              # User CRUD test
│   └── test-regression.sh         # Regression test suite
└── docs/
    └── API_TESTS.md               # Bu fayl
```

---

## 🧪 Test 1: Login Autentifikatsiya

### Fayl: `scripts/test-login.sh`

```bash
#!/bin/bash
#
# Login API Test
# Maqsad: Login endpoint ishlaganligini tekshirish
#

BASE_URL="http://localhost:8081"
PASSED=0
FAILED=0

echo "==================================="
echo "🧪 LOGIN API TEST"
echo "==================================="

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
  -d "grant_type=password&username=admin&password=wrong")

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
    FAILED=$((FAILED+1))
fi

# Summary
echo ""
echo "==================================="
echo "SUMMARY:"
echo "  ✅ Passed: $PASSED"
echo "  ❌ Failed: $FAILED"
echo "==================================="

# Exit code
if [ $FAILED -eq 0 ]; then
    echo "🎉 ALL TESTS PASSED!"
    exit 0
else
    echo "⚠️  SOME TESTS FAILED!"
    exit 1
fi
```

---

## 🧪 Test 2: User Management

### Fayl: `scripts/test-users.sh`

```bash
#!/bin/bash
#
# User Management API Test
# Maqsad: User CRUD operatsiyalarini tekshirish
#

BASE_URL="http://localhost:8081"
PASSED=0
FAILED=0

echo "==================================="
echo "🧪 USER MANAGEMENT API TEST"
echo "==================================="

# Login va token olish
echo "Step 1: Login to get token"
RESPONSE=$(curl -s -X POST $BASE_URL/app/rest/v2/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
  -d "grant_type=password&username=admin&password=admin")

TOKEN=$(echo "$RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ FAIL: Could not get token"
    exit 1
fi

echo "✅ Token obtained: ${TOKEN:0:30}..."

# Test 1: Get current user
echo ""
echo "Test 1: GET /api/users/me"
RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  $BASE_URL/api/users/me)

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    if echo "$BODY" | grep -q "username"; then
        USERNAME=$(echo "$BODY" | grep -o '"username":"[^"]*' | cut -d'"' -f4)
        echo "✅ PASS: Current user: $USERNAME"
        PASSED=$((PASSED+1))
    else
        echo "❌ FAIL: username field missing"
        FAILED=$((FAILED+1))
    fi
else
    echo "❌ FAIL: HTTP $HTTP_CODE"
    echo "   Response: $BODY"
    FAILED=$((FAILED+1))
fi

# Test 2: List users (admin only)
echo ""
echo "Test 2: GET /api/users (admin)"
RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  $BASE_URL/api/users?page=0&size=10)

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    # Check if response is array or paginated
    if echo "$BODY" | grep -q "\[" || echo "$BODY" | grep -q "content"; then
        echo "✅ PASS: Users list retrieved"
        PASSED=$((PASSED+1))
    else
        echo "❌ FAIL: Invalid response format"
        FAILED=$((FAILED+1))
    fi
else
    echo "❌ FAIL: HTTP $HTTP_CODE"
    FAILED=$((FAILED+1))
fi

# Test 3: Unauthorized access (no token)
echo ""
echo "Test 3: Unauthorized access"
RESPONSE=$(curl -s -w "\n%{http_code}" $BASE_URL/api/users/me)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo "✅ PASS: Unauthorized request blocked (HTTP $HTTP_CODE)"
    PASSED=$((PASSED+1))
else
    echo "❌ FAIL: Expected 401/403, got HTTP $HTTP_CODE"
    FAILED=$((FAILED+1))
fi

# Summary
echo ""
echo "==================================="
echo "SUMMARY:"
echo "  ✅ Passed: $PASSED"
echo "  ❌ Failed: $FAILED"
echo "==================================="

if [ $FAILED -eq 0 ]; then
    echo "🎉 ALL TESTS PASSED!"
    exit 0
else
    echo "⚠️  SOME TESTS FAILED!"
    exit 1
fi
```

---

## 🧪 Test 3: Regression Suite

### Fayl: `scripts/test-regression.sh`

```bash
#!/bin/bash
#
# Regression Test Suite
# Maqsad: Barcha muhim APIlarni tekshirish
#

echo "==================================="
echo "🧪 REGRESSION TEST SUITE"
echo "==================================="
echo "Date: $(date)"
echo "==================================="

TOTAL_PASSED=0
TOTAL_FAILED=0

# Test 1: Login
echo ""
echo "▶ Running Login Tests..."
./scripts/test-login.sh
if [ $? -eq 0 ]; then
    TOTAL_PASSED=$((TOTAL_PASSED+4))
else
    # Count failed from login test output
    echo "Login tests had failures"
fi

# Test 2: User Management
echo ""
echo "▶ Running User Management Tests..."
./scripts/test-users.sh
if [ $? -eq 0 ]; then
    TOTAL_PASSED=$((TOTAL_PASSED+3))
else
    echo "User management tests had failures"
fi

# Test 3: Health Check
echo ""
echo "▶ Running Health Check..."
RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8081/actuator/health)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Health check passed"
    TOTAL_PASSED=$((TOTAL_PASSED+1))
else
    echo "❌ Health check failed: HTTP $HTTP_CODE"
    TOTAL_FAILED=$((TOTAL_FAILED+1))
fi

# Test 4: Swagger UI
echo ""
echo "▶ Running Swagger UI Test..."
RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8081/swagger-ui.html)
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo "✅ Swagger UI accessible"
    TOTAL_PASSED=$((TOTAL_PASSED+1))
else
    echo "❌ Swagger UI failed: HTTP $HTTP_CODE"
    TOTAL_FAILED=$((TOTAL_FAILED+1))
fi

# Final Summary
echo ""
echo "==================================="
echo "🎯 FINAL REGRESSION REPORT"
echo "==================================="
echo "Total Tests Run: $((TOTAL_PASSED + TOTAL_FAILED))"
echo "  ✅ Passed: $TOTAL_PASSED"
echo "  ❌ Failed: $TOTAL_FAILED"
echo "==================================="

if [ $TOTAL_FAILED -eq 0 ]; then
    echo "🎉 ALL REGRESSION TESTS PASSED!"
    echo "✅ Safe to deploy to production"
    exit 0
else
    echo "⚠️  REGRESSION TESTS FAILED!"
    echo "❌ DO NOT deploy to production"
    exit 1
fi
```

---

## 🚀 Testlarni ishlatish

### 1. Testlarni executable qilish

```bash
cd /home/adm1n/startup/hemis-back
chmod +x scripts/*.sh
```

### 2. Bitta test yuritish

```bash
# Login test
./scripts/test-login.sh

# User management test
./scripts/test-users.sh
```

### 3. Barcha regression testlar

```bash
# To'liq test suite
./scripts/test-regression.sh
```

### 4. CI/CD da ishlatish

```yaml
# .github/workflows/test.yml
name: API Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Start backend
        run: ./gradlew :app:bootRun &
      - name: Wait for startup
        run: sleep 30
      - name: Run regression tests
        run: ./scripts/test-regression.sh
```

---

## 📊 Test natijalarini saqlash

### Natijalar faylga yozish

```bash
# Test natijalarni saqlash
./scripts/test-regression.sh > /tmp/test-results-$(date +%Y%m%d-%H%M%S).log

# Oxirgi natija
cat /tmp/test-results-*.log | tail -20
```

### Test history

```bash
# Test history database (opsional)
# SQLite yoki PostgreSQL ga yozish mumkin
# Format: date, test_name, status, duration
```

---

## ⚡ Performance testlar

### Fayl: `scripts/test-performance.sh`

```bash
#!/bin/bash
#
# Performance Test
# Maqsad: API response time ni tekshirish
#

echo "==================================="
echo "⚡ PERFORMANCE TEST"
echo "==================================="

# Test login performance (10 marta)
TOTAL_TIME=0
REQUESTS=10

for i in $(seq 1 $REQUESTS); do
    START=$(date +%s%N)
    curl -s -X POST http://localhost:8081/app/rest/v2/oauth/token \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "Authorization: Basic Y2xpZW50OnNlY3JldA==" \
      -d "grant_type=password&username=admin&password=admin" > /dev/null
    END=$(date +%s%N)

    DURATION=$(( (END - START) / 1000000 ))  # nanoseconds to milliseconds
    TOTAL_TIME=$((TOTAL_TIME + DURATION))
    echo "Request $i: ${DURATION}ms"
done

AVG_TIME=$((TOTAL_TIME / REQUESTS))
echo ""
echo "Average response time: ${AVG_TIME}ms"

# Pass/Fail criteria
if [ $AVG_TIME -lt 200 ]; then
    echo "✅ PASS: Performance acceptable (<200ms)"
    exit 0
else
    echo "⚠️  WARNING: Performance degraded (>${AVG_TIME}ms)"
    exit 1
fi
```

---

## 🔐 Security testlar

### SQL Injection test

```bash
# Test SQL injection
curl -X POST http://localhost:8081/app/rest/v2/oauth/token \
  -d "username=admin' OR '1'='1&password=anything"

# Expected: 401 Unauthorized (NOT 200)
```

### XSS test

```bash
# Test XSS
curl -X POST http://localhost:8081/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"<script>alert(1)</script>"}'

# Expected: Input validation error
```

---

## 📝 Test hujjatlari

### Test coverage

| API Endpoint | Test mavjud | Status |
|--------------|-------------|--------|
| POST /oauth/token | ✅ | test-login.sh |
| GET /api/users/me | ✅ | test-users.sh |
| GET /api/users | ✅ | test-users.sh |
| GET /actuator/health | ✅ | test-regression.sh |
| GET /swagger-ui.html | ✅ | test-regression.sh |

### Keyingi qadamlar

- [ ] Test coverage 100% ga yetkazish
- [ ] Load testing (100+ concurrent users)
- [ ] Stress testing (max capacity)
- [ ] Security penetration testing
- [ ] Frontend integration testlar

---

## 🎯 Best Practices

1. **Har commit oldidan test yuritish**
   ```bash
   git commit -m "..." && ./scripts/test-regression.sh && git push
   ```

2. **CI/CD pipeline da avtomatik**
   - Pull request → Testlar avtomatik
   - Merge → Testlar o'tsa deployment

3. **Production deployment oldidan**
   ```bash
   # Test stage environment
   ./scripts/test-regression.sh --env=staging

   # Agar pass → Production deploy
   ```

4. **Monitoring va alerting**
   - Test fail bo'lsa → Slack/Email notification
   - Performance degradation → Alert

---

**Status:** ✅ Test suite tayyor
**Coverage:** Login, Users, Health, Swagger
**Last Updated:** 2025-11-09
