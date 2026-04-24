# Classifier Refactor — Bosqich 0: Inventarizatsiya

**Sana:** 2026-04-21
**Auditor:** Senior team deep-dive
**Maqsad:** V009-V013 DDL va ReferenceEntity entity'larini solishtirib, aniq gap ro'yxati

---

## 1. Umumiy statistika

| Metrika | Qiymat | Izoh |
|---|---|---|
| V009-V013 classifier jadvallari | **100** | Har biri `hemishe_h_*` dan INSERT ... SELECT bilan to'ldirilgan |
| V009-V013 operational jadvallari | **10** | organization, university_*, position*, employee* |
| Jami yangi jadvallar | **110** | |
| ReferenceEntity entity'lari (hozirda) | **63** | — |
| Classifier entity'lari (ReferenceEntity) mos keladi | **64** | 1 ta `HemisVersion` yangi jadvalga mos, 63 + system/hemis_version |
| **YO'Q entity (yetishmaydi)** | **36** | — |

---

## 2. Data parity — TO'LIQ (migration ichida copy qilingan)

Har bir V009-V013 classifier uchun `INSERT INTO <yangi> SELECT ... FROM hemishe_h_<eski> WHERE delete_ts IS NULL` qo'yilgan. Ya'ni:

- ✅ 100/100 classifier uchun data parity ta'minlangan (migration paytida)
- ⚠️ Agar live DB'da eski jadvalga yangi yozuv qo'shilgan bo'lsa, u yangi jadvalda yo'q (hozir biz testiviy DB'da — muammo emas)

---

## 3. Yetishmaydigan 36 entity (aniq ro'yxat)

### V011 dan — 13 ta

| # | Table | Extra column | Target folder |
|---|---|---|---|
| 1 | `certificate_language` | certificate_type_code (FK) | classifier/ |
| 2 | `class_type` | — | academic/ |
| 3 | `education_week_type` | — | academic/ |
| 4 | `exam_finish` | — | academic/ |
| 5 | `exam_type` | — | academic/ |
| 6 | `final_exam_type` | — | academic/ |
| 7 | `grade_system_type` | — | academic/ |
| 8 | `score_type` | grade_system_code (FK) | academic/ |
| 9 | `student_achievement_type` | parent_code (self-ref) | student/ |
| 10 | `study_schedule_type` | — | academic/ |
| 11 | `subject_block` | — | academic/ |
| 12 | `subject_choose_type` | — | academic/ |
| 13 | `subject_type` | — | academic/ |
| 14 | `absence_reason` | — | academic/ |

*14 ta — qaytadan sanadim, 14 edi*

### V012 dan — 11 ta

| # | Table | Extra column | Target folder |
|---|---|---|---|
| 1 | `contract_summa_type` | — | finance/ |
| 2 | `contract_type` | — | finance/ |
| 3 | `contract_class` | — | finance/ |
| 4 | `decree_type` | — | classifier/ |
| 5 | `employee_age_range` | — | employee/ |
| 6 | `external_service_type` | — | classifier/ |
| 7 | `graduate_fields_type` | — | classifier/ |
| 8 | `graduate_inactive_type` | — | classifier/ |
| 9 | `locality_type` | — | classifier/ |
| 10 | `outside_activity` | — | classifier/ |
| 11 | `qualification` | — | classifier/ |
| 12 | `scholarship_decree_type` | — | finance/ |

*12 ta — qaytadan sanadim*

### V013 dan — 12 ta

| # | Table | Extra column | Target folder |
|---|---|---|---|
| 1 | `attandance_setting` | — (typo saqlangan) | classifier/ |
| 2 | `auditorium_type` | — | classifier/ |
| 3 | `device_type` | — | classifier/ |
| 4 | `diplom_blank_status` | — | classifier/ |
| 5 | `internship_form` | — | classifier/ |
| 6 | `internship_type` | — | classifier/ |
| 7 | `resource_type` | — | classifier/ |
| 8 | `scientific_project_type` | — | research/ |
| 9 | `sport_type` | — | classifier/ |
| 10 | `teacher_achievement_type` | — | classifier/ |
| 11 | `teacher_conduction_form` | — | classifier/ |
| 12 | `workplace_compatibility` | — | classifier/ |
| 13 | `language_certificate` | certificate_language_code (FK) | classifier/ |

*13 ta*

**JAMI: 14 + 12 + 13 = 39 ta entity yetishmaydi** (oldin 36 deb hisoblagan edim — qayta sanash natijasi 39)

### Extra-column entity'lari (4 ta)
- `certificate_language.certificate_type_code` → FK qil Java'da
- `score_type.grade_system_code` → FK qil Java'da
- `student_achievement_type.parent_code` → self-ref qil Java'da
- `language_certificate.certificate_language_code` → FK qil Java'da

---

## 4. Mavjud entity'larda extra-column'lar to'g'ri map qilinganmi?

Bular allaqachon mavjud, lekin extra ustunlari bor — Java'da map qilinganini tekshirish kerak:

| Entity | Table | Extra column | Yaxshi map qilinganmi? |
|---|---|---|---|
| GrantType | grant_type | payment_form_code, grant_form | TEKSHIRISH |
| StipendRate | stipend_rate | category_code | TEKSHIRISH |
| Soato | soato | parent_code (self-ref) | TEKSHIRISH |
| Terrain | terrain | soato_code (FK) | TEKSHIRISH |

---

## 5. 3 anomaliya entity (Bosqich 2 uchun)

Bular ReferenceEntity uzaytirmaydi, CUBA `hemishe_h_*` jadvallarda:

| Entity | Jadval | Muammo |
|---|---|---|
| ScienceBranch | hemishe_h_science_branch | `implements Serializable` |
| UniversityActivityStatus | hemishe_h_university_activity_status | `implements Serializable` |
| UniversityContractCategory | hemishe_h_university_contract_category | `implements Serializable` |

**Yechim (2026-04-23 yangilandi):** Classifier refactor yakunlandi — entity'lar to'g'ridan-to'g'ri `hemishe_h_*` ga map qilindi (LegacyClassifierEntity base class). Yangi classifier jadvallar yaratilmadi (Single Source of Truth — rules.md v2.0).

---

## 6. Extra findings (avvalgi auditdan)

- **Speciality* (4 ta):** BaseEntity + hemishe_h_speciality_* — rules.md bo'yicha tegilmaydi
- **ExpelReason:** @Table(name="expel") — Bu to'g'ri. Jadval nomi "expel", entity nomi "ExpelReason" (API misformat)
- **V013 FK constraints:** Employee 6 FK + EmployeeJobs 2 FK allaqachon DB'da
- **V013 missing FK:** employment_form_code, employee_rate_code VARCHAR lekin FK YO'Q

---

## 7. Bosqich 1 uchun batch reja

**Batch 1 (26 ta, simple):** Extra column'siz — bitta strukturaga ega
```java
@Entity
@Table(name = "...")
@Getter @Setter
public class ... extends ReferenceEntity {}
```

**Batch 2 (4 ta, extra column):** FK/self-ref bilan
- CertificateLanguage (+certificateTypeCode)
- ScoreType (+gradeSystemCode)
- StudentAchievementType (+parentCode self-ref)
- LanguageCertificate (+certificateLanguageCode)

**Batch 3:** Repository'larni qo'shish (aksiyalar uchun)
**Batch 4:** Compile verify (`./gradlew :domain:compileJava`)

---

## 8. Keyingi qadamlar

- [x] Bosqich 0: Inventarizatsiya (shu hujjat)
- [ ] Bosqich 1: 39 ta entity yaratish (batch-batch, compile verify bilan)
- [x] Bosqich 2: Classifier refactor yakunlandi (102 dublikat olib tashlandi, entity'lar hemishe_h_* ga map)
- [ ] Bosqich 3: Read path (alias mapping)
- [ ] Bosqich 4: Hardcoded SQL fayllar
- [ ] Bosqich 5: Write path (ClassifierWebService)
- [ ] Bosqich 6: Employee FK (Java only)
- [ ] Bosqich 7: Boshqa entity FK'lar
- [ ] Bosqich 8: Legacy qaror doc
- [ ] Bosqich 9: Yakuniy test
