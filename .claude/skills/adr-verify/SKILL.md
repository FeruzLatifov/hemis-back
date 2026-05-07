---
name: adr-verify
description: Verify whether an ADR's decision has been implemented in the codebase. Use when user asks "is ADR-NNNN done?", "ADR status", "implementation status", or before sprint planning to detect ADR Status Drift. Compares ADR's stated status against actual code/schema state.
---

# Verify ADR Implementation Status

## When to invoke

Trigger phrases:
- "ADR-NNNN bajarildimi?", "ADR status tekshir", "ADR audit"
- "implementation status of <feature>"
- Sprint planning'da: "qaysi ADR'lar hali pending?"
- ADR'da `status: in-progress` yoki `status: accepted` deb yozilgan, lekin haqiqat noma'lum

## Workflow

### 1. Read ADR file

```
Read: /home/adm1n/projects/startup/hemis-back/docs/adr/NNNN-*.md
# Yoki: cd $(git rev-parse --show-toplevel) avval
```

> **Diqqat:** Skill subagent cwd har xil bo'lishi mumkin — absolute path tavsiya etiladi.
> **Tools dependency:** `yq` o'rnatilishi shart (`apt install yq` yoki `brew install yq`). Yo'q bo'lsa frontmatter'ni `head -20` + manual parse.

YAML frontmatter'dan ajratib oling:
- `status` (proposed/accepted/in-progress/implemented)
- `affects` (qaysi modullar)
- `liquibase` (qaysi changesetlar)
- `entities` (qaysi JPA entity'lar)
- `verification` (bajariladigan komanda)

### 2. Run frontmatter `verification` command

```bash
# YAML'da: verification: ./scripts/check_table_mappings.sh --adr=0008
bash -c "$(yq -r '.verification' docs/adr/NNNN-*.md)"
```

Yoki manual:
```bash
# entities ro'yxatidan har biri uchun
for entity in $(yq -r '.entities[]' docs/adr/NNNN-*.md); do
    grep -rln "class $entity" domain/src/main/java
done
```

### 3. Cross-check decision vs code

Har qaror turi uchun farqli tekshiruv:

#### Schema decision (Liquibase)

```bash
# 1. Changeset mavjudligini tekshirish
yq -r '.liquibase[]' docs/adr/NNNN-*.md | while read cs; do
    find domain/src/main/resources/db/changelog -name "$cs"
done

# 2. master.yaml'da ro'yxatga olinganligini tekshirish
grep -E "<changeset_id>" domain/src/main/resources/db/changelog/db.changelog-master.yaml

# 3. Lokal DB'da jadval mavjudmi (ixtiyoriy)
./scripts/check_table_mappings.sh
```

#### Entity refactor (Java)

```bash
# Eski import qoldimi?
grep -rn "import uz.hemis.domain.entity.<old_path>" <module>/src/main/java
# Yangi import ishlatilyaptimi?
grep -rn "import uz.hemis.domain.entity.<new_path>" <module>/src/main/java
```

#### Module boundary (architectural)

```bash
# Misol: ADR-0008 — api-legacy yangi schema entity
grep -rlnE "import uz\.hemis\.domain\.entity\.(security\.User|employee\.Employee|employee\.EmployeeJobs);" \
    api-legacy/src/main/java/ | grep -v "Legacy"
# Bu zero bo'lishi shart — agar yo'q bo'lsa Stage 2-5 hali bajarilmagan
```

#### API contract (api-legacy)

```bash
# Univer kontrakt 175/175
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js
```

### 4. Compute true status

```
Has frontmatter status say one thing, but code shows another?
├─ status=accepted/in-progress, kod 0% → REAL: proposed (drift!)
├─ status=accepted, kod 100% → REAL: implemented (yangilash kerak)
├─ status=implemented, kod regression → BLOCKER (audit kerak)
└─ frontmatter mos → ✅ green
```

### 5. Report format

```
=== ADR-NNNN Verification ===
File: docs/adr/NNNN-<title>.md

Frontmatter status: <status>
Real code status:   <inferred>

Stage progress (from ## Implementation):
  ✅ Stage 1 — Audit
  ⏳ Stage 2 — Refactor (X/Y files done)
  ❌ Stage 3 — Blocked (sabab)

Verification commands run:
  - <cmd1> → <result>
  - <cmd2> → <result>

Drift detected: <YES|NO>
Action required: <update frontmatter | finish implementation | none>
```

## Common drift patterns

| ADR | Frontmatter | Real | Sabab |
|-----|-------------|------|-------|
| 0005 (OAuth) | proposed | server-side 100% | Status update kerak edi (hozir tuzatildi: in-progress) |
| 0007 (Kafka) | proposed | 0% kod (faqat docker-compose) | To'g'ri (Stage 0) |
| 0008 (rebinding) | accepted | Stage 1 only (4 import qoldi) | To'g'ri (Stage 2-5 pending) |

## Sprint integration

```bash
# Har sprint boshida — drift detection
for adr in docs/adr/[0-9]*.md; do
    echo "=== $adr ==="
    bash -c "$(yq -r '.verification' "$adr")"
done > /tmp/adr-audit.log
```

## See also

- `docs/adr/README.md` — barcha ADR ro'yxati
- `.claude/rules.md` "ADR Status Drift Detection" — qoida
- `.claude/skills/adr-create/SKILL.md` — yangi ADR yaratish
- `scripts/check_table_mappings.sh` — JPA ↔ DB moslik
- `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js` — Univer 175/175
