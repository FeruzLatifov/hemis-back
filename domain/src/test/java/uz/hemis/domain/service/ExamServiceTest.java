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
import uz.hemis.common.dto.ExamDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.Exam;
import uz.hemis.domain.mapper.ExamMapper;
import uz.hemis.domain.repository.ExamRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamMapper examMapper;

    @InjectMocks
    private ExamService examService;

    private Exam exam;
    private ExamDto examDto;
    private UUID examId;
    private UUID courseId;
    private UUID groupId;
    private UUID teacherId;
    private LocalDate examDate;

    @BeforeEach
    void setUp() {
        examId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        examDate = LocalDate.of(2024, 10, 26);

        exam = new Exam();
        exam.setId(examId);
        exam.setExamName("Final Exam - Mathematics");
        exam.setCourse(courseId);
        exam.setGroup(groupId);
        exam.setTeacher(teacherId);
        exam.setExamDate(examDate);
        exam.setStartTime(LocalTime.of(9, 0));
        exam.setEndTime(LocalTime.of(11, 0));
        exam.setExamType("FINAL");
        exam.setMaxScore(100);
        exam.setPassingScore(55);
        exam.setIsPublished(true);

        examDto = new ExamDto();
        examDto.setId(examId);
        examDto.setExamName("Final Exam - Mathematics");
        examDto.setCourse(courseId);
        examDto.setGroup(groupId);
        examDto.setTeacher(teacherId);
        examDto.setExamDate(examDate);
        examDto.setStartTime(LocalTime.of(9, 0));
        examDto.setEndTime(LocalTime.of(11, 0));
        examDto.setExamType("FINAL");
        examDto.setMaxScore(100);
        examDto.setPassingScore(55);
        examDto.setIsPublished(true);
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedExam() {
        when(examMapper.toEntity(examDto)).thenReturn(exam);
        when(examRepository.save(exam)).thenReturn(exam);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        ExamDto result = examService.create(examDto);

        assertNotNull(result);
        assertEquals(examId, result.getId());
        assertEquals("Final Exam - Mathematics", result.getExamName());
        assertEquals("FINAL", result.getExamType());
        assertEquals(100, result.getMaxScore());
        verify(examRepository).save(exam);
    }

    @Test
    void findById_WithExistingId_ShouldReturnExam() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examMapper.toDto(exam)).thenReturn(examDto);

        ExamDto result = examService.findById(examId);

        assertNotNull(result);
        assertEquals(examId, result.getId());
        assertEquals("Final Exam - Mathematics", result.getExamName());
        verify(examRepository).findById(examId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.findById(examId));
        verify(examRepository).findById(examId);
    }

    @Test
    void findAll_ShouldReturnPageOfExams() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Exam> examPage = new PageImpl<>(Arrays.asList(exam));
        when(examRepository.findAll(pageable)).thenReturn(examPage);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        Page<ExamDto> result = examService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(examRepository).findAll(pageable);
    }

    @Test
    void findByCourse_ShouldReturnPageOfExams() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Exam> examPage = new PageImpl<>(Arrays.asList(exam));
        when(examRepository.findByCourse(courseId, pageable)).thenReturn(examPage);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        Page<ExamDto> result = examService.findByCourse(courseId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(examRepository).findByCourse(courseId, pageable);
    }

    @Test
    void findByGroup_ShouldReturnPageOfExams() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Exam> examPage = new PageImpl<>(Arrays.asList(exam));
        when(examRepository.findByGroup(groupId, pageable)).thenReturn(examPage);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        Page<ExamDto> result = examService.findByGroup(groupId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(examRepository).findByGroup(groupId, pageable);
    }

    @Test
    void findByTeacher_ShouldReturnPageOfExams() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Exam> examPage = new PageImpl<>(Arrays.asList(exam));
        when(examRepository.findByTeacher(teacherId, pageable)).thenReturn(examPage);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        Page<ExamDto> result = examService.findByTeacher(teacherId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(examRepository).findByTeacher(teacherId, pageable);
    }

    @Test
    void findPublishedExams_ShouldReturnOnlyPublishedExams() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Exam> examPage = new PageImpl<>(Arrays.asList(exam));
        when(examRepository.findPublishedExams(pageable)).thenReturn(examPage);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        Page<ExamDto> result = examService.findPublishedExams(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsPublished());
        verify(examRepository).findPublishedExams(pageable);
    }

    @Test
    void findByGroupAndDate_ShouldReturnListOfExams() {
        List<Exam> exams = Arrays.asList(exam);
        when(examRepository.findByGroupAndDate(groupId, examDate)).thenReturn(exams);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        List<ExamDto> result = examService.findByGroupAndDate(groupId, examDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(examDate, result.get(0).getExamDate());
        verify(examRepository).findByGroupAndDate(groupId, examDate);
    }

    @Test
    void countByCourse_ShouldReturnCount() {
        when(examRepository.countByCourse(courseId)).thenReturn(5L);

        long result = examService.countByCourse(courseId);

        assertEquals(5L, result);
        verify(examRepository).countByCourse(courseId);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedExam() {
        examDto.setExamName("Updated Exam Name");
        examDto.setMaxScore(120);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examRepository.save(exam)).thenReturn(exam);
        when(examMapper.toDto(exam)).thenReturn(examDto);

        ExamDto result = examService.update(examId, examDto);

        assertNotNull(result);
        verify(examMapper).updateEntityFromDto(examDto, exam);
        verify(examRepository).save(exam);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.update(examId, examDto));
        verify(examRepository).findById(examId);
        verify(examRepository, never()).save(any());
    }

    @Test
    void softDelete_WithExistingId_ShouldSetDeleteTimestamp() {
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examRepository.save(exam)).thenReturn(exam);

        examService.softDelete(examId);

        assertNotNull(exam.getDeleteTs());
        verify(examRepository).findById(examId);
        verify(examRepository).save(exam);
    }

    @Test
    void softDelete_WithNonExistingId_ShouldThrowResourceNotFoundException() {
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.softDelete(examId));
        verify(examRepository).findById(examId);
        verify(examRepository, never()).save(any());
    }
}
