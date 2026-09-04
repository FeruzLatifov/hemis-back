package uz.hemis.web.service;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import uz.hemis.service.classifier.dto.SpecialityNodeDto;
import uz.hemis.web.export.XlsxSupport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Renders the unified speciality classifier tree to a professional {@code .xlsx}.
 *
 * <p>Each worksheet carries a provenance band (title, applied filters, generated-at,
 * per-status counts), then a styled + auto-filtered header, then the classifier flattened
 * depth-first in canonical <strong>display order</strong>: every level-1 category is
 * immediately followed by its level-2 → level-3 → level-4 descendants (siblings newest-edition-
 * year first, then ascending numeric code) — mirroring the frontend {@code sortSpecialityNodes}.
 * The hierarchy is conveyed three ways so it survives any Excel operation: a numeric
 * {@code Level} column, a real {@code parent code} column, and native Excel row grouping
 * (collapsible outline) plus a light indent on the primary name.</p>
 *
 * <p><strong>In-memory only</strong> — {@link XSSFWorkbook} + {@link ByteArrayOutputStream};
 * nothing is written to disk (no {@code SXSSFWorkbook} temp files), so no artifact is ever
 * left behind on the server. POI lives in {@code api-web} only.</p>
 *
 * <p>All display strings (headers, sheet titles, level/status labels, band captions) are
 * supplied by the caller via {@link Labels} so the workbook can be localized to the requesting
 * user's language. Every text cell is neutralized against Excel/CSV formula injection
 * (CWE-1236) — the {@code NEEDS_REVIEW} rows are hand-entered and therefore untrusted.</p>
 *
 * @since 2.2.0
 */
@Component
public class SpecialityExcelExporter {

    /** Column order (indices referenced throughout). */
    private static final int COL_LEVEL = 0;
    private static final int COL_TAXONOMY = 1;
    private static final int COL_CODE = 2;
    private static final int COL_PARENT_CODE = 3;
    private static final int COL_NAME_UZ = 4;
    private static final int COL_NAME_OZ = 5;
    private static final int COL_NAME_RU = 6;
    private static final int COL_NAME_EN = 7;
    private static final int COL_EDU_LEVEL = 8;
    private static final int COL_STATUS = 9;
    private static final int COL_YEARS = 10;
    private static final int COL_COUNT = 11;

    /** char-count × 256 (POI column-width unit), aligned to the column order above. */
    private static final int[] WIDTHS = {8, 22, 16, 16, 56, 40, 40, 40, 12, 26, 18};

    /** Rows 0-2 = provenance band, row 3 = spacer, row 4 = table header, row 5+ = data. */
    private static final int TABLE_HEADER_ROW = 4;
    private static final int FIRST_DATA_ROW = TABLE_HEADER_ROW + 1;
    /** Freeze the header band + the identity columns (Level..Nomi UZ) so they stay pinned. */
    private static final int FREEZE_COLS = COL_NAME_UZ + 1;

    private static final Comparator<SpecialityNodeDto> DISPLAY_ORDER =
            Comparator.comparingInt(SpecialityExcelExporter::newestYear).reversed()
                    .thenComparingLong(SpecialityExcelExporter::codeValue)
                    .thenComparing(SpecialityNodeDto::nameUz, Comparator.nullsLast(Comparator.naturalOrder()));

    /**
     * Localized display strings for one export (built by the controller from the request language).
     *
     * @param headers     the {@value #COL_COUNT} column headers, in column order
     * @param taxonomy    hierarchy-level number → its taxonomy label (1=Bilim sohasi … 4=Ichki yo'nalish)
     * @param bachelor    worksheet title for education type '11'
     * @param master      worksheet title for education type '12'
     * @param residency   worksheet title for education type '13' (Ordinatura)
     * @param approved    label for {@code APPROVED}
     * @param needsReview label for {@code NEEDS_REVIEW}
     * @param titlePrefix band title prefix (e.g. "Mutaxassislik klassifikatori")
     * @param generated   band caption for the generated-at timestamp
     * @param total       band caption for the total-count
     * @param filters     band caption for the applied-filters line
     * @param noFilter    band value when no filter was applied
     */
    public record Labels(
            List<String> headers,
            Map<Integer, String> taxonomy,
            String bachelor, String master, String residency,
            String approved, String needsReview,
            String titlePrefix, String generated, String total,
            String filters, String noFilter
    ) {
    }

    /**
     * @param sheets      ordered sheet-title → classifier-tree map (one worksheet per entry)
     * @param labels      localized display strings
     * @param generatedAt formatted generation timestamp for the provenance band
     * @param filtersText human-readable applied-filter summary (blank ⇒ {@link Labels#noFilter()})
     * @return the {@code .xlsx} bytes (in-memory; never persisted to disk)
     */
    public byte[] toXlsx(SequencedMap<String, List<SpecialityNodeDto>> sheets,
                         Labels labels, String generatedAt, String filtersText) {
        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Styles styles = new Styles(wb);
            Set<String> usedSheetNames = new HashSet<>();
            sheets.forEach((name, tree) ->
                    writeSheet(wb, styles, labels, name, tree, generatedAt, filtersText, usedSheetNames));

            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build speciality .xlsx", e);
        }
    }

    /** One worksheet: provenance band → frozen, auto-filtered header → grouped depth-first rows. */
    private void writeSheet(Workbook wb, Styles st, Labels L, String sheetName,
                            List<SpecialityNodeDto> tree, String generatedAt, String filtersText,
                            Set<String> usedSheetNames) {
        Sheet sheet = wb.createSheet(XlsxSupport.uniqueSafeSheetName(sheetName, usedSheetNames));
        sheet.setRowSumsBelow(false); // outline toggle sits on the parent (top) row

        int lastCol = COL_COUNT - 1;
        int[] counts = new int[3]; // total, approved, needsReview
        countInto(tree, counts);

        // ---- Provenance band (rows 0-2), spacer (row 3) ----
        bandRow(sheet, st.title, 0, lastCol, L.titlePrefix() + " — " + sheetName);
        String filters = (filtersText == null || filtersText.isBlank()) ? L.noFilter() : filtersText;
        bandRow(sheet, st.band, 1, lastCol, L.filters() + ": " + filters);
        bandRow(sheet, st.band, 2, lastCol, String.format(
                "%s: %s  ·  %s: %d  (%s: %d · %s: %d)",
                L.generated(), generatedAt, L.total(), counts[0],
                L.approved(), counts[1], L.needsReview(), counts[2]));

        // ---- Table header (row 4) ----
        Row header = sheet.createRow(TABLE_HEADER_ROW);
        header.setHeightInPoints(22);
        List<String> headers = L.headers();
        for (int i = 0; i < COL_COUNT; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(i < headers.size() ? headers.get(i) : "");
            c.setCellStyle(st.header);
            sheet.setColumnWidth(i, WIDTHS[i] * 256);
        }

        // ---- Data rows (row 5+) ----
        int end = writeNodes(sheet, st, L, tree, null, FIRST_DATA_ROW);

        // ---- Freeze + AutoFilter, both derived from the computed header row (never hardcoded 0/1) ----
        sheet.createFreezePane(FREEZE_COLS, FIRST_DATA_ROW);
        int lastData = Math.max(end - 1, TABLE_HEADER_ROW);
        sheet.setAutoFilter(new CellRangeAddress(TABLE_HEADER_ROW, lastData, 0, lastCol));
    }

    /**
     * Depth-first preorder: sort this level, write each node with its parent's code, then recurse
     * and wrap each parent's descendant span in a native outline group.
     *
     * @return the next free row index after this subtree
     */
    private int writeNodes(Sheet sheet, Styles st, Labels L,
                           List<SpecialityNodeDto> nodes, SpecialityNodeDto parent, int rowIdx) {
        for (SpecialityNodeDto n : nodes.stream().sorted(DISPLAY_ORDER).toList()) {
            int level = n.hierarchyLevel() != null ? n.hierarchyLevel() : 1;
            Row r = sheet.createRow(rowIdx++);

            cell(r, COL_LEVEL, st.data).setCellValue(level);
            text(r, COL_TAXONOMY, st.data, L.taxonomy().get(level));
            text(r, COL_CODE, st.code, n.code());
            text(r, COL_PARENT_CODE, st.code, parent != null ? parent.code() : null);
            text(r, COL_NAME_UZ, st.nameByLevel(level), n.nameUz());
            text(r, COL_NAME_OZ, st.data, n.nameOz());
            text(r, COL_NAME_RU, st.data, n.nameRu());
            text(r, COL_NAME_EN, st.data, n.nameEn());
            text(r, COL_EDU_LEVEL, st.data,
                    n.educationTypeName() != null && !n.educationTypeName().isBlank()
                            ? n.educationTypeName()
                            : (n.educationType() == null ? "" : n.educationType()));
            text(r, COL_STATUS, st.data, statusLabel(L, n.reviewStatus()));
            text(r, COL_YEARS, st.data, yearsLabel(n.years()));

            if (n.children() != null && !n.children().isEmpty()) {
                int firstChild = rowIdx;
                rowIdx = writeNodes(sheet, st, L, n.children(), n, rowIdx);
                sheet.groupRow(firstChild, rowIdx - 1); // native collapsible outline (levels 1-4)
            }
        }
        return rowIdx;
    }

    // ---- cell helpers ----

    private static Cell cell(Row row, int col, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellStyle(style);
        return c;
    }

    /** Writes a sanitized (formula-injection-safe) string; null/blank ⇒ empty cell. */
    private static void text(Row row, int col, CellStyle style, String value) {
        cell(row, col, style).setCellValue(sanitizeCell(value));
    }

    private static void bandRow(Sheet sheet, CellStyle style, int rowIdx, int lastCol, String value) {
        Cell c = cell(sheet.createRow(rowIdx), 0, style);
        c.setCellValue(sanitizeCell(value));
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, lastCol));
    }

    /**
     * Neutralize Excel/CSV formula injection (CWE-1236): a value whose first character is one
     * Excel treats as a formula/DDE trigger ({@code = + - @}, TAB, CR, LF) is prefixed with an
     * apostrophe so Excel renders it as literal text. Hand-entered NEEDS_REVIEW names/codes are
     * the untrusted vector.
     */
    static String sanitizeCell(String v) {
        return XlsxSupport.sanitizeCell(v);
    }

    private static String statusLabel(Labels L, String reviewStatus) {
        if ("APPROVED".equals(reviewStatus)) return L.approved();
        if ("NEEDS_REVIEW".equals(reviewStatus)) return L.needsReview();
        return reviewStatus == null ? "" : reviewStatus;
    }

    private static String yearsLabel(List<Integer> years) {
        if (years == null || years.isEmpty()) return "";
        return years.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    /** Total / APPROVED / NEEDS_REVIEW counts of the whole subtree, into {@code out[0..2]}. */
    private static void countInto(List<SpecialityNodeDto> nodes, int[] out) {
        for (SpecialityNodeDto n : nodes) {
            out[0]++;
            if ("APPROVED".equals(n.reviewStatus())) out[1]++;
            else if ("NEEDS_REVIEW".equals(n.reviewStatus())) out[2]++;
            if (n.children() != null && !n.children().isEmpty()) {
                countInto(n.children(), out);
            }
        }
    }

    /** Newest edition year (nodes without years sort last via the reversed comparator). */
    private static int newestYear(SpecialityNodeDto n) {
        List<Integer> ys = n.years();
        return (ys == null || ys.isEmpty())
                ? Integer.MIN_VALUE
                : ys.stream().mapToInt(Integer::intValue).max().orElse(Integer.MIN_VALUE);
    }

    /**
     * Leading-digit numeric value, matching JS {@code parseInt(code, 10)}: {@code "0100000"}→100000,
     * {@code "01.03.02"}→1. Missing / no-leading-digit codes sort last.
     */
    private static long codeValue(SpecialityNodeDto n) {
        String c = n.code();
        if (c == null) return Long.MAX_VALUE;
        int i = 0;
        while (i < c.length() && Character.isDigit(c.charAt(i))) {
            i++;
        }
        if (i == 0) return Long.MAX_VALUE;
        try {
            return Long.parseLong(c.substring(0, i));
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Reusable cell styles, created ONCE per workbook (XSSF hard-caps at 64k styles, so styles are
     * never cloned per cell across thousands of rows). Holds one indented name style per level 1-4.
     */
    private static final class Styles {
        final CellStyle title;
        final CellStyle band;
        final CellStyle header;
        final CellStyle data;
        final CellStyle code;
        private final CellStyle[] name = new CellStyle[5]; // 1..4 indented; [0] unused

        Styles(Workbook wb) {
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 13);
            title = wb.createCellStyle();
            title.setFont(titleFont);
            title.setVerticalAlignment(VerticalAlignment.CENTER);

            Font bandFont = wb.createFont();
            bandFont.setItalic(true);
            bandFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            band = wb.createCellStyle();
            band.setFont(bandFont);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            header = wb.createCellStyle();
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);
            header.setWrapText(true);
            thinBorder(header);

            data = wb.createCellStyle();
            data.setVerticalAlignment(VerticalAlignment.TOP);
            thinBorder(data);

            // "Kod" / "Ota kodi": force TEXT format so leading zeros ("0100000") and dotted codes
            // ("01.03.02") never get coerced to numbers and no green "number stored as text" flag shows.
            code = wb.createCellStyle();
            code.cloneStyleFrom(data);
            code.setDataFormat(wb.createDataFormat().getFormat("@"));

            for (int lvl = 1; lvl <= 4; lvl++) {
                CellStyle s = wb.createCellStyle();
                s.cloneStyleFrom(data);
                s.setWrapText(true);
                s.setIndention((short) ((lvl - 1) * 2)); // real Excel indent — value stays clean
                name[lvl] = s;
            }
        }

        CellStyle nameByLevel(int level) {
            return (level >= 1 && level <= 4) ? name[level] : data;
        }

        private static void thinBorder(CellStyle s) {
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
            s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }
}
