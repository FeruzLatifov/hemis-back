# Changelog

Loyihaning sezilarli o'zgarishlari. Format [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) asosida.
Versiyalash: [SemVer](https://semver.org/spec/v2.0.0.html).

Tarixiy ADR'lar uchun: [`docs/adr/`](docs/adr/).

---

## [Unreleased]

### Audit Hardening (2026-05-21 → 2026-05-22) — P0+P1+P2+P3 yopildi

8 ta commit, 54 fayl, +3435/-329 qator. ~30 audit item yakunlandi
(asoslar to'liq; konkret biznes-policy class'lar, Sealed-Secrets va vazirlik
credential ulanishi keyingi sprint). Univer kontrakt **175/175 MATCH**
saqlandi (2026-05-22 jonli tasdiqlash).

**Infrastructure (fix — P0 production blockers):**
- `Dockerfile` — `eclipse-temurin 21-jdk/jre-alpine` → **25** (`build.gradle.kts` `JavaLanguageVersion(25)` bilan moslashtirildi; 21-jre prod'da `UnsupportedClassVersionError` xavfini bartaraf etdi).
- `SystemConfiguration` entity `@Table(name="configurations")` → **`configuration`** + index nomlari singular — V012 schema bilan drift bartaraf etildi (Hibernate boot `relation "configurations" does not exist` xatosi).
- `helm/hemis-back/values.yaml` — `livenessProbe.path: /actuator/health` → **`/actuator/health/liveness`**, `readinessProbe.path` → `/actuator/health/readiness` (application.yml liveness=livenessState, readiness=readinessState,db,redis group'lari sozlangan — DB tushganda pod restart cascade'ni oldini oladi).
- `application-prod.yml` Prometheus expose qoldirildi (cluster'da hozir deploy yo'q, Sentry self-hosted observability uchun yetadi; kelajakda 1 qator `include` ga `prometheus` qo'shiladi).

**Security:**
- **OAuth weak-default soft-fail** (`LegacyOAuthClientProperties`): `EnvironmentAware` + `@PostConstruct validateCredentialStrength()` — `client/secret/admin/password/changeme/test/demo`/qisqa (<16 char) qiymatlar prod profile'da `log.error` + Sentry auto-capture, dev'da `log.warn`. **Boot fail YO'Q** — 200+ legacy klient (Univer Yii2, CUBA desktop) zarar ko'rmaydi. Sabab: `k8s-secret.env` shipping default `OAUTH_CLIENT_ID=client / OAUTH_CLIENT_SECRET=secret` brute-force xavfi. 6 yangi unit test.
- **`BusinessRuleException` (HTTP 422)** — common module foundation (`ruleCode + message + cause`). `GlobalExceptionHandler` `@ExceptionHandler` mapping → 422 Unprocessable Entity. Kelajak policy class'lar (StudentInsertionPolicy, GradeEditPolicy, EnrollmentWindowGuard) uchun. 4 yangi unit test.
- **gitleaks pre-commit hook** + `.gitleaks.toml` (6 HEMIS-specific rule: jwt-secret, db-password, oauth-default, webhook-secret, redis-password, external-api-key; allowlist `build/`+`docs/`+`${VAR}`). `scripts/git-hooks-pre-commit` — graceful skip (gitleaks o'rnatilmagan bo'lsa WARN, mavjud bo'lsa avtomatik secret-scan).
- **SQL injection defense-in-depth** — `common/util/SqlTableValidator` regex `^(hemishe_[her]_[a-z_]+|h_[a-z_]+)$` guard. `VerificationService.loadSimpleReference` va `LegacyClassifierReferenceLoader.loadSimpleReference` SQL build oldidan validate (callsite'lar bugun hardcoded literal — future dynamic caller'lar guard'lab qo'yiladi). 50+ parameterized test (injection vector + malformed).

**Database — M002 CONCURRENTLY refactor (zero-downtime):**
- M002 monolith (1 ta `DO $$` block, 26 ta `CREATE INDEX`, `ACCESS EXCLUSIVE LOCK` 30+ daqiqa prod'da) → **5 ta alohida changeset** (`M002a_pg_trgm_extension`, `M002b_student_indexes` 23 idx, `M002c_student_pinfl_master_unique`, `M002d_diploma_trigram`, `M002e_student_meta_uid_unique`). Har biri `runInTransaction: false` + `CREATE INDEX CONCURRENTLY` + `splitStatements: true` + Liquibase `preConditions` (jadval mavjudligi + UNIQUE uchun duplicate-check, MARK_RAN gracefully). Rollback ham CONCURRENTLY.
- 5 yillik real baza (1.15M+ talaba) — prod migration `ACCESS EXCLUSIVE LOCK` 30+ daqiqa downtime, Univer 224 OTM write'lari timeout. CONCURRENTLY = online build, write/read uzilmaydi.
- **CONCURRENTLY pattern qoidaga aylantirildi:** [`LIQUIBASE_GUIDE.md`](.claude/LIQUIBASE_GUIDE.md) "CONCURRENTLY pattern — MAJBURIY (1M+ row jadvallar)" to'liq bo'lim, [`domain/CLAUDE.md`](domain/CLAUDE.md) qisqartirilgan eslatma, [`liquibase-reviewer.md`](.claude/agents/liquibase-reviewer.md) subagent uchun "red flags" — PR review paytida avtomatik tekshiriladi.

**Observability:**
- **`Sentry.captureException` kritik domain joylariga (6 joy)** — avval faqat 2 ta (GlobalExceptionHandler, WebExceptionHandler), domain silent fail Sentry'ga yetib bormas edi: `WebhookDispatcher.consume()` ERROR + kafka_offset/partition; `.dispatchWithRetry()` unexpected Exception ERROR + event_id/attempt; `.sendToDlq()` **FATAL** + dlq_topic (event tamoman yo'qoladi); `WebhookRetryScheduler.processDueRetries()` ERROR; `.retryOne()` WARNING + delivery_log_id; `WebhookFanoutConsumer` ERROR + kafka_topic/aggregate_id; `OutboxPoller.pollAndPublish()` ERROR; `EmployeeSyncConsumer.consume()` deserialize **FATAL** (poison pill); process WARNING. PII xavfsizlik: PINFL tag/extra'da YO'Q (rules.md Rule #7).
- `service/build.gradle.kts`: `compileOnly("io.sentry:sentry-spring-boot-4:8.40.0")` + `testRuntimeOnly` (api-web bilan bir xil pattern — runtime app modul'dan).
- **Custom metrics — 3 yangi class** (WebhookMetrics pattern): `EmployeeSyncMetrics` (`hemis_employee_sync_total{status,university}` + duration + `deserialize_failed`); `OutboxMetrics` (`hemis_outbox_publish_total{status,topic}` + duration + `hemis_outbox_queue_depth` Gauge + retention_deleted); `WebhookFanoutMetrics` (`hemis_webhook_fanout_total{topic,status}` + targets). `@Component` ro'yxatda — call site wire'lash keyingi sprint.
- **Micrometer Tracing** — `micrometer-tracing-bridge-otel` + `sentry-opentelemetry-bootstrap:8.40.0`. Logback `%X{traceId}/%X{spanId}` pattern (logback-spring.xml) endi avtomatik to'ladi (span propagation).

**Testing — +98 yangi `@Test` (markaziy auth/webhook pipeline qoplandi):**
- `WebhookDispatcherTest` (8) — Mockito spy + `doHttpPost` protected (brittle `RestClient.Builder` fluent-chain mock o'rniga). 2xx success, 4xx terminal FAILED, 5xx retry+next_retry_at, 5xx attempt 3/3 DLQ + Kafka publish, ResourceAccessException retry, exponential backoff sanity, DLQ Kafka publish failure silenced, per-target maxRetries override.
- `WebhookRetrySchedulerTest` (8) — empty queue early return, 3 due retries processed, retryOne placeholder DLQ, per-item isolation, `@Scheduled` top-level swallow, batchSize PageRequest, cleanup cutoff now-60d, cleanup 0 rows silent.
- `CookieJwtAuthenticationFilterTest` (11) — no token pass-through, valid Bearer/cookie, header wins over cookie, blacklisted reject, no JTI authenticate, JwtException swallow, unexpected exception non-fatal, existing auth preserved, non-Bearer fallback, other cookies ignored.
- `TokenBlacklistServiceTest` (14) — addToBlacklist TTL math, key prefix, null/empty/already-expired/expiry=now guards; isBlacklisted hasKey null (Redis quirk) fail-open; removeFromBlacklist null/empty no-op; clearAllBlacklist scan + delete per match.
- `LegacyOAuthClientPropertiesTest` (+6) — weak default dev/prod no-throw, strong creds OK, short ID warning, no environment safe, other weak values (`admin/password`).
- `ExceptionClassesTest.BusinessRuleException` (+4) — ctor ruleCode+message+cause, RuntimeException, ruleCode required.
- `SqlTableValidatorTest` (50+ parameterized) — accept legacy + modern `h_*`, reject injection vectors (`;DROP TABLE`, `--`, UNION, schema escape, case mismatch).
- `WireMockSampleTest` (3) — HTTP stub infra pattern (200 stub, header matcher, 503 error). MSPD/BIMM/GUVD client testlari uchun foundation.
- `service/build.gradle.kts`: `testImplementation("org.wiremock:wiremock-standalone:3.10.0")`.

**Documentation:**
- `CLAUDE.md` 154 → 143 qator (DevGenius 2026 tamoyillari): ADR jadval kompakt (12 ADR → [`docs/adr/README.md`](docs/adr/README.md) link), Further Reading kompakt (Canonical/Reference/Context guruh), yangi `## Workflow (senior tarz)` bo'limi — Plan Mode 80% + staff-review, Simplicity Mandate (Boris Cherny), Avval mavjud kodni o'rgan, Repetition → command, Xato → qoida.
- `docs/runbooks/webhook-delivery-failure.md` (yangi) — 5 failure mode diagnostic SQL/komanda (5xx retry loop, 403 HMAC fail, DLQ grow, Univer worker offline, rule.push DEFERRED) + lokal e2e test stack qadamlari.
- `docs/runbooks/jwt-secret-rotation.md` (yangi) — 9 bo'lim: trigger, pre-flight, generate (`openssl rand -base64 64`), K8s Secret update, RollingUpdate restart, validation, eski session bekor strategy, post-rotation cleanup, incident response (leaked secret).
- `docs/runbooks/audit-db-partition.md` (yangi) — 7 yillik retention strategiyasi (RANGE PARTITION BY created_at yearly), `pg_partman` avto-rotation namuna, migration template (V005 kelajakda yoziladi — actual data yo'q, AUDIT_ENABLED=false).
- `docs/operations/external-credentials-pending.md` (yangi) — 7 ta tashqi integratsiya stub holatida (Billing, Email/SMTP, Tax, UzASBO, Mehnat, MyGov, OneID), har biri talab credential + effort.
- `docs/integration/webhook-implementation-guide.md` sec 3.3 sinxronlash — eski `hemishe_h_*` prefiks → defensive `h_*` (actual `ApplyHemisEventJob.php:103` 2026-05-19 fix).
- `OpenApiConfig.apiTags()` 67 → 83 tag (ADR-0011 polish): 07.Doktorant, 08.O'quv reja, 08.Yuridik shaxs, 10.Imtihonlar, 11.Fanlar, 12.Dars jadvali, 14.GUVD, 30.Inspeksiya, 32.Akademik, 39/40 administrative teacher, 62-64 Hokimiyat/shaxsiy/yuridik, 98.Xabarlar, 99.Test.
- `application-test.yml` comment'lari to'g'rilandi (ikki test infra: H2 light slice + Testcontainers PG-16 — aniq tushuntirildi).

**Qoldi (sizning qaroringizga):**
- Konkret biznes-policy class'lar (`StudentInsertionPolicy`, `GradeEditPolicy`, `EnrollmentWindowGuard`) — foundation tayyor, lekin yangi feature
- `AUDIT_DB_PASSWORD` ajratish (`.env` / `k8s-secret.env`) — ADR-0003 isolation, deploy paytida siz
- Sealed-Secrets Helm integration — alohida deploy sprint
- Vazirlik credential keldi paytida: Email SMTP / Tax / UzASBO / Mehnat actual ulanishi (Email birinchi — infra tayyor)
- Konkret prod-time JWT secret rotation amalga oshirish (runbook tayyor)

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
