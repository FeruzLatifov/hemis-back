package uz.hemis.common.dto.report;

import java.io.Serializable;
import java.util.List;

/**
 * Shared response contract for the ministry analytics report cards
 * ({@code /api/v1/web/reports/{students|institutions|scientific|teachers}}).
 *
 * <p>One common shape drives all report pages: headline {@link ReportKpiDto} cards plus a list of
 * {@link ReportBlockDto} visualisation blocks (bar / pie / table). Backend produces stable English
 * i18n keys for every {@code label}/{@code title}/column label; the frontend renders them via {@code t()}.</p>
 *
 * @param kpis   headline metric cards
 * @param blocks visualisation blocks
 * @since 3.0.0
 */
public record ReportDto(List<ReportKpiDto> kpis, List<ReportBlockDto> blocks) implements Serializable {
}
