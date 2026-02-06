package uz.hemis.service.legacy.science;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScienceEntityLegacyService Unit Tests")
class ScienceEntityLegacyServiceTest {

    @Mock private PublicationCriteriaRepository publicationCriteriaRepository;
    @Mock private PublicationAuthorMetaRepository publicationAuthorMetaRepository;
    @Mock private PublicationMethodicalRepository publicationMethodicalRepository;
    @Mock private MethodicalPublicationTypeRepository methodicalPublicationTypeRepository;
    @Mock private ProjectMetaRepository projectMetaRepository;
    @Mock private ResearchActivityRepository researchActivityRepository;
    @Mock private ProjectExecutorRepository projectExecutorRepository;
    @Mock private ProjectRepository projectRepository;

    @InjectMocks
    private ScienceEntityLegacyService service;

    // ====================================================================
    //  PublicationCriteria
    // ====================================================================
    @Nested
    @DisplayName("PublicationCriteria")
    class PublicationCriteriaTests {

        @Test
        @DisplayName("toPublicationCriteriaMap returns correct fields")
        void toMap_returnsCorrectFields() {
            PublicationCriteria entity = new PublicationCriteria();
            entity.setId(UUID.randomUUID());
            entity.setVersion(1);
            entity.setActive(true);
            entity.setPublicationTypeTable("hemishe_EPublicationScientific");
            entity.setMarkValue(10);

            Map<String, Object> result = service.toPublicationCriteriaMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_EPublicationCriteria");
            assertThat(result).containsEntry("active", true);
            assertThat(result).containsEntry("markValue", 10);
            assertThat(result).containsEntry("publicationTypeTable", "hemishe_EPublicationScientific");
            assertThat(result).containsKey("id");
        }

        @Test
        @DisplayName("updatePublicationCriteriaFromMap sets fields correctly")
        void updateFromMap_setsFields() {
            PublicationCriteria entity = new PublicationCriteria();
            Map<String, Object> map = new HashMap<>();
            map.put("_university", "401");
            map.put("_education_year", "2024");
            map.put("markValue", 15);
            map.put("active", true);

            service.updatePublicationCriteriaFromMap(entity, map);

            assertThat(entity.getUniversity()).isEqualTo("401");
            assertThat(entity.getEducationYear()).isEqualTo("2024");
            assertThat(entity.getMarkValue()).isEqualTo(15);
            assertThat(entity.getActive()).isTrue();
        }

        @Test
        @DisplayName("findPublicationCriteriaById delegates to repository")
        void findById_delegatesToRepository() {
            UUID id = UUID.randomUUID();
            PublicationCriteria entity = new PublicationCriteria();
            when(publicationCriteriaRepository.findById(id)).thenReturn(Optional.of(entity));

            Optional<PublicationCriteria> result = service.findPublicationCriteriaById(id);

            assertThat(result).isPresent();
            verify(publicationCriteriaRepository).findById(id);
        }

        @Test
        @DisplayName("savePublicationCriteria delegates to repository")
        void save_delegatesToRepository() {
            PublicationCriteria entity = new PublicationCriteria();
            when(publicationCriteriaRepository.save(entity)).thenReturn(entity);

            PublicationCriteria result = service.savePublicationCriteria(entity);

            assertThat(result).isSameAs(entity);
            verify(publicationCriteriaRepository).save(entity);
        }
    }

    // ====================================================================
    //  PublicationAuthorMeta
    // ====================================================================
    @Nested
    @DisplayName("PublicationAuthorMeta")
    class PublicationAuthorMetaTests {

        @Test
        @DisplayName("toPublicationAuthorMetaMap returns correct fields")
        void toMap_returnsCorrectFields() {
            PublicationAuthorMeta entity = new PublicationAuthorMeta();
            entity.setId(UUID.randomUUID());
            entity.setActive(true);
            entity.setIsCheckedByAuthor(false);
            entity.setIsMainAuthor(1);

            Map<String, Object> result = service.toPublicationAuthorMetaMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_EPublicationAuthorMeta");
            assertThat(result).containsEntry("active", true);
            assertThat(result).containsEntry("isCheckedByAuthor", false);
            assertThat(result).containsEntry("isMainAuthor", 1);
        }

        @Test
        @DisplayName("updatePublicationAuthorMetaFromMap handles isMainAuthor as Boolean")
        void updateFromMap_handlesIsMainAuthorBoolean() {
            PublicationAuthorMeta entity = new PublicationAuthorMeta();
            Map<String, Object> map = new HashMap<>();
            map.put("isMainAuthor", true);

            service.updatePublicationAuthorMetaFromMap(entity, map);

            assertThat(entity.getIsMainAuthor()).isEqualTo(1);
        }

        @Test
        @DisplayName("updatePublicationAuthorMetaFromMap handles nested employee UUID")
        void updateFromMap_handlesNestedEmployee() {
            PublicationAuthorMeta entity = new PublicationAuthorMeta();
            UUID empId = UUID.randomUUID();
            Map<String, Object> map = new HashMap<>();
            map.put("employee", Map.of("id", empId.toString()));

            service.updatePublicationAuthorMetaFromMap(entity, map);

            assertThat(entity.getEmployee()).isEqualTo(empId);
        }
    }

    // ====================================================================
    //  PublicationMethodical
    // ====================================================================
    @Nested
    @DisplayName("PublicationMethodical")
    class PublicationMethodicalTests {

        @Test
        @DisplayName("toPublicationMethodicalMap returns name-based instanceName")
        void toMap_returnsNameBasedInstanceName() {
            PublicationMethodical entity = new PublicationMethodical();
            entity.setId(UUID.randomUUID());
            entity.setName("Test Publication");

            Map<String, Object> result = service.toPublicationMethodicalMap(entity, false);

            assertThat(result.get("_instanceName")).isEqualTo("Test Publication");
        }

        @Test
        @DisplayName("updatePublicationMethodicalFromMap supports both field formats")
        void updateFromMap_supportsBothFormats() {
            PublicationMethodical entity = new PublicationMethodical();
            Map<String, Object> map = new HashMap<>();
            map.put("authorCounts", 3);
            map.put("issueYear", 2024);
            map.put("_university", "401");

            service.updatePublicationMethodicalFromMap(entity, map);

            assertThat(entity.getAuthorCounts()).isEqualTo(3);
            assertThat(entity.getIssueYear()).isEqualTo(2024);
            assertThat(entity.getUniversity()).isEqualTo("401");
        }
    }

    // ====================================================================
    //  MethodicalPublicationType
    // ====================================================================
    @Nested
    @DisplayName("MethodicalPublicationType")
    class MethodicalPublicationTypeTests {

        @Test
        @DisplayName("toMethodicalPublicationTypeListMap returns code-based id")
        void toListMap_returnsCodeBasedId() {
            MethodicalPublicationType entity = new MethodicalPublicationType();
            entity.setCode("11");
            entity.setName("Darslik");
            entity.setActive(true);
            entity.setVersion(1);

            Map<String, Object> result = service.toMethodicalPublicationTypeListMap(entity);

            assertThat(result).containsEntry("id", "11");
            assertThat(result).containsEntry("code", "11");
            assertThat(result).containsEntry("name", "Darslik");
            assertThat(result.get("_instanceName")).isEqualTo("11 Darslik");
        }

        @Test
        @DisplayName("findMethodicalPublicationTypeByCode delegates to repository")
        void findByCode_delegatesToRepository() {
            when(methodicalPublicationTypeRepository.findByCodeAndDeleteTsIsNull("11"))
                .thenReturn(Optional.of(new MethodicalPublicationType()));

            Optional<MethodicalPublicationType> result = service.findMethodicalPublicationTypeByCode("11");

            assertThat(result).isPresent();
        }
    }

    // ====================================================================
    //  ProjectMeta
    // ====================================================================
    @Nested
    @DisplayName("ProjectMeta")
    class ProjectMetaTests {

        @Test
        @DisplayName("toProjectMetaMap returns CUBA class instance name")
        void toMap_returnsCubaInstanceName() {
            ProjectMeta entity = new ProjectMeta();
            UUID id = UUID.randomUUID();
            entity.setId(id);
            entity.setActive(true);
            entity.setFiscalYear(2024);
            entity.setBudget(50000000.0);

            Map<String, Object> result = service.toProjectMetaMap(entity, false);

            assertThat(result.get("_instanceName").toString())
                .contains("com.company.hemishe.entity.EProjectMeta");
            assertThat(result).containsEntry("fiscalYear", 2024);
            assertThat(result).containsEntry("budget", 50000000.0);
        }
    }

    // ====================================================================
    //  ResearchActivity
    // ====================================================================
    @Nested
    @DisplayName("ResearchActivity")
    class ResearchActivityTests {

        @Test
        @DisplayName("toResearchActivityMap returns all fields")
        void toMap_returnsAllFields() {
            ResearchActivity entity = new ResearchActivity();
            entity.setId(UUID.randomUUID());
            entity.setHIndex("5");
            entity.setScientificWorkCount("10");
            entity.setReferenceCount("25");
            entity.setLink("https://scholar.google.com/test");
            entity.setVersion(1);

            Map<String, Object> result = service.toResearchActivityMap(entity, false);

            assertThat(result).containsEntry("hIndex", "5");
            assertThat(result).containsEntry("scientificWorkCount", "10");
            assertThat(result).containsEntry("referenceCount", "25");
            assertThat(result).containsEntry("link", "https://scholar.google.com/test");
        }

        @Test
        @DisplayName("updateResearchActivityFromMap handles nested code objects")
        void updateFromMap_handlesNestedCode() {
            ResearchActivity entity = new ResearchActivity();
            Map<String, Object> map = new HashMap<>();
            map.put("university", Map.of("code", "401"));
            map.put("educationYear", Map.of("code", "2024"));

            service.updateResearchActivityFromMap(entity, map);

            assertThat(entity.getUniversity()).isEqualTo("401");
            assertThat(entity.getEducationYear()).isEqualTo("2024");
        }
    }

    // ====================================================================
    //  ProjectExecutor
    // ====================================================================
    @Nested
    @DisplayName("ProjectExecutor")
    class ProjectExecutorTests {

        @Test
        @DisplayName("toProjectExecutorMap formats dates correctly")
        void toMap_formatsDates() {
            ProjectExecutor entity = new ProjectExecutor();
            entity.setId(UUID.randomUUID());
            entity.setStartDate(LocalDate.of(2024, 1, 15));
            entity.setEndDate(LocalDate.of(2024, 12, 31));
            entity.setActive(true);
            entity.setVersion(1);

            Map<String, Object> result = service.toProjectExecutorMap(entity, false);

            assertThat(result).containsEntry("startDate", "2024-01-15");
            assertThat(result).containsEntry("endDate", "2024-12-31");
        }

        @Test
        @DisplayName("updateProjectExecutorFromMap handles nested project UUID")
        void updateFromMap_handlesNestedProject() {
            ProjectExecutor entity = new ProjectExecutor();
            UUID projectId = UUID.randomUUID();
            Map<String, Object> map = new HashMap<>();
            map.put("project", Map.of("id", projectId.toString()));
            map.put("outsider", "Test");
            map.put("startDate", "2024-01-01");

            service.updateProjectExecutorFromMap(entity, map);

            assertThat(entity.getProject()).isEqualTo(projectId);
            assertThat(entity.getOutsider()).isEqualTo("Test");
            assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        }
    }

    // ====================================================================
    //  Project
    // ====================================================================
    @Nested
    @DisplayName("Project")
    class ProjectTests {

        @Test
        @DisplayName("toProjectMap returns full field set with dates")
        void toMap_returnsFullFieldSet() {
            Project entity = new Project();
            entity.setId(UUID.randomUUID());
            entity.setName("Test Project");
            entity.setProjectNumber("PRJ-001");
            entity.setContractNumber("SH-001");
            entity.setContractDate(LocalDate.of(2024, 1, 15));
            entity.setStartDate(LocalDate.of(2024, 2, 1));
            entity.setEndDate(LocalDate.of(2025, 1, 31));
            entity.setActive(true);
            entity.setVersion(1);

            Map<String, Object> result = service.toProjectMap(entity, false);

            assertThat(result).containsEntry("_entityName", "hemishe_EProject");
            assertThat(result).containsEntry("_instanceName", "Test Project");
            assertThat(result).containsEntry("contractDate", "2024-01-15");
            assertThat(result).containsEntry("startDate", "2024-02-01");
            assertThat(result).containsEntry("endDate", "2025-01-31");
            assertThat(result).containsEntry("projectNumber", "PRJ-001");
        }

        @Test
        @DisplayName("updateProjectFromMap handles nested objects")
        void updateFromMap_handlesNestedObjects() {
            Project entity = new Project();
            Map<String, Object> map = new HashMap<>();
            map.put("name", "Updated Project");
            map.put("university", Map.of("id", "401"));
            map.put("department", Map.of("id", "100"));
            map.put("contractDate", "2024-06-01");

            service.updateProjectFromMap(entity, map);

            assertThat(entity.getName()).isEqualTo("Updated Project");
            assertThat(entity.getUniversity()).isEqualTo("401");
            assertThat(entity.getDepartment()).isEqualTo("100");
            assertThat(entity.getContractDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        }

        @Test
        @DisplayName("toProjectMinimalMap returns only 3 fields")
        void toMinimalMap_returnsOnlyThreeFields() {
            Project entity = new Project();
            entity.setId(UUID.randomUUID());
            entity.setName("Test");

            Map<String, Object> result = service.toProjectMinimalMap(entity);

            assertThat(result).hasSize(3);
            assertThat(result).containsKeys("_entityName", "_instanceName", "id");
        }
    }
}
