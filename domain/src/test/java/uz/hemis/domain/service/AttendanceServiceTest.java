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
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.AttendanceDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Attendance;
import uz.hemis.domain.mapper.AttendanceMapper;
import uz.hemis.domain.repository.AttendanceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @InjectMocks
    private AttendanceService attendanceService;

    private Attendance attendance;
    private AttendanceDto attendanceDto;
    private UUID attendanceId;
    private UUID studentId;
    private UUID courseId;
    private UUID groupId;
    private LocalDate attendanceDate;

    @BeforeEach
    void setUp() {
        attendanceId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        attendanceDate = LocalDate.of(2024, 10, 26);

        attendance = new Attendance();
        attendance.setId(attendanceId);
        attendance.setStudent(studentId);
        attendance.setCourse(courseId);
        attendance.setGroup(groupId);
        attendance.setAttendanceDate(attendanceDate);
        attendance.setAttendanceType("PRESENT");
        attendance.setIsPresent(true);

        attendanceDto = new AttendanceDto();
        attendanceDto.setId(attendanceId);
        attendanceDto.setStudent(studentId);
        attendanceDto.setCourse(courseId);
        attendanceDto.setGroup(groupId);
        attendanceDto.setAttendanceDate(attendanceDate);
        attendanceDto.setAttendanceType("PRESENT");
        attendanceDto.setIsPresent(true);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedAttendance() {
        when(attendanceMapper.toEntity(attendanceDto)).thenReturn(attendance);
        when(attendanceRepository.save(attendance)).thenReturn(attendance);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        AttendanceDto result = attendanceService.create(attendanceDto);

        assertNotNull(result);
        assertEquals(attendanceId, result.getId());
        assertEquals(studentId, result.getStudent());
        assertEquals("PRESENT", result.getAttendanceType());
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void findById_WithExistingId_ShouldReturnAttendance() {
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        AttendanceDto result = attendanceService.findById(attendanceId);

        assertNotNull(result);
        assertEquals(attendanceId, result.getId());
        verify(attendanceRepository).findById(attendanceId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attendanceService.findById(attendanceId));
        verify(attendanceRepository).findById(attendanceId);
    }

    @Test
    void findAll_ShouldReturnPageOfAttendances() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Attendance> attendancePage = new PageImpl<>(Arrays.asList(attendance));
        when(attendanceRepository.findAll(pageable)).thenReturn(attendancePage);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        Page<AttendanceDto> result = attendanceService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(attendanceRepository).findAll(pageable);
    }

    @Test
    void findByStudent_ShouldReturnPageOfAttendances() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Attendance> attendancePage = new PageImpl<>(Arrays.asList(attendance));
        when(attendanceRepository.findByStudent(studentId, pageable)).thenReturn(attendancePage);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        Page<AttendanceDto> result = attendanceService.findByStudent(studentId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(attendanceRepository).findByStudent(studentId, pageable);
    }

    @Test
    void findByCourse_ShouldReturnPageOfAttendances() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Attendance> attendancePage = new PageImpl<>(Arrays.asList(attendance));
        when(attendanceRepository.findByCourse(courseId, pageable)).thenReturn(attendancePage);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        Page<AttendanceDto> result = attendanceService.findByCourse(courseId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(attendanceRepository).findByCourse(courseId, pageable);
    }

    @Test
    void findByGroup_ShouldReturnPageOfAttendances() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Attendance> attendancePage = new PageImpl<>(Arrays.asList(attendance));
        when(attendanceRepository.findByGroup(groupId, pageable)).thenReturn(attendancePage);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        Page<AttendanceDto> result = attendanceService.findByGroup(groupId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(attendanceRepository).findByGroup(groupId, pageable);
    }

    @Test
    void findByGroupAndDate_ShouldReturnListOfAttendances() {
        List<Attendance> attendances = Arrays.asList(attendance);
        when(attendanceRepository.findByGroupAndDate(groupId, attendanceDate)).thenReturn(attendances);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        List<AttendanceDto> result = attendanceService.findByGroupAndDate(groupId, attendanceDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(attendanceRepository).findByGroupAndDate(groupId, attendanceDate);
    }

    @Test
    void countPresentByStudent_ShouldReturnCount() {
        when(attendanceRepository.countPresentByStudent(studentId)).thenReturn(15L);

        long result = attendanceService.countPresentByStudent(studentId);

        assertEquals(15L, result);
        verify(attendanceRepository).countPresentByStudent(studentId);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedAttendance() {
        attendanceDto.setAttendanceType("ABSENT");
        attendanceDto.setIsPresent(false);

        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(attendance)).thenReturn(attendance);
        when(attendanceMapper.toDto(attendance)).thenReturn(attendanceDto);

        AttendanceDto result = attendanceService.update(attendanceId, attendanceDto);

        assertNotNull(result);
        verify(attendanceMapper).updateEntityFromDto(attendanceDto, attendance);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attendanceService.update(attendanceId, attendanceDto));
        verify(attendanceRepository).findById(attendanceId);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(attendance)).thenReturn(attendance);

        attendanceService.softDelete(attendanceId);

        assertNotNull(attendance.getDeleteTs());
        verify(attendanceRepository).findById(attendanceId);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void softDelete_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attendanceService.softDelete(attendanceId));
        verify(attendanceRepository).findById(attendanceId);
        verify(attendanceRepository, never()).save(any());
    }
}
