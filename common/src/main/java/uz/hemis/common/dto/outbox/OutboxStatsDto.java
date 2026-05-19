package uz.hemis.common.dto.outbox;

/**
 * Outbox health overview — admin dashboard top widget.
 *
 * @since 2026-05-19
 */
public record OutboxStatsDto(
        long total,
        long pending,
        long published,
        long dlq,
        long oldestPendingMinutes  // alarm threshold (>15 min = stuck poller)
) {
}
