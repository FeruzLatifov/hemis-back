# HEMIS Integration Tester

Univer (Yii2 PHP) tizimi old-hemis ga **v2/ URL** orqali murojaat qiladi.
Bu vosita hemis-back barcha endpointlarni qo'llab-quvvatlashini avtomatik tekshiradi.

## Ishga tushirish

1. hemis-back ni ishga tushiring:
   ```bash
   cd hemis-back
   ./gradlew :app:bootRun
   ```

2. Brauzerda oching:
   ```
   http://localhost:8081/docs/integration_tester.html
   ```

3. Login ma'lumotlarini kiriting (default: `otm401` / `XCZDAb7qvGTXxz`)

4. **Login & Bootstrap** tugmasini bosing

5. **Barchasini bajarish** tugmasini bosing

## Fayl tuzilmasi

```
integration/
  lib/
    validator.js    — Response tekshiruv (pure logic)
    bootstrap.js    — API dan test ma'lumotlarini yuklash
    runner.js       — Test bajaruv mexanizmi
    ui.js           — Dashboard rendering
  tests/
    00-auth.js      — OAuth2 token testlari (3)
    01-classifiers.js — Klassifikatorlar (3)
    02-student-services.js — Talaba xizmatlari (7)
    03-teacher-services.js — O'qituvchi xizmatlari (2)
    04-passport.js  — Passport ma'lumotlari (3)
    05-university.js — OTM xizmatlari (3)
    06-external.js  — Tashqi xizmatlar (15)
    07-entity-student.js — Entity: talaba (4)
    08-entity-teacher.js — Entity: xodim (4)
    09-entity-structure.js — Entity: tuzilma (6)
    10-entity-diploma.js — Entity: diploma (4)
    11-entity-science.js — Entity: ilmiy (9)
    12-entity-admin.js — Entity: ma'muriy (7)
    _index.js       — Barcha testlarni birlashtirish
```

## Qanday ishlaydi

### Auto-Bootstrap
Test ma'lumotlari hemis-back ning o'z API endpointlaridan olinadi:
1. `POST /app/rest/v2/oauth/token` — token olish
2. Entity list endpointlardan random ma'lumotlar yuklash (parallel)
3. Yuklangan ma'lumotlar bilan testlarni bajarish

### Placeholder tizimi
Test ta'riflarida `{{students[0].pinfl}}` ko'rinishidagi placeholderlar ishlatiladi.
Ular runtime da bootstrap qilingan ma'lumotlar bilan almashtiriladi.

### Dependency ketma-ketligi
Testlar `dependsOn` parametri orqali bog'lanadi:
- `auth-token` — barcha testlar uchun majburiy
- Ba'zi testlar oldingi test natijasiga bog'liq (`storeValues`)

## Yangi test qo'shish

`tests/` papkasiga yangi fayl yarating yoki mavjud faylga test qo'shing:

```javascript
const tests_XX = [
    {
        id: 'unique-test-id',
        category: 'Kategoriya nomi',
        name: 'Test tavsifi',
        method: 'GET',
        url: '/app/rest/v2/...',
        auth: 'bearer',
        params: { key: 'value' },
        dependsOn: ['auth-token'],
        expectedStatus: 200,
        requiredFields: ['field1'],
    }
];
```

Keyin `_index.js` ga qo'shing va `integration_tester.html` ga `<script>` tegi bilan ulang.
