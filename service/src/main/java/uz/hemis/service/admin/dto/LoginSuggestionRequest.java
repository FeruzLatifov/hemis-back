package uz.hemis.service.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Login taklifi so'rovi — PERSON akkaunt yaratish formasi uchun.
 *
 * <p><strong>Nega GET emas, POST:</strong> ism va familiya — shaxsiy ma'lumot. GET bo'lsa ular
 * URL query-string'ga tushardi, nginx / reverse-proxy esa uni access log'ga aynan yozadi.
 * Body orqali yuborish ularni log'dan tashqarida qoldiradi (person-lookup endpoint bilan bir xil
 * mulohaza). So'rov faqat o'qiydi — DB'ga hech narsa yozmaydi.</p>
 */
@Schema(description = "First/last name to build a login suggestion from")
public record LoginSuggestionRequest(

        @Size(max = 255, message = "First name must be at most 255 characters")
        @Schema(description = "First name, as typed or as autofilled from the GUVD gateway", nullable = true)
        String firstName,

        @Size(max = 255, message = "Last name must be at most 255 characters")
        @Schema(description = "Last name, as typed or as autofilled from the GUVD gateway", nullable = true)
        String lastName
) {
}
