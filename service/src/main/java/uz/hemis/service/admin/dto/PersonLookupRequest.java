package uz.hemis.service.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Person-lookup request body (GUVD/api_mspd autofill).
 *
 * <p><strong>Why a POST body and not GET query-params:</strong> PINFL (national ID) and passport
 * are PII. In a GET they would land in the URL query-string, which nginx / reverse-proxies write
 * verbatim into access logs. Carrying them in the request body keeps them out of access logs.</p>
 */
public record PersonLookupRequest(
        @NotBlank(message = "pinfl is required")
        @Pattern(regexp = "\\d{14}", message = "PINFL must be 14 digits")
        String pinfl,

        /** Passport series+number, e.g. AB1234567 (preferred over birthDate). */
        String document,

        /** Birth date {@code yyyy-MM-dd}, used when document is absent. */
        String birthDate
) {
}
