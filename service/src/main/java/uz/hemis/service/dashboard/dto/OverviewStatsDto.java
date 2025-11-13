package uz.hemis.service.dashboard.dto;

import lombok.Data;

@Data
public class OverviewStatsDto {
    // Total counts
    private Long totalStudents;
    private Long totalTeachers;
    private Integer totalUniversities;
    private Long totalDiplomas;
    private Long totalProjects;
    private Long totalPublications;
    
    // Students by status
    private Long activeStudents;           // 11 - O'qimoqda
    private Long graduatedStudents;        // 14 - Bitirgan
    private Long expelledStudents;         // 12 - Chetlashgan
    private Long academicLeaveStudents;    // 13 - Akademik ta'til
    private Long cancelledStudents;        // 17 - Bekor qilingan
    
    // Payment type (only active students)
    private Long grantStudents;
    private Long contractStudents;
    
    // Gender (only active students)
    private Long maleCount;
    private Long femaleCount;
}
