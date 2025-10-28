package uz.hemis.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uz.hemis.app.service.StudentService;
import uz.hemis.common.dto.StudentDto;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.common.exception.ValidationException;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for StudentController
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>Use @WebMvcTest for controller layer testing</li>
 *   <li>Mock StudentService with @MockBean</li>
 *   <li>Test HTTP requests/responses with MockMvc</li>
 *   <li>Verify JSON serialization/deserialization</li>
 *   <li>Test error handling via GlobalExceptionHandler</li>
 * </ul>
 *
 * <p><strong>Coverage:</strong></p>
 * <ul>
 *   <li>All REST endpoints</li>
 *   <li>Request/response formats (legacy JSON fields)</li>
 *   <li>HTTP status codes</li>
 *   <li>Validation and error responses</li>
 * </ul>
 *
 * @since 1.0.0
 */
@WebMvcTest(StudentController.class)
@DisplayName("StudentController Integration Tests")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private UUID testId;
    private StudentDto testStudentDto;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testStudentDto = new StudentDto();
        testStudentDto.setId(testId);
        testStudentDto.setCode("STU001");
        testStudentDto.setPinfl("12345678901234");
        testStudentDto.setUniversity("UNI001");
        testStudentDto.setFirstname("John");
        testStudentDto.setLastname("Doe");
        testStudentDto.setFathername("Smith");
    }

    // =====================================================
    // GET /app/rest/v2/students - List all students
    // =====================================================

    @Test
    @DisplayName("GET /students - should return paginated list of students")
    void getAllStudents_ShouldReturnPaginatedList() throws Exception {
        // Given
        Page<StudentDto> page = new PageImpl<>(
                Arrays.asList(testStudentDto),
                PageRequest.of(0, 20),
                1
        );
        when(studentService.findAll(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(testId.toString()))
                .andExpect(jsonPath("$.data.content[0].code").value("STU001"))
                .andExpect(jsonPath("$.data.content[0]._university").value("UNI001"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(studentService, times(1)).findAll(any());
    }

    // =====================================================
    // GET /app/rest/v2/students/{id} - Get by ID
    // =====================================================

    @Test
    @DisplayName("GET /students/{id} - should return student when found")
    void getStudentById_WhenFound_ShouldReturnStudent() throws Exception {
        // Given
        when(studentService.findById(testId)).thenReturn(testStudentDto);

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testId.toString()))
                .andExpect(jsonPath("$.data.code").value("STU001"))
                .andExpect(jsonPath("$.data._university").value("UNI001"))
                .andExpect(jsonPath("$.data.firstname").value("John"))
                .andExpect(jsonPath("$.data.lastname").value("Doe"));

        verify(studentService, times(1)).findById(testId);
    }

    @Test
    @DisplayName("GET /students/{id} - should return 404 when not found")
    void getStudentById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(studentService.findById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Student", "id", nonExistentId));

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").containsString("Student"))
                .andExpect(jsonPath("$.message").containsString("not found"));

        verify(studentService, times(1)).findById(nonExistentId);
    }

    // =====================================================
    // GET /app/rest/v2/students?university={code}
    // =====================================================

    @Test
    @DisplayName("GET /students?university={code} - should return filtered students")
    void getStudentsByUniversity_ShouldReturnFilteredList() throws Exception {
        // Given
        String universityCode = "UNI001";
        Page<StudentDto> page = new PageImpl<>(
                Arrays.asList(testStudentDto),
                PageRequest.of(0, 20),
                1
        );
        when(studentService.findByUniversity(eq(universityCode), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students")
                        .param("university", universityCode)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0]._university").value(universityCode));

        verify(studentService, times(1)).findByUniversity(eq(universityCode), any());
    }

    // =====================================================
    // POST /app/rest/v2/students - Create student
    // =====================================================

    @Test
    @DisplayName("POST /students - should create student and return 201")
    void createStudent_WithValidData_ShouldReturn201() throws Exception {
        // Given
        StudentDto createDto = new StudentDto();
        createDto.setCode("STU002");
        createDto.setPinfl("98765432109876");
        createDto.setUniversity("UNI001");
        createDto.setFirstname("Jane");
        createDto.setLastname("Smith");

        StudentDto createdDto = new StudentDto();
        createdDto.setId(UUID.randomUUID());
        createdDto.setCode("STU002");
        createdDto.setPinfl("98765432109876");
        createdDto.setUniversity("UNI001");
        createdDto.setFirstname("Jane");
        createdDto.setLastname("Smith");

        when(studentService.create(any(StudentDto.class))).thenReturn(createdDto);

        // When & Then
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.code").value("STU002"))
                .andExpect(jsonPath("$.data._university").value("UNI001"));

        verify(studentService, times(1)).create(any(StudentDto.class));
    }

    @Test
    @DisplayName("POST /students - should return 400 when PINFL already exists")
    void createStudent_WithDuplicatePinfl_ShouldReturn400() throws Exception {
        // Given
        StudentDto createDto = new StudentDto();
        createDto.setCode("STU002");
        createDto.setPinfl("12345678901234"); // Duplicate

        when(studentService.create(any(StudentDto.class)))
                .thenThrow(new ValidationException(
                        "Student with this PINFL already exists",
                        "pinfl",
                        "PINFL must be unique"
                ));

        // When & Then
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").containsString("PINFL"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("pinfl"))
                .andExpect(jsonPath("$.errors[0].message").value("PINFL must be unique"));

        verify(studentService, times(1)).create(any(StudentDto.class));
    }

    // =====================================================
    // PUT /app/rest/v2/students/{id} - Full update
    // =====================================================

    @Test
    @DisplayName("PUT /students/{id} - should update student successfully")
    void updateStudent_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        StudentDto updateDto = new StudentDto();
        updateDto.setFirstname("Jane");
        updateDto.setLastname("Smith");
        updateDto.setPinfl("12345678901234");

        StudentDto updatedDto = new StudentDto();
        updatedDto.setId(testId);
        updatedDto.setCode("STU001");
        updatedDto.setFirstname("Jane");
        updatedDto.setLastname("Smith");
        updatedDto.setPinfl("12345678901234");
        updatedDto.setUniversity("UNI001");

        when(studentService.update(eq(testId), any(StudentDto.class))).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/app/rest/v2/students/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testId.toString()))
                .andExpect(jsonPath("$.data.firstname").value("Jane"))
                .andExpect(jsonPath("$.data.lastname").value("Smith"));

        verify(studentService, times(1)).update(eq(testId), any(StudentDto.class));
    }

    @Test
    @DisplayName("PUT /students/{id} - should return 404 when student not found")
    void updateStudent_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        StudentDto updateDto = new StudentDto();
        updateDto.setFirstname("Jane");

        when(studentService.update(eq(nonExistentId), any(StudentDto.class)))
                .thenThrow(new ResourceNotFoundException("Student", "id", nonExistentId));

        // When & Then
        mockMvc.perform(put("/app/rest/v2/students/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(studentService, times(1)).update(eq(nonExistentId), any(StudentDto.class));
    }

    // =====================================================
    // PATCH /app/rest/v2/students/{id} - Partial update
    // =====================================================

    @Test
    @DisplayName("PATCH /students/{id} - should partially update student")
    void partialUpdateStudent_WithPartialData_ShouldReturnUpdated() throws Exception {
        // Given
        StudentDto partialDto = new StudentDto();
        partialDto.setFirstname("Jane");

        StudentDto updatedDto = new StudentDto();
        updatedDto.setId(testId);
        updatedDto.setCode("STU001");
        updatedDto.setFirstname("Jane"); // Updated
        updatedDto.setLastname("Doe"); // Unchanged
        updatedDto.setPinfl("12345678901234");
        updatedDto.setUniversity("UNI001");

        when(studentService.partialUpdate(eq(testId), any(StudentDto.class))).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(patch("/app/rest/v2/students/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testId.toString()))
                .andExpect(jsonPath("$.data.firstname").value("Jane"))
                .andExpect(jsonPath("$.data.lastname").value("Doe"));

        verify(studentService, times(1)).partialUpdate(eq(testId), any(StudentDto.class));
    }

    // =====================================================
    // Legacy JSON Field Names Verification
    // =====================================================

    @Test
    @DisplayName("Response should contain legacy JSON field names with underscores")
    void response_ShouldContainLegacyFieldNames() throws Exception {
        // Given
        testStudentDto.setStudentStatus("ACTIVE");
        testStudentDto.setFaculty("FAC001");
        testStudentDto.setSpecialty("SPEC001");

        when(studentService.findById(testId)).thenReturn(testStudentDto);

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data._university").exists()) // Underscore prefix!
                .andExpect(jsonPath("$.data._student_status").exists()) // Underscore prefix!
                .andExpect(jsonPath("$.data._faculty").exists()) // Underscore prefix!
                .andExpect(jsonPath("$.data._specialty").exists()); // Underscore prefix!

        verify(studentService, times(1)).findById(testId);
    }

    // =====================================================
    // Error Handling Tests
    // =====================================================

    @Test
    @DisplayName("Malformed JSON should return 400 Bad Request")
    void malformedJson_ShouldReturn400() throws Exception {
        // Given
        String malformedJson = "{\"code\": \"STU001\", invalid}";

        // When & Then
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed JSON"));
    }

    @Test
    @DisplayName("Invalid UUID format should return 400 Bad Request")
    void invalidUuidFormat_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/app/rest/v2/students/{id}", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // =====================================================
    // NO DELETE ENDPOINT TEST
    // =====================================================

    @Test
    @DisplayName("DELETE /students/{id} - should return 405 Method Not Allowed")
    void deleteStudent_ShouldReturn405() throws Exception {
        // When & Then
        mockMvc.perform(delete("/app/rest/v2/students/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());

        // Verify service method NEVER called
        verify(studentService, never()).softDelete(any());
    }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT TESTS
    // =====================================================
    // Physical DELETE is prohibited (NDG)
    // DELETE HTTP method returns 405 Method Not Allowed
    // =====================================================
}
