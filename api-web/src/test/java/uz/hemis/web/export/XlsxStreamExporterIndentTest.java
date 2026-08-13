package uz.hemis.web.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the classifier-style tree export used by the speciality-attachments page:
 * no separate parent column, and the L4 "Ichki yo'nalish" name is real-Excel-indented one
 * level under its L3 "Yo'nalish" parent (which sits directly above it in code order).
 *
 * <p>Plain unit test — no Spring context, no DB; drives {@link XlsxStreamExporter} directly.</p>
 */
class XlsxStreamExporterIndentTest {

    /** One row plus its taxonomy depth (3 = Yo'nalish, 4 = Ichki yo'nalish). */
    private record Row9(String[] cells, int level) {
    }

    @Test
    void exportsClassifierStyleTreeWithIndentedChild() throws Exception {
        XlsxStreamExporter exporter = new XlsxStreamExporter();

        List<String> headers = List.of("OTM kodi", "OTM nomi", "O'quv yili", "Ierarxiya darajasi",
                "Kod", "Mutaxassislik", "Ta'lim turi", "Ta'lim shakli", "Holati");
        int[] widths = {14, 44, 12, 18, 14, 60, 16, 16, 10};

        Row9 parent = new Row9(new String[]{
                "301", "Andijon", "2026-2027", "Yo'nalish", "60210300", "Muzeyshunoslik",
                "Bakalavr", "Kunduzgi", "ACTIVE"}, 3);
        Row9 child = new Row9(new String[]{
                "301", "Andijon", "2026-2027", "Ichki yo'nalish", "60210300",
                "Muzeyshunoslik (muzey menejmenti va madaniy turizm)", "Bakalavr", "Kunduzgi", "ACTIVE"}, 4);
        List<Row9> data = List.of(parent, child);

        Function<Pageable, Page<Row9>> fetch = p ->
                new PageImpl<>(p.getPageNumber() == 0 ? data : List.of(), p, data.size());
        Function<Row9, String[]> mapper = Row9::cells;
        Function<Row9, Integer> indent = r -> r.level() == 4 ? 1 : 0;

        ResponseEntity<StreamingResponseBody> resp = exporter.export(
                "test", "Attachments", headers, widths, fetch, mapper, 5, indent);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resp.getBody().writeTo(bos);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bos.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);

            // Header: classifier-style, and NO separate parent column.
            Row header = sheet.getRow(0);
            assertEquals(9, header.getPhysicalNumberOfCells());
            for (Cell c : header) {
                assertNotEquals("Yo'nalish (ota)", c.getStringCellValue());
            }
            assertEquals("Ierarxiya darajasi", header.getCell(3).getStringCellValue());
            assertEquals("Kod", header.getCell(4).getStringCellValue());
            assertEquals("Mutaxassislik", header.getCell(5).getStringCellValue());

            // L3 name flush-left; L4 name indented one level under its parent.
            Row l3 = sheet.getRow(1);
            Row l4 = sheet.getRow(2);
            assertEquals("Muzeyshunoslik", l3.getCell(5).getStringCellValue());
            assertEquals((short) 0, l3.getCell(5).getCellStyle().getIndention(),
                    "L3 Yo'nalish must be flush left");
            assertTrue(l4.getCell(5).getCellStyle().getIndention() > 0,
                    "L4 Ichki yo'nalish must be indented under its L3 parent");
        }
    }
}
