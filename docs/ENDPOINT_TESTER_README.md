# Endpoint Tester

Yangi hemis-back va old-hemis endpointlarini side-by-side solishtirish vositasi.

## Fayllar

| Fayl | Vazifasi |
|------|---------|
| `endpoint_tester.html` | Asosiy test sahifasi (70 kategoriya, 300+ endpoint) |
| `endpoint_comparator.html` | Javoblarni vizual solishtirish |
| `proxy-server.py` | CORS proxy server (port 9000, old-hemis uchun) |
| `endpoints/` | JS modullari (har bir kategoriya alohida fayl) |

## Ishga tushirish

### Variant A: To'g'ridan-to'g'ri (faqat yangi hemis)

```bash
cd hemis-back
./gradlew :app:bootRun

# Brauzerda:
# http://localhost:8081/docs/endpoint_tester.html
```

### Variant B: Proxy orqali (yangi + old hemis)

```bash
cd hemis-back/docs
python3 proxy-server.py

# Brauzerda:
# http://localhost:9000/endpoint_tester.html
```

## Proxy server (`proxy-server.py`)

Old-hemis da CORS headerlari yo'q. Proxy server bu muammoni hal qiladi.

```
Browser → http://localhost:9000/proxy/old-hemis/app/rest/v2/...
          ↓
Proxy Server (9000) → Old-Hemis (8082) → Response
          ↓ (CORS headerlar qo'shiladi)
Browser ← Response
```

| Port | Server |
|------|--------|
| 9000 | proxy-server.py |
| 8081 | hemis-back |
| 8082 | old-hemis |

## Credentials

- Username: `otm401`
- Password: `XCZDAb7qvGTXxz`
- Client: `Basic Y2xpZW50OnNlY3JldA==`
