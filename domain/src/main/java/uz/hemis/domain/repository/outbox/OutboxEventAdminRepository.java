package uz.hemis.domain.repository.outbox;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.hemis.common.dto.outbox.OutboxStatsDto;
import uz.hemis.domain.entity.outbox.OutboxEvent;

/**
 * Admin-side queries for outbox observability (separate from poll/publish ops).
 *
 * <p>Status filter mapping:
 * <ul>
 *   <li>{@code PENDING}    — published_at IS NULL AND retry_count = 0</li>
 *   <li>{@code RETRYING}   — published_at IS NULL AND retry_count BETWEEN 1 AND (dlqThreshold-1)</li>
 *   <li>{@code DLQ}        — published_at IS NULL AND retry_count >= dlqThreshold</li>
 *   <li>{@code PUBLISHED}  — published_at IS NOT NULL</li>
 *   <li>{@code null}/all   — no status filter</li>
 * </ul></p>
 *
 * @since 2026-05-19
 */
public interface OutboxEventAdminRepository {

    Page<OutboxEvent> search(String status, String aggregateType, int dlqThreshold, Pageable pageable);

    OutboxStatsDto statsSnapshot(int dlqThreshold);
}
