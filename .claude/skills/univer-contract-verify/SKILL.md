---
name: univer-contract-verify
description: api-legacy o'zgartirilgandan keyin Univer 175/175 kontraktni tekshirish. Trigger - "Univer kontrakt", "175/175", "compare_endpoints", "api-legacy o'zgardi", "MATCH tekshir".
allowed-tools: Read, Bash, Grep, Glob
---

# Univer Contract Verification

> **Golden Rule #2:** `api-legacy` har o'zgarishi 175/175 testdan o'tishi shart. Univer 224 OTM bu kontraktga **muzlatilgan** holda bog'lanadi.

## Workflow

### 1. Server holatini tekshirish

```bash
# old-hemis (CUBA, port 8082) va hemis-back (Spring, port 8081) ikkalasi up bo'lishi shart
curl -s -o /dev/null -w "old-hemis: %{http_code}\n"   http://localhost:8082/
curl -s -o /dev/null -w "hemis-back: %{http_code}\n"  http://localhost:8081/
```

Yo'q bo'lsa: `./gradlew :app:bootRun` (8081) + old-hemis CUBA tomonida (8082).

### 2. Compare tool ishga tushirish

```bash
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js
# Maqsad: MATCH 175/175
```

Output saqlash:
```bash
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js \
  | tee /tmp/univer-contract.log
```

### 3. Diff interpretatsiya

| Diff turi | Sabab | Tuzatish |
|-----------|-------|----------|
| Field tartibi farqli | `HashMap` ishlatilgan | `LinkedHashMap` ga almashtirish |
| `_entityName` / `_instanceName` yo'q | CUBA meta yetishmaydi | `toMap()` ichiga qo'shish |
| FK flat string `"uuid"` | Nested object kutilgan | `{"id": "uuid"}` shakliga |
| Datetime format farq | ISO/timestamp/null | `yyyy-MM-dd'T'HH:mm:ss.SSS` |
| Status code 4xx/5xx | Auth yoki path noto'g'ri | Token/permission/path tekshirish |
| Pagination `total` farqli | Filter mantiq farqli | Repository query solishtirish |
| 404 endpoint | Hali port qilinmagan | `/port-endpoint` workflow |

### 4. Per-endpoint chuqur diff

Faqat sinishlar uchun:
```bash
TOKEN_OLD=$(curl -s -X POST http://localhost:8082/app/rest/v2/oauth/token \
  -u "myclient:myclient" \
  -d "grant_type=password&username=otm351&password=$OTM_PASSWORD" | jq -r .access_token)

TOKEN_NEW=$(curl -s -X POST http://localhost:8081/app/rest/v2/oauth/token \
  -u "myclient:myclient" \
  -d "grant_type=password&username=otm351&password=$OTM_PASSWORD" | jq -r .access_token)

PATH=/app/rest/v2/entities/hemishe_EStudent
curl -s -H "Authorization: Bearer $TOKEN_OLD" "http://localhost:8082$PATH" | jq -S . > /tmp/old.json
curl -s -H "Authorization: Bearer $TOKEN_NEW" "http://localhost:8081$PATH" | jq -S . > /tmp/new.json
diff /tmp/old.json /tmp/new.json
```

### 5. Hisobot

```
=== Univer Contract Verify ===
Result: <X>/175 MATCH

❌ Sindirgan endpoint'lar (N):
  1. GET /entities/hemishe_EStudent
     - field "createTs" tartibda 5-pozitsiyada (kutilgan: 3-pozitsiya)
     - sabab: HashMap ishlatilgan StudentEntityController.java:78
  2. ...

✅ Ko'k: <X> endpoint
🚨 Action: <ro'yxat>
```

### 6. Regression bo'lsa — fixture qayta yozish

`api-legacy/src/test/resources/legacy-fixtures/<endpoint>.json` ni **faqat** qaror qabul qilingan field qo'shilganda yangilang. Aksincha — kod xato.

## Pre-merge gate

`api-legacy/**` PR'da MATCH 175/175 majburiy. Pre-commit hook ishlamasa, manual:
```bash
git diff --name-only main...HEAD | grep -q "^api-legacy/" && \
  node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js
```

## See also

- `docs/UNIVER_CONTRACT.md` — 67 frozen endpoint contract
- `docs/UNIVER_ENDPOINT_AUDIT.md` — per-endpoint file:line
- `api-legacy/CLAUDE.md` — CUBA format qoidalar
- `.claude/skills/endpoint-port` (agar mavjud bo'lsa) — yangi endpoint port qilish
- ADR-0008 — Legacy* prefiks rebinding
