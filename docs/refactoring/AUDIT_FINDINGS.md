# Enterprise Audit — Yakuniy topilmalar

**Sana:** 2026-04-21
**Auditorlar:** 5 parallel agent (security, repository, entity, api, build)
**Jami topilgan:** 38 ta masala (12 tuzatildi, 17 document qilindi, 9 business qaror talab qiladi)

---

## ✅ Tuzatilgan muammolar (shu sessiyada)

### Round 1 (initial refactor)
1. Email regex bug (V009) — `[^@\s]` → `[^@[:space:]]`
2. V012 data_source CHECK constraint qo'shildi
3. V013 `employee_type_code` → `position_type_code` rename (5 fayl)
4. V009-V013 103 ta INSERT ON CONFLICT idempotent
5. V009 rollback — 7 ta FK degraded state fail-safe
6. S008 comment clarify
7. master.yaml CUBA dependency hujjat

### Round 2 (re-audit)
8. **HokimiyatClassifierService.HOKIMIYAT_CLASSIFIER_MAP** — 20 ta mapping yangi jadvallarga
9. HokimiyatClassifierService.buildItemsSql — `is_active` aliasing qo'shildi
10. ClassifierLegacyService.getSingleClassifier() — `OLD_CLASSIFIER_MAP` lookup
11. V013 FK lengths: `VARCHAR(2/10)` → `VARCHAR(20)` (5 ustun)
12. V013 employee.email CHECK constraint
13. ClassifierWebService audit gap — `created_by/updated_by` + SecurityContextHolder

### Round 3 (deep audit)
14. **5 ta missing repository** yaratildi:
    - ClassTypeRepository
    - ScoreTypeRepository (with findByGradeSystemCode)
    - ContractSummaTypeRepository
    - ContractClassRepository
    - EmployeeAgeRangeRepository

---

## 📋 Hujjatlashtirilgan (business qaror talab qiladi)

### 🚨 STUDENTDIPLOMA VS DIPLOMA — DUPLICATE @Table
**Fayl:**
- `domain/src/main/java/uz/hemis/domain/entity/student/StudentDiploma.java:25`
- `domain/src/main/java/uz/hemis/domain/entity/finance/Diploma.java:33`

**Bug:** Ikkala entity `hemishe_e_student_diploma` jadvalga map qilinadi. Runtime'da Hibernate'da conflict bo'lishi mumkin.

**Qaror talab qiladi:** qaysi entity'ni saqlab qolish (Diploma finance/ yoki StudentDiploma student/), boshqasini o'chirish yoki rename qilish.

**Vaqtinchalik ta'sir:** Build o'tadi (Hibernate 2 ta @Entity'ni xush ko'rmaydi lekin xatoga olib kelmaydi). Ishlayotganga qaraganda ikkala mapping ham mavjud bo'lib, Hibernate ularni bir-biridan alohida traktovat qiladi.

### Language @SQLRestriction
**Fayl:** `domain/src/main/java/uz/hemis/domain/entity/reference/Language.java`

**Masala:** Language extends AuditableEntity (has `deleted_at`) but comment says "no soft delete — uses is_active". Agar `deleted_at` bo'lsa, soft-deleted rows query'larda qaytadi.

**Qaror talab qiladi:** Language'ni ReferenceEntity'ga o'tkazish YOKI `@SQLRestriction("deleted_at IS NULL")` qo'shish.

---

## 🔐 Security topilmalari (infrastruktura)

### 🚨 KRITIK — production deploy'dan oldin
1. **JWT access token TTL 30 kun** — `TokenService.java:52` — Industry standard 15 daqiqa
2. **HS256 algorithm** — `SecurityConfig.java:283` — RS256 ga o'tish tavsiya
3. **Cookie.setSecure(false) production'da** — `WebAuthCookieService.java:44`
4. **Redis parol production'da enforce qilinmagan** — `RedisConfig.java:94`
5. **Rate limiting** — service mavjud, login endpoint'ga ulanganligini verify qilish kerak

### 🔴 SECRETS — darhol rotate qilinishi kerak
- `DB_MASTER_PASSWORD` — `.env:23` (git'ga commit bo'lgan)
- `JWT_SECRET` — `.env:80` (git'ga commit bo'lgan)
- `.env.example` yaratish template sifatida

### O'RTA darajali
6. Univer classifier endpoint (`/app/rest/v2/services/classifiers/info`) — permitAll, IP whitelist tavsiya
7. Dev'da `error.include-stacktrace: always` — o'chirish
8. Token type claim null check yo'q — `TokenService.java:180`
9. Actuator prod'da — `mappings` va `liquibase` exclude qilingan ✓

---

## ⚙️ Build/Config kamchiliklari

### Checksum mismatch xavfi
**Ta'sir:** V009 yangilandi (ON CONFLICT + 2 yangi jadval) — agar DB'ga allaqachon tushurilgan bo'lsa, Liquibase checksum mismatch bilan fail qiladi.

**Yechim** (test DB uchun):
```bash
# Option 1: liquibaseClearChecksums task qo'shish build.gradle'ga
./gradlew :domain:liquibaseClearCheckSums
./gradlew :domain:liquibaseUpdate

# Option 2: DB drop + re-run
./gradlew :domain:liquibaseDropAll
./gradlew :domain:liquibaseUpdate
```

### Boshqa optimizatsiyalar
- Prod replica `leak-detection-threshold` 60s → 30s
- Prod Redis Jedis `max-active` 8 → 16
- `.env.example` yaratish
- API version springdoc config'ga qo'shish

---

## 🏗️ API/DTO masalalari (ko'rib chiqish kerak)

### HIGH
- **PageResponse.of(Page, List)** `numberOfElements=content.size()` — aslida to'g'ri (transformed content size)
- **Response wrapping inconsistency** — ba'zi controller'lar `ResponseWrapper<T>` ishlatadi, ba'zilari `ResponseEntity<T>` — standartlash kerak
- **api-external security** — S2S endpoint'lar uchun API key / certificate validation yo'q

### O'RTA
- **ClassifierItemUpdateDto** — @NotBlank validation yo'q
- **AuditLogController** `Map<String, Object>` qaytaradi — strongly-typed DTO tavsiya
- **CubaCatchAllController** — error format ErrorResponse DTO'ga mos emas

---

## 📊 Yakuniy baho

| Qatlam | Auditdan keyin | Status |
|---|---|---|
| Entity layer | 104 ReferenceEntity, dual-mapping FK, type consistency | ✅ |
| Repository layer | 99 → 104 (5 yangi) | ✅ |
| Migration (V001-V013) | Idempotent, FK fail-safe rollback | ✅ |
| Classifier services (4 ta) | Schema-adaptive, aliasing | ✅ |
| Security | Funksional, lekin 4 kritik item production'gacha | ⚠️ |
| Build/Config | Ishlayapti, 3 optimizatsiya tavsiya | ⚠️ |
| API contract | 2 entity duplicate @Table kutmoqda | ⚠️ |

## Qolgan ishlar (keyingi iteration'lar)

### Darhol (refactor davomi)
1. StudentDiploma vs Diploma qaror (business + kod)
2. Language entity refactor
3. liquibaseClearCheckSums task build.gradle'ga

### Production deploy'dan oldin
4. JWT TTL 30 kun → 15 daqiqa
5. `.env` secrets rotation + `.env.example`
6. RS256 JWT migration
7. api-external S2S security

### Optimizatsiya
8. API response wrapping standartlashtirish
9. PageResponse.of(Page, List) semantics documentation
10. Integration test (auto_compare.js + univer sync)
