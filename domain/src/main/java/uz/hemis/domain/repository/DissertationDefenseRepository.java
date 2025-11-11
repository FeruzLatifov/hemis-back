package uz.hemis.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.DissertationDefense;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface DissertationDefenseRepository extends JpaRepository<DissertationDefense, UUID> {

    List<DissertationDefense> findByDoctorateStudent(UUID doctorateStudent);

    List<DissertationDefense> findByDoctorateStudentAndSpeciality(UUID doctorateStudent, UUID speciality);

    Page<DissertationDefense> findByDoctorateStudentAndSpeciality(UUID doctorateStudent, UUID speciality, Pageable pageable);

    long countByDoctorateStudentAndSpeciality(UUID doctorateStudent, UUID speciality);
}
