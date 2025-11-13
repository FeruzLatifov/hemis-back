#!/bin/bash
# I18n Key Validator - Build-time Check

PROJECT_ROOT="/home/adm1n/startup/hemis-back"
MENU_CONFIG="$PROJECT_ROOT/service/src/main/java/uz/hemis/service/menu/MenuConfig.java"
I18N_DIR="$PROJECT_ROOT/service/src/main/resources/i18n"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 I18n Key Validator - Build-time Check"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Extract i18n keys from MenuConfig.java
echo "📄 Extracting i18n keys from MenuConfig.java..."
CODE_KEYS=$(grep -oP '\.i18nKey\("\K[^"]+' "$MENU_CONFIG" | sort -u)
CODE_COUNT=$(echo "$CODE_KEYS" | wc -l)
echo "   Found ${CODE_COUNT} unique keys in code"

# Extract keys from properties files
echo "📄 Extracting keys from menu_uz.properties..."
PROPS_KEYS=$(grep -v '^#' "$I18N_DIR/menu_uz.properties" | grep -v '^$' | cut -d'=' -f1 | sort -u)
PROPS_COUNT=$(echo "$PROPS_KEYS" | wc -l)
echo "   Found ${PROPS_COUNT} keys in properties"

# Find missing keys
echo ""
echo "🔍 Checking for missing translations..."
MISSING_KEYS=$(comm -23 <(echo "$CODE_KEYS") <(echo "$PROPS_KEYS"))
MISSING_COUNT=$(echo "$MISSING_KEYS" | grep -c '.' || echo "0")

if [ "$MISSING_COUNT" -gt 0 ]; then
    echo "❌ Found $MISSING_COUNT missing keys:"
    echo "$MISSING_KEYS" | head -10
else
    echo "✅ All code keys exist in properties"
fi

# Find extra keys
echo ""
echo "🔍 Checking for unused translations..."
EXTRA_KEYS=$(comm -13 <(echo "$CODE_KEYS") <(echo "$PROPS_KEYS"))
EXTRA_COUNT=$(echo "$EXTRA_KEYS" | grep -c '.' || echo "0")

if [ "$EXTRA_COUNT" -gt 0 ]; then
    echo "⚠️  Found $EXTRA_COUNT unused keys (showing first 10):"
    echo "$EXTRA_KEYS" | head -10
else
    echo "✅ No unused keys found"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Summary: $CODE_COUNT code keys, $PROPS_COUNT property keys"
echo "   Missing: $MISSING_COUNT, Unused: $EXTRA_COUNT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ "$MISSING_COUNT" -gt 0 ]; then
    exit 1
fi
exit 0
