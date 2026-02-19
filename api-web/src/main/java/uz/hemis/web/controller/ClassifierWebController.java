package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.classifier.*;
import uz.hemis.service.classifier.ClassifierWebService;

import java.util.List;

/**
 * Klasifikatorlar Universal Web API Controller.
 *
 * <p>Barcha 90+ klasifikator jadvali uchun yagona REST API.
 * Whitelist-based validation orqali SQL injection oldini olinadi.</p>
 */
@RestController
@RequestMapping("/api/v1/web/classifiers")
@Tag(name = "Klasifikatorlar", description = "Klasifikatorlarni ko'rish va boshqarish (universal API)")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ClassifierWebController {

    private final ClassifierWebService classifierWebService;

    // ==================== Kategoriyalar ====================

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('classifiers.view')")
    @Operation(summary = "Barcha kategoriyalar ro'yxati", description = "Klasifikator kategoriyalari va ularning klasifikatorlar sonini qaytaradi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategoriyalar ro'yxati"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<ResponseWrapper<List<ClassifierCategoryDto>>> getCategories() {
        log.info("GET /api/v1/web/classifiers/categories");
        List<ClassifierCategoryDto> categories = classifierWebService.getCategories();
        return ResponseEntity.ok(ResponseWrapper.success(categories));
    }

    @GetMapping("/categories/{category}")
    @PreAuthorize("hasAuthority('classifiers.view')")
    @Operation(summary = "Kategoriya ichidagi klasifikatorlar", description = "Tanlangan kategoriya ichidagi klasifikatorlar ro'yxatini elementlar soni bilan qaytaradi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Klasifikatorlar ro'yxati"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Kategoriya topilmadi")
    })
    public ResponseEntity<ResponseWrapper<List<ClassifierMetadataDto>>> getClassifiersByCategory(
            @Parameter(description = "Kategoriya kaliti", example = "general")
            @PathVariable String category) {
        log.info("GET /api/v1/web/classifiers/categories/{}", category);
        List<ClassifierMetadataDto> classifiers = classifierWebService.getClassifiersByCategory(category);
        if (classifiers.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ResponseWrapper.success(classifiers));
    }

    // ==================== Classifier Items ====================

    @GetMapping("/{apiKey}")
    @PreAuthorize("hasAuthority('classifiers.view')")
    @Operation(summary = "Klasifikator elementlari", description = "Bitta klasifikatorning barcha elementlarini pagination va qidiruv bilan qaytaradi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elementlar ro'yxati"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Klasifikator topilmadi")
    })
    public ResponseEntity<ResponseWrapper<Page<ClassifierItemDto>>> getClassifierItems(
            @Parameter(description = "Klasifikator API kaliti", example = "education-type")
            @PathVariable String apiKey,
            @Parameter(description = "Qidiruv so'zi (kod, nom bo'yicha)", example = "bakalavr")
            @RequestParam(required = false) String search,
            @Parameter(hidden = true)
            @PageableDefault(size = 50, sort = "code", direction = Sort.Direction.ASC)
            Pageable pageable) {
        log.info("GET /api/v1/web/classifiers/{} - search={}, page={}", apiKey, search, pageable.getPageNumber());
        try {
            Page<ClassifierItemDto> items = classifierWebService.getClassifierItems(apiKey, search, pageable);
            return ResponseEntity.ok(ResponseWrapper.success(items));
        } catch (IllegalArgumentException e) {
            log.warn("Classifier not found: {}", apiKey);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{apiKey}/{code}")
    @PreAuthorize("hasAuthority('classifiers.view')")
    @Operation(summary = "Bitta element", description = "Klasifikatorning bitta elementini kod bo'yicha qaytaradi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Element topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Element topilmadi")
    })
    public ResponseEntity<ResponseWrapper<ClassifierItemDto>> getClassifierItem(
            @Parameter(description = "Klasifikator API kaliti", example = "gender")
            @PathVariable String apiKey,
            @Parameter(description = "Element kodi", example = "11")
            @PathVariable String code) {
        log.info("GET /api/v1/web/classifiers/{}/{}", apiKey, code);
        try {
            ClassifierItemDto item = classifierWebService.getClassifierItem(apiKey, code);
            if (item == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ResponseWrapper.success(item));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== WRITE Operations ====================

    @PostMapping("/{apiKey}")
    @PreAuthorize("hasAuthority('classifiers.edit')")
    @Operation(summary = "Yangi element yaratish", description = "Klasifikatorga yangi element qo'shadi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Element yaratildi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri ma'lumot"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Klasifikator topilmadi"),
            @ApiResponse(responseCode = "409", description = "Bu kodli element allaqachon mavjud")
    })
    public ResponseEntity<ResponseWrapper<ClassifierItemDto>> createClassifierItem(
            @Parameter(description = "Klasifikator API kaliti", example = "gender")
            @PathVariable String apiKey,
            @Valid @RequestBody ClassifierItemCreateDto dto) {
        log.info("POST /api/v1/web/classifiers/{} - code={}", apiKey, dto.getCode());
        try {
            ClassifierItemDto created = classifierWebService.createClassifierItem(apiKey, dto);
            return ResponseEntity.ok(ResponseWrapper.success(created, "Element muvaffaqiyatli yaratildi"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("allaqachon mavjud")) {
                return ResponseEntity.status(409).body(ResponseWrapper.error(e.getMessage()));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @PutMapping("/{apiKey}/{code}")
    @PreAuthorize("hasAuthority('classifiers.edit')")
    @Operation(summary = "Elementni tahrirlash", description = "Mavjud klasifikator elementini yangilaydi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Element yangilandi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri ma'lumot"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Element topilmadi")
    })
    public ResponseEntity<ResponseWrapper<ClassifierItemDto>> updateClassifierItem(
            @Parameter(description = "Klasifikator API kaliti", example = "gender")
            @PathVariable String apiKey,
            @Parameter(description = "Element kodi", example = "11")
            @PathVariable String code,
            @Valid @RequestBody ClassifierItemUpdateDto dto) {
        log.info("PUT /api/v1/web/classifiers/{}/{}", apiKey, code);
        try {
            ClassifierItemDto updated = classifierWebService.updateClassifierItem(apiKey, code, dto);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ResponseWrapper.success(updated, "Element muvaffaqiyatli yangilandi"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ResponseWrapper.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{apiKey}/{code}")
    @PreAuthorize("hasAuthority('classifiers.edit')")
    @Operation(summary = "Elementni o'chirish", description = "Klasifikator elementini o'chiradi (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Element o'chirildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab qilinadi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Element topilmadi")
    })
    public ResponseEntity<ResponseWrapper<Void>> deleteClassifierItem(
            @Parameter(description = "Klasifikator API kaliti", example = "gender")
            @PathVariable String apiKey,
            @Parameter(description = "Element kodi", example = "99")
            @PathVariable String code) {
        log.info("DELETE /api/v1/web/classifiers/{}/{}", apiKey, code);
        try {
            classifierWebService.deleteClassifierItem(apiKey, code);
            return ResponseEntity.ok(ResponseWrapper.success("Element muvaffaqiyatli o'chirildi"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ResponseWrapper.error(e.getMessage()));
        }
    }
}
