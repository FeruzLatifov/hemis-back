#!/bin/bash
# =====================================================
# HEMIS API Contract Test
# =====================================================
# Purpose: Verify API endpoints have not changed
# Mode: NO-BREAKING-CHANGES
#
# Usage: ./ci/api-contract-test.sh
# Exit: 0 = pass, 1 = fail (contract violation)
# =====================================================

set -e

SNAPSHOT_FILE="ci/api-contract-snapshot.json"
TEMP_SNAPSHOT="ci/api-contract-current.json"
APP_PORT=8080
APP_HOST="localhost"

# =====================================================
# Colors
# =====================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# =====================================================
# Main Logic
# =====================================================

echo ""
echo "======================================================"
echo "  HEMIS API Contract Test"
echo "======================================================"
echo ""

# Check if app module exists
if [[ ! -f "app/build.gradle.kts" ]]; then
    echo -e "${RED}❌ ERROR: App module not found${NC}"
    exit 1
fi

echo "🚀 Building application..."
./gradlew :app:build -x test > /dev/null 2>&1

if [[ $? -ne 0 ]]; then
    echo -e "${RED}❌ ERROR: Build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Build successful${NC}"
echo ""

# Start application in test mode
echo "🔧 Starting application (test mode)..."
./gradlew :app:bootRun --args='--spring.profiles.active=test --server.port=8080' > /dev/null 2>&1 &
APP_PID=$!

# Wait for application to start
echo "⏳ Waiting for application to start..."
RETRY_COUNT=0
MAX_RETRIES=30

while [[ $RETRY_COUNT -lt $MAX_RETRIES ]]; do
    if curl -s "http://${APP_HOST}:${APP_PORT}/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Application started${NC}"
        break
    fi

    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 1
done

if [[ $RETRY_COUNT -eq $MAX_RETRIES ]]; then
    echo -e "${RED}❌ ERROR: Application failed to start${NC}"
    kill $APP_PID 2>/dev/null || true
    exit 1
fi

echo ""
echo "📊 Extracting API contract..."

# Extract API mappings from actuator
curl -s "http://${APP_HOST}:${APP_PORT}/actuator/mappings" | \
    jq '.contexts.application.mappings.dispatcherServlets.dispatcherServlet[] | {
        handler: .handler,
        predicate: .predicate,
        details: .details
    }' > "$TEMP_SNAPSHOT"

# Stop application
echo "🛑 Stopping application..."
kill $APP_PID 2>/dev/null || true
wait $APP_PID 2>/dev/null || true

echo ""

# Compare with snapshot
if [[ ! -f "$SNAPSHOT_FILE" ]]; then
    echo -e "${YELLOW}⚠️  No baseline snapshot found${NC}"
    echo ""
    echo "Creating baseline snapshot..."
    cp "$TEMP_SNAPSHOT" "$SNAPSHOT_FILE"
    echo -e "${GREEN}✅ Baseline snapshot created: $SNAPSHOT_FILE${NC}"
    echo ""
    echo "This snapshot will be used for future contract validation."
    echo "Commit this file to version control."
    rm "$TEMP_SNAPSHOT"
    exit 0
fi

# Compare snapshots
if diff "$SNAPSHOT_FILE" "$TEMP_SNAPSHOT" > /dev/null; then
    echo -e "${GREEN}✅ PASSED: API contract unchanged${NC}"
    echo ""
    echo "All endpoints match the baseline snapshot."
    echo "No breaking changes detected."
    rm "$TEMP_SNAPSHOT"
    exit 0
else
    echo -e "${RED}❌ FAILED: API contract violation detected${NC}"
    echo ""
    echo "Changes detected in API endpoints:"
    echo ""
    diff "$SNAPSHOT_FILE" "$TEMP_SNAPSHOT" || true
    echo ""
    echo "======================================================"
    echo "  API Contract Violation"
    echo "======================================================"
    echo ""
    echo "The API contract has changed. This violates the"
    echo "NO-BREAKING-CHANGES guarantee."
    echo ""
    echo "Impact:"
    echo "  • 200+ universities depend on this API"
    echo "  • External integrations will break"
    echo "  • Backward compatibility violated"
    echo ""
    echo "If this change is intentional:"
    echo ""
    echo "  1. Verify it's backward-compatible:"
    echo "     - Adding optional fields: ✅ OK"
    echo "     - Adding new endpoints: ✅ OK"
    echo "     - Removing fields: ❌ FORBIDDEN"
    echo "     - Changing field types: ❌ FORBIDDEN"
    echo "     - Removing endpoints: ❌ FORBIDDEN"
    echo ""
    echo "  2. Document the change in CHANGELOG.md"
    echo ""
    echo "  3. Update snapshot (only if approved):"
    echo "     cp $TEMP_SNAPSHOT $SNAPSHOT_FILE"
    echo ""
    echo "  4. Notify all stakeholders"
    echo ""
    echo "======================================================"
    echo ""

    rm "$TEMP_SNAPSHOT"
    exit 1
fi
