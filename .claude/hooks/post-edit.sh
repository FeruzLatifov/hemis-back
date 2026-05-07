#!/bin/bash
# Post-edit hook for HEMIS Backend
# Runs after Claude edits a file. Catches common issues early.
# Hook is registered in .claude/settings.json under hooks.PostToolUse.

set -u  # treat unset vars as errors, but DO NOT exit on grep miss

# Claude Code passes hook input as JSON on stdin (PostToolUse).
# We accept either: argv[1] = file path (legacy mode) OR JSON on stdin with .tool_input.file_path
FILE="${1:-}"
if [ -z "$FILE" ] && [ ! -t 0 ]; then
    # Try to extract from JSON stdin
    INPUT="$(cat)"
    FILE="$(printf '%s' "$INPUT" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)"
fi

[ -z "$FILE" ] && exit 0
[ ! -f "$FILE" ] && exit 0

# Only check Java/SQL/YAML files
case "$FILE" in
    *.java)
        # 1. Check for forbidden Lombok @Data on JPA entity
        if grep -q "@Data" "$FILE" && grep -q "@Entity" "$FILE"; then
            echo "⚠️  WARNING: $FILE — @Data on JPA entity is forbidden (triggers N+1 via equals/hashCode)"
            echo "   Fix: Replace @Data with @Getter @Setter"
        fi

        # 2. Check for @ManyToOne without explicit LAZY (two-stage filter — POSIX-safe)
        # Stage A: lines mentioning @ManyToOne (any context — bare, with parens, with newline)
        # Stage B: drop ones that mention LAZY in the same line
        if grep "@ManyToOne" "$FILE" | grep -v "LAZY" > /dev/null 2>&1; then
            echo "⚠️  WARNING: $FILE — @ManyToOne without explicit fetch=FetchType.LAZY (default EAGER)"
            echo "   Fix: @ManyToOne(fetch = FetchType.LAZY)"
        fi
        # Same check for @OneToOne (also EAGER by default)
        if grep "@OneToOne" "$FILE" | grep -v "LAZY" > /dev/null 2>&1; then
            echo "⚠️  WARNING: $FILE — @OneToOne without explicit fetch=FetchType.LAZY (default EAGER)"
            echo "   Fix: @OneToOne(fetch = FetchType.LAZY)"
        fi

        # 3. Check for AOP annotations on private methods
        # awk approach: track lines with @Cacheable/@Transactional/@Async, then check next 5 lines for `private`
        if awk '
            /@(Cacheable|Transactional|Async)/ {seen=1; lines=0; next}
            seen && lines<5 {
                lines++
                if ($0 ~ /private[[:space:]]+[A-Za-z<]/) {found=1; exit}
                if ($0 ~ /public|protected|class/) {seen=0}
            }
            END {exit !found}
        ' "$FILE" 2>/dev/null; then
            echo "⚠️  WARNING: $FILE — Spring AOP annotation on private method (silent fail)"
            echo "   Fix: Make public OR move to separate @Service bean"
        fi

        # 4. Check for hardcoded password/secret
        if grep -E "password\s*=\s*\"[a-zA-Z0-9]{4,}\"|secret\s*=\s*\"[a-zA-Z0-9]{8,}\"" "$FILE" > /dev/null; then
            echo "🔴 BLOCKER: $FILE — Hardcoded password/secret detected"
            echo "   Fix: Use \${ENV_VAR} placeholder"
        fi

        # 5. Check for SQL string concatenation
        if grep -E "(\"SELECT|\"INSERT|\"UPDATE|\"DELETE).*\\+" "$FILE" > /dev/null; then
            echo "🔴 BLOCKER: $FILE — SQL injection risk (string concat in query)"
            echo "   Fix: Use parameter (?, :name) instead of concatenation"
        fi

        # 6. Check for PII in log statements
        if grep -E "log\\.(info|debug|warn|error).*(pinfl|password|jwt|token)" "$FILE" | grep -v "// ok\|mask\|substring" > /dev/null; then
            echo "⚠️  WARNING: $FILE — Possible PII in log statement"
            echo "   Fix: Mask PII (e.g. pinfl.substring(0,8)+'****')"
        fi

        # 7. Golden Rule #1 — new @Table(name="hemishe_*") added → run check_table_mappings.sh
        # ADR-0008: api-legacy faqat hemishe_*/sec_*; api-university/api-web yangi schema.
        if grep -E '@Table\(name[[:space:]]*=[[:space:]]*"hemishe_' "$FILE" > /dev/null 2>&1; then
            echo "ℹ️  INFO: $FILE — new @Table(hemishe_*) mapping detected"
            echo "   Required: ./scripts/check_table_mappings.sh (CLAUDE.md Golden Rule #1)"
            # Try to run if script exists and we are in repo root
            if [ -x "./scripts/check_table_mappings.sh" ] && [ -f ".env" ]; then
                ./scripts/check_table_mappings.sh 2>&1 | tail -20 || \
                    echo "   ⚠️  check_table_mappings.sh exited non-zero — manual review needed"
            fi
        fi

        # 8. Golden Rule #3 — api-legacy MUST NOT import new schema entities (ADR-0008)
        case "$FILE" in
            */api-legacy/*)
                if grep -E '^import uz\.hemis\.domain\.entity\.(security\.User|employee\.Employee|employee\.EmployeeJobs);?$' "$FILE" > /dev/null 2>&1; then
                    echo "🔴 BLOCKER: $FILE — api-legacy imports NEW SCHEMA entity (ADR-0008 violation)"
                    echo "   Fix: Use Legacy variant — SecUser, Teacher, LegacyEmployeeJobs"
                    echo "   Reference: docs/adr/0008-api-legacy-entity-rebinding.md"
                fi
                ;;
        esac
        ;;

    *.sql)
        # 9. Liquibase migration checks (V###/M###/S### naming)
        case "$FILE" in
            *changesets/*/V[0-9]*.sql|*changesets/*/M[0-9]*.sql|*changesets/*/S[0-9]*.sql)
                # Forbidden ALTER on hemishe_*
                if grep -E "ALTER TABLE.*hemishe_[eh]_|DROP TABLE.*hemishe_[eh]_|RENAME.*hemishe_[eh]_" "$FILE" > /dev/null; then
                    echo "🔴 BLOCKER: $FILE — Forbidden DDL on hemishe_* legacy table"
                    echo "   Schema is FROZEN. Use ref_ext schema for new columns."
                fi

                # Missing IF NOT EXISTS
                if grep -E "^CREATE (TABLE|INDEX) [^I]" "$FILE" | grep -v "IF NOT EXISTS" > /dev/null; then
                    echo "⚠️  WARNING: $FILE — Migration not idempotent (missing IF NOT EXISTS)"
                fi

                # Check rollback file exists
                BASE="${FILE%.sql}"
                if [ ! -f "${BASE}_rollback.sql" ]; then
                    echo "🔴 BLOCKER: $FILE — Rollback file missing: ${BASE}_rollback.sql"
                fi
                ;;
        esac
        ;;

    *application*.yml|*application*.yaml)
        # 8. Check for plain secrets in config
        if grep -E "^[[:space:]]*(password|secret|api-key|api_key):[[:space:]]*[a-zA-Z0-9]" "$FILE" | grep -v '\${' | grep -v "test\|example\|default-dev" > /dev/null; then
            echo "🔴 BLOCKER: $FILE — Plain secret in config (use \${ENV_VAR})"
        fi
        ;;
esac

exit 0
