package com.petro.admin_dashboard.report;

import com.petro.admin_dashboard.exception.ApiException;
import com.petro.admin_dashboard.model.Customer;
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
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

@Slf4j
public class CustomerReport {
    private final XSSFWorkbook workbook;
    private final XSSFSheet sheet;
    private final List<Customer> customers;
    private static final String[] HEADERS = { "ID", "Name", "Email", "Type", "Status", "Address", "Phone", "Created At" };

    public CustomerReport(List<Customer> customers) {
        this.customers = customers;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Customers");
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
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            int rowIndex = 1;
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(customer.getId());
                row.createCell(1).setCellValue(customer.getName());
                row.createCell(2).setCellValue(customer.getEmail());
                row.createCell(3).setCellValue(customer.getType());
                row.createCell(4).setCellValue(customer.getStatus());
                row.createCell(5).setCellValue(customer.getAddress());
                row.createCell(6).setCellValue(customer.getPhone());
                row.createCell(7).setCellValue(format(customer.getCreatedAt(), "yyyy-MM-dd hh:mm:ss"));
            }

            workbook.write(out);
            return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("Unable to export report file.");
        }
    }
}
