# HEMIS API Test Vositalari

Bu papkada HEMIS backend API ni test qilish uchun ikkita mustaqil vosita mavjud.

## Vositalar

### 1. Endpoint Tester — yangi vs old hemis solishtirish

Yangi hemis-back va old-hemis endpointlarini side-by-side solishtirish.
Endpoint porting natijasini tekshirish uchun ishlatiladi.

**Papka:** `endpoint_tool/`

**Fayllar:**
- `endpoint_tester.html` — asosiy test sahifasi
- `endpoint_comparator.html` — javoblarni vizual solishtirish
- `proxy-server.py` — CORS proxy (port 9000)
- `endpoints/` — JS modullari (70 ta kategoriya)

**Batafsil:** `endpoint_tool/README.md`

---

### 2. Univer Integration Tester — PHP integratsiya testi

Univer (PHP) tizimi hemis-back ga yuboradigan barcha v2/ endpointlarni avtomatik tekshirish.
PHP format bilan mos kelishini sinash uchun ishlatiladi.

**Papka:** `univer_tool/`

**Fayllar:**
- `integration_tester.html` — asosiy test sahifasi
- `integration-proxy.py` — CORS proxy + DB bootstrap (port 9001)
- `integration/lib/` — JS kutubxonalar
- `integration/tests/` — test ta'riflari (72+ test)

**Batafsil:** `univer_tool/README.md`

---

## Tezkor boshlash

```bash
# hemis-back ni ishga tushiring
cd hemis-back
./gradlew :app:bootRun

# Brauzerda:
# Endpoint Tester:    http://localhost:8081/docs/endpoint_tool/endpoint_tester.html
# Univer Tester:      http://localhost:8081/docs/univer_tool/integration_tester.html
```

## Boshqa fayllar

- `old_hemis.md` — Old-hemis tizimi haqida ma'lumotnoma
