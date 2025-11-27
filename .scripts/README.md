# HEMIS Backend Test Scripts

Bu papkada HEMIS backend API larini test qilish uchun scriptlar joylashgan.

## 🆕 Yangi scriptlar

### 1. `test_create_new_student.sh` - Yangi talaba yaratish
**Maqsad**: POST /services/student/id endpoint orqali yangi talaba yaratish va ID generatsiyasini test qilish

**Ishlatish**:
```bash
bash ./.scripts/test_create_new_student.sh
```

**Nima qiladi**:
- Random PINFL (14 raqam) va Serial (AA + 7 raqam) generatsiya qiladi
- NEW-HEMIS va OLD-HEMIS da bir xil ma'lumotlar bilan yangi talaba yaratadi
- Yaratilgan talaba ID larini ko'rsatadi
- ID generatsiya mexanizmini tekshiradi
- Code (unique_id) ni ko'rsatadi

**Natija misoli**:
```
✅ OLD-HEMIS: Talaba muvaffaqiyatli yaratildi!
   📌 Student ID: 1d41e44d-985d-f4c4-8e8b-ab32c943074b
   📝 PINFL: 20925658178205
   📄 Serial: AA9042671
   📁 Code: 351241102829
```

**Test qilingan endpointlar**:
- POST `/app/rest/v2/services/student/id` (endpoint #14)

---

### 2. `get_active_student_ids.sh` - Active talabalarni topish
**Maqsad**: Har ikkala tizimdan `active=true` bo'lgan talabalarni topib, ularning ID larini olish

**Ishlatish**:
```bash
bash ./.scripts/get_active_student_ids.sh
```

**Nima qiladi**:
- NEW-HEMIS dan birinchi active talabani topadi
- OLD-HEMIS dan birinchi active talabani topadi
- endpoint_tester.html ga qo'yish uchun formatda ko'rsatadi

**Natija misoli**:
```
✅ NEW-HEMIS active talaba: 057de9db-b1db-c2b0-522d-7edca7d02f06
✅ OLD-HEMIS active talaba: 057de9db-b1db-c2b0-522d-7edca7d02f06

NEW: <input id="newStudentId" value="057de9db-b1db-c2b0-522d-7edca7d02f06">
OLD: <input id="oldStudentId" value="057de9db-b1db-c2b0-522d-7edca7d02f06">
```

---

### 3. `test_student_delete_recreate.sh` - DELETE va qayta yaratish
**Maqsad**: DELETE endpoint (#13) ni test qilib, keyin POST (#14) bilan qayta yaratish

**Ishlatish**:
```bash
bash ./.scripts/test_student_delete_recreate.sh
```

**Nima qiladi**:
1. Belgilangan student ID larni o'chiradi (soft delete)
2. Test ma'lumotlar bilan yangi talaba yaratadi
3. Eski va yangi ID larni solishtiradi

---

## 📝 Mavjud scriptlar

### 4. `test_passport_endpoint.sh` - Passport endpoint workflow test
**Maqsad**: Captcha + Passport ma'lumotlarini olish endpointlarini test qilish

**Ishlatish**:
```bash
bash ./.scripts/test_passport_endpoint.sh [PINFL] [Serial]
```

**Default qiymatlar**:
- PINFL: `61902025630068`
- Serial: `AC2455764`

**Workflow**:
1. Token olish
2. Captcha generatsiya qilish
3. Passport ma'lumotlarini tekshirish

---

### 5. `test_endpoint_comparison.sh` - Endpoint solishtirish
**Maqsad**: Bir xil endpointni NEW-HEMIS va OLD-HEMIS da test qilib, javoblarni solishtirish

**Ishlatish**:
```bash
bash ./.scripts/test_endpoint_comparison.sh "/app/rest/v2/userInfo"
```

**Natija**:
- Javoblar bir xil bo'lsa: ✅ "Responses are 100% IDENTICAL!"
- Farq bo'lsa: ⚠️ farqlarni ko'rsatadi

---

### 6. `test_student_id.sh` - Student ID olish (oddiy)
**Maqsad**: POST /services/student/id ni test qilish

**Ishlatish**:
```bash
bash ./.scripts/test_student_id.sh
```

---

### 7. `test_student_put.sh` - Student ma'lumotlarini yangilash
**Maqsad**: PUT endpoint ni test qilish (partial update)

**Ishlatish**:
```bash
bash ./.scripts/test_student_put.sh
```

---

### 8. `compare_students.sh` - Talabalarni solishtirish
**Maqsad**: NEW-HEMIS va OLD-HEMIS dagi talabalar ma'lumotlarini solishtirish

---

## 🔧 Konfiguratsiya

Barcha scriptlar quyidagi default qiymatlardan foydalanadi:

**NEW-HEMIS (port 8081)**:
- Username: `otm401`
- Password: `XCZDAb7qvGTXxz`
- University: 401

**OLD-HEMIS (port 8082)**:
- Username: `otm351`
- Password: `XCZDAb7qvGTXxz`
- University: 351

**OAuth Client**:
- Client ID: `client`
- Client Secret: `secret`

---

## 💡 Maslahatlar

1. **Server ishlab turganini tekshiring**:
   ```bash
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   ```

2. **Yangi talaba yaratish uchun**: `test_create_new_student.sh` dan foydalaning - bu har safar yangi random ma'lumotlar generatsiya qiladi

3. **Active talabalarni topish uchun**: `get_active_student_ids.sh` - bu DELETE test qilish uchun kerak

4. **endpoint_tester.html** avtomatik ravishda active talabalarni yuklaydi (JavaScript)

---

## 📊 Test natijalarini saqlash

Barcha scriptlar `/tmp/` papkasiga natijalarni saqlaydi:
- `/tmp/new_created_student.json` - yangi yaratilgan talaba (NEW-HEMIS)
- `/tmp/old_created_student.json` - yangi yaratilgan talaba (OLD-HEMIS)
- `/tmp/new_response.json` - oxirgi NEW-HEMIS javobi
- `/tmp/old_response.json` - oxirgi OLD-HEMIS javobi

---

## 🐛 Muammolarni hal qilish

**CORS xatosi (old-hemis)**:
- Browser dan emas, terminaldan test qiling
- yoki proxy-server.py dan foydalaning

**Token olishda xato**:
- Username/password to'g'riligini tekshiring
- Server ishlab turganini tekshiring

**Student yaratilmadi (NEW-HEMIS)**:
- "User university not configured" - userga universitet biriktirilmagan
- Admin orqali university assign qiling
