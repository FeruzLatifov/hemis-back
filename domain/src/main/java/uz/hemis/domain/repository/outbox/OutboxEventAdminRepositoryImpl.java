package uz.hemis.domain.repository.outbox;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import uz.hemis.common.dto.outbox.OutboxStatsDto;
import uz.hemis.domain.entity.outbox.OutboxEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic JPQL builder for outbox admin queries — status + aggregateType filter,
 * sortable + paginated. Stats query uses native SQL for COUNT FILTER aggregates.
 *
 * <p>Stats query single-shot snapshot — admin dashboard widget refresh ~5s.</p>
 *
 * @since 2026-05-19
 */
@Repository
@RequiredArgsConstructor
public class OutboxEventAdminRepositoryImpl implements OutboxEventAdminRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<OutboxEvent> search(String status, String aggregateType, int dlqThreshold, Pageable pageable) {
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (aggregateType != null && !aggregateType.isBlank()) {
            where.append(" AND e.aggregateType = :aggregateType ");
            params.put("aggregateType", aggregateType);
        }

        if (status != null) {
            switch (status) {
                case "PENDING" -> where.append(" AND e.publishedAt IS NULL AND e.retryCount = 0 ");
                case "RETRYING" -> {
                    where.append(" AND e.publishedAt IS NULL AND e.retryCount > 0 AND e.retryCount < :dlqThreshold ");
                    params.put("dlqThreshold", dlqThreshold);
                }
                case "DLQ" -> {
                    where.append(" AND e.publishedAt IS NULL AND e.retryCount >= :dlqThreshold ");
                    params.put("dlqThreshold", dlqThreshold);
                }
                case "PUBLISHED" -> where.append(" AND e.publishedAt IS NOT NULL ");
                default -> {
                    // no-op
                }
            }
        }

        String orderBy = buildOrderBy(pageable.getSort());
        TypedQuery<OutboxEvent> listQuery = em.createQuery(
                "SELECT e FROM OutboxEvent e " + where + orderBy, OutboxEvent.class);
        TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(e.id) FROM OutboxEvent e " + where, Long.class);
        params.forEach((k, v) -> {
            listQuery.setParameter(k, v);
            countQuery.setParameter(k, v);
        });

        listQuery.setFirstResult((int) pageable.getOffset());
        listQuery.setMaxResults(pageable.getPageSize());

        List<OutboxEvent> rows = listQuery.getResultList();
        long total = countQuery.getSingleResult();
        return new PageImpl<>(rows, pageable, total);
    }

    @Override
    public OutboxStatsDto statsSnapshot(int dlqThreshold) {
        Object[] row = (Object[]) em.createNativeQuery("""
                SELECT
                  COUNT(*) AS total,
                  COUNT(*) FILTER (WHERE published_at IS NULL AND retry_count < :dlqThreshold) AS pending,
                  COUNT(*) FILTER (WHERE published_at IS NOT NULL) AS published,
                  COUNT(*) FILTER (WHERE published_at IS NULL AND retry_count >= :dlqThreshold) AS dlq,
                  COALESCE(EXTRACT(EPOCH FROM (NOW() - MIN(occurred_at) FILTER (WHERE published_at IS NULL))) / 60, 0) AS oldest_min
                FROM outbox_event
                """)
                .setParameter("dlqThreshold", dlqThreshold)
                .getSingleResult();
        return new OutboxStatsDto(
                ((Number) row[0]).longValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue()
        );
    }

    private String buildOrderBy(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return " ORDER BY e.occurredAt DESC ";
        }
        List<String> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String field = switch (order.getProperty()) {
                case "occurredAt" -> "e.occurredAt";
                case "publishedAt" -> "e.publishedAt";
                case "retryCount" -> "e.retryCount";
                case "aggregateType" -> "e.aggregateType";
                default -> "e.occurredAt";
            };
            orders.add(field + " " + (order.isAscending() ? "ASC" : "DESC"));
        }
        return " ORDER BY " + String.join(", ", orders) + " ";
    }

    @SuppressWarnings("unused")
    private Duration since(LocalDateTime t) {
        return Duration.between(t, LocalDateTime.now());
    }
}
