# Menu va i18n Tizimi

> Backend-driven menu + ko'p tilli tizim. F12 manipulyatsiyadan himoyalangan.
>
> **Schema haqiqat manbai:** `domain/src/main/resources/db/changelog/changesets/schema/V011-V013` (real DDL).

---

## Xavfsizlik Arxitekturasi

```
Frontend (React) — faqat KO'RSATADI
        │
        ▼ API Call (JWT token bilan)
Backend (Spring Boot)
  ├── L1 Cache (Caffeine) — 1 min TTL
  ├── L2 Cache (Redis) — 5 min TTL (permissions, menus), 1 hour (translations)
  └── PostgreSQL — SINGLE SOURCE OF TRUTH
```

| Qoida | Noto'g'ri | To'g'ri |
|-------|-----------|---------|
| Menu | Frontend da hardcoded | Backend API dan olish |
| Permission | JWT da saqlash | Har so'rovda backend tekshiradi |
| Role | localStorage da | Redis da, JWT faqat userId |
| Tarjima | JSON fayllar | Database + Cache |
| Admin panel | Frontend route guard | Backend `@PreAuthorize` |

---

## Database Strukturasi (Real V011-V013)

7 ta jadval, **barchasi SINGULAR** (rules.md "Naming Exceptions"):

| Jadval | Changeset | Maqsad |
|--------|-----------|--------|
| `language` | V012 | Qo'llab-quvvatlanadigan tillar (`code` PK: `uz-UZ`, `ru-RU`, `en-US`, `oz-UZ`) |
| `system_message` | V011 | i18n xabar kalitlari (`message_key` UNIQUE partial) |
| `system_message_translation` | V011 | Tarjimalar (FK `system_message`, UNIQUE `message_id+language`) |
| `menu` | V013 | Menyu hierarchy (parent_id self-FK, `menu_type`: main/system) |
| `role` | V001 | Rollar (RBAC) |
| `permission` | V002 | Huquqlar |
| `user_role`, `role_permission` | V007 | RBAC junction (N:N) |

**Real DDL mismatches** (eski hujjat → real):
- `languages` → `language`
- `system_messages` → `system_message`
- `system_message_translations` → `system_message_translation`
- `menus` → `menu`
- `roles` → `role`
- `permissions` → `permission`
- `role_permissions` → `role_permission`
- `user_roles` → `user_role`
- Faqat `users` PLURAL (PostgreSQL `user` reserved word)

### Menu jadval (V013)

```sql
CREATE TABLE menu (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    i18n_key VARCHAR(200) NOT NULL,        -- → system_message.message_key
    url VARCHAR(500),
    icon VARCHAR(100),
    permission VARCHAR(200),                -- → permission.code
    parent_id UUID REFERENCES menu(id) ON DELETE CASCADE,
    order_number INTEGER NOT NULL DEFAULT 0,
    menu_type VARCHAR(20) NOT NULL DEFAULT 'main' CHECK (menu_type IN ('main','system')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    -- audit ustunlar (AuditableEntity pattern)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);
```

Tafsilot: `V013_create_menus.sql` (49 seed item bilan).

### system_message jadval (V011)

```sql
CREATE TABLE system_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,         -- 'menu','label','message','error'
    message_key VARCHAR(255) NOT NULL,      -- 'menu.dashboard','label.save'
    message TEXT NOT NULL,                  -- default qiymat (uz-UZ)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- audit + soft-delete
    deleted_at TIMESTAMP,
    -- ...
);

-- Partial UNIQUE: soft-deleted xabar key'ini qayta ishlatishga ruxsat
CREATE UNIQUE INDEX uq_system_message_key
    ON system_message(message_key)
    WHERE deleted_at IS NULL;
```

Tarjimalar: `system_message_translation(message_id, language)` UNIQUE.

### Seed Data (S005-S010)

| Seed | Maqsad |
|------|--------|
| `S005_seed_languages.sql` | 4 til: uz-UZ (default), oz-UZ, ru-RU, en-US |
| `S006_seed_translations.sql` | Asosiy translation kalitlari |
| `S007_seed_classifier_menus.sql` | Menu kategoriyalari |
| `S009_seed_universities_translations.sql` | ~115 keys × 4 tilda |
| `S010_seed_frontend_translations.sql` | ~260 keys × 4 tilda (auth, reports, va h.k.) |

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

    private final Cache<UUID, Set<String>> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build();

    public Set<String> getUserPermissions(UUID userId) {
        // L1
        Set<String> cached = localCache.getIfPresent(userId);
        if (cached != null) return cached;

        // L2
        String redisKey = "user:" + userId + ":permissions";
        cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            localCache.put(userId, cached);
            return cached;
        }

        // DB
        List<UUID> roleIds = userRoleRepo.findRoleIdsByUserId(userId);
        Set<String> permissions = rolePermissionRepo.findPermissionCodesByRoleIds(roleIds);

        redisTemplate.opsForValue().set(redisKey, permissions, Duration.ofMinutes(5));
        localCache.put(userId, permissions);
        return permissions;
    }

    public void invalidateUserCache(UUID userId) {
        localCache.invalidate(userId);
        redisTemplate.delete("user:" + userId + ":permissions");
    }
}
```

### MenuService

```java
@Service
@Transactional(readOnly = true)
public class MenuService {

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
        return systemMessageTranslationRepository.findAllByLanguage(language).stream()
            .collect(Collectors.toMap(
                t -> t.getSystemMessage().getMessageKey(),
                SystemMessageTranslation::getTranslation
            ));
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
    public ResponseEntity<MenuDTO> createMenu(@RequestBody CreateMenuRequest req) { /* ... */ }
}
```

### Cache Config

```yaml
cache:
  permissions:
    ttl: 300       # 5 minutes
  menus:
    ttl: 300       # 5 minutes
  translations:
    ttl: 3600      # 1 hour
```

**Invalidation triggers:**
- User role o'zgardi → o'sha user cache invalidate
- Permission o'zgardi → barcha user cache invalidate
- Menu o'zgardi → barcha menu cache invalidate
- Translation o'zgardi → translation cache invalidate

---

## Frontend O'zgarishlar

### Hardcoded menu → API

```tsx
// OLDIN (xavfli — F12 dan o'zgartirish mumkin)
const menuItems = [
  { path: '/dashboard', title: 'Dashboard', permission: 'dashboard.view' },
];

// KEYIN (xavfsiz)
const { data: menus } = useQuery({
  queryKey: ['menus', language],
  queryFn: () => apiClient.get('/api/v1/web/menus').then(r => r.data),
});
{menus?.map(item => <MenuItem key={item.id} {...item} />)}
```

### Permission tekshiruvi → backend

```tsx
// OLDIN (xavfli) — localStorage permission
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]');
{permissions.includes('users.delete') && <DeleteButton />}

// KEYIN (xavfsiz) — backend 403 qaytaradi
const handleDelete = async (id) => {
  try {
    await apiClient.delete(`/api/v1/users/${id}`);
  } catch (e) {
    if (e.response?.status === 403) toast.error("Huquq yo'q");
  }
};
```

### Tarjimalar API dan

```tsx
i18n.use(initReactI18next).init({
  lng: localStorage.getItem('language') || 'uz-UZ',
  fallbackLng: 'uz-UZ',
  resources: {},  // Bo'sh — API dan yuklanadi
});

export const loadTranslations = async (lang: string) => {
  const response = await apiClient.get(`/api/v1/web/i18n/messages?lang=${lang}`);
  i18n.addResourceBundle(lang, 'translation', response.data, true, true);
  i18n.changeLanguage(lang);
};
```

### Route guard (soddalashtirilgan)

```tsx
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  if (isLoading) return <Spinner />;
  if (!isAuthenticated) return <Navigate to="/login" />;
  return children;  // Permission check — backend, frontend EMAS
};
```

---

## Checklist

### Database
- [ ] V011-V013 migration applied (`./gradlew :domain:liquibaseUpdate`)
- [ ] FK va Partial UNIQUE constraint'lar to'g'ri (`uq_system_message_key WHERE deleted_at IS NULL`)
- [ ] Seed data kiritildi (S005-S010)
- [ ] Barcha 4 til uchun tarjimalar bor
- [ ] SUPER_ADMIN barcha permission'larga ega (S004)

### Backend
- [ ] PermissionService — L1 (Caffeine) + L2 (Redis)
- [ ] MenuService — permission filter + i18n
- [ ] I18nService — tarjimalar cache
- [ ] JWT'da permission saqlanmayapti (faqat userId)
- [ ] Har endpoint `@PreAuthorize`
- [ ] Cache invalidation ishlayapti

### Frontend
- [ ] Hardcoded menu o'chirildi → API
- [ ] localStorage permission o'chirildi
- [ ] Frontend permission check o'chirildi (backend)
- [ ] Tarjimalar API dan
- [ ] Route guard sodda

### Xavfsizlik (F12 test)
- [ ] localStorage o'zgartirish ta'sir qilmaydi
- [ ] API response o'zgartirish ta'sir qilmaydi
- [ ] Ruxsatsiz endpoint 403 qaytaradi
- [ ] Audit log yozilayapti
