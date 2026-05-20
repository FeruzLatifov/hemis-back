package uz.hemis.service.menu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.system.Menu;
import uz.hemis.domain.entity.system.MenuType;
import uz.hemis.domain.repository.MenuRepository;
import uz.hemis.domain.repository.UserRepository;
import uz.hemis.service.cache.CacheVersionService;
import uz.hemis.service.config.LanguageProperties;
import uz.hemis.service.menu.dto.MenuItem;
import uz.hemis.service.menu.dto.MenuResponse;
import uz.hemis.service.shared.I18nService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MenuService}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>getMenuForUser - permission filtering, locale handling, empty menu</li>
 *   <li>invalidateMenuCache - version increment and publish</li>
 *   <li>getMenuCacheVersion - current version retrieval</li>
 * </ul>
 *
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService Unit Tests")
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private I18nService i18nService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheVersionService cacheVersionService;

    @Mock
    private LanguageProperties languageProperties;

    @InjectMocks
    private MenuService menuService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    /** Test helper — build a root menu without parent. */
    private static Menu menu(String code, String i18nKey, String url, String permission, int order) {
        Menu m = new Menu();
        m.setId(UUID.randomUUID());
        m.setCode(code);
        m.setI18nKey(i18nKey);
        m.setUrl(url);
        m.setPermission(permission);
        m.setActive(true);
        m.setOrderNumber(order);
        return m;
    }

    // =====================================================
    // getMenuForUser tests
    // =====================================================

    @Nested
    @DisplayName("getMenuForUser")
    class GetMenuForUser {

        @Test
        @DisplayName("returns menu response with correct locale and permissions")
        void returnsMenuResponseWithCorrectLocaleAndPermissions() {
            // Given
            String locale = "uz-UZ";
            List<String> permissions = List.of("dashboard.view", "students.view");
            when(permissionService.getUserPermissions(userId)).thenReturn(permissions);
            when(menuRepository.findAllActive()).thenReturn(Collections.emptyList());
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ", "ru-RU"));
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(Map.of());
            when(i18nService.getAllMessages("ru-RU")).thenReturn(Map.of());

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getLocale()).isEqualTo(locale);
            assertThat(response.getPermissions()).isEqualTo(permissions);
            assertThat(response.getMenu()).isEmpty();

            verify(permissionService).getUserPermissions(userId);
        }

        @Test
        @DisplayName("returns empty menu when no active menus exist in database")
        void returnsEmptyMenuWhenNoActiveMenus() {
            // Given
            when(permissionService.getUserPermissions(userId)).thenReturn(List.of("*"));
            when(menuRepository.findAllActive()).thenReturn(Collections.emptyList());
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ"));
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(Map.of());

            // When
            MenuResponse response = menuService.getMenuForUser(userId, "uz-UZ");

            // Then
            assertThat(response.getMenu()).isEmpty();
        }

        @Test
        @DisplayName("filters out menu items when user lacks required permission")
        void filtersOutMenuItemsWithoutPermission() {
            // Given
            String locale = "uz-UZ";
            List<String> permissions = List.of("dashboard.view");

            Menu dashboardMenu = new Menu();
            dashboardMenu.setId(UUID.randomUUID());
            dashboardMenu.setCode("dashboard");
            dashboardMenu.setI18nKey("Dashboard");
            dashboardMenu.setUrl("/dashboard");
            dashboardMenu.setPermission("dashboard.view");
            dashboardMenu.setActive(true);
            dashboardMenu.setOrderNumber(1);

            Menu studentsMenu = new Menu();
            studentsMenu.setId(UUID.randomUUID());
            studentsMenu.setCode("students");
            studentsMenu.setI18nKey("Students");
            studentsMenu.setUrl("/students");
            studentsMenu.setPermission("students.view");
            studentsMenu.setActive(true);
            studentsMenu.setOrderNumber(2);

            when(permissionService.getUserPermissions(userId)).thenReturn(permissions);
            when(menuRepository.findAllActive()).thenReturn(List.of(dashboardMenu, studentsMenu));
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ"));

            Map<String, String> translations = new HashMap<>();
            translations.put("Dashboard", "Bosh sahifa");
            translations.put("Students", "Talabalar");
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(translations);

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then
            assertThat(response.getMenu()).hasSize(1);
            assertThat(response.getMenu().get(0).getId()).isEqualTo("dashboard");
        }

        @Test
        @DisplayName("includes menu items with null permission (public access)")
        void includesPublicMenuItems() {
            // Given
            String locale = "uz-UZ";
            List<String> permissions = List.of();

            Menu publicMenu = new Menu();
            publicMenu.setId(UUID.randomUUID());
            publicMenu.setCode("home");
            publicMenu.setI18nKey("Home");
            publicMenu.setUrl("/home");
            publicMenu.setPermission(null);
            publicMenu.setActive(true);
            publicMenu.setOrderNumber(0);

            when(permissionService.getUserPermissions(userId)).thenReturn(permissions);
            when(menuRepository.findAllActive()).thenReturn(List.of(publicMenu));
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ"));

            Map<String, String> translations = new HashMap<>();
            translations.put("Home", "Bosh sahifa");
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(translations);

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then
            assertThat(response.getMenu()).hasSize(1);
            assertThat(response.getMenu().get(0).getId()).isEqualTo("home");
            assertThat(response.getMenu().get(0).getLabel()).isEqualTo("Bosh sahifa");
        }

        @Test
        @DisplayName("wildcard prefix permission (e.g. 'students.*') matches all children")
        void wildcardPrefixPermissionMatchesChildren() {
            // Given
            String locale = "uz-UZ";
            List<String> permissions = List.of("students.*");

            Menu studentList = menu("student-list", "Student data", "/students", "students.list.view", 1);
            Menu studentDiplomas = menu("student-diplomas", "Diplomas", "/students/diplomas", "students.diplomas.view", 2);
            Menu otherMenu = menu("reports", "Reports", "/reports", "reports.view", 3);

            when(permissionService.getUserPermissions(userId)).thenReturn(permissions);
            when(menuRepository.findAllActive()).thenReturn(List.of(studentList, studentDiplomas, otherMenu));
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ"));
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(Map.of(
                "Student data", "Talabalar ma'lumoti",
                "Diplomas", "Diplomlar",
                "Reports", "Hisobotlar"
            ));

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then
            assertThat(response.getMenu()).hasSize(2);
            assertThat(response.getMenu()).extracting(MenuItem::getId)
                .containsExactlyInAnyOrder("student-list", "student-diplomas");
        }

        @Test
        @DisplayName("empty parent (accordion-only without URL) dropped if all children filtered out")
        void emptyParentDroppedWhenAllChildrenFiltered() {
            // Given
            String locale = "uz-UZ";
            List<String> permissions = List.of(); // No permissions

            UUID parentId = UUID.randomUUID();
            Menu parent = new Menu();
            parent.setId(parentId);
            parent.setCode("students");
            parent.setI18nKey("Students");
            parent.setUrl(null); // accordion only
            parent.setPermission(null);
            parent.setActive(true);
            parent.setOrderNumber(1);

            Menu child = new Menu();
            child.setId(UUID.randomUUID());
            child.setCode("student-list");
            child.setI18nKey("Student data");
            child.setUrl("/students");
            child.setPermission("students.list.view"); // user lacks this
            child.setActive(true);
            child.setOrderNumber(1);
            child.setParent(parent);

            when(permissionService.getUserPermissions(userId)).thenReturn(permissions);
            when(menuRepository.findAllActive()).thenReturn(List.of(parent, child));
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ"));
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(Map.of(
                "Students", "Talabalar",
                "Student data", "Talabalar ma'lumoti"
            ));

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then — parent has no URL + no visible children → dropped
            assertThat(response.getMenu()).isEmpty();
        }

        @Test
        @DisplayName("locale raw qaytariladi — translation lookup'da i18nKey fallback")
        void localeRawPassedThrough_translationsFallbackToKey() {
            // Given — qo'llab-quvvatlanmaydigan locale.
            // Service raw locale'ni response.locale'ga yozadi (frontend o'zi resolve qiladi);
            // translation lookup'da label = i18nKey (Map.getOrDefault key, key) qaytadi.
            String unsupportedLocale = "xx-XX";
            when(permissionService.getUserPermissions(userId)).thenReturn(List.of("*"));

            Menu dashboard = menu("dashboard", "Dashboard", "/dashboard", null, 1);
            when(menuRepository.findAllActive()).thenReturn(List.of(dashboard));

            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ", "en-US"));
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(Map.of("Dashboard", "Bosh sahifa"));
            when(i18nService.getAllMessages("en-US")).thenReturn(Map.of("Dashboard", "Dashboard"));

            // When
            MenuResponse response = menuService.getMenuForUser(userId, unsupportedLocale);

            // Then — locale raw (xx-XX), label = i18nKey (Dashboard) — fallback xulq.
            assertThat(response.getLocale()).isEqualTo(unsupportedLocale);
            assertThat(response.getMenu()).hasSize(1);
            assertThat(response.getMenu().get(0).getLabel()).isEqualTo("Dashboard");
        }

        @Test
        @DisplayName("in-memory tree build — root menus get children attached from same SELECT")
        void inMemoryTreeBuildAttachesChildren() {
            // Given
            String locale = "uz-UZ";
            UUID parentId = UUID.randomUUID();

            Menu parent = new Menu();
            parent.setId(parentId);
            parent.setCode("students");
            parent.setI18nKey("Students");
            parent.setUrl(null);
            parent.setPermission(null);
            parent.setActive(true);
            parent.setOrderNumber(1);

            Menu child1 = new Menu();
            child1.setId(UUID.randomUUID());
            child1.setCode("student-list");
            child1.setI18nKey("Student data");
            child1.setUrl("/students");
            child1.setPermission(null);
            child1.setActive(true);
            child1.setOrderNumber(1);
            child1.setParent(parent);

            Menu child2 = new Menu();
            child2.setId(UUID.randomUUID());
            child2.setCode("student-diplomas");
            child2.setI18nKey("Diplomas");
            child2.setUrl("/students/diplomas");
            child2.setPermission(null);
            child2.setActive(true);
            child2.setOrderNumber(2);
            child2.setParent(parent);

            when(permissionService.getUserPermissions(userId)).thenReturn(List.of("*"));
            when(menuRepository.findAllActive()).thenReturn(List.of(parent, child1, child2));
            when(languageProperties.getSupported()).thenReturn(List.of("uz-UZ"));
            when(i18nService.getAllMessages("uz-UZ")).thenReturn(Map.of(
                "Students", "Talabalar",
                "Student data", "Talabalar",
                "Diplomas", "Diplomlar"
            ));

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then — single root with 2 children, NO extra DB calls
            assertThat(response.getMenu()).hasSize(1);
            MenuItem root = response.getMenu().get(0);
            assertThat(root.getId()).isEqualTo("students");
            assertThat(root.getItems()).hasSize(2);
            assertThat(root.getItems()).extracting(MenuItem::getId)
                .containsExactly("student-list", "student-diplomas");

            // Verify single SELECT (no N+1 loop)
            verify(menuRepository, times(1)).findAllActive();
            verifyNoMoreInteractions(menuRepository);
        }

        @Test
        @DisplayName("super admin (*) sees all menu items")
        void superAdminSeesAllMenuItems() {
            // Given
            String locale = "en-US";
            List<String> permissions = List.of("*");

            Menu menu1 = new Menu();
            menu1.setId(UUID.randomUUID());
            menu1.setCode("dashboard");
            menu1.setI18nKey("Dashboard");
            menu1.setUrl("/dashboard");
            menu1.setPermission("dashboard.view");
            menu1.setActive(true);
            menu1.setOrderNumber(1);

            Menu menu2 = new Menu();
            menu2.setId(UUID.randomUUID());
            menu2.setCode("admin");
            menu2.setI18nKey("Admin");
            menu2.setUrl("/admin");
            menu2.setPermission("admin.view");
            menu2.setActive(true);
            menu2.setOrderNumber(2);

            when(permissionService.getUserPermissions(userId)).thenReturn(permissions);
            when(menuRepository.findAllActive()).thenReturn(List.of(menu1, menu2));
            when(languageProperties.getSupported()).thenReturn(List.of("en-US"));

            Map<String, String> translations = new HashMap<>();
            translations.put("Dashboard", "Dashboard");
            translations.put("Admin", "Admin");
            when(i18nService.getAllMessages("en-US")).thenReturn(translations);

            // When
            MenuResponse response = menuService.getMenuForUser(userId, locale);

            // Then
            assertThat(response.getMenu()).hasSize(2);
        }
    }

    // =====================================================
    // invalidateMenuCache tests
    // =====================================================

    @Nested
    @DisplayName("invalidateMenuCache")
    class InvalidateMenuCache {

        @Test
        @DisplayName("increments version and publishes invalidation event")
        void incrementsVersionAndPublishes() {
            // Given
            when(cacheVersionService.incrementVersionAndPublish("menu")).thenReturn(5L);

            // When
            menuService.invalidateMenuCache();

            // Then
            verify(cacheVersionService).incrementVersionAndPublish("menu");
        }

        @Test
        @DisplayName("delegates to CacheVersionService with 'menu' namespace")
        void delegatesToCacheVersionService() {
            // Given
            when(cacheVersionService.incrementVersionAndPublish("menu")).thenReturn(1L);

            // When
            menuService.invalidateMenuCache();

            // Then
            verify(cacheVersionService, times(1)).incrementVersionAndPublish(eq("menu"));
            verifyNoMoreInteractions(cacheVersionService);
        }
    }

    // =====================================================
    // getMenuCacheVersion tests
    // =====================================================

    @Nested
    @DisplayName("getMenuCacheVersion")
    class GetMenuCacheVersion {

        @Test
        @DisplayName("returns current cache version from CacheVersionService")
        void returnsCurrentCacheVersion() {
            // Given
            when(cacheVersionService.getCurrentVersion("menu")).thenReturn(42L);

            // When
            long version = menuService.getMenuCacheVersion();

            // Then
            assertThat(version).isEqualTo(42L);
            verify(cacheVersionService).getCurrentVersion("menu");
        }
    }
}
