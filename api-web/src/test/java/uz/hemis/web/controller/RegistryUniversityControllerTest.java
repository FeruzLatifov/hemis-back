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
import uz.hemis.service.registry.UniversityRegistryService;
import uz.hemis.service.registry.dto.UniversityDictionariesDto;
import uz.hemis.service.shared.I18nService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
 * </ul></p>
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
}
