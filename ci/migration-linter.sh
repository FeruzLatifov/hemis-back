#!/bin/bash
# =====================================================
# HEMIS Migration Linter
# =====================================================
# Purpose: Detect forbidden DDL operations in Flyway migrations
# Mode: NO-RENAME • NO-DELETE • NO-BREAKING-CHANGES
#
# Usage: ./ci/migration-linter.sh
# Exit: 0 = pass, 1 = fail (forbidden operation found)
# =====================================================

set -e

MIGRATION_DIR="domain/src/main/resources/db/migration"

# =====================================================
# Forbidden Patterns
# =====================================================
# These patterns would break replication or violate immutability

FORBIDDEN_PATTERNS=(
    "DROP\s+TABLE"
    "DROP\s+COLUMN"
    "DROP\s+INDEX"
    "DROP\s+CONSTRAINT"
    "DROP\s+SEQUENCE"
    "DROP\s+VIEW"
    "DROP\s+FUNCTION"
    "ALTER.*RENAME\s+TO"
    "ALTER.*RENAME\s+COLUMN"
    "TRUNCATE"
    "DELETE\s+FROM.*WHERE\s+1\s*=\s*1"
    "DELETE\s+FROM.*WHERE\s+TRUE"
    "UPDATE.*WHERE\s+1\s*=\s*1"
    "UPDATE.*WHERE\s+TRUE"
)

# =====================================================
# Colors for output
# =====================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# =====================================================
# Main Linting Logic
# =====================================================

echo ""
echo "======================================================"
echo "  HEMIS Migration Linter"
echo "======================================================"
echo ""
echo "🔍 Scanning Flyway migrations for forbidden operations..."
echo ""

EXIT_CODE=0
FILE_COUNT=0
VIOLATION_COUNT=0

# Check if migration directory exists
if [[ ! -d "$MIGRATION_DIR" ]]; then
    echo -e "${RED}❌ ERROR: Migration directory not found: $MIGRATION_DIR${NC}"
    exit 1
fi

# Iterate through all SQL files
for sql_file in "$MIGRATION_DIR"/*.sql; do
    if [[ ! -f "$sql_file" ]]; then
        continue
    fi

    FILE_COUNT=$((FILE_COUNT + 1))
    filename=$(basename "$sql_file")

    echo "Checking: $filename"

    VIOLATIONS_IN_FILE=0

    # Check each forbidden pattern
    for pattern in "${FORBIDDEN_PATTERNS[@]}"; do
        if grep -iE "$pattern" "$sql_file" > /dev/null 2>&1; then
            if [[ $VIOLATIONS_IN_FILE -eq 0 ]]; then
                echo -e "${RED}  ❌ VIOLATIONS FOUND:${NC}"
            fi

            echo -e "${RED}     Pattern: '$pattern'${NC}"

            # Show line numbers and content
            grep -inE "$pattern" "$sql_file" | while read -r line; do
                echo -e "${YELLOW}     $line${NC}"
            done

            VIOLATIONS_IN_FILE=$((VIOLATIONS_IN_FILE + 1))
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            EXIT_CODE=1
        fi
    done

    if [[ $VIOLATIONS_IN_FILE -eq 0 ]]; then
        echo -e "${GREEN}  ✅ OK${NC}"
    fi

    echo ""
done

# =====================================================
# Summary
# =====================================================

echo "======================================================"
echo "  Summary"
echo "======================================================"
echo ""
echo "Files scanned: $FILE_COUNT"
echo "Violations found: $VIOLATION_COUNT"
echo ""

if [[ $EXIT_CODE -eq 0 ]]; then
    echo -e "${GREEN}✅ PASSED: All migrations are safe${NC}"
    echo ""
    echo "No forbidden operations detected."
    echo "Migrations are replication-safe and backward-compatible."
else
    echo -e "${RED}❌ FAILED: Forbidden operations detected${NC}"
    echo ""
    echo "The following operations are PROHIBITED:"
    echo ""
    echo "  ❌ DROP TABLE / COLUMN / INDEX / CONSTRAINT"
    echo "  ❌ ALTER ... RENAME TO ..."
    echo "  ❌ TRUNCATE"
    echo "  ❌ DELETE FROM ... WHERE 1=1 (mass delete)"
    echo "  ❌ UPDATE ... WHERE 1=1 (mass update)"
    echo ""
    echo "These operations would:"
    echo "  • Break database replication"
    echo "  • Violate API backward compatibility"
    echo "  • Cause data loss"
    echo "  • Break 200+ university integrations"
    echo ""
    echo "Allowed operations:"
    echo "  ✅ CREATE INDEX CONCURRENTLY"
    echo "  ✅ CREATE VIEW (read-only alias)"
    echo "  ✅ COMMENT ON (documentation)"
    echo "  ✅ GRANT / REVOKE (permissions)"
    echo ""
    echo "If you need to make structural changes:"
    echo "  1. Review MASTER PROMPT constraints"
    echo "  2. Consider using updatable VIEWs for aliases"
    echo "  3. Consult with architecture team"
    echo ""
fi

echo "======================================================"
echo ""

exit $EXIT_CODE
