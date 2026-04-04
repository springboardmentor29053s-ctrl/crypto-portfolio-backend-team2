package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.TaxReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class ExcelGenerator {

    public static ByteArrayInputStream generateTaxExcel(List<TaxReportDTO> reports) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Tax Report");

            // ===== Header Style =====
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // ===== Total Style =====
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));

            // ===== Header =====
            Row header = sheet.createRow(0);
            String[] columns = {"Asset", "Total Buy", "Total Sell", "PnL", "Tax"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // ===== Data =====
            int rowIdx = 1;

            for (TaxReportDTO r : reports) {

                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(r.getAsset());
                row.createCell(1).setCellValue(r.getTotalBuy().doubleValue());
                row.createCell(2).setCellValue(r.getTotalSell().doubleValue());
                row.createCell(3).setCellValue(r.getRealizedProfitLoss().doubleValue());
                row.createCell(4).setCellValue(r.getTax().doubleValue());

            }

            // ===== TOTAL ROW  =====
            Row totalRow = sheet.createRow(rowIdx);

            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL");
            totalLabel.setCellStyle(totalStyle);

            Cell totalTaxCell = totalRow.createCell(4);
            totalTaxCell.setCellFormula("SUM(E2:E" + rowIdx + ")");
            totalTaxCell.setCellStyle(totalStyle);


            // ===== Auto-size columns =====
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed");
        }
    }
}
