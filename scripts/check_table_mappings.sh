#!/usr/bin/env bash
# ════════════════════════════════════════════════════════════════════
# check_table_mappings.sh — JPA @Table mapping vs DB introspection
#
# Maqsad: kod'da @Table(name = "hemishe_e_*") deb yozilgan jadvallar
# haqiqatan ham bizning bazada (test1_hemis) mavjudligini tekshirish.
# OTM bazalari (Univer hemis_NNN) bilan chalkashtirib qo'yilgan
# entity'larni topish uchun.
#
# Ishlatish:
#   ./scripts/check_table_mappings.sh
#
# Output:
#   - "Code'da bor, DB'da YO'Q" — bu xato! Entity olib tashlanishi kerak
#   - "DB'da bor, code'da yo'q" — informational (uy vazifasi emas)
# ════════════════════════════════════════════════════════════════════

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

if [[ ! -f .env ]]; then
    echo "ERROR: .env not found in $PROJECT_ROOT" >&2
    exit 1
fi

# .env'dan DB connection variable'larini yuklash
set -a
# shellcheck disable=SC1091
. .env 2>/dev/null || true
set +a

: "${DB_MASTER_HOST:?DB_MASTER_HOST not set}"
: "${DB_MASTER_PORT:?DB_MASTER_PORT not set}"
: "${DB_MASTER_NAME:?DB_MASTER_NAME not set}"
: "${DB_MASTER_USERNAME:?DB_MASTER_USERNAME not set}"
: "${DB_MASTER_PASSWORD:?DB_MASTER_PASSWORD not set}"

DB_TABLES=$(mktemp)
CODE_TABLES=$(mktemp)
trap 'rm -f "$DB_TABLES" "$CODE_TABLES"' EXIT

# 1. DB'dagi haqiqiy hemishe_* jadvallar
PGPASSWORD="$DB_MASTER_PASSWORD" psql \
    -h "$DB_MASTER_HOST" -p "$DB_MASTER_PORT" \
    -U "$DB_MASTER_USERNAME" -d "$DB_MASTER_NAME" \
    -tAc "SELECT table_name FROM information_schema.tables
          WHERE table_schema='public'
            AND table_name LIKE 'hemishe_%'
          ORDER BY table_name" 2>/dev/null \
    | sort -u > "$DB_TABLES"

# 2. Kod'dagi @Table mapping'lar
grep -rh "@Table(name = \"hemishe_" \
    --include="*.java" \
    domain/src/main/java/uz/hemis/domain/entity 2>/dev/null \
    | sed 's/.*name = "\([^"]*\)".*/\1/' \
    | sort -u > "$CODE_TABLES"

DB_COUNT=$(wc -l < "$DB_TABLES")
CODE_COUNT=$(wc -l < "$CODE_TABLES")

echo "════════════════════════════════════════════════════════════════"
echo "JPA @Table vs DB ($DB_MASTER_NAME) introspection"
echo "════════════════════════════════════════════════════════════════"
echo "DB'da hemishe_*  jadvallar:    $DB_COUNT"
echo "Kod'da @Table(hemishe_*):       $CODE_COUNT"
echo "════════════════════════════════════════════════════════════════"

# 3. Code'da bor, DB'da YO'Q — XATO
MISSING=$(comm -23 "$CODE_TABLES" "$DB_TABLES")
if [[ -n "$MISSING" ]]; then
    echo
    echo "❌ ENTITY MAVJUD, JADVAL DB'DA YO'Q (cleanup kerak):"
    echo "─────────────────────────────────────────────────────"
    echo "$MISSING" | while read -r table; do
        # Mapping'ni qaysi entity'da topish
        entity=$(grep -rl "@Table(name = \"$table\")" \
            --include="*.java" \
            domain/src/main/java/uz/hemis/domain/entity 2>/dev/null \
            | head -1 | sed 's|.*/||')
        echo "  $table  →  $entity"
    done
    echo
    echo "Bu entity'lar Univer (OTM hemis_NNN) bazasiga tegishli yoki"
    echo "noto'g'ri jadval nomi bilan map qilingan. CLAUDE.md ga qarang."
    EXIT_CODE=1
else
    echo
    echo "✅ Hamma entity mapping'lar DB bilan mos."
    EXIT_CODE=0
fi

# 5. api-legacy GOLDEN RULE — faqat eski (hemishe_*, sec_*) jadvallar
echo
echo "════════════════════════════════════════════════════════════════"
echo "api-legacy → faqat eski jadvallar (hemishe_*, sec_*)"
echo "════════════════════════════════════════════════════════════════"
LEGACY_VIOLATIONS=$(
    grep -rh "import uz\.hemis\.domain\.entity\." \
        --include="*.java" \
        api-legacy/src/main/java/uz/hemis/api/legacy/controller 2>/dev/null \
        | sed 's/import uz\.hemis\.domain\.entity\.[a-z]*\.\([A-Za-z]*\);.*/\1/' \
        | sort -u \
        | while read entity; do
            ENTITY_FILE=$(find domain/src/main/java -name "${entity}.java" -not -path "*/build/*" 2>/dev/null | head -1)
            [ -z "$ENTITY_FILE" ] && continue
            TABLE=$(grep '@Table' "$ENTITY_FILE" 2>/dev/null | sed 's/.*name = "\([^"]*\)".*/\1/' | head -1)
            if [ -n "$TABLE" ] && [[ ! "$TABLE" =~ ^(hemishe_|sec_) ]]; then
                # Qaysi controller ishlatadi
                CALLERS=$(grep -rln "import uz\.hemis\.domain\.entity\.[a-z]*\.${entity};" api-legacy/src/main/java 2>/dev/null | head -3 | xargs -n1 basename 2>/dev/null | tr '\n' ',' | sed 's/,$//')
                echo "  $entity → $TABLE  ($CALLERS)"
            fi
        done
)

if [[ -n "$LEGACY_VIOLATIONS" ]]; then
    echo
    echo "❌ api-legacy YANGI JADVAL (modern schema) ENTITY'LARNI ISHLATAYAPTI:"
    echo "─────────────────────────────────────────────────────"
    echo "$LEGACY_VIOLATIONS"
    echo
    echo "GOLDEN RULE: api-legacy faqat hemishe_*, sec_* jadvallar bilan ishlashi kerak."
    echo "api-web/api-university/api-external — yangi jadvallar uchun."
    echo "Tafsilot: api-legacy/CLAUDE.md → 'GOLDEN RULE' bo'limi."
    EXIT_CODE=1
else
    echo "✅ api-legacy faqat eski jadvallar bilan ishlaydi."
fi

# 4. DB'da bor, code'da yo'q (informational)
EXTRA=$(comm -13 "$CODE_TABLES" "$DB_TABLES")
if [[ -n "$EXTRA" ]]; then
    echo
    echo "ℹ️  DB'da bor, code'da map qilinmagan (OK — kerak bo'lsa entity yarating):"
    echo "─────────────────────────────────────────────────────"
    echo "$EXTRA" | sed 's/^/  /'
fi

exit $EXIT_CODE
