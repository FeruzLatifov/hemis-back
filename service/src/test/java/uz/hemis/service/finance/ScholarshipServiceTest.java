package uz.hemis.service.finance;

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
import uz.hemis.common.dto.finance.ScholarshipDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.finance.Scholarship;
import uz.hemis.domain.repository.ScholarshipRepository;
import uz.hemis.service.finance.mapper.ScholarshipMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScholarshipService")
class ScholarshipServiceTest {

    @Mock private ScholarshipRepository repository;
    @Mock private ScholarshipMapper mapper;

    @InjectMocks
    private ScholarshipService service;

    private UUID id;
    private Scholarship entity;
    private ScholarshipDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Scholarship();
        entity.setId(id);
        dto = new ScholarshipDto();
        dto.setId(id);
    }

    @Nested
    @DisplayName("Read")
    class Read {

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
        void findAll_pageable() {
            Page<Scholarship> page = new PageImpl<>(List.of(entity));
            when(repository.findAll(PageRequest.of(0, 10))).thenReturn(page);
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
        }

        @Test
        void findByStudent() {
            UUID studentId = UUID.randomUUID();
            when(repository.findByStudent(studentId)).thenReturn(List.of(entity));
            when(mapper.toDtoList(List.of(entity))).thenReturn(List.of(dto));

            assertThat(service.findByStudent(studentId)).containsExactly(dto);
        }

        @Test
        void findByUniversity() {
            when(repository.findByUniversity("337")).thenReturn(List.of(entity));
            when(mapper.toDtoList(List.of(entity))).thenReturn(List.of(dto));

            assertThat(service.findByUniversity("337")).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("Write")
    class Write {

        @Test
        void create_savesAndReturnsDto() {
            when(mapper.toEntity(dto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.create(dto)).isEqualTo(dto);
            verify(repository).save(entity);
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

        @Test
        void softDelete_notFound() {
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.softDelete(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
