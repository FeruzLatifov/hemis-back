package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.StudentCertificate;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface StudentCertificateRepository extends JpaRepository<StudentCertificate, UUID> {

    List<StudentCertificate> findByUniversity(UUID university);

    List<StudentCertificate> findByUniversityAndStudent(UUID university, UUID student);

    Page<StudentCertificate> findByUniversityAndStudent(UUID university, UUID student, Pageable pageable);

    long countByUniversityAndStudent(UUID university, UUID student);
}
