package uz.hemis.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.dto.StudentDto;
import uz.hemis.domain.entity.Student;
import uz.hemis.domain.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Tests for Student API
 *
 * <p><strong>Test Strategy:</strong></p>
 * <ul>
 *   <li>@SpringBootTest - Full application context</li>
 *   <li>@AutoConfigureMockMvc - MockMvc for HTTP testing</li>
 *   <li>Real database (H2 in-memory for tests)</li>
 *   <li>@Transactional - Rollback after each test</li>
 * </ul>
 *
 * <p><strong>Coverage:</strong></p>
 * <ul>
 *   <li>Complete CRUD operations (Create, Read, Update, Partial Update)</li>
 *   <li>Soft delete behavior (@Where clause filtering)</li>
 *   <li>Validation (PINFL uniqueness)</li>
 *   <li>Pagination and filtering</li>
 *   <li>Legacy JSON field names preservation</li>
 *   <li>NO DELETE endpoint (NDG enforcement)</li>
 * </ul>
 *
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Student API End-to-End Integration Tests")
class StudentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    private UUID testStudentId;

    @BeforeEach
    void setUp() {
        // Clean up test data
        studentRepository.deleteAll();
    }

    // =====================================================
    // Create Tests
    // =====================================================

    @Test
    @Order(1)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("E2E: Create student - full flow")
    @Transactional
    void createStudent_FullFlow() throws Exception {
        // Given
        StudentDto createDto = new StudentDto();
        createDto.setCode("STU001");
        createDto.setPinfl("12345678901234");
        createDto.setUniversity("UNI001");
        createDto.setFirstname("John");
        createDto.setLastname("Doe");
        createDto.setFathername("Smith");

        // When: Create student
        MvcResult result = mockMvc.perform(post("/app/rest/v2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.code").value("STU001"))
                .andExpect(jsonPath("$.data._university").value("UNI001"))
                .andExpect(jsonPath("$.data.firstname").value("John"))
                .andReturn();

        // Then: Verify in database
        String responseBody = result.getResponse().getContentAsString();
        String studentId = objectMapper.readTree(responseBody).get("data").get("id").asText();

        Student savedStudent = studentRepository.findById(UUID.fromString(studentId)).orElseThrow();
        assertThat(savedStudent.getCode()).isEqualTo("STU001");
        assertThat(savedStudent.getPinfl()).isEqualTo("12345678901234");
        assertThat(savedStudent.getCreateTs()).isNotNull();
        assertThat(savedStudent.getDeleteTs()).isNull();
    }

    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("E2E: Create student with duplicate PINFL should fail")
    @Transactional
    void createStudent_DuplicatePinfl_ShouldFail() throws Exception {
        // Given: Existing student
        Student existing = createTestStudent("STU001", "12345678901234", "UNI001");
        studentRepository.save(existing);

        StudentDto createDto = new StudentDto();
        createDto.setCode("STU002");
        createDto.setPinfl("12345678901234"); // Duplicate PINFL
        createDto.setUniversity("UNI001");
        createDto.setFirstname("Jane");
        createDto.setLastname("Smith");

        // When & Then
        mockMvc.perform(post("/app/rest/v2/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").containsString("PINFL"))
                .andExpect(jsonPath("$.errors[0].field").value("pinfl"));
    }

    // =====================================================
    // Read Tests
    // =====================================================

    @Test
    @Order(3)
    @WithMockUser
    @DisplayName("E2E: Get all students with pagination")
    @Transactional
    void getAllStudents_WithPagination() throws Exception {
        // Given: Multiple students
        for (int i = 1; i <= 5; i++) {
            Student student = createTestStudent("STU00" + i, "1234567890123" + i, "UNI001");
            studentRepository.save(student);
        }

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students")
                        .param("page", "0")
                        .param("size", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(3));
    }

    @Test
    @Order(4)
    @WithMockUser
    @DisplayName("E2E: Get student by ID")
    @Transactional
    void getStudentById() throws Exception {
        // Given
        Student student = createTestStudent("STU001", "12345678901234", "UNI001");
        student = studentRepository.save(student);

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students/{id}", student.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(student.getId().toString()))
                .andExpect(jsonPath("$.data.code").value("STU001"))
                .andExpect(jsonPath("$.data._university").value("UNI001"));
    }

    @Test
    @Order(5)
    @WithMockUser
    @DisplayName("E2E: Get students by university")
    @Transactional
    void getStudentsByUniversity() throws Exception {
        // Given: Students from different universities
        studentRepository.save(createTestStudent("STU001", "12345678901231", "UNI001"));
        studentRepository.save(createTestStudent("STU002", "12345678901232", "UNI001"));
        studentRepository.save(createTestStudent("STU003", "12345678901233", "UNI002"));

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students")
                        .param("university", "UNI001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*]._university", everyItem(equalTo("UNI001"))));
    }

    @Test
    @Order(6)
    @WithMockUser
    @DisplayName("E2E: Get student not found should return 404")
    @Transactional
    void getStudentById_NotFound_ShouldReturn404() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/app/rest/v2/students/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").containsString("Student not found"));
    }

    // =====================================================
    // Update Tests
    // =====================================================

    @Test
    @Order(7)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("E2E: Update student - full update")
    @Transactional
    void updateStudent_FullUpdate() throws Exception {
        // Given: Existing student
        Student existing = createTestStudent("STU001", "12345678901234", "UNI001");
        existing = studentRepository.save(existing);

        StudentDto updateDto = new StudentDto();
        updateDto.setCode("STU001");
        updateDto.setPinfl("12345678901234");
        updateDto.setUniversity("UNI001");
        updateDto.setFirstname("Jane");
        updateDto.setLastname("Smith");
        updateDto.setFathername("Brown");

        // When
        mockMvc.perform(put("/app/rest/v2/students/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstname").value("Jane"))
                .andExpect(jsonPath("$.data.lastname").value("Smith"));

        // Then: Verify in database
        Student updated = studentRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getFirstname()).isEqualTo("Jane");
        assertThat(updated.getLastname()).isEqualTo("Smith");
        assertThat(updated.getUpdateTs()).isNotNull();
    }

    @Test
    @Order(8)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("E2E: Partial update student - PATCH")
    @Transactional
    void partialUpdateStudent() throws Exception {
        // Given: Existing student
        Student existing = createTestStudent("STU001", "12345678901234", "UNI001");
        existing.setFirstname("John");
        existing.setLastname("Doe");
        existing = studentRepository.save(existing);

        StudentDto partialDto = new StudentDto();
        partialDto.setFirstname("Jane"); // Only update firstname

        // When
        mockMvc.perform(patch("/app/rest/v2/students/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstname").value("Jane"))
                .andExpect(jsonPath("$.data.lastname").value("Doe")); // Unchanged

        // Then: Verify lastname unchanged
        Student updated = studentRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getFirstname()).isEqualTo("Jane");
        assertThat(updated.getLastname()).isEqualTo("Doe"); // Unchanged
    }

    // =====================================================
    // Soft Delete Tests
    // =====================================================

    @Test
    @Order(9)
    @WithMockUser
    @DisplayName("E2E: Soft deleted students should be filtered by @Where clause")
    @Transactional
    void softDeletedStudents_ShouldBeFiltered() throws Exception {
        // Given: One active, one soft-deleted student
        Student active = createTestStudent("STU001", "12345678901231", "UNI001");
        studentRepository.save(active);

        Student deleted = createTestStudent("STU002", "12345678901232", "UNI001");
        deleted.setDeleteTs(LocalDateTime.now());
        deleted.setDeletedBy("admin");
        studentRepository.save(deleted);

        // When: Get all students
        mockMvc.perform(get("/app/rest/v2/students"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].code").value("STU001"));

        // Then: Deleted student not in results
        mockMvc.perform(get("/app/rest/v2/students/{id}", deleted.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // NO DELETE Endpoint Test
    // =====================================================

    @Test
    @Order(10)
    @WithMockUser(roles = "ADMIN")
    @DisplayName("E2E: DELETE endpoint should return 405 Method Not Allowed (NDG)")
    @Transactional
    void deleteEndpoint_ShouldReturn405() throws Exception {
        // Given
        Student student = createTestStudent("STU001", "12345678901234", "UNI001");
        student = studentRepository.save(student);

        // When & Then
        mockMvc.perform(delete("/app/rest/v2/students/{id}", student.getId()))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());

        // Verify student still exists
        assertThat(studentRepository.findById(student.getId())).isPresent();
    }

    // =====================================================
    // Legacy JSON Field Names Test
    // =====================================================

    @Test
    @Order(11)
    @WithMockUser
    @DisplayName("E2E: Response should contain legacy JSON field names with underscores")
    @Transactional
    void response_ShouldContainLegacyFieldNames() throws Exception {
        // Given
        Student student = createTestStudent("STU001", "12345678901234", "UNI001");
        student.setStudentStatus("ACTIVE");
        student.setFaculty("FAC001");
        student.setSpecialty("SPEC001");
        student = studentRepository.save(student);

        // When & Then
        MvcResult result = mockMvc.perform(get("/app/rest/v2/students/{id}", student.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        // Verify underscore-prefixed field names
        assertThat(json).contains("\"_university\"");
        assertThat(json).contains("\"_student_status\"");
        assertThat(json).contains("\"_faculty\"");
        assertThat(json).contains("\"_specialty\"");

        // Should NOT contain camelCase variants
        assertThat(json).doesNotContain("\"university\":");
        assertThat(json).doesNotContain("\"studentStatus\":");
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private Student createTestStudent(String code, String pinfl, String university) {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setCode(code);
        student.setPinfl(pinfl);
        student.setUniversity(university);
        student.setFirstname("Test");
        student.setLastname("Student");
        student.setFathername("Middle");
        student.setCreateTs(LocalDateTime.now());
        return student;
    }

    // =====================================================
    // NOTE: NO DELETE OPERATIONS TESTED
    // =====================================================
    // Physical DELETE is prohibited (NDG)
    // Only soft delete behavior is tested (@Where clause)
    // =====================================================
}
