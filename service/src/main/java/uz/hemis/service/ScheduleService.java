package uz.hemis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.ScheduleDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Schedule;
import uz.hemis.domain.mapper.ScheduleMapper;
import uz.hemis.domain.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleMapper scheduleMapper;

    @Transactional
    public ScheduleDto create(ScheduleDto dto) {
        Schedule schedule = scheduleMapper.toEntity(dto);
        return scheduleMapper.toDto(scheduleRepository.save(schedule));
    }

    public ScheduleDto findById(UUID id) {
        return scheduleRepository.findById(id)
                .map(scheduleMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
    }

    public Page<ScheduleDto> findAll(Pageable pageable) {
        return scheduleRepository.findAll(pageable).map(scheduleMapper::toDto);
    }

    public Page<ScheduleDto> findByGroup(UUID groupId, Pageable pageable) {
        return scheduleRepository.findByGroup(groupId, pageable).map(scheduleMapper::toDto);
    }

    public Page<ScheduleDto> findByTeacher(UUID teacherId, Pageable pageable) {
        return scheduleRepository.findByTeacher(teacherId, pageable).map(scheduleMapper::toDto);
    }

    public List<ScheduleDto> findByGroupAndDate(UUID groupId, LocalDate date) {
        return scheduleRepository.findByGroupAndDate(groupId, date)
                .stream().map(scheduleMapper::toDto).collect(Collectors.toList());
    }

    public List<ScheduleDto> findByTeacherAndDate(UUID teacherId, LocalDate date) {
        return scheduleRepository.findByTeacherAndDate(teacherId, date)
                .stream().map(scheduleMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ScheduleDto update(UUID id, ScheduleDto dto) {
        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
        scheduleMapper.updateEntityFromDto(dto, existing);
        return scheduleMapper.toDto(scheduleRepository.save(existing));
    }

    @Transactional
    public void softDelete(UUID id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
        schedule.setDeleteTs(LocalDateTime.now());
        scheduleRepository.save(schedule);
    }

    public long countByGroup(UUID groupId) {
        return scheduleRepository.countByGroup(groupId);
    }
}
