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
import uz.hemis.common.dto.academic.GradeDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;
import uz.hemis.domain.entity.student.Grade;
import uz.hemis.domain.repository.CourseRepository;
import uz.hemis.domain.repository.GradeRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.service.academic.mapper.GradeMapper;
import uz.hemis.service.security.TenantGuard;

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
@DisplayName("GradeService — BOLA defense + finalize lock + mass-assignment")
class GradeServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private GradeMapper gradeMapper;
    @Mock private CourseRepository courseRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private TenantGuard tenantGuard;

    @InjectMocks
    private GradeService service;

    private UUID id;
    private Grade entity;
    private GradeDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Grade();
        entity.setId(id);
        entity.setUniversity("337");
        dto = new GradeDto();
        dto.setId(id);
        dto.setUniversity("337");

        // Validation defaults: course + university exists
        lenient().when(courseRepository.existsById(any(UUID.class))).thenReturn(true);
        lenient().when(universityRepository.existsByCode("337")).thenReturn(true);
    }

    @Test
    void create_happyPath() {
        when(gradeMapper.toEntity(dto)).thenReturn(entity);
        when(gradeRepository.save(entity)).thenReturn(entity);
        when(gradeMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.create(dto)).isEqualTo(dto);
        verify(tenantGuard).verifyOwnershipOrAdmin("337");
        verify(gradeRepository).save(entity);
    }

    @Test
    void create_courseNotFound_throws() {
        UUID courseId = UUID.randomUUID();
        dto.setCourse(courseId);
        when(courseRepository.existsById(courseId)).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void create_universityNotFound_throws() {
        dto.setUniversity("999");
        when(universityRepository.existsByCode("999")).thenReturn(false);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_nullUniversity_skipsTenantGuard() {
        dto.setUniversity(null);
        when(gradeMapper.toEntity(dto)).thenReturn(entity);
        when(gradeRepository.save(entity)).thenReturn(entity);
        when(gradeMapper.toDto(entity)).thenReturn(dto);

        service.create(dto);

        verify(tenantGuard, never()).verifyOwnershipOrAdmin(any());
    }

    @Test
    void findById_callsTenantGuard() {
        when(gradeRepository.findById(id)).thenReturn(Optional.of(entity));
        when(gradeMapper.toDto(entity)).thenReturn(dto);

        service.findById(id);

        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }

    @Test
    void findById_notFound() {
        when(gradeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_paginated() {
        when(gradeRepository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(gradeMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void calculateGPA_delegate() {
        UUID studentId = UUID.randomUUID();
        when(gradeRepository.calculateGPA(studentId)).thenReturn(4.5);

        assertThat(service.calculateGPA(studentId)).isEqualTo(4.5);
    }

    @Test
    void update_finalized_throws() {
        entity.setIsFinalized(true);
        when(gradeRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("finalized");

        verify(gradeRepository, never()).save(any());
    }

    @Test
    void update_massAssignment_universityPinned() {
        when(gradeRepository.findById(id)).thenReturn(Optional.of(entity));
        when(gradeRepository.save(entity)).thenReturn(entity);
        when(gradeMapper.toDto(entity)).thenReturn(dto);

        GradeDto patch = new GradeDto();
        patch.setUniversity("999"); // cross-tenant relocate attempt

        service.update(id, patch);

        // Service pins to original university
        assertThat(entity.getUniversity()).isEqualTo("337");
    }

    @Test
    void update_notFound() {
        when(gradeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void softDelete_setsDeleteTs() {
        when(gradeRepository.findById(id)).thenReturn(Optional.of(entity));

        service.softDelete(id);

        assertThat(entity.getDeleteTs()).isNotNull();
        verify(gradeRepository).save(entity);
        verify(tenantGuard).verifyOwnershipOrAdmin("337");
    }
}
