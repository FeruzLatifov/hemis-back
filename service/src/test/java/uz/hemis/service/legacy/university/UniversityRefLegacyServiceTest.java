package uz.hemis.service.legacy.university;

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
import org.springframework.data.domain.Pageable;
import uz.hemis.domain.entity.infrastructure.IctEquipment;
import uz.hemis.domain.entity.infrastructure.Laboratories;
import uz.hemis.domain.entity.university.UniversityAttachedSpeciality;
import uz.hemis.domain.repository.IctEquipmentRepository;
import uz.hemis.domain.repository.LaboratoriesRepository;
import uz.hemis.domain.repository.UniversityAttachedSpecialityRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityRefLegacyService Tests")
class UniversityRefLegacyServiceTest {

    @Mock
    private IctEquipmentRepository ictEquipmentRepository;

    @Mock
    private LaboratoriesRepository laboratoriesRepository;

    @Mock
    private UniversityAttachedSpecialityRepository universityAttachedSpecialityRepository;

    @InjectMocks
    private UniversityRefLegacyService service;

    @Nested
    @DisplayName("IctEquipment Tests")
    class IctEquipmentTests {

        private IctEquipment ictEquipment;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            ictEquipment = new IctEquipment();
            ictEquipment.setId(testId);
            ictEquipment.setUniversity("TATU");
            ictEquipment.setEducationYear("2023");
            ictEquipment.setRoomCount(50);
            ictEquipment.setValidProjectorCount(45);
            ictEquipment.setInvalidProjectorCount(5);
            ictEquipment.setTotalCount(100);
            ictEquipment.setTotalGrade(90);
        }

        @Test
        @DisplayName("findIctEquipmentById - topilganda")
        void findIctEquipmentById_found() {
            when(ictEquipmentRepository.findById(testId)).thenReturn(Optional.of(ictEquipment));

            Optional<IctEquipment> result = service.findIctEquipmentById(testId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(testId);
            verify(ictEquipmentRepository).findById(testId);
        }

        @Test
        @DisplayName("findAllIctEquipment - pageable")
        void findAllIctEquipment_pageable() {
            Page<IctEquipment> page = new PageImpl<>(List.of(ictEquipment));
            Pageable pageable = PageRequest.of(0, 10);
            when(ictEquipmentRepository.findAll(pageable)).thenReturn(page);

            Page<IctEquipment> result = service.findAllIctEquipment(pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(ictEquipmentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("toIctEquipmentMap - barcha maydonlar")
        void toIctEquipmentMap_allFields() {
            Map<String, Object> map = service.toIctEquipmentMap(ictEquipment, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RIctEquipment");
            assertThat((String) map.get("_instanceName")).contains("com.company.hemishe.entity.RIctEquipment-" + testId);
            assertThat(map.get("id")).isEqualTo(testId);
            assertThat(map.get("universityCode")).isEqualTo("TATU");
            assertThat(map.get("educationYearCode")).isEqualTo("2023");
            assertThat(map.get("roomCount")).isEqualTo(50);
            assertThat(map.get("validProjectorCount")).isEqualTo(45);
            assertThat(map.get("invalidProjectorCount")).isEqualTo(5);
            assertThat(map.get("totalCount")).isEqualTo(100);
            assertThat(map.get("totalGrade")).isEqualTo(90);
        }

        @Test
        @DisplayName("updateIctEquipmentFromMap")
        void updateIctEquipmentFromMap() {
            IctEquipment entity = new IctEquipment();
            Map<String, Object> data = new HashMap<>();
            data.put("university_code", "TDTU");
            data.put("education_year_code", "2024");
            data.put("room_count", 100);
            data.put("valid_projector_count", 90);

            service.updateIctEquipmentFromMap(entity, data);

            assertThat(entity.getUniversity()).isEqualTo("TDTU");
            assertThat(entity.getEducationYear()).isEqualTo("2024");
            assertThat(entity.getRoomCount()).isEqualTo(100);
            assertThat(entity.getValidProjectorCount()).isEqualTo(90);
        }

        @Test
        @DisplayName("saveIctEquipment")
        void saveIctEquipment() {
            when(ictEquipmentRepository.save(any())).thenReturn(ictEquipment);

            IctEquipment result = service.saveIctEquipment(ictEquipment);

            assertThat(result).isNotNull();
            verify(ictEquipmentRepository).save(ictEquipment);
        }
    }

    @Nested
    @DisplayName("Laboratories Tests")
    class LaboratoriesTests {

        private Laboratories laboratories;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            laboratories = new Laboratories();
            laboratories.setId(testId);
            laboratories.setUniversity("TATU");
            laboratories.setEducationYear("2023");
            laboratories.setSpecialityId("SPEC001");
            laboratories.setSpecialityCode("5330100");
            laboratories.setSpecialityName("Kompyuter injiniringi");
            laboratories.setStudentCount(200);
            laboratories.setValidLaboratoriesCount(10);
            laboratories.setValidWorkshopsCount(5);
            laboratories.setInvalidLaboratoriesCount(2);
            laboratories.setInvalidWorkshopsCount(1);
            laboratories.setTotalLaboratories(12);
            laboratories.setTotalWorkshops(6);
            laboratories.setTotalGrade(85);
        }

        @Test
        @DisplayName("findLaboratoriesById - topilganda")
        void findLaboratoriesById_found() {
            when(laboratoriesRepository.findById(testId)).thenReturn(Optional.of(laboratories));

            Optional<Laboratories> result = service.findLaboratoriesById(testId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(testId);
        }

        @Test
        @DisplayName("toLaboratoriesMap - barcha maydonlar")
        void toLaboratoriesMap_allFields() {
            Map<String, Object> map = service.toLaboratoriesMap(laboratories, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_RLaboratories");
            assertThat((String) map.get("_instanceName")).contains("com.company.hemishe.entity.RLaboratories-" + testId);
            assertThat(map.get("id")).isEqualTo(testId);
            assertThat(map.get("universityCode")).isEqualTo("TATU");
            assertThat(map.get("educationYearCode")).isEqualTo("2023");
            assertThat(map.get("specialityId")).isEqualTo("SPEC001");
            assertThat(map.get("specialityCode")).isEqualTo("5330100");
            assertThat(map.get("specialityName")).isEqualTo("Kompyuter injiniringi");
            assertThat(map.get("studentCount")).isEqualTo(200);
            assertThat(map.get("validLaboratoriesCount")).isEqualTo(10);
            assertThat(map.get("validWorkshopsCount")).isEqualTo(5);
            assertThat(map.get("totalLaboratories")).isEqualTo(12);
            assertThat(map.get("totalWorkshops")).isEqualTo(6);
            assertThat(map.get("totalGrade")).isEqualTo(85);
        }

        @Test
        @DisplayName("toLaboratoriesMap - specialityName null bo'lganda")
        void toLaboratoriesMap_nullSpecialityName() {
            laboratories.setSpecialityName(null);
            Map<String, Object> map = service.toLaboratoriesMap(laboratories, false);

            assertThat((String) map.get("_instanceName")).contains("[detached]");
        }

        @Test
        @DisplayName("updateLaboratoriesFromMap")
        void updateLaboratoriesFromMap() {
            Laboratories entity = new Laboratories();
            Map<String, Object> data = new HashMap<>();
            data.put("university_code", "TDTU");
            data.put("speciality_name", "Axborot texnologiyalari");
            data.put("student_count", 300);
            data.put("valid_laboratories_count", 15);

            service.updateLaboratoriesFromMap(entity, data);

            assertThat(entity.getUniversity()).isEqualTo("TDTU");
            assertThat(entity.getSpecialityName()).isEqualTo("Axborot texnologiyalari");
            assertThat(entity.getStudentCount()).isEqualTo(300);
            assertThat(entity.getValidLaboratoriesCount()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("UniversityAttachedSpeciality Tests")
    class UniversityAttachedSpecialityTests {

        private UniversityAttachedSpeciality attachedSpeciality;
        private UUID testId;

        @BeforeEach
        void setUp() {
            testId = UUID.randomUUID();
            attachedSpeciality = new UniversityAttachedSpeciality();
            attachedSpeciality.setId(testId);
            attachedSpeciality.setUniversity("TATU");
            attachedSpeciality.setEducationForm("11");
            attachedSpeciality.setEducationType("11");
            attachedSpeciality.setActive(true);
            attachedSpeciality.setSpecialityBachelor(UUID.randomUUID());
        }

        @Test
        @DisplayName("findUniversityAttachedSpecialityById - topilganda")
        void findById_found() {
            when(universityAttachedSpecialityRepository.findById(testId)).thenReturn(Optional.of(attachedSpeciality));

            Optional<UniversityAttachedSpeciality> result = service.findUniversityAttachedSpecialityById(testId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(testId);
        }

        @Test
        @DisplayName("toUniversityAttachedSpecialityMap - asosiy maydonlar")
        void toMap_basicFields() {
            Map<String, Object> map = service.toUniversityAttachedSpecialityMap(attachedSpeciality, false);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_EUniversityAttachedSpeciality");
            assertThat((String) map.get("_instanceName")).contains("com.company.hemishe.entity.EUniversityAttachedSpeciality");
            assertThat(map.get("id")).isEqualTo(testId.toString());
            assertThat(map.get("active")).isEqualTo(true);
            // OLD-HEMIS: educationType default view da qaytarilmaydi
            assertThat(map).doesNotContainKey("educationType");
        }

        @Test
        @DisplayName("toUniversityAttachedSpecialityMinimalMap")
        void toMinimalMap() {
            Map<String, Object> map = service.toUniversityAttachedSpecialityMinimalMap(attachedSpeciality);

            assertThat(map.get("_entityName")).isEqualTo("hemishe_EUniversityAttachedSpeciality");
            assertThat((String) map.get("_instanceName")).contains("[detached]");
            assertThat(map.get("id")).isEqualTo(testId.toString());
            assertThat(map).hasSize(3);
        }

        @Test
        @DisplayName("updateUniversityAttachedSpecialityFromMap - nested university")
        void updateFromMap_nestedUniversity() {
            UniversityAttachedSpeciality entity = new UniversityAttachedSpeciality();
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> universityNested = new HashMap<>();
            universityNested.put("code", "TDTU");
            data.put("university", universityNested);
            data.put("educationType", "12");
            data.put("active", true);

            service.updateUniversityAttachedSpecialityFromMap(entity, data);

            assertThat(entity.getUniversity()).isEqualTo("TDTU");
            assertThat(entity.getEducationType()).isEqualTo("12");
            assertThat(entity.getActive()).isTrue();
        }

        @Test
        @DisplayName("updateUniversityAttachedSpecialityFromMap - string university")
        void updateFromMap_stringUniversity() {
            UniversityAttachedSpeciality entity = new UniversityAttachedSpeciality();
            Map<String, Object> data = new HashMap<>();
            data.put("university", "TATU");
            data.put("educationForm", "11");

            service.updateUniversityAttachedSpecialityFromMap(entity, data);

            assertThat(entity.getUniversity()).isEqualTo("TATU");
            assertThat(entity.getEducationForm()).isEqualTo("11");
        }

        @Test
        @DisplayName("updateUniversityAttachedSpecialityFromMap - UUID fields")
        void updateFromMap_uuidFields() {
            UniversityAttachedSpeciality entity = new UniversityAttachedSpeciality();
            UUID bachelorId = UUID.randomUUID();
            UUID masterId = UUID.randomUUID();

            Map<String, Object> data = new HashMap<>();
            data.put("specialityBachelor", bachelorId.toString());

            Map<String, Object> masterNested = new HashMap<>();
            masterNested.put("id", masterId.toString());
            data.put("specialityMaster", masterNested);

            service.updateUniversityAttachedSpecialityFromMap(entity, data);

            assertThat(entity.getSpecialityBachelor()).isEqualTo(bachelorId);
            assertThat(entity.getSpecialityMaster()).isEqualTo(masterId);
        }

        @Test
        @DisplayName("saveUniversityAttachedSpeciality")
        void save() {
            when(universityAttachedSpecialityRepository.save(any())).thenReturn(attachedSpeciality);

            UniversityAttachedSpeciality result = service.saveUniversityAttachedSpeciality(attachedSpeciality);

            assertThat(result).isNotNull();
            verify(universityAttachedSpecialityRepository).save(attachedSpeciality);
        }
    }
}
