package com.expensetracker.util;

import com.expensetracker.model.Expense;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class PDFReportGenerator {

    public static String generateReport(List<Expense> expenses, String username, String currency,
                                         String fromDate, String toDate, String outputDir) {
        String fileName = "ExpenseReport_" + username + "_" + System.currentTimeMillis() + ".pdf";
        String filePath = outputDir + "/" + fileName;

        try {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(79, 70, 229));
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(107, 114, 128));

            // Title
            Paragraph title = new Paragraph("QuickExpense Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph period = new Paragraph("Period: " + fromDate + " to " + toDate, subtitleFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(8);
            document.add(period);

            Paragraph user = new Paragraph("Generated for: " + username + " | Date: " + LocalDate.now(), subtitleFont);
            user.setAlignment(Element.ALIGN_CENTER);
            user.setSpacingAfter(20);
            document.add(user);

            // Summary Section
            double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
            Map<String, Double> categoryTotals = new LinkedHashMap<>();
            for (Expense e : expenses) {
                categoryTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
            }
            String topCategory = categoryTotals.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("-");

            // Summary table
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10);
            summaryTable.setSpacingAfter(20);

            addSummaryCell(summaryTable, "Total Spent", currency + String.format("%,.2f", total), new Color(79, 70, 229));
            addSummaryCell(summaryTable, "Transactions", String.valueOf(expenses.size()), new Color(16, 185, 129));
            addSummaryCell(summaryTable, "Top Category", topCategory, new Color(245, 158, 11));
            addSummaryCell(summaryTable, "Avg/Transaction", currency + String.format("%,.2f", expenses.isEmpty() ? 0 : total / expenses.size()), new Color(6, 182, 212));
            document.add(summaryTable);

            // Category Breakdown
            Paragraph catTitle = new Paragraph("Category Breakdown", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(31, 41, 55)));
            catTitle.setSpacingAfter(10);
            document.add(catTitle);

            PdfPTable catTable = new PdfPTable(3);
            catTable.setWidthPercentage(100);
            catTable.setWidths(new float[]{3, 2, 2});
            addTableHeader(catTable, new String[]{"Category", "Amount", "% of Total"}, headerFont);
            for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                double pct = total > 0 ? (entry.getValue() / total) * 100 : 0;
                catTable.addCell(createCell(entry.getKey(), normalFont));
                catTable.addCell(createCell(currency + String.format("%,.2f", entry.getValue()), boldFont));
                catTable.addCell(createCell(String.format("%.1f%%", pct), normalFont));
            }
            catTable.setSpacingAfter(20);
            document.add(catTable);

            // Expense Details Table
            Paragraph detailTitle = new Paragraph("Expense Details", new Font(Font.HELVETICA, 14, Font.BOLD, new Color(31, 41, 55)));
            detailTitle.setSpacingAfter(10);
            document.add(detailTitle);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 2, 2});
            addTableHeader(table, new String[]{"#", "Title", "Category", "Amount", "Date"}, headerFont);

            int idx = 1;
            for (Expense e : expenses) {
                Color rowColor = idx % 2 == 0 ? new Color(249, 250, 251) : Color.WHITE;
                table.addCell(createColoredCell(String.valueOf(idx++), normalFont, rowColor));
                table.addCell(createColoredCell(e.getTitle(), normalFont, rowColor));
                table.addCell(createColoredCell(e.getCategory(), normalFont, rowColor));
                table.addCell(createColoredCell(currency + String.format("%,.2f", e.getAmount()), boldFont, rowColor));
                table.addCell(createColoredCell(e.getDate().toString(), normalFont, rowColor));
            }

            // Total row
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)));
            totalLabel.setColspan(3);
            totalLabel.setBackgroundColor(new Color(79, 70, 229));
            totalLabel.setPadding(8);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(currency + String.format("%,.2f", total), new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)));
            totalValue.setBackgroundColor(new Color(79, 70, 229));
            totalValue.setPadding(8);
            table.addCell(totalValue);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBackgroundColor(new Color(79, 70, 229));
            table.addCell(emptyCell);

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("\nGenerated by QuickExpense — Professional Expense Management", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            writer.close();
            return filePath;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addSummaryCell(PdfPTable table, String label, String value, Color accentColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setPadding(12);
        cell.setBackgroundColor(Color.WHITE);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", new Font(Font.HELVETICA, 8, Font.BOLD, new Color(107, 114, 128))));
        p.add(new Chunk(value, new Font(Font.HELVETICA, 14, Font.BOLD, accentColor)));
        cell.addElement(p);
        table.addCell(cell);
    }

    private static void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(new Color(79, 70, 229));
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(new Color(229, 231, 235));
        return cell;
    }

    private static PdfPCell createColoredCell(String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setBackgroundColor(bgColor);
        return cell;
    }
}
