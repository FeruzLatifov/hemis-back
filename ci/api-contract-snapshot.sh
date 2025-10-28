#!/bin/bash

# =====================================================
# API Contract Snapshot Generator
# =====================================================
# Purpose: Extract all REST endpoints and generate snapshot
# Usage: ./api-contract-snapshot.sh [generate|verify]
# =====================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SNAPSHOT_FILE="$SCRIPT_DIR/api-contract-snapshot.json"
CURRENT_SNAPSHOT="/tmp/api-contract-current.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# =====================================================
# Functions
# =====================================================

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Extract API endpoints from Spring Boot application
extract_endpoints() {
    local output_file=$1

    log_info "Extracting API endpoints from Spring Boot application..."

    # Build the application
    cd "$PROJECT_ROOT"
    ./gradlew :app:build -x test > /dev/null 2>&1 || {
        log_error "Failed to build application"
        return 1
    }

    # Start the application in background
    log_info "Starting application..."
    java -jar app/build/libs/app-*.jar \
        --spring.profiles.active=test \
        --server.port=9999 \
        --spring.datasource.url=jdbc:h2:mem:testdb \
        --spring.jpa.hibernate.ddl-auto=create-drop \
        > /tmp/app.log 2>&1 &

    APP_PID=$!

    # Wait for application to start
    log_info "Waiting for application to start (PID: $APP_PID)..."
    for i in {1..30}; do
        if curl -s http://localhost:9999/actuator/health > /dev/null 2>&1; then
            log_info "Application started successfully"
            break
        fi
        sleep 2

        if [ $i -eq 30 ]; then
            log_error "Application failed to start within 60 seconds"
            kill $APP_PID 2>/dev/null || true
            return 1
        fi
    done

    # Extract endpoints from Spring Boot Actuator mappings
    log_info "Extracting endpoint mappings..."
    curl -s http://localhost:9999/actuator/mappings | jq '{
        "timestamp": now | todate,
        "endpoints": [
            .contexts[].mappings.dispatcherServlets.dispatcherServlet[]
            | select(.predicate != null)
            | {
                "pattern": .predicate | match("\\{([^}]+)\\}") | .captures[0].string,
                "methods": (.details.requestMappingConditions.methods // ["ANY"]),
                "consumes": (.details.requestMappingConditions.consumes // []),
                "produces": (.details.requestMappingConditions.produces // []),
                "params": (.details.requestMappingConditions.params // []),
                "headers": (.details.requestMappingConditions.headers // []),
                "handler": .details.handlerMethod.className + "#" + .details.handlerMethod.name
            }
        ] | sort_by(.pattern, .methods[0])
    }' > "$output_file"

    # Stop the application
    log_info "Stopping application (PID: $APP_PID)..."
    kill $APP_PID 2>/dev/null || true
    wait $APP_PID 2>/dev/null || true

    log_info "Endpoints extracted to: $output_file"

    # Display endpoint count
    local endpoint_count=$(jq '.endpoints | length' "$output_file")
    log_info "Total endpoints found: $endpoint_count"
}

# Generate baseline snapshot
generate_baseline() {
    log_info "Generating baseline API contract snapshot..."

    extract_endpoints "$SNAPSHOT_FILE" || return 1

    log_info "Baseline snapshot saved to: $SNAPSHOT_FILE"
    log_info ""
    log_info "Baseline endpoints:"
    jq -r '.endpoints[] | "\(.methods[0]) \(.pattern)"' "$SNAPSHOT_FILE"

    return 0
}

# Verify current API against baseline
verify_contract() {
    log_info "Verifying API contract against baseline..."

    if [ ! -f "$SNAPSHOT_FILE" ]; then
        log_error "Baseline snapshot not found: $SNAPSHOT_FILE"
        log_error "Run './api-contract-snapshot.sh generate' first to create baseline"
        return 1
    fi

    # Extract current endpoints
    extract_endpoints "$CURRENT_SNAPSHOT" || return 1

    # Compare snapshots
    log_info "Comparing current API with baseline..."

    local breaking_changes=0

    # Check for removed endpoints
    log_info "Checking for removed endpoints..."
    local removed=$(jq -r --slurpfile current "$CURRENT_SNAPSHOT" '
        .endpoints[]
        | select(
            . as $baseline
            | $current[0].endpoints
            | map(select(.pattern == $baseline.pattern and .methods == $baseline.methods))
            | length == 0
        )
        | "\(.methods[0]) \(.pattern)"
    ' "$SNAPSHOT_FILE")

    if [ -n "$removed" ]; then
        log_error "❌ BREAKING CHANGE: Removed endpoints detected:"
        echo "$removed" | while read line; do
            echo "  - $line"
        done
        breaking_changes=$((breaking_changes + 1))
    fi

    # Check for modified endpoints (different handler)
    log_info "Checking for modified endpoint handlers..."
    local modified=$(jq -r --slurpfile current "$CURRENT_SNAPSHOT" '
        .endpoints[]
        | . as $baseline
        | $current[0].endpoints[]
        | select(.pattern == $baseline.pattern and .methods == $baseline.methods and .handler != $baseline.handler)
        | "MODIFIED: \(.methods[0]) \(.pattern)\n  Old: \($baseline.handler)\n  New: \(.handler)"
    ' "$SNAPSHOT_FILE")

    if [ -n "$modified" ]; then
        log_warn "⚠️  Modified endpoint handlers:"
        echo "$modified"
    fi

    # Check for new endpoints (informational only)
    log_info "Checking for new endpoints..."
    local added=$(jq -r --slurpfile baseline "$SNAPSHOT_FILE" '
        .endpoints[]
        | select(
            . as $current
            | $baseline[0].endpoints
            | map(select(.pattern == $current.pattern and .methods == $current.methods))
            | length == 0
        )
        | "\(.methods[0]) \(.pattern)"
    ' "$CURRENT_SNAPSHOT")

    if [ -n "$added" ]; then
        log_info "✅ New endpoints added (not breaking):"
        echo "$added" | while read line; do
            echo "  + $line"
        done
    fi

    # Summary
    echo ""
    if [ $breaking_changes -eq 0 ]; then
        log_info "✅ API CONTRACT VERIFICATION PASSED"
        log_info "No breaking changes detected"
        return 0
    else
        log_error "❌ API CONTRACT VERIFICATION FAILED"
        log_error "Breaking changes detected: $breaking_changes"
        return 1
    fi
}

# Display help
show_help() {
    cat << EOF
API Contract Snapshot Tool

Usage:
  ./api-contract-snapshot.sh [command]

Commands:
  generate    Generate baseline API contract snapshot
  verify      Verify current API against baseline snapshot
  help        Show this help message

Examples:
  # Generate baseline (run once after implementing endpoints)
  ./api-contract-snapshot.sh generate

  # Verify contract before deployment
  ./api-contract-snapshot.sh verify

CRITICAL:
  - Baseline snapshot must exist before verification
  - Removed endpoints = BREAKING CHANGE (fails CI)
  - Modified handlers = WARNING (manual review needed)
  - New endpoints = OK (backward compatible)

Legacy API Contract:
  - /app/rest/v2/students (GET, POST, PUT, PATCH)
  - NO DELETE endpoint allowed (NDG)
  - JSON field names must preserve underscores (_university, _student_status)

EOF
}

# =====================================================
# Main
# =====================================================

case "${1:-help}" in
    generate)
        generate_baseline
        exit $?
        ;;
    verify)
        verify_contract
        exit $?
        ;;
    help|--help|-h)
        show_help
        exit 0
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
