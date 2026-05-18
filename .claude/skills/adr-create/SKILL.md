---
name: adr-create
description: Yangi ADR yaratish (AgDR 2026 standart). Trigger - "ADR yoz", "decision record", "arxitektura qaror". Skip - bugfix, code style, vaqtinchalik workaround.
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Create ADR

## Skip qachon

Bugfix · code style · vaqtinchalik workaround · mavjud ADR'ni kichik tuzatish → ADR EMAS.

## Workflow

### 1. Kerakli ma'lumot

Foydalanuvchidan: **title** · **context** (muammo+cheklov) · **alternatives** (rad sabab) · **decision** (sabab) · **affects** (modullar) · **stages** · **verification cmd**.

### 2. Keyingi ID

```bash
REPO=$(git rev-parse --show-toplevel)
ls "$REPO"/docs/adr/[0-9]*.md | tail -1   # → keyingisi +1
```

### 3. Fayl yaratish

`docs/adr/NNNN-kebab-title.md` — `docs/adr/template.md` ni asos qiling. Frontmatter (id, status=proposed, date, agent, model, affects, liquibase, entities, verification, related) + Y-Statement + Michael Nygard sections (Status, Context, Decision, Alternatives, Consequences, Implementation, Verification).

**Y-Statement:** `Quyidagi savol uchun: <X>, biz <yo'l>'ni tanladik (<muqobil> o'rniga), chunki <sabab>; oqibatda <natija>.`

### 4. Index yangilash

`docs/adr/README.md` ichidagi index jadvaliga qator qo'shish:
```
| [NNNN](NNNN-title.md) | Full Title | Proposed | YYYY-MM-DD |
```

### 5. CLAUDE.md (kuchli ta'sir bo'lsa)

Modul boundary / schema / security qaror → root `CLAUDE.md` "Architecture Decision Records" jadvaliga qator. Modul-darajadagi `CLAUDE.md`'ga reference.

### 6. CHANGELOG

`[Unreleased]` ostida `### Documented`:
```
- ADR-NNNN: <title>
```

## Status semantikasi (AgDR 2026)

| Status | Ma'no | Kod % |
|--------|-------|-------|
| proposed | Taklif | 0 |
| accepted | Qabul, hali boshlanmagan/qisman | 0-50 |
| in-progress | Stage'lar bajarilmoqda | 50-95 |
| implemented | Tugagan | 100 |
| deprecated | Eskirgan/superseded | — |

> "Accepted" = qaror, **implementatsiya emas**. Implementation status `## Implementation` bo'limida ✅/⏳/❌.

## See also

- `docs/adr/template.md` · `docs/adr/README.md` · `docs/adr/0008-*.md` (yaxshi misol)
- `.claude/rules.md` "ADR Status Drift Detection"
- `.claude/skills/adr-verify/SKILL.md` — bajarilganlikni tekshirish
