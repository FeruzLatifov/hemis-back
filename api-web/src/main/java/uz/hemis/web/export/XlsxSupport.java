package uz.hemis.web.export;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Set;

/**
 * Shared, POI-interface-level helpers for every Excel exporter in {@code api-web}
 * (works with both {@code XSSFWorkbook} and streaming {@code SXSSFWorkbook}).
 *
 * <p>Single source of truth for the two security/robustness rules that must never diverge
 * between exporters: the Excel/CSV formula-injection guard ({@link #sanitizeCell}) and safe,
 * collision-free worksheet naming ({@link #uniqueSafeSheetName}).</p>
 */
public final class XlsxSupport {

    private XlsxSupport() {
    }

    /** OOXML {@code .xlsx} MIME type. */
    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Neutralize Excel/CSV formula injection (CWE-1236): a value whose first character is one
     * Excel treats as a formula/DDE trigger ({@code = + - @}, TAB, CR, LF) is prefixed with an
     * apostrophe so Excel renders it as literal text. Hand-entered / OTM-supplied cells are the
     * untrusted vector.
     */
    public static String sanitizeCell(String v) {
        if (v == null || v.isEmpty()) {
            return "";
        }
        char first = v.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@'
                || first == '\t' || first == '\r' || first == '\n') {
            return "'" + v;
        }
        return v;
    }

    /**
     * A worksheet name that Excel accepts (≤31 chars, none of {@code : \ / ? * [ ]}) AND is unique
     * within {@code used}. On collision a {@code " (2)"}, {@code " (3)"}… suffix is appended (still
     * kept ≤31). The chosen name is added to {@code used}. Prevents a duplicate-name
     * {@code IllegalArgumentException} from {@code Workbook#createSheet} aborting the whole export.
     */
    public static String uniqueSafeSheetName(String desired, Set<String> used) {
        String cleaned = (desired == null ? "" : desired).replaceAll("[:\\\\/?*\\[\\]]", " ").trim();
        if (cleaned.isEmpty()) {
            cleaned = "Sheet";
        }
        if (cleaned.length() > 31) {
            cleaned = cleaned.substring(0, 31);
        }
        String candidate = cleaned;
        int n = 2;
        while (used.contains(candidate)) {
            String suffix = " (" + n++ + ")";
            String base = cleaned.length() + suffix.length() > 31
                    ? cleaned.substring(0, 31 - suffix.length())
                    : cleaned;
            candidate = base + suffix;
        }
        used.add(candidate);
        return candidate;
    }

    /**
     * Reusable header + data cell styles, created ONCE per workbook (XSSF/SXSSF hard-cap at 64k
     * styles, so styles are never cloned per cell across thousands of rows).
     *
     * <p>Data cells use the text ({@code @}) number format so codes like {@code "0100000"} or
     * {@code "01.03.02"} are never coerced into numbers/dates and no "number stored as text"
     * flag appears.</p>
     */
    public static final class ExcelStyles {

        public final CellStyle header;
        public final CellStyle text;

        public ExcelStyles(Workbook wb) {
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

            text = wb.createCellStyle();
            text.setVerticalAlignment(VerticalAlignment.TOP);
            text.setDataFormat(wb.createDataFormat().getFormat("@"));
            thinBorder(text);
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
