package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.AttendanceDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Attendance;
import uz.hemis.domain.mapper.AttendanceMapper;
import uz.hemis.domain.repository.AttendanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public AttendanceDto create(AttendanceDto dto) {
        Attendance attendance = attendanceMapper.toEntity(dto);
        return attendanceMapper.toDto(attendanceRepository.save(attendance));
    }

    public AttendanceDto findById(UUID id) {
        return attendanceRepository.findById(id)
                .map(attendanceMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found: " + id));
    }

    public Page<AttendanceDto> findAll(Pageable pageable) {
        return attendanceRepository.findAll(pageable).map(attendanceMapper::toDto);
    }

    public Page<AttendanceDto> findByStudent(UUID studentId, Pageable pageable) {
        return attendanceRepository.findByStudent(studentId, pageable).map(attendanceMapper::toDto);
    }

    public Page<AttendanceDto> findByCourse(UUID courseId, Pageable pageable) {
        return attendanceRepository.findByCourse(courseId, pageable).map(attendanceMapper::toDto);
    }

    public Page<AttendanceDto> findByGroup(UUID groupId, Pageable pageable) {
        return attendanceRepository.findByGroup(groupId, pageable).map(attendanceMapper::toDto);
    }

    public List<AttendanceDto> findByGroupAndDate(UUID groupId, LocalDate date) {
        return attendanceRepository.findByGroupAndDate(groupId, date)
                .stream().map(attendanceMapper::toDto).collect(Collectors.toList());
    }

    public long countPresentByStudent(UUID studentId) {
        return attendanceRepository.countPresentByStudent(studentId);
    }

    @Transactional
    public AttendanceDto update(UUID id, AttendanceDto dto) {
        Attendance existing = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found: " + id));
        attendanceMapper.updateEntityFromDto(dto, existing);
        return attendanceMapper.toDto(attendanceRepository.save(existing));
    }

    @Transactional
    public void softDelete(UUID id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found: " + id));
        attendance.setDeleteTs(LocalDateTime.now());
        attendanceRepository.save(attendance);
    }
}
