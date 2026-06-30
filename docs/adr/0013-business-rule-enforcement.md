---
id: ADR-0013
status: partially-implemented
date: 2026-05-21
revised: 2026-06-30
deciders: hemis-team
agent: claude-code
model: claude-opus-4-8
affects:
  - common
  - app
  - service
entities: []
verification: |
  grep -rn "BusinessRuleException" common/src/main/java service/src/main/java
  grep -n "handleBusinessRule" app/src/main/java/uz/hemis/app/exception/GlobalExceptionHandler.java
related:
  - ADR-0008
---

# ADR 0013: Business Rule Enforcement Foundation (Rules Engine)

## Status

Partially Implemented (foundation ✅ 2026-05-21; konkret policy klasslar ⏳ pending)

(Sana: 2026-05-21; ADR sifatida rasmiylashtirildi: 2026-06-30)

> **Y-Statement:** Loyihaning 3-asosiy maqsadi (vazirlik qoidalarini 224 OTM bo'ylab markaziy joriy qilish — talaba kiritish cheklash, baho o'zgartirish cheklash, vaqt-oynasi cheklov) uchun, biz **`BusinessRuleException` (HTTP 422) + `ruleCode` shartnomasi + `@RestControllerAdvice` mapping** asosidagi yagona biznes-qoida buzilishi primitivi'ni tanladik, chunki bu validatsiya (400 format) va konflikt (409 mavjud yozuv) dan aniq ajralgan, machine-readable kod beradi va kelajakdagi policy klasslar uchun barqaror foundation; oqibatda Univer (224 OTM) klientlari qoida buzilishini 422 + `ruleCode` orqali bir xil format'da oladi.

## Context

HEMIS-back markaziy server vazifasi nafaqat ma'lumot aggregatsiyasi, balki **qoidalarni joriy qilish** (loyiha maqsadi #3). Misollar:
- CLOSED (yopilgan) OTM'ga yangi talaba kiritib bo'lmaydi.
- Semestr/imtihon yopilgandan keyin baho o'zgartirib bo'lmaydi.
- Ro'yxatdan o'tish oynasi (enrollment window) muddati o'tgan.
- OTM bloklangan / litsenziyasi to'xtatilgan.

Bu holatlar **input sintaktik to'g'ri** (PINFL 14 raqam, email format to'g'ri) — ya'ni `400 ValidationException` emas; va **mavjud yozuv bilan to'qnashuv ham emas** — ya'ni `409 ConflictException` emas. Ular **domen qoidasi bo'yicha amalga oshirib bo'lmaydigan** holatlar → semantik jihatdan **HTTP 422 Unprocessable Entity**.

Bu xato turini umumiy `RuntimeException` yoki `IllegalStateException` bilan ifodalansa: (1) klient (Univer) uni 500 dan ajrata olmaydi; (2) machine-readable sabab (`ruleCode`) yo'qoladi; (3) policy mantiq tarqoq bo'ladi.

## Decision

`common` modulda yagona **`BusinessRuleException`** (HTTP 422) primitivi:
- `ruleCode` (String, machine-readable: `OTM_CLOSED`, `GRADE_FINALIZED`, `ENROLLMENT_WINDOW_EXPIRED`, ...) + human `message`.
- `app` GlobalExceptionHandler `@ExceptionHandler(BusinessRuleException.class)` → `422` + `ErrorResponse{status:422, error:ruleCode, message, path}`; `log.warn` (5xx emas — bu kutilgan biznes holati).
- Kelajakdagi konkret policy klasslar (`StudentInsertionPolicy`, `GradeEditPolicy`, `EnrollmentWindowGuard`) shu exception'ni ko'taradi — service qatlamida, domen entity holatiga qarab (`university.lifecycleStatus`, semestr/imtihon holati, vaqt oynasi).

Bu — **rules engine'ning foundation primitivi**, full DSL/Drools emas (over-engineering): oddiy guard klasslar + bitta exception turi yetadi (markazda ~0.3 event/sec, deterministik qoidalar).

## Alternatives Considered

### Alternative 1: Drools / tashqi rules-engine kutubxonasi
- Deklarativ DSL, hot-reload.
- **Rad etish sababi:** og'ir bog'liqlik, learning curve, deterministik bir nechta guard uchun keraksiz murakkablik. Spring Boot 4 bilan integratsiya qo'shimcha yuk. Re-evaluate faqat qoidalar soni 50+ dinamik bo'lsa.

### Alternative 2: Har policy uchun alohida exception turi (`OtmClosedException`, `GradeFinalizedException`, ...)
- Type-safe.
- **Rad etish sababi:** exception turlari portlashi (har qoidaga klass), GlobalExceptionHandler'da har biriga handler. `ruleCode` field bitta turda bir xil natijani arzonroq beradi.

### Alternative 3: `400 ValidationException`'ni qayta ishlatish
- **Rad etish sababi:** semantik noto'g'ri — 400 = format/syntax xatosi. Biznes qoidasi buzilishi 422. Klient ikkalasini ajratishi shart (retry vs foydalanuvchiga xabar).

## Consequences

### Positive
- Univer (224 OTM) klientlari qoida buzilishini bir xil 422 + `ruleCode` format'da oladi.
- Policy mantiq markazlashgan, testlanadigan (har guard alohida unit test).
- Validatsiya / konflikt / biznes-qoida aniq ajralgan (400/409/422).

### Negative
- `ruleCode` lug'ati qo'lda boshqariladi (kanonik kodlar ro'yxati kerak).
- Policy klasslar hali yo'q — foundation primitiv bo'sh ishlamaydi (faqat tayyor).

### Risks
- **Risk:** har policy `ruleCode`'ni ad-hoc string sifatida yozsa, klient tomonda nomuvofiqlik.
  **Mitigation:** kanonik `ruleCode` enum/constant ro'yxati (policy klasslar bilan birga qo'shiladi).

## Implementation

- [x] Stage 1 — `BusinessRuleException` (422, `ruleCode`) `common/exception/` (commit `4fbe612`, 2026-05-21). ✅
- [x] Stage 2 — `GlobalExceptionHandler.handleBusinessRule` → 422 + `ErrorResponse` mapping. ✅
- [x] Stage 3 — Unit test (`ExceptionClassesTest.BusinessRuleException`). ✅
- [ ] Stage 4 — Konkret policy klasslar: `StudentInsertionPolicy` (OTM lifecycle), `GradeEditPolicy` (semestr/imtihon yopilishi), `EnrollmentWindowGuard` (vaqt oynasi). ⏳ pending (vazirlik qoida-konfiguratsiyasi keladi).
- [ ] Stage 5 — Kanonik `ruleCode` lug'ati (constant/enum) + i18n message kalitlari. ⏳ pending.

> **Eslatma:** Foundation primitiv (Stage 1-3) deploy qilingan; enforcement nuqtalari (Stage 4-5) vazirlik qoida-konfiguratsiyasi aniqlangach yoziladi.

## Verification

```bash
# Foundation mavjud
grep -rn "class BusinessRuleException" common/src/main/java
grep -n "handleBusinessRule\|BusinessRuleException.class" app/src/main/java/uz/hemis/app/exception/GlobalExceptionHandler.java

# Policy klasslar (Stage 4) — hozircha bo'sh kutilgan
grep -rn "StudentInsertionPolicy\|GradeEditPolicy\|EnrollmentWindowGuard" service/src/main/java || echo "Stage 4 pending"
```

**Acceptance criteria:**
- [x] `BusinessRuleException` (422, `ruleCode`) common'da
- [x] GlobalExceptionHandler 422 mapping + `log.warn` (5xx emas)
- [ ] Kamida 1 ta policy klass enforcement nuqtasi (Stage 4)
- [ ] Kanonik `ruleCode` ro'yxati (Stage 5)

## References

- Code: `common/src/main/java/uz/hemis/common/exception/BusinessRuleException.java`
- Code: `app/src/main/java/uz/hemis/app/exception/GlobalExceptionHandler.java` (`handleBusinessRule`)
- Test: `common/.../ExceptionClassesTest` (BusinessRuleException)
- Related: ADR-0008 (module ↔ entity ownership) — policy klasslar domen entity holatiga tayanadi
- Loyiha maqsadi #3: `CLAUDE.md` "Qoidalarni JORIY qilish"
