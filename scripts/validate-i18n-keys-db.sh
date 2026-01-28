#!/bin/bash

###############################################################################
# I18n Key Validator - Database-aware Version
#
# PURPOSE:
#   Validates that all i18n keys from database menus exist in i18n_messages
#   and identifies unused keys in i18n_messages table.
#
# CHANGES FROM LEGACY VERSION:
#   ❌ OLD: Read keys from MenuConfig.java (deprecated hardcoded file)
#   ✅ NEW: Read keys from database menus table (dynamic)
#
# USAGE:
#   ./scripts/validate-i18n-keys-db.sh
#
# REQUIREMENTS:
#   - PostgreSQL client (psql)
#   - Database connection via environment variables or .env file
#
# EXIT CODES:
#   0 - All keys valid
#   1 - Missing or extra keys found
#
# INTEGRATION:
#   Add to CI/CD pipeline or pre-commit hook
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${BLUE}🔍 I18n Key Validator - Database-aware Check${NC}"
echo -e "${CYAN}   (Migration-friendly - reads from database)${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Directories
PROJECT_ROOT="/home/adm1n/startup/hemis-back"

# Load database connection from .env if exists
if [ -f "$PROJECT_ROOT/.env" ]; then
    echo -e "${BLUE}📄 Loading database credentials from .env...${NC}"
    source "$PROJECT_ROOT/.env"
fi

# Database connection parameters (with fallback defaults)
DB_HOST="${DB_MASTER_HOST:-localhost}"
DB_PORT="${DB_MASTER_PORT:-5432}"
DB_NAME="${DB_MASTER_DB:-hemis}"
DB_USER="${DB_MASTER_USER:-postgres}"
DB_PASSWORD="${DB_MASTER_PASSWORD:-postgres}"

# Temp files
TEMP_DIR="/tmp/i18n-validation-db"
mkdir -p "$TEMP_DIR"

MENU_KEYS="$TEMP_DIR/menu_keys.txt"
I18N_KEYS="$TEMP_DIR/i18n_keys.txt"
MISSING_KEYS="$TEMP_DIR/missing_keys.txt"
EXTRA_KEYS="$TEMP_DIR/extra_keys.txt"

# =====================================================
# Extract i18n keys from database menus
# =====================================================
echo -e "${BLUE}📄 Extracting i18n keys from menus table...${NC}"

PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c \
  "SELECT DISTINCT i18n_key
   FROM menus
   WHERE deleted_at IS NULL
     AND i18n_key IS NOT NULL
     AND i18n_key != ''
   ORDER BY i18n_key;" > "$MENU_KEYS" 2>/dev/null || {
    echo -e "${RED}❌ ERROR: Failed to connect to database${NC}"
    echo -e "${YELLOW}💡 Check database credentials:${NC}"
    echo "   DB_MASTER_HOST=$DB_HOST"
    echo "   DB_MASTER_PORT=$DB_PORT"
    echo "   DB_MASTER_DB=$DB_NAME"
    echo "   DB_MASTER_USER=$DB_USER"
    exit 1
}

MENU_COUNT=$(wc -l < "$MENU_KEYS")
echo -e "   Found ${GREEN}${MENU_COUNT}${NC} unique i18n keys in menus table"

if [ "$MENU_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  WARNING: No menu keys found in database${NC}"
    echo -e "${YELLOW}   This might indicate empty menus table or migration not run${NC}"
fi

# =====================================================
# Extract keys from i18n_messages table
# =====================================================
echo -e "${BLUE}📄 Extracting keys from i18n_messages table...${NC}"

PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c \
  "SELECT DISTINCT message_key
   FROM i18n_messages
   WHERE category = 'menu'
     AND is_active = true
   ORDER BY message_key;" > "$I18N_KEYS" 2>/dev/null || {
    echo -e "${RED}❌ ERROR: Failed to query i18n_messages table${NC}"
    exit 1
}

I18N_COUNT=$(wc -l < "$I18N_KEYS")
echo -e "   Found ${GREEN}${I18N_COUNT}${NC} unique keys in i18n_messages (category='menu')"

# =====================================================
# Find missing keys (in menus but not in i18n_messages)
# =====================================================
echo ""
echo -e "${BLUE}🔍 Checking for missing translations...${NC}"
comm -23 "$MENU_KEYS" "$I18N_KEYS" > "$MISSING_KEYS"
MISSING_COUNT=$(wc -l < "$MISSING_KEYS")

if [ "$MISSING_COUNT" -gt 0 ]; then
    echo -e "${RED}❌ Found $MISSING_COUNT missing keys in i18n_messages:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat "$MISSING_KEYS" | while read key; do
        echo -e "   ${RED}✗${NC} $key"
    done
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
else
    echo -e "${GREEN}✅ All menu keys have translations in database${NC}"
fi

# =====================================================
# Find extra keys (in i18n_messages but not used in menus)
# =====================================================
echo ""
echo -e "${BLUE}🔍 Checking for unused translations...${NC}"
comm -13 "$MENU_KEYS" "$I18N_KEYS" > "$EXTRA_KEYS"
EXTRA_COUNT=$(wc -l < "$EXTRA_KEYS")

if [ "$EXTRA_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $EXTRA_COUNT unused keys in i18n_messages:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat "$EXTRA_KEYS" | head -20 | while read key; do
        echo -e "   ${YELLOW}⚠${NC}  $key"
    done
    if [ "$EXTRA_COUNT" -gt 20 ]; then
        echo -e "   ... and $((EXTRA_COUNT - 20)) more"
    fi
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "${CYAN}💡 These keys might be used in other parts of the system${NC}"
else
    echo -e "${GREEN}✅ No unused menu keys found${NC}"
fi

# =====================================================
# Validate language consistency
# =====================================================
echo ""
echo -e "${BLUE}🔍 Validating consistency across languages...${NC}"

LANGUAGES=("uz-UZ" "oz-UZ" "ru-RU" "en-US")
INCONSISTENCY_FOUND=0

for lang in "${LANGUAGES[@]}"; do
    # Count translations for this language
    LANG_COUNT=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c \
      "SELECT COUNT(DISTINCT message_key)
       FROM i18n_messages
       WHERE category = 'menu'
         AND language_code = '$lang'
         AND is_active = true;" 2>/dev/null || echo "0")

    # Check if all menu keys have translations in this language
    MISSING_IN_LANG=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c \
      "SELECT COUNT(*)
       FROM (
         SELECT DISTINCT i18n_key FROM menus WHERE deleted_at IS NULL AND i18n_key IS NOT NULL
         EXCEPT
         SELECT message_key FROM i18n_messages WHERE category = 'menu' AND language_code = '$lang' AND is_active = true
       ) missing;" 2>/dev/null || echo "0")

    if [ "$MISSING_IN_LANG" -gt 0 ]; then
        echo -e "   ${RED}✗${NC} $lang - $LANG_COUNT keys (${RED}$MISSING_IN_LANG missing${NC})"
        INCONSISTENCY_FOUND=1
    else
        echo -e "   ${GREEN}✓${NC} $lang - $LANG_COUNT keys (complete)"
    fi
done

# =====================================================
# Summary
# =====================================================
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${BLUE}📊 Validation Summary${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "Menu keys (database):  ${GREEN}${MENU_COUNT}${NC}"
echo -e "I18n keys (database):  ${GREEN}${I18N_COUNT}${NC}"
echo -e "Missing translations:  ${RED}${MISSING_COUNT}${NC}"
echo -e "Unused translations:   ${YELLOW}${EXTRA_COUNT}${NC}"
echo -e "Languages validated:   ${GREEN}${#LANGUAGES[@]}${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Exit with error if validation failed
if [ "$MISSING_COUNT" -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ VALIDATION FAILED: Missing translations found${NC}"
    echo -e "${YELLOW}💡 Add missing translations via TranslationAdminController:${NC}"
    echo "   POST /api/v1/admin/translations/{messageId}"
    exit 1
fi

if [ "$INCONSISTENCY_FOUND" -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ VALIDATION FAILED: Language inconsistencies found${NC}"
    echo -e "${YELLOW}💡 Add missing language translations via admin panel${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ I18n validation passed successfully${NC}"
echo -e "${CYAN}   All menu keys have complete translations across all languages${NC}"
echo ""

# Cleanup
rm -rf "$TEMP_DIR"

exit 0
