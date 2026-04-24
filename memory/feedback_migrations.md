---
name: Migrations — in-place edits allowed (test DB)
description: Test DB'da (not yet production) migration fayllarini o'z joyida to'g'irlash — V020, V021 kabi yangi fayl yaratmaslik
type: feedback
---

Agar foydalanuvchi test DB (hali production emas) bilan ishlayotgan bo'lsa, mavjud migration fayllarini (V009, V010, ...) o'z joyida to'g'irlash afzal ko'riladi — yangi V020 yoki V019 yaratilmaydi.

**Why:** Foydalanuvchi test DB'ni qayta ishga tushirishga tayyor — migration faylini o'z joyida to'g'irlash faylga nisbatan oson. Yangi migration fayllar faqat production'da kerak (liquibase checksumni qo'llab-quvvatlash uchun).

**How to apply:** Agar foydalanuvchi: "databasega hali qo'llanilmagan", "test database", "mavjud migration o'zida to'g'irla" kabi so'zlarni ishlatsa — yangi changeset yaratmasdan, kerakli CREATE TABLE / INSERT ni mavjud V###_create_*.sql fayliga qo'shaman. Rollback ham yangilashi kerak. Production deploy'dan oldin bu yondashuv ruxsat etiladi.
