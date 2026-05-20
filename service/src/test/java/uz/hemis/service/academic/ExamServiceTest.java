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
import uz.hemis.common.dto.academic.ExamDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.student.Exam;
import uz.hemis.domain.repository.ExamRepository;
import uz.hemis.service.academic.mapper.ExamMapper;
import uz.hemis.service.security.TenantGuard;

import java.time.LocalDate;
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
@DisplayName("ExamService — BOLA + mass-assignment")
class ExamServiceTest {

    @Mock private ExamRepository repository;
    @Mock private ExamMapper mapper;
    @Mock private TenantGuard tenantGuard;

    @InjectMocks
    private ExamService service;

    private UUID id;
    private Exam entity;
    private ExamDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Exam();
        entity.setId(id);
        entity.setUniversity("337");
        dto = new ExamDto();
        dto.setId(id);
        dto.setUniversity("337");
    }

    @Test
    void create_callsTenantGuard() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.create(dto);

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void create_nullUniversity_skipsTenantGuard() {
        dto.setUniversity(null);
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.create(dto);

        verify(tenantGuard, never()).verifyOwnershipOrAdmin(any());
    }

    @Test
    void findById_callsTenantGuard() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        service.findById(id);

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
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
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findByCourse_pageable() {
        UUID courseId = UUID.randomUUID();
        when(repository.findByCourse(courseId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByCourse(courseId, PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findByGroup_pageable() {
        UUID groupId = UUID.randomUUID();
        when(repository.findByGroup(groupId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByGroup(groupId, PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findByTeacher_pageable() {
        UUID teacherId = UUID.randomUUID();
        when(repository.findByTeacher(teacherId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByTeacher(teacherId, PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findPublishedExams_pageable() {
        when(repository.findPublishedExams(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findPublishedExams(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findByGroupAndDate_listResult() {
        UUID groupId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 20);
        when(repository.findByGroupAndDate(groupId, date)).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByGroupAndDate(groupId, date)).containsExactly(dto);
    }

    @Test
    void countByCourse_returnsValue() {
        UUID courseId = UUID.randomUUID();
        when(repository.countByCourse(courseId)).thenReturn(42L);

        assertThat(service.countByCourse(courseId)).isEqualTo(42L);
    }

    @Test
    void update_massAssignment_universityPinned() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        ExamDto patch = new ExamDto();
        patch.setUniversity("999");

        service.update(id, patch);

        // Original university restored after mapper
        assertThat(entity.getUniversity()).isEqualTo("337");
    }

    @Test
    void update_notFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_setsDeleteTs() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.softDelete(id);

        assertThat(entity.getDeleteTs()).isNotNull();
        verify(repository).save(entity);
    }
}
