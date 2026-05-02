---
description: Audit all @Cacheable annotations across the codebase, verify TTL config, evict pairs, and AOP safety
allowed-tools: Read, Grep, Glob, Bash
---

Run a comprehensive cache audit. Output a structured report.

## Workflow

### 1. Find all `@Cacheable` and related annotations

```bash
cd /home/adm1n/projects/startup/hemis-back

# All cache annotations
grep -rn -E "@Cacheable|@CacheEvict|@CachePut|@Caching" \
  --include="*.java" \
  service/ api-web/ api-legacy/ api-external/ api-university/ security/
```

For each match, capture:
- File:line
- Annotation type
- Cache name (`value` or `cacheNames`)
- Key SpEL
- `unless` / `condition` clauses

### 2. Compare against TTL configuration

Read `service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java`. Find `TwoLevelCacheManager` cache name list with TTL.

```bash
grep -A 100 "TwoLevelCacheManager\|cacheNames\|Caffeine.*expireAfterWrite" \
  service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java
```

For every cache name used in code:
- ✓ TTL configured → OK
- ✗ TTL missing → P0 (default TTL undefined → memory leak risk)

### 3. Verify AOP safety (self-invocation trap)

For each `@Cacheable` method, check if it's called from a different bean:

```bash
# For each @Cacheable method, search who calls it
for METHOD in $cacheableMethods; do
  grep -rn "$METHOD(" --include="*.java" service/ api-*/
  # If only same class calls → AOP fails → P0
done
```

Specifically check:
- Method visibility (private/package = always fail)
- Caller class != Cacheable's class

### 4. Verify evict pair completeness

For each `@Cacheable` cache name, find mutation methods that should evict:

```bash
# For each cache value 'X', check class has methods that should evict
# Methods: save*, update*, create*, delete*, batch*, import*
# Each must have @CacheEvict targeting cache 'X'
```

Cross-reference: if class has `@Cacheable("X")` and also `update()`, `delete()`, etc. without `@CacheEvict` → P0.

### 5. Check key SpEL safety

For each `@Cacheable(key = "...")`:
- ❌ `key = "#dto.id"` — NPE if dto null
- ❌ `key = "#pageable"` — Pageable.toString() variants explode
- ❌ Missing `unless = "#result == null"` for nullable returns
- ✓ `key = "#id"` simple
- ✓ `key = "#facultyId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize"` deterministic

### 6. Detect anti-patterns

#### Mutable list cached
```java
@Cacheable("classifiers")
public List<X> findAll() { return repo.findAll(); }  // Caller can mutate
```
Fix: `return List.copyOf(repo.findAll());`

#### Big aggregate cached
```java
@Cacheable("studentDetail")
public StudentFullDto getFullDetail(Long id) { /* > 1MB */ }
```
Suggest decomposing.

#### Mutation-heavy cache
If cache evicted on every save (`allEntries=true` everywhere) → cache useless.

### 7. Cache hit ratio (if metrics available)

```bash
# Prometheus metrics endpoint
curl -s http://localhost:8081/actuator/prometheus | grep cache_gets_total

# Compute: hit_ratio = hits / (hits + misses)
# Target: > 85%
```

Per-cache hit ratio < 50% → cache not effective, reconsider TTL or strategy.

### 8. Output

```
=== Cache Audit Report ===
Date: <today>

Summary:
  Total @Cacheable: N
  Cache names used: M
  TTL configured: X / M  (Y missing)
  AOP-safe: A / N
  Evict pair complete: B / N
  Hit ratio average: 78%

🔴 P0 BLOCKING:
  1. service/StudentService.java:45
     @Cacheable("studentLookup") — TTL not configured in DashboardCacheConfig
     Impact: cache may grow unbounded (memory leak)
     Fix: Add 'studentLookup' to TwoLevelCacheManager with 1h TTL

  2. service/FacultyService.java:78
     @Cacheable("faculties") used, but update() lacks @CacheEvict
     Impact: stale data after update — users see old data for 24h
     Fix: Add @CacheEvict(value="faculties", key="#dto.id") to update()

  3. service/Loader.java:12
     @Cacheable on private method — AOP self-invocation, cache silently fails
     Fix: Make public OR move to separate @Service bean

🟡 P1 HIGH:
  ...

🟢 P2 IMPROVEMENTS:
  ...

✅ Healthy patterns:
  - HokimiyatClassifierService — proper @Cacheable + 24h TTL ✓
  - ClassifierReferenceLoader — extracted bean for AOP safety ✓

Cache TTL summary table:
  hokimiyatClassifiers   24h  ✓
  classifierEducationType 24h ✓
  studentGpa             1h   ✓
  studentLookup          ❌ NOT CONFIGURED
  ...

Recommendations:
  1. Add missing TTL configurations (3 cache names)
  2. Fix 2 AOP self-invocation traps
  3. Add 5 missing @CacheEvict pairs
  4. Estimated effort: 4 hours

Next: ./gradlew test --tests "*CacheTest*"
```

## Constraints

- Don't suggest second-level Hibernate cache (different concern)
- Don't propose `allEntries=true` as default mutation strategy without justification
- Don't recommend cache for write-heavy entities (sessions, audit logs)
- Skip Spring Security's internal caches (different lifecycle)
