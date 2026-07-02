package uz.hemis.service.registry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Create payload for an attached-speciality (ministry → university classifier attach).
 *
 * <p>{@code specialityLevel} chooses which {@code _speciality_*} column receives
 * {@code specialityId}; the other three columns are NULLed by the service.</p>
 *
 * @since 2.0.0
 */
public record AttachedSpecialityCreateDto(
        @NotBlank(message = "universityCode is required")
        String universityCode,

        @NotBlank(message = "educationType is required")
        String educationType,

        String educationForm,

        @NotNull(message = "specialityLevel is required")
        SpecialityLevel specialityLevel,

        @NotNull(message = "specialityId is required")
        UUID specialityId,

        Boolean active
) {
}
