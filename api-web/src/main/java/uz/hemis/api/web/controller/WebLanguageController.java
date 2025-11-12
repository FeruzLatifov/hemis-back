package uz.hemis.api.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.LanguageDto;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.entity.SystemConfiguration;
import uz.hemis.domain.repository.SystemConfigurationRepository;
import uz.hemis.service.LanguageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Language Management REST Controller - UNIVER Pattern
 *
 * <p><strong>Purpose:</strong></p>
 * <ul>
 *   <li>Manage system languages (9 languages)</li>
 *   <li>Toggle language active/inactive status</li>
 *   <li>Configure default language</li>
 *   <li>Get available languages for UI selectors</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /api/v1/web/languages - Get all languages</li>
 *   <li>GET /api/v1/web/languages/active - Get only active languages</li>
 *   <li>GET /api/v1/web/system/configuration - Get system configuration</li>
 *   <li>POST /api/v1/web/system/configuration - Update language settings (admin)</li>
 * </ul>
 *
 * @see LanguageDto
 * @see SystemConfiguration
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/web")
@Tag(name = "Language Management API", description = "Manage system languages and configuration")
@RequiredArgsConstructor
@Slf4j
public class WebLanguageController {

    private final LanguageService languageService;
    private final SystemConfigurationRepository configRepository;

    /**
     * Get all languages
     */
    @GetMapping("/languages")
    @Operation(
        summary = "Get all languages",
        description = """
            Returns all system languages (9 languages) ordered by position.

            **Use Case:** Admin panel language management

            **Languages:**
            - uz-UZ (O'zbek - Lotin) - Always active
            - oz-UZ (Ўзбек - Kirill) - Always active
            - ru-RU (Русский) - Always active
            - en-US (English) - Can toggle
            - kk-UZ (Қарақалпақша) - Can toggle
            - tg-TG (Тоҷикӣ) - Can toggle
            - kz-KZ (Қазақша) - Can toggle
            - tm-TM (Türkmençe) - Can toggle
            - kg-KG (Кыргызча) - Can toggle
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all languages",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "data": [
                            {
                              "code": "uz-UZ",
                              "name": "Uzbek (Latin)",
                              "nativeName": "O'zbekcha",
                              "position": 1,
                              "isActive": true,
                              "isDefault": true
                            },
                            {
                              "code": "ru-RU",
                              "name": "Russian",
                              "nativeName": "Русский",
                              "position": 3,
                              "isActive": true,
                              "isDefault": true
                            },
                            {
                              "code": "en-US",
                              "name": "English",
                              "nativeName": "English",
                              "position": 4,
                              "isActive": true,
                              "isDefault": false
                            }
                          ]
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<List<LanguageDto>>> getAllLanguages() {
        log.info("GET /api/v1/web/languages - Fetching all languages");

        List<LanguageDto> languages = languageService.getAllLanguages();

        return ResponseEntity.ok(ResponseWrapper.success(languages));
    }

    /**
     * Get only active languages
     */
    @GetMapping("/languages/active")
    @Operation(
        summary = "Get active languages",
        description = """
            Returns only active languages for UI language selector.

            **Use Case:** Frontend language selector dropdown

            **Frontend Integration:**
            ```javascript
            const response = await fetch('/api/v1/web/languages/active');
            const {data} = await response.json();
            // data = [{code: 'uz-UZ', name: 'O\\'zbekcha'}, ...]
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved active languages"
        )
    })
    public ResponseEntity<ResponseWrapper<List<LanguageDto>>> getActiveLanguages() {
        log.info("GET /api/v1/web/languages/active - Fetching active languages");

        List<LanguageDto> languages = languageService.getActiveLanguages();

        return ResponseEntity.ok(ResponseWrapper.success(languages));
    }

    /**
     * Get system configuration
     */
    @GetMapping("/system/configuration")
    @Operation(
        summary = "Get system configuration",
        description = """
            Returns system configuration including language settings.

            **Use Case:** Admin panel system settings page

            **Configuration Keys:**
            - system.language.uz_uz - Enable Uzbek (Latin)
            - system.language.ru_ru - Enable Russian
            - system.language.en_us - Enable English
            - system.default_language - Default system language
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved configuration",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "success": true,
                          "data": {
                            "languages": [
                              {
                                "code": "uz-UZ",
                                "name": "Uzbek (Latin)",
                                "nativeName": "O'zbekcha",
                                "enabled": true,
                                "canDisable": false
                              },
                              {
                                "code": "en-US",
                                "name": "English",
                                "nativeName": "English",
                                "enabled": true,
                                "canDisable": true
                              }
                            ],
                            "defaultLanguage": "uz-UZ"
                          }
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSystemConfiguration() {
        log.info("GET /api/v1/web/system/configuration - Fetching system configuration");

        List<LanguageDto> languages = languageService.getAllLanguages();
        List<SystemConfiguration> languageConfigs = configRepository.findAllLanguageConfigurations();

        // Build configuration map
        Map<String, Boolean> configMap = languageConfigs.stream()
            .collect(Collectors.toMap(
                SystemConfiguration::getPath,
                SystemConfiguration::getBooleanValue
            ));

        // Build response
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> languageList = languages.stream()
            .map(lang -> {
                Map<String, Object> langMap = new HashMap<>();
                langMap.put("code", lang.getCode());
                langMap.put("name", lang.getName());
                langMap.put("nativeName", lang.getNativeName());
                langMap.put("position", lang.getPosition());
                langMap.put("enabled", lang.getIsActive());
                langMap.put("canDisable", lang.getCanDisable());
                return langMap;
            })
            .collect(Collectors.toList());

        response.put("languages", languageList);

        // Get default language
        configRepository.findByPath("system.default_language")
            .ifPresent(config -> response.put("defaultLanguage", config.getValue()));

        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    /**
     * Update system configuration (admin only)
     */
    @PostMapping("/system/configuration")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Update system configuration",
        description = """
            Update language enable/disable settings.

            **Authorization:** Admin only

            **Request Body:**
            ```json
            {
              "languages": {
                "en-US": true,
                "kk-UZ": false
              },
              "defaultLanguage": "uz-UZ"
            }
            ```

            **Note:** Default languages (uz-UZ, oz-UZ, ru-RU) cannot be disabled.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Configuration updated successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required"
        )
    })
    public ResponseEntity<ResponseWrapper<String>> updateSystemConfiguration(
        @RequestBody Map<String, Object> request
    ) {
        log.info("POST /api/v1/web/system/configuration - Updating system configuration");

        // Update language settings
        if (request.containsKey("languages")) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> languages = (Map<String, Boolean>) request.get("languages");

            languages.forEach((code, enabled) -> {
                languageService.toggleLanguage(code, enabled);
            });
        }

        // Update default language
        if (request.containsKey("defaultLanguage")) {
            String defaultLang = (String) request.get("defaultLanguage");
            configRepository.findByPath("system.default_language").ifPresent(config -> {
                config.setValue(defaultLang);
                configRepository.save(config);
                log.info("Default language set to {}", defaultLang);
            });
        }

        return ResponseEntity.ok(ResponseWrapper.success("Configuration updated successfully"));
    }
}
