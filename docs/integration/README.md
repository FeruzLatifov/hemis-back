# Integration Tester

Univer (PHP) tizimi hemis-back ga yuboradigan barcha v2/ endpointlarni avtomatik tekshirish vositasi.

## Fayllar

| Fayl | Vazifasi |
|------|---------|
| `integration_tester.html` | Asosiy test sahifasi |
| `integration-proxy.py` | CORS proxy + DB bootstrap server (port 9001) |
| `integration/lib/` | JS kutubxonalar (runner, validator, bootstrap, ui) |
| `integration/tests/` | Test ta'riflari (00-12 kategoriya, 72+ test) |

## Ishga tushirish

### Variant A: API Bootstrap (oddiy)

```bash
cd hemis-back
./gradlew :app:bootRun

# Brauzerda:
# http://localhost:8081/docs/integration_tester.html
# Login → "Login & API Bootstrap"
```

Test ma'lumotlari hemis-back ning o'z entity endpointlaridan olinadi.

### Variant B: DB Bootstrap (proxy orqali)

```bash
cd hemis-back/docs
pip3 install psycopg2-binary    # birinchi marta
python3 integration-proxy.py

# Brauzerda:
# http://localhost:9001/integration_tester.html
# Login → "DB Bootstrap"
```

Test ma'lumotlari to'g'ridan-to'g'ri PostgreSQL bazasidan olinadi (UUID formatda).

## Proxy server (`integration-proxy.py`)

Bir vaqtda 3 ta vazifa bajaradi:

1. Statik fayllarni beradi (`http://localhost:9001/`)
2. API so'rovlarni hemis-back ga proxy qiladi (`/app/rest/v2/*` → `localhost:8081`)
3. DB bootstrap — bazadan test ma'lumotlarini oladi (`/db/bootstrap`)

```bash
# Default
python3 integration-proxy.py

# Old-hemis ga proxy
python3 integration-proxy.py http://localhost:8082

# DB parametrlar bilan
python3 integration-proxy.py http://localhost:8081 hemis_401 postgres postgres
```

| Port | Server |
|------|--------|
| 9001 | integration-proxy.py |
| 8081 | hemis-back (default target) |
| 8082 | old-hemis (optional target) |

### DB Bootstrap — UUID mapping

Bazadagi jadvallar turli UUID ustunlaridan foydalanadi:

| Jadval | UUID ustuni |
|--------|-----------|
| `e_student`, `e_employee`, `e_group`, `e_diploma_blank`, ... | `_uid` |
| `e_university`, `e_department` | `_qid` |
| `e_student_certificate` | `id` (UUID yo'q) |

Foreign key lar (masalan `e_student_gpa._student`) integer bo'lgani uchun,
proxy JOIN orqali parent jadvalning UUID sini oladi.

## Test kategoriyalari

| Fayl | Kategoriya | Testlar |
|------|-----------|---------|
| 00-auth | OAuth2 token | 3 |
| 01-classifiers | Klassifikatorlar | 3 |
| 02-student-services | Talaba xizmatlari | 9 |
| 03-teacher-services | O'qituvchi | 2 |
| 04-passport | Passport | 3 |
| 05-university | OTM xizmatlari | 3 |
| 06-external | Tashqi xizmatlar (BIMM, social, billing) | 15 |
| 07-entity-student | Entity: talaba | 4 |
| 08-entity-teacher | Entity: xodim | 4 |
| 09-entity-structure | Entity: tuzilma | 6 |
| 10-entity-diploma | Entity: diploma | 4 |
| 11-entity-science | Entity: ilmiy | 9 |
| 12-entity-admin | Entity: ma'muriy | 7 |

## Yangi test qo'shish

`tests/` papkasidagi faylga test qo'shing:

```javascript
{
    id: 'unique-test-id',
    category: 'Kategoriya',
    name: 'Test tavsifi',
    method: 'GET',
    url: '/app/rest/v2/...',
    auth: 'bearer',
    params: { key: '{{students[0].pinfl}}' },
    dependsOn: ['auth-token'],
    expectedStatus: 200,
    requiredFields: ['field1'],
}
```

Keyin `_index.js` ga qo'shing va `integration_tester.html` ga `<script>` tegi bilan ulang.

## Credentials

- Username: `otm401`
- Password: `XCZDAb7qvGTXxz`
- DB: `hemis_401` @ `localhost:5432` (postgres/postgres)
