package uz.hemis.domain.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data convention: {@code XxxImpl} suffix bilan custom repo'ga otomatik wire bo'ladi.
 */
@Repository
public class EmployeeUpsertRepositoryImpl implements EmployeeUpsertRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO employee (
                id, pinfl, person_type,
                first_name, last_name, middle_name, birth_date,
                gender_code, citizenship_code, nationality_code,
                passport, email, address,
                academic_degree_code, academic_rank_code,
                synced_at,
                version, created_at, created_by, updated_at, updated_by
            ) VALUES (
                gen_random_uuid(), :pinfl, 'UNIVERSITY_STAFF',
                :firstName, :lastName, :middleName, :birthDate,
                :genderCode, :citizenshipCode, :nationalityCode,
                :passport, :email, :address,
                :academicDegreeCode, :academicRankCode,
                :now,
                1, :now, :auditUser, NULL, NULL
            )
            ON CONFLICT (pinfl) WHERE deleted_at IS NULL
            DO UPDATE SET
                first_name           = EXCLUDED.first_name,
                last_name            = EXCLUDED.last_name,
                middle_name          = EXCLUDED.middle_name,
                birth_date           = EXCLUDED.birth_date,
                gender_code          = EXCLUDED.gender_code,
                citizenship_code     = EXCLUDED.citizenship_code,
                nationality_code     = EXCLUDED.nationality_code,
                passport             = EXCLUDED.passport,
                email                = EXCLUDED.email,
                address              = EXCLUDED.address,
                academic_degree_code = EXCLUDED.academic_degree_code,
                academic_rank_code   = EXCLUDED.academic_rank_code,
                synced_at            = EXCLUDED.synced_at,
                updated_at           = EXCLUDED.synced_at,
                updated_by           = :auditUser,
                version              = employee.version + 1
            RETURNING id
            """;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID upsertFromSync(
            String pinfl,
            String firstName,
            String lastName,
            String middleName,
            LocalDate birthDate,
            String genderCode,
            String citizenshipCode,
            String nationalityCode,
            String passport,
            String email,
            String address,
            String academicDegreeCode,
            String academicRankCode,
            LocalDateTime now,
            String auditUser
    ) {
        Object id = em.createNativeQuery(UPSERT_SQL)
                .setParameter("pinfl", pinfl)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("middleName", middleName)
                .setParameter("birthDate", birthDate)
                .setParameter("genderCode", genderCode)
                .setParameter("citizenshipCode", citizenshipCode)
                .setParameter("nationalityCode", nationalityCode)
                .setParameter("passport", passport)
                .setParameter("email", email)
                .setParameter("address", address)
                .setParameter("academicDegreeCode", academicDegreeCode)
                .setParameter("academicRankCode", academicRankCode)
                .setParameter("now", now)
                .setParameter("auditUser", auditUser)
                .getSingleResult();
        return (UUID) id;
    }
}
