package com.expensetracker.util;

import com.expensetracker.model.Expense;
import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.dao.BudgetDAO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates professional Excel (.xlsx) expense reports with charts.
 */
public class ExcelReportGenerator {

    private static final String[] CATEGORIES = {"Food", "Travel", "Shopping", "Bills", "Entertainment", "Health", "Education", "Others"};

    /**
     * Generate a full Excel report for a user within a date range.
     * Includes: Summary sheet, Expense details, Category breakdown pie chart, Monthly trend bar chart, Budget vs Actual.
     */
    public static File generateReport(int userId, LocalDate fromDate, LocalDate toDate,
                                       String filePath, String currencySymbol, String username) throws IOException {
        ExpenseDAO expenseDAO = new ExpenseDAO();
        BudgetDAO budgetDAO = new BudgetDAO();

        List<Expense> expenses = expenseDAO.getFilteredExpenses(userId,
                fromDate.toString(), toDate.toString(), null);

        // Category totals
        Map<String, Double> catTotals = new LinkedHashMap<>();
        for (String cat : CATEGORIES) catTotals.put(cat, 0.0);
        double grandTotal = 0;
        for (Expense e : expenses) {
            catTotals.merge(e.getCategory(), e.getAmount(), Double::sum);
            grandTotal += e.getAmount();
        }

        // Monthly totals
        Map<String, Double> monthTotals = new LinkedHashMap<>();
        for (Expense e : expenses) {
            if (e.getDate() != null) {
                String key = e.getDate().getMonth().toString().substring(0, 3) + " " + e.getDate().getYear();
                monthTotals.merge(key, e.getAmount(), Double::sum);
            }
        }

        // Budget data for current month
        LocalDate now = LocalDate.now();
        Map<String, Double> budgetMap = budgetDAO.getBudgetMap(userId, now.getMonthValue(), now.getYear());

        if (currencySymbol == null) currencySymbol = "₹";

        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ========== STYLES ==========
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(new XSSFColor(new byte[]{(byte) 79, (byte) 70, (byte) 229}, null));

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 11);

            XSSFFont normalFont = workbook.createFont();
            normalFont.setFontHeightInPoints((short) 10);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 79, (byte) 70, (byte) 229}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(normalFont);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setFont(normalFont);
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            amountStyle.setBorderBottom(BorderStyle.THIN);
            amountStyle.setBorderTop(BorderStyle.THIN);
            amountStyle.setBorderLeft(BorderStyle.THIN);
            amountStyle.setBorderRight(BorderStyle.THIN);
            amountStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFont(boldFont);
            totalStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 238, (byte) 242, (byte) 255}, null));
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            totalStyle.setBorderBottom(BorderStyle.MEDIUM);
            totalStyle.setBorderTop(BorderStyle.MEDIUM);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setFont(normalFont);
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);

            // ======= SHEET 1: SUMMARY =======
            XSSFSheet summarySheet = workbook.createSheet("Summary");
            summarySheet.setColumnWidth(0, 6000);
            summarySheet.setColumnWidth(1, 5000);

            int row = 0;
            Row r = summarySheet.createRow(row++);
            Cell c = r.createCell(0);
            c.setCellValue("QuickExpense — Expense Report");
            c.setCellStyle(titleStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            row++;
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("User:");
            r.createCell(1).setCellValue(username != null ? username : "N/A");
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("Period:");
            r.createCell(1).setCellValue(fromDate.format(DateTimeFormatter.ISO_DATE) + " to " + toDate.format(DateTimeFormatter.ISO_DATE));
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("Currency:");
            r.createCell(1).setCellValue(currencySymbol);
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("Total Expenses:");
            Cell totalCell = r.createCell(1);
            totalCell.setCellValue(grandTotal);
            totalCell.setCellStyle(totalStyle);
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("Transactions:");
            r.createCell(1).setCellValue(expenses.size());
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("Avg per Transaction:");
            Cell avgCell = r.createCell(1);
            avgCell.setCellValue(expenses.isEmpty() ? 0 : grandTotal / expenses.size());
            avgCell.setCellStyle(amountStyle);
            r = summarySheet.createRow(row++);
            r.createCell(0).setCellValue("Generated:");
            r.createCell(1).setCellValue(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

            // Category breakdown in summary
            row += 2;
            r = summarySheet.createRow(row++);
            Cell catHeader = r.createCell(0);
            catHeader.setCellValue("Category Breakdown");
            catHeader.setCellStyle(titleStyle);
            summarySheet.addMergedRegion(new CellRangeAddress(row - 1, row - 1, 0, 1));

            r = summarySheet.createRow(row++);
            Cell h1 = r.createCell(0); h1.setCellValue("Category"); h1.setCellStyle(headerStyle);
            Cell h2 = r.createCell(1); h2.setCellValue("Amount"); h2.setCellStyle(headerStyle);

            for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
                if (entry.getValue() > 0) {
                    r = summarySheet.createRow(row++);
                    Cell cc = r.createCell(0); cc.setCellValue(entry.getKey()); cc.setCellStyle(cellStyle);
                    Cell ca = r.createCell(1); ca.setCellValue(entry.getValue()); ca.setCellStyle(amountStyle);
                }
            }
            r = summarySheet.createRow(row++);
            Cell tt = r.createCell(0); tt.setCellValue("TOTAL"); tt.setCellStyle(totalStyle);
            Cell ta = r.createCell(1); ta.setCellValue(grandTotal); ta.setCellStyle(totalStyle);

            // ======= SHEET 2: EXPENSE DETAILS =======
            XSSFSheet detailSheet = workbook.createSheet("Expense Details");
            String[] detailHeaders = {"#", "Title", "Category", "Amount", "Date", "Description"};
            int[] detailWidths = {2000, 6000, 4500, 4000, 3500, 8000};
            for (int i = 0; i < detailWidths.length; i++) detailSheet.setColumnWidth(i, detailWidths[i]);

            Row dhr = detailSheet.createRow(0);
            for (int i = 0; i < detailHeaders.length; i++) {
                Cell hc = dhr.createCell(i);
                hc.setCellValue(detailHeaders[i]);
                hc.setCellStyle(headerStyle);
            }

            int drow = 1;
            for (int i = 0; i < expenses.size(); i++) {
                Expense ex = expenses.get(i);
                Row dr = detailSheet.createRow(drow++);
                Cell dc0 = dr.createCell(0); dc0.setCellValue(i + 1); dc0.setCellStyle(cellStyle);
                Cell dc1 = dr.createCell(1); dc1.setCellValue(ex.getTitle()); dc1.setCellStyle(cellStyle);
                Cell dc2 = dr.createCell(2); dc2.setCellValue(ex.getCategory()); dc2.setCellStyle(cellStyle);
                Cell dc3 = dr.createCell(3); dc3.setCellValue(ex.getAmount()); dc3.setCellStyle(amountStyle);
                Cell dc4 = dr.createCell(4);
                dc4.setCellValue(ex.getDate() != null ? ex.getDate().format(DateTimeFormatter.ISO_DATE) : "");
                dc4.setCellStyle(cellStyle);
                Cell dc5 = dr.createCell(5);
                dc5.setCellValue(ex.getDescription() != null ? ex.getDescription() : "");
                dc5.setCellStyle(cellStyle);
            }

            // Total row
            Row totalRow = detailSheet.createRow(drow);
            Cell tc1 = totalRow.createCell(0); tc1.setCellValue(""); tc1.setCellStyle(totalStyle);
            Cell tc2 = totalRow.createCell(1); tc2.setCellValue(""); tc2.setCellStyle(totalStyle);
            Cell tc3 = totalRow.createCell(2); tc3.setCellValue("TOTAL"); tc3.setCellStyle(totalStyle);
            Cell tc4 = totalRow.createCell(3); tc4.setCellValue(grandTotal); tc4.setCellStyle(totalStyle);
            Cell tc5 = totalRow.createCell(4); tc5.setCellValue(""); tc5.setCellStyle(totalStyle);
            Cell tc6 = totalRow.createCell(5); tc6.setCellValue(""); tc6.setCellStyle(totalStyle);

            // ======= SHEET 3: CHARTS =======
            XSSFSheet chartSheet = workbook.createSheet("Charts");

            // Write category data for charts
            Row cr0 = chartSheet.createRow(0);
            cr0.createCell(0).setCellValue("Category");
            cr0.createCell(1).setCellValue("Amount");
            int chartRow = 1;
            List<String> catLabels = new ArrayList<>();
            List<Double> catValues = new ArrayList<>();
            for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
                if (entry.getValue() > 0) {
                    Row cr = chartSheet.createRow(chartRow++);
                    cr.createCell(0).setCellValue(entry.getKey());
                    cr.createCell(1).setCellValue(entry.getValue());
                    catLabels.add(entry.getKey());
                    catValues.add(entry.getValue());
                }
            }

            // PIE CHART - Category Breakdown
            if (!catLabels.isEmpty()) {
                XSSFDrawing drawing = chartSheet.createDrawingPatriarch();
                XSSFClientAnchor pieAnchor = drawing.createAnchor(0, 0, 0, 0, 3, 0, 13, 18);
                XSSFChart pieChart = drawing.createChart(pieAnchor);
                pieChart.setTitleText("Expense by Category");

                XDDFDataSource<String> pieCategories = XDDFDataSourcesFactory.fromStringCellRange(
                        chartSheet, new CellRangeAddress(1, chartRow - 1, 0, 0));
                XDDFNumericalDataSource<Double> pieValues = XDDFDataSourcesFactory.fromNumericCellRange(
                        chartSheet, new CellRangeAddress(1, chartRow - 1, 1, 1));

                XDDFChartData pieData = pieChart.createData(ChartTypes.PIE, null, null);
                pieData.setVaryColors(true);
                XDDFChartData.Series pieSeries = pieData.addSeries(pieCategories, pieValues);
                pieSeries.setTitle("Expenses", null);
                pieChart.plot(pieData);

                XDDFChartLegend pieLegend = pieChart.getOrAddLegend();
                pieLegend.setPosition(LegendPosition.BOTTOM);
            }

            // Monthly data below the category data
            int monthStart = chartRow + 2;
            Row mr0 = chartSheet.createRow(monthStart);
            mr0.createCell(0).setCellValue("Month");
            mr0.createCell(1).setCellValue("Amount");
            int monthRow = monthStart + 1;
            for (Map.Entry<String, Double> entry : monthTotals.entrySet()) {
                Row mr = chartSheet.createRow(monthRow++);
                mr.createCell(0).setCellValue(entry.getKey());
                mr.createCell(1).setCellValue(entry.getValue());
            }

            // BAR CHART - Monthly Trend
            if (!monthTotals.isEmpty()) {
                XSSFDrawing drawing2 = chartSheet.getDrawingPatriarch();
                if (drawing2 == null) drawing2 = chartSheet.createDrawingPatriarch();
                XSSFClientAnchor barAnchor = drawing2.createAnchor(0, 0, 0, 0, 3, 20, 13, 38);
                XSSFChart barChart = drawing2.createChart(barAnchor);
                barChart.setTitleText("Monthly Expense Trend");

                XDDFCategoryAxis bottomAxis = barChart.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxis.setTitle("Month");
                XDDFValueAxis leftAxis = barChart.createValueAxis(AxisPosition.LEFT);
                leftAxis.setTitle("Amount");
                leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);

                XDDFDataSource<String> monthCategories = XDDFDataSourcesFactory.fromStringCellRange(
                        chartSheet, new CellRangeAddress(monthStart + 1, monthRow - 1, 0, 0));
                XDDFNumericalDataSource<Double> monthValues = XDDFDataSourcesFactory.fromNumericCellRange(
                        chartSheet, new CellRangeAddress(monthStart + 1, monthRow - 1, 1, 1));

                XDDFChartData barData = barChart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
                barData.setVaryColors(false);
                XDDFChartData.Series barSeries = barData.addSeries(monthCategories, monthValues);
                barSeries.setTitle("Expenses", null);
                barChart.plot(barData);

                XDDFChartLegend barLegend = barChart.getOrAddLegend();
                barLegend.setPosition(LegendPosition.TOP);
            }

            // ======= SHEET 4: BUDGET vs ACTUAL =======
            if (!budgetMap.isEmpty()) {
                XSSFSheet budgetSheet = workbook.createSheet("Budget vs Actual");
                budgetSheet.setColumnWidth(0, 5000);
                budgetSheet.setColumnWidth(1, 4000);
                budgetSheet.setColumnWidth(2, 4000);
                budgetSheet.setColumnWidth(3, 4000);
                budgetSheet.setColumnWidth(4, 4000);

                Row bTitle = budgetSheet.createRow(0);
                Cell btc = bTitle.createCell(0);
                btc.setCellValue("Budget vs Actual — " + now.getMonth() + " " + now.getYear());
                btc.setCellStyle(titleStyle);
                budgetSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

                Row bhr = budgetSheet.createRow(2);
                String[] bHeaders = {"Category", "Budget", "Actual", "Remaining", "Status"};
                for (int i = 0; i < bHeaders.length; i++) {
                    Cell bhc = bhr.createCell(i);
                    bhc.setCellValue(bHeaders[i]);
                    bhc.setCellStyle(headerStyle);
                }

                // Get current month expenses
                List<Expense> monthExpenses = expenseDAO.getExpensesByMonth(userId, YearMonth.of(now.getYear(), now.getMonthValue()));
                Map<String, Double> monthActuals = ExpenseDAO.getCategoryTotals(monthExpenses);

                int brow = 3;
                double totalBudget = 0, totalActual = 0;
                for (Map.Entry<String, Double> be : budgetMap.entrySet()) {
                    double budgetAmt = be.getValue();
                    double actualAmt = monthActuals.getOrDefault(be.getKey(), 0.0);
                    double remaining = budgetAmt - actualAmt;
                    String status = remaining >= 0 ? "✓ Under Budget" : "✗ Over Budget";

                    Row br = budgetSheet.createRow(brow++);
                    Cell bc0 = br.createCell(0); bc0.setCellValue(be.getKey()); bc0.setCellStyle(cellStyle);
                    Cell bc1 = br.createCell(1); bc1.setCellValue(budgetAmt); bc1.setCellStyle(amountStyle);
                    Cell bc2 = br.createCell(2); bc2.setCellValue(actualAmt); bc2.setCellStyle(amountStyle);
                    Cell bc3 = br.createCell(3); bc3.setCellValue(remaining); bc3.setCellStyle(amountStyle);
                    Cell bc4 = br.createCell(4); bc4.setCellValue(status); bc4.setCellStyle(cellStyle);

                    totalBudget += budgetAmt;
                    totalActual += actualAmt;
                }

                Row bTotalRow = budgetSheet.createRow(brow);
                Cell bt0 = bTotalRow.createCell(0); bt0.setCellValue("TOTAL"); bt0.setCellStyle(totalStyle);
                Cell bt1 = bTotalRow.createCell(1); bt1.setCellValue(totalBudget); bt1.setCellStyle(totalStyle);
                Cell bt2 = bTotalRow.createCell(2); bt2.setCellValue(totalActual); bt2.setCellStyle(totalStyle);
                Cell bt3 = bTotalRow.createCell(3); bt3.setCellValue(totalBudget - totalActual); bt3.setCellStyle(totalStyle);
                Cell bt4 = bTotalRow.createCell(4);
                bt4.setCellValue(totalBudget - totalActual >= 0 ? "✓ Under Budget" : "✗ Over Budget");
                bt4.setCellStyle(totalStyle);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }

        return file;
    }
}
