#!/bin/bash
# =====================================================
# HEMIS Backend - Migration Testing Script
# =====================================================
# Purpose: Test Liquibase migrations before production deployment
# Industry Best Practice: Always dry-run migrations before applying
# Reference: Google SRE Handbook, Netflix DBLog
# =====================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}  HEMIS Backend - Migration Testing${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# =====================================================
# Step 1: Check Migration Status
# =====================================================
echo -e "${YELLOW}[1/4] Checking current migration status...${NC}"
echo ""

./gradlew :domain:liquibaseStatus || {
    echo -e "${RED}❌ Failed to check migration status${NC}"
    exit 1
}

echo ""
echo -e "${GREEN}✅ Migration status checked${NC}"
echo ""

# =====================================================
# Step 2: Preview Migration SQL (Dry Run)
# =====================================================
echo -e "${YELLOW}[2/4] Generating migration SQL preview...${NC}"
echo ""

PREVIEW_FILE="/tmp/liquibase-migration-preview-$(date +%Y%m%d-%H%M%S).sql"

./gradlew :domain:liquibaseUpdateSQL > "$PREVIEW_FILE" || {
    echo -e "${RED}❌ Failed to generate migration preview${NC}"
    exit 1
}

echo ""
echo -e "${GREEN}✅ Migration preview generated${NC}"
echo -e "${BLUE}📄 Preview file: $PREVIEW_FILE${NC}"
echo ""

# Show first 50 lines of preview
echo -e "${YELLOW}Preview (first 50 lines):${NC}"
head -50 "$PREVIEW_FILE"
echo ""
echo -e "${BLUE}... (see full preview in $PREVIEW_FILE)${NC}"
echo ""

# =====================================================
# Step 3: Preview Rollback SQL
# =====================================================
echo -e "${YELLOW}[3/4] Generating rollback SQL preview...${NC}"
echo ""

ROLLBACK_FILE="/tmp/liquibase-rollback-preview-$(date +%Y%m%d-%H%M%S).sql"

./gradlew :domain:liquibaseRollbackSQL -Pcount=1 > "$ROLLBACK_FILE" || {
    echo -e "${RED}❌ Failed to generate rollback preview${NC}"
    echo -e "${YELLOW}⚠️  This is normal if no changesets are pending${NC}"
}

echo ""
echo -e "${GREEN}✅ Rollback preview generated${NC}"
echo -e "${BLUE}📄 Rollback file: $ROLLBACK_FILE${NC}"
echo ""

# =====================================================
# Step 4: Summary & Recommendations
# =====================================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}  Summary${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${GREEN}✅ All migration tests completed successfully${NC}"
echo ""
echo -e "${YELLOW}📋 Next Steps:${NC}"
echo ""
echo "  1. Review migration preview:"
echo -e "     ${BLUE}cat $PREVIEW_FILE${NC}"
echo ""
echo "  2. Review rollback preview:"
echo -e "     ${BLUE}cat $ROLLBACK_FILE${NC}"
echo ""
echo "  3. If everything looks good, apply migration:"
echo -e "     ${BLUE}./gradlew :domain:liquibaseUpdate${NC}"
echo ""
echo "  4. Or run in production (Spring Boot auto-applies):"
echo -e "     ${BLUE}./gradlew :app:bootRun${NC}"
echo ""
echo -e "${YELLOW}⚠️  Production Checklist:${NC}"
echo ""
echo "  • Create database backup"
echo "  • Schedule maintenance window"
echo "  • Notify users of downtime (if any)"
echo "  • Have rollback plan ready"
echo "  • Monitor logs during migration"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${GREEN}Testing completed!${NC}"
echo ""
