package uz.hemis.service.document.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uz.hemis.common.dto.document.DiplomaDto;
import uz.hemis.domain.entity.Diploma;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DiplomaMapper} (MapStruct-generated implementation).
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
@DisplayName("DiplomaMapper Unit Tests")
class DiplomaMapperTest {

    private DiplomaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DiplomaMapper.class);
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
            UUID studentId = UUID.randomUUID();
            UUID specialtyId = UUID.randomUUID();
            UUID diplomaBlankId = UUID.randomUUID();
            LocalDate issueDate = LocalDate.of(2025, 6, 15);
            LocalDate registrationDate = LocalDate.of(2025, 6, 20);
            LocalDateTime createTs = LocalDateTime.of(2025, 1, 10, 8, 0);
            LocalDateTime updateTs = LocalDateTime.of(2025, 6, 15, 14, 30);

            Diploma entity = new Diploma();
            entity.setId(id);
            entity.setDiplomaNumber("UZ-2025-TUIT-00001");
            entity.setStudent(studentId);
            entity.setUniversity("TUIT");
            entity.setSpecialty(specialtyId);
            entity.setDiplomaBlank(diplomaBlankId);
            entity.setSerialNumber("AB 1234567");
            entity.setDiplomaType("BACHELOR");
            entity.setIssueDate(issueDate);
            entity.setRegistrationDate(registrationDate);
            entity.setGraduationYear(2025);
            entity.setQualification("Bachelor of Computer Science");
            entity.setAverageGrade(4.5);
            entity.setHonors("HONORS");
            entity.setDiplomaHash("sha256hashvalue");
            entity.setRectorName("Prof. Abdullayev A.A.");
            entity.setStatus("ISSUED");
            entity.setQrCode("qr-code-data-here");
            entity.setVerificationUrl("https://verify.hemis.uz/diploma/abc123");
            entity.setNotes("Cum laude graduate");
            entity.setCreateTs(createTs);
            entity.setCreatedBy("admin");
            entity.setUpdateTs(updateTs);
            entity.setUpdatedBy("registrar");

            // When
            DiplomaDto dto = mapper.toDto(entity);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getDiplomaNumber()).isEqualTo("UZ-2025-TUIT-00001");
            assertThat(dto.getStudent()).isEqualTo(studentId);
            assertThat(dto.getUniversity()).isEqualTo("TUIT");
            assertThat(dto.getSpecialty()).isEqualTo(specialtyId);
            assertThat(dto.getDiplomaBlank()).isEqualTo(diplomaBlankId);
            assertThat(dto.getSerialNumber()).isEqualTo("AB 1234567");
            assertThat(dto.getDiplomaType()).isEqualTo("BACHELOR");
            assertThat(dto.getIssueDate()).isEqualTo(issueDate);
            assertThat(dto.getRegistrationDate()).isEqualTo(registrationDate);
            assertThat(dto.getGraduationYear()).isEqualTo(2025);
            assertThat(dto.getQualification()).isEqualTo("Bachelor of Computer Science");
            assertThat(dto.getAverageGrade()).isEqualTo(4.5);
            assertThat(dto.getHonors()).isEqualTo("HONORS");
            assertThat(dto.getDiplomaHash()).isEqualTo("sha256hashvalue");
            assertThat(dto.getRectorName()).isEqualTo("Prof. Abdullayev A.A.");
            assertThat(dto.getStatus()).isEqualTo("ISSUED");
            assertThat(dto.getQrCode()).isEqualTo("qr-code-data-here");
            assertThat(dto.getVerificationUrl()).isEqualTo("https://verify.hemis.uz/diploma/abc123");
            assertThat(dto.getNotes()).isEqualTo("Cum laude graduate");
            assertThat(dto.getCreateTs()).isEqualTo(createTs);
            assertThat(dto.getCreatedBy()).isEqualTo("admin");
            assertThat(dto.getUpdateTs()).isEqualTo(updateTs);
            assertThat(dto.getUpdatedBy()).isEqualTo("registrar");
        }

        @Test
        @DisplayName("Null entity returns null DTO")
        void nullEntityReturnsNull() {
            DiplomaDto dto = mapper.toDto(null);
            assertThat(dto).isNull();
        }

        @Test
        @DisplayName("Entity with all null fields returns DTO with all null fields")
        void emptyEntityMapsToEmptyDto() {
            Diploma entity = new Diploma();

            DiplomaDto dto = mapper.toDto(entity);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isNull();
            assertThat(dto.getDiplomaNumber()).isNull();
            assertThat(dto.getStudent()).isNull();
            assertThat(dto.getUniversity()).isNull();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getAverageGrade()).isNull();
            assertThat(dto.getGraduationYear()).isNull();
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
            UUID dtoId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            UUID specialtyId = UUID.randomUUID();
            UUID diplomaBlankId = UUID.randomUUID();
            LocalDate issueDate = LocalDate.of(2025, 7, 1);
            LocalDate registrationDate = LocalDate.of(2025, 7, 5);

            DiplomaDto dto = new DiplomaDto();
            dto.setId(dtoId);
            dto.setDiplomaNumber("UZ-2025-NDPI-00010");
            dto.setStudent(studentId);
            dto.setUniversity("NDPI");
            dto.setSpecialty(specialtyId);
            dto.setDiplomaBlank(diplomaBlankId);
            dto.setSerialNumber("CD 9876543");
            dto.setDiplomaType("MASTER");
            dto.setIssueDate(issueDate);
            dto.setRegistrationDate(registrationDate);
            dto.setGraduationYear(2025);
            dto.setQualification("Master of Information Technology");
            dto.setAverageGrade(4.8);
            dto.setHonors("HIGH_HONORS");
            dto.setDiplomaHash("anotherhashvalue");
            dto.setRectorName("Prof. Karimov K.K.");
            dto.setStatus("REGISTERED");
            dto.setQrCode("qr-code-master");
            dto.setVerificationUrl("https://verify.hemis.uz/diploma/xyz789");
            dto.setNotes("Magna cum laude");
            // Set audit fields on DTO (should be ignored during mapping)
            dto.setCreateTs(LocalDateTime.of(2024, 1, 1, 0, 0));
            dto.setCreatedBy("should-be-ignored");
            dto.setUpdateTs(LocalDateTime.of(2024, 6, 1, 0, 0));
            dto.setUpdatedBy("also-ignored");

            // When
            Diploma entity = mapper.toEntity(dto);

            // Then - business fields are mapped
            assertThat(entity).isNotNull();
            assertThat(entity.getDiplomaNumber()).isEqualTo("UZ-2025-NDPI-00010");
            assertThat(entity.getStudent()).isEqualTo(studentId);
            assertThat(entity.getUniversity()).isEqualTo("NDPI");
            assertThat(entity.getSpecialty()).isEqualTo(specialtyId);
            assertThat(entity.getDiplomaBlank()).isEqualTo(diplomaBlankId);
            assertThat(entity.getSerialNumber()).isEqualTo("CD 9876543");
            assertThat(entity.getDiplomaType()).isEqualTo("MASTER");
            assertThat(entity.getIssueDate()).isEqualTo(issueDate);
            assertThat(entity.getRegistrationDate()).isEqualTo(registrationDate);
            assertThat(entity.getGraduationYear()).isEqualTo(2025);
            assertThat(entity.getQualification()).isEqualTo("Master of Information Technology");
            assertThat(entity.getAverageGrade()).isEqualTo(4.8);
            assertThat(entity.getHonors()).isEqualTo("HIGH_HONORS");
            assertThat(entity.getDiplomaHash()).isEqualTo("anotherhashvalue");
            assertThat(entity.getRectorName()).isEqualTo("Prof. Karimov K.K.");
            assertThat(entity.getStatus()).isEqualTo("REGISTERED");
            assertThat(entity.getQrCode()).isEqualTo("qr-code-master");
            assertThat(entity.getVerificationUrl()).isEqualTo("https://verify.hemis.uz/diploma/xyz789");
            assertThat(entity.getNotes()).isEqualTo("Magna cum laude");

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
            Diploma entity = mapper.toEntity(null);
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("DTO with only partial fields maps those fields, rest are null")
        void partialDtoMapsPartialFields() {
            DiplomaDto dto = new DiplomaDto();
            dto.setDiplomaNumber("UZ-2025-TATU-00050");
            dto.setStatus("DRAFT");

            Diploma entity = mapper.toEntity(dto);

            assertThat(entity).isNotNull();
            assertThat(entity.getDiplomaNumber()).isEqualTo("UZ-2025-TATU-00050");
            assertThat(entity.getStatus()).isEqualTo("DRAFT");
            assertThat(entity.getStudent()).isNull();
            assertThat(entity.getUniversity()).isNull();
            assertThat(entity.getAverageGrade()).isNull();
            assertThat(entity.getIssueDate()).isNull();
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
            LocalDateTime originalCreateTs = LocalDateTime.of(2025, 1, 1, 10, 0);
            String originalCreatedBy = "original-admin";
            LocalDateTime originalUpdateTs = LocalDateTime.of(2025, 3, 15, 12, 0);
            String originalUpdatedBy = "original-updater";

            Diploma entity = new Diploma();
            entity.setId(entityId);
            entity.setDiplomaNumber("UZ-2025-TUIT-00001");
            entity.setStatus("DRAFT");
            entity.setUniversity("TUIT");
            entity.setQualification("Bachelor of Science");
            entity.setAverageGrade(3.5);
            entity.setCreateTs(originalCreateTs);
            entity.setCreatedBy(originalCreatedBy);
            entity.setUpdateTs(originalUpdateTs);
            entity.setUpdatedBy(originalUpdatedBy);

            // Given - DTO with updated values (including audit fields that should be ignored)
            DiplomaDto dto = new DiplomaDto();
            dto.setId(UUID.randomUUID()); // different ID - should be ignored
            dto.setDiplomaNumber("UZ-2025-TUIT-00001-UPDATED");
            dto.setStatus("ISSUED");
            dto.setQualification("Bachelor of Computer Science");
            dto.setAverageGrade(4.2);
            dto.setHonors("HONORS");
            dto.setRectorName("Prof. Toshmatov T.T.");
            dto.setCreateTs(LocalDateTime.of(2020, 1, 1, 0, 0)); // should be ignored
            dto.setCreatedBy("hacker"); // should be ignored
            dto.setUpdateTs(LocalDateTime.of(2020, 6, 1, 0, 0)); // should be ignored
            dto.setUpdatedBy("hacker"); // should be ignored

            // When
            mapper.updateEntityFromDto(dto, entity);

            // Then - business fields are updated
            assertThat(entity.getDiplomaNumber()).isEqualTo("UZ-2025-TUIT-00001-UPDATED");
            assertThat(entity.getStatus()).isEqualTo("ISSUED");
            assertThat(entity.getQualification()).isEqualTo("Bachelor of Computer Science");
            assertThat(entity.getAverageGrade()).isEqualTo(4.2);
            assertThat(entity.getHonors()).isEqualTo("HONORS");
            assertThat(entity.getRectorName()).isEqualTo("Prof. Toshmatov T.T.");

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
            // Given - entity with all fields populated
            UUID entityId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();

            Diploma entity = new Diploma();
            entity.setId(entityId);
            entity.setDiplomaNumber("UZ-2025-TUIT-00001");
            entity.setStudent(studentId);
            entity.setUniversity("TUIT");
            entity.setStatus("ISSUED");
            entity.setAverageGrade(4.0);
            entity.setQualification("Bachelor of Science");

            // Given - DTO with only some fields set (rest are null)
            DiplomaDto dto = new DiplomaDto();
            dto.setStatus("REGISTERED"); // only update status

            // When
            mapper.updateEntityFromDto(dto, entity);

            // Then - non-null DTO field is updated
            assertThat(entity.getStatus()).isEqualTo("REGISTERED");

            // Then - null DTO fields do NOT overwrite existing values
            // (NullValuePropertyMappingStrategy.IGNORE is configured)
            assertThat(entity.getDiplomaNumber()).isEqualTo("UZ-2025-TUIT-00001");
            assertThat(entity.getStudent()).isEqualTo(studentId);
            assertThat(entity.getUniversity()).isEqualTo("TUIT");
            assertThat(entity.getAverageGrade()).isEqualTo(4.0);
            assertThat(entity.getQualification()).isEqualTo("Bachelor of Science");
        }

        @Test
        @DisplayName("Null DTO does not modify entity")
        void nullDtoDoesNotModifyEntity() {
            UUID entityId = UUID.randomUUID();
            Diploma entity = new Diploma();
            entity.setId(entityId);
            entity.setDiplomaNumber("UZ-2025-TUIT-00001");
            entity.setStatus("DRAFT");

            mapper.updateEntityFromDto(null, entity);

            assertThat(entity.getId()).isEqualTo(entityId);
            assertThat(entity.getDiplomaNumber()).isEqualTo("UZ-2025-TUIT-00001");
            assertThat(entity.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("University field is preserved when DTO university is null")
        void universityPreservedWhenDtoUniversityNull() {
            Diploma entity = new Diploma();
            entity.setId(UUID.randomUUID());
            entity.setUniversity("TUIT");

            DiplomaDto dto = new DiplomaDto();
            dto.setUniversity(null); // null should not overwrite

            mapper.updateEntityFromDto(dto, entity);

            assertThat(entity.getUniversity()).isEqualTo("TUIT");
        }
    }
}
