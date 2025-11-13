package uz.hemis.service.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import uz.hemis.service.dashboard.dto.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Dashboard Service
 * Provides aggregated statistics for dashboard with caching
 * 
 * IMPORTANT: Uses READ REPLICA database via dashboardJdbcTemplate
 * - All queries go to DB_REPLICA_HOST (from .env)
 * - Zero load on master database
 * - Perfect for analytics and reporting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    @Qualifier("dashboardJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;  // ✅ Uses REPLICA database

    /**
     * Get all dashboard statistics (cached for 30 minutes in Redis)
     * 
     * READS FROM: DB_REPLICA_HOST (replica database)
     * CACHES IN: Redis (hemis:dashboard:stats:all)
     * 
     * Performance:
     * - First call: ~30-40 seconds (database aggregation)
     * - Cached calls: <50ms (Redis)
     * - Cache TTL: 30 minutes
     */
    @Cacheable(
        value = "stats",
        key = "'all'"
        // Uses @Primary CacheManager (TwoLevelCacheManager)
    )
    public DashboardResponse getDashboardStats() {
        log.info("📊 Fetching dashboard statistics from REPLICA database (cache miss)");
        long startTime = System.currentTimeMillis();

        DashboardResponse response = new DashboardResponse();
        response.setTimestamp(LocalDateTime.now());

        // Fetch all statistics
        response.setOverview(getOverviewStats());
        response.setStudents(getStudentStats());
        response.setEducationTypes(getEducationTypeStats());
        response.setTopUniversities(getTopUniversities());
        response.setRecentActivities(getRecentActivities());

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Dashboard statistics fetched successfully in {}ms", duration);
        return response;
    }

    /**
     * Get overview statistics
     */
    private OverviewStatsDto getOverviewStats() {
        String sql = """
            SELECT 
              -- Total counts
              COUNT(*) as total_students,
              COUNT(DISTINCT university_code) as total_universities,
              
              -- By status (O'qimoqda, Bitirgan, Chetlashgan, etc.)
              COUNT(CASE WHEN status_code = '11' THEN 1 END) as active_students,
              COUNT(CASE WHEN status_code = '14' THEN 1 END) as graduated_students,
              COUNT(CASE WHEN status_code = '12' THEN 1 END) as expelled_students,
              COUNT(CASE WHEN status_code = '13' THEN 1 END) as academic_leave_students,
              COUNT(CASE WHEN status_code = '17' THEN 1 END) as cancelled_students,
              
              -- Payment type (only for active students)
              COUNT(CASE WHEN status_code = '11' AND payment_form_code = '11' THEN 1 END) as grant_students,
              COUNT(CASE WHEN status_code = '11' AND payment_form_code = '12' THEN 1 END) as contract_students,
              
              -- Gender (only for active students)
              COUNT(CASE WHEN status_code = '11' AND gender_code = '11' THEN 1 END) as male_count,
              COUNT(CASE WHEN status_code = '11' AND gender_code = '12' THEN 1 END) as female_count,
              
              -- Single-roundtrip additional totals (subselects)
              (SELECT COUNT(*) FROM hemishe_e_employee_job WHERE delete_ts IS NULL) as total_teachers,
              (SELECT COUNT(*) FROM hemishe_e_student_diploma WHERE delete_ts IS NULL) as total_diplomas,
              (SELECT COUNT(*) FROM hemishe_e_project) as total_projects,
              (SELECT COUNT(*) FROM hemishe_e_publication_scientific) as total_publications
            FROM hemishe_r_student_full 
            WHERE (is_expel IS NULL OR is_expel = false)
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            OverviewStatsDto dto = new OverviewStatsDto();
            
            // Total counts
            dto.setTotalStudents(rs.getLong("total_students"));
            dto.setTotalUniversities(rs.getInt("total_universities"));
            
            // By status
            dto.setActiveStudents(rs.getLong("active_students"));           // O'qimoqda
            dto.setGraduatedStudents(rs.getLong("graduated_students"));     // Bitirgan
            dto.setExpelledStudents(rs.getLong("expelled_students"));       // Chetlashgan
            dto.setAcademicLeaveStudents(rs.getLong("academic_leave_students")); // Akademik ta'til
            dto.setCancelledStudents(rs.getLong("cancelled_students"));     // Bekor qilingan
            
            // Payment type (only active students)
            dto.setGrantStudents(rs.getLong("grant_students"));
            dto.setContractStudents(rs.getLong("contract_students"));
            
            // Gender (only active students)
            dto.setMaleCount(rs.getLong("male_count"));
            dto.setFemaleCount(rs.getLong("female_count"));

            // Calculate teacher count from employee table
            String teacherSql = "SELECT COUNT(*) FROM hemishe_e_employee_job WHERE delete_ts IS NULL";
            Long teacherCount = jdbcTemplate.queryForObject(teacherSql, Long.class);
            dto.setTotalTeachers(teacherCount != null ? teacherCount : 0L);

            // Calculate diploma count
            String diplomaSql = "SELECT COUNT(*) FROM hemishe_e_student_diploma WHERE delete_ts IS NULL";
            Long diplomaCount = jdbcTemplate.queryForObject(diplomaSql, Long.class);
            dto.setTotalDiplomas(diplomaCount != null ? diplomaCount : 0L);

            // Calculate project count
            String projectSql = "SELECT COUNT(*) FROM hemishe_e_project";
            Long projectCount = jdbcTemplate.queryForObject(projectSql, Long.class);
            dto.setTotalProjects(projectCount != null ? projectCount : 0L);

            // Calculate publication count
            String publicationSql = "SELECT COUNT(*) FROM hemishe_e_publication_scientific";
            Long publicationCount = jdbcTemplate.queryForObject(publicationSql, Long.class);
            dto.setTotalPublications(publicationCount != null ? publicationCount : 0L);

            return dto;
        });
    }

    /**
     * Get student statistics by various categories
     * ONLY ACTIVE STUDENTS (status_code = '11')
     */
    private StudentStatsDto getStudentStats() {
        StudentStatsDto dto = new StudentStatsDto();

        // By education form (only active)
        String formSql = """
            SELECT education_form_name, COUNT(*) as count 
            FROM hemishe_r_student_full 
            WHERE (is_expel IS NULL OR is_expel = false)
              AND status_code = '11'
              AND education_form_name IS NOT NULL
            GROUP BY education_form_name
            ORDER BY count DESC
            LIMIT 10
            """;
        dto.setByEducationForm(queryForCategoryStats(formSql));

        // By region (only active)
        String regionSql = """
            SELECT university_region_name, COUNT(*) as count 
            FROM hemishe_r_student_full 
            WHERE (is_expel IS NULL OR is_expel = false)
              AND status_code = '11'
              AND university_region_name IS NOT NULL
            GROUP BY university_region_name
            ORDER BY count DESC
            LIMIT 10
            """;
        dto.setByRegion(queryForCategoryStats(regionSql));

        // By language (only active)
        String langSql = """
            SELECT education_language_name, COUNT(*) as count 
            FROM hemishe_r_student_full 
            WHERE (is_expel IS NULL OR is_expel = false)
              AND status_code = '11'
              AND education_language_name IS NOT NULL
            GROUP BY education_language_name
            ORDER BY count DESC
            """;
        dto.setByLanguage(queryForCategoryStats(langSql));

        return dto;
    }

    /**
     * Get education type statistics (only active students)
     */
    private List<EducationTypeDto> getEducationTypeStats() {
        String sql = """
            SELECT 
              education_type_name,
              education_type_code,
              COUNT(*) as student_count
            FROM hemishe_r_student_full 
            WHERE (is_expel IS NULL OR is_expel = false)
              AND status_code = '11'
              AND education_type_name IS NOT NULL
            GROUP BY education_type_code, education_type_name
            ORDER BY student_count DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            EducationTypeDto dto = new EducationTypeDto();
            dto.setName(rs.getString("education_type_name"));
            dto.setCode(rs.getString("education_type_code"));
            dto.setCount(rs.getLong("student_count"));
            return dto;
        });
    }

    /**
     * Get top universities by student count (only active students)
     */
    private List<TopUniversityDto> getTopUniversities() {
        String sql = """
            SELECT 
              university_code,
              university_name,
              COUNT(*) as student_count,
              COUNT(CASE WHEN gender_code = '11' THEN 1 END) as male_count,
              COUNT(CASE WHEN gender_code = '12' THEN 1 END) as female_count,
              COUNT(CASE WHEN payment_form_code = '11' THEN 1 END) as grant_count,
              COUNT(CASE WHEN payment_form_code = '12' THEN 1 END) as contract_count
            FROM hemishe_r_student_full 
            WHERE (is_expel IS NULL OR is_expel = false)
              AND status_code = '11'
              AND university_code IS NOT NULL
            GROUP BY university_code, university_name
            ORDER BY student_count DESC
            LIMIT 10
            """;

        List<TopUniversityDto> universities = jdbcTemplate.query(sql, (rs, rowNum) -> {
            TopUniversityDto dto = new TopUniversityDto();
            dto.setCode(rs.getString("university_code"));
            dto.setName(rs.getString("university_name"));
            dto.setStudentCount(rs.getLong("student_count"));
            dto.setMaleCount(rs.getLong("male_count"));
            dto.setFemaleCount(rs.getLong("female_count"));
            dto.setGrantCount(rs.getLong("grant_count"));
            dto.setContractCount(rs.getLong("contract_count"));
            dto.setRank(rowNum + 1);
            return dto;
        });

        return universities;
    }

    /**
     * Get recent activities (mock data for now - can be implemented later with audit tables)
     */
    private List<RecentActivityDto> getRecentActivities() {
        // This is a placeholder - in production, fetch from audit/history tables
        List<RecentActivityDto> activities = new ArrayList<>();
        
        // Get recent students (as example)
        String studentSql = """
            SELECT fullname, created_at, 'student' as type
            FROM hemishe_r_student_full 
            WHERE created_at IS NOT NULL
            ORDER BY created_at DESC
            LIMIT 5
            """;

        jdbcTemplate.query(studentSql, rs -> {
            RecentActivityDto dto = new RecentActivityDto();
            dto.setType("student");
            dto.setAction("Yangi talaba qo'shildi");
            dto.setName(rs.getString("fullname"));
            dto.setTime(rs.getTimestamp("created_at").toLocalDateTime());
            activities.add(dto);
        });

        return activities;
    }

    /**
     * Helper method to query category statistics
     */
    private List<CategoryStatDto> queryForCategoryStats(String sql) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CategoryStatDto dto = new CategoryStatDto();
            dto.setName(rs.getString(1));
            dto.setCount(rs.getLong("count"));
            return dto;
        });
    }
}
