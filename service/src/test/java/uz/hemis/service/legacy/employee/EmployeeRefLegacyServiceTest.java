package uz.hemis.service.legacy.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;
import uz.hemis.service.legacy.SoftDeleteRestoreLegacyService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeRefLegacyServiceTest {

    @Mock private TeacherPositionTypeRepository teacherPositionTypeRepository;
    @Mock private UniversityEmployeeRateRepository universityEmployeeRateRepository;
    @Mock private EmployeeCertificateRepository employeeCertificateRepository;
    @Mock private UniversityEmployeeFormRepository universityEmployeeFormRepository;
    @Mock private UniversityEmployeeStatusTypeRepository universityEmployeeStatusTypeRepository;
    @Mock private UniversityEmployeeTypeRepository universityEmployeeTypeRepository;
    @Mock private SoftDeleteRestoreLegacyService softDeleteRestoreService;

    @InjectMocks
    private EmployeeRefLegacyService service;

    // ====================================================================
    //  TeacherPositionType
    // ====================================================================

    @Nested
    @DisplayName("TeacherPositionType")
    class TeacherPositionTypeTests {

        @Test
        void toTeacherPositionTypeMap_shouldBuildCorrectMap() {
            TeacherPositionType entity = new TeacherPositionType();
            entity.setCode("12");
            entity.setName("O'qituvchi");
            entity.setActive(true);
            entity.setVersion(1);

            Map<String, Object> map = service.toTeacherPositionTypeMap(entity, false);

            assertThat(map).containsEntry("_entityName", "hemishe_HTeacherPositionType");
            assertThat(map).containsEntry("_instanceName", "O'qituvchi");
            assertThat(map).containsEntry("id", "12");
            assertThat(map).containsEntry("code", "12");
            assertThat(map).containsEntry("name", "O'qituvchi");
            assertThat(map).containsEntry("active", true);
            assertThat(map).doesNotContainKey("deleteTs");
        }

        @Test
        void toTeacherPositionTypeMap_withReturnNulls_includesNulls() {
            TeacherPositionType entity = new TeacherPositionType();
            entity.setCode("12");
            entity.setName("O'qituvchi");

            Map<String, Object> map = service.toTeacherPositionTypeMap(entity, true);

            assertThat(map).containsKey("deleteTs");
            assertThat(map).containsKey("deletedBy");
            assertThat(map.get("deleteTs")).isNull();
        }
    }

    // ====================================================================
    //  UniversityEmployeeRate
    // ====================================================================

    @Nested
    @DisplayName("UniversityEmployeeRate")
    class UniversityEmployeeRateTests {

        @Test
        void toUniversityEmployeeRateMap_shouldBuildCorrectMap() {
            UniversityEmployeeRate entity = new UniversityEmployeeRate();
            entity.setCode("11");
            entity.setName("1,00 stavka");
            entity.setActive(true);

            Map<String, Object> map = service.toUniversityEmployeeRateMap(entity, false);

            assertThat(map).containsEntry("_entityName", "hemishe_HUniversityEmployeeRate");
            assertThat(map).containsEntry("id", "11");
            assertThat(map).containsEntry("name", "1,00 stavka");
        }
    }

    // ====================================================================
    //  EmployeeCertificate
    // ====================================================================

    @Nested
    @DisplayName("EmployeeCertificate")
    class EmployeeCertificateTests {

        @Test
        void toEmployeeCertificateMap_shouldIncludeAllFields() {
            UUID id = UUID.randomUUID();
            UUID empId = UUID.randomUUID();
            EmployeeCertificate cert = new EmployeeCertificate();
            cert.setId(id);
            cert.setSerialNumber("SN-001");
            cert.setUniversity("401");
            cert.setEmployee(empId);
            cert.setCertificateType("IELTS");
            cert.setCertificateName("IELTS Certificate");
            cert.setIssueDate(LocalDate.of(2024, 1, 15));
            cert.setActive(true);
            cert.setCreateTs(LocalDateTime.now());

            Map<String, Object> map = service.toEmployeeCertificateMap(cert);

            assertThat(map).containsEntry("_entityName", "hemishe_EEmpoyeeCertificate");
            assertThat(map).containsEntry("_instanceName", "SN-001");
            assertThat(map).containsEntry("id", id);
            assertThat(map).containsEntry("university", "401");
            assertThat(map).containsEntry("employee", empId);
            assertThat(map).containsEntry("certificateType", "IELTS");
            assertThat(map).containsEntry("issueDate", LocalDate.of(2024, 1, 15));
        }

        @Test
        void updateEmployeeCertificateFromMap_shouldUpdateFields() {
            EmployeeCertificate cert = new EmployeeCertificate();

            Map<String, Object> data = new HashMap<>();
            data.put("university", "401");
            data.put("certificateName", "Test Certificate");
            data.put("issueDate", "2024-06-15");
            data.put("active", true);

            service.updateEmployeeCertificateFromMap(cert, data);

            assertThat(cert.getUniversity()).isEqualTo("401");
            assertThat(cert.getCertificateName()).isEqualTo("Test Certificate");
            assertThat(cert.getIssueDate()).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(cert.getActive()).isTrue();
        }

        @Test
        void createOrUpsertEmployeeCertificate_existingId_updatesEntity() {
            UUID existingId = UUID.randomUUID();
            EmployeeCertificate existing = new EmployeeCertificate();
            existing.setId(existingId);
            existing.setSerialNumber("OLD-SN");

            when(employeeCertificateRepository.findById(existingId)).thenReturn(Optional.of(existing));
            when(employeeCertificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> data = new HashMap<>();
            data.put("id", existingId.toString());
            data.put("serialNumber", "NEW-SN");

            EmployeeCertificate result = service.createOrUpsertEmployeeCertificate(data);

            assertThat(result.getSerialNumber()).isEqualTo("NEW-SN");
            assertThat(result.getUpdateTs()).isNotNull();
        }

        @Test
        void createOrUpsertEmployeeCertificate_noId_createsNewEntity() {
            when(employeeCertificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> data = new HashMap<>();
            data.put("serialNumber", "NEW-SN");
            data.put("certificateName", "New Cert");

            EmployeeCertificate result = service.createOrUpsertEmployeeCertificate(data);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getSerialNumber()).isEqualTo("NEW-SN");
            assertThat(result.getCreateTs()).isNotNull();
        }
    }

    // ====================================================================
    //  UniversityEmployeeForm
    // ====================================================================

    @Nested
    @DisplayName("UniversityEmployeeForm")
    class UniversityEmployeeFormTests {

        @Test
        void toUniversityEmployeeFormMap_preservesOldHemisFieldOrder() {
            UniversityEmployeeForm entity = new UniversityEmployeeForm();
            entity.setCode("11");
            entity.setName("Asosiy shtat");
            entity.setNameRu(null);
            entity.setNameEn(null);
            entity.setActive(true);
            entity.setVersion(1);

            Map<String, Object> map = service.toUniversityEmployeeFormMap(entity, true);

            assertThat(map).containsEntry("_entityName", "hemishe_HUniversityEmployeeForm");
            assertThat(map).containsEntry("id", "11");
            // Verify OLD-HEMIS field order
            List<String> keys = new ArrayList<>(map.keySet());
            assertThat(keys.indexOf("nameRu")).isLessThan(keys.indexOf("code"));
            assertThat(keys.indexOf("deleteTs")).isLessThan(keys.indexOf("code"));
            assertThat(keys.indexOf("code")).isLessThan(keys.indexOf("nameEn"));
        }
    }

    // ====================================================================
    //  UniversityEmployeeStatusType
    // ====================================================================

    @Nested
    @DisplayName("UniversityEmployeeStatusType")
    class UniversityEmployeeStatusTypeTests {

        @Test
        void toUniversityEmployeeStatusTypeMap_shouldBuildCorrectMap() {
            UniversityEmployeeStatusType entity = new UniversityEmployeeStatusType();
            entity.setCode("11");
            entity.setName("Ishlamoqda");
            entity.setActive(true);

            Map<String, Object> map = service.toUniversityEmployeeStatusTypeMap(entity, false);

            assertThat(map).containsEntry("_entityName", "hemishe_HUniversityEmployeeStatusType");
            assertThat(map).containsEntry("id", "11");
            assertThat(map).containsEntry("name", "Ishlamoqda");
        }
    }

    // ====================================================================
    //  UniversityEmployeeType
    // ====================================================================

    @Nested
    @DisplayName("UniversityEmployeeType")
    class UniversityEmployeeTypeTests {

        @Test
        void toUniversityEmployeeTypeMap_shouldBuildCorrectMap() {
            UniversityEmployeeType entity = new UniversityEmployeeType();
            entity.setCode("12");
            entity.setName("Professor-o'qituvchi xodim");
            entity.setNameEn("Teaching staff");
            entity.setNameRu("Профессорско-преподавательский состав");
            entity.setActive(true);

            Map<String, Object> map = service.toUniversityEmployeeTypeMap(entity, false);

            assertThat(map).containsEntry("_entityName", "hemishe_HUniversityEmployeeType");
            assertThat(map).containsEntry("id", "12");
            assertThat(map).containsEntry("code", "12");
            assertThat(map).containsEntry("nameEn", "Teaching staff");
            assertThat(map).containsEntry("nameRu", "Профессорско-преподавательский состав");
        }

        @Test
        void toUniversityEmployeeTypeMinimalMap_shouldReturnMinimalFields() {
            UniversityEmployeeType entity = new UniversityEmployeeType();
            entity.setCode("12");
            entity.setName("Professor-o'qituvchi xodim");

            Map<String, Object> map = service.toUniversityEmployeeTypeMinimalMap(entity);

            assertThat(map).hasSize(3);
            assertThat(map).containsKeys("_entityName", "_instanceName", "id");
        }

        @Test
        void updateUniversityEmployeeType_shouldUpdateFieldsAndTimestamps() {
            UniversityEmployeeType entity = new UniversityEmployeeType();
            entity.setCode("12");
            entity.setName("Old Name");

            when(universityEmployeeTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> data = new HashMap<>();
            data.put("name", "New Name");
            data.put("nameEn", "New Name En");
            data.put("active", false);

            UniversityEmployeeType result = service.updateUniversityEmployeeType(entity, data, "admin");

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getNameEn()).isEqualTo("New Name En");
            assertThat(result.getActive()).isFalse();
            assertThat(result.getUpdatedBy()).isEqualTo("admin");
            assertThat(result.getUpdateTs()).isNotNull();
        }

        @Test
        void createOrUpsertUniversityEmployeeType_existingEntity_updates() {
            UniversityEmployeeType existing = new UniversityEmployeeType();
            existing.setCode("12");
            existing.setName("Old");

            when(universityEmployeeTypeRepository.findById("12")).thenReturn(Optional.of(existing));
            when(universityEmployeeTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> data = new HashMap<>();
            data.put("nameEn", "Teaching");

            UniversityEmployeeType result = service.createOrUpsertUniversityEmployeeType("12", "New Name", data, "admin");

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getNameEn()).isEqualTo("Teaching");
        }

        @Test
        void createOrUpsertUniversityEmployeeType_softDeleted_restores() {
            when(universityEmployeeTypeRepository.findById("99"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new UniversityEmployeeType()));
            when(softDeleteRestoreService.hasSoftDeletedRecord("hemishe_h_university_employee_type", "99"))
                .thenReturn(true);
            when(universityEmployeeTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> data = new HashMap<>();
            UniversityEmployeeType result = service.createOrUpsertUniversityEmployeeType("99", "Restored", data, "admin");

            verify(softDeleteRestoreService).restoreSoftDeletedRecord("hemishe_h_university_employee_type", "99");
            assertThat(result.getName()).isEqualTo("Restored");
        }

        @Test
        void createOrUpsertUniversityEmployeeType_newEntity_creates() {
            when(universityEmployeeTypeRepository.findById("99")).thenReturn(Optional.empty());
            when(softDeleteRestoreService.hasSoftDeletedRecord(anyString(), eq("99"))).thenReturn(false);
            when(universityEmployeeTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> data = new HashMap<>();

            UniversityEmployeeType result = service.createOrUpsertUniversityEmployeeType("99", "Brand New", data, "admin");

            assertThat(result.getCode()).isEqualTo("99");
            assertThat(result.getName()).isEqualTo("Brand New");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getCreatedBy()).isEqualTo("admin");
        }

        @Test
        void applyCubaFilter_shouldFilterByConditions() {
            UniversityEmployeeType e1 = new UniversityEmployeeType();
            e1.setCode("12");
            e1.setName("Professor");
            e1.setActive(true);

            UniversityEmployeeType e2 = new UniversityEmployeeType();
            e2.setCode("13");
            e2.setName("Texnik");
            e2.setActive(false);

            List<UniversityEmployeeType> entities = List.of(e1, e2);

            Map<String, Object> filter = Map.of(
                "conditions", List.of(
                    Map.of("property", "active", "operator", "=", "value", "true")
                )
            );

            List<UniversityEmployeeType> result = service.applyCubaFilter(entities, filter);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("12");
        }

        @Test
        void applyCubaFilter_likeOperator_shouldFilterByPartialMatch() {
            UniversityEmployeeType e1 = new UniversityEmployeeType();
            e1.setCode("12");
            e1.setName("Professor-o'qituvchi");

            UniversityEmployeeType e2 = new UniversityEmployeeType();
            e2.setCode("13");
            e2.setName("Texnik xodim");

            List<UniversityEmployeeType> entities = List.of(e1, e2);

            Map<String, Object> filter = Map.of(
                "conditions", List.of(
                    Map.of("property", "name", "operator", "like", "value", "%Professor%")
                )
            );

            List<UniversityEmployeeType> result = service.applyCubaFilter(entities, filter);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCode()).isEqualTo("12");
        }
    }
}
