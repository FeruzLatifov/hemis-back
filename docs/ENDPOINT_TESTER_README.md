# 🧪 HEMIS API Endpoint Tester

Bu fayl **yangi hemis-back** va **old-hemis** tizimlari o'rtasidagi API endpoint solishtirish uchun mo'ljallangan.

## 📋 Xususiyatlar

- ✅ **Side-by-side test**: 🆕 Yangi Hemis va 🏛️ Old Hemis bir vaqtda test
- ✅ **3 test rejimi**: Yangi, Old, yoki Ikkalasini ham
- ✅ **Avtomatik solishtirish**: Response farqlarini avtomatik aniqlash
- ✅ **10 endpoint**: Token, Refresh Token, Passport (7 ta)
- ✅ **Progress tracking**: Har bir tizim uchun alohida progress bar
- ✅ **Same-origin hosting**: Swagger bilan bir serverda, CORS muammosi yo'q!

## 🚀 TEZKOR BOSHLASH (ENG OSON!)

### ✅ Usul 1: Hemis-Back Server orqali (TAVSIYA ETILADI!)

Bu usul **Swagger kabi** ishlaydi - same-origin, CORS muammosi yo'q!

```bash
# 1. Serverni ishga tushiring
cd /home/adm1n/startup/hemis-back
./gradlew :app:bootRun

# 2. Brauzerda oching
# http://localhost:8081/docs/endpoint-tester.html
```

**Afzalliklari:**
- ✅ CORS muammosi yo'q (same-origin)
- ✅ Swagger bilan bir joyda
- ✅ Production kabi ishlaydi
- ✅ File hosting kerak emas

---

## ⚠️ Boshqa Usullar (CORS Muammoli!)

Agar yuqoridagi usul ishlamasa, quyidagilarni sinab ko'ring:

### 1️⃣ Python HTTP Server (ENG OSON!)

```bash
cd /home/adm1n/startup/docs/hemis-back
python3 -m http.server 9000
```

Keyin brauzerda: **http://localhost:9000/endpoint_tester.html**

### 2️⃣ Node.js http-server

```bash
npm install -g http-server
http-server /home/adm1n/startup/docs/hemis-back -p 9000
```

Keyin brauzerda: **http://localhost:9000/endpoint_tester.html**

### 3️⃣ VS Code Live Server Extension

1. VS Code da `endpoint_tester.html` ni oching
2. O'ng pastdagi **"Go Live"** tugmasini bosing
3. Brauzerda avtomatik ochiladi

## 🚀 Qanday Ishlatish

### ⚠️ MUHIM: Avval serverlarni ishga tushiring!

Endpoint tester faqat HTML fayl - u **API so'rovlarni** yuboradi. API serverlar ishga tushgan bo'lishi kerak!

### 1. Serverlarni ishga tushiring

**Yangi Hemis-Back:**
```bash
cd /home/adm1n/startup/hemis-back
./gradlew :app:bootRun
# Port: 8081 da ishga tushadi
# Kutish: "Started HemisBackendApplication..."
```

**Old Hemis:**
```bash
cd /home/adm1n/startup/old-hemis
# Start old-hemis server
# Port: 8082 da ishga tushishi kerak
```

**Serverlar ishga tushganligini tekshiring:**
```bash
# Yangi Hemis
curl http://localhost:8081/actuator/health
# {"status":"UP"}

# Old Hemis
curl http://localhost:8082/app/rest/v2/oauth/token
# 401 yoki boshqa javob (404 emas!)
```

### 2. HTTP Server ishga tushiring

```bash
cd /home/adm1n/startup/docs/hemis-back
python3 -m http.server 9000
```

### 3. Brauzerda oching

**http://localhost:9000/endpoint_tester.html**

### 4. Konfiguratsiya

**✨ Base URLs avtomatik aniqlanadi!**

Endpoint tester browser location'dan avtomatik Base URLlarni aniqlaydi:

| Ochilgan URL | Yangi Hemis Base | Old Hemis Base | CORS |
|--------------|------------------|----------------|------|
| `http://localhost:8081/docs/endpoint-tester.html` | `http://localhost:8081` (same-origin) | `http://localhost:8082` | ✅ Yangi uchun CORS yo'q |
| `http://localhost:9000/endpoint_tester.html` | `http://localhost:8081` | `http://localhost:8082` | ⚠️ Ikkalasi uchun CORS kerak |
| `http://localhost:8082/...` | `http://localhost:8081` | `http://localhost:8082` (same-origin) | ✅ Old uchun CORS yo'q |

**Credentials (default):**
- Username: `feruz`
- Password: `BvZzXW6oQxEEte`
- PINFL: `32906015500045`

**Agar URL noto'g'ri bo'lsa:** Input fieldlarni qo'lda o'zgartirishingiz mumkin.

### 5. Testlash

Har bir endpoint uchun **3 tugma** mavjud:

1. **🆕 Yangi Hemis** - Faqat yangi tizimni test qilish
2. **🏛️ Old Hemis** - Faqat eski tizimni test qilish
3. **Ikkalasini Ham Test** - Har ikki tizimni test qilib, natijalarni solishtirish

### 6. Natijalar

**Smart Comparison** - aqlli solishtirish:

- ✅ **Response structure bir xil!** - Fieldlar va typelar mos (yashil)
- ⚠️ **Farqlar topildi** - Strukturada muammo bor (sariq)
- ❌ **Xato** - So'rov muvaffaqiyatsiz (qizil)

**Solishtirish qoidalari:**

1. **Field nomlari** - bir xil bo'lishi kerak (`access_token`, `token_type`, ...)
2. **Field typelari** - bir xil bo'lishi kerak (string, number, object)
3. **OAuth Token (endpoint #1, #2):**
   - JWT decode qilinadi
   - `username`, `scope` solishtirildadi
   - Token uzunligi **farq qilishi mumkin** (JWT vs random string)
   - Bu normal! Backward compatible ✅

**Misol:**
```
Yangi: "access_token": "eyJ..." (JWT - 500+ chars)
Eski: "access_token": "tAeNHc3..." (random - 27 chars)

Natija: ✅ Response structure bir xil!
        • Yangi: JWT format, Eski: Random string (backward compatible ✅)
```

### 7. Porting Status

Har bir endpoint yonida status badge mavjud:

- ✅ **PORTED** - Controller allaqachon yangi hemis-back ga ko'chirilgan
- ⏳ **NOT PORTED** - Hali ko'chirilmagan (faqat old-hemis da mavjud)

---

## 🔧 Old-Hemis uchun CORS Headerlarini Qo'shish (Ixtiyoriy)

**Muammo:** Old-Hemis `http://localhost:8082` da bo'lsa, yangi hemis `http://localhost:8081` dan so'rov yuborilganda CORS xatosi bo'ladi (cross-origin).

**Yechimlar:**

### ✅ Variant 1: Faqat Yangi Hemis ni Test Qilish (ENG OSON!)

Old-hemis CORS muammosini yechish shart emas! Faqat yangi hemis endpointlarini test qilishingiz mumkin:

1. **🆕 Yangi Hemis** tugmasini bosing - ishlaydi! ✅
2. Old-hemis ni alohida Swagger UI orqali test qiling
3. Natijalarni qo'lda solishtiring

**Afzalligi:** CORS sozlash shart emas, tezroq!

### ✅ Variant 2: Old-Hemis web.xml ga CORS Filter qo'shish

Old-Hemis CUBA Platform bo'lgani uchun `web.xml` da CORS filter sozlash kerak:

**Fayl:** `/old-hemis/modules/web/web/WEB-INF/web.xml`

```xml
<!-- CORS Filter for Cross-Origin Requests -->
<filter>
    <filter-name>CorsFilter</filter-name>
    <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
    <init-param>
        <param-name>cors.allowed.origins</param-name>
        <param-value>http://localhost:8081,http://localhost:9000</param-value>
    </init-param>
    <init-param>
        <param-name>cors.allowed.methods</param-name>
        <param-value>GET,POST,PUT,DELETE,OPTIONS</param-value>
    </init-param>
    <init-param>
        <param-name>cors.allowed.headers</param-name>
        <param-value>Authorization,Content-Type,Accept</param-value>
    </init-param>
    <init-param>
        <param-name>cors.exposed.headers</param-name>
        <param-value>Access-Control-Allow-Origin,Access-Control-Allow-Credentials</param-value>
    </init-param>
    <init-param>
        <param-name>cors.support.credentials</param-name>
        <param-value>true</param-value>
    </init-param>
</filter>

<filter-mapping>
    <filter-name>CorsFilter</filter-name>
    <url-pattern>/rest/*</url-pattern>
</filter-mapping>
```

**Keyin old-hemis ni rebuild va restart qiling:**

```bash
cd /home/adm1n/startup/old-hemis
./gradlew clean build deploy
# Restart Tomcat/server
```

### 🎯 Alternativ: Faqat Yangi Hemis ni Test Qilish

Agar old-hemis CORS muammosi yechilmasa:

1. **Faqat yangi hemis** endpointlarini test qiling (🆕 Yangi Hemis tugmasi)
2. Old-hemis ni Swagger UI orqali alohida test qiling
3. Natijalarni qo'lda solishtiring

## 📊 Endpoint Ro'yxati

| ID | Method | Endpoint | Tavsif |
|----|--------|----------|--------|
| 1 | POST | `/oauth/token` | Password grant (access token) |
| 2 | POST | `/oauth/token` | Refresh token grant |
| 3 | GET | `/services/pass/data` | Passport asosiy ma'lumotlari |
| 4 | GET | `/services/pass/registration` | Ro'yxatga olish ma'lumotlari |
| 5 | GET | `/services/pass/document` | Passport hujjat ma'lumotlari |
| 6 | GET | `/services/pass/birth` | Tug'ilish guvohnomasi |
| 7 | GET | `/services/pass/photo` | Passport rasmni olish |
| 8 | GET | `/services/pass/citizenship` | Fuqarolik ma'lumotlari |
| 9 | GET | `/services/pass/address` | Manzil ma'lumotlari |
| 10 | GET | `/services/pass/person` | Shaxs to'liq ma'lumotlari |

## 🔧 Muammolarni Yechish

### ❌ "Failed to fetch" xatosi

**Sabab:** CORS muammosi yoki server ishlamayapti

**Yechim:**
1. ✅ HTTP server orqali ochganingizni tekshiring (`http://localhost:9000/...`, **NOT** `file://...`)
2. ✅ Serverlar ishga tushganini tekshiring (`http://localhost:8081`, `http://localhost:8082`)
3. ✅ URL to'g'riligini tekshiring (konfiguratsiya panelida)

### ❌ 401 Unauthorized

**Sabab:** Username/password noto'g'ri yoki token muddati tugagan

**Yechim:**
1. Username va password to'g'riligini tekshiring
2. Token endpointni qayta test qiling (#1 yoki #2)

### ❌ 404 Not Found

**Sabab:** Endpoint old-hemis da mavjud emas yoki URL noto'g'ri

**Yechim:**
1. Old-hemis Swagger ni tekshiring: `http://localhost:8082/app/rest/docs/swagger.json`
2. URL to'g'riligini tasdiqlang

### ❌ Response farq bor (⚠️)

**Sabab:** Yangi va eski tizimlar turli javob qaytarmoqda

**Yechim:**
1. Response farqlarini tahlil qiling
2. Yangi tizimda backward compatibility buzilgan bo'lishi mumkin
3. Developer bilan bog'laning

## 📝 Qo'shimcha

- **Swagger Docs (Yangi):** http://localhost:8081/api/swagger-ui.html
- **Swagger Docs (Old):** http://localhost:8082/app/rest/docs/swagger.json

## 🛠️ Development

Bu fayl quyidagi texnologiyalar bilan qurilgan:

- HTML5 + Bootstrap 5.3
- Font Awesome 6.4 (icons)
- Vanilla JavaScript (fetch API)
- No dependencies (standalone file)

---

**Muallif:** HEMIS Development Team
**Sana:** 2025-01-18
**Versiya:** 2.0 (Side-by-side comparison)
