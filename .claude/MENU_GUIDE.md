# Menu va Language (i18n) Tizimi

> Backend-driven menu + ko'p tilli tizim. F12 manipulyatsiyadan himoyalangan.

---

## Xavfsizlik Arxitekturasi

```
Frontend (React) — faqat KO'RSATADI
        │
        ▼ API Call (JWT token bilan)
Backend (Spring Boot)
  ├── L1 Cache (Caffeine/Local) — 1 min TTL
  ├── L2 Cache (Redis) — 5 min TTL (permissions, menus), 1 hour (translations)
  └── Database (PostgreSQL) — SINGLE SOURCE OF TRUTH
```

| Qoida | Noto'g'ri | To'g'ri |
|-------|-----------|---------|
| Menu | Frontend da hardcoded | Backend API dan olish |
| Permission | JWT da saqlash | Har so'rovda backend tekshiradi |
| Role | localStorage da | Redis da, JWT faqat userId |
| Tarjima | JSON fayllar | Database + Cache |
| Admin panel | Frontend route guard | Backend @PreAuthorize |

---

## Database Strukturasi

### Yaratish tartibi (FK bog'lanishlari tufayli)

```
1. languages              (bog'lanish yo'q)
2. system_messages        (bog'lanish yo'q)
3. system_message_translations (→ system_messages)
4. roles                  (bog'lanish yo'q)
5. permissions            (bog'lanish yo'q)
6. role_permissions       (→ roles, permissions)
7. menus                  (→ menus[parent], permissions)
8. user_roles             (→ users, roles)
```

### Jadvallar

```sql
-- Tillar
CREATE TABLE languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL UNIQUE,  -- 'uz-UZ', 'ru-RU', 'en-US', 'oz-UZ'
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE UNIQUE INDEX idx_languages_single_default ON languages(is_default) WHERE is_default = TRUE;

-- Tarjima kalitlari
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

-- Tarjimalar
CREATE TABLE system_message_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_messages(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(message_id, language)
);

-- Menyular
CREATE TABLE menus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,     -- 'dashboard', 'system.users'
    i18n_key VARCHAR(255) NOT NULL,        -- → system_messages.message_key
    url VARCHAR(500),
    icon VARCHAR(100),
    permission VARCHAR(100),               -- → permissions.code
    parent_id UUID REFERENCES menus(id),
    order_number INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Ruxsatlar
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

-- Rollar (mavjud jadvaldan foydalanish yoki yangi yaratish)
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rol-Ruxsat bog'lanishi
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);
```

### Seed Data

```sql
-- Tillar
INSERT INTO languages (code, name, native_name, is_default, sort_order) VALUES
    ('uz-UZ', 'Uzbek (Latin)', 'O''zbekcha', TRUE, 1),
    ('oz-UZ', 'Uzbek (Cyrillic)', 'Ўзбекча', FALSE, 2),
    ('ru-RU', 'Russian', 'Русский', FALSE, 3),
    ('en-US', 'English', 'English', FALSE, 4)
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, native_name = EXCLUDED.native_name;

-- Menu system_messages + menus
INSERT INTO system_messages (category, message_key, message) VALUES
    ('menu', 'menu.dashboard', 'Bosh sahifa'),
    ('menu', 'menu.users', 'Foydalanuvchilar'),
    ('menu', 'menu.settings', 'Sozlamalar'),
    ('menu', 'menu.system', 'Tizim')
ON CONFLICT (message_key) DO NOTHING;

INSERT INTO menus (code, i18n_key, url, icon, permission, parent_id, order_number) VALUES
    ('dashboard', 'menu.dashboard', '/', 'home', 'dashboard.view', NULL, 1),
    ('users', 'menu.users', '/users', 'users', 'users.view', NULL, 2),
    ('settings', 'menu.settings', '/settings', 'settings', 'settings.view', NULL, 3),
    ('system', 'menu.system', NULL, 'cog', 'system.view', NULL, 99)
ON CONFLICT (code) DO UPDATE SET i18n_key = EXCLUDED.i18n_key, url = EXCLUDED.url;

-- Tarjimalar (har bir til uchun)
INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'ru-RU',
    CASE m.message_key
        WHEN 'menu.dashboard' THEN 'Главная'
        WHEN 'menu.users' THEN 'Пользователи'
        WHEN 'menu.settings' THEN 'Настройки'
        WHEN 'menu.system' THEN 'Система'
    END
FROM system_messages m WHERE m.category = 'menu'
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- en-US, uz-UZ, oz-UZ uchun ham shu pattern
```

---

## Backend Implementatsiya

### PermissionService (L1 + L2 Cache)

```java
@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepo;
    private final UserRoleRepository userRoleRepo;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    // L1: Local cache
    private final Cache<UUID, Set<String>> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build();

    public Set<String> getUserPermissions(UUID userId) {
        // L1 check
        Set<String> cached = localCache.getIfPresent(userId);
        if (cached != null) return cached;

        // L2 check (Redis)
        String redisKey = "user:" + userId + ":permissions";
        cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            localCache.put(userId, cached);
            return cached;
        }

        // Database
        List<UUID> roleIds = userRoleRepo.findRoleIdsByUserId(userId);
        Set<String> permissions = rolePermissionRepo.findPermissionCodesByRoleIds(roleIds);

        // Save to caches
        redisTemplate.opsForValue().set(redisKey, permissions, Duration.ofMinutes(5));
        localCache.put(userId, permissions);
        return permissions;
    }

    public void invalidateUserCache(UUID userId) {
        localCache.invalidate(userId);
        redisTemplate.delete("user:" + userId + ":permissions");
        redisTemplate.delete("user:" + userId + ":menus:*");
    }
}
```

### MenuService

```java
@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final PermissionService permissionService;
    private final I18nService i18nService;
    private final RedisTemplate<String, List<MenuDTO>> redisTemplate;

    public List<MenuDTO> getUserMenus(UUID userId, String language) {
        String cacheKey = "user:" + userId + ":menus:" + language;
        List<MenuDTO> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        Set<String> permissions = permissionService.getUserPermissions(userId);
        List<Menu> allMenus = menuRepository.findByIsActiveTrueOrderByOrderNumber();
        List<MenuDTO> result = buildMenuTree(allMenus, permissions, language);

        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));
        return result;
    }

    private boolean hasPermission(String required, Set<String> userPerms) {
        if (required == null || required.isEmpty()) return true;
        if (userPerms.contains("*") || userPerms.contains("system.*")) return true;
        return userPerms.contains(required);
    }
}
```

### I18nService

```java
@Service
@Transactional(readOnly = true)
public class I18nService {

    @Cacheable(value = "translations", key = "#language")
    public Map<String, String> getTranslations(String language) {
        return messageRepository.findAllTranslations(language).stream()
            .collect(Collectors.toMap(t -> t.getMessageKey(), t -> t.getTranslation()));
    }

    public List<LanguageDTO> getActiveLanguages() {
        return languageRepository.findByIsActiveTrueOrderBySortOrder().stream()
            .map(this::toDTO).toList();
    }
}
```

### Controller

```java
@RestController
@RequestMapping("/api/v1/web")
public class SecureMenuController {

    @GetMapping("/menus")
    public ResponseEntity<List<MenuDTO>> getMenus(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestHeader(value = "Accept-Language", defaultValue = "uz-UZ") String lang) {
        return ResponseEntity.ok(menuService.getUserMenus(user.getId(), lang));
    }

    @GetMapping("/i18n/messages")
    public ResponseEntity<Map<String, String>> getTranslations(
            @RequestParam(defaultValue = "uz-UZ") String lang) {
        return ResponseEntity.ok(i18nService.getTranslations(lang));
    }

    @GetMapping("/i18n/languages")
    public ResponseEntity<List<LanguageDTO>> getLanguages() {
        return ResponseEntity.ok(i18nService.getActiveLanguages());
    }

    @PreAuthorize("hasAuthority('system.menus.manage')")
    @PostMapping("/admin/menus")
    public ResponseEntity<MenuDTO> createMenu(@RequestBody CreateMenuRequest req) { }

    @PreAuthorize("hasAuthority('system.menus.manage')")
    @PutMapping("/admin/menus/{id}")
    public ResponseEntity<MenuDTO> updateMenu(@PathVariable UUID id, @RequestBody UpdateMenuRequest req) { }
}
```

### Cache Config

```yaml
cache:
  permissions:
    ttl: 300  # 5 minutes
  menus:
    ttl: 300  # 5 minutes
  translations:
    ttl: 3600 # 1 hour
```

Cache invalidation triggers:
- User role o'zgardi → user cache invalidate
- Permission o'zgardi → all users cache invalidate
- Menu o'zgardi → all menu cache invalidate
- Translation o'zgardi → translation cache invalidate

---

## Frontend O'zgarishlar

### Hardcoded menu → API

```tsx
// OLDIN (xavfli)
const menuItems = [
  { path: '/dashboard', title: 'Dashboard', permission: 'dashboard.view' },
];
{menuItems.filter(item => userPermissions.includes(item.permission)).map(...)}

// KEYIN (xavfsiz)
const { data: menus } = useQuery({
  queryKey: ['menus', language],
  queryFn: () => apiClient.get('/api/v1/web/menus').then(r => r.data),
});
{menus?.map(item => <MenuItem key={item.id} {...item} />)}
```

### Permission → backend ga o'tkazish

```tsx
// OLDIN (xavfli) — localStorage da permission
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]');
{permissions.includes('users.delete') && <DeleteButton />}

// KEYIN (xavfsiz) — backend tekshiradi, 403 qaytaradi
const handleDelete = async (id) => {
  try {
    await apiClient.delete(`/api/v1/users/${id}`);
  } catch (error) {
    if (error.response?.status === 403) toast.error("Huquq yo'q");
  }
};
```

### Tarjimalar → API dan yuklash

```tsx
// i18n/index.ts
i18n.use(initReactI18next).init({
  lng: localStorage.getItem('language') || 'uz-UZ',
  fallbackLng: 'uz-UZ',
  resources: {},  // Bo'sh! API dan yuklanadi
});

export const loadTranslations = async (lang: string) => {
  const response = await apiClient.get(`/api/v1/web/i18n/messages?lang=${lang}`);
  i18n.addResourceBundle(lang, 'translation', response.data, true, true);
  i18n.changeLanguage(lang);
  localStorage.setItem('language', lang);
};
```

### Route guard (soddalashtirilgan)

```tsx
// KEYIN — permission tekshiruvi YO'Q (backend qiladi)
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <Spinner />;
  if (!isAuthenticated) return <Navigate to="/login" />;
  return children;
};
```

---

## Checklist

### Database
- [ ] Barcha jadvallar yaratildi (tartibga rioya qilib)
- [ ] Foreign key va unique constraint lar to'g'ri
- [ ] Seed data kiritildi (roles, permissions, menus, languages)
- [ ] Barcha tillar uchun tarjimalar bor (uz-UZ, oz-UZ, ru-RU, en-US)
- [ ] SUPER_ADMIN barcha permission larga ega

### Backend
- [ ] PermissionService — L1 (Caffeine) + L2 (Redis) cache
- [ ] MenuService — permission filter + i18n
- [ ] I18nService — tarjimalarni cache qilish
- [ ] JWT da permission saqlanmayapti (faqat userId)
- [ ] Har bir endpoint @PreAuthorize bor
- [ ] Cache invalidation ishlayapti

### Frontend
- [ ] Hardcoded menu lar o'chirildi → API dan yuklanadi
- [ ] localStorage dagi permission lar o'chirildi
- [ ] Frontend permission check o'chirildi (backend qiladi)
- [ ] Tarjimalar API dan yuklanayapti
- [ ] Route guard soddalashtirildi

### Xavfsizlik (F12 test)
- [ ] localStorage o'zgartirish ta'sir qilmaydi
- [ ] API response o'zgartirish ta'sir qilmaydi
- [ ] Ruxsatsiz endpoint 403 qaytaradi
- [ ] Audit log yozilayapti
