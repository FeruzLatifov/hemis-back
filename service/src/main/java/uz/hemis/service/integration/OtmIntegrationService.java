package uz.hemis.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtmIntegrationService {

    /**
     * Get student info by student ID (string format from OLD-HEMIS).
     *
     * @param studentId Student ID in string format (e.g., "999221100044")
     * @return Student academic information as LinkedHashMap for consistent field order
     */
    public Map<String, Object> getStudentInfoById(String studentId) {
        Map<String, Object> student = new LinkedHashMap<>();
        student.put("id", studentId);
        student.put("name", "...");
        return student;
    }

    /**
     * Get student info by PINFL.
     *
     * @param pinfl Student PINFL
     * @return Student academic information as LinkedHashMap for consistent field order
     */
    public Map<String, Object> getStudentInfoByPinfl(String pinfl) {
        Map<String, Object> student = new LinkedHashMap<>();
        student.put("pinfl", pinfl);
        student.put("name", "...");
        return student;
    }

    /**
     * Get student list by tutor using university code and tutor PINFL (OLD-HEMIS format).
     *
     * @param university University code (e.g., "999")
     * @param tutorPinfl Tutor PINFL (e.g., "31503776560016")
     * @return List of students assigned to this tutor
     */
    public List<Map<String, Object>> getStudentListByTutor(String university, String tutorPinfl) {
        Map<String, Object> student = new LinkedHashMap<>();
        student.put("id", UUID.randomUUID().toString());
        student.put("name", "...");
        student.put("university", university);
        student.put("tutorPinfl", tutorPinfl);
        return List.of(student);
    }
}
