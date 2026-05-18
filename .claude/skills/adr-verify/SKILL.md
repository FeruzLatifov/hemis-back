---
name: adr-verify
description: ADR'ning kod bilan mosligini tekshirish (drift detection). Trigger - "ADR-NNNN bajarildimi", "ADR status", "implementation status", sprint planning.
allowed-tools: Read, Bash, Grep, Glob
---

# Verify ADR Implementation

## Workflow

### 1. ADR o'qish

```bash
REPO=$(git rev-parse --show-toplevel)
cat "$REPO"/docs/adr/NNNN-*.md
```

Frontmatter'dan: `status`, `affects`, `liquibase`, `entities`, `verification`. `yq` bo'lsa `yq -r '.status' file.md`, yo'q bo'lsa `head -20` + manual parse.

### 2. Verification cmd ishga tushirish

Frontmatter'dagi `verification` qiymati — odatda script. Bajaring va exit kodini saqlang.

### 3. Decision turi bo'yicha cross-check

| Tur | Tekshirish |
|-----|------------|
| **Schema** (Liquibase) | `liquibase[]` fayllari mavjud · `db.changelog-master.yaml`'da ro'yxatda · jadval real DB'da (ixtiyoriy: `./scripts/check_table_mappings.sh`) |
| **Entity refactor** | Eski import qoldimi: `grep -rn "import <old.path>"` · Yangi ishlatilyaptimi: `grep -rn "import <new.path>"` |
| **Module boundary** | Cross-module import bloklash (masalan ADR-0008): `grep -rlnE "import <forbidden>" <module>/` |
| **API contract** | `node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js` → 175/175 |

### 4. True status hisoblash

```
frontmatter   |  kod          |  natija
proposed      |  0%           |  ✅ mos
accepted/in-p |  0%           |  🔴 DRIFT (frontmatter advance)
accepted      |  100%         |  🟡 status update kerak (→ implemented)
implemented   |  regression   |  🚨 BLOCKER (audit)
mos           |  mos          |  ✅ green
```

### 5. Hisobot

```
=== ADR-NNNN Verification ===
File: docs/adr/NNNN-<title>.md
Frontmatter: <status>      Real: <inferred>
Stages: ✅ Stage1 · ⏳ Stage2 (X/Y) · ❌ Stage3 (sabab)
Verification: <cmd> → <exit code> / <natija>
Drift: <YES|NO>
Action: <update frontmatter | finish stage | none>
```

## Sprint audit (batch)

```bash
REPO=$(git rev-parse --show-toplevel)
for adr in "$REPO"/docs/adr/[0-9]*.md; do
  echo "=== $(basename "$adr") ==="
  bash -c "$(yq -r '.verification' "$adr" 2>/dev/null)" || echo "no verification cmd"
done
```

## See also

- `docs/adr/README.md` · `.claude/rules.md` "ADR Status Drift Detection"
- `.claude/skills/adr-create/SKILL.md` · `scripts/check_table_mappings.sh`
