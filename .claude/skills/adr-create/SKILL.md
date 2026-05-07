---
name: adr-create
description: Create a new Architecture Decision Record (ADR) following the AgDR 2026 standard. Use when user wants to document a significant technical decision (new module, schema change, third-party library, security pattern, migration strategy). Don't use for: minor bugfixes, code style preferences, temporary workarounds.
---

# Create ADR

## When to invoke

Trigger phrases (foydalanuvchi):
- "yangi ADR yarat", "ADR yoz", "decision record"
- "qaysi yo'l tanlash kerak — A yoki B?" (qaror so'ralganda)
- "<modul/feature> uchun arxitektura qaror kerak"

**Skip cases** (ADR yozish KERAK EMAS):
- Kichik bugfix yoki refactoring
- Code style preferences (linter qoidalari)
- Vaqtinchalik workaround
- Mavjud ADR'ni kichik tuzatish

## Workflow

### 1. Collect inputs from user

Quyidagilarni so'rang:
- **Title** (qisqa, masalan: "Klassifikator jadvallariga h_ prefiks")
- **Context** — qanday vaziyat? Qaysi muammo? Qaysi tarixiy kontekst, texnik cheklov, biznes talab?
- **Alternatives** — qaysi yo'llar ko'rib chiqildi? Nimaga rad etildi?
- **Decision** — qaysi yo'l tanlandi va NEGA?
- **Affects** — qaysi modullar/papkalar/jadvallar ta'sirlanadi?
- **Implementation** — bosqichlar (Stage 1, 2, 3, …)
- **Verification** — qaror bajarilganligini tekshirish komandasi

### 2. Find next ADR number

```bash
ls docs/adr/[0-9]*.md | sort | tail -1
# Misol: 0008-* → keyingisi 0009
```

### 3. Generate ADR file

`docs/adr/template.md` ni asos qilib, yangi fayl yarating:
- Path: `docs/adr/NNNN-short-title-kebab-case.md`
- AgDR YAML frontmatter (id, status, date, agent, model, affects, liquibase, entities, verification, related)
- Y-Statement (1 qator)
- Michael Nygard format: Status, Context, Decision, Alternatives, Consequences, Implementation, Verification, References

### 4. Update README.md index

`docs/adr/README.md` ichidagi `## Index` jadvaliga yangi qator qo'shing:

```markdown
| [NNNN](NNNN-short-title.md) | Full Title | Proposed | YYYY-MM-DD |
```

### 5. Update CLAUDE.md (root)

Agar ADR loyiha bo'yicha kuchli ta'sirga ega bo'lsa (modul boundary, schema, security):
- Root `CLAUDE.md` "Architecture Decision Records" jadvaliga qator qo'sh
- Tegishli modul `CLAUDE.md` (api-legacy, domain, …) ichida ADR'ga reference

### 6. Update CHANGELOG.md

`[Unreleased]` bo'limida `### Documented` (data unchanged) ostida:

```markdown
- ADR-NNNN: <title>
```

## Status convention (AgDR 2026)

| Status | Anglatadi | Kod holati |
|--------|-----------|------------|
| `proposed` | Taklif, hali muhokama | 0% |
| `accepted` | Qabul qilingan, implementatsiya boshlanmagan/qisman | 0-50% |
| `in-progress` | Stage'lar bajarilmoqda | 50-95% |
| `implemented` | To'liq amalga oshirilgan | 100% |
| `deprecated` | Eskirgan, yangi ADR ga superseded | — |

> **MUHIM:** "Accepted" qarorni anglatadi — implementatsiyani **EMAS**. Implementation status alohida `## Implementation` bo'limida ko'rsatilishi shart (✅ done / ⏳ pending / ❌ blocked).

## Y-Statement format

```
Quyidagi savol uchun: <savol/muammo>,
biz <yo'l>'ni tanladik (boshqa <muqobil> o'rniga),
chunki <sabab>;
oqibatda <natija>.
```

Misol (ADR-0008):
> Quyidagi savol uchun: api-legacy ichida yangi schema entity ishlatilishi,
> biz Legacy* prefiks rebinding'ni tanladik (status quo o'rniga),
> chunki Univer 224 OTM split-brain bug riski mavjud;
> oqibatda 4 ta controller refactor + 175/175 test saqlanadi.

## Verification block (har ADR oxirida)

```markdown
## Verification

\`\`\`bash
# Bajarilganligini tekshirish
./scripts/check_table_mappings.sh
grep -rn "<pattern>" <path>
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js
\`\`\`

**Acceptance criteria:**
- [ ] DDL applied (V###)
- [ ] Entity layer matches DDL
- [ ] Controllers using correct imports
- [ ] Integration test passing
```

## See also

- `docs/adr/template.md` — to'liq AgDR template
- `docs/adr/README.md` — index va status qiymatlari
- `docs/adr/0008-*.md` — yaxshi misol (frontmatter + Verification + Implementation stages)
- `.claude/rules.md` "ADR Status Drift Detection" — status semantikasi
