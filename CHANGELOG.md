# Changelog

Loyihaning sezilarli o'zgarishlari. Format [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) asosida.
Versiyalash: [SemVer](https://semver.org/spec/v2.0.0.html).

Tarixiy ADR'lar uchun: [`docs/adr/`](docs/adr/).

---

## [Unreleased]

### Architecture
- **Webhook Outbound Infrastructure** (ADR-0012): Markaz → 224 Univer real-time event push. 5-bosqichli implementatsiya (Sprint 1-5):
  - **Sprint 1 (Foundation):** V015 migration (`webhook_target` + `webhook_delivery_log` jadvallar + 7 ta index), `OutboxEvent` JPA entity (V014 outbox_event'ga map), `WebhookTarget`/`WebhookDeliveryLog` entitylar + repository, `spring-kafka` dependency + `application.yml` Kafka config (idempotent producer, manual ack consumer).
  - **Sprint 2 (Producer):** `KafkaTopicConfig` (7 ta topic auto-create: classifier/rule/employee/student/university domain + webhook events + DLQ), `OutboxEventPublisher` programmatic API (`Propagation.MANDATORY`), `OutboxPoller` `@Scheduled(1s)` FOR UPDATE SKIP LOCKED + retention cleanup (cron 03:00).
  - **Sprint 3 (Consumer + Dispatcher):** `HmacSigner` (SHA-256 + constant-time verify), `WebhookEventEnvelope` canonical JSON, `WebhookFanoutConsumer` (domain topic → per-OTM message), `WebhookDispatcher` (Kafka → REST POST + retry routing 4xx/5xx), `WebhookSecretVault` in-memory cache, `WebhookRetryScheduler` DB queue (5s interval).
  - **Sprint 4 (Admin API):** 5 ta DTO + `ConflictException`, `WebhookSecretService` (`whsec_xxx` + bcrypt-12 hash), `WebhookTargetService` (CRUD + regenerate-secret + delivery log views), `WebhookTargetController` (`/api/v1/web/admin/webhooks/**` — 11 ta endpoint, @PreAuthorize webhook.view/create/update/delete/manage).
  - **Sprint 5 (Polish):** `WebhookMetrics` (Prometheus `hemis_webhook_dispatch_total` counter + `hemis_webhook_dispatch_duration_seconds` histogram per OTM), M004 migration (5 ta webhook permission + SUPER_ADMIN/MINISTRY_ADMIN grant), **sandbox endpoint** `POST /api/v1/web/admin/webhooks/{id}/test` (synthetic event yuborish), ADR-0012, `docs/integration/webhook-implementation-guide.md` (Univer dasturchilari uchun PHP Yii2 namuna).

### Documentation
- **ADR-0012** yaratildi: Webhook Outbound Infrastructure.
- `docs/integration/webhook-implementation-guide.md` (yangi papka): Univer kod jamoa + 224 OTM IT uchun deploy checklist + Yii2 controller/worker/migration namuna.
- `docs/architecture/hemis-univer-integration-patterns.html` — animatsion arxitektura tahlili (8 pattern + Kafka deep dive + 3 sequence diagram).
- `docs/architecture/kafka-deployment-strategy.html` — Kafka deploy variantlari (Strimzi tavsiya).

### Security
- **Production'da Swagger butunlay o'chirilgan** (ADR-0011): `application-prod.yml`'ga `springdoc.api-docs.enabled: ${SWAGGER_ENABLED:false}` + `swagger-ui.enabled: ${SWAGGER_ENABLED:false}` qo'shildi. `application.yml`'da default `${SWAGGER_ENABLED:true}` (dev'da ochiq). `SecurityConfig.java`'da swagger requestMatchers'lar `if (swaggerEnabled)` bloki ichida — false bo'lsa 401 qaytadi. Sabab: ichki API strukturasi tashqaridan ko'rinmasligi.

### API Documentation
- **OpenAPI 4-group refactor** (ADR-0011): `webApi` (web), `legacyApi` (legacy), `universityApi` (university), **yangi `externalApi` (external)** — har biri auditoriya bo'yicha aniq ajratilgan. Eski group nomlari (`Web Frontend API v1`, `university`, `university-new`) → slug-friendly (`web`, `legacy`, `university`, `external`). `pathsToExclude` har group'da to'g'ri (URL overlap yo'q).
- `OpenApiConfig.Info` description haqiqatga moslandi: Spring Boot 3.5.7→4.0.6, Java 21→25, 170+→780+ endpoint, JWT TTL 30 days→12h+7d (ADR-0009 reference). Placeholder linklar (docs.hemis.uz/*, github.com/hemis-uz, telefon, Telegram) olib tashlandi.
- `applicationVersion` MANIFEST'dan (`Implementation-Version` — Spring Boot Gradle plugin avtomatik). `Title` hardcode → `applicationName` ENV'dan.
- **`basicAuth` security scheme qo'shildi** — CUBA legacy `/app/rest/v2/oauth/token` uchun (Basic Authorization header). Swagger UI "Authorize" tugmasidan client:secret kiritib token olish mumkin.
- **`defaultResponsesCustomizer` Bean** — har endpoint'ga 401/403/500 default `ApiResponse` qo'shadi. api-legacy `@ApiResponse` qoplama 71.8%→100% (manual override saqlanadi).
- **`fallbackSummaryCustomizer` Bean** — 532 ta `@Operation`'siz endpoint'ga `operationId` + HTTP method'dan avtomatik summary generatsiya (`loadStudentByPinfl` + GET → "Get: load student by pinfl"). Manuel `@Operation(summary=...)` saqlanadi. Audit (Rob Pike "avval tekshir" qoidasi) avvalgi `grep -c @Operation` hisoblashi noto'g'ri ekanligini aniqladi (`awk` script orqali har endpoint tekshiruvi: 532 ta to'liq @Operation siz).
- **Tag izolatsiyasi:** 70 ta numbered legacy tag (`01.Token` — `70.Qo'shimcha xizmatlar`) faqat `legacyApi` group'ida ko'rinadi. Avval `hemisOpenAPI()` global Bean'da `.tags(apiTags())` o'rnatilgan edi → web/university/external dropdown'larida ham bo'sh "01.Token..70.Qo'shimcha" tag'lari chiqayotgan edi. Endi `legacyApi.addOpenApiCustomizer` ichida `setTags(apiTags())`, qolgan group'lar controller `@Tag` annotatsiyasidan avto-discover.
- `webApi` eskirgan `setTags` ro'yxati (Translation Admin, Language API, Dashboard Statistics — controller'larda mavjud emas) olib tashlandi. Tag'lar avtomatik `@Tag` annotatsiyalardan discover qilinadi.
- `application-dev.yml:207` `urlsPrimaryName: all` → `urlsPrimaryName: web` (eski "all" group hech qachon mavjud bo'lmagan).

### Documentation
- **ADR-0011 yaratildi**: Swagger Multi-Group Strategiyasi va Production Xavfsizligi.
- `common/CLAUDE.md` `ResponseWrapper` bo'limi real klassga moslangan: `record + timestamp + page` → `class + 4 maydon (success, message, data, error)`. ErrorResponse strukturasi alohida tushuntirildi. Pagination uchun Spring `Page<T>` to'g'ridan-to'g'ri `data` ichida (`ResponseWrapper<Page<T>>`).
- `api-web/CLAUDE.md` JSON misollar real ResponseWrapper formatiga moslangan (timestamp, page maydonlar olib tashlandi).

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
