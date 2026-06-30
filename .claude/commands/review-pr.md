---
description: Run a comprehensive PR review using all specialized subagents in parallel
argument-hint: [PR_number_or_branch]  (optional)
allowed-tools: Bash, Read, Grep, Glob, Task
---

Multi-agent PR review for: ${ARGUMENTS:-current branch}

## Workflow

### 1. Identify changed files

```bash
cd /home/adm1n/projects/startup/hemis-back

# If PR number provided
gh pr diff $ARGUMENTS --name-only 2>/dev/null

# If branch
git diff --name-only main...HEAD 2>/dev/null

# If neither — show working tree changes
git diff --name-only HEAD
```

Categorize files:
- Java source: by module (`api-*`, `service/`, `domain/`, `security/`)
- Liquibase: `domain/src/main/resources/db/changelog/changesets/**`
- Config: `application*.yml`, `build.gradle*`
- Tests: `src/test/`

### 2. Show PR summary

```
=== PR Review: <branch or #PR> ===
Files changed: N
  Java: X (api-legacy: a, api-web: b, service: c, domain: d, security: e)
  Migration: Y
  Config: Z
  Tests: T

Lines: +AAAA / -BBB
```

### 3. Launch agents in parallel

Use the `Task` tool to dispatch specialized subagent reviews. **Run all in a single message
with multiple Task calls** so they execute concurrently — sequential calls double review time.

Trigger each agent only when relevant files changed (skip otherwise — empty input wastes context):

#### a) N+1 Detector — if `service/**`, `domain/**/repository/**`, or `**/*Mapper.java` changed
```
Task({
  subagent_type: "n-plus-one-detector",
  description: "N+1 query detection",
  prompt: "Review these changed files for N+1 patterns: <comma-separated paths>. Focus on JPA fetch type, JOIN FETCH usage, @EntityGraph, Lombok @Data on entities, accessor calls inside iteration."
})
```

#### b) Liquibase Reviewer — if `domain/src/main/resources/db/changelog/**` changed
```
Task({
  subagent_type: "liquibase-reviewer",
  description: "Migration safety review",
  prompt: "Review these migration changesets: <list>. Verify rollback file presence, idempotency (IF NOT EXISTS), no ALTER on hemishe_* tables, master.yaml registration, lock-free DDL."
})
```

#### c) Cache Strategist — if `@Cacheable` / `@CacheEvict` / `@CachePut` in diff
```
Task({
  subagent_type: "cache-strategist",
  description: "Cache annotation review",
  prompt: "Review cache annotations added/changed: <list>. Verify TTL config in CacheConfig, AOP self-invocation safety, missing @CacheEvict pair on writes, mutable list caching, SpEL key safety."
})
```

#### d) CUBA Format Checker — if `api-legacy/**` changed
```
Task({
  subagent_type: "cuba-format-checker",
  description: "CUBA backward compat check",
  prompt: "Validate api-legacy changes preserve CUBA format: <list>. Check: LinkedHashMap (not HashMap), @JsonPropertyOrder, _entityName/_instanceName fields, FK as nested object, datetime format, error envelope shape."
})
```

#### e) Security Auditor — if `security/**`, `**/controller/**`, or auth-related changed
```
Task({
  subagent_type: "security-auditor",
  description: "OWASP 2025 audit",
  prompt: "Audit for OWASP Top 10:2025 violations: <list>. Pay special attention to: missing @PreAuthorize on controller methods, SQL injection (string concat in queries), PII logging (pinfl/password/token), hardcoded secrets, weak crypto, SSRF, unsafe deserialization."
})
```

#### f) Webhook/Outbox Reviewer — if `service/webhook/**`, `service/outbox/**`, `service/employee/**`, `domain/entity/{webhook,outbox}/**`, or V014/V015 changed
```
Task({
  subagent_type: "webhook-outbox-reviewer",
  description: "Webhook/outbox pipeline review",
  prompt: "Review webhook/outbox/employee-sync changes: <list>. Verify: outbox atomicity (Propagation.MANDATORY), idempotent upsert (ON CONFLICT), HMAC signature (outbound + ack constant-time), secret persistence (secret_enc AES-256-GCM, no plaintext), DLQ routing + FATAL Sentry, retention/max_retries config drift, K2 apply-status feedback, OTM existsByCode validation, no PINFL in Sentry."
})
```

### 4. Run static checks

In parallel with agents:
```bash
./gradlew check -x test    # SpotBugs, PMD, Checkstyle (if configured)
./gradlew test --tests "*<changed-classes>*"
./gradlew dependencyCheckAnalyze 2>/dev/null  # if plugin present
```

### 5. Aggregate findings

Wait for all agents to complete. Collate findings:

```
=== Aggregated Review ===

🔴 P0 BLOCKING (must fix before merge):
  N+1: 1 finding (StudentMapper.java:45 - JOIN FETCH missing)
  Cache: 1 finding (FacultyService.java:78 - missing @CacheEvict pair)
  Security: 0 findings
  Migration: 0 findings
  CUBA: 0 findings
  Static: 0 findings

🟡 P1 HIGH:
  ...

🟢 P2 IMPROVEMENTS:
  ...

✅ Compliant areas:
  - All endpoints have @PreAuthorize
  - All migrations have rollback files
  - LinkedHashMap used in api-legacy
  - ...
```

### 6. Test coverage delta

```bash
git diff main...HEAD --name-only -- '*.java' | grep -v Test
# For each, check if matching *Test.java exists or was modified
```

Report files without test coverage in the PR.

### 7. Final verdict

```
=== Verdict ===

P0 blocking: 2
P1 high: 5
P2 minor: 8

Recommendation: ❌ REQUEST CHANGES
  Required fixes before merge:
    1. Add JOIN FETCH at StudentMapper.java:45 (N+1)
    2. Add @CacheEvict at FacultyService.java:78 (stale cache)

Suggested follow-ups (not blocking):
  - Improve test coverage (3 files in PR have no tests)
  - Consider Argon2id for password encoder (P2)

Approve only after P0 items resolved.
```

If 0 P0 + 0 P1 → `✅ APPROVE`. Otherwise → `❌ REQUEST CHANGES`.

## Constraints

- Always run agents in PARALLEL (single message with multiple Task calls), not sequential
- If a subagent doesn't apply (e.g., no migration files) → skip, don't run with empty input
- Don't approve PR with P0 findings, regardless of urgency
- Don't suggest disabling agents to "speed up review"
- For automated CI: exit code = number of P0 findings
