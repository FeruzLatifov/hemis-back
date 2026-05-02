---
description: Port a CUBA Platform endpoint from old-hemis to api-legacy module with full backward compatibility
argument-hint: <HTTP_METHOD> <PATH>  (e.g. "GET /services/tax/rent")
allowed-tools: Read, Grep, Glob, Bash, Edit, Write
---

Port the legacy endpoint specified in arguments: $ARGUMENTS

## Workflow

Execute these steps in order. Stop and report if any blocker is found.

### 1. Parse the trigger

Extract HTTP method and path from `$ARGUMENTS`. Examples:
- `GET /services/tax/rent` → method=GET, path=/services/tax/rent
- `POST /entities/hemishe$Student` → method=POST, path=/entities/hemishe$Student

### 2. Check duplicate

Search if this endpoint is already implemented:
```bash
grep -rn "$path" --include="*Controller.java" /home/adm1n/projects/startup/hemis-back/api-legacy
```

If found → STOP and report: "Already implemented at: <file>". Don't duplicate.

### 3. Read old-hemis metadata

```bash
# Endpoint metadata from old API spec
jq '.paths."<path>"."<method>"' /home/adm1n/startup/old_hemis.json
```

Extract: parameters, request body schema, response schema, security requirements.

### 4. Capture live response (if old-hemis is running)

```bash
# Old-hemis runs on :8082, user otm351
TOKEN=$(curl -s -X POST http://localhost:8082/app/rest/v2/oauth/token \
    -u "myclient:myclient" \
    -d "grant_type=password&username=otm351&password=<from .env>" | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8082<path>" \
  > /home/adm1n/projects/startup/hemis-back/api-legacy/src/test/resources/legacy-fixtures/<endpoint-name>.json
```

If old-hemis not running, skip but warn: fixture-based test won't be possible.

### 5. Generate Controller

Create or update controller in `api-legacy/src/main/java/uz/hemis/api/legacy/controller/`.

**Pattern:**
```java
@RestController
@RequestMapping("/app/rest/v2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "<Service> Legacy", description = "CUBA-compatible <description>")
public class <Service>LegacyController {

    private final <Service>Service modernService;  // SHARED with api-web
    private final <Service>LegacyMapper legacyMapper;

    @Operation(summary = "<from old_hemis.json>")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(schema = @Schema(implementation = <Dto>.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @<Method>Mapping("<path>")
    @PreAuthorize("hasAuthority('<resource>.<action>')")
    public ResponseEntity<<Dto>LegacyDto> <method>(...) {
        // 1. Map legacy params → modern DTO
        // 2. Call shared service
        // 3. Map modern result → legacy DTO (CUBA format)
        return ResponseEntity.ok(legacyMapper.toLegacy(result));
    }
}
```

### 6. Generate DTO with CUBA format

```java
@Data
@JsonPropertyOrder({
    "_entityName",
    "_instanceName",
    "id",
    // ... fields in OLD response order
})
public class <Service>LegacyDto {

    @JsonProperty("_entityName")
    private String entityName = "<hemishe$Entity>";

    @JsonProperty("_instanceName")
    private String instanceName;

    private String id;

    // Datetime fields:
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createTs;

    // FK as nested object:
    private FacultyReference faculty;

    public static class FacultyReference {
        @JsonProperty("_entityName")
        private String entityName = "hemishe$Faculty";
        private String id;
        @JsonProperty("_instanceName")
        private String instanceName;
    }
}
```

### 7. Generate MapStruct mapper

```java
@Mapper(componentModel = "spring")
public interface <Service>LegacyMapper {

    @Mapping(target = "entityName", constant = "hemishe$<Entity>")
    @Mapping(target = "instanceName", expression = "java(buildInstanceName(entity))")
    <Service>LegacyDto toLegacy(<Service>Dto entity);

    default String buildInstanceName(<Service>Dto e) {
        return e.<lastName>() + " " + e.<firstName>();
    }
}
```

### 8. Generate integration test

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class <Service>LegacyControllerTest {

    @Autowired private MockMvc mvc;

    @Test
    @WithMockUser(authorities = {"<resource>.<action>"})
    void <method>_shouldMatchLegacyFormat() throws Exception {
        String expected = Files.readString(
            Path.of("src/test/resources/legacy-fixtures/<endpoint-name>.json")
        );
        String actual = mvc.perform(<method>("<path>"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // STRICT_ORDER — field order must match CUBA exactly
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT_ORDER);
    }

    @Test
    void <method>_unauthorized() throws Exception {
        mvc.perform(<method>("<path>")).andExpect(status().isUnauthorized());
    }
}
```

### 9. Add test button to endpoint_tester.html

```html
<!-- Insert in correct section -->
<div class="endpoint">
    <h4><method> <path></h4>
    <button onclick="testEndpoint('<method>', '<path>')">Test</button>
</div>
```

File: `/home/adm1n/projects/startup/hemis-back/docs/endpoint_tester.html`

### 10. Verify backward compatibility

Run:
```bash
./gradlew :api-legacy:test --tests "*<Service>LegacyControllerTest*"
```

Then live diff:
```bash
diff <(jq -S . /tmp/old.json) <(jq -S . /tmp/new.json)
```

**Should produce: NO output** (perfect match).

If diff exists → fix mapper before approving.

### 11. Report

```
=== Endpoint Ported ===
Method: <method>
Path: <path>
Files created/modified:
  - api-legacy/.../<Service>LegacyController.java
  - common/.../dto/<Service>LegacyDto.java
  - api-legacy/.../mapper/<Service>LegacyMapper.java
  - api-legacy/src/test/.../<Service>LegacyControllerTest.java
  - api-legacy/src/test/resources/legacy-fixtures/<endpoint-name>.json
  - docs/endpoint_tester.html

CUBA format compliance:
  - LinkedHashMap: ✓
  - @JsonPropertyOrder: ✓
  - _entityName/_instanceName: ✓
  - FK nested object: ✓
  - Datetime format: ✓

Test status: <PASS|FAIL>
Live diff vs old-hemis: <CLEAN|<count> mismatches>

Next: ./gradlew :api-legacy:test
```

## Constraints

- DO NOT add business logic in controller (delegate to existing `service` module)
- DO NOT use HashMap (always LinkedHashMap)
- DO NOT skip `_entityName` / `_instanceName`
- DO NOT change `api-web` service signatures (api-legacy uses them as-is)
- IF service for this entity doesn't exist in `service` module → STOP and ask user

## See also
- `@ENDPOINT_PORTING_GUIDE.md` — full porting workflow
- `@api-legacy/CLAUDE.md` — CUBA format rules
