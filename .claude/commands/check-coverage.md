---
description: Run Jacoco coverage and report gaps below 70% threshold by module
argument-hint: [module-name]  (optional, defaults to all)
allowed-tools: Bash, Read, Grep, Glob
---

Check test coverage and identify gaps. Module: ${ARGUMENTS:-all}

## Workflow

### 1. Run tests with coverage

```bash
cd /home/adm1n/projects/startup/hemis-back
./gradlew clean test jacocoTestReport
```

Report which modules failed tests; if failures exist, list them and stop.

### 2. Aggregate coverage report

```bash
./gradlew jacocoRootReport
```

Parse the XML report at `build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml`:
```bash
xmllint --xpath "//report/counter[@type='LINE']/@covered" \
  build/reports/jacoco/jacocoRootReport/jacocoRootReport.xml
```

### 3. Per-module breakdown

For each module: `app`, `api-external`, `api-legacy`, `api-university`, `api-web`, `common`, `domain`, `security`, `service`:

```bash
xmllint --xpath "//package[@name]/counter[@type='LINE']" \
  <module>/build/reports/jacoco/test/jacocoTestReport.xml
```

Compute per-package coverage = covered / (covered + missed).

### 4. Identify gaps below threshold

**Threshold targets (from rules.md):**
- Overall: ≥70%
- Service layer: ≥90%
- Controller layer: ≥80%
- Repository layer: ≥60% (mostly Spring-generated)
- Domain entity: ≥40% (getters/setters not worth testing)

For every class below threshold, list:
- File path
- Coverage %
- Lines uncovered count
- Methods uncovered

### 5. Suggest priority

Rank gaps by:
1. **High risk** — public service methods, controllers, custom exceptions
2. **Medium** — utility classes, mappers
3. **Low** — DTO classes, generated code

### 6. Show example gap

For TOP 5 gaps, show 1 example uncovered method:

```bash
# Find untested methods in a class
grep -n "public.*(" <file> | head -10
```

### 7. Output

```
=== Test Coverage Report ===
Date: <today>
Module filter: <module|all>

Overall: 62.4% / 70% target ❌

Per-module:
  app           : 78%  ✓
  service       : 71%  ✓ (target 90% ❌)
  domain        : 65%  ✓
  security      : 81%  ✓
  api-web       : 68%  ✗
  api-legacy    : 54%  ✗
  api-external  : 42%  ✗
  common        : 88%  ✓

TOP 5 priority gaps:
  1. service/.../StudentLegacyService.java — 45% (methods: 12 of 22 uncovered)
     Example: createBatch() — 0/35 lines covered
  2. ...

Quality gate: FAIL (overall < 70%, service < 90%)

Recommended actions:
  1. Add tests for StudentLegacyService (largest gap, P1)
  2. Add api-legacy controller integration tests (5 controllers untested)
  3. Add api-external integration smoke tests

Estimated effort: ~12 hours to reach 70% threshold.
```

### 8. Optional — run mutation testing

If user requests deep analysis:
```bash
./gradlew :service:pitest
```

Mutation score < 60% → tests are weak (assert presence but not behavior).

## Constraints

- Don't suggest disabling coverage threshold to "pass" the build
- Don't suggest test-coverage-only commits (write meaningful tests)
- Skip generated files (MapStruct generated mappers, Lombok, build/)
- Skip pure DTO records (no logic to test)
