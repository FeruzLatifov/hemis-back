package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uz.hemis.domain.entity.finance.DiplomaBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DiplomaBlank Entity Unit Tests
 *
 * <p>Tests Lombok-generated getters/setters, constructor variants,
 * and inherited soft delete / audit logic from BaseEntity.</p>
 *
 * <p>DiplomaBlank extends BaseEntity (UUID primary key, CUBA audit columns)
 * and maps to table {@code hemishe_e_diploma_blank}.</p>
 *
 * <p>Covers key fields:</p>
 * <ul>
 *   <li>blankCode, series, number - blank identification</li>
 *   <li>status - workflow state (AVAILABLE, ASSIGNED, ISSUED, etc.)</li>
 *   <li>university - allocated university code</li>
 *   <li>academicYear - allocation year</li>
 *   <li>blankType - diploma level (BACHELOR, MASTER, etc.)</li>
 *   <li>receivedDate, issuedDate - date tracking</li>
 *   <li>supplier, batchNumber - production tracking</li>
 *   <li>statusReason, securityFeatures, notes - supplementary fields</li>
 * </ul>
 */
@DisplayName("DiplomaBlank Entity Tests")
class DiplomaBlankEntityTest {

    // ==================================================================
    // Default state
    // ==================================================================

    @Nested
    @DisplayName("default state")
    class DefaultState {

        @Test
        @DisplayName("Should create instance with no-arg constructor")
        void shouldCreateInstance() {
            DiplomaBlank blank = new DiplomaBlank();

            assertThat(blank).isNotNull();
            assertThat(blank.getId()).isNull();
            assertThat(blank.getBlankCode()).isNull();
            assertThat(blank.getSeries()).isNull();
            assertThat(blank.getNumber()).isNull();
            assertThat(blank.getStatus()).isNull();
            assertThat(blank.getUniversity()).isNull();
            assertThat(blank.getBlankType()).isNull();
            assertThat(blank.getAcademicYear()).isNull();
        }
    }

    // ==================================================================
    // Key identification fields
    // ==================================================================

    @Nested
    @DisplayName("blank identification fields")
    class BlankIdentification {

        @Test
        @DisplayName("Should set and get blankCode")
        void shouldSetAndGetBlankCode() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBlankCode("AB 1234567");

            assertThat(blank.getBlankCode()).isEqualTo("AB 1234567");
        }

        @Test
        @DisplayName("Should set and get series")
        void shouldSetAndGetSeries() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setSeries("CD");

            assertThat(blank.getSeries()).isEqualTo("CD");
        }

        @Test
        @DisplayName("Should set and get number")
        void shouldSetAndGetNumber() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setNumber("7654321");

            assertThat(blank.getNumber()).isEqualTo("7654321");
        }

        @Test
        @DisplayName("BlankCode should be combination of series and number conceptually")
        void blankCodeShouldCombineSeriesAndNumber() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setSeries("EF");
            blank.setNumber("9876543");
            blank.setBlankCode("EF 9876543");

            assertThat(blank.getBlankCode()).startsWith(blank.getSeries());
            assertThat(blank.getBlankCode()).endsWith(blank.getNumber());
        }
    }

    // ==================================================================
    // Status (workflow)
    // ==================================================================

    @Nested
    @DisplayName("status field (workflow)")
    class StatusField {

        @Test
        @DisplayName("Should set status to AVAILABLE")
        void shouldSetStatusAvailable() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatus("AVAILABLE");

            assertThat(blank.getStatus()).isEqualTo("AVAILABLE");
        }

        @Test
        @DisplayName("Should set status to ASSIGNED")
        void shouldSetStatusAssigned() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatus("ASSIGNED");

            assertThat(blank.getStatus()).isEqualTo("ASSIGNED");
        }

        @Test
        @DisplayName("Should set status to ISSUED")
        void shouldSetStatusIssued() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatus("ISSUED");

            assertThat(blank.getStatus()).isEqualTo("ISSUED");
        }

        @Test
        @DisplayName("Should set status to DAMAGED with reason")
        void shouldSetStatusDamagedWithReason() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatus("DAMAGED");
            blank.setStatusReason("Printing defect on security hologram");

            assertThat(blank.getStatus()).isEqualTo("DAMAGED");
            assertThat(blank.getStatusReason()).isEqualTo("Printing defect on security hologram");
        }

        @Test
        @DisplayName("Should set status to LOST with reason")
        void shouldSetStatusLostWithReason() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatus("LOST");
            blank.setStatusReason("Missing from storage during inventory check");

            assertThat(blank.getStatus()).isEqualTo("LOST");
            assertThat(blank.getStatusReason()).isNotEmpty();
        }

        @Test
        @DisplayName("Should set status to ANNULLED")
        void shouldSetStatusAnnulled() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatus("ANNULLED");

            assertThat(blank.getStatus()).isEqualTo("ANNULLED");
        }
    }

    // ==================================================================
    // University and academic year
    // ==================================================================

    @Nested
    @DisplayName("university and academic year")
    class UniversityAndYear {

        @Test
        @DisplayName("Should set and get university code")
        void shouldSetAndGetUniversity() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setUniversity("TATU");

            assertThat(blank.getUniversity()).isEqualTo("TATU");
        }

        @Test
        @DisplayName("Should set and get academic year")
        void shouldSetAndGetAcademicYear() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setAcademicYear(2024);

            assertThat(blank.getAcademicYear()).isEqualTo(2024);
        }
    }

    // ==================================================================
    // Blank type
    // ==================================================================

    @Nested
    @DisplayName("blank type")
    class BlankType {

        @Test
        @DisplayName("Should set blankType to BACHELOR")
        void shouldSetBlankTypeBachelor() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBlankType("BACHELOR");

            assertThat(blank.getBlankType()).isEqualTo("BACHELOR");
        }

        @Test
        @DisplayName("Should set blankType to MASTER")
        void shouldSetBlankTypeMaster() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBlankType("MASTER");

            assertThat(blank.getBlankType()).isEqualTo("MASTER");
        }

        @Test
        @DisplayName("Should set blankType to DOCTORATE")
        void shouldSetBlankTypeDoctorate() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBlankType("DOCTORATE");

            assertThat(blank.getBlankType()).isEqualTo("DOCTORATE");
        }

        @Test
        @DisplayName("Should set blankType to CERTIFICATE")
        void shouldSetBlankTypeCertificate() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBlankType("CERTIFICATE");

            assertThat(blank.getBlankType()).isEqualTo("CERTIFICATE");
        }
    }

    // ==================================================================
    // Date fields
    // ==================================================================

    @Nested
    @DisplayName("date fields")
    class DateFields {

        @Test
        @DisplayName("Should set and get receivedDate")
        void shouldSetAndGetReceivedDate() {
            DiplomaBlank blank = new DiplomaBlank();
            LocalDate received = LocalDate.of(2024, 9, 1);
            blank.setReceivedDate(received);

            assertThat(blank.getReceivedDate()).isEqualTo(received);
        }

        @Test
        @DisplayName("Should set and get issuedDate")
        void shouldSetAndGetIssuedDate() {
            DiplomaBlank blank = new DiplomaBlank();
            LocalDate issued = LocalDate.of(2024, 6, 25);
            blank.setIssuedDate(issued);

            assertThat(blank.getIssuedDate()).isEqualTo(issued);
        }

        @Test
        @DisplayName("issuedDate should be after or equal to receivedDate in typical workflow")
        void issuedDateShouldBeAfterReceivedDate() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setReceivedDate(LocalDate.of(2024, 3, 1));
            blank.setIssuedDate(LocalDate.of(2024, 6, 25));

            assertThat(blank.getIssuedDate()).isAfterOrEqualTo(blank.getReceivedDate());
        }
    }

    // ==================================================================
    // Production tracking fields
    // ==================================================================

    @Nested
    @DisplayName("production tracking fields")
    class ProductionTracking {

        @Test
        @DisplayName("Should set and get supplier")
        void shouldSetAndGetSupplier() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setSupplier("Davlat bosma markazi");

            assertThat(blank.getSupplier()).isEqualTo("Davlat bosma markazi");
        }

        @Test
        @DisplayName("Should set and get batchNumber")
        void shouldSetAndGetBatchNumber() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBatchNumber("BATCH-2024-001");

            assertThat(blank.getBatchNumber()).isEqualTo("BATCH-2024-001");
        }
    }

    // ==================================================================
    // Supplementary fields
    // ==================================================================

    @Nested
    @DisplayName("supplementary fields")
    class SupplementaryFields {

        @Test
        @DisplayName("Should set and get securityFeatures")
        void shouldSetAndGetSecurityFeatures() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setSecurityFeatures("Watermark, hologram, UV ink, microtext");

            assertThat(blank.getSecurityFeatures()).isEqualTo("Watermark, hologram, UV ink, microtext");
        }

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setNotes("Reserved for graduation ceremony on June 25, 2024");

            assertThat(blank.getNotes()).isEqualTo("Reserved for graduation ceremony on June 25, 2024");
        }

        @Test
        @DisplayName("Should set and get statusReason")
        void shouldSetAndGetStatusReason() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setStatusReason("Misprinted student name");

            assertThat(blank.getStatusReason()).isEqualTo("Misprinted student name");
        }
    }

    // ==================================================================
    // Inherited BaseEntity fields
    // ==================================================================

    @Nested
    @DisplayName("inherited BaseEntity fields")
    class InheritedFields {

        @Test
        @DisplayName("Should inherit UUID id from BaseEntity")
        void shouldInheritId() {
            DiplomaBlank blank = new DiplomaBlank();
            UUID id = UUID.randomUUID();
            blank.setId(id);

            assertThat(blank.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should inherit version from BaseEntity")
        void shouldInheritVersion() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setVersion(3);

            assertThat(blank.getVersion()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should inherit createdBy from BaseEntity")
        void shouldInheritCreatedBy() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setCreatedBy("registrar");

            assertThat(blank.getCreatedBy()).isEqualTo("registrar");
        }

        @Test
        @DisplayName("isDeleted should return true when deleteTs is set (inherited from BaseEntity)")
        void isDeletedShouldWorkFromBaseEntity() {
            DiplomaBlank blank = new DiplomaBlank();

            assertThat(blank.isDeleted()).isFalse();

            blank.setDeleteTs(LocalDateTime.now());
            assertThat(blank.isDeleted()).isTrue();

            blank.setDeleteTs(null);
            assertThat(blank.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should set and get soft delete audit fields")
        void shouldSetAndGetSoftDeleteAuditFields() {
            DiplomaBlank blank = new DiplomaBlank();
            LocalDateTime ts = LocalDateTime.of(2025, 3, 10, 14, 0);
            blank.setDeleteTs(ts);
            blank.setDeletedBy("admin");

            assertThat(blank.getDeleteTs()).isEqualTo(ts);
            assertThat(blank.getDeletedBy()).isEqualTo("admin");
        }
    }

    // ==================================================================
    // All-args constructor
    // ==================================================================

    @Nested
    @DisplayName("all-args constructor")
    class AllArgsConstructor {

        @Test
        @DisplayName("Should create instance with all-args constructor")
        void shouldCreateInstanceWithAllArgs() {
            DiplomaBlank blank = new DiplomaBlank(
                    "AB 1234567",   // blankCode
                    "AB",           // series
                    "1234567",      // number
                    "TATU",         // university
                    "BACHELOR",     // blankType
                    "AVAILABLE",    // status
                    LocalDate.of(2024, 9, 1),   // receivedDate
                    null,           // issuedDate
                    2024,           // academicYear
                    "State Printing Centre",    // supplier
                    "BATCH-001",    // batchNumber
                    null,           // statusReason
                    "Hologram, watermark",      // securityFeatures
                    "First batch"   // notes
            );

            assertThat(blank.getBlankCode()).isEqualTo("AB 1234567");
            assertThat(blank.getSeries()).isEqualTo("AB");
            assertThat(blank.getNumber()).isEqualTo("1234567");
            assertThat(blank.getUniversity()).isEqualTo("TATU");
            assertThat(blank.getBlankType()).isEqualTo("BACHELOR");
            assertThat(blank.getStatus()).isEqualTo("AVAILABLE");
            assertThat(blank.getReceivedDate()).isEqualTo(LocalDate.of(2024, 9, 1));
            assertThat(blank.getIssuedDate()).isNull();
            assertThat(blank.getAcademicYear()).isEqualTo(2024);
            assertThat(blank.getSupplier()).isEqualTo("State Printing Centre");
            assertThat(blank.getBatchNumber()).isEqualTo("BATCH-001");
            assertThat(blank.getStatusReason()).isNull();
            assertThat(blank.getSecurityFeatures()).isEqualTo("Hologram, watermark");
            assertThat(blank.getNotes()).isEqualTo("First batch");
        }
    }

    // ==================================================================
    // Typical workflow scenario
    // ==================================================================

    @Nested
    @DisplayName("typical workflow")
    class TypicalWorkflow {

        @Test
        @DisplayName("Blank lifecycle: AVAILABLE -> ASSIGNED -> ISSUED")
        void blankLifecycleAvailableToIssued() {
            DiplomaBlank blank = new DiplomaBlank();
            blank.setBlankCode("GH 5555555");
            blank.setSeries("GH");
            blank.setNumber("5555555");
            blank.setUniversity("TATU");
            blank.setBlankType("BACHELOR");
            blank.setAcademicYear(2024);
            blank.setReceivedDate(LocalDate.of(2024, 3, 1));

            // Step 1: Blank received - AVAILABLE
            blank.setStatus("AVAILABLE");
            assertThat(blank.getStatus()).isEqualTo("AVAILABLE");
            assertThat(blank.getIssuedDate()).isNull();

            // Step 2: Blank assigned to student diploma - ASSIGNED
            blank.setStatus("ASSIGNED");
            assertThat(blank.getStatus()).isEqualTo("ASSIGNED");

            // Step 3: Diploma printed and issued - ISSUED
            blank.setStatus("ISSUED");
            blank.setIssuedDate(LocalDate.of(2024, 6, 25));
            assertThat(blank.getStatus()).isEqualTo("ISSUED");
            assertThat(blank.getIssuedDate()).isEqualTo(LocalDate.of(2024, 6, 25));
        }
    }
}
