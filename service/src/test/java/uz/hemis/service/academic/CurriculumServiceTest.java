package uz.hemis.service.academic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.dto.academic.CurriculumDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.academic.Curriculum;
import uz.hemis.domain.repository.CurriculumRepository;
import uz.hemis.domain.repository.SpecialtyRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.academic.mapper.CurriculumMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurriculumService")
class CurriculumServiceTest {

    @Mock private CurriculumRepository repository;
    @Mock private CurriculumMapper mapper;
    @Mock private UniversityRepository universityRepository;
    @Mock private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private CurriculumService service;

    private UUID id;
    private Curriculum entity;
    private CurriculumDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Curriculum();
        entity.setId(id);
        entity.setCode("CURR-001");
        dto = new CurriculumDto();
        dto.setId(id);
        dto.setCode("CURR-001");
        dto.setName("Mathematics");
        dto.setUniversity("337");

        lenient().when(universityRepository.existsByCode("337")).thenReturn(true);
    }

    @Test
    void create_happyPath() {
        when(repository.existsByCode("CURR-001")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.create(dto)).isEqualTo(dto);
        verify(repository).save(entity);
    }

    @Test
    void create_codeRequired_throws() {
        dto.setCode(null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_blankCode_throws() {
        dto.setCode("   ");

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_duplicateCode_throws() {
        when(repository.existsByCode("CURR-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_nameRequired_throws() {
        dto.setName(null);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_universityNotFound_throws() {
        dto.setUniversity("999");
        when(universityRepository.existsByCode("999")).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_specialtyNotFound_throws() {
        UUID specId = UUID.randomUUID();
        dto.setSpecialty(specId);
        when(specialtyRepository.existsById(specId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void findById_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByCode_found() {
        when(repository.findByCode("CURR-001")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByCode("CURR-001")).isEqualTo(dto);
    }

    @Test
    void findByCode_notFound() {
        when(repository.findByCode("X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("X"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_pageable() {
        when(repository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findByUniversity_pageable() {
        when(repository.findByUniversity("337", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByUniversity("337", PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findAllByUniversity_list() {
        when(repository.findAllByUniversity("337")).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAllByUniversity("337")).containsExactly(dto);
    }

    @Test
    void findActive_pageable() {
        when(repository.findByActiveTrue(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findActive(PageRequest.of(0, 10))).hasSize(1);
    }
}
