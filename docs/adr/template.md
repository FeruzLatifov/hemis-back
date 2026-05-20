---
id: ADR-NNNN
status: proposed              # proposed | accepted | in-progress | implemented | deprecated | superseded
date: YYYY-MM-DD
deciders: hemis-team
agent: claude-code            # claude-code | human | mixed
model: claude-opus-4-7        # ishlatilgan model (agar agent yozgan bo'lsa)
affects:                      # qaysi modullar/papkalar ta'sirlangan
  - api-legacy
  - domain
liquibase:                    # tegishli changeset fayllari
  - V016_create_X.sql
entities:                     # tegishli JPA entity'lar
  - Department
verification: ./scripts/check_table_mappings.sh   # bajarilganlikni tekshirish komandasi
related:                      # tegishli ADR'lar
  - ADR-0006
---

# ADR NNNN: <Short title>

## Status

<Proposed | Accepted | In Progress | Implemented | Deprecated | Superseded by ADR-NNNN>

(Sana: YYYY-MM-DD)

> **Y-Statement:** Quyidagi savol uchun: <savol/muammo>, biz <yo'l>'ni tanladik, chunki <sabab>; oqibatda <natija>.

## Context

Qanday vaziyat? Qaysi muammo paydo bo'ldi? Nima sabab bu qarorni qabul qilish kerakligini keltirib chiqardi?

- Tarixiy kontekst
- Texnik cheklovlar
- Biznes talablari

## Decision

Qaysi qaror qabul qilindi? Aniq, bir ma'noli.

(Eng muhim qism — bitta paragraf yoki qisqa ro'yxat.)

## Alternatives Considered

Boshqa qaysi yo'llar ko'rib chiqilgan? Nimaga rad etilgan?

### Alternative 1: <Nom>
- Tasvir
- Afzalligi
- Kamchiligi
- **Rad etish sababi:** ...

### Alternative 2: <Nom>
- ...

## Consequences

### Positive (afzalliklar)
- ...

### Negative (kamchiliklar)
- ...

### Risks (xavflar va yumshatish)
- **Risk:** ...
  **Mitigation:** ...

## Implementation

Qanday amalga oshiriladi? Bosqichlar:

- [ ] Stage 1 — ...
- [ ] Stage 2 — ...
- [ ] Stage 3 — ...

> **Eslatma:** ADR `Accepted` qarorni anglatadi, **implementatsiyani EMAS**. Implementatsiya holati majburiy:
> - ✅ done
> - ⏳ pending / in progress
> - ❌ blocked (sabab: ...)

## Verification

Qarorning amalga oshirilganligini qanday tekshirish:

```bash
# Misol komandalar
./scripts/check_table_mappings.sh
./gradlew :domain:liquibaseStatus
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js
grep -rn "<entity_or_pattern>" <path>
```

**Acceptance criteria:**
- [ ] DDL applied (changeset ID: V###)
- [ ] Entity layer matches DDL (`@Table`, `@Column`)
- [ ] All controllers using correct imports
- [ ] Integration test 175/175 (api-legacy uchun)
- [ ] Pre-commit hook reject pattern qo'shilgan (agar kerak)

## References

- Code: `path/to/file.java`
- Schema: `domain/src/main/resources/db/changelog/changesets/V###_*.sql`
- Documentation: `link or file`
- RFC / Standard: ...
- Related ADRs: ADR-NNNN
