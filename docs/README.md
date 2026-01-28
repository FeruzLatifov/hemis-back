# HEMIS API Test Vositalari

Bu papkada HEMIS backend API ni test qilish uchun ikkita mustaqil vosita mavjud.

## Vositalar

### 1. Endpoint Tester — yangi vs old hemis solishtirish

Yangi hemis-back va old-hemis endpointlarini side-by-side solishtirish.
Endpoint porting natijasini tekshirish uchun ishlatiladi.

**Fayllar:**
- `endpoint_tester.html` — asosiy test sahifasi
- `endpoint_comparator.html` — javoblarni vizual solishtirish
- `proxy-server.py` — CORS proxy (port 9000, old-hemis uchun)
- `endpoints/` — JS modullari (70 ta kategoriya, 300+ endpoint)

**Batafsil:** `ENDPOINT_TESTER_README.md`

---

### 2. Integration Tester — Univer (PHP) integratsiya testi

Univer tizimi hemis-back ga yuboradigan barcha v2/ endpointlarni avtomatik tekshirish.
PHP format bilan mos kelishini sinash uchun ishlatiladi.

**Fayllar:**
- `integration_tester.html` — asosiy test sahifasi
- `integration-proxy.py` — CORS proxy + DB bootstrap (port 9001)
- `integration/lib/` — JS kutubxonalar (runner, validator, bootstrap, ui)
- `integration/tests/` — test ta'riflari (00-12 kategoriya, 72+ test)

**Batafsil:** `integration/README.md`

---

## Tezkor boshlash

```bash
# hemis-back ni ishga tushiring
cd hemis-back
./gradlew :app:bootRun

# Brauzerda:
# Endpoint Tester:    http://localhost:8081/docs/endpoint_tester.html
# Integration Tester: http://localhost:8081/docs/integration_tester.html
```

## Boshqa fayllar

- `old_hemis.md` — Old-hemis tizimi haqida ma'lumotnoma
