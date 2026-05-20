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
import uz.hemis.common.dto.academic.AttendanceDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.student.Attendance;
import uz.hemis.domain.repository.AttendanceRepository;
import uz.hemis.service.academic.mapper.AttendanceMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService")
class AttendanceServiceTest {

    @Mock private AttendanceRepository repository;
    @Mock private AttendanceMapper mapper;

    @InjectMocks
    private AttendanceService service;

    private UUID id;
    private Attendance entity;
    private AttendanceDto dto;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        entity = new Attendance();
        entity.setId(id);
        dto = new AttendanceDto();
        dto.setId(id);
    }

    @Test
    void create_savesAndReturnsDto() {
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.create(dto)).isEqualTo(dto);
        verify(repository).save(entity);
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
    void findAll_pageable() {
        when(repository.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(1);
    }

    @Test
    void findByStudent_pageable() {
        UUID studentId = UUID.randomUUID();
        when(repository.findByStudent(studentId, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByStudent(studentId, PageRequest.of(0, 10))).hasSize(1);
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
    void findByGroupAndDate_listResult() {
        UUID groupId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2026, 5, 20);
        when(repository.findByGroupAndDate(groupId, date)).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findByGroupAndDate(groupId, date)).containsExactly(dto);
    }

    @Test
    void countPresentByStudent() {
        UUID studentId = UUID.randomUUID();
        when(repository.countPresentByStudent(studentId)).thenReturn(42L);

        assertThat(service.countPresentByStudent(studentId)).isEqualTo(42L);
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
    void softDelete_setsDeleteTs() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.softDelete(id);

        assertThat(entity.getDeleteTs()).isNotNull();
        verify(repository).save(entity);
    }
}
