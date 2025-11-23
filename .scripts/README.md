# 🧪 HEMIS Backend Test Scripts

Bu papkada endpoint testlari uchun bash scriptlar mavjud.

## 📋 Mavjud Scriptlar

### 1. test_endpoint_comparison.sh

**Maqsad:** Old-hemis va yangi-hemis endpointlarini solishtirish

**Foydalanish:**
```bash
# Oddiy endpoint (token talab qilmaydigan)
./test_endpoint_comparison.sh "/app/rest/v2/oauth/token"

# Token talab qiladigan endpoint
./test_endpoint_comparison.sh "/app/rest/v2/userInfo"

# Parametrli endpoint
./test_endpoint_comparison.sh "/app/rest/v2/services/pass/data?pinfl=31503776560016"
```

**Xususiyatlari:**
- ✅ Avtomatik token olish
- ✅ Ikki tizim parallel test qilish
- ✅ Response solishtirish
- ✅ HTTP status code tekshirish
- ✅ JSON formatda natija

**Output:**
- `/tmp/old_response.json` - Old-hemis response
- `/tmp/new_response.json` - New-hemis response

---

### 2. test_passport_endpoint.sh

**Maqsad:** Passport endpoint to'liq workflow test (token + captcha + passport)

**Foydalanish:**
```bash
# Default qiymatlar bilan (PINFL: 61902025630068, Seria: AC2455764)
./.scripts/test_passport_endpoint.sh

# Maxsus qiymatlar bilan
./.scripts/test_passport_endpoint.sh 31501662700089 AB4518000

# Faqat PINFL o'zgartirish (seria default)
./.scripts/test_passport_endpoint.sh 31503776560016
```

**Workflow:**
1. 📝 Token olish (`/app/rest/v2/oauth/token`)
2. 🎨 Captcha olish (`/app/rest/v2/services/captcha/getNumericCaptcha`)
3. 🔍 Passport test (`/app/rest/v2/services/passport-data/getDataBySN`)
4. 📊 Responselarni solishtirish

**Xususiyatlari:**
- ✅ Ikki tizim (old-hemis va new-hemis) to'liq workflow test
- ✅ Captcha validation test
- ✅ Token authentication test
- ✅ Response format solishtirish
- ✅ Ranglar bilan chiroyli output
- ✅ Xato holatlarini to'g'ri handle qilish

**Output:**
- `/tmp/old_passport_token.txt` - Old-hemis access token
- `/tmp/new_passport_token.txt` - New-hemis access token
- `/tmp/old_passport_captcha.json` - Old-hemis captcha (base64 image)
- `/tmp/new_passport_captcha.json` - New-hemis captcha (base64 image)
- `/tmp/old_passport_response.json` - Old-hemis passport response
- `/tmp/new_passport_response.json` - New-hemis passport response

**Captcha imageini ko'rish:**
```bash
# PNG faylga decode qilish
cat /tmp/new_passport_captcha.json | jq -r '.image' | base64 -d > /tmp/captcha.png

# Imageini ochish (Linux)
xdg-open /tmp/captcha.png

# Yoki endpoint_tester.html da ko'rish
# http://localhost:9000/endpoint_tester.html
```

---

## 🎨 Test Natijasi Formatlari

### Muvaffaqiyatli Response

```json
{
  "success": true,
  "data": {
    "status": 1,
    "error": null,
    "data": [
      {
        "current_pinpp": "61902025630068",
        "sur_name_latin": "ABDULLAYEV",
        "name_latin": "AKMAL",
        "patronym_name_latin": "AHMADOVICH",
        "birth_date": "1990-01-15",
        "birth_place": "TOSHKENT SHAHAR",
        "sex": "M",
        "doc_give_place": "TOSHKENT SHAHAR IIB",
        "issued_date": "2020-05-20",
        "expiry_date": "2030-05-20",
        "document": "AC2455764",
        "nationality": "O'ZBEKISTON",
        "photo": "base64_encoded_photo_string"
      }
    ]
  },
  "address": {
    "status": 1,
    "data": {
      "permanent_registration": {
        "region": "TOSHKENT SHAHAR",
        "district": "YUNUSOBOD TUMANI",
        "address": "AMIR TEMUR SHOX KO'CHASI, 1-UY"
      }
    }
  }
}
```

### Xato Response (Invalid Captcha)

```json
{
  "success": false,
  "data": "Invalid captcha!"
}
```

---

## 🛠️ Konfiguratsiya

Scriptlardagi default qiymatlar:

```bash
OLD_BASE="http://localhost:8082"    # Old-hemis base URL
NEW_BASE="http://localhost:8081"    # New-hemis base URL
USERNAME="feruz"                      # Test user
PASSWORD="BvZzXW6oQxEEte"            # Test password
CLIENT_ID="client"                    # OAuth client ID
CLIENT_SECRET="secret"                # OAuth client secret
DEFAULT_PINFL="61902025630068"        # Default PINFL (from database)
DEFAULT_SERIA="AC2455764"             # Default passport seria
```

Bu qiymatlarni o'zgartirish uchun scriptni edit qiling yoki environment variablelar qo'ying.

---

## 📊 Test Natijalarini Solishtirish

### test_endpoint_comparison.sh

```bash
# 1. Endpointni test qilish
./test_endpoint_comparison.sh "/app/rest/v2/userInfo"

# 2. Responselarni tekshirish
cat /tmp/old_response.json | jq '.'
cat /tmp/new_response.json | jq '.'

# 3. Farqlarni ko'rish
diff <(jq -S '.' /tmp/old_response.json) <(jq -S '.' /tmp/new_response.json)
```

### test_passport_endpoint.sh

Script avtomatik solishtiradi va natijani ranglar bilan ko'rsatadi:
- 🟢 **Green (✅)**: Mos keladi
- 🟡 **Yellow (⚠️)**: Ogohlantirish
- 🔴 **Red (❌)**: Xato

---

## 🚦 Server Holati Tekshirish

Agar scriptlar "server not running" xatosini ko'rsatsa:

```bash
# Old-hemis holati
curl http://localhost:8082/actuator/health

# New-hemis holati
curl http://localhost:8081/actuator/health

# Old-hemis ishga tushirish
cd /home/adm1n/startup/old-hemis && ./run.sh

# New-hemis ishga tushirish
cd /home/adm1n/startup/hemis-back && ./gradlew :app:bootRun
```

---

## 💡 Maslahatlar

### 1. Captcha Validation Test

Captcha validation to'g'ri ishlashini tekshirish:

```bash
# 1. Script ishga tushiring (noto'g'ri captcha bilan test qiladi)
./.scripts/test_passport_endpoint.sh

# 2. Captcha imageini ko'ring
cat /tmp/new_passport_captcha.json | jq -r '.image'

# 3. To'g'ri captcha bilan manual test qiling:
curl -s "http://localhost:8081/app/rest/v2/services/passport-data/getDataBySN?pinfl=61902025630068&seriaNumber=AC2455764&captchaId=YOUR_CAPTCHA_ID&captchaValue=CORRECT_VALUE" \
  -H "Authorization: Bearer YOUR_TOKEN" | jq '.'
```

### 2. Database Test Ma'lumotlari

Default PINFL va passport seria database dan olingan:

```sql
-- PINFL va passport seria olish
SELECT passport_pin, passport_given_date, passport_seria, passport_number
FROM e_student
WHERE passport_pin IS NOT NULL
LIMIT 10;
```

### 3. Parallel Test

Bir vaqtning o'zida ikkala scriptni ishga tushirish:

```bash
# Terminal 1
./.scripts/test_passport_endpoint.sh

# Terminal 2
./.scripts/test_endpoint_comparison.sh "/app/rest/v2/userInfo"
```

---

## 🐛 Debugging

Agar muammo bo'lsa:

1. **Verbose mode yoqish:**
   ```bash
   bash -x ./.scripts/test_passport_endpoint.sh
   ```

2. **Network traffic ko'rish:**
   ```bash
   # tcpdump yoki wireshark ishlatish
   sudo tcpdump -i lo port 8081 or port 8082
   ```

3. **Server loglarini tekshirish:**
   ```bash
   # New-hemis logs
   tail -f /tmp/hemis_server.log

   # Old-hemis logs
   # (old-hemis log location)
   ```

---

## ✅ Test Checklist

Har bir endpoint port qilgandan keyin:

- [ ] `test_endpoint_comparison.sh` bilan basic test
- [ ] Response format solishtirish (success, data, address)
- [ ] HTTP status code tekshirish (200, 400, 401, 404, 500)
- [ ] Captcha validation (agar kerak bo'lsa)
- [ ] Token authentication (agar kerak bo'lsa)
- [ ] Error handling (noto'g'ri input, expired token, etc.)
- [ ] endpoint_tester.html da manual test
- [ ] Swagger documentation tekshirish

---

## 📚 Qo'shimcha Ma'lumotlar

- **Endpoint porting guide:** `.claude/ENDPOINT_PORTING_GUIDE.md`
- **Test guide:** `.claude/TESTING_GUIDE.md`
- **Frontend tester:** `http://localhost:9000/endpoint_tester.html`

---

**Muallif:** HEMIS Backend Team
**Oxirgi yangilanish:** 2025-11-21
