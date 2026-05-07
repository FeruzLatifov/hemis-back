---
description: Port a CUBA Platform endpoint from old-hemis to api-legacy module with full backward compatibility
argument-hint: <HTTP_METHOD> <PATH>  (e.g. "GET /services/tax/rent")
allowed-tools: Read, Grep, Glob, Bash, Edit, Write
---

Port the legacy endpoint specified in arguments: $ARGUMENTS

> **Canonical workflow:** `.claude/ENDPOINT_PORTING_GUIDE.md` (8 qadam, to'liq spec).
> Bu slash command — qisqa avtomatlashtirilgan boshqaruv. Tafsilot uchun `@.claude/ENDPOINT_PORTING_GUIDE.md` o'qing.

## Quick Workflow (8 qadam)

| # | Qadam | Vosita | Output |
|---|-------|--------|--------|
| 1 | Trigger parse | `$ARGUMENTS` → method + path | — |
| 2 | Duplicate check | `grep -rn "<path>" api-legacy/` | STOP if topildi |
| 3 | Old-hemis live response | `curl -H "Authorization: Bearer $TOKEN" :8082<path>` | `legacy-fixtures/<name>.json` |
| 4 | Metadata extract | `old_hemis.md` (tag/desc) + `rest-services.xml` (params) | tag, summary |
| 5 | Controller generation | `toMap()` + `LinkedHashMap` pattern (NOT MapStruct) | `<Service>EntityController.java` |
| 6 | Test va solishtirish | `diff old.json new.json` → 100% MATCH | green |
| 7 | endpoint_tester.html | `endpoints/XX-*.js` ga test button | UI |
| 8 | Univer kontrakt verify | `node compare_endpoints.js` → 175/175 | green |

## Critical Patterns (api-legacy)

**Real implementation:** 261 ta controller `toMap()` + `LinkedHashMap` ishlatadi. **MapStruct ishlatilmaydi** api-legacy'da.

```java
// ✅ TO'G'RI — LinkedHashMap (CUBA field order saqlanadi)
private Map<String, Object> toMap(Entity entity) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("_entityName", "hemishe$Student");          // hash, dollar — CUBA convention
    map.put("_instanceName", buildInstanceName(entity));
    map.put("id", entity.getId().toString());
    // Old-hemis qaytargan tartibda, faqat o'sha maydonlar
    map.put("createTs", formatDateTime(entity.getCreatedAt()));  // yyyy-MM-dd'T'HH:mm:ss.SSS
    map.put("_employee", nestedRef(entity.getEmployee()));        // {"id": "uuid"}
    return map;
}

// ❌ NOTO'G'RI — HashMap field order'ni yo'qotadi
Map<String, Object> map = new HashMap<>();

// ❌ NOTO'G'RI — MapStruct CUBA dynamic field'larni boshqara olmaydi
@Mapper(componentModel = "spring")
public interface StudentLegacyMapper { ... }
```

**FK = nested object majburiy:**
```json
// ✅ {"_employee": {"id": "uuid-string"}}
// ✅ {"_university": {"code": "401"}}
// ❌ {"_employee": "uuid-string"}     ← flat string QABUL QILINMAYDI
```

**Datetime format:** `yyyy-MM-dd'T'HH:mm:ss.SSS` (3 raqam millisecond, T separator).

## Controller Pattern

```java
@RestController
@RequestMapping("/app/rest/v2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "06.Talaba", description = "<from old_hemis.md>")
public class StudentEntityController {

    private final StudentService modernService;     // SHARED with api-web
    private final UniversityFilterHelper univHelper;

    @GetMapping("/entities/hemishe_EStudent")
    @PreAuthorize("hasAuthority('student.view')")
    @Transactional(readOnly = true)
    @Operation(summary = "Talaba ro'yxati (CUBA format)")
    public ResponseEntity<List<Map<String, Object>>> getAll(...) {
        String universityCode = univHelper.currentUniversityCode();
        Page<Student> page = repository.findByUniversityCode(universityCode, pageable);
        return ResponseEntity.ok(toMapList(page.getContent()));
    }

    private Map<String, Object> toMap(Student e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_entityName", "hemishe$Student");
        // ... old-hemis tartibida
        return m;
    }
}
```

## Verification

```bash
# 1. Live diff vs old-hemis
TOKEN=$(curl -s -X POST http://localhost:8082/app/rest/v2/oauth/token \
    -u "myclient:myclient" \
    -d "grant_type=password&username=otm351&password=${OTM_PASSWORD}" | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082<path> > /tmp/old.json
curl -s -H "Authorization: Bearer ${NEW_TOKEN}" http://localhost:8081<path> > /tmp/new.json
diff <(jq -S . /tmp/old.json) <(jq -S . /tmp/new.json)
# Should be empty (100% match)

# 2. Univer kontrakt 175/175
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js

# 3. Lokal test
./gradlew :api-legacy:test --tests "*<Service>EntityControllerTest*"
```

## Constraints

- DO NOT add business logic in controller (delegate to existing `service` module)
- DO NOT use HashMap (always LinkedHashMap)
- DO NOT use MapStruct in api-legacy (`toMap()` patterni canonical — 261 controller ishlatadi)
- DO NOT skip `_entityName` / `_instanceName`
- DO NOT change `api-web` service signatures (api-legacy uses them as-is)
- DO NOT import `domain.entity.security.User`, `domain.entity.employee.Employee`, `domain.entity.employee.EmployeeJobs` — ADR-0008 violation, pre-commit reject.
  Legacy variant: `SecUser`, `Teacher`, `LegacyEmployeeJobs`.
- IF service for this entity doesn't exist in `service` module → STOP and ask user

## See also

- `@.claude/ENDPOINT_PORTING_GUIDE.md` — to'liq workflow (8 qadam) + URL→Tag mapping + FK helpers
- `@api-legacy/CLAUDE.md` — module-level CUBA format rules
- `@docs/UNIVER_CONTRACT.md` — 67 frozen endpoint contract
- `@docs/adr/0008-api-legacy-entity-rebinding.md` — entity ownership
- Test tool: `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js`
