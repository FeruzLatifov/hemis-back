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
import uz.hemis.common.dto.academic.AcademicScoreDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.academic.AcademicScore;
import uz.hemis.domain.repository.AcademicScoreRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcademicScoreService")
class AcademicScoreServiceTest {

    @Mock private AcademicScoreRepository repository;

    @InjectMocks
    private AcademicScoreService service;

    private UUID id;
    private AcademicScore entity;
    private AcademicScoreDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new AcademicScore();
        entity.setId(id);
        entity.setUniversityCode("337");
        dto = AcademicScoreDto.builder()
                .id(id)
                .universityCode("337")
                .build();
    }

    @Test
    void findById_found() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        AcademicScoreDto result = service.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUniversityCode()).isEqualTo("337");
    }

    @Test
    void findById_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_pageable() {
        when(repository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void create_setsIdAndCreateTs() {
        when(repository.save(any(AcademicScore.class))).thenAnswer(inv -> inv.getArgument(0));

        AcademicScoreDto result = service.create(dto);

        assertThat(result.getUniversityCode()).isEqualTo("337");
        verify(repository).save(any(AcademicScore.class));
    }

    @Test
    void update_found_updateTsSet() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        service.update(id, dto);

        assertThat(entity.getUpdateTs()).isNotNull();
        verify(repository).save(entity);
    }

    @Test
    void update_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_delegatesWithSystemUser() {
        when(repository.existsById(id)).thenReturn(true);

        service.softDelete(id);

        verify(repository).softDelete(eq(id), anyString(), any(LocalDateTime.class));
    }

    @Test
    void softDelete_notFound_throws() {
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.softDelete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).softDelete(any(), any(), any());
    }

    @Test
    void update_partial_nullFieldsSkipped() {
        // Update with sparse DTO — only universityName set
        AcademicScoreDto sparse = AcademicScoreDto.builder()
                .universityName("New Name")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        service.update(id, sparse);

        // Existing universityCode preserved (null in DTO → skipped)
        assertThat(entity.getUniversityCode()).isEqualTo("337");
        // New universityName applied
        assertThat(entity.getUniversityName()).isEqualTo("New Name");
    }
}
