# Liquibase 4.x Rollback Qo'llanma

## Arxitektura

Liquibase 4.x Modern CLI framework ishlatadi. Har bir migration (changeset) dan keyin **tag** qo'yilgan. Tag orqali istalgan nuqtaga rollback qilish mumkin.

### Tag'lar Ro'yxati

```
v1-schema-complete              → V1: Schema yaratildi
v2-seed-data-complete          → V2: Seed data qo'shildi
v3-users-migrated              → V3: 339 user ko'chirildi
v4-menu-translations-complete  → V4: Menu tarjimalari
v5-faculty-translations-complete → V5: Fakultet tarjimalari
```

---

## Rollback Usullari

### 1. N ta Changeset'ni Rollback Qilish

**Komanda:**
```bash
./gradlew :domain:liquibaseRollbackCount -Pcount=5
```

**Misol:**
```bash
# Oxirgi 1 ta changeset'ni qaytarish
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# Oxirgi 3 ta changeset'ni qaytarish (V5, V4, V3)
./gradlew :domain:liquibaseRollbackCount -Pcount=3
```

**Nima sodir bo'ladi:**
- Oxirgi `N` ta changeset teskari tartibda rollback qilinadi
- `databasechangelog` jadvalidan yozuvlar o'chiriladi
- Database o'zgarishlar bekor qilinadi

---

### 2. Tag'ga Rollback Qilish

**Komanda:**
```bash
./gradlew :domain:liquibaseRollbackToTag -Ptag=TAG_NAME
```

**Misollar:**
```bash
# V3 gacha rollback (V5 va V4 bekor qilinadi)
./gradlew :domain:liquibaseRollbackToTag -Ptag=v3-users-migrated

# V2 gacha rollback (V5, V4, V3 bekor qilinadi)
./gradlew :domain:liquibaseRollbackToTag -Ptag=v2-seed-data-complete

# V1 gacha rollback (barcha user/tarjimalar o'chiriladi)
./gradlew :domain:liquibaseRollbackToTag -Ptag=v1-schema-complete
```

**Nima sodir bo'ladi:**
- Ko'rsatilgan tag'dan **keyingi** barcha changeset'lar rollback qilinadi
- Tag o'zi saqlanib qoladi
- Database ko'rsatilgan holatga qaytadi

---

### 3. Rollback SQL Ko'rish (Xavfsiz Test)

**Komanda:**
```bash
./gradlew :domain:liquibaseRollbackSQL -Pcount=5
```

**Nima qiladi:**
- Rollback SQL scriptni **ekranga chiqaradi**
- Database'ga hech qanday o'zgartirish kiritmaydi
- SQL ko'rib chiqish va test qilish uchun

**Misol:**
```bash
# Oxirgi 2 ta changeset uchun rollback SQL ni ko'rish
./gradlew :domain:liquibaseRollbackSQL -Pcount=2 > rollback.sql

# Faylni tekshirish
cat rollback.sql
```

---

### 4. Migration Holatini Tekshirish

**Komanda:**
```bash
./gradlew :domain:liquibaseStatus
```

**Natija:**
```
5 changesets have been applied:
  v1-complete-schema               (EXECUTED)
  tag-v1                          (EXECUTED)
  v2-complete-seed-data           (EXECUTED)
  tag-v2                          (EXECUTED)
  ...
```

---

### 5. Migration Tarixini Ko'rish

**Komanda:**
```bash
./gradlew :domain:liquibaseHistory
```

**Natija:**
- Qachon bajarilgan
- Kim bajargan
- Qaysi changeset'lar apply qilingan

---

## Amaliy Stsenariylar

### Stsenariy 1: V5 (Fakultet) Migration Xato - Rollback Kerak

```bash
# 1. Avval rollback SQL ni ko'ramiz (xavfsiz)
./gradlew :domain:liquibaseRollbackSQL -Pcount=1

# 2. Agar SQL to'g'ri bo'lsa, rollback qilamiz
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# 3. Holatni tekshiramiz
./gradlew :domain:liquibaseStatus

# 4. Migration faylni tuzatamiz
# 5. Qayta apply qilamiz
./gradlew :domain:liquibaseUpdate
```

---

### Stsenariy 2: V3 (Users Migration) ga Qaytish Kerak

```bash
# Tag orqali rollback (V4 va V5 bekor qilinadi)
./gradlew :domain:liquibaseRollbackToTag -Ptag=v3-users-migrated

# Holatni tekshirish
./gradlew :domain:liquibaseStatus

# Endi faqat V1, V2, V3 mavjud
```

---

### Stsenariy 3: Barcha Migration'larni Qaytarish

```bash
# Barcha tag'larni sanash
./gradlew :domain:liquibaseHistory

# 5 ta changeset (+ 5 ta tag = 10 ta entry) rollback qilish
./gradlew :domain:liquibaseRollbackCount -Pcount=10

# Yoki birinchi tag'ga qaytish
./gradlew :domain:liquibaseRollbackToTag -Ptag=v1-schema-complete

# DIQQAT: Bu barcha jadvallarni o'chiradi!
```

---

### Stsenariy 4: Production'da Rollback (Ehtiyot!)

```bash
# 1. BACKUP OLING!
pg_dump -U postgres test_hemis > backup_before_rollback.sql

# 2. Rollback SQL ni tekshiring
./gradlew :domain:liquibaseRollbackSQL -Pcount=1 > /tmp/rollback-preview.sql
cat /tmp/rollback-preview.sql

# 3. Staging muhitda test qiling
SPRING_PROFILES_ACTIVE=staging ./gradlew :domain:liquibaseRollbackCount -Pcount=1

# 4. Agar test muvaffaqiyatli bo'lsa, production'da bajaring
SPRING_PROFILES_ACTIVE=production ./gradlew :domain:liquibaseRollbackCount -Pcount=1
```

---

## Muhim Eslatmalar

### ⚠️ Xavfsizlik

1. **Har doim backup oling** rollback'dan oldin
2. **Rollback SQL ni ko'ring** bajarishdan oldin (`liquibaseRollbackSQL`)
3. **Staging'da test qiling** production'dan oldin
4. **Monitoring qo'ying** rollback jarayonida

### ⚠️ Tag'lar

- Tag'lar **avtomatik yaratiladi** har migration'dan keyin
- Tag'lar rollback paytida **o'chirilmaydi**
- Yangi migration qo'shsangiz, **yangi tag** yarating

### ⚠️ Rollback Skriptlar

- Har bir migration'da `rollback:` bo'limi mavjud
- Rollback skriptlar **qo'lda yozilgan** va test qilingan
- Agar rollback skripsiz changeset bo'lsa, rollback **ishlamaydi**

---

## Muammolarni Bartaraf Etish

### Muammo: "No changesets to rollback"

**Sabab:** Database allaqachon rollback qilingan yoki migration bajarilmagan.

**Yechim:**
```bash
# Holatni tekshiring
./gradlew :domain:liquibaseStatus

# Agar changeset'lar mavjud bo'lmasa, yangi migration apply qiling
./gradlew :domain:liquibaseUpdate
```

---

### Muammo: "Rollback script not found"

**Sabab:** Changeset'da rollback bo'limi yo'q yoki fayl topilmadi.

**Yechim:**
1. Rollback fayl mavjudligini tekshiring:
   ```bash
   ls -la domain/src/main/resources/db/changelog/changesets/*rollback*
   ```
2. `db.changelog-master.yaml` da rollback path to'g'ri ekanligini tekshiring

---

### Muammo: "Tag not found"

**Sabab:** Ko'rsatilgan tag database'da mavjud emas.

**Yechim:**
```bash
# Barcha tag'larni ko'rish
./gradlew :domain:liquibaseHistory

# Tag mavjud emasligini tekshiring
PGPASSWORD=postgres psql -h localhost -U postgres -d test_hemis \
  -c "SELECT tag FROM databasechangelog WHERE tag IS NOT NULL;"
```

---

## Qo'shimcha Komandalar

### Barcha Liquibase Tasklar

```bash
# Tasklar ro'yxatini ko'rish
./gradlew :domain:tasks --group=liquibase

# Yordam
./gradlew :domain:help --task liquibaseRollbackCount
```

### Qisqa (Alias) Komandalar

Eskiy komandalar ham qo'llab-quvvatlanadi (backward compatibility):

```bash
./gradlew :domain:status           # → liquibaseStatus
./gradlew :domain:update           # → liquibaseUpdate
./gradlew :domain:rollbackCount    # → liquibaseRollbackCount (deprecated)
```

---

## Xulosa

Liquibase 4.x **professional rollback tizimi** bilan jihozlangan:

✅ **Tag-based rollback** - istalgan holatga qaytish
✅ **Count-based rollback** - N ta changeset qaytarish
✅ **SQL preview** - xavfsiz test qilish
✅ **Rollback history** - har bir o'zgarishni kuzatish
✅ **Production-ready** - ishonchli va xavfsiz

**Eslatma:** Rollback production'da kamdan-kam ishlatiladi. Odatda yangi migration bilan oldinga borish yaxshiroqdir (forward-only migrations).
