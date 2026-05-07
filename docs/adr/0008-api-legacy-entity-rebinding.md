---
id: ADR-0008
status: in-progress
date: 2026-05-07
deciders: hemis-team
agent: claude-code
model: claude-opus-4-7
affects: [api-legacy, domain]
liquibase:
  - V006_create_users.sql       # users jadval (yangi schema)
  - M001_migrate_old_hemis_users.sql  # sec_user → users
entities: [User, SecUser, Employee, Teacher, EmployeeJobs, LegacyEmployeeJobs]
verification: |
  grep -rn "uz.hemis.domain.entity.security.User\|.entity.employee.Employee\|.entity.employee.EmployeeJobs" api-legacy/ | wc -l  # 0 bo'lishi kerak (Stage 5 da)
  node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js  # 175/175
related: [ADR-0004, ADR-0005, ADR-0007]
---

# ADR 0008: api-legacy uchta entity'ni eski jadvallarga qaytarish (Legacy* prefiks)

## Status

Accepted (Stage 1 audit: 2026-05-07); Stages 2-5 — Pending

**Implementation:**
- ✅ Stage 1 — Audit + reja (3 buzilgan import aniqlangan, `LegacyEmployeeJobs` yaratilgan)
- ⏳ Stage 2 — `User` → `SecUser` rebinding (4 fayl: `LegacySecurityHelper`, `UserController`, `LegacyUserInfoController`, `EmployeeJobsEntityController`)
- ⏳ Stage 3 — `EmployeeJobs` → `LegacyEmployeeJobs`
- ⏳ Stage 4 — `Employee` → `Teacher` (yoki `LegacyEmployee`)
- ⏳ Stage 5 — Verification + lock (cuba-format-checker yangi rule)

**Quality gate:** har Stage'dan keyin `compare_endpoints.js` 175/175 saqlanadi.

## Context

GOLDEN RULE (CLAUDE.md, api-legacy/CLAUDE.md):
> *api-legacy controller'lari faqat eski jadvallarga (`hemishe_*`, `sec_*`) yozadi.
> Yangi schema (`employee_job`, `users`, `employee`) — `api-university`/`api-web` modullari uchun.*

Bu qoida buzilishi 224 ta Univer (Yii2 PHP backend, per-OTM) uchun **split-brain** bug'ga olib keladi:
Univer eski URL bilan POST qiladi → api-legacy yangi jadvalga yozadi → keyingi GET eski jadvaldan
o'qiydi → ma'lumot topilmaydi → "Yangi xodim qo'shildi, lekin ko'rinmaydi".

Audit (`grep` `api-legacy/src/main/java/`) shuni ko'rsatdi: hozirda **3 ta entity** GOLDEN RULE'ni
buzayapti:

| Buzilgan import | Map qiladigan jadval | api-legacy ichidagi controller'lar |
|-----------------|-----------------------|-----------------------------------|
| `uz.hemis.domain.entity.security.User` | `users` (yangi) | `LegacyUserInfoController`, `UserController`, `EmployeeJobsEntityController`, `LegacySecurityHelper` |
| `uz.hemis.domain.entity.employee.Employee` | `employee` (yangi) | `EmployeeJobsEntityController` |
| `uz.hemis.domain.entity.employee.EmployeeJobs` | `employee_job` (yangi) | `EmployeeJobsEntityController` |

`LegacyEmployeeJobs` (`hemishe_e_employee_jobs`) entity 2026-05-06'da allaqachon yaratilgan — lekin
controller hali yangi `EmployeeJobs`'dan foydalanmoqda. `Teacher` (`hemishe_e_teacher`) —
mavjud, `Employee` o'rniga ishlatilishi kerak. `SecUser` (`sec_user`) — mavjud, `User` o'rniga.

### Tarixiy kontekst

Loyiha boshida (2025), api-legacy modul yangi schema'ga yo'naltirilgan edi (data migration nazariya).
Lekin Univer 224 OTM hali ishlab turadi va eski format kutadi. Qaytishni "kelajakdagi sprint" ga qoldirib,
hozirda 175/175 integration testdan o'tib turibmiz, chunki controller'lar eski URL+request shape'ni
saqlaydi. Lekin **birinchi ma'lumot mismatch** (yangi yangilangan `employee_job` row, lekin Univer
`hemishe_e_employee_jobs`'dan o'qiyapti) — biznes incident.

### Texnik cheklovlar

- Univer Yii2 (224 OTM) deploy'ga to'liq tegmaslik (Yii2 changes 6+ oy).
- `users` va `sec_user` parallel ishlaydi (`HybridUserDetailsService`, ADR-0005). Migrate yo'l xaritasi
  bilan birga ishlash kerak.
- `LegacyEmployeeJobs` allaqachon yaratilgan (2026-05-06) — kuch sarflanmaydi.

## Decision

3 ta `api-legacy` entity import'ini Legacy variant'ga ko'chiramiz:

1. **`User` → `SecUser`** (`sec_user`): Barcha api-legacy controller/util import'lari `SecUser`'ni ishlatadi.
   `LegacySecurityHelper` shu bilan birga `sec_user` jadvalga yo'naltiriladi.

2. **`Employee` → `Teacher`** (`hemishe_e_teacher`): `EmployeeJobsEntityController` ichida
   `Teacher` (yoki yangi yaratilgan `LegacyEmployee` bo'lsa, undan foydalanish) ishlatiladi.

3. **`EmployeeJobs` → `LegacyEmployeeJobs`** (`hemishe_e_employee_jobs`): Controller import'i
   yangi `LegacyEmployeeJobs` entity'ga qaytariladi.

### Test asosi

Har bosqichdan keyin `compare_endpoints.js` 175/175 saqlanadi — agar testdan biron mismatch
chiqsa, sprint to'xtaydi va sabab tekshiriladi.

## Alternatives Considered

### Alternative 1: Hech narsa qilmaslik (status quo)

- **Afzalligi:** Hech qanday refactor riski yo'q.
- **Kamchiligi:** Birinchi sync hold (yangi web frontend → `employee_job` yangilaydi, Univer
  `hemishe_e_employee_jobs` o'qiydi → mismatch) — ma'lumot yo'qoladi. Bu hozir avtomatik test'lar
  topa olmaydigan integratsiya bug.
- **Rad etish sababi:** Tarmoq audit'da topilgan **kritik** xavf, kelajakda incident'ga olib keladi.

### Alternative 2: api-legacy controller'larida ikkala jadvalga yozish (dual-write)

- **Afzalligi:** Yangi va eski schema bir vaqtda yangilanadi.
- **Kamchiligi:** Tranzaktsion atomik emas (`PostgreSQL` ikki jadval, lekin alohida row); rollback
  semantics murakkab; data drift xavfi.
- **Rad etish sababi:** Murakkablik o'sadi, GOLDEN RULE'ga zid (bir modul = bir jadval guruhi).

### Alternative 3: Eski-yangi sync qatlami (alohida service)

- **Afzalligi:** api-legacy toza qoladi, sync alohida Kafka topic (ADR-0007) orqali.
- **Kamchiligi:** Hozirgi 3 ta xato uchun overkill — sync qatlami 6+ oy ish, bizga zudlik bilan
  chora kerak.
- **Rad etish sababi:** Kelajakda 224 OTM Univer integratsiyasi (`api-university`) uchun dolzarb, lekin hozirgi 3
  ta entity uchun kichik direct-fix arzonroq.

## Consequences

### Positive

- GOLDEN RULE qayta tiklanadi: `api-legacy` faqat eski jadvallarga.
- Univer 224 OTM uchun split-brain bug yopiladi — ma'lumot yo'qotish riski yo'q.
- Yangi `Legacy*` entity konvensiyasi 60+ qolgan eski entity uchun shablon bo'ladi.

### Negative

- 4 ta controller/util fayl o'zgaradi (`LegacyUserInfoController`, `UserController`,
  `EmployeeJobsEntityController`, `LegacySecurityHelper`). Har biri kichik diff, lekin diqqat talab.
- `Teacher` entity'ning `Employee`'dan farqli atributlari bor (faculty FK shape farqli) — mapper
  layer kerak bo'lishi mumkin.
- `compare_endpoints.js` har bosqichda majburiy run.

### Risks

- **Risk:** `users` va `sec_user` parallel — `User` import o'chirilganda boshqa modul (api-web,
  api-university) ishlamay qoladi.
  **Mitigation:** `User` import'i faqat **api-legacy** ichida o'zgaradi. Boshqa modullar tegmaydi.
  `grep -rn "uz.hemis.domain.entity.security.User" api-web/ api-university/` farzandsizligini tekshirib boring.

- **Risk:** `Employee` o'rniga `Teacher` ishlatilganda — `EmployeeJobs.teacher` FK turi mos kelmaydi.
  **Mitigation:** `LegacyEmployeeJobs` entity allaqachon `Teacher` bilan to'g'ri map qilingan
  (2026-05-06 changeset). Tekshirish: `LegacyEmployeeJobs.java` field signature.

- **Risk:** Test fixture'lar (`legacy-fixtures/*.json`) eski shape kutmoqda — rebind o'zgartiradi.
  **Mitigation:** Fixture'larni `compare_endpoints.js` jonli javob bilan qayta tasdiqlash.

## Implementation

Sprint reja (1 sprint = 1 hafta):

### Bosqich 1: Audit va precheck (1 kun)
1. Hozirgi `grep -rn "uz.hemis.domain.entity.security.User\|...Employee\|...EmployeeJobs" api-legacy/`
   chiqishini hisobotga yozish.
2. `LegacyEmployeeJobs.java` mavjudligini tasdiqlash (2026-05-06 changeset).
3. `Teacher.java` ni `Employee.java` bilan solishtirish (field/FK farqlari).
4. Yangi `LegacyEmployee.java` kerakmi yoki `Teacher` yetadimi — qaror.

### Bosqich 2: User → SecUser rebinding (2 kun)
1. `LegacySecurityHelper.java` import + usage o'zgartirish.
2. `UserController.java`, `LegacyUserInfoController.java`, `EmployeeJobsEntityController.java` ichida `User` → `SecUser`.
3. `compare_endpoints.js` run — auth/user-info endpoint'lari 100% match bo'lishi shart.
4. Pull request — `fix(api-legacy): User entity → SecUser`.

### Bosqich 3: EmployeeJobs → LegacyEmployeeJobs (2 kun)
1. `EmployeeJobsEntityController.java` ichida `EmployeeJobs` → `LegacyEmployeeJobs`.
2. Bog'liq mapper, repository, dto — to'g'rilash.
3. `compare_endpoints.js` run — `hemishe_EEmployeeJobs` endpoint'i 100% match bo'lishi shart.
4. Pull request — `fix(api-legacy): EmployeeJobs → LegacyEmployeeJobs`.

### Bosqich 4: Employee → Teacher (yoki LegacyEmployee) (1 kun)
1. Qaror bosqich-1 dan: `Teacher` yetadi yoki yangi `LegacyEmployee` kerak.
2. `EmployeeJobsEntityController.java` `Employee` import → `Teacher`/`LegacyEmployee`.
3. `compare_endpoints.js` run.
4. Pull request — `fix(api-legacy): Employee → Teacher`.

### Bosqich 5: Verification + lock (1 kun)
1. `grep -rn "uz.hemis.domain.entity.security.User\|.entity.employee.Employee\|.entity.employee.EmployeeJobs" api-legacy/`
   → bo'sh bo'lishi shart.
2. `scripts/check_table_mappings.sh` natija: green.
3. `compare_endpoints.js` final: 175/175.
4. `cuba-format-checker` subagent uchun yangi check qo'shish: `api-legacy` ichida yangi schema
   entity import topilsa, lint xatolik beradi.

### Quality gates

- Har bosqichdan keyin `compare_endpoints.js` 175/175 majburiy.
- `./gradlew :api-legacy:test` 100% pass.
- `./gradlew :app:bootRun` muvaffaqiyat — lokal smoke test (login + hemishe_EEmployeeJobs CRUD).

## Verification

Stage'lar bajarilganligini tekshirish:

```bash
# 1. api-legacy ichida yangi schema entity import — 0 bo'lishi shart
grep -rn "uz.hemis.domain.entity.security.User\|.entity.employee.Employee\|.entity.employee.EmployeeJobs" \
    api-legacy/src/main/java/ | grep -v "Legacy" | wc -l

# 2. Legacy* entity'lar ishlatilayotganini tasdiqlash
grep -rn "import uz.hemis.domain.entity..*\.\(SecUser\|Teacher\|LegacyEmployeeJobs\)" \
    api-legacy/src/main/java/

# 3. JPA @Table mapping moslik
./scripts/check_table_mappings.sh

# 4. Univer kontrakt 175/175
node /home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js

# 5. Lokal smoke test
./gradlew :api-legacy:test
```

**Acceptance criteria:**
- [ ] Stage 1 — Audit complete (✅ 2026-05-07)
- [ ] Stage 2 — `User` → `SecUser` rebinding (4 fayl)
- [ ] Stage 3 — `EmployeeJobs` → `LegacyEmployeeJobs`
- [ ] Stage 4 — `Employee` → `Teacher`
- [ ] Stage 5 — Verification + cuba-format-checker rule

## References

- Code: `api-legacy/src/main/java/uz/hemis/api/legacy/controller/employee/EmployeeJobsEntityController.java`
- Code: `api-legacy/src/main/java/uz/hemis/api/legacy/controller/auth/UserController.java`
- Code: `api-legacy/src/main/java/uz/hemis/api/legacy/util/LegacySecurityHelper.java`
- Documentation: `api-legacy/CLAUDE.md` ("GOLDEN RULE" bo'limi)
- Documentation: `CLAUDE.md` ("Modul ↔ Jadval mosligi qoidasi")
- Test: `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/compare_endpoints.js`
- Related ADRs: ADR-0004 (api-university yangi modul), ADR-0005 (sec_user parallel ishlaydi),
  ADR-0007 (Kafka-first sync — kelajakdagi yo'l)
