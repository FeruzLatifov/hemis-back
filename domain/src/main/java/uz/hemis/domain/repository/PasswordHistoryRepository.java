package uz.hemis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.hemis.domain.entity.PasswordHistory;
import uz.hemis.domain.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    List<PasswordHistory> findTop5ByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
