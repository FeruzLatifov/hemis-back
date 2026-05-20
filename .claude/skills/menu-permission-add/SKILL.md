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
INSERT INTO sys_permission(code, name_uz, name_ru, name_en, category) VALUES
  ('<feature>.view',   'Ko''rish',  'Просмотр',     'View',   '<feature>'),
  ('<feature>.create', 'Yaratish',  'Создание',     'Create', '<feature>'),
  ('<feature>.update', 'Tahrirlash','Редактирование','Update','<feature>'),
  ('<feature>.delete', "O'chirish", 'Удаление',     'Delete', '<feature>')
ON CONFLICT (code) DO NOTHING;
```

Convention: `<resource>.<action>` (lower kebab/dot). Action: `view/create/update/delete/import/export`.

### 2. Role'ga bog'lash

```sql
INSERT INTO sys_role_permission(role_id, permission_id)
SELECT r.id, p.id FROM sys_role r CROSS JOIN sys_permission p
WHERE r.code = 'ADMIN' AND p.code IN ('<feature>.view','<feature>.create','<feature>.update','<feature>.delete')
ON CONFLICT DO NOTHING;
```

### 3. Menu item

```sql
INSERT INTO sys_menu(code, parent_code, path, icon, sort_order, permission_code, is_active) VALUES
  ('<feature>', 'admin', '/admin/<feature>', 'feature-icon', 50, '<feature>.view', TRUE)
ON CONFLICT (code) DO NOTHING;
```

> `permission_code` MAJBURIY — frontend menyu render qilishdan oldin tekshiradi.

### 4. i18n keys

```sql
INSERT INTO sys_i18n(key, locale, value) VALUES
  ('menu.<feature>',           'uz', 'Funksiya nomi'),
  ('menu.<feature>',           'ru', 'Название функции'),
  ('menu.<feature>',           'en', 'Feature name'),
  ('<feature>.title',          'uz', 'Sahifa sarlavhasi'),
  ('<feature>.action.create',  'uz', 'Yangi qo''shish')
ON CONFLICT (key, locale) DO NOTHING;
```

3 til majburiy: `uz`, `ru`, `en`.

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

Frontend menu komponenti `permission_code` asosida render qiladi. i18n key — `menu.<feature>` formatida.

## Verification

```bash
# Permission joylashganmi
psql -d $DB_MASTER_NAME -c "SELECT code FROM sys_permission WHERE category='<feature>';"

# Menu joylashganmi
psql -d $DB_MASTER_NAME -c "SELECT code, path, permission_code FROM sys_menu WHERE code='<feature>';"

# i18n 3 ta tilda
psql -d $DB_MASTER_NAME -c "SELECT locale, value FROM sys_i18n WHERE key='menu.<feature>';"

# Backend @PreAuthorize barcha endpoint'larda
grep -L "@PreAuthorize" api-web/src/main/java/uz/hemis/web/<feature>/*.java
```

## Constraints

- ❌ Permission yo'q-u menu bor → frontend render qiladi, backend 403
- ❌ Bir til (uz/ru/en) yetishmayapti → UI'da key ko'rinadi
- ❌ Controller'da `@PreAuthorize` yo'q (OWASP A01)
- ❌ Permission code free-form (har xil pattern) → role bog'lash buziladi
- ✅ `ON CONFLICT DO NOTHING` idempotent seed

## See also

- `.claude/MENU_GUIDE.md` — to'liq schema
- V011/V012/V013 migrations
- `security` modul — RBAC implementation
- `.claude/agents/security-auditor.md` — review
