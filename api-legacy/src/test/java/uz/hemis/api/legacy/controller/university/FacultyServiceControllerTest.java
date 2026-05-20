package uz.hemis.api.legacy.controller.university;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.academic.Faculty;
import uz.hemis.service.legacy.university.UniversityRefLegacyService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FacultyServiceController — CUBA faculty endpoints")
class FacultyServiceControllerTest {

    @Mock private UniversityRefLegacyService universityService;
    @Mock private LegacySecurityHelper securityHelper;

    @InjectMocks
    private FacultyServiceController controller;

    private Faculty fac(String code, String name, String universityCode) {
        Faculty f = new Faculty();
        f.setCode(code);
        f.setName(name);
        f.setUniversity(universityCode);
        return f;
    }

    @Test
    @DisplayName("GET /get — faculty CUBA format + university filter")
    void get_returnsFilteredFaculties() {
        Faculty f337a = fac("337-IT", "Axborot texnologiyalari", "337");
        Faculty f337b = fac("337-MATH", "Matematika", "337");
        Faculty f401 = fac("401-IT", "TUIT IT", "401");

        when(universityService.findAllFaculty()).thenReturn(List.of(f337a, f337b, f401));

        ResponseEntity<Map<String, Object>> response = controller.get("337");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsEntry("success", true);
        assertThat(body).containsEntry("count", 2);
        assertThat(body).isInstanceOf(LinkedHashMap.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
        assertThat(data).hasSize(2);
        // Old-hemis CUBA format check
        assertThat(data.get(0)).containsEntry("_entityName", "hemishe_EUniversityDepartment");
        assertThat(data.get(0)).containsEntry("version", 4);
        assertThat(data.get(0)).containsEntry("status", true);
    }

    @Test
    @DisplayName("GET /get — boshqa OTM ham ko'rinadi (cross-tenant old-hemis compat)")
    void get_crossTenantAllowed() {
        Faculty f401 = fac("401-IT", "TUIT IT", "401");
        when(universityService.findAllFaculty()).thenReturn(List.of(f401));

        ResponseEntity<Map<String, Object>> response = controller.get("401");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("count", 1);
    }

    @Test
    @DisplayName("GET /get — natija yo'q → count=0, data=[]")
    void get_noMatching_empty() {
        when(universityService.findAllFaculty()).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = controller.get("999");

        Map<String, Object> body = response.getBody();
        assertThat(body).containsEntry("count", 0);
        assertThat((List<?>) body.get("data")).isEmpty();
    }

    @Test
    @DisplayName("GET /list — get() ga delegate qiladi")
    void list_delegatesToGet() {
        when(universityService.findAllFaculty())
                .thenReturn(List.of(fac("337-IT", "IT", "337")));

        ResponseEntity<Map<String, Object>> response = controller.list("337");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("count", 1);
    }

    @Test
    @DisplayName("GET /count — faqat count qaytaradi (data yo'q)")
    void count_onlyCount() {
        when(universityService.findAllFaculty()).thenReturn(List.of(
                fac("337-IT", "IT", "337"),
                fac("337-MATH", "Math", "337"),
                fac("401-IT", "TUIT IT", "401")));

        ResponseEntity<Map<String, Object>> response = controller.count("337");

        Map<String, Object> body = response.getBody();
        assertThat(body).containsEntry("success", true);
        assertThat(body).containsEntry("count", 2L);
        assertThat(body).doesNotContainKey("data");
    }
}
