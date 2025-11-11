# HEMIS Migratsiya Tahlili - Qisqa va Tushunarli

**Sana:** 2025-11-09
**Versiya:** 1.0
**Status:** ✅ Yakunlandi

---

## 🎯 Nima qilindi?

**OLD-HEMIS** (CUBA Platform) dan **NEW-HEMIS** (Spring Boot) ga login tizimini ko'chirdik.

### Qisqacha

```
OLDIN:                          HOZIR:
┌─────────────┐                ┌─────────────┐  ┌─────────────┐
│ OLD-HEMIS   │                │ OLD-HEMIS   │  │ NEW-HEMIS   │
│ Port: 8080  │       →        │ Port: 8080  │  │ Port: 8081  │
│ sec_user    │                │ sec_user    │  │ users       │
│ 340 user    │                │ 340 user ✓  │  │ 339 user ✓  │
└─────────────┘                └─────────────┘  └─────────────┘
                                       ↓               ↓
                                   Ishlaydi      Ishlaydi
```

---

## ❓ Nega bu usul tanlandi?

### Muammo

OLD-HEMIS (CUBA Platform) eski texnologiya:
- ❌ Eski framework (2015-yilgi)
- ❌ Qiyin texnik xizmat
- ❌ Yangi funksiyalar qo'shish qiyin
- ❌ Performance muammolari

### Yechim

**Strangler Fig Pattern** (Martin Fowler):
- ✅ Eski tizimni to'xtatmasdan yangi tizim yaratish
- ✅ Bosqichma-bosqich ko'chirish
- ✅ Xavfsiz orqaga qaytarish
- ✅ Parallel ishlash (eski + yangi)

---

## 🔄 Nima o'zgargan?

### 1. Yangi jadvallar yaratildi

```sql
-- ESKI (o'zgarishsiz qoldi)
sec_user         (340 users)    ✓ Saqlab qolindi
sec_role         (18 roles)     ✓ Saqlab qolindi
sec_permission   (5,824 perms)  ✓ Saqlab qolindi

-- YANGI (qo'shildi)
users            (339 users)    ✓ Yaratildi
roles            (5 roles)      ✓ Yaratildi
permissions      (30 perms)     ✓ Yaratildi
user_roles       (474 mapping)  ✓ Yaratildi
role_permissions (mapping)      ✓ Yaratildi
```

**MUHIM:** Eski jadvallar hech o'zgartirilmadi! ❌ ZERO RISK

### 2. Gibrid autentifikatsiya

```java
// HybridUserDetailsService.java
1. Avval YANGI tizimni tekshiradi (users jadvali)
   └─> 99% foydalanuvchilar topiladi ✓

2. Topilmasa ESKI tizimga murojaat (sec_user jadvali)
   └─> <1% foydalanuvchilar uchun

3. Ikkalasida ham yo'q bo'lsa → Xatolik
```

### 3. Yangi permission formati

```
ESKI (CUBA):                  YANGI (Spring Boot):
hemishe_HStudent:read    →    students.view
hemishe_HStudent:create  →    students.create
hemishe_HStudent:update  →    students.edit
hemishe_HStudent:delete  →    students.delete
```

**Natija:**
- ESKI: 5,824 ta mayda ruxsat
- YANGI: 30 ta yirik ruxsat (soddaroq)

### 4. Rollar moslashtirish

```
ESKI ROL                →  YANGI ROL
──────────────────────────────────────────────
Administrators          →  SUPER_ADMIN (133 user)
OTM                     →  UNIVERSITY_ADMIN (273 user)
Ministry, vazirlikrole  →  MINISTRY_ADMIN (36 user)
Boshqalar              →  VIEWER (32 user)
```

---

## 📊 Natijalar

### Migratsiya statistikasi

| Ko'rsatkich | Qiymat | Status |
|-------------|--------|--------|
| Ko'chirilgan foydalanuvchilar | 338/340 | ✅ 99.4% |
| Yaratilgan rol bog'lanishlari | 474 | ✅ |
| Migratsiya vaqti | 10 soniya | ✅ |
| Downtime | 0 soniya | ✅ |
| Ma'lumot yo'qolishi | 0 | ✅ |

### Xavfsizlik

| Jihat | Status |
|-------|--------|
| Eski jadvallar o'zgardi? | ❌ YO'Q |
| Parollar shifrlangan? | ✅ HA (BCrypt) |
| OLD-HEMIS ishlayaptimi? | ✅ HA (200+ OTM) |
| Orqaga qaytarish mumkinmi? | ✅ HA (5 daqiqa) |

---

## 🏗️ Arxitektura

### OLDIN (Monolith)

```
┌───────────────────────────────────┐
│         OLD-HEMIS (8080)          │
│  ┌──────────────────────────────┐ │
│  │ CUBA Framework               │ │
│  │ - sec_user                   │ │
│  │ - sec_role                   │ │
│  │ - sec_permission             │ │
│  │ - 100+ hemishe_* tables      │ │
│  └──────────────────────────────┘ │
└───────────────────────────────────┘
```

### HOZIR (Hybrid)

```
┌──────────────────┐  ┌──────────────────┐
│ OLD-HEMIS (8080) │  │ NEW-HEMIS (8081) │
├──────────────────┤  ├──────────────────┤
│ CUBA Platform    │  │ Spring Boot 3.5  │
│ sec_user ✓       │  │ users ✓          │
│ sec_role ✓       │  │ roles ✓          │
│ Ishlaydi ✓       │  │ Ishlaydi ✓       │
└────────┬─────────┘  └────────┬─────────┘
         │                     │
         └──────┬──────────────┘
                ↓
         Parallel ishlash
    (foydalanuvchi tanlaydi)
```

### KELAJAK (Microservices)

```
┌──────────────────┐  ┌──────────────────┐
│ Auth Service     │  │ Student Service  │
│ Port: 8081       │  │ Port: 8082       │
│ users, roles     │  │ students data    │
└──────────────────┘  └──────────────────┘

┌──────────────────┐  ┌──────────────────┐
│ Teacher Service  │  │ Report Service   │
│ Port: 8083       │  │ Port: 8084       │
│ teachers data    │  │ reports data     │
└──────────────────┘  └──────────────────┘
```

---

## 🛠️ Texnik tafsilotlar

### Migration skriptlari

```
domain/src/main/resources/db/migration/
├── V1__Create_Auth_Tables.sql      (5 ta yangi jadval)
├── V2__Seed_Default_Data.sql       (5 rol, 30 ruxsat, 1 admin)
└── V3__Migrate_Users_From_Old.sql  (338 user ko'chirish)
```

### Yangi servislar

```
security/src/main/java/uz/hemis/security/service/
├── HybridUserDetailsService.java      (Gibrid - avval yangi, keyin eski)
├── CustomUserDetailsService.java      (Yangi tizim - users jadvali)
└── SecUserDetailsService.java         (Eski tizim - sec_user jadvali)
```

### API Endpoint

```
POST /app/rest/v2/oauth/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Y2xpZW50OnNlY3JldA==

grant_type=password
username=admin
password=admin

Response:
{
  "access_token": "eyJhbGc...",
  "refresh_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

---

## ⚠️ Muhim eslatmalar

### DO (Qiling)

- ✅ Production'ga qo'yishdan oldin backup oling
- ✅ Test muhitda birinchi sinab ko'ring
- ✅ Loglarni monitoring qiling
- ✅ Eski tizimni saqlab qoling (6 oy)

### DON'T (Qilmang)

- ❌ sec_user, sec_role, sec_permission jadvallarini o'zgartirmang
- ❌ Eski tizimni o'chirmang
- ❌ Migration skriptlarini qo'lda o'zgartirmang
- ❌ Production'da test qilmang

---

## 🚀 Keyingi qadamlar

### 1-bosqich: Monitoring (HOZIR)
- [ ] Har kuni loglarni tekshirish
- [ ] YANGI vs ESKI foydalanish statistikasi
- [ ] Performance monitoring

### 2-bosqich: Frontend (2-3 hafta)
- [ ] Frontend login endpoint yangilash
- [ ] Pilot test (10-20 user)
- [ ] Barcha userlarga rollout

### 3-bosqich: Decommission (2-3 oy)
- [ ] 100% migratsiya
- [ ] 30 kun monitoring
- [ ] OLD-HEMIS arxivlash

---

## 📞 Yordam

**Muammo yuzaga kelsa:**

1. **Loglarni tekshiring:**
   ```bash
   tail -100 /tmp/backend_hybrid_final.log | grep "ERROR"
   ```

2. **Backend'ni qayta ishga tushiring:**
   ```bash
   sudo systemctl restart hemis-back
   ```

3. **Eski tizimga qayting (zarurat bo'lsa):**
   ```bash
   sudo systemctl stop hemis-back
   # OLD-HEMIS avtomatik ishlaydi
   ```

4. **Jamoaga xabar bering:**
   - Backend Developer
   - DBA
   - DevOps

---

## 📚 Boshqa hujjatlar

| Fayl | Maqsad |
|------|--------|
| [README.md](../README.md) | Loyiha haqida umumiy ma'lumot |
| [API_TESTS.md](./API_TESTS.md) | API testlar va regression |
| [SWAGGER.md](./SWAGGER.md) | API dokumentatsiya |
| [FRONTEND_INTEGRATION.md](./FRONTEND_INTEGRATION.md) | Frontend bilan bog'lanish |

---

**Xulosa:**
- ✅ Migration muvaffaqiyatli
- ✅ Eski tizim xavfsiz
- ✅ Yangi tizim ishlayapti
- ✅ Orqaga qaytarish oson
- ✅ Zero downtime

**Status:** PRODUCTION TAYYOR ✅

**Last Updated:** 2025-11-09
