package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.Faculty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID>, JpaSpecificationExecutor<Faculty> {

    /**
     * Get university groups with faculty counts (for root level of tree)
     * Returns: universityId, universityName, facultyCount, activeCount, inactiveCount
     */
    @Query(value = """
        SELECT 
            u.code as universityId,
            u.name as universityName,
            COUNT(f.id) as facultyCount,
            COUNT(CASE WHEN f.active = true THEN 1 END) as activeCount,
            COUNT(CASE WHEN f.active = false OR f.active IS NULL THEN 1 END) as inactiveCount
        FROM hemishe_e_university u
        LEFT JOIN hemishe_e_faculty f ON f._university = u.code AND f.delete_ts IS NULL
        WHERE u.delete_ts IS NULL
            AND (:q IS NULL OR u.name ILIKE '%' || :q || '%' OR u.code ILIKE '%' || :q || '%')
            AND (:status IS NULL 
                OR (:status = true AND u.active = true)
                OR (:status = false AND (u.active = false OR u.active IS NULL)))
        GROUP BY u.code, u.name, u.active
        ORDER BY u.name
        """, 
        countQuery = """
        SELECT COUNT(DISTINCT u.code)
        FROM hemishe_e_university u
        WHERE u.delete_ts IS NULL
            AND (:q IS NULL OR u.name ILIKE '%' || :q || '%' OR u.code ILIKE '%' || :q || '%')
            AND (:status IS NULL 
                OR (:status = true AND u.active = true)
                OR (:status = false AND (u.active = false OR u.active IS NULL)))
        """,
        nativeQuery = true)
    Page<Map<String, Object>> findUniversityGroups(
        @Param("q") String searchQuery,
        @Param("status") Boolean status,
        Pageable pageable
    );

    /**
     * Get faculties by university code (lazy load children)
     */
    @Query(value = """
        SELECT 
            f.id,
            f.code,
            f.name as nameUz,
            f.name as nameRu,
            f.short_name as shortName,
            f._university as universityId,
            f.active
        FROM hemishe_e_faculty f
        WHERE f.delete_ts IS NULL
            AND f._university = :universityCode
            AND (:q IS NULL OR f.name ILIKE '%' || :q || '%' OR f.code ILIKE '%' || :q || '%')
            AND (:status IS NULL 
                OR (:status = true AND f.active = true)
                OR (:status = false AND (f.active = false OR f.active IS NULL)))
        ORDER BY f.name
        """,
        countQuery = """
        SELECT COUNT(f.id)
        FROM hemishe_e_faculty f
        WHERE f.delete_ts IS NULL
            AND f._university = :universityCode
            AND (:q IS NULL OR f.name ILIKE '%' || :q || '%' OR f.code ILIKE '%' || :q || '%')
            AND (:status IS NULL 
                OR (:status = true AND f.active = true)
                OR (:status = false AND (f.active = false OR f.active IS NULL)))
        """,
        nativeQuery = true)
    Page<Map<String, Object>> findByUniversityCode(
        @Param("universityCode") String universityCode,
        @Param("q") String searchQuery,
        @Param("status") Boolean status,
        Pageable pageable
    );

    /**
     * Get faculty detail with university name
     */
    @Query(value = """
        SELECT 
            f.id,
            f.code,
            f.name,
            f.short_name as shortName,
            f._university as universityCode,
            u.name as universityName,
            f._faculty_type as facultyType,
            f.active,
            f.create_ts as createdAt,
            f.created_by as createdBy,
            f.update_ts as updatedAt,
            f.updated_by as updatedBy
        FROM hemishe_e_faculty f
        LEFT JOIN hemishe_e_university u ON u.code = f._university
        WHERE f.id = :id AND f.delete_ts IS NULL
        """,
        nativeQuery = true)
    Map<String, Object> findFacultyDetailById(@Param("id") UUID id);

    /**
     * Find all faculties for export (with filters)
     */
    @Query(value = """
        SELECT 
            f.id,
            f.code,
            f.name as nameUz,
            f.short_name as shortName,
            u.name as universityName,
            f.active,
            f.create_ts as createdAt
        FROM hemishe_e_faculty f
        LEFT JOIN hemishe_e_university u ON u.code = f._university
        WHERE f.delete_ts IS NULL
            AND (:universityCode IS NULL OR f._university = :universityCode)
            AND (:q IS NULL OR f.name ILIKE '%' || :q || '%' OR f.code ILIKE '%' || :q || '%')
            AND (:status IS NULL 
                OR (:status = true AND f.active = true)
                OR (:status = false AND (f.active = false OR f.active IS NULL)))
        ORDER BY u.name, f.name
        """,
        nativeQuery = true)
    List<Map<String, Object>> findAllForExport(
        @Param("universityCode") String universityCode,
        @Param("q") String searchQuery,
        @Param("status") Boolean status
    );
}
