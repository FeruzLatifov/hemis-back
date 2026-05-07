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
| [0001](0001-building-table-design.md) | University Buildings — Alohida jadval + Lifecycle log + Coordinates NULLABLE | Accepted | 2026-04-21 |
| [0002](0002-java-25-upgrade.md) | Java 21 → 25 LTS migration | Accepted | 2026-05-04 |
| [0003](0003-audit-db-isolation.md) | Audit DB alohida hemis_audit bazasi | Accepted | 2026-05-04 |
| [0004](0004-api-university-module.md) | api-university yangi modul | Accepted | 2026-05-04 |
| [0005](0005-oauth-client-credentials.md) | OAuth client_credentials grant 224 OTM uchun | Accepted (server) / In Progress (rollout) | 2026-05-04 |
| [0006](0006-classifier-h-prefix.md) | Klassifikator jadvallariga `h_*` prefiks konventsiyasi | Accepted | 2026-05-04 |
| [0007](0007-sync-architecture-evolution.md) | Sync Architecture — Kafka-first Approach (Greenfield) | Proposed | 2026-05-06 |
| [0008](0008-api-legacy-entity-rebinding.md) | api-legacy uchta entity'ni eski jadvallarga qaytarish (Legacy* prefiks) | Accepted (Stage 1) / Pending (Stages 2-5) | 2026-05-07 |

## Status qiymatlari

- **Proposed** — taklif, hali muhokama jarayonida (kod yo'q)
- **Accepted** — qaror qabul qilingan, implementatsiya boshlandi yoki rejada
- **In Progress** — qaror amalga oshirilmoqda (Stage X / N)
- **Implemented** — to'liq amalga oshirilgan, kod va kuzatuv mavjud
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
