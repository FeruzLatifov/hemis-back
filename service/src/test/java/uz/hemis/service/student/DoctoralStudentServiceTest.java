package uz.hemis.service.student;

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
import uz.hemis.common.dto.student.DoctoralStudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.student.DoctoralStudent;
import uz.hemis.domain.repository.DoctoralStudentRepository;
import uz.hemis.service.student.mapper.DoctoralStudentMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DoctoralStudentService} unit testlar — CRUD + cache alias evict + uniqueness validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DoctoralStudentService")
class DoctoralStudentServiceTest {

    @Mock private DoctoralStudentRepository repository;
    @Mock private DoctoralStudentMapper mapper;

    @InjectMocks
    private DoctoralStudentService service;

    private UUID id;
    private DoctoralStudent entity;
    private DoctoralStudentDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new DoctoralStudent();
        entity.setId(id);
        entity.setPassportPin("12345678901234");
        entity.setStudentIdNumber("S2026001");
        dto = new DoctoralStudentDto();
        dto.setId(id);
        dto.setPassportPin("12345678901234");
        dto.setStudentIdNumber("S2026001");
    }

    @Nested
    @DisplayName("Read operations")
    class Read {

        @Test
        @DisplayName("findById — found")
        void findById_found() {
            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.findById(id)).isEqualTo(dto);
        }

        @Test
        @DisplayName("findById — not found → ResourceNotFoundException")
        void findById_notFound() {
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findByStudentIdNumber — found")
        void findByStudentIdNumber_found() {
            when(repository.findByStudentIdNumber("S2026001")).thenReturn(Optional.of(entity));
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.findByStudentIdNumber("S2026001")).isEqualTo(dto);
        }

        @Test
        @DisplayName("findByStudentIdNumber — not found throws")
        void findByStudentIdNumber_notFound() {
            when(repository.findByStudentIdNumber("X")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findByStudentIdNumber("X"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findByPassportPin — found")
        void findByPassportPin_found() {
            when(repository.findByPassportPin("12345678901234")).thenReturn(Optional.of(entity));
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.findByPassportPin("12345678901234")).isEqualTo(dto);
        }

        @Test
        @DisplayName("findAll — pageable")
        void findAll_pageable() {
            PageRequest req = PageRequest.of(0, 10);
            Page<DoctoralStudent> page = new PageImpl<>(List.of(entity));
            when(repository.findAll(req)).thenReturn(page);
            when(mapper.toDto(entity)).thenReturn(dto);

            Page<DoctoralStudentDto> result = service.findAll(req);

            assertThat(result.getContent()).containsExactly(dto);
        }

        @Test
        @DisplayName("findByUniversity — pageable")
        void findByUniversity() {
            PageRequest req = PageRequest.of(0, 10);
            when(repository.findByUniversity("337", req))
                    .thenReturn(new PageImpl<>(List.of(entity)));
            when(mapper.toDto(entity)).thenReturn(dto);

            Page<DoctoralStudentDto> result = service.findByUniversity("337", req);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("findActiveByUniversity — list")
        void findActive() {
            when(repository.findActiveByUniversity("337")).thenReturn(List.of(entity));
            when(mapper.toDtoList(List.of(entity))).thenReturn(List.of(dto));

            assertThat(service.findActiveByUniversity("337")).containsExactly(dto);
        }

        @Test
        @DisplayName("countActiveByUniversity — qaytariladi")
        void count() {
            when(repository.countActiveByUniversity("337")).thenReturn(42L);

            assertThat(service.countActiveByUniversity("337")).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("happy path — yangi DoctoralStudent saqlanadi")
        void create_happyPath() {
            when(repository.existsByPassportPin("12345678901234")).thenReturn(false);
            when(repository.existsByStudentIdNumber("S2026001")).thenReturn(false);
            when(mapper.toEntity(dto)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.create(dto)).isEqualTo(dto);
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("duplicate PASSPORT_PIN — ValidationException")
        void create_duplicatePinfl() {
            when(repository.existsByPassportPin("12345678901234")).thenReturn(true);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("passport PIN");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("duplicate STUDENT_ID — ValidationException")
        void create_duplicateStudentId() {
            when(repository.existsByPassportPin("12345678901234")).thenReturn(false);
            when(repository.existsByStudentIdNumber("S2026001")).thenReturn(true);

            assertThatThrownBy(() -> service.create(dto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("student ID");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("found — update muvaffaqiyatli")
        void update_happy() {
            when(repository.findById(id)).thenReturn(Optional.of(entity));
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDto(entity)).thenReturn(dto);

            assertThat(service.update(id, dto)).isEqualTo(dto);
            verify(mapper).updateEntityFromDto(dto, entity);
        }

        @Test
        @DisplayName("not found — ResourceNotFoundException")
        void update_notFound() {
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(id, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("softDelete()")
    class SoftDelete {

        @Test
        @DisplayName("Active entity — deleteTs set + saqlash")
        void softDelete_active() {
            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.softDelete(id);

            assertThat(entity.getDeleteTs()).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Already deleted — early return, save chaqirilmaydi")
        void softDelete_alreadyDeleted() {
            entity.setDeleteTs(java.time.LocalDateTime.now().minusDays(1));
            when(repository.findById(id)).thenReturn(Optional.of(entity));

            service.softDelete(id);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Not found — ResourceNotFoundException")
        void softDelete_notFound() {
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.softDelete(id))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Legacy entity access")
    class LegacyAccess {

        @Test
        @DisplayName("findEntityByPassportPin — Optional qaytaradi")
        void entityByPin() {
            when(repository.findByPassportPin("12345678901234")).thenReturn(Optional.of(entity));

            assertThat(service.findEntityByPassportPin("12345678901234")).contains(entity);
        }

        @Test
        @DisplayName("findEntityByPassportPin — empty Optional")
        void entityByPin_empty() {
            when(repository.findByPassportPin("X")).thenReturn(Optional.empty());

            assertThat(service.findEntityByPassportPin("X")).isEmpty();
        }
    }
}
