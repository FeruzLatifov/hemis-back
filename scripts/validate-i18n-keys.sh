#!/bin/bash

###############################################################################
# I18n Key Validator - Build-time Differential Check
#
# PURPOSE:
#   Validates that all i18n keys referenced in code exist in properties files
#   and identifies unused keys in properties files.
#
# USAGE:
#   ./scripts/validate-i18n-keys.sh
#
# EXIT CODES:
#   0 - All keys valid
#   1 - Missing or extra keys found
#
# INTEGRATION:
#   Add to build.gradle.kts:
#   tasks.named("build") {
#       doFirst {
#           exec {
#               commandLine("./scripts/validate-i18n-keys.sh")
#           }
#       }
#   }
###############################################################################

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${BLUE}🔍 I18n Key Validator - Build-time Check${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Directories
PROJECT_ROOT="/home/adm1n/startup/hemis-back"
MENU_CONFIG="$PROJECT_ROOT/service/src/main/java/uz/hemis/service/menu/MenuConfig.java"
I18N_DIR="$PROJECT_ROOT/service/src/main/resources/i18n"

# Temp files
TEMP_DIR="/tmp/i18n-validation"
mkdir -p "$TEMP_DIR"

CODE_KEYS="$TEMP_DIR/code_keys.txt"
PROPERTIES_KEYS="$TEMP_DIR/properties_keys.txt"
MISSING_KEYS="$TEMP_DIR/missing_keys.txt"
EXTRA_KEYS="$TEMP_DIR/extra_keys.txt"

# Extract i18n keys from MenuConfig.java
echo -e "${BLUE}📄 Extracting i18n keys from MenuConfig.java...${NC}"
grep -oP '\.i18nKey\("\K[^"]+' "$MENU_CONFIG" | sort -u > "$CODE_KEYS" || true

CODE_COUNT=$(wc -l < "$CODE_KEYS")
echo -e "   Found ${GREEN}${CODE_COUNT}${NC} unique keys in code"

# Extract keys from properties files (uz-UZ as reference)
echo -e "${BLUE}📄 Extracting keys from menu_uz.properties...${NC}"
if [ -f "$I18N_DIR/menu_uz.properties" ]; then
    # Extract keys (everything before =)
    grep -v '^#' "$I18N_DIR/menu_uz.properties" | grep -v '^$' | cut -d'=' -f1 | sort -u > "$PROPERTIES_KEYS" || true
    PROPS_COUNT=$(wc -l < "$PROPERTIES_KEYS")
    echo -e "   Found ${GREEN}${PROPS_COUNT}${NC} keys in properties"
else
    echo -e "${RED}❌ ERROR: menu_uz.properties not found${NC}"
    exit 1
fi

# Find missing keys (in code but not in properties)
echo ""
echo -e "${BLUE}🔍 Checking for missing translations...${NC}"
comm -23 "$CODE_KEYS" "$PROPERTIES_KEYS" > "$MISSING_KEYS"
MISSING_COUNT=$(wc -l < "$MISSING_KEYS")

if [ "$MISSING_COUNT" -gt 0 ]; then
    echo -e "${RED}❌ Found $MISSING_COUNT missing keys in properties files:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat "$MISSING_KEYS" | while read key; do
        echo -e "   ${RED}✗${NC} $key"
    done
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
else
    echo -e "${GREEN}✅ All code keys exist in properties${NC}"
fi

# Find extra keys (in properties but not in code)
echo ""
echo -e "${BLUE}🔍 Checking for unused translations...${NC}"
comm -13 "$CODE_KEYS" "$PROPERTIES_KEYS" > "$EXTRA_KEYS"
EXTRA_COUNT=$(wc -l < "$EXTRA_KEYS")

if [ "$EXTRA_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $EXTRA_COUNT unused keys in properties files:${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat "$EXTRA_KEYS" | head -20 | while read key; do
        echo -e "   ${YELLOW}⚠${NC}  $key"
    done
    if [ "$EXTRA_COUNT" -gt 20 ]; then
        echo -e "   ... and $((EXTRA_COUNT - 20)) more"
    fi
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
else
    echo -e "${GREEN}✅ No unused keys found${NC}"
fi

# Validate all 4 language files have same keys
echo ""
echo -e "${BLUE}🔍 Validating consistency across languages...${NC}"

LANGUAGES=("uz" "oz" "ru" "en")
INCONSISTENCY_FOUND=0

for lang in "${LANGUAGES[@]}"; do
    FILE="$I18N_DIR/menu_${lang}.properties"
    if [ -f "$FILE" ]; then
        LANG_KEYS="$TEMP_DIR/keys_${lang}.txt"
        grep -v '^#' "$FILE" | grep -v '^$' | cut -d'=' -f1 | sort -u > "$LANG_KEYS" || true
        LANG_COUNT=$(wc -l < "$LANG_KEYS")

        # Compare with uz (reference)
        DIFF_COUNT=$(comm -3 "$PROPERTIES_KEYS" "$LANG_KEYS" | wc -l)

        if [ "$DIFF_COUNT" -gt 0 ]; then
            echo -e "   ${RED}✗${NC} menu_${lang}.properties - $LANG_COUNT keys (${RED}$DIFF_COUNT differences${NC})"
            INCONSISTENCY_FOUND=1
        else
            echo -e "   ${GREEN}✓${NC} menu_${lang}.properties - $LANG_COUNT keys (consistent)"
        fi
    else
        echo -e "   ${RED}✗${NC} menu_${lang}.properties - ${RED}FILE NOT FOUND${NC}"
        INCONSISTENCY_FOUND=1
    fi
done

# Summary
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${BLUE}📊 Validation Summary${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "Code keys:         ${GREEN}${CODE_COUNT}${NC}"
echo -e "Properties keys:   ${GREEN}${PROPS_COUNT}${NC}"
echo -e "Missing keys:      ${RED}${MISSING_COUNT}${NC}"
echo -e "Unused keys:       ${YELLOW}${EXTRA_COUNT}${NC}"
echo -e "Languages checked: ${GREEN}${#LANGUAGES[@]}${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Exit with error if validation failed
if [ "$MISSING_COUNT" -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ VALIDATION FAILED: Missing translations found${NC}"
    echo -e "${YELLOW}💡 Add missing keys to all 4 language files:${NC}"
    echo "   - $I18N_DIR/menu_uz.properties"
    echo "   - $I18N_DIR/menu_oz.properties"
    echo "   - $I18N_DIR/menu_ru.properties"
    echo "   - $I18N_DIR/menu_en.properties"
    exit 1
fi

if [ "$INCONSISTENCY_FOUND" -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ VALIDATION FAILED: Language file inconsistencies found${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ I18n validation passed successfully${NC}"
echo ""

# Cleanup
rm -rf "$TEMP_DIR"

exit 0
