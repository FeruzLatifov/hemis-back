package uz.hemis.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ScholarshipBillingRequest {
    private String period;
    private List<StudentScholarship> students;
    
    @Data
    public static class StudentScholarship {
        private String studentId;
        private String pinfl;
        private BigDecimal amount;
        private String scholarshipType;
    }
}
