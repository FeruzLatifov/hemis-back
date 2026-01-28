# Mavjud Loyihani Xavfsiz Menu/Language Tizimiga O'tkazish

## Maqsad
F12 (DevTools) orqali frontend manipulyatsiyani yopish - menyular va tillar backend database'dan boshqariladi, frontend faqat ko'rsatadi.

---

# FAZA 1: MAVJUD LOYIHANI TAHLIL QILISH

## 1.1 Frontend tahlili

### Tekshirish kerak:
```bash
# 1. Menu qayerda saqlanayotganini toping
grep -r "menu" --include="*.tsx" --include="*.ts" src/
grep -r "sidebar" --include="*.tsx" --include="*.ts" src/
grep -r "navigation" --include="*.tsx" --include="*.ts" src/

# 2. Hardcoded menu bormi?
grep -r "menuItems" --include="*.tsx" --include="*.ts" src/
grep -r "routes" --include="*.tsx" --include="*.ts" src/

# 3. i18n qanday ishlayapti?
grep -r "useTranslation" --include="*.tsx" src/
grep -r "i18n" --include="*.ts" src/
ls -la src/locales/ 2>/dev/null || ls -la src/i18n/ 2>/dev/null

# 4. Permission tekshiruvi bormi?
grep -r "permission" --include="*.tsx" --include="*.ts" src/
grep -r "hasAccess" --include="*.tsx" --include="*.ts" src/
grep -r "canView" --include="*.tsx" --include="*.ts" src/
```

### Topilishi kerak:
- [ ] Menu array/object qayerda (hardcoded yoki API)
- [ ] Tarjimalar qayerda (JSON fayllar yoki API)
- [ ] Permission tekshiruvi qanday (frontend yoki backend)
- [ ] Role qayerdan olinadi (JWT yoki API)

## 1.2 Backend tahlili

### Tekshirish kerak:
```bash
# 1. Mavjud security konfiguratsiya
find . -name "*Security*.java" -o -name "*Config*.java" | head -20
grep -r "@PreAuthorize" --include="*.java" src/

# 2. Mavjud entity'lar
find . -name "*.java" -path "*/entity/*" | head -30
find . -name "*.java" -path "*/model/*" | head -30

# 3. Mavjud API endpoint'lar
grep -r "@RequestMapping" --include="*.java" src/
grep -r "@GetMapping" --include="*.java" src/

# 4. Mavjud database tabllar (application.yml dan)
cat src/main/resources/application.yml | grep -A5 "datasource"
```

### Topilishi kerak:
- [ ] Security qanday sozlangan (JWT, Session, OAuth)
- [ ] User/Role entity bormi
- [ ] Menu API bormi yoki yo'qmi
- [ ] Database connection ma'lumotlari

## 1.3 Database tahlili

```sql
-- Mavjud jadvallarni ko'rish
SELECT tablename FROM pg_tables WHERE schemaname = 'public';

-- User/Role jadvallar bormi
SELECT * FROM information_schema.tables
WHERE table_name LIKE '%user%' OR table_name LIKE '%role%';

-- Menu jadvali bormi
SELECT * FROM information_schema.tables
WHERE table_name LIKE '%menu%';

-- Permission jadvali bormi
SELECT * FROM information_schema.tables
WHERE table_name LIKE '%permission%';

-- Til jadvali bormi
SELECT * FROM information_schema.tables
WHERE table_name LIKE '%lang%' OR table_name LIKE '%locale%' OR table_name LIKE '%i18n%';
```

---

# FAZA 2: XAVFSIZLIK ARXITEKTURASI

## 2.1 Asosiy tamoyil

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                         │
│  - Faqat KO'RSATADI (display only)                             │
│  - Menu/Permission o'zgartira OLMAYDI                          │
│  - localStorage/sessionStorage ishonchsiz                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ API Call (JWT token bilan)
┌─────────────────────────────────────────────────────────────────┐
│                     BACKEND (Spring Boot)                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  L1 CACHE (Redis) - 5 min TTL                           │   │
│  │  - user:{userId}:permissions                            │   │
│  │  - user:{userId}:menus:{lang}                           │   │
│  │  - translations:{lang}                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼ Cache miss                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  L2 CACHE (Caffeine/Local) - 1 min TTL                  │   │
│  │  - Hot data uchun                                       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼ Cache miss                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  DATABASE (PostgreSQL)                                  │   │
│  │  - menus, permissions, roles, translations              │   │
│  │  - SINGLE SOURCE OF TRUTH                               │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## 2.2 Xavfsizlik qoidalari

| Qoida | Noto'g'ri | To'g'ri |
|-------|-----------|---------|
| Menu | Frontend'da hardcoded | Backend API'dan olish |
| Permission | JWT'da saqlash | Har so'rovda backend tekshiradi |
| Role | localStorage'da | Redis'da, JWT faqat userId |
| Tarjima | JSON fayllar | Database + Cache |
| Admin panel | Frontend route guard | Backend @PreAuthorize |

---

# FAZA 3: DATABASE MIGRATSIYA

## 3.1 Yangi jadvallar yaratish tartibi

```
Tartib muhim! Foreign key bog'lanishlari tufayli:

1. languages              (bog'lanish yo'q)
2. system_messages        (bog'lanish yo'q)
3. system_message_translations (→ system_messages, languages)
4. roles                  (bog'lanish yo'q)
5. permissions            (bog'lanish yo'q)
6. role_permissions       (→ roles, permissions)
7. menus                  (→ menus[parent], permissions)
8. users                  (agar yangi bo'lsa)
9. user_roles             (→ users, roles)
```

## 3.2 Mavjud menu'larni database'ga ko'chirish

### 3.2.1 Frontend'dan menu'larni eksport qilish

```javascript
// Browser console'da ishga tushiring
// Mavjud menu strukturasini JSON ga eksport
const menus = []; // Loyihangizdan menu array'ni toping
console.log(JSON.stringify(menus, null, 2));
```

### 3.2.2 JSON'dan SQL generatsiya

```javascript
// Node.js script: generate-menu-sql.js
const menus = require('./exported-menus.json');

function generateSQL(items, parentCode = null, level = 0) {
  let sql = '';
  let order = 1;

  for (const item of items) {
    const code = item.code || item.key || item.id;
    const i18nKey = `menu.${code.replace(/-/g, '.')}`;

    // system_messages INSERT
    sql += `INSERT INTO system_messages (category, message_key, message) VALUES ('menu', '${i18nKey}', '${item.title || item.label}') ON CONFLICT DO NOTHING;\n`;

    // menus INSERT
    sql += `INSERT INTO menus (code, i18n_key, url, icon, permission, parent_id, order_number) `;
    sql += `SELECT '${code}', '${i18nKey}', ${item.path ? `'${item.path}'` : 'NULL'}, `;
    sql += `${item.icon ? `'${item.icon}'` : 'NULL'}, '${code}.view', `;
    sql += parentCode ? `(SELECT id FROM menus WHERE code = '${parentCode}')` : 'NULL';
    sql += `, ${order} ON CONFLICT (code) DO NOTHING;\n`;

    // Recursive for children
    if (item.children?.length) {
      sql += generateSQL(item.children, code, level + 1);
    }
    order++;
  }
  return sql;
}

console.log(generateSQL(menus));
```

## 3.3 Tarjimalarni ko'chirish

### Mavjud JSON tarjimalardan SQL

```javascript
// Node.js script: generate-translation-sql.js
const uzTranslations = require('./locales/uz.json');
const ruTranslations = require('./locales/ru.json');
const enTranslations = require('./locales/en.json');

function flattenObject(obj, prefix = '') {
  return Object.keys(obj).reduce((acc, key) => {
    const newKey = prefix ? `${prefix}.${key}` : key;
    if (typeof obj[key] === 'object' && obj[key] !== null) {
      Object.assign(acc, flattenObject(obj[key], newKey));
    } else {
      acc[newKey] = obj[key];
    }
    return acc;
  }, {});
}

const uzFlat = flattenObject(uzTranslations);
const ruFlat = flattenObject(ruTranslations);
const enFlat = flattenObject(enTranslations);

// Generate SQL
for (const [key, value] of Object.entries(uzFlat)) {
  console.log(`INSERT INTO system_messages (category, message_key, message) VALUES ('ui', '${key}', '${value.replace(/'/g, "''")}') ON CONFLICT DO NOTHING;`);
}

// Translations
for (const [key, value] of Object.entries(ruFlat)) {
  console.log(`INSERT INTO system_message_translations (message_id, language, translation) SELECT id, 'ru-RU', '${value.replace(/'/g, "''")}' FROM system_messages WHERE message_key = '${key}' ON CONFLICT DO NOTHING;`);
}
```

---

# FAZA 4: BACKEND IMPLEMENTATSIYA

## 4.1 Cache konfiguratsiya

### application.yml
```yaml
spring:
  cache:
    type: redis
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}

# Custom cache settings
cache:
  permissions:
    ttl: 300  # 5 minutes
  menus:
    ttl: 300  # 5 minutes
  translations:
    ttl: 3600 # 1 hour
```

### CacheConfig.java
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "permissions", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "menus", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "translations", defaultConfig.entryTtl(Duration.ofHours(1))
        );

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

## 4.2 Permission Service (L1 + L2 Cache)

```java
@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepo;
    private final UserRoleRepository userRoleRepo;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    // L2 Cache (Local)
    private final Cache<UUID, Set<String>> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build();

    public Set<String> getUserPermissions(UUID userId) {
        // L2: Local cache
        Set<String> cached = localCache.getIfPresent(userId);
        if (cached != null) return cached;

        // L1: Redis cache
        String redisKey = "user:" + userId + ":permissions";
        cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            localCache.put(userId, cached);
            return cached;
        }

        // Database
        Set<String> permissions = loadFromDatabase(userId);

        // Save to caches
        redisTemplate.opsForValue().set(redisKey, permissions, Duration.ofMinutes(5));
        localCache.put(userId, permissions);

        return permissions;
    }

    private Set<String> loadFromDatabase(UUID userId) {
        List<UUID> roleIds = userRoleRepo.findRoleIdsByUserId(userId);
        return rolePermissionRepo.findPermissionCodesByRoleIds(roleIds);
    }

    // Cache invalidation
    public void invalidateUserCache(UUID userId) {
        localCache.invalidate(userId);
        redisTemplate.delete("user:" + userId + ":permissions");
        redisTemplate.delete("user:" + userId + ":menus:*");
    }
}
```

## 4.3 Menu Service (Xavfsiz)

```java
@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final PermissionService permissionService;
    private final I18nService i18nService;
    private final RedisTemplate<String, List<MenuDTO>> redisTemplate;

    public List<MenuDTO> getUserMenus(UUID userId, String language) {
        // Redis cache key
        String cacheKey = "user:" + userId + ":menus:" + language;

        // Check cache
        List<MenuDTO> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;

        // Get user permissions (also cached)
        Set<String> permissions = permissionService.getUserPermissions(userId);

        // Get all active root menus
        List<Menu> allMenus = menuRepository.findByIsActiveTrueOrderByOrderNumber();

        // Filter by permission and build tree
        List<MenuDTO> result = buildMenuTree(allMenus, permissions, language);

        // Cache result
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));

        return result;
    }

    private List<MenuDTO> buildMenuTree(List<Menu> menus, Set<String> permissions, String lang) {
        Map<String, String> translations = i18nService.getTranslations(lang);

        return menus.stream()
            .filter(m -> m.getParentId() == null)
            .filter(m -> hasPermission(m.getPermission(), permissions))
            .map(m -> toDTO(m, menus, permissions, translations))
            .sorted(Comparator.comparing(MenuDTO::getOrder))
            .collect(Collectors.toList());
    }

    private boolean hasPermission(String required, Set<String> userPerms) {
        if (required == null || required.isEmpty()) return true;
        // SUPER_ADMIN has all permissions
        if (userPerms.contains("*") || userPerms.contains("system.*")) return true;
        return userPerms.contains(required);
    }

    private MenuDTO toDTO(Menu menu, List<Menu> allMenus, Set<String> perms, Map<String, String> trans) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setCode(menu.getCode());
        dto.setTitle(trans.getOrDefault(menu.getI18nKey(), menu.getCode()));
        dto.setUrl(menu.getUrl());
        dto.setIcon(menu.getIcon());
        dto.setOrder(menu.getOrderNumber());

        // Recursive children
        List<MenuDTO> children = allMenus.stream()
            .filter(m -> menu.getId().equals(m.getParentId()))
            .filter(m -> hasPermission(m.getPermission(), perms))
            .map(m -> toDTO(m, allMenus, perms, trans))
            .sorted(Comparator.comparing(MenuDTO::getOrder))
            .collect(Collectors.toList());

        dto.setChildren(children.isEmpty() ? null : children);
        return dto;
    }
}
```

## 4.4 Xavfsiz API Controller

```java
@RestController
@RequestMapping("/api/v1/web")
public class SecureMenuController {

    private final MenuService menuService;
    private final I18nService i18nService;

    @GetMapping("/menus")
    public ResponseEntity<List<MenuDTO>> getMenus(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestHeader(value = "Accept-Language", defaultValue = "uz-UZ") String lang) {

        // User ID from JWT (not from request!)
        UUID userId = user.getId();

        // Backend filters by permission
        List<MenuDTO> menus = menuService.getUserMenus(userId, lang);

        return ResponseEntity.ok(menus);
    }

    @GetMapping("/i18n/messages")
    public ResponseEntity<Map<String, String>> getTranslations(
            @RequestParam(defaultValue = "uz-UZ") String lang) {
        return ResponseEntity.ok(i18nService.getTranslations(lang));
    }

    // ADMIN ONLY endpoints
    @PreAuthorize("hasAuthority('system.menus.manage')")
    @PostMapping("/admin/menus")
    public ResponseEntity<MenuDTO> createMenu(@RequestBody CreateMenuRequest req) {
        // Only admins can create menus
    }

    @PreAuthorize("hasAuthority('system.menus.manage')")
    @PutMapping("/admin/menus/{id}")
    public ResponseEntity<MenuDTO> updateMenu(@PathVariable UUID id, @RequestBody UpdateMenuRequest req) {
        // Only admins can update menus
    }
}
```

## 4.5 JWT Filter (Permission yuklamaslik)

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    // Permission'lar JWT'da SAQLANMAYDI!

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) {
        String token = extractToken(request);
        if (token != null && jwtService.isValid(token)) {
            UUID userId = jwtService.extractUserId(token);
            String username = jwtService.extractUsername(token);

            // JWT'dan faqat userId va username olinadi
            // Permission'lar har safar backend'dan tekshiriladi
            JwtUserDetails userDetails = new JwtUserDetails(userId, username);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
```

---

# FAZA 5: FRONTEND O'ZGARISHLAR

## 5.1 Hardcoded menu'larni o'chirish

### OLDIN (xavfli):
```tsx
// ❌ NOTO'G'RI - F12 orqali o'zgartirish mumkin
const menuItems = [
  { path: '/dashboard', title: 'Dashboard', permission: 'dashboard.view' },
  { path: '/admin', title: 'Admin', permission: 'admin.view' },
];

// ❌ NOTO'G'RI - Frontend permission check
{menuItems.filter(item => userPermissions.includes(item.permission)).map(...)}
```

### KEYIN (xavfsiz):
```tsx
// ✅ TO'G'RI - Backend API'dan olish
const { data: menus } = useQuery({
  queryKey: ['menus', language],
  queryFn: () => apiClient.get('/api/v1/web/menus').then(r => r.data),
});

// ✅ TO'G'RI - Backend allaqachon filter qilgan
{menus?.map(item => <MenuItem key={item.id} {...item} />)}
```

## 5.2 Permission tekshiruvni backend'ga o'tkazish

### OLDIN (xavfli):
```tsx
// ❌ NOTO'G'RI - localStorage'dan permission olish
const permissions = JSON.parse(localStorage.getItem('permissions') || '[]');

// ❌ NOTO'G'RI - Frontend'da tekshirish
{permissions.includes('users.delete') && <DeleteButton />}
```

### KEYIN (xavfsiz):
```tsx
// ✅ TO'G'RI - Har bir action uchun API call
const handleDelete = async (id) => {
  try {
    await apiClient.delete(`/api/v1/users/${id}`);
    // Backend @PreAuthorize tekshiradi
  } catch (error) {
    if (error.response?.status === 403) {
      toast.error("Sizda bu amalni bajarish huquqi yo'q");
    }
  }
};

// ✅ TO'G'RI - Button ko'rinishini backend'dan olish
const { data: userActions } = useQuery({
  queryKey: ['user-actions', userId],
  queryFn: () => apiClient.get(`/api/v1/users/${userId}/actions`).then(r => r.data),
});

{userActions?.canDelete && <DeleteButton onClick={handleDelete} />}
```

## 5.3 Tarjimalarni API'dan yuklash

```tsx
// src/i18n/index.ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { apiClient } from '@/api/client';

// ❌ O'CHIRISH: import uz from './locales/uz.json'
// ❌ O'CHIRISH: import ru from './locales/ru.json'

i18n.use(initReactI18next).init({
  lng: localStorage.getItem('language') || 'uz-UZ',
  fallbackLng: 'uz-UZ',
  resources: {}, // Bo'sh! API'dan yuklanadi
});

export const loadTranslations = async (lang: string) => {
  try {
    const response = await apiClient.get(`/api/v1/web/i18n/messages?lang=${lang}`);
    i18n.addResourceBundle(lang, 'translation', response.data, true, true);
    i18n.changeLanguage(lang);
    localStorage.setItem('language', lang);
  } catch (error) {
    console.error('Failed to load translations', error);
  }
};

// App.tsx da
useEffect(() => {
  loadTranslations(i18n.language);
}, []);
```

## 5.4 Route guard'larni backend bilan tekshirish

### OLDIN (xavfli):
```tsx
// ❌ NOTO'G'RI
const ProtectedRoute = ({ permission, children }) => {
  const permissions = usePermissions(); // localStorage'dan
  if (!permissions.includes(permission)) return <Navigate to="/403" />;
  return children;
};
```

### KEYIN (xavfsiz):
```tsx
// ✅ TO'G'RI - Route'lar menu API'dan keladi
const { data: menus } = useMenus();

// Menu'da bo'lmagan route'ga kirsa - 404
// Permission yo'q route'ga kirsa - backend 403 qaytaradi

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) return <Spinner />;
  if (!isAuthenticated) return <Navigate to="/login" />;

  // Permission tekshiruvi YO'Q - backend o'zi tekshiradi
  return children;
};
```

---

# FAZA 6: TEKSHIRISH VA XAVFSIZLIK AUDIT

## 6.1 F12 manipulyatsiya testlari

```javascript
// Browser Console'da test qiling:

// 1. localStorage'ni o'zgartirish
localStorage.setItem('permissions', '["admin.*", "system.*"]');
// Sahifani yangilang - hech narsa o'zgarmasligi kerak!

// 2. Menu API response'ni o'zgartirish
// Network tab → menus request → Edit and Resend
// Yangi menu qo'shib yuboring - backend rad etishi kerak!

// 3. Permission tekshiruvi
fetch('/api/v1/admin/users', { method: 'DELETE' })
// 403 Forbidden qaytishi kerak (agar permission yo'q bo'lsa)
```

## 6.2 Backend xavfsizlik tekshiruvi

```java
// Har bir endpoint @PreAuthorize bo'lishi kerak
@PreAuthorize("hasAuthority('users.view')")
@GetMapping("/users")
public List<UserDTO> getUsers() { }

@PreAuthorize("hasAuthority('users.create')")
@PostMapping("/users")
public UserDTO createUser() { }

@PreAuthorize("hasAuthority('users.delete')")
@DeleteMapping("/users/{id}")
public void deleteUser() { }
```

## 6.3 Audit log

```java
@Aspect
@Component
public class SecurityAuditAspect {

    @AfterReturning("@annotation(preAuthorize)")
    public void logSuccess(JoinPoint jp, PreAuthorize preAuthorize) {
        log.info("ACCESS GRANTED: {} by user {} for {}",
            preAuthorize.value(),
            getCurrentUserId(),
            jp.getSignature().getName());
    }

    @AfterThrowing(pointcut = "@annotation(preAuthorize)", throwing = "ex")
    public void logFailure(JoinPoint jp, PreAuthorize preAuthorize, AccessDeniedException ex) {
        log.warn("ACCESS DENIED: {} by user {} for {}",
            preAuthorize.value(),
            getCurrentUserId(),
            jp.getSignature().getName());
    }
}
```

---

# FAZA 7: CHECKLIST

## Database
- [ ] languages jadvali yaratildi
- [ ] system_messages jadvali yaratildi
- [ ] system_message_translations jadvali yaratildi
- [ ] menus jadvali yaratildi
- [ ] permissions jadvali yaratildi
- [ ] role_permissions jadvali yaratildi
- [ ] Mavjud menu'lar database'ga ko'chirildi
- [ ] Barcha tillar uchun tarjimalar qo'shildi (uz-UZ, oz-UZ, ru-RU, en-US)

## Backend
- [ ] Redis cache sozlandi
- [ ] L1 (Redis) + L2 (Caffeine) cache ishlayapti
- [ ] PermissionService cache bilan ishlayapti
- [ ] MenuService permission filter qilayapti
- [ ] I18nService tarjimalarni cache qilayapti
- [ ] JWT'da permission SAQLANMAYAPTI
- [ ] Har bir endpoint @PreAuthorize bor
- [ ] Cache invalidation ishlayapti

## Frontend
- [ ] Hardcoded menu'lar O'CHIRILDI
- [ ] Menu API'dan yuklanayapti
- [ ] Tarjimalar API'dan yuklanayapti
- [ ] localStorage'dagi permission'lar O'CHIRILDI
- [ ] Frontend permission check O'CHIRILDI (backend qiladi)
- [ ] Route guard'lar soddalashtirildi

## Xavfsizlik
- [ ] F12 orqali localStorage o'zgartirish ta'sir QILMAYDI
- [ ] F12 orqali API response o'zgartirish ta'sir QILMAYDI
- [ ] Ruxsatsiz endpoint 403 qaytaradi
- [ ] Audit log yozilayapti

---

# XULOSA

## Xavfsizlik tamoyillari:

1. **Backend = Haqiqat manbai** - Frontend faqat ko'rsatadi
2. **Har safar tekshirish** - JWT'ga ishonmaslik, database'dan tekshirish
3. **Cache but verify** - Cache tezlik uchun, lekin database haqiqat
4. **Defense in depth** - Frontend + Backend + Database hammasi tekshiradi
5. **Audit everything** - Barcha access log qilinadi

## Cache strategiyasi:

| Ma'lumot | L1 (Redis) | L2 (Local) | Database |
|----------|------------|------------|----------|
| Permissions | 5 min | 1 min | Source |
| Menus | 5 min | 1 min | Source |
| Translations | 1 hour | 5 min | Source |
| User data | 5 min | - | Source |

## Invalidation triggers:

- User role o'zgardi → user cache invalidate
- Permission o'zgardi → all users cache invalidate
- Menu o'zgardi → all menu cache invalidate
- Translation o'zgardi → translation cache invalidate
