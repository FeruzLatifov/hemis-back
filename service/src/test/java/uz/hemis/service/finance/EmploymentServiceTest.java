package uz.hemis.service.finance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.dto.finance.EmploymentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.employee.Employment;
import uz.hemis.domain.repository.EmploymentRepository;
import uz.hemis.service.finance.mapper.EmploymentMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmploymentService")
class EmploymentServiceTest {

    @Mock private EmploymentRepository repository;
    @Mock private EmploymentMapper mapper;

    @InjectMocks
    private EmploymentService service;

    private UUID id;
    private Employment entity;
    private EmploymentDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Employment();
        entity.setId(id);
        dto = new EmploymentDto();
        dto.setId(id);
        dto.setEmploymentCode("EMP-001");
    }

    @Test
    void findById_found() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findById(id)).isEqualTo(dto);
    }

    @Test
    void findById_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByCode_found() {
        when(repository.findByEmploymentCode("EMP-001")).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByCode("EMP-001")).isEqualTo(dto);
    }

    @Test
    void findByCode_notFound() {
        when(repository.findByEmploymentCode("X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("X"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_pageable() {
        Page<Employment> page = new PageImpl<>(List.of(entity));
        when(repository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void countActiveByUniversity() {
        when(repository.countActiveByUniversity("337")).thenReturn(42L);

        assertThat(service.countActiveByUniversity("337")).isEqualTo(42L);
    }

    @Test
    void countSpecialtyRelatedByUniversity() {
        when(repository.countSpecialtyRelatedByUniversity("337")).thenReturn(15L);

        assertThat(service.countSpecialtyRelatedByUniversity("337")).isEqualTo(15L);
    }

    @Test
    void create_happyPath() {
        when(repository.existsByEmploymentCode("EMP-001")).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.create(dto)).isEqualTo(dto);
        verify(repository).save(entity);
    }

    @Test
    void create_duplicateCode_throws() {
        when(repository.existsByEmploymentCode("EMP-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_happy() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.update(id, dto)).isEqualTo(dto);
        verify(mapper).updateEntityFromDto(dto, entity);
    }

    @Test
    void update_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_active() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.softDelete(id);

        assertThat(entity.getDeleteTs()).isNotNull();
        verify(repository).save(entity);
    }

    @Test
    void softDelete_alreadyDeleted_noSave() {
        entity.setDeleteTs(java.time.LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.softDelete(id);

        verify(repository, never()).save(entity);
    }
}
