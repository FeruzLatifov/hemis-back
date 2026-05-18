---
name: cache-add
description: Yangi @Cacheable qo'shish - to'liq pattern (TTL, evict pair, AOP safety). Trigger - "cache qo'sh", "@Cacheable", "TTL", "kesh strategiya".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Add @Cacheable

> 4 nuqta o'zgarish — biri ham unutilsa cache buziladi yoki memory leak.

## Workflow

### 1. Annotation

```java
@Cacheable(value = "<cacheName>", key = "#id", unless = "#result == null")
@Transactional(readOnly = true)
public Foo findById(Long id) { return repo.findById(id).orElse(null); }
```

**Key SpEL safety:**
- ✅ `key = "#id"` (oddiy, deterministik)
- ✅ `key = "#facultyId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize"`
- ❌ `key = "#dto.id"` (NPE agar dto null)
- ❌ `key = "#pageable"` (toString variants explode)

### 2. TTL (MAJBURIY) — `DashboardCacheConfig`

`service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java` — `TwoLevelCacheManager` ro'yxatiga `<cacheName>` + TTL qo'shish:

| Data turi | TTL |
|-----------|-----|
| Klassifikator (kam o'zgaradi) | 24h |
| Aggregation/dashboard | 5-15m |
| Per-user lookup | 1h |
| Hot read (talaba detail) | 1h |

> TTL yo'q → memory leak (cache cheksiz o'sadi).

### 3. Evict pair

Har `save/update/delete/batch/import` metod uchun:

```java
@CacheEvict(value = "<cacheName>", key = "#dto.id")        // single key
@CacheEvict(value = "<cacheName>", allEntries = true)       // batch ops
@Caching(evict = {                                          // multi cache
    @CacheEvict(value = "fooById", key = "#id"),
    @CacheEvict(value = "fooList", allEntries = true)
})
```

### 4. AOP self-invocation tekshirish

```bash
# private/package metod — AOP fail
grep -B 1 "@Cacheable" <file> | grep -E "(private|package)"

# Same-class invocation — AOP fail
# this.findById() ichida → cache ishlamaydi
```

Tuzatish: metod `public` + boshqa bean'dan chaqirilsin yoki bean ajratish.

### 5. Mutable list tekshirish

```java
// ❌ Caller list'ni mutate qilishi mumkin
@Cacheable("xs") public List<X> all() { return repo.findAll(); }

// ✅ Immutable copy
@Cacheable("xs") public List<X> all() { return List.copyOf(repo.findAll()); }
```

## Verification

```bash
grep -n "<cacheName>" service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java
./gradlew test --tests "*CacheTest*"
curl -s http://localhost:8081/actuator/prometheus | grep "cache_gets_total.*<cacheName>"
```

## Constraints

- ❌ TTL config yo'q → memory leak (P0)
- ❌ Evict pair yo'q → stale data
- ❌ Private/same-class invocation → silent fail
- ❌ Mutable list cached
- ❌ Big aggregate (>1MB) bitta cache key'da
- ❌ Pageable to'g'ridan-to'g'ri key'da
- ✅ `unless = "#result == null"` nullable return uchun

## See also

- `.claude/agents/cache-strategist.md` — review checklist
- `.claude/commands/audit-cache.md` — codebase audit
- `service/.../config/DashboardCacheConfig.java`
- ADR cache invariant — `.claude/rules.md`
