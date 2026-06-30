# Architecture Decision Records (ADR)

Bu katalog HEMIS-back loyihasidagi muhim arxitektura qarorlarini saqlaydi.

> **HEMIS-back qisqacha:** O'zbekiston Oliy ta'lim vazirligi tasarrufidagi **MARKAZIY** Java/Spring Boot server (vazirlik darajasidagi 1 deploy/cluster). `/home/adm1n/projects/startup/old-hemis` (**CUBA Platform 7.3 — Java + Groovy**, Haulmont) ning **Spring Boot 4 + Java 25** ga qayta yozilishi va optimizatsiyasi.
>
> **4 ta asosiy maqsad:**
> 1. **230 OTM dan aggregation** (markaziy ma'lumot ombori)
> 2. **Klassifikator distribution** (markaz → OTM push, yagona manba)
> 3. **Qoidalar joriy qilish** (talaba kiritish, baho lock, vaqt cheklov)
> 4. **Davlat integratsiya** (MyGov, MSPD, BIMM, Tax, GUVD)
>
> **Mijozlari:** Vazirlik admin (web), 224 ta Univer Yii2 PHP (per-OTM, REST), davlat sistemalari (S2S).

## ADR nima?

**ADR (Architecture Decision Record)** — bir arxitektura qarorining yozma izohi. Bitta ADR = bitta qaror.

Maqsadi:
- **Tarix saqlash** — kelajakdagi developerlar nimaga shu yo'l tanlanganini biladi
- **Onboarding tezlashtirish** — yangi a'zo loyihaga 1 hafta o'rniga 1 kunda kiradi
- **"Nima uchun?" savolini kamaytirish** — har qaror uchun kontekst bor
- **Refactoring xavfsizroq** — qaysi qaror qaysi sababga asoslangani aniq

ADR — bu qisqa Markdown fayl (1-2 sahifa). Git tarixi orqali version-controlled.

## Naming convention

```
docs/adr/NNNN-short-title.md
```
- `NNNN` — 4 raqamli ketma-ket nomer (0001, 0002, ...)
- `short-title` — kichik harf, defis bilan (`java-25-upgrade`, `audit-db-isolation`)

## Index

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [0001](0001-building-table-design.md) | University Buildings — Alohida jadval + Lifecycle log + Coordinates NULLABLE | Implemented | 2026-04-21 |
| [0002](0002-java-25-upgrade.md) | Java 21 → 25 LTS migration | Implemented | 2026-05-04 |
| [0003](0003-audit-db-isolation.md) | Audit DB alohida hemis_audit bazasi | Implemented (disabled-by-default) | 2026-05-04 |
| [0004](0004-api-university-module.md) | api-university yangi modul | Implemented | 2026-05-04 |
| [0005](0005-oauth-client-credentials.md) | OAuth client_credentials grant 224 OTM uchun | In Progress (server done; 224 OTM rollout deferred) | 2026-05-04 |
| [0006](0006-classifier-h-prefix.md) | Klassifikator jadvallariga `h_*` prefiks konventsiyasi | Implemented | 2026-05-04 |
| [0007](0007-sync-architecture-evolution.md) | Sync Architecture — Selective Kafka adoption (Greenfield) | Partially Implemented | 2026-05-06 |
| [0008](0008-api-legacy-entity-rebinding.md) | api-legacy uchta entity'ni eski jadvallarga qaytarish (Legacy* prefiks) | Accepted (Stage 1/3/4/5 done; Stage 2 User → documented exception) | 2026-05-07 |
| [0009](0009-jwt-ttl-and-refresh-rotation.md) | JWT TTL qisqartirish (12h→1h) + Refresh rotation + jti/kid | Implemented | 2026-05-07 |
| [0010](0010-employee-sync-outbox-implementation.md) | Employee Sync Outbox Implementation | Implemented | 2026-05-08 |
| [0011](0011-swagger-multi-group-strategy.md) | Swagger Multi-Group Strategy (web/legacy/university/external) | Accepted | 2026-05-10 |
| [0012](0012-webhook-outbound-infrastructure.md) | Webhook Outbound Infrastructure (markaz → 224 Univer) | Implemented (hardened 2026-05-27: K1/K2/Y1/O) | 2026-05-13 |
| [0013](0013-business-rule-enforcement.md) | Business Rule Enforcement Foundation (Rules Engine) | Partially Implemented (foundation done; policy klasslar pending) | 2026-05-21 |

## Status qiymatlari

> **Eslatma:** frontmatter `status:` slug-format ishlatadi (`proposed`/`accepted`/`in-progress`/`partially-implemented`/`implemented`/`implemented-disabled-by-default`/`superseded`); Index ustuni Title-case ko'rsatadi. **Ground-truth = frontmatter.**

- **Proposed** — taklif, hali muhokama jarayonida (kod yo'q)
- **Accepted** — qaror qabul qilingan, implementatsiya boshlandi yoki rejada
- **In Progress** — qaror amalga oshirilmoqda (Stage X / N)
- **Partially Implemented** — bir qism deliver qilingan, qoldiq aniqlangan
- **Implemented** — to'liq amalga oshirilgan, kod va kuzatuv mavjud
- **Implemented (disabled-by-default)** — kod tayyor, lekin default flag bilan o'chiq (masalan ADR-0003 audit DB)
- **Deprecated** — eskirgan, lekin hali ham qoidaviy kuchda
- **Superseded by ADR-NNNN** — boshqa ADR bilan almashtirilgan

> **Eslatma:** "Accepted" status faqat qarorni anglatadi, implementatsiyani EMAS. Implementatsiya holati alohida `**Implementation:**` bo'limida ko'rsatilishi shart (✅ done / ⏳ pending / ❌ blocked).

## Yangi ADR qachon yoziladi?

ADR yozish kerak bo'lgan holatlar:
- ✅ Yangi modul yoki kutubxona qo'shilganda
- ✅ Texnologiya stack'da o'zgarish (Java versiyasi, DB engine)
- ✅ Xavfsizlik yoki performans masalasi (cache strategy, auth flow)
- ✅ Schema yoki API breaking change
- ✅ Migration plan (eski ↔ yangi)

ADR yozish KERAK EMAS:
- ❌ Kichik bugfix yoki refactoring
- ❌ Code style preferences (linter qoidalari)
- ❌ Vaqtinchalik workaround

## Template

Yangi ADR yaratish uchun [template.md](template.md) dan foydalaning.

## References

- [Michael Nygard — "Documenting Architecture Decisions"](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ThoughtWorks — ADR adoption](https://www.thoughtworks.com/insights/blog/architecture/scaling-architecture-conversationally)
- [GitHub — adr-tools](https://github.com/npryce/adr-tools)
