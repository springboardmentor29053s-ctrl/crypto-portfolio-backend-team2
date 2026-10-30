package com.crypto_portfolio_tracker.service;

import com.crypto_portfolio_tracker.dto.TaxReportDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;


@Service
public class PdfGenerator {

    public byte[] generateTaxReportPdf(List<TaxReportDTO> report) {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            document.add(new Paragraph("Crypto Tax Report", titleFont));
            document.add(new Paragraph("Tax percentage on each asset profit as per rule "  + " %30 "  + " that is 0.30 "));
            document.add(new Paragraph(" "));

            // Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            addHeader(table, "Asset");
            addHeader(table, "Total Buy");
            addHeader(table, "Total Sell");
            addHeader(table, "ProfitLoss");
            addHeader(table, "Tax");

            BigDecimal totalTax = BigDecimal.ZERO;

            for (TaxReportDTO r : report) {

                table.addCell(r.getAsset());
                table.addCell(r.getTotalBuy().toString());
                table.addCell(r.getTotalSell().toString());
                table.addCell(r.getRealizedProfitLoss().toString());
                table.addCell(r.getTax().toString());

                totalTax = totalTax.add(r.getTax());
            }

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Tax: " + totalTax));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }

        return out.toByteArray();
    }

    private void addHeader(PdfPTable table, String title) {
        PdfPCell header = new PdfPCell();
        header.setPhrase(new Phrase(title));
        header.setBackgroundColor(Color.LIGHT_GRAY);
        table.addCell(header);
    }
}
