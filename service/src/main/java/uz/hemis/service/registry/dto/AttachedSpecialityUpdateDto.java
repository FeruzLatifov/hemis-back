package uz.hemis.service.registry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Update payload for an attached-speciality. Same shape as
 * {@link AttachedSpecialityCreateDto}; the {@code id} comes from the path.
 *
 * @since 2.0.0
 */
public record AttachedSpecialityUpdateDto(
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
