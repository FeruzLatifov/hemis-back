package uz.hemis.web.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * Generic, constant-memory streaming {@code .xlsx} exporter for the web registries.
 *
 * <p>Streams the FULL filtered result set (no row cap) as an OOXML workbook while keeping heap
 * flat: rows are fetched one {@link Page} at a time and written into an {@link SXSSFWorkbook}
 * whose sliding window ({@value #WINDOW} rows) flushes older rows to a temp file — so the resident
 * footprint is bounded by one page + the window regardless of how many rows the query returns.
 * The response is chunked (no {@code Content-Length}); the temp files are always removed via
 * {@link SXSSFWorkbook#dispose()} in a {@code finally}.</p>
 *
 * <p>Every text cell is neutralized against Excel formula injection (CWE-1236) via
 * {@link XlsxSupport#sanitizeCell}. Data cells use the text format so codes keep their leading
 * zeros and dotted codes never become dates.</p>
 *
 * <p><strong>Fail-fast:</strong> the first page is fetched on the request thread, so a query error
 * surfaces as a clean JSON error (via the exception advice) BEFORE the {@code 200} + stream is
 * committed. A failure on a later page can no longer change the already-sent status — it only
 * truncates the download — which is why the first fetch is done eagerly.</p>
 */
@Component
public class XlsxStreamExporter {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** Rows fetched per DB round-trip (bounds resident page size). */
    private static final int PAGE_SIZE = 2000;
    /** SXSSF sliding-window size — rows beyond this are flushed to a temp file. */
    private static final int WINDOW = 200;
    private static final int DEFAULT_WIDTH = 24;

    /**
     * Build a streaming {@code .xlsx} download of every row {@code fetchPage} returns.
     *
     * @param baseFilename filename stem; a {@code _yyyyMMdd_HHmmss.xlsx} suffix is appended
     * @param sheetName    worksheet title (sanitized to ≤31 valid chars)
     * @param headers      column headers, in column order
     * @param widths       per-column character widths (may be {@code null}/shorter → default)
     * @param fetchPage    fetches one page of rows for a given {@link Pageable} (filters pre-bound)
     * @param rowMapper    maps one row to its column values, in header order
     */
    public <T> ResponseEntity<StreamingResponseBody> export(
            String baseFilename, String sheetName, List<String> headers, int[] widths,
            Function<Pageable, Page<T>> fetchPage, Function<T, String[]> rowMapper) {

        // Fail-fast: a query error here maps to a clean JSON error before the stream is committed.
        Page<T> firstPage = fetchPage.apply(PageRequest.of(0, PAGE_SIZE));
        int cols = headers.size();

        StreamingResponseBody body = out -> {
            // try-with-resources: SXSSFWorkbook.close() also disposes the temp files (POI 5.x),
            // so cleanup runs even on a mid-stream failure.
            try (SXSSFWorkbook wb = new SXSSFWorkbook(WINDOW)) {
                Sheet sheet = wb.createSheet(XlsxSupport.uniqueSafeSheetName(sheetName, new HashSet<>()));
                XlsxSupport.ExcelStyles styles = new XlsxSupport.ExcelStyles(wb);

                Row headerRow = sheet.createRow(0);
                headerRow.setHeightInPoints(20);
                for (int c = 0; c < cols; c++) {
                    Cell cell = headerRow.createCell(c);
                    cell.setCellValue(headers.get(c));
                    cell.setCellStyle(styles.header);
                    int w = (widths != null && c < widths.length) ? widths[c] : DEFAULT_WIDTH;
                    sheet.setColumnWidth(c, w * 256);
                }

                int rowIdx = 1;
                Page<T> page = firstPage;
                int pageNo = 0;
                while (true) {
                    for (T item : page.getContent()) {
                        String[] values = rowMapper.apply(item);
                        Row row = sheet.createRow(rowIdx++);
                        for (int c = 0; c < cols; c++) {
                            Cell cell = row.createCell(c);
                            cell.setCellStyle(styles.text);
                            cell.setCellValue(XlsxSupport.sanitizeCell(c < values.length ? values[c] : ""));
                        }
                    }
                    if (!page.hasNext()) {
                        break;
                    }
                    page = fetchPage.apply(PageRequest.of(++pageNo, PAGE_SIZE));
                }

                sheet.createFreezePane(0, 1); // keep the header visible while scrolling
                sheet.setAutoFilter(new CellRangeAddress(0, Math.max(rowIdx - 1, 0), 0, cols - 1));
                wb.write(out);
            }
        };

        String filename = baseFilename + "_" + LocalDateTime.now().format(TS) + ".xlsx";
        HttpHeaders headers0 = new HttpHeaders();
        headers0.setContentType(MediaType.parseMediaType(XlsxSupport.XLSX_CONTENT_TYPE));
        headers0.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8).build());
        headers0.setCacheControl(CacheControl.noStore()); // NO Content-Length — response is chunked/streamed
        return ResponseEntity.ok().headers(headers0).body(body);
    }
}
