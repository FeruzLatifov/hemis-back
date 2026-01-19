# HEMIS API Endpoint Tester

Bu katalogda HEMIS backend API endpointlarini test qilish uchun vositalar joylashgan.

## 📁 Fayllar

### 1. `endpoint_tester.html` - Browser-based Tester

**Joylashuvi:** `/home/adm1n/startup/hemis-back/docs/endpoint_tester.html`

**Ishlatish:**

1. **HTTP server orqali ochish (CORS muammosi bo'lmasligi uchun):**

```bash
cd /home/adm1n/startup/hemis-back/docs
python3 -m http.server 9000
```

2. Brauzerda ochish:
```
http://localhost:9000/endpoint_tester.html
```

**Xususiyatlari:**
- ✅ Dual config panel (Yangi Hemis vs Old Hemis)
- ✅ Side-by-side response display
- ✅ Avtomatik javoblarni solishtirish
- ✅ 3 ta test tugmasi har bir endpoint uchun:
  - 🆕 Yangi Hemis
  - 🏛️ Old Hemis
  - Ikkalasini Ham Test
- ✅ Rangli natijalar (✅/⚠️)
- ✅ Collapsible kategoriyalar (Swagger style)

**Default Test Ma'lumotlar:**
- **Username:** `feruz`
- **Password:** `BvZzXW6oQxEEte`
- **PINFL:** `61902025630068` (database dan olingan: `e_student.passport_pin`)

---

### 2. `test_endpoint_comparison.sh` - CLI-based Tester

**Joylashuvi:** `/home/adm1n/startup/hemis-back/.scripts/test_endpoint_comparison.sh`

**Ishlatish:**

```bash
# Bitta endpoint test qilish
bash /home/adm1n/startup/hemis-back/.scripts/test_endpoint_comparison.sh "/app/rest/v2/userInfo"

# Parametrli endpoint test qilish
bash /home/adm1n/startup/hemis-back/.scripts/test_endpoint_comparison.sh "/app/rest/v2/services/pass/data?pinfl=61902025630068"
```

**Xususiyatlari:**
- ✅ Avtomatik token olish (old + new)
- ✅ Parallel test (ikki tizimdan bir vaqtda)
- ✅ JSON response solishtirish (`diff`)
- ✅ Rangli terminal output
- ✅ Response fayllarni saqlash:
  - `/tmp/old_response.json`
  - `/tmp/new_response.json`

**Default Test Ma'lumotlar:**
- **OLD_BASE:** `http://localhost:8082` (old-hemis)
- **NEW_BASE:** `http://localhost:8081` (new-hemis)
- **USERNAME:** `feruz`
- **PASSWORD:** `BvZzXW6oQxEEte`
- **DEFAULT_PINFL:** `61902025630068` (database dan)

**Natija Format:**
```
✅ Responses are 100% IDENTICAL!        → Test muvaffaqiyatli, PORT tugallangan
⚠️ DIFFERENCES FOUND!                   → Farqlar bor, controller tuzatish kerak
⚠️ OLD-HEMIS DA HAM XATOLIK!            → PORT qilish KERAK EMAS (ishlamagan endpoint)
❌ HTTP 500 - ERROR                     → Server xatosi
```

**⚠️ MUHIM QOIDA:**
Agar old-hemis (port 8082) da endpoint ishlamasa (HTTP 500, 404, etc.), bu endpointni yangi hemis ga ko'chirish **KERAK EMAS**! Skript avtomatik "PORT qilish kerak emas" deb xabar beradi va exit code 0 (success) qaytaradi.

**Sabab:** Ishlamagan yoki mavjud bo'lmagan endpointlarni migration qilishning ma'nosi yo'q. Avval old-hemis da tuzatish kerak.

---

## 🔧 Serverlarni Ishga Tushirish

### Old-Hemis (port 8082)
```bash
cd /home/adm1n/startup/old-hemis
# Start old-hemis server...
```

### Yangi Hemis (port 8081)
```bash
cd /home/adm1n/startup/hemis-back
./gradlew :app:bootRun
```

---

## 📊 Test Ma'lumotlari

Barcha test ma'lumotlar `hemis_401` database dan olingan:

### PINFLlar (e_student.passport_pin)
```sql
SELECT passport_pin
FROM e_student
WHERE passport_pin IS NOT NULL
ORDER BY id
LIMIT 5;
```

**Natija:**
- `61902025630068` ✅ (default)
- `13500095268393`
- `28051513500030`
- `30101612550167`
- `30101901661820`

### Credentials
- **Username:** `feruz`
- **Password:** `BvZzXW6oQxEEte`
- **Client:** `client:secret` (Basic Auth)

---

## 🎯 Qaysi Tester Ishlatish Kerak?

| Holat | Tester | Sabab |
|-------|--------|-------|
| UI bilan test qilish | `endpoint_tester.html` | Vizual, interaktiv |
| CI/CD pipeline | `test_endpoint_comparison.sh` | Avtomatik, scriptable |
| Quick test | `test_endpoint_comparison.sh` | Tezkor, terminal |
| Multiple endpoints | `endpoint_tester.html` | Batch testing |
| Response taqqoslash | Ikkalasi ham | Side-by-side / diff |

---

## 📝 Namunalar

### Browser Tester
1. `http://localhost:9000/endpoint_tester.html` ni oching
2. "Ikkalasini Ham Test Qilish" tugmasini bosing
3. Barcha endpointlar avtomatik test qilinadi

### CLI Tester
```bash
# userInfo test
bash .scripts/test_endpoint_comparison.sh "/app/rest/v2/userInfo"

# Passport test (PINFL bilan)
bash .scripts/test_endpoint_comparison.sh "/app/rest/v2/services/pass/data?pinfl=61902025630068"

# Token test
bash .scripts/test_endpoint_comparison.sh "/app/rest/v2/oauth/token"
```

---

## ⚠️ Muhim

1. **CORS:** `endpoint_tester.html` faqat HTTP server orqali ishlaydi (file:// emas!)
2. **Serverlar:** Ikki server ham ishlab turishi kerak (8081, 8082)
3. **Database:** Test ma'lumotlar `hemis_401` database dan olingan
4. **Credentials:** `.env` faylda saqlanadi (commit qilmaslik!)

---

## 🔗 Qo'shimcha

- **ENDPOINT_PORTING_GUIDE:** `/home/adm1n/startup/hemis-back/.claude/ENDPOINT_PORTING_GUIDE.md`
- **Test Script:** `/home/adm1n/startup/hemis-back/.scripts/test_endpoint_comparison.sh`
- **HTML Tester:** `/home/adm1n/startup/hemis-back/docs/endpoint_tester.html`
