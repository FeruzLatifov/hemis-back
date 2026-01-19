# HEMIS Endpoint Tester - Proxy Server

## Muammo

Old-hemis serverda CORS headerlari yo'q, shuning uchun brauzerdan to'g'ridan-to'g'ri so'rov yuborib bo'lmaydi.

## Yechim

Python proxy server - 9000 portda ishlab, old-hemis ga server-side so'rov yuboradi va CORS headerlarini qo'shadi.

## Ishlatish

### 1️⃣ Proxy serverni ishga tushirish

```bash
cd /home/adm1n/startup/docs/hemis-back
python3 proxy-server.py
```

Natija:
```
╔══════════════════════════════════════════════════════════════╗
║  🚀 HEMIS Endpoint Tester - Proxy Server                    ║
╚══════════════════════════════════════════════════════════════╝

📡 Server: http://localhost:9000
📄 HTML:   http://localhost:9000/endpoint_tester.html
🔄 Proxy:  http://localhost:9000/proxy/old-hemis/app/rest/v2/...

✅ Proxy old-hemis ga so'rov yuboradi (CORS muammosini hal qiladi)
✅ Token avtomatik qo'shiladi

🛑 To'xtatish: Ctrl+C
```

### 2️⃣ Brauzerda ochish

```
http://localhost:9000/endpoint_tester.html
```

### 3️⃣ Ishlash printsipi

1. **9000 portda** proxy server ishga tushadi
2. **endpoint_tester.html** 9000 portda ochiladi
3. **Yangi Hemis** → `http://localhost:8081` (to'g'ridan-to'g'ri)
4. **Old Hemis** → `http://localhost:9000/proxy/old-hemis/...` (proxy orqali)

## Qanday ishlaydi?

### Eski usul (ishlamaydi):
```
Browser → http://localhost:8082/app/rest/v2/... ❌ CORS error
```

### Yangi usul (proxy orqali):
```
Browser → http://localhost:9000/proxy/old-hemis/app/rest/v2/...
          ↓
Python Proxy Server (9000 port)
          ↓ (token qo'shadi, GET/POST qo'llab-quvvatlaydi)
Old-Hemis (8082 port) → Response
          ↓ (CORS headerlari qo'shiladi)
Browser ← Response ✅
```

### Qo'llab-quvvatlanadigan metodlar:
- ✅ **GET** - Query parametrlar bilan (masalan, `/services/pass/data?pinfl=...`)
- ✅ **POST** - Form data yoki JSON body bilan (masalan, `/oauth/token`)
- ✅ **OPTIONS** - CORS preflight so'rovlar uchun

## Test qilish

### Captcha endpoint

**Yangi hemis:**
```bash
curl http://localhost:8081/app/rest/v2/services/captcha/getNumericCaptcha
```

**Old hemis (proxy orqali):**
```bash
curl http://localhost:9000/proxy/old-hemis/app/rest/v2/services/captcha/getNumericCaptcha
```

Ikkala so'rov ham bir xil formatda javob qaytarishi kerak: `{id, image}`

## Token

Token proxy serverda hardcoded: `zao3XK716L4zcP83DFgQpsdy4Fw`

Agar token muddati o'tsa, `proxy-server.py` faylida yangilash kerak:

```python
OLD_HEMIS_TOKEN = "YANGI_TOKEN"
```

## Afzalliklari

✅ **CORS muammosi yo'q** - proxy server server-side so'rov yuboradi
✅ **Token avtomatik** - foydalanuvchi token kiritmasligi mumkin
✅ **Solishtirish oson** - ikkala tizimni bir vaqtda test qilish
✅ **Oddiy** - faqat Python 3 kerak (qo'shimcha kutubxonalar shart emas)

## To'xtatish

```
Ctrl+C
```
