package uz.hemis.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uz.hemis.common.dto.ScheduleDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Schedule;
import uz.hemis.domain.mapper.ScheduleMapper;
import uz.hemis.domain.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleRepository scheduleRepository;
    @Mock private ScheduleMapper scheduleMapper;
    @InjectMocks private ScheduleService scheduleService;

    private Schedule testSchedule;
    private ScheduleDto testScheduleDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testSchedule = new Schedule();
        testSchedule.setId(testId);
        testSchedule.setScheduleDate(LocalDate.now());
        testSchedule.setStartTime(LocalTime.of(9, 0));
        testSchedule.setEndTime(LocalTime.of(10, 30));
        testSchedule.setActive(true);

        testScheduleDto = new ScheduleDto();
        testScheduleDto.setId(testId);
        testScheduleDto.setScheduleDate(LocalDate.now());
        testScheduleDto.setStartTime(LocalTime.of(9, 0));
        testScheduleDto.setEndTime(LocalTime.of(10, 30));
        testScheduleDto.setActive(true);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedSchedule() {
        when(scheduleMapper.toEntity(testScheduleDto)).thenReturn(testSchedule);
        when(scheduleRepository.save(testSchedule)).thenReturn(testSchedule);
        when(scheduleMapper.toDto(testSchedule)).thenReturn(testScheduleDto);

        ScheduleDto result = scheduleService.create(testScheduleDto);

        assertThat(result).isNotNull();
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void findById_WithExistingId_ShouldReturnSchedule() {
        when(scheduleRepository.findById(testId)).thenReturn(Optional.of(testSchedule));
        when(scheduleMapper.toDto(testSchedule)).thenReturn(testScheduleDto);

        ScheduleDto result = scheduleService.findById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(scheduleRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.findById(testId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnPageOfSchedules() {
        Page<Schedule> page = new PageImpl<>(Arrays.asList(testSchedule));
        when(scheduleRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(scheduleMapper.toDto(testSchedule)).thenReturn(testScheduleDto);

        Page<ScheduleDto> result = scheduleService.findAll(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByGroupAndDate_ShouldReturnListOfSchedules() {
        UUID groupId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        when(scheduleRepository.findByGroupAndDate(groupId, date)).thenReturn(Arrays.asList(testSchedule));
        when(scheduleMapper.toDto(testSchedule)).thenReturn(testScheduleDto);

        List<ScheduleDto> result = scheduleService.findByGroupAndDate(groupId, date);

        assertThat(result).hasSize(1);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedSchedule() {
        when(scheduleRepository.findById(testId)).thenReturn(Optional.of(testSchedule));
        when(scheduleRepository.save(testSchedule)).thenReturn(testSchedule);
        when(scheduleMapper.toDto(testSchedule)).thenReturn(testScheduleDto);

        ScheduleDto result = scheduleService.update(testId, new ScheduleDto());

        assertThat(result).isNotNull();
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(scheduleRepository.findById(testId)).thenReturn(Optional.of(testSchedule));

        scheduleService.softDelete(testId);

        assertThat(testSchedule.getDeleteTs()).isNotNull();
        verify(scheduleRepository).save(testSchedule);
    }

    @Test
    void countByGroup_ShouldReturnCount() {
        UUID groupId = UUID.randomUUID();
        when(scheduleRepository.countByGroup(groupId)).thenReturn(10L);

        long count = scheduleService.countByGroup(groupId);

        assertThat(count).isEqualTo(10L);
    }
}
