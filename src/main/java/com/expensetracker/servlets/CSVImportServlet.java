package com.expensetracker.servlets;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;
import com.opencsv.CSVReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/CSVImportServlet")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024) // 5MB max
public class CSVImportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        Part filePart = request.getPart("csvFile");
        if (filePart == null || filePart.getSize() == 0) {
            response.sendRedirect("viewExpenses.jsp?error=No file uploaded");
            return;
        }

        List<Expense> expenses = new ArrayList<>();
        int errors = 0;
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext(); // Skip header row
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                try {
                    if (line.length < 4) continue;

                    Expense e = new Expense();
                    e.setUserId(userId);
                    e.setTitle(line[0].trim());
                    e.setCategory(line[1].trim());
                    e.setAmount(Double.parseDouble(line[2].trim()));
                    
                    // Try multiple date formats
                    LocalDate date = null;
                    for (DateTimeFormatter fmt : formatters) {
                        try {
                            date = LocalDate.parse(line[3].trim(), fmt);
                            break;
                        } catch (DateTimeParseException ignored) {}
                    }
                    if (date == null) {
                        errors++;
                        continue;
                    }
                    e.setDate(date);
                    e.setDescription(line.length > 4 ? line[4].trim() : "");
                    expenses.add(e);
                } catch (Exception ex) {
                    errors++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("viewExpenses.jsp?error=Error reading CSV file");
            return;
        }

        if (!expenses.isEmpty()) {
            ExpenseDAO dao = new ExpenseDAO();
            int imported = dao.bulkAddExpenses(expenses);
            String msg = imported + " expenses imported successfully";
            if (errors > 0) msg += " (" + errors + " rows skipped)";
            response.sendRedirect("viewExpenses.jsp?message=" + java.net.URLEncoder.encode(msg, "UTF-8"));
        } else {
            response.sendRedirect("viewExpenses.jsp?error=No valid expenses found in CSV");
        }
    }
}
