package com.petro.admin_dashboard.report;

import com.petro.admin_dashboard.exception.ApiException;
import com.petro.admin_dashboard.model.Customer;
import com.petro.admin_dashboard.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.List;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

@Slf4j
public class InvoiceReport {
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private final List<Invoice> invoices;
    private static final String[] HEADERS = { "Invoice Number", "Service", "Status", "Date", "Total" };

    public InvoiceReport(List<Invoice> invoices) {
        this.invoices = invoices;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Invoices");
        setHeaders();
    }

    public InputStreamResource export() {
        return generateReport();
    }

    private void setHeaders() {
        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(18);
        style.setFont(font);

        range(0, HEADERS.length).forEach(index -> {
            Cell cell = headerRow.createCell(index);
            cell.setCellValue(HEADERS[index]);
            cell.setCellStyle(style);
        });
    }

    private InputStreamResource generateReport() {
        NumberFormat usFormat = NumberFormat.getCurrencyInstance();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            int rowIndex = 1;
            for (Invoice invoice : invoices) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(invoice.getInvoiceNumber());
                row.createCell(1).setCellValue(invoice.getServices());
                row.createCell(2).setCellValue(invoice.getStatus());
                row.createCell(3).setCellValue(format(invoice.getDate(), "yyyy-MM-dd hh:mm:ss"));
                row.createCell(4).setCellValue(usFormat.format(invoice.getTotal()));
            }

            workbook.write(out);
            return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("Unable to export report file.");
        }
    }
}
