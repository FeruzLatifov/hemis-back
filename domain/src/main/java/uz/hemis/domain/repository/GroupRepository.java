package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.student.Group;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Query("SELECT g FROM Group g WHERE g.university = :university AND g.educationType = :educationType AND g.educationYear = :educationYear AND g.groupId = :groupId AND g.groupName = :groupName")
    Optional<Group> findByUniqueKey(
            @Param("university") String university,
            @Param("educationType") String educationType,
            @Param("educationYear") String educationYear,
            @Param("groupId") String groupId,
            @Param("groupName") String groupName);
}
