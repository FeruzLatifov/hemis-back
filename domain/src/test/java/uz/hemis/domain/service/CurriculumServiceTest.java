package uz.hemis.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.CurriculumDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.Curriculum;
import uz.hemis.domain.mapper.CurriculumMapper;
import uz.hemis.domain.repository.CurriculumRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

    @Mock private CurriculumRepository curriculumRepository;
    @Mock private CurriculumMapper curriculumMapper;
    @Mock private UniversityRepository universityRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @InjectMocks private CurriculumService curriculumService;

    private Curriculum testCurriculum;
    private CurriculumDto testCurriculumDto;
    private UUID testId;
    private UUID testSpecialtyId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testSpecialtyId = UUID.randomUUID();

        testCurriculum = new Curriculum();
        testCurriculum.setId(testId);
        testCurriculum.setCode("CURR2024");
        testCurriculum.setName("Computer Science Curriculum 2024");
        testCurriculum.setUniversity("UNI001");
        testCurriculum.setSpecialty(testSpecialtyId);
        testCurriculum.setAcademicYear("2024/2025");
        testCurriculum.setActive(true);

        testCurriculumDto = new CurriculumDto();
        testCurriculumDto.setId(testId);
        testCurriculumDto.setCode("CURR2024");
        testCurriculumDto.setName("Computer Science Curriculum 2024");
        testCurriculumDto.setUniversity("UNI001");
        testCurriculumDto.setSpecialty(testSpecialtyId);
        testCurriculumDto.setAcademicYear("2024/2025");
        testCurriculumDto.setActive(true);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedCurriculum() {
        when(curriculumRepository.existsByCode("CURR2024")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(specialtyRepository.existsById(testSpecialtyId)).thenReturn(true);
        when(curriculumMapper.toEntity(testCurriculumDto)).thenReturn(testCurriculum);
        when(curriculumRepository.save(testCurriculum)).thenReturn(testCurriculum);
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        CurriculumDto result = curriculumService.create(testCurriculumDto);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CURR2024");
        verify(curriculumRepository).save(testCurriculum);
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowValidationException() {
        when(curriculumRepository.existsByCode("CURR2024")).thenReturn(true);

        assertThatThrownBy(() -> curriculumService.create(testCurriculumDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
        verify(curriculumRepository, never()).save(any());
    }

    @Test
    void create_WithNullCode_ShouldThrowValidationException() {
        testCurriculumDto.setCode(null);

        assertThatThrownBy(() -> curriculumService.create(testCurriculumDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("code is required");
    }

    @Test
    void create_WithNullName_ShouldThrowValidationException() {
        testCurriculumDto.setName(null);
        when(curriculumRepository.existsByCode("CURR2024")).thenReturn(false);

        assertThatThrownBy(() -> curriculumService.create(testCurriculumDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void create_WithNonExistentUniversity_ShouldThrowValidationException() {
        when(curriculumRepository.existsByCode("CURR2024")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(false);

        assertThatThrownBy(() -> curriculumService.create(testCurriculumDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("University")
                .hasMessageContaining("not found");
    }

    @Test
    void create_WithNonExistentSpecialty_ShouldThrowValidationException() {
        when(curriculumRepository.existsByCode("CURR2024")).thenReturn(false);
        when(universityRepository.existsByCode("UNI001")).thenReturn(true);
        when(specialtyRepository.existsById(testSpecialtyId)).thenReturn(false);

        assertThatThrownBy(() -> curriculumService.create(testCurriculumDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Specialty")
                .hasMessageContaining("not found");
    }

    @Test
    void findById_WithExistingId_ShouldReturnCurriculum() {
        when(curriculumRepository.findById(testId)).thenReturn(Optional.of(testCurriculum));
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        CurriculumDto result = curriculumService.findById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        verify(curriculumRepository).findById(testId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(curriculumRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curriculumService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Curriculum not found");
    }

    @Test
    void findByCode_WithExistingCode_ShouldReturnCurriculum() {
        when(curriculumRepository.findByCode("CURR2024")).thenReturn(Optional.of(testCurriculum));
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        CurriculumDto result = curriculumService.findByCode("CURR2024");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CURR2024");
    }

    @Test
    void findAll_ShouldReturnPageOfCurricula() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Curriculum> page = new PageImpl<>(Arrays.asList(testCurriculum));

        when(curriculumRepository.findAll(pageable)).thenReturn(page);
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        Page<CurriculumDto> result = curriculumService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByUniversity_ShouldReturnPageOfCurricula() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Curriculum> page = new PageImpl<>(Arrays.asList(testCurriculum));

        when(curriculumRepository.findByUniversity("UNI001", pageable)).thenReturn(page);
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        Page<CurriculumDto> result = curriculumService.findByUniversity("UNI001", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAllByUniversity_ShouldReturnListOfCurricula() {
        when(curriculumRepository.findAllByUniversity("UNI001")).thenReturn(Arrays.asList(testCurriculum));
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        List<CurriculumDto> result = curriculumService.findAllByUniversity("UNI001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("CURR2024");
    }

    @Test
    void findBySpecialty_ShouldReturnPageOfCurricula() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Curriculum> page = new PageImpl<>(Arrays.asList(testCurriculum));

        when(curriculumRepository.findBySpecialty(testSpecialtyId, pageable)).thenReturn(page);
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        Page<CurriculumDto> result = curriculumService.findBySpecialty(testSpecialtyId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAllBySpecialty_ShouldReturnListOfCurricula() {
        when(curriculumRepository.findAllBySpecialty(testSpecialtyId)).thenReturn(Arrays.asList(testCurriculum));
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        List<CurriculumDto> result = curriculumService.findAllBySpecialty(testSpecialtyId);

        assertThat(result).hasSize(1);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedCurriculum() {
        CurriculumDto updateDto = new CurriculumDto();
        updateDto.setName("Updated Curriculum Name");

        when(curriculumRepository.findById(testId)).thenReturn(Optional.of(testCurriculum));
        when(curriculumRepository.save(testCurriculum)).thenReturn(testCurriculum);
        when(curriculumMapper.toDto(testCurriculum)).thenReturn(testCurriculumDto);

        CurriculumDto result = curriculumService.update(testId, updateDto);

        assertThat(result).isNotNull();
        verify(curriculumMapper).updateEntityFromDto(updateDto, testCurriculum);
        verify(curriculumRepository).save(testCurriculum);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(curriculumRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curriculumService.update(testId, new CurriculumDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_WithDuplicateCode_ShouldThrowValidationException() {
        CurriculumDto updateDto = new CurriculumDto();
        updateDto.setCode("CURR2025");

        when(curriculumRepository.findById(testId)).thenReturn(Optional.of(testCurriculum));
        when(curriculumRepository.existsByCodeAndIdNot("CURR2025", testId)).thenReturn(true);

        assertThatThrownBy(() -> curriculumService.update(testId, updateDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(curriculumRepository.findById(testId)).thenReturn(Optional.of(testCurriculum));

        curriculumService.softDelete(testId);

        assertThat(testCurriculum.getDeleteTs()).isNotNull();
        verify(curriculumRepository).save(testCurriculum);
    }

    @Test
    void softDelete_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(curriculumRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> curriculumService.softDelete(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void countByUniversity_ShouldReturnCount() {
        when(curriculumRepository.countByUniversity("UNI001")).thenReturn(3L);

        long count = curriculumService.countByUniversity("UNI001");

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void countBySpecialty_ShouldReturnCount() {
        when(curriculumRepository.countBySpecialty(testSpecialtyId)).thenReturn(2L);

        long count = curriculumService.countBySpecialty(testSpecialtyId);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    void existsByCode_WhenExists_ShouldReturnTrue() {
        when(curriculumRepository.existsByCode("CURR2024")).thenReturn(true);

        boolean exists = curriculumService.existsByCode("CURR2024");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_WhenNotExists_ShouldReturnFalse() {
        when(curriculumRepository.existsByCode("INVALID")).thenReturn(false);

        boolean exists = curriculumService.existsByCode("INVALID");

        assertThat(exists).isFalse();
    }
}
