# Changelog

Loyihaning sezilarli o'zgarishlari. Format [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) asosida.
Versiyalash: [SemVer](https://semver.org/spec/v2.0.0.html).

Tarixiy ADR'lar uchun: [`docs/adr/`](docs/adr/).

---

## [Unreleased]

### Tooling
- `.claude/CLAUDE.md` symlink olib tashlandi (root `CLAUDE.md` bilan dublikat kontekst yuklash).
- `post-edit.sh` hook `.claude/settings.json` `PostToolUse` matcher'iga ulandi (Edit/Write/MultiEdit). Hook detektorlari: `@Data` on `@Entity`, `@ManyToOne`/`@OneToOne` without `LAZY`, AOP annotation on private method, hardcoded secret, SQL string concat, PII in logs, forbidden DDL on `hemishe_*`, missing rollback, plain secret in YAML.
- 5 ta subagentga `model: opus` qo'shildi (cache-strategist, cuba-format-checker, liquibase-reviewer, n-plus-one-detector, security-auditor).
- `review-pr.md` slash command'da `Agent` o'rniga `Task` tool ishlatiladi (Anthropic 2026 API).
- `companyAnnouncements` 14 → 7 punktga qisqartirildi (qolgan qoidalar `rules.md`/modul CLAUDE.md'larida bor).
- `autoMemoryEnabled: true` aniq belgilandi (`~/.claude/projects/<repo>/memory/MEMORY.md`).

### Documentation
- Yo'l xatolari tuzatildi: `/home/adm1n/startup/...` → `/home/adm1n/projects/startup/...` (CLAUDE.md, ENDPOINT_PORTING_GUIDE.md, port-endpoint.md, README.md).
- `old_hemis.json` referansi `hemis-tools/docs/old_hemis.md` + jonli `curl` workflow'ga almashtirildi (JSON fayl mavjud emas).
- `endpoint_tester.html` yangi joyga ko'chdi: `hemis-tools/docs/endpoint_tool/endpoint_tester.html`.
- `@` import sintaksisining yolg'on "on-demand" izohi tuzatildi (Anthropic memory hujjati: imports load at launch). Modul CLAUDE.md fayllarida 22 ta `@` reference oddiy markdown link'ga o'tkazildi (token tejash).
- `UNIVER_CONTRACT.md` boshiga endpoint son glossariy qo'shildi (67 contract / 175 test / ~659 method farqi).
- `common/CLAUDE.md` namuna kodida Spring import (`org.springframework.data.domain.Page`) olib tashlandi — converter `service` modulga ko'chirildi.

### Database
- `application.yml`'ga `hibernate.default_batch_fetch_size: 20` qo'shildi (lazy-load batch — N+1 SELECT loop'larni `ceil(N/20)` IN-clause query'ga aylantiradi).

### Documented (data unchanged)
- Spring Boot version izchillik: `.claude/context.md` `4.0.2 → 4.0.6` (haqiqiy versiya `build.gradle.kts:12`'da).

---

## 2026-05-04 — Schema cleanup

- `users` jadvali toza schema'ga o'tdi (41 → 30 ustun, 11 ta legacy CUBA olib tashlangan).
- Soft-delete + UNIQUE konflikti hal qilindi (partial UNIQUE indekslar).
- 224 OTM B2B uchun `oauth_client` migration plan tasdiqlandi (ADR-0005).
- Old-hemis CUBA `sec_user` parallel ishlaydi (HybridUserDetailsService).
- 5 ta klassifikator butun stack bo'ylab `h_*`/`H` prefiks oldi: `h_position_type`, `h_position`, `h_building_category`, `h_construction_material`, `h_roof_type` (Java: `HPositionType`, `HPosition`, `HBuildingCategory`, `HConstructionMaterial`, `HRoofType`). 224 OTM ekosistemi konvensiyasi (ADR-0006).
