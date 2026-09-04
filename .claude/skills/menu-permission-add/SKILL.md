---
name: menu-permission-add
description: Admin web menyu + permission + i18n qo'shish (V011-V013 schema). Trigger - "menu qo'sh", "permission yarat", "tarjima qo'sh", "i18n key", "role permission".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Add Menu / Permission / i18n

> Schema: V013 (menu), V011-V012 (i18n), V001-V002+V007 (permission/RBAC). To'liq spec: `.claude/MENU_GUIDE.md`.

## Workflow

### 1. Permission yaratish

`liquibase-changeset` skill orqali V###/S### migration:

```sql
-- S###_seed_<feature>_permissions.sql
-- permission(resource, action, code, name, description, category, created_by) — V002 schema
-- name_uz/ru/en YO'Q. name = inglizcha label; tarjima system_message orqali (4-bo'lim).
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('<feature>', 'view', '<feature>.view', 'View <Feature>', 'View access', 'CUSTOM', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';
-- create/edit/delete uchun ham xuddi shu blok (action almashadi).
```

### Ruxsat nomlash qoidasi (BITTA qoida — har safar shu)

**`code` = `resource` + `.` + `action`.** Boshqa variant yo'q. Uchta natija shu qoidadan kelib chiqadi,
shuning uchun `resource`ni tanlashda ikkalasini ham o'ylab tanlang:

| Rol editorida nima ko'rinadi | Qayerdan olinadi |
|---|---|
| **Guruh** (Klassifikatorlar / Muassasalar / Tizim …) | `resource`ning **birinchi** segmenti (`permissions.meta.ts` → `DOMAIN_OF`; ro'yxatda yo'q bo'lsa → Tizim) |
| **Kartochka nomi** | `resource`ning **oxirgi** segmenti → `humanize()` → `t()` |
| **Chip** (Ko'rish / Yaratish / …) | `action` (`ACTION_META`) |

Amaliy qoidalar:
1. `resource`ning oxirgi segmenti — **ma'noli, tarjima qilinadigan ot** bo'lsin. `audit.history` → "Tarix" ✅,
   `audit.entity` → "Ob'ekt" ❌. Kalit `uz/oz/ru/en` da bo'lmasa — S### i18n seed bilan qo'shing.
2. **Bitta `(resource, action)` juftligi ikki marta bo'lmasin.** Aks holda bitta kartochkada ikkita bir xil
   chip chiqadi (`audit.view` + `audit.entity.view` aynan shunday bo'lgan edi). Yangi imkoniyat —
   yangi `resource`, ya'ni `classifiers.speciality` `classifiers`dan alohida bo'lgani kabi.
3. Yangi `action` qiymati **uch joyga** qo'shiladi: DB `chk_permission_action`, Java `PermissionAction`
   enum, `isWritePermission()`. Enumda bo'lmasa — o'sha ruxsat egasi login'da 500 oladi.
4. Chiqarilmagan (prodga ketmagan) ruxsat nomini o'zgartirsangiz — eskisini seed'da `DELETE` qiling,
   alias qoldirmang.

Action `chk_permission_action`: `view/create/edit/delete/export/import/manage/access/sync/approve` (`update` EMAS — `edit`). Category `chk_permission_category`: `CORE/ADMIN/MENU/CUSTOM/REPORTS`. UNIQUE indeks partial — `ON CONFLICT (code) WHERE deleted_at IS NULL`.

### 2. Role'ga bog'lash

```sql
-- role(code) ←→ role_permission(role_id, permission_id, assigned_by) — V001/V007 schema
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN ('<feature>.view','<feature>.create','<feature>.edit','<feature>.delete')
ON CONFLICT DO NOTHING;
```

> Real system role'lar: `SUPER_ADMIN`, `ADMIN` (S001'da `MINISTRY_ADMIN` yaratiladi, S038 uni `ADMIN`ga o'zgartiradi), `OTM_API`, `INSPECTOR`, `VIEWER`, `REPORT_VIEWER`, `CLASSIFIER_MANAGER`, `TECH_STAFF`. To'liq mapping: `S004_seed_role_permissions.sql` + pog'onalar: `S038_seed_access_control.sql`.

### 3. Menu item

```sql
-- menu(code, i18n_key, url, icon, permission, order_number, parent_id, menu_type, is_active) — V013 schema
-- parent_id — UUID (parent_code EMAS); i18n_key — system_message.message_key (4-bo'lim).
INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at, menu_type)
VALUES (
    '<deterministic-uuid>', '<feature>', '<Feature>', '/system/<feature>', 'feature-icon',
    '<feature>.view', 50, true, '<parent-uuid>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'main'
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key, url = EXCLUDED.url, icon = EXCLUDED.icon,
    permission = EXCLUDED.permission, order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id, is_active = EXCLUDED.is_active, menu_type = EXCLUDED.menu_type,
    updated_at = CURRENT_TIMESTAMP;
```

> Muqobil: V013 `upsert_menu(p_id, p_code, p_i18n_key, p_url, p_icon, p_permission, p_order_number, p_parent_id, p_menu_type)` helper — seed migration'lar shuni ishlatadi.
> `permission` ustuni (= `permission.code`) MAJBURIY — frontend menyu render qilishdan oldin tekshiradi. `parent_id` → `menu(id)` FK; misol: `system` parent UUID `S012_seed_webhook_menu.sql`'da.

### 4. i18n keys

i18n — gettext modeli (V011/V012, S006). `sys_i18n(key/locale/value)` EMAS. Ikki jadval:
`system_message(category, message_key, message)` + `system_message_translation(message_id, language, translation)`.
`message_key` — inglizcha matn (UNIQUE, en-US tarjima sifatida xizmat qiladi); menu `i18n_key` shunga ishora qiladi.

S006'dagi `_seed_msg(category, key_en, uz, oz, ru)` helper'ni ishlating (message + 4 tilni bitta chaqiruvda yozadi):

```sql
DO $$ BEGIN
  PERFORM _seed_msg('menu', '<Feature>', 'Funksiya nomi', 'Функция номи', 'Название функции');
END $$;
```

`language` jadvali (V012) qo'llab-quvvatlangan tillar ro'yxati. 4 til seed qilinadi: `uz-UZ`, `oz-UZ`, `ru-RU`, `en-US` (en-US = `message_key`ning o'zi). Menu `i18n_key` qiymati `system_message.message_key` bilan mos kelishi shart.

### 5. Backend `@PreAuthorize`

Har controller metod uchun:

```java
@GetMapping
@PreAuthorize("hasAuthority('<feature>.view')")
public List<...> list() { ... }

@PostMapping
@PreAuthorize("hasAuthority('<feature>.create')")
public ... create(...) { ... }
```

> ❌ `@PreAuthorize` yo'q → security-auditor agent reject (OWASP A01).

### 6. Frontend integration (admin web)

Frontend menu komponenti `menu.permission` (= `permission.code`) asosida render qiladi. i18n — menu `i18n_key` (= `system_message.message_key`, inglizcha matn) orqali yechiladi.

## Verification

```bash
# Permission joylashganmi
psql -d $DB_MASTER_NAME -c "SELECT code, action, category FROM permission WHERE resource='<feature>' AND deleted_at IS NULL;"

# Menu joylashganmi (permission ustuni — permission_code EMAS)
psql -d $DB_MASTER_NAME -c "SELECT code, url, permission, parent_id FROM menu WHERE code='<feature>';"

# i18n 4 ta tilda (message_key = inglizcha matn)
psql -d $DB_MASTER_NAME -c "SELECT t.language, t.translation FROM system_message_translation t JOIN system_message m ON m.id = t.message_id WHERE m.message_key='<Feature>';"

# Backend @PreAuthorize barcha endpoint'larda
grep -L "@PreAuthorize" api-web/src/main/java/uz/hemis/web/<feature>/*.java
```

## Constraints

- ❌ Permission yo'q-u menu bor → frontend render qiladi, backend 403
- ❌ Til (uz-UZ/oz-UZ/ru-RU/en-US) yetishmayapti → UI'da message_key ko'rinadi
- ❌ Controller'da `@PreAuthorize` yo'q (OWASP A01)
- ❌ Permission code free-form (har xil pattern) → role bog'lash buziladi
- ✅ `ON CONFLICT DO NOTHING` idempotent seed

## See also

- `.claude/MENU_GUIDE.md` — to'liq schema
- V011/V012/V013 migrations
- `security` modul — RBAC implementation
- `.claude/agents/security-auditor.md` — review
