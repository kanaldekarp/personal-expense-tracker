package com.expensetracker.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class ReportGeneratorThread extends Thread {
    private int userId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String filePath;

    public ReportGeneratorThread(int userId, LocalDate fromDate, LocalDate toDate, String filePath) {
        this.userId = userId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try (
                Connection conn = DBConnection.getConnection();) {
            if (conn == null) {
                System.out.println("❌ Database connection failed in ReportGeneratorThread");
                return;
            }

            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // Ensure directory exists
            }

            try (FileWriter writer = new FileWriter(file)) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT amount, category, date, description FROM expenses " +
                                "WHERE user_id = ? AND date BETWEEN ? AND ?");
                ps.setInt(1, userId);
                ps.setDate(2, java.sql.Date.valueOf(fromDate));
                ps.setDate(3, java.sql.Date.valueOf(toDate));

                ResultSet rs = ps.executeQuery();

                writer.write("Expense Report from " + fromDate + " to " + toDate + "\n");
                writer.write("====================================\n");

                double total = 0;
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    String category = rs.getString("category");
                    java.sql.Date date = rs.getDate("date");
                    String description = rs.getString("description");

                    writer.write(String.format(
                            "Date: %s | Category: %s | Amount: %.2f | Description: %s\n",
                            date.toString(), category, amount, description));

                    total += amount;
                }

                writer.write("====================================\n");
                writer.write(String.format("Total Expense: %.2f\n", total));
                writer.flush();
            } // Close FileWriter

        } catch (IOException | SQLException e) {
            e.printStackTrace(); // Ideally, log this properly in real apps
        }
    }
}
