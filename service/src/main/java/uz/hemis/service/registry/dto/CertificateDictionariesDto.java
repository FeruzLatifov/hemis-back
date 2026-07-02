package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Certificate Registry Dictionaries DTO - reference data for the Certificates registry filters.
 *
 * <p>Cached ("certificatesDictionaries").</p>
 */
@Schema(name = "CertificateDictionaries", description = "Reference data for certificate filter dropdowns (cached)")
public record CertificateDictionariesDto(

    @Schema(description = "University options {code,name}")
    List<DictionaryItem> universities,

    @Schema(description = "Certificate type options {code,name}")
    List<DictionaryItem> certificateTypes
) {

    @Schema(name = "CertificateDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(

        @Schema(description = "Code/value", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Display label", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
    ) {}
}
