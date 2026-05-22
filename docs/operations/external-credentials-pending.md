# Vazirlik tomonidan kelishi kutilayotgan credential/integration ro'yxati

> **Maqsad:** Hozirgi stub holatda turgan tashqi integratsiyalar — vazirlik
> rasmiy credential yoki API spec bergandan keyin actual ulanishi mumkin.
>
> **Ma'lumotnoma:** [audit hisoboti](../adr/) ichida P3 polish.

---

## Tezkor xulosa

| # | Integration | Hozirgi xulq | Univer kontrakti | Talab |
|---|-------------|--------------|------------------|-------|
| 1 | **Billing invoice/scholarship** | `[]` bo'sh massiv qaytaradi | Univer chaqiradi (`/services/billing/invoice`, `/services/billing/scholarship`) | Vazirlik billing DB schema yoki real API |
| 2 | **Email (MailService)** | `success: false` har doim | SMS ishlaydi (bimmService.smsUserPay), email branch stub | Vazirlik SMTP server creds: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, `MAIL_FROM` |
| 3 | **Tax/Soliq** (`/services/tax/rent`) | `Collections.emptyList()` qaytaradi | Univer chaqiradi (ijara shartnomasi check) | GNK API endpoint + OAuth2 client credentials |
| 4 | **UzASBO** (`/services/uzasbo/scholarship`) | `Collections.emptyList()` | Univer chaqiradi (stipendiya) | UzASBO API credentials + endpoint URL |
| 5 | **Mehnat (Workbook)** | `result=4` ("data not found") qaytaradi | Univer chaqiradi (mehnat daftari) | Mehnat va Aholini Ijtimoiy Himoya Vazirligi API |
| 6 | **MyGov** | Mavjud emas (skeleton ham yo'q) | — | OAuth2 client + scope ro'yxati |
| 7 | **OneID SSO** | Mavjud emas | — | OAuth2 IdP ulanish ma'lumoti |

---

## 1. Billing — invoice + scholarship

### Hozirgi kod
- `api-legacy/.../BillingServiceController.java:167,216` — `invoices: []`,
  `students: []` qaytaradi. Univer JSON shape mos, lekin biznes ma'lumot yo'q.
- Old-hemis CUBA original implementatsiyada ham bo'sh qaytar edi
  (audit'da tasdiqlandi) — demak Univer kontrakt 175/175 MATCH saqlanadi.

### Vazirlik kerakli ma'lumoti
- **Variant A**: Billing markaziy DB jadvali (`hemishe_e_invoice`,
  `hemishe_e_scholarship`) → JdbcTemplate query bilan ulash
- **Variant B**: Tashqi billing service API + OAuth2 token. Endpoint
  spec va credential .env'ga qo'shiladi
- **Variant C**: 224 OTM Univer DB orqali aggregate (REST proxy)

### Effort actual ulanish bilan
- Variant A: 1-2 kun (schema'ni o'rganib, query yozish)
- Variant B: 2-3 kun (HTTP client + OAuth + caching)
- Variant C: 3-5 kun (fan-out + parallel fetch + cache)

---

## 2. Email (MailServiceController)

### Hozirgi kod
- `api-legacy/.../MailServiceController.java:271-279` — email branch
  `success: false` qaytaradi. Faqat SMS (`bimmService.smsUserPay`) ishlaydi.
- Old-hemis comment'da "SMTP not configured" deyilgan — bizda ham shu.

### Talab
1. Vazirlik SMTP server (yoki SaaS — Mailgun/SendGrid/SES) credentials:
   ```
   SMTP_HOST=smtp.hemis.uz
   SMTP_PORT=587
   SMTP_USERNAME=noreply@hemis.uz
   SMTP_PASSWORD=<secret>
   SMTP_FROM_ADDRESS=noreply@hemis.uz
   SMTP_FROM_NAME=HEMIS Vazirlik
   SMTP_STARTTLS=true
   ```
2. `spring-boot-starter-mail` allaqachon `service/build.gradle.kts`
   dependency ichida (line 35)
3. `MailServiceController.java:265-280` email branch'ini `JavaMailSender`
   bilan implement qilish (10-15 qator kod)

### Effort
- 30 daqiqa kod + 1 soat e2e test (lokal Mailhog yoki real test'da)
- Rate limit kerak — bulk send'ni cheklash

---

## 3. Tax / GNK Soliq

### Hozirgi kod
- `service/.../ExternalIntegrationService.getTaxRent()` — `Collections.emptyList()`
- `api-legacy/.../ExternalIntegrationController` `/services/tax/rent` endpoint
- Univer chaqiradi: talaba ijara shartnomasi ma'lumotini bilish uchun

### Talab
1. GNK Soliq API endpoint URL (test va prod)
2. OAuth2 client credentials:
   ```
   GNK_OAUTH_CLIENT_ID=<from-vazirlik>
   GNK_OAUTH_CLIENT_SECRET=<from-vazirlik>
   GNK_OAUTH_TOKEN_URL=https://...
   GNK_API_BASE_URL=https://...
   ```
3. API spec (request/response shape)

### Mavjud infra
- `LegalEntityOldServiceController.java:43,46` — GNK OAuth2 client placeholder
- `service/.../integration/` — pattern bor (BIMM, GUVD, MSPD)

### Effort
- 1-2 kun (mavjud pattern qayta ishlatiladi)

---

## 4. UzASBO scholarship

### Hozirgi kod
- `service/.../ExternalIntegrationService.getUzasboScholarship()` — bo'sh
- Univer chaqiradi: talaba stipendiya statusini bilish uchun

### Talab
1. UzASBO API endpoint URL + token
2. Response shape (talaba qaysi turdagi stipendiya oladi)
3. Vazirlik bilan integration agreement

### Effort
- 1-2 kun (BIMM/GUVD pattern qayta ishlatiladi)

---

## 5. Mehnat (Workbook)

### Hozirgi kod
- `service/.../EmploymentIntegrationService.getWorkbook()` — `result=4` qaytaradi
  ("data not found" — old-hemis convention)
- Comment: "DEFERRED: API endpoint va credential kerak"

### Talab
1. Mehnat va Aholini Ijtimoiy Himoya Vazirligi API endpoint
2. Credential (ehtimol B2B OAuth2)
3. Workbook (mehnat daftari) JSON schema

### Effort
- 2-3 kun (API spec'ni o'rganish + integration)

---

## 6. MyGov

### Hozirgi kod
- Skeleton ham yo'q (audit tasdiq)
- Faqat `OAuthClient` enum'da nomi qayd qilingan
- Foydalanuvchi 2026-05 da: hozir kerak emas, kelajakda kerak bo'lganda

### Talab (kelajakda)
1. MyGov OAuth2 IdP discovery URL
2. Client ID + Secret + redirect URI
3. Scope ro'yxati (sub, name, pinfl, …)
4. Yangi modul yoki `api-external/auth/` kengaytirish

### Effort
- ADR-0014 (kelajakda) + 5-7 kun sprint
- **Hozir boshlanmaydi** (foydalanuvchi qaroriga)

---

## 7. OneID SSO

### Hozirgi kod
- Skeleton ham yo'q
- O'zbekiston respublikasi yagona identifikatsiya tizimi (e-imzo, paspport)

### Talab (kelajakda)
1. OneID OAuth2 IdP credentials (vazirlik egasi)
2. Federation flow (SAML yoki OIDC)
3. `users.pinfl` bilan matching

### Effort
- ADR-0015 + 7-10 kun sprint
- **Hozir boshlanmaydi**

---

## Tartiblash tavsiyam (vazirlik credential keldi paytida)

| Tartib | Item | Sabab |
|--------|------|-------|
| 1 | Email (SMTP) | Eng kichik, infra tayyor (`spring-boot-starter-mail`) |
| 2 | Tax / UzASBO | Pattern mavjud (BIMM/GUVD), credential kelishi bilanoq |
| 3 | Billing | Schema/API qaroriga bog'liq (3 variant) |
| 4 | Mehnat | API discovery kerak |
| 5 | MyGov | Yangi ADR + sprint |
| 6 | OneID | Yangi ADR + sprint (federation murakkab) |

---

## Ma'lumotnoma fayllar

- `service/src/main/java/uz/hemis/service/integration/ExternalIntegrationService.java`
- `service/src/main/java/uz/hemis/service/integration/EmploymentIntegrationService.java`
- `api-legacy/.../BillingServiceController.java` (line 167, 216)
- `api-legacy/.../MailServiceController.java` (line 271-279)
- `api-legacy/.../LegalEntityOldServiceController.java` (GNK placeholder)
- ADR-0005 — OAuth client credentials migration
- `.env` template — credential ENV nomlari
- `k8s-secret.env` — production credential storage (Sealed-Secrets sprint qoldi)
