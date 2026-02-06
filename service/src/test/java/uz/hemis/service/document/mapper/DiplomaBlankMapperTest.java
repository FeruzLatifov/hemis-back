package uz.hemis.service.document.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uz.hemis.common.dto.document.DiplomaBlankDto;
import uz.hemis.domain.entity.DiplomaBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DiplomaBlankMapper} (MapStruct-generated implementation).
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>toDto - all entity fields mapped correctly to DTO</li>
 *   <li>toEntity - DTO fields mapped correctly, audit fields (id, createTs, createdBy, updateTs, updatedBy, deleteTs, deletedBy) are ignored</li>
 *   <li>updateEntityFromDto - updates existing entity fields from DTO, preserves entity ID and audit fields</li>
 *   <li>Null input handling for all three methods</li>
 * </ul>
 *
 * @since 1.0.0
 */
@DisplayName("DiplomaBlankMapper Unit Tests")
class DiplomaBlankMapperTest {

    private DiplomaBlankMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DiplomaBlankMapper.class);
    }

    // =====================================================
    // toDto tests
    // =====================================================

    @Nested
    @DisplayName("toDto")
    class ToDtoTests {

        @Test
        @DisplayName("All entity fields are mapped correctly to DTO")
        void allFieldsMappedCorrectly() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDate receivedDate = LocalDate.of(2025, 3, 1);
            LocalDate issuedDate = LocalDate.of(2025, 6, 15);
            LocalDateTime createTs = LocalDateTime.of(2025, 2, 20, 9, 0);
            LocalDateTime updateTs = LocalDateTime.of(2025, 6, 15, 16, 0);

            DiplomaBlank entity = new DiplomaBlank();
            entity.setId(id);
            entity.setBlankCode("AB 1234567");
            entity.setSeries("AB");
            entity.setNumber("1234567");
            entity.setUniversity("TUIT");
            entity.setBlankType("BACHELOR");
            entity.setStatus("ISSUED");
            entity.setReceivedDate(receivedDate);
            entity.setIssuedDate(issuedDate);
            entity.setAcademicYear(2025);
            entity.setSupplier("Davlat matbaa markazi");
            entity.setBatchNumber("BATCH-2025-001");
            entity.setStatusReason(null);
            entity.setSecurityFeatures("Watermark, hologram, UV ink");
            entity.setNotes("Standard issue blank");
            entity.setCreateTs(createTs);
            entity.setCreatedBy("admin");
            entity.setUpdateTs(updateTs);
            entity.setUpdatedBy("registrar");

            // When
            DiplomaBlankDto dto = mapper.toDto(entity);

            // Then
            assertThat(dto).isNotNull();
            // Note: BaseEntity.id is UUID, but DiplomaBlankDto.id is String for legacy compatibility
            assertThat(dto.getId()).isEqualTo(id.toString());
            assertThat(dto.getBlankCode()).isEqualTo("AB 1234567");
            assertThat(dto.getSeries()).isEqualTo("AB");
            assertThat(dto.getNumber()).isEqualTo("1234567");
            assertThat(dto.getUniversity()).isEqualTo("TUIT");
            assertThat(dto.getBlankType()).isEqualTo("BACHELOR");
            assertThat(dto.getStatus()).isEqualTo("ISSUED");
            assertThat(dto.getReceivedDate()).isEqualTo(receivedDate);
            assertThat(dto.getIssuedDate()).isEqualTo(issuedDate);
            assertThat(dto.getAcademicYear()).isEqualTo(2025);
            assertThat(dto.getSupplier()).isEqualTo("Davlat matbaa markazi");
            assertThat(dto.getBatchNumber()).isEqualTo("BATCH-2025-001");
            assertThat(dto.getStatusReason()).isNull();
            assertThat(dto.getSecurityFeatures()).isEqualTo("Watermark, hologram, UV ink");
            assertThat(dto.getNotes()).isEqualTo("Standard issue blank");
            assertThat(dto.getCreateTs()).isEqualTo(createTs);
            assertThat(dto.getCreatedBy()).isEqualTo("admin");
            assertThat(dto.getUpdateTs()).isEqualTo(updateTs);
            assertThat(dto.getUpdatedBy()).isEqualTo("registrar");
        }

        @Test
        @DisplayName("Null entity returns null DTO")
        void nullEntityReturnsNull() {
            DiplomaBlankDto dto = mapper.toDto(null);
            assertThat(dto).isNull();
        }

        @Test
        @DisplayName("Entity with all null fields returns DTO with all null fields")
        void emptyEntityMapsToEmptyDto() {
            DiplomaBlank entity = new DiplomaBlank();

            DiplomaBlankDto dto = mapper.toDto(entity);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isNull();
            assertThat(dto.getBlankCode()).isNull();
            assertThat(dto.getSeries()).isNull();
            assertThat(dto.getNumber()).isNull();
            assertThat(dto.getUniversity()).isNull();
            assertThat(dto.getBlankType()).isNull();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getReceivedDate()).isNull();
            assertThat(dto.getIssuedDate()).isNull();
            assertThat(dto.getAcademicYear()).isNull();
        }
    }

    // =====================================================
    // toEntity tests
    // =====================================================

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("DTO fields are mapped correctly, audit fields are ignored")
        void dtoFieldsMappedAndAuditFieldsIgnored() {
            // Given
            DiplomaBlankDto dto = new DiplomaBlankDto();
            dto.setId(UUID.randomUUID().toString());
            dto.setBlankCode("CD 9876543");
            dto.setSeries("CD");
            dto.setNumber("9876543");
            dto.setUniversity("NDPI");
            dto.setBlankType("MASTER");
            dto.setStatus("AVAILABLE");
            dto.setReceivedDate(LocalDate.of(2025, 4, 10));
            dto.setIssuedDate(null);
            dto.setAcademicYear(2025);
            dto.setSupplier("O'zbekiston Respublikasi Vazirlar Mahkamasi");
            dto.setBatchNumber("BATCH-2025-002");
            dto.setStatusReason(null);
            dto.setSecurityFeatures("Holographic strip, microtext");
            dto.setNotes("Reserved for Master graduates");
            // Set audit fields on DTO (should be ignored during mapping)
            dto.setCreateTs(LocalDateTime.of(2024, 1, 1, 0, 0));
            dto.setCreatedBy("should-be-ignored");
            dto.setUpdateTs(LocalDateTime.of(2024, 6, 1, 0, 0));
            dto.setUpdatedBy("also-ignored");

            // When
            DiplomaBlank entity = mapper.toEntity(dto);

            // Then - business fields are mapped
            assertThat(entity).isNotNull();
            assertThat(entity.getBlankCode()).isEqualTo("CD 9876543");
            assertThat(entity.getSeries()).isEqualTo("CD");
            assertThat(entity.getNumber()).isEqualTo("9876543");
            assertThat(entity.getUniversity()).isEqualTo("NDPI");
            assertThat(entity.getBlankType()).isEqualTo("MASTER");
            assertThat(entity.getStatus()).isEqualTo("AVAILABLE");
            assertThat(entity.getReceivedDate()).isEqualTo(LocalDate.of(2025, 4, 10));
            assertThat(entity.getIssuedDate()).isNull();
            assertThat(entity.getAcademicYear()).isEqualTo(2025);
            assertThat(entity.getSupplier()).isEqualTo("O'zbekiston Respublikasi Vazirlar Mahkamasi");
            assertThat(entity.getBatchNumber()).isEqualTo("BATCH-2025-002");
            assertThat(entity.getSecurityFeatures()).isEqualTo("Holographic strip, microtext");
            assertThat(entity.getNotes()).isEqualTo("Reserved for Master graduates");

            // Then - audit fields are ignored (id, createTs, createdBy, updateTs, updatedBy, deleteTs, deletedBy)
            assertThat(entity.getId()).isNull();
            assertThat(entity.getCreateTs()).isNull();
            assertThat(entity.getCreatedBy()).isNull();
            assertThat(entity.getUpdateTs()).isNull();
            assertThat(entity.getUpdatedBy()).isNull();
            assertThat(entity.getDeleteTs()).isNull();
            assertThat(entity.getDeletedBy()).isNull();
        }

        @Test
        @DisplayName("Null DTO returns null entity")
        void nullDtoReturnsNull() {
            DiplomaBlank entity = mapper.toEntity(null);
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("DTO with only partial fields maps those fields, rest are null")
        void partialDtoMapsPartialFields() {
            DiplomaBlankDto dto = new DiplomaBlankDto();
            dto.setBlankCode("EF 1111111");
            dto.setStatus("AVAILABLE");
            dto.setBlankType("CERTIFICATE");

            DiplomaBlank entity = mapper.toEntity(dto);

            assertThat(entity).isNotNull();
            assertThat(entity.getBlankCode()).isEqualTo("EF 1111111");
            assertThat(entity.getStatus()).isEqualTo("AVAILABLE");
            assertThat(entity.getBlankType()).isEqualTo("CERTIFICATE");
            assertThat(entity.getSeries()).isNull();
            assertThat(entity.getNumber()).isNull();
            assertThat(entity.getUniversity()).isNull();
            assertThat(entity.getReceivedDate()).isNull();
            assertThat(entity.getAcademicYear()).isNull();
        }
    }

    // =====================================================
    // updateEntityFromDto tests
    // =====================================================

    @Nested
    @DisplayName("updateEntityFromDto")
    class UpdateEntityFromDtoTests {

        @Test
        @DisplayName("Updates entity fields from DTO while preserving entity ID and audit fields")
        void updatesFieldsPreservesIdAndAuditFields() {
            // Given - existing entity with ID and audit fields
            UUID entityId = UUID.randomUUID();
            LocalDateTime originalCreateTs = LocalDateTime.of(2025, 2, 1, 8, 0);
            String originalCreatedBy = "supply-admin";
            LocalDateTime originalUpdateTs = LocalDateTime.of(2025, 5, 10, 14, 0);
            String originalUpdatedBy = "registrar";

            DiplomaBlank entity = new DiplomaBlank();
            entity.setId(entityId);
            entity.setBlankCode("AB 1234567");
            entity.setSeries("AB");
            entity.setNumber("1234567");
            entity.setUniversity("TUIT");
            entity.setBlankType("BACHELOR");
            entity.setStatus("AVAILABLE");
            entity.setReceivedDate(LocalDate.of(2025, 3, 1));
            entity.setAcademicYear(2025);
            entity.setCreateTs(originalCreateTs);
            entity.setCreatedBy(originalCreatedBy);
            entity.setUpdateTs(originalUpdateTs);
            entity.setUpdatedBy(originalUpdatedBy);

            // Given - DTO with updated values
            DiplomaBlankDto dto = new DiplomaBlankDto();
            dto.setId(UUID.randomUUID().toString()); // different ID - should be ignored
            dto.setStatus("ASSIGNED");
            dto.setIssuedDate(LocalDate.of(2025, 6, 20));
            dto.setStatusReason("Assigned to diploma UZ-2025-TUIT-00001");
            dto.setCreateTs(LocalDateTime.of(2020, 1, 1, 0, 0)); // should be ignored
            dto.setCreatedBy("hacker"); // should be ignored

            // When
            mapper.updateEntityFromDto(dto, entity);

            // Then - updated fields
            assertThat(entity.getStatus()).isEqualTo("ASSIGNED");
            assertThat(entity.getIssuedDate()).isEqualTo(LocalDate.of(2025, 6, 20));
            assertThat(entity.getStatusReason()).isEqualTo("Assigned to diploma UZ-2025-TUIT-00001");

            // Then - ID is preserved (ignored from DTO)
            assertThat(entity.getId()).isEqualTo(entityId);

            // Then - audit fields are preserved (ignored from DTO)
            assertThat(entity.getCreateTs()).isEqualTo(originalCreateTs);
            assertThat(entity.getCreatedBy()).isEqualTo(originalCreatedBy);
            assertThat(entity.getUpdateTs()).isEqualTo(originalUpdateTs);
            assertThat(entity.getUpdatedBy()).isEqualTo(originalUpdatedBy);
            assertThat(entity.getDeleteTs()).isNull();
            assertThat(entity.getDeletedBy()).isNull();
        }

        @Test
        @DisplayName("Null DTO fields do not overwrite existing entity fields (IGNORE strategy)")
        void nullDtoFieldsDoNotOverwriteExistingValues() {
            // Given - entity with fields populated
            UUID entityId = UUID.randomUUID();

            DiplomaBlank entity = new DiplomaBlank();
            entity.setId(entityId);
            entity.setBlankCode("AB 1234567");
            entity.setSeries("AB");
            entity.setNumber("1234567");
            entity.setUniversity("TUIT");
            entity.setBlankType("BACHELOR");
            entity.setStatus("AVAILABLE");
            entity.setSupplier("Davlat matbaa markazi");
            entity.setAcademicYear(2025);

            // Given - DTO with only status set (rest are null)
            DiplomaBlankDto dto = new DiplomaBlankDto();
            dto.setStatus("DAMAGED");
            dto.setStatusReason("Printing defect detected");

            // When
            mapper.updateEntityFromDto(dto, entity);

            // Then - non-null DTO fields are updated
            assertThat(entity.getStatus()).isEqualTo("DAMAGED");
            assertThat(entity.getStatusReason()).isEqualTo("Printing defect detected");

            // Then - null DTO fields do NOT overwrite existing values
            // (NullValuePropertyMappingStrategy.IGNORE is configured)
            assertThat(entity.getBlankCode()).isEqualTo("AB 1234567");
            assertThat(entity.getSeries()).isEqualTo("AB");
            assertThat(entity.getNumber()).isEqualTo("1234567");
            assertThat(entity.getUniversity()).isEqualTo("TUIT");
            assertThat(entity.getBlankType()).isEqualTo("BACHELOR");
            assertThat(entity.getSupplier()).isEqualTo("Davlat matbaa markazi");
            assertThat(entity.getAcademicYear()).isEqualTo(2025);
        }

        @Test
        @DisplayName("Null DTO does not modify entity")
        void nullDtoDoesNotModifyEntity() {
            UUID entityId = UUID.randomUUID();
            DiplomaBlank entity = new DiplomaBlank();
            entity.setId(entityId);
            entity.setBlankCode("GH 5555555");
            entity.setStatus("AVAILABLE");

            mapper.updateEntityFromDto(null, entity);

            assertThat(entity.getId()).isEqualTo(entityId);
            assertThat(entity.getBlankCode()).isEqualTo("GH 5555555");
            assertThat(entity.getStatus()).isEqualTo("AVAILABLE");
        }

        @Test
        @DisplayName("University field is preserved when DTO university is null")
        void universityPreservedWhenDtoUniversityNull() {
            DiplomaBlank entity = new DiplomaBlank();
            entity.setId(UUID.randomUUID());
            entity.setUniversity("TUIT");
            entity.setBlankCode("AB 1234567");

            DiplomaBlankDto dto = new DiplomaBlankDto();
            dto.setUniversity(null); // null should not overwrite

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getUniversity()).isEqualTo("TUIT");
            assertThat(entity.getBlankCode()).isEqualTo("AB 1234567");
        }

        @Test
        @DisplayName("Full update replaces all business fields")
        void fullUpdateReplacesAllBusinessFields() {
            UUID entityId = UUID.randomUUID();
            DiplomaBlank entity = new DiplomaBlank();
            entity.setId(entityId);
            entity.setBlankCode("AB 1234567");
            entity.setSeries("AB");
            entity.setNumber("1234567");
            entity.setUniversity("TUIT");
            entity.setBlankType("BACHELOR");
            entity.setStatus("AVAILABLE");

            DiplomaBlankDto dto = new DiplomaBlankDto();
            dto.setBlankCode("XY 9999999");
            dto.setSeries("XY");
            dto.setNumber("9999999");
            dto.setUniversity("NDPI");
            dto.setBlankType("MASTER");
            dto.setStatus("LOST");
            dto.setStatusReason("Misplaced during transport");
            dto.setSecurityFeatures("UV reactive ink, microchip");
            dto.setNotes("Report filed to ministry");

            mapper.updateEntityFromDto(dto, entity);

            // ID preserved
            assertThat(entity.getId()).isEqualTo(entityId);
            // All business fields updated
            assertThat(entity.getBlankCode()).isEqualTo("XY 9999999");
            assertThat(entity.getSeries()).isEqualTo("XY");
            assertThat(entity.getNumber()).isEqualTo("9999999");
            assertThat(entity.getUniversity()).isEqualTo("NDPI");
            assertThat(entity.getBlankType()).isEqualTo("MASTER");
            assertThat(entity.getStatus()).isEqualTo("LOST");
            assertThat(entity.getStatusReason()).isEqualTo("Misplaced during transport");
            assertThat(entity.getSecurityFeatures()).isEqualTo("UV reactive ink, microchip");
            assertThat(entity.getNotes()).isEqualTo("Report filed to ministry");
        }
    }
}
