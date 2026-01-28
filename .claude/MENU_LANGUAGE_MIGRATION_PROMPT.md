# Master Prompt: Menu va Language Tizimini Migratsiya Qilish

## Maqsad
Mavjud Java (Spring Boot) + React loyihasiga HEMIS uslubidagi menu va ko'p tilli (i18n) tizimni qo'shish.

---

## 1-BOSQICH: DATABASE STRUKTURASI (Liquibase)

### 1.1 Kerakli jadvallar yaratish

Quyidagi jadvallarni yarating (har biri alohida migration fayl):

```
changesets/
├── schema/
│   ├── V001_create_users.sql
│   ├── V002_create_roles.sql
│   ├── V003_create_permissions.sql
│   ├── V004_create_user_roles.sql
│   ├── V005_create_role_permissions.sql
│   ├── V006_create_system_messages.sql
│   ├── V007_create_system_message_translations.sql
│   ├── V008_create_menus.sql
│   ├── V009_create_languages.sql
│   └── V010_create_language_translations.sql
├── seed/
│   ├── S001_seed_roles.sql
│   ├── S002_seed_permissions.sql
│   ├── S003_seed_role_permissions.sql
│   ├── S004_seed_languages.sql
│   ├── S005_seed_menus.sql
│   └── S006_seed_translations.sql
└── migration/
    └── M001_migrate_existing_users.sql
```

### 1.2 Jadval strukturalari

#### languages (Tillar)
```sql
CREATE TABLE languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL UNIQUE,  -- 'uz-UZ', 'ru-RU', 'en-US'
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Faqat bitta default til bo'lishi mumkin
CREATE UNIQUE INDEX idx_languages_single_default
ON languages(is_default) WHERE is_default = TRUE;
```

#### system_messages (Tarjima kalitlari)
```sql
CREATE TABLE system_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(50) NOT NULL,     -- 'menu', 'label', 'message', 'error'
    message_key VARCHAR(255) NOT NULL UNIQUE,  -- 'menu.dashboard', 'label.save'
    message TEXT NOT NULL,             -- Default qiymat (uz-UZ)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
```

#### system_message_translations (Tarjimalar)
```sql
CREATE TABLE system_message_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_messages(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,     -- 'uz-UZ', 'ru-RU', 'en-US'
    translation TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(message_id, language)
);
```

#### menus (Menyular)
```sql
CREATE TABLE menus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,     -- 'dashboard', 'system.users'
    i18n_key VARCHAR(255) NOT NULL,        -- 'menu.dashboard' -> system_messages.message_key
    url VARCHAR(500),
    icon VARCHAR(100),
    permission VARCHAR(100),               -- 'dashboard.view' -> permissions.code
    parent_id UUID REFERENCES menus(id),
    order_number INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
```

#### permissions (Ruxsatlar)
```sql
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,    -- 'dashboard.view', 'users.edit'
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),                 -- 'menu', 'api', 'action'
    action VARCHAR(50),                   -- 'view', 'create', 'edit', 'delete'
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### roles (Rollar)
```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,     -- 'SUPER_ADMIN', 'ADMIN', 'USER'
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE,      -- Tizim rollari o'chirilmasin
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### role_permissions (Rol-Ruxsat bog'lanishi)
```sql
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);
```

---

## 2-BOSQICH: SEED DATA (Boshlang'ich ma'lumotlar)

### 2.1 Tillar (S004_seed_languages.sql)
```sql
INSERT INTO languages (code, name, native_name, is_default, sort_order)
VALUES
    ('uz-UZ', 'Uzbek (Latin)', 'O''zbekcha', TRUE, 1),
    ('oz-UZ', 'Uzbek (Cyrillic)', 'Ўзбекча', FALSE, 2),
    ('ru-RU', 'Russian', 'Русский', FALSE, 3),
    ('en-US', 'English', 'English', FALSE, 4)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    native_name = EXCLUDED.native_name,
    updated_at = CURRENT_TIMESTAMP;
```

### 2.2 Rollar (S001_seed_roles.sql)
```sql
INSERT INTO roles (code, name, description, is_system)
VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'Tizim administratori - barcha huquqlar', TRUE),
    ('ADMIN', 'Administrator', 'Administrator - boshqaruv huquqlari', TRUE),
    ('MODERATOR', 'Moderator', 'Moderator - cheklangan boshqaruv', FALSE),
    ('USER', 'User', 'Oddiy foydalanuvchi', FALSE),
    ('VIEWER', 'Viewer', 'Faqat ko''rish huquqi', FALSE)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description;
```

### 2.3 Menyular (S005_seed_menus.sql)
```sql
-- Root menyular
INSERT INTO system_messages (category, message_key, message)
VALUES
    ('menu', 'menu.dashboard', 'Bosh sahifa'),
    ('menu', 'menu.users', 'Foydalanuvchilar'),
    ('menu', 'menu.settings', 'Sozlamalar'),
    ('menu', 'menu.system', 'Tizim')
ON CONFLICT (message_key) DO NOTHING;

INSERT INTO menus (code, i18n_key, url, icon, permission, parent_id, order_number)
VALUES
    ('dashboard', 'menu.dashboard', '/', 'home', 'dashboard.view', NULL, 1),
    ('users', 'menu.users', '/users', 'users', 'users.view', NULL, 2),
    ('settings', 'menu.settings', '/settings', 'settings', 'settings.view', NULL, 3),
    ('system', 'menu.system', NULL, 'cog', 'system.view', NULL, 99)
ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number;
```

### 2.4 Tarjimalar (S006_seed_translations.sql)

**MUHIM:** Har bir til uchun `system_message_translations` ga qo'shish kerak!

```sql
-- Russian translations
INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'ru-RU',
    CASE m.message_key
        WHEN 'menu.dashboard' THEN 'Главная'
        WHEN 'menu.users' THEN 'Пользователи'
        WHEN 'menu.settings' THEN 'Настройки'
        WHEN 'menu.system' THEN 'Система'
        ELSE m.message
    END
FROM system_messages m
WHERE m.category = 'menu'
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- English translations
INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'en-US',
    CASE m.message_key
        WHEN 'menu.dashboard' THEN 'Dashboard'
        WHEN 'menu.users' THEN 'Users'
        WHEN 'menu.settings' THEN 'Settings'
        WHEN 'menu.system' THEN 'System'
        ELSE m.message
    END
FROM system_messages m
WHERE m.category = 'menu'
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- Uzbek Latin (uz-UZ) - MUHIM: har doim qo'shish kerak!
INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'uz-UZ', m.message  -- Default message already in Uzbek
FROM system_messages m
WHERE m.category = 'menu'
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- Uzbek Cyrillic (oz-UZ)
INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'oz-UZ',
    CASE m.message_key
        WHEN 'menu.dashboard' THEN 'Бош саҳифа'
        WHEN 'menu.users' THEN 'Фойдаланувчилар'
        WHEN 'menu.settings' THEN 'Созламалар'
        WHEN 'menu.system' THEN 'Тизим'
        ELSE m.message
    END
FROM system_messages m
WHERE m.category = 'menu'
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;
```

---

## 3-BOSQICH: JAVA BACKEND

### 3.1 Entity klasslar

#### Language.java
```java
@Entity
@Table(name = "languages")
public class Language {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private String name;

    private String nativeName;
    private Boolean isDefault = false;
    private Boolean isActive = true;
    private Integer sortOrder = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### SystemMessage.java
```java
@Entity
@Table(name = "system_messages")
public class SystemMessage {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(unique = true, nullable = false)
    private String messageKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private Boolean isActive = true;

    @OneToMany(mappedBy = "systemMessage", cascade = CascadeType.ALL)
    private List<SystemMessageTranslation> translations;
}
```

#### Menu.java
```java
@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String i18nKey;

    private String url;
    private String icon;
    private String permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("orderNumber")
    private List<Menu> children;

    private Integer orderNumber = 0;
    private Boolean isActive = true;
}
```

### 3.2 Service klasslar

#### I18nService.java
```java
@Service
@Transactional(readOnly = true)
public class I18nService {

    private final SystemMessageRepository messageRepository;
    private final LanguageRepository languageRepository;

    @Cacheable(value = "translations", key = "#language")
    public Map<String, String> getTranslations(String language) {
        return messageRepository.findAllTranslations(language)
            .stream()
            .collect(Collectors.toMap(
                t -> t.getMessageKey(),
                t -> t.getTranslation()
            ));
    }

    public List<LanguageDTO> getActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderBySortOrder()
            .stream()
            .map(this::toDTO)
            .toList();
    }
}
```

#### MenuService.java
```java
@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final PermissionService permissionService;

    public List<MenuDTO> getUserMenus(UUID userId, String language) {
        Set<String> userPermissions = permissionService.getUserPermissions(userId);

        List<Menu> rootMenus = menuRepository.findByParentIdIsNullAndIsActiveTrue();

        return rootMenus.stream()
            .filter(menu -> hasPermission(menu, userPermissions))
            .map(menu -> toDTO(menu, language, userPermissions))
            .sorted(Comparator.comparing(MenuDTO::getOrderNumber))
            .toList();
    }

    private boolean hasPermission(Menu menu, Set<String> permissions) {
        if (menu.getPermission() == null) return true;
        return permissions.contains(menu.getPermission());
    }
}
```

### 3.3 REST Controller

#### I18nController.java
```java
@RestController
@RequestMapping("/api/v1/i18n")
@Tag(name = "I18n", description = "Internationalization API")
public class I18nController {

    private final I18nService i18nService;

    @GetMapping("/messages")
    @Operation(summary = "Get translations for language")
    public ResponseEntity<Map<String, String>> getMessages(
            @RequestParam(defaultValue = "uz-UZ") String lang) {
        return ResponseEntity.ok(i18nService.getTranslations(lang));
    }

    @GetMapping("/languages")
    @Operation(summary = "Get active languages")
    public ResponseEntity<List<LanguageDTO>> getLanguages() {
        return ResponseEntity.ok(i18nService.getActiveLanguages());
    }
}
```

#### MenuController.java
```java
@RestController
@RequestMapping("/api/v1/menus")
@Tag(name = "Menus", description = "Menu API")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Get user menus")
    public ResponseEntity<List<MenuDTO>> getMenus(
            @AuthenticationPrincipal UserDetails user,
            @RequestHeader(value = "Accept-Language", defaultValue = "uz-UZ") String language) {
        return ResponseEntity.ok(menuService.getUserMenus(user.getId(), language));
    }
}
```

---

## 4-BOSQICH: REACT FRONTEND

### 4.1 i18n sozlash (src/i18n/index.ts)
```typescript
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { apiClient } from '@/api/client';

const loadTranslations = async (language: string) => {
  const response = await apiClient.get(`/api/v1/i18n/messages?lang=${language}`);
  return response.data;
};

i18n
  .use(initReactI18next)
  .init({
    lng: localStorage.getItem('language') || 'uz-UZ',
    fallbackLng: 'uz-UZ',
    interpolation: { escapeValue: false },
    resources: {},
  });

export const changeLanguage = async (language: string) => {
  const translations = await loadTranslations(language);
  i18n.addResourceBundle(language, 'translation', translations, true, true);
  i18n.changeLanguage(language);
  localStorage.setItem('language', language);
};

export default i18n;
```

### 4.2 Language Switcher komponenti
```tsx
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Select } from '@/components/ui/select';
import { apiClient } from '@/api/client';
import { changeLanguage } from '@/i18n';

export function LanguageSwitcher() {
  const { i18n } = useTranslation();

  const { data: languages } = useQuery({
    queryKey: ['languages'],
    queryFn: () => apiClient.get('/api/v1/i18n/languages').then(r => r.data),
  });

  return (
    <Select
      value={i18n.language}
      onValueChange={changeLanguage}
    >
      {languages?.map((lang) => (
        <SelectItem key={lang.code} value={lang.code}>
          {lang.nativeName}
        </SelectItem>
      ))}
    </Select>
  );
}
```

### 4.3 Menu komponenti
```tsx
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { NavLink } from 'react-router-dom';
import { apiClient } from '@/api/client';

interface MenuItem {
  id: string;
  code: string;
  title: string;
  url?: string;
  icon?: string;
  children?: MenuItem[];
}

export function SidebarMenu() {
  const { t, i18n } = useTranslation();

  const { data: menus } = useQuery({
    queryKey: ['menus', i18n.language],
    queryFn: () => apiClient.get('/api/v1/menus', {
      headers: { 'Accept-Language': i18n.language }
    }).then(r => r.data),
  });

  const renderMenuItem = (item: MenuItem) => (
    <div key={item.id}>
      {item.url ? (
        <NavLink to={item.url} className="menu-item">
          {item.icon && <Icon name={item.icon} />}
          <span>{item.title}</span>
        </NavLink>
      ) : (
        <div className="menu-group">
          <span>{item.title}</span>
        </div>
      )}
      {item.children?.map(renderMenuItem)}
    </div>
  );

  return (
    <nav className="sidebar-menu">
      {menus?.map(renderMenuItem)}
    </nav>
  );
}
```

---

## 5-BOSQICH: TEKSHIRISH RO'YXATI

### Database
- [ ] Barcha jadvallar yaratildi
- [ ] Foreign key'lar to'g'ri
- [ ] Unique constraint'lar qo'shildi
- [ ] Seed data kiritildi

### Translations
- [ ] `uz-UZ` tarjimalari bor (MUHIM!)
- [ ] `oz-UZ` tarjimalari bor
- [ ] `ru-RU` tarjimalari bor
- [ ] `en-US` tarjimalari bor
- [ ] Har bir menu uchun barcha tillarda tarjima bor

### Permissions
- [ ] Menu permission'lar yaratildi
- [ ] Role-permission bog'lanishlari to'g'ri
- [ ] SUPER_ADMIN barcha permission'larga ega
- [ ] Admin user'ga SUPER_ADMIN roli tayinlangan

### Backend
- [ ] Entity'lar yaratildi
- [ ] Repository'lar yaratildi
- [ ] Service'lar yaratildi
- [ ] Controller'lar yaratildi
- [ ] Cache sozlandi

### Frontend
- [ ] i18n sozlandi
- [ ] Language switcher ishlayapti
- [ ] Menu API chaqirilayapti
- [ ] Menu permission bo'yicha filter qilinayapti

---

## 6-BOSQICH: UMUMIY XATOLAR VA YECHIMLAR

### Xato 1: Til tanlanganda menu ko'rinmaydi
**Sabab:** `system_message_translations` jadvalida shu til uchun tarjima yo'q

**Yechim:**
```sql
-- Tekshirish
SELECT language, COUNT(*) FROM system_message_translations
GROUP BY language;

-- Yo'q tilni qo'shish
INSERT INTO system_message_translations (message_id, language, translation)
SELECT id, 'uz-UZ', message FROM system_messages WHERE category = 'menu'
ON CONFLICT DO NOTHING;
```

### Xato 2: Admin uchun menu ko'rinmaydi
**Sabab:** Admin user'ga SUPER_ADMIN roli tayinlanmagan

**Yechim:**
```sql
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'migration'
FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;
```

### Xato 3: SQL Operator Precedence Bug
**Sabab:** `AND` `OR` dan oldin bajariladi

**Xato:**
```sql
WHERE role = 'USER' AND permission LIKE 'menu.%' OR permission LIKE 'data.%'
```

**To'g'ri:**
```sql
WHERE role = 'USER' AND (permission LIKE 'menu.%' OR permission LIKE 'data.%')
```

### Xato 4: Liquibase checksum error
**Sabab:** Migration fayli o'zgartirilgan

**Yechim:**
```sql
-- Checksumni yangilash
UPDATE databasechangelog
SET md5sum = 'NEW_CHECKSUM'
WHERE id = 'changeset_id';

-- Yoki qayta hisoblash
./gradlew :domain:liquibaseClearChecksums
./gradlew :domain:liquibaseUpdate
```

---

## 7-BOSQICH: BEST PRACTICES

1. **1 fayl = 1 jadval** - schema'da
2. **Har bir til uchun tarjima** - `system_message_translations` ga
3. **Idempotent seed** - `ON CONFLICT DO UPDATE`
4. **PreConditions** - `IF NOT EXISTS` o'rniga
5. **Rollback script** - har bir migration uchun
6. **Safety guards** - rollback'da row count tekshirish
7. **logicalFilePath** - Gradle/Spring mos kelishi uchun
8. **splitStatements: false** - PL/pgSQL bloklar uchun
9. **Redis cache** - tarjimalar va menu uchun
10. **Admin SUPER_ADMIN** - migration'da tayinlash

---

## Foydalanish

Bu promptni yangi loyihaga qo'llash uchun:

1. Database strukturasini yarating (1-bosqich)
2. Seed data kiriting (2-bosqich)
3. Java entity va service'larni yarating (3-bosqich)
4. React komponentlarni yarating (4-bosqich)
5. Tekshirish ro'yxatidan o'ting (5-bosqich)
6. Xatolar bo'lsa 6-bosqichdan yechim toping
