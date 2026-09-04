package uz.hemis.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.exception.GlobalExceptionHandler;
import uz.hemis.common.dto.university.UniversityDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.service.registry.UniversityRegistryService;
import uz.hemis.service.registry.dto.UniversityDeletedRowDto;
import uz.hemis.service.registry.dto.UniversityDictionariesDto;
import uz.hemis.service.shared.I18nService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link RegistryUniversityController} slice testlari — @WebMvcTest (faqat web qatlam).
 *
 * <p>Test case'lar:
 * <ul>
 *   <li>GET /universities — list with pagination (200 OK)</li>
 *   <li>GET /universities — without permission (403)</li>
 *   <li>GET /universities/{id} — non-existent (404)</li>
 *   <li>GET /universities/dictionaries — returns dictionaries</li>
 *   <li>POST /universities — without edit permission (403)</li>
 *   <li>DELETE /universities/{code} — without delete permission (403)</li>
 *   <li>DELETE /universities/{code} — with 'universities.delete' (204) — gate regression guard</li>
 *   <li>GET /universities/deleted — soft-deleted rows (200 OK, 'universities.restore')</li>
 *   <li>GET /universities/deleted — 'universities.delete' alone is not enough (403)</li>
 *   <li>POST /universities/{code}/restore — restored university (200 OK, 'universities.restore')</li>
 *   <li>POST /universities/{code}/restore — nothing deleted under this code (404)</li>
 * </ul></p>
 *
 * <p>The bin and the restore sit behind {@code universities.restore}, not {@code universities.delete}:
 * the only purpose of the bin is to undo a delete. {@code DELETE /{code}} keeps
 * {@code universities.delete}, so the two rights are asserted separately here.</p>
 */
@WebMvcTest(
        controllers = RegistryUniversityController.class,
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@Import({RegistryUniversityControllerTest.Config.class, GlobalExceptionHandler.class})
@DisplayName("RegistryUniversityController Web Layer Tests")
class RegistryUniversityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UniversityRegistryService universityRegistryService;

    @SpringBootApplication
    @EnableMethodSecurity
    static class TestApp {
    }

    @TestConfiguration
    static class Config {
        @Bean
        I18nService i18nService() {
            I18nService mock = Mockito.mock(I18nService.class);
            when(mock.getMessage(anyString(), anyString()))
                    .thenAnswer(inv -> inv.getArgument(0));
            return mock;
        }
    }

    private static final String BASE_URL = "/api/v1/web/registry/universities";

    @Test
    @DisplayName("GET /universities — paginated list, 200 OK")
    @WithMockUser(authorities = "institutions.universities.view")
    void searchUniversities_returnsPagedList() throws Exception {
        Page<UniversityDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(universityRegistryService.searchUniversities(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /universities — ruxsatsiz 403")
    @WithMockUser(authorities = "basic.view")
    void searchUniversities_withoutPermission_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /universities/{id} — null qaytsa 404")
    @WithMockUser(authorities = "institutions.universities.view")
    void getUniversity_notFound_returns404() throws Exception {
        when(universityRegistryService.getUniversityById("MISSING_CODE")).thenReturn(null);

        mockMvc.perform(get(BASE_URL + "/MISSING_CODE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /universities/dictionaries — 200 OK")
    @WithMockUser(authorities = "institutions.universities.view")
    void getDictionaries_returnsDto() throws Exception {
        UniversityDictionariesDto dto = UniversityDictionariesDto.builder()
                .regions(List.of())
                .types(List.of())
                .ownerships(List.of())
                .activityStatuses(List.of())
                .belongsToOptions(List.of())
                .contractCategories(List.of())
                .versionTypes(List.of())
                .districts(List.of())
                .build();
        when(universityRegistryService.getDictionaries()).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/dictionaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("POST /universities — universities.edit ruxsatsiz 403")
    @WithMockUser(authorities = "institutions.universities.view")
    void createUniversity_withoutEditPermission_returns403() throws Exception {
        mockMvc.perform(post(BASE_URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"TEST\",\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /universities/{code} — universities.delete ruxsatsiz 403")
    @WithMockUser(authorities = "institutions.universities.view")
    void deleteUniversity_withoutDeletePermission_returns403() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/TEST_CODE").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /universities/{code} — universities.delete bilan 204 (gate regressiya qorovuli)")
    @WithMockUser(authorities = "universities.delete")
    void deleteUniversity_withDeletePermission_returns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/310").with(csrf()))
                .andExpect(status().isNoContent());

        verify(universityRegistryService).deleteUniversity("310");
    }

    @Test
    @DisplayName("GET /universities/deleted — o'chirilganlar ro'yxati, 200 OK")
    @WithMockUser(authorities = "universities.restore")
    void listDeleted_returnsDeletedRows() throws Exception {
        when(universityRegistryService.listDeletedUniversities()).thenReturn(List.of(
                new UniversityDeletedRowDto("310", "Test universiteti", "300000000",
                        LocalDateTime.of(2026, 9, 1, 10, 0), "admin"),
                new UniversityDeletedRowDto("311", "Ikkinchi universitet", null,
                        LocalDateTime.of(2026, 8, 30, 9, 0), null)
        ));

        mockMvc.perform(get(BASE_URL + "/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("310"))
                .andExpect(jsonPath("$.data[0].name").value("Test universiteti"))
                .andExpect(jsonPath("$.data[0].tin").value("300000000"))
                .andExpect(jsonPath("$.data[0].deletedAt").exists())
                .andExpect(jsonPath("$.data[0].deletedBy").value("admin"))
                .andExpect(jsonPath("$.data[1].code").value("311"));
    }

    @Test
    @DisplayName("GET /universities/deleted — universities.delete yetarli emas, 403")
    @WithMockUser(authorities = "universities.delete")
    void listDeleted_withDeleteButWithoutRestorePermission_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/deleted"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /universities/{code}/restore — tiklandi, 200 OK")
    @WithMockUser(authorities = "universities.restore")
    void restoreUniversity_returnsRestored() throws Exception {
        UniversityDto restored = new UniversityDto();
        restored.setCode("310");
        restored.setName("Test universiteti");
        when(universityRegistryService.restoreUniversity("310")).thenReturn(restored);

        mockMvc.perform(post(BASE_URL + "/310/restore").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("310"));

        verify(universityRegistryService).restoreUniversity("310");
    }

    @Test
    @DisplayName("POST /universities/{code}/restore — o'chirilgan qator yo'q, 404")
    @WithMockUser(authorities = "universities.restore")
    void restoreUniversity_notFound_returns404() throws Exception {
        when(universityRegistryService.restoreUniversity("MISSING_CODE"))
                .thenThrow(new ResourceNotFoundException("University (deleted)", "code", "MISSING_CODE"));

        mockMvc.perform(post(BASE_URL + "/MISSING_CODE/restore").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
