#!/bin/bash
# Post-edit hook for HEMIS Backend
# Runs after Claude edits a file. Catches common issues early.

set -e

FILE="$1"
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

        # 2. Check for @ManyToOne without explicit LAZY
        if grep -E "@ManyToOne[[:space:]]*$|@ManyToOne[[:space:]]*\\(" "$FILE" | grep -v "FetchType.LAZY" > /dev/null 2>&1; then
            if grep -E "@ManyToOne[[:space:]]*\\((?!.*LAZY)" "$FILE" > /dev/null 2>&1; then
                echo "⚠️  WARNING: $FILE — @ManyToOne without explicit fetch=FetchType.LAZY (default EAGER)"
            fi
        fi

        # 3. Check for @Cacheable on private method
        if grep -B 1 "private " "$FILE" | grep -q "@Cacheable\|@Transactional\|@Async"; then
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
        ;;

    *.sql)
        # 7. Liquibase migration checks
        case "$FILE" in
            *changesets/*V*.sql|*changesets/*M*.sql)
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
