package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.domain.entity.security.PasswordHistory;
import uz.hemis.domain.entity.security.User;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    List<PasswordHistory> findTop5ByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
