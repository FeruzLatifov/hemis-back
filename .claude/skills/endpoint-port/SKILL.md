---
name: endpoint-port
description: CUBA endpoint'ni old-hemis'dan api-legacy modulga 1:1 port qilish. Trigger - "PORT GET/POST /services/...", "PORT /app/rest/...", "endpoint ko'chir", "legacy port".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Port Legacy Endpoint

> **Canonical:** `.claude/ENDPOINT_PORTING_GUIDE.md` (8 qadam to'liq spec). Bu skill — qisqa avtomatlashtirish.
> **Pattern:** `toMap()` + `LinkedHashMap` (NOT MapStruct — 261 ta controller shu pattern).

## Workflow (8 qadam)

| # | Qadam | Output |
|---|-------|--------|
| 1 | `$METHOD $PATH` parse | method, path |
| 2 | Duplicate: `grep -rn "<path>" api-legacy/` | STOP if topildi |
| 3 | Old-hemis live response (8082) → `legacy-fixtures/<name>.json` | fixture |
| 4 | Metadata: `old_hemis.md` (tag/desc) + `rest-services.xml` (params) | tag, summary |
| 5 | Controller — `toMap()` + LinkedHashMap pattern | `<E>EntityController.java` |
| 6 | Live diff: 8082 vs 8081 → 100% MATCH | green |
| 7 | `endpoint_tester.html` test button | UI |
| 8 | `compare_endpoints.js` → 175/175 | green |

## Critical Patterns

```java
// ✅ TO'G'RI — LinkedHashMap (CUBA field order)
private Map<String, Object> toMap(Entity e) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("_entityName", "hemishe$Student");           // hash, dollar
    m.put("_instanceName", buildInstanceName(e));
    m.put("id", e.getId().toString());
    m.put("createTs", formatDateTime(e.getCreatedAt())); // yyyy-MM-dd'T'HH:mm:ss.SSS
    m.put("_employee", nestedRef(e.getEmployee()));      // {"id": "uuid"}
    return m;
}
```

❌ `HashMap` · ❌ `MapStruct` · ❌ FK flat string · ❌ `_entityName`/`_instanceName` yo'q.

```java
@RestController
@RequestMapping("/app/rest/v2")
@RequiredArgsConstructor
@Tag(name = "06.Talaba")
public class StudentEntityController {
    private final StudentService modernService;       // SHARED with api-web
    private final UniversityFilterHelper univHelper;

    @GetMapping("/entities/hemishe_EStudent")
    @PreAuthorize("hasAuthority('student.view')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAll(...) {
        String code = univHelper.currentUniversityCode();
        Page<Student> page = repository.findByUniversityCode(code, pageable);
        return ResponseEntity.ok(toMapList(page.getContent()));
    }
}
```

## Verification

```bash
# Live diff
TOKEN_OLD=$(curl -s -X POST http://localhost:8082/app/rest/v2/oauth/token \
  -u myclient:myclient -d "grant_type=password&username=otm351&password=$OTM_PASSWORD" | jq -r .access_token)
curl -s -H "Authorization: Bearer $TOKEN_OLD" http://localhost:8082<path> > /tmp/old.json
curl -s -H "Authorization: Bearer $TOKEN_NEW" http://localhost:8081<path> > /tmp/new.json
diff <(jq -S . /tmp/old.json) <(jq -S . /tmp/new.json)

# Univer 175/175 — `univer-contract-verify` skill
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js

# Lokal test
./gradlew :api-legacy:test --tests "*<E>EntityControllerTest*"
```

## Constraints (ADR-0008 + CUBA)

- ❌ Business logic controller'da (delegate to `service` modul)
- ❌ `HashMap` (har doim `LinkedHashMap`)
- ❌ `MapStruct` api-legacy'da
- ❌ `_entityName` / `_instanceName` skip
- ❌ `api-web` service signature o'zgartirish
- ❌ Import `domain.entity.security.User`, `domain.entity.employee.Employee`, `domain.entity.employee.EmployeeJobs` — pre-commit reject. Legacy variant: `SecUser`, `Teacher`, `LegacyEmployeeJobs`
- ⚠ Service mavjud emas → STOP, foydalanuvchidan so'ra

## See also

- `.claude/ENDPOINT_PORTING_GUIDE.md` — to'liq workflow + URL→Tag mapping
- `api-legacy/CLAUDE.md` — CUBA format qoidalar
- `docs/UNIVER_CONTRACT.md` — 67 frozen endpoint
- ADR-0008 — entity rebinding
- `.claude/skills/univer-contract-verify` — 175/175 tekshirish
- `.claude/agents/cuba-format-checker.md` — review
