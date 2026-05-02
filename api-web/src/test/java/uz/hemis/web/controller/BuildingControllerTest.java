package uz.hemis.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.common.dto.building.BuildingCreateUpdateDto;
import uz.hemis.common.dto.building.BuildingDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.service.infrastructure.BuildingLifecycleService;
import uz.hemis.service.infrastructure.UniversityBuildingService;
import uz.hemis.service.shared.I18nService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link BuildingController} slice testlar — @WebMvcTest (faqat web qatlam).
 *
 * <p>Test case'lar:
 * <ul>
 *   <li>GET list — 200 OK</li>
 *   <li>GET detail — 404 not found</li>
 *   <li>POST invalid — 400 (cross-field validation)</li>
 *   <li>POST unauthorized — 403 (no permission)</li>
 *   <li>DELETE — 200 soft delete</li>
 * </ul></p>
 */
@WebMvcTest(controllers = BuildingController.class)
@Import(BuildingControllerTest.Config.class)
@DisplayName("BuildingController Web Layer Tests")
class BuildingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UniversityBuildingService buildingService;

    @MockitoBean
    private BuildingLifecycleService lifecycleService;

    @TestConfiguration
    static class Config {
        @Bean
        I18nService i18nService() {
            // Mock i18n for exception handler
            I18nService mock = org.mockito.Mockito.mock(I18nService.class);
            when(mock.getMessage(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                    .thenAnswer(inv -> inv.getArgument(0));
            return mock;
        }
    }

    @Test
    @DisplayName("GET /universities/{code}/buildings — 200 OK bilan pagination")
    @WithMockUser(authorities = "buildings.view")
    void list_returnsPagedBuildings() throws Exception {
        Page<BuildingDto> page = new PageImpl<>(
                List.of(BuildingDto.builder().id(UUID.randomUUID()).name("Bosh").build()),
                PageRequest.of(0, 50), 1
        );
        when(buildingService.findByUniversity(eq("401"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/web/universities/401/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Bosh"));
    }

    @Test
    @DisplayName("GET /buildings/{id} — 404 topilmaganda")
    @WithMockUser(authorities = "buildings.view")
    void detail_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(buildingService.findById(id))
                .thenThrow(new ResourceNotFoundException("Building", "id", id));

        mockMvc.perform(get("/api/v1/web/buildings/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST — validation xatosi bilan 400")
    @WithMockUser(authorities = "buildings.edit")
    void create_withInvalidData_returns400() throws Exception {
        // name bo'sh + categoryCode bo'sh → @NotBlank violation
        BuildingCreateUpdateDto invalid = BuildingCreateUpdateDto.builder().build();

        mockMvc.perform(post("/api/v1/web/universities/401/buildings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — coordinates juft emas (faqat lat), 400")
    @WithMockUser(authorities = "buildings.edit")
    void create_withUnpairedCoordinates_returns400() throws Exception {
        BuildingCreateUpdateDto dto = BuildingCreateUpdateDto.builder()
                .name("Test")
                .categoryCode("ACADEMIC")
                .latitude(new BigDecimal("41.31"))
                // longitude null — juft emas
                .build();

        mockMvc.perform(post("/api/v1/web/universities/401/buildings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — ruxsat yo'q bo'lsa 403")
    @WithMockUser(authorities = "buildings.view")  // only view, no edit
    void create_withoutEditPermission_returns403() throws Exception {
        BuildingCreateUpdateDto dto = BuildingCreateUpdateDto.builder()
                .name("Test")
                .categoryCode("ACADEMIC")
                .build();

        mockMvc.perform(post("/api/v1/web/universities/401/buildings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE — 200 OK soft delete")
    @WithMockUser(authorities = "buildings.edit")
    void delete_returnsSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/web/buildings/" + id).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET history — lifecycle event'lar ro'yxati")
    @WithMockUser(authorities = "buildings.view")
    void history_returnsLifecycleEvents() throws Exception {
        UUID id = UUID.randomUUID();
        when(lifecycleService.getHistory(id)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/web/buildings/" + id + "/history"))
                .andExpect(status().isOk());
    }
}
