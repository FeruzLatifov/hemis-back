package uz.hemis.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Diploma Entity Unit Tests
 *
 * <p>Tests Lombok-generated getters/setters and inherited soft delete logic.</p>
 *
 * <p>Diploma extends BaseEntity (UUID primary key)
 * and uses @NoArgsConstructor / @AllArgsConstructor.</p>
 */
@DisplayName("Diploma Entity Tests")
class DiplomaEntityTest {

    @Test
    @DisplayName("Should create instance with no-arg constructor")
    void shouldCreateInstance() {
        Diploma diploma = new Diploma();

        assertThat(diploma).isNotNull();
        assertThat(diploma.getId()).isNull();
        assertThat(diploma.getDiplomaNumber()).isNull();
        assertThat(diploma.getStatus()).isNull();
    }

    @Test
    @DisplayName("Should set and get key business fields")
    void shouldSetAndGetKeyFields() {
        Diploma diploma = new Diploma();
        UUID studentId = UUID.randomUUID();
        UUID blankId = UUID.randomUUID();

        diploma.setDiplomaNumber("UZ-2024-TATU-0001");
        diploma.setStudent(studentId);
        diploma.setUniversity("TATU");
        diploma.setDiplomaBlank(blankId);
        diploma.setSerialNumber("AB 1234567");
        diploma.setDiplomaType("BACHELOR");
        diploma.setIssueDate(LocalDate.of(2024, 6, 25));
        diploma.setGraduationYear(2024);
        diploma.setQualification("Bachelor of Computer Science");
        diploma.setAverageGrade(4.5);
        diploma.setHonors("HONORS");
        diploma.setStatus("ISSUED");

        assertThat(diploma.getDiplomaNumber()).isEqualTo("UZ-2024-TATU-0001");
        assertThat(diploma.getStudent()).isEqualTo(studentId);
        assertThat(diploma.getUniversity()).isEqualTo("TATU");
        assertThat(diploma.getDiplomaBlank()).isEqualTo(blankId);
        assertThat(diploma.getSerialNumber()).isEqualTo("AB 1234567");
        assertThat(diploma.getDiplomaType()).isEqualTo("BACHELOR");
        assertThat(diploma.getIssueDate()).isEqualTo(LocalDate.of(2024, 6, 25));
        assertThat(diploma.getGraduationYear()).isEqualTo(2024);
        assertThat(diploma.getQualification()).isEqualTo("Bachelor of Computer Science");
        assertThat(diploma.getAverageGrade()).isEqualTo(4.5);
        assertThat(diploma.getHonors()).isEqualTo("HONORS");
        assertThat(diploma.getStatus()).isEqualTo("ISSUED");
    }

    @Test
    @DisplayName("isDeleted should return true when deleteTs is set (inherited from BaseEntity)")
    void isDeletedShouldWorkFromBaseEntity() {
        Diploma diploma = new Diploma();

        assertThat(diploma.isDeleted()).isFalse();

        diploma.setDeleteTs(LocalDateTime.now());
        assertThat(diploma.isDeleted()).isTrue();

        diploma.setDeleteTs(null);
        assertThat(diploma.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Should set and get verification fields")
    void shouldSetAndGetVerificationFields() {
        Diploma diploma = new Diploma();

        diploma.setDiplomaHash("abc123def456");
        diploma.setQrCode("https://verify.edu.uz/qr/abc123");
        diploma.setVerificationUrl("https://verify.edu.uz/diploma/abc123");
        diploma.setRectorName("Prof. Aliyev A.A.");

        assertThat(diploma.getDiplomaHash()).isEqualTo("abc123def456");
        assertThat(diploma.getQrCode()).isEqualTo("https://verify.edu.uz/qr/abc123");
        assertThat(diploma.getVerificationUrl()).isEqualTo("https://verify.edu.uz/diploma/abc123");
        assertThat(diploma.getRectorName()).isEqualTo("Prof. Aliyev A.A.");
    }

    @Test
    @DisplayName("Should inherit UUID id and version from BaseEntity")
    void shouldInheritBaseEntityFields() {
        Diploma diploma = new Diploma();
        UUID id = UUID.randomUUID();

        diploma.setId(id);
        diploma.setVersion(1);
        diploma.setCreatedBy("registrar");

        assertThat(diploma.getId()).isEqualTo(id);
        assertThat(diploma.getVersion()).isEqualTo(1);
        assertThat(diploma.getCreatedBy()).isEqualTo("registrar");
    }
}
