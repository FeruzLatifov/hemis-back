---
description: Codebase-wide cache audit — barcha @Cacheable annotation'larni cache-strategist agent orqali tekshirish
allowed-tools: Bash, Grep, Glob, Task
---

Codebase'dagi barcha cache annotation'larini audit qilish.

## Workflow

### 1. Cache annotation'larni topish

```bash
cd /home/adm1n/projects/startup/hemis-back
grep -rln -E "@Cacheable|@CacheEvict|@CachePut|@Caching" \
  --include="*.java" \
  service/ api-web/ api-legacy/ api-external/ api-university/ security/ \
  | sort -u > /tmp/cache-files.txt

wc -l /tmp/cache-files.txt
```

### 2. `cache-strategist` agent'ni codebase-wide chaqirish

```
Agent({
  subagent_type: "cache-strategist",
  description: "Codebase-wide cache audit",
  prompt: "Audit ALL cache annotations across the codebase. Files: <paste /tmp/cache-files.txt>. \
  Check every @Cacheable for: (1) TTL configured in DashboardCacheConfig.java, (2) @CacheEvict pair on mutation methods, \
  (3) AOP self-invocation safety (private/same-class), (4) SpEL key safety (no Pageable/null deref), \
  (5) mutable list returns (need List.copyOf), (6) cache name typos. \
  Reference: service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java for TTL list. \
  Output: P0/P1/P2 priority report with file:line and concrete fix."
})
```

### 3. Cache hit ratio (ixtiyoriy)

```bash
curl -s http://localhost:8081/actuator/prometheus 2>/dev/null \
  | grep -E "cache_(gets|puts)_total" | head -20
```

Per-cache hit ratio < 50% → cache effective emas.

### 4. Hisobotni foydalanuvchiga qaytarish

Agent natijasini quyidagi shaklda yetkazing:
```
=== Cache Audit ===
Total @Cacheable: N · Cache names: M · TTL configured: X/M

🔴 P0: <ro'yxat>
🟡 P1: <ro'yxat>
🟢 P2: <ro'yxat>

✅ Healthy: <ro'yxat>
Estimated effort: <vaqt>
```

## See also

- `.claude/agents/cache-strategist.md` — agent qoidalar
- `.claude/skills/cache-add/SKILL.md` — yangi cache qo'shish workflow
- `service/src/main/java/uz/hemis/service/config/DashboardCacheConfig.java`
