package uz.hemis.api.legacy.controller.university;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.UniversityDepartment;
import uz.hemis.domain.repository.UniversityDepartmentRepository;
import uz.hemis.service.legacy.UniversityDepartmentLegacyService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityDepartmentEntityController Tests")
class UniversityDepartmentEntityControllerTest {

    @Mock
    private UniversityDepartmentRepository repository;

    @Mock
    private CubaFilterHelper filterHelper;

    @Mock
    private LegacySecurityHelper securityHelper;

    @Mock
    private UniversityDepartmentLegacyService legacyService;

    @InjectMocks
    private UniversityDepartmentEntityController controller;

    // =============================================
    // GET by ID
    // =============================================

    @Test
    @DisplayName("GET /{id} - mavjud entity qaytariladi")
    void getByIdFound() {
        String code = "305-10";
        UniversityDepartment dept = createDepartment(code, "Informatika");
        when(repository.findByCode(code)).thenReturn(Optional.of(dept));

        ResponseEntity<Map<String, Object>> response = controller.getById(code, null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("_entityName", "hemishe_EUniversityDepartment");
        assertThat(response.getBody()).containsKey("id");
    }

    @Test
    @DisplayName("GET /{id} - topilmagan entity uchun 404")
    void getByIdNotFound() {
        String code = "999-99";
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.getById(code, null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    // =============================================
    // GET ALL (pagination)
    // =============================================

    @Test
    @DisplayName("GET / - sahifalangan ro'yxat qaytariladi")
    void getAllReturnsPaginatedList() {
        List<UniversityDepartment> depts = List.of(
                createDepartment("305-10", "Informatika"),
                createDepartment("305-11", "Matematika")
        );

        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(depts));

        ResponseEntity<List<Map<String, Object>>> response =
                controller.getAll(null, 0, 50, null, null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0)).containsEntry("_entityName", "hemishe_EUniversityDepartment");
    }

    // =============================================
    // DELETE (soft delete)
    // =============================================

    @Test
    @DisplayName("DELETE /{id} - soft delete muvaffaqiyatli")
    void deleteReturns200() {
        String code = "305-10";
        UniversityDepartment dept = createDepartment(code, "Informatika");
        when(repository.findByCode(code)).thenReturn(Optional.of(dept));
        when(securityHelper.getCurrentUsername()).thenReturn("otm520");

        ResponseEntity<?> response = controller.delete(code);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(repository).save(any(UniversityDepartment.class));
    }

    @Test
    @DisplayName("DELETE /{id} - topilmagan entity uchun 404")
    void deleteReturns404() {
        String code = "999-99";
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.delete(code);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    // =============================================
    // CUBA format validation
    // =============================================

    @Test
    @DisplayName("Response formati CUBA standartiga mos")
    void responseContainsCubaMetadata() {
        String code = "305-10";
        UniversityDepartment dept = createDepartment(code, "Informatika kafedrasi");
        when(repository.findByCode(code)).thenReturn(Optional.of(dept));

        ResponseEntity<Map<String, Object>> response = controller.getById(code, null, null, null);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();

        // CUBA metadata fieldlar majburiy
        assertThat(body).containsKey("_entityName");
        assertThat(body).containsKey("_instanceName");
        assertThat(body).containsKey("id");

        // _entityName to'g'ri
        assertThat(body.get("_entityName")).isEqualTo("hemishe_EUniversityDepartment");

        // id string formatda
        assertThat(body.get("id")).isInstanceOf(String.class);
    }

    // =============================================
    // Helper methods
    // =============================================

    private UniversityDepartment createDepartment(String code, String name) {
        UniversityDepartment dept = new UniversityDepartment();
        dept.setCode(code);
        dept.setNameUz(name);
        dept.setStatus(true);
        return dept;
    }
}
