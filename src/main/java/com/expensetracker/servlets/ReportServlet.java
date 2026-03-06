package com.expensetracker.servlets;

import com.expensetracker.util.ExcelReportGenerator;
import com.expensetracker.util.PDFReportGenerator;
import com.expensetracker.util.EmailService;
import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/ReportServlet")
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("dashboard.jsp");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            String username = (String) session.getAttribute("username");
            String userEmail = (String) session.getAttribute("userEmail");
            String currencySymbol = (String) session.getAttribute("currencySymbol");
            if (currencySymbol == null) currencySymbol = "₹";

            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");

            if (fromDateStr == null || toDateStr == null || fromDateStr.isEmpty() || toDateStr.isEmpty()) {
                response.sendRedirect("dashboard.jsp?error=" + java.net.URLEncoder.encode("Date range is required for report generation", "UTF-8"));
                return;
            }

            LocalDate fromDate = LocalDate.parse(fromDateStr);
            LocalDate toDate = LocalDate.parse(toDateStr);

            // Generate report based on format
            String format = request.getParameter("format");
            if (format == null) format = "excel";

            String reportsDir = getServletContext().getRealPath("/") + "reports/";
            new File(reportsDir).mkdirs();

            if ("pdf".equals(format)) {
                // Generate PDF
                ExpenseDAO expenseDAO = new ExpenseDAO();
                List<Expense> expenses = expenseDAO.getFilteredExpenses(userId, fromDateStr, toDateStr, null);
                String filePath = PDFReportGenerator.generateReport(expenses, username, currencySymbol, fromDateStr, toDateStr, reportsDir);
                if (filePath != null) {
                    File reportFile = new File(filePath);
                    String relativePath = "/reports/" + reportFile.getName();
                    session.setAttribute("reportPath", relativePath);
                    session.setAttribute("reportFromDate", fromDateStr);
                    session.setAttribute("reportToDate", toDateStr);

                    if (userEmail != null && !userEmail.isEmpty() && reportFile.exists()) {
                        final String email = userEmail;
                        final String uname = username != null ? username : "User";
                        final String from = fromDateStr;
                        final String to = toDateStr;
                        final File rf = reportFile;
                        new Thread(() -> {
                            try { EmailService.sendReportEmail(email, uname, from, to, rf); }
                            catch (Exception e) { System.out.println("⚠️ Report email failed: " + e.getMessage()); }
                        }).start();
                    }
                    String msg = userEmail != null && !userEmail.isEmpty()
                        ? "PDF report generated! Also emailed to " + userEmail
                        : "PDF report generated successfully!";
                    response.sendRedirect("dashboard.jsp?message=" + java.net.URLEncoder.encode(msg, "UTF-8"));
                } else {
                    response.sendRedirect("dashboard.jsp?error=" + java.net.URLEncoder.encode("Failed to generate PDF report", "UTF-8"));
                }
            } else {
                // Generate Excel file (existing logic)
                String fileName = "ExpenseReport_" + userId + "_" + System.currentTimeMillis() + ".xlsx";
                String filePath = reportsDir + fileName;

            File reportFile = ExcelReportGenerator.generateReport(userId, fromDate, toDate,
                    filePath, currencySymbol, username);

            // Save path for download
            String relativePath = "/reports/" + fileName;
            session.setAttribute("reportPath", relativePath);
            session.setAttribute("reportFromDate", fromDateStr);
            session.setAttribute("reportToDate", toDateStr);

            // Auto-email the report if user has email
            if (userEmail != null && !userEmail.isEmpty() && reportFile.exists()) {
                final String email = userEmail;
                final String uname = username != null ? username : "User";
                final String from = fromDateStr;
                final String to = toDateStr;
                final File rf = reportFile;
                new Thread(() -> {
                    try {
                        EmailService.sendReportEmail(email, uname, from, to, rf);
                    } catch (Exception e) {
                        System.out.println("⚠️ Report email failed: " + e.getMessage());
                    }
                }).start();
            }

            String msg = userEmail != null && !userEmail.isEmpty()
                ? "Excel report generated! Also emailed to " + userEmail
                : "Excel report generated successfully!";
            response.sendRedirect("dashboard.jsp?message=" + java.net.URLEncoder.encode(msg, "UTF-8"));
            } // end else (excel format)

        } catch (DateTimeParseException | NumberFormatException e) {
            response.sendRedirect("dashboard.jsp?error=" + java.net.URLEncoder.encode("Invalid date format", "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("dashboard.jsp?error=" + java.net.URLEncoder.encode("Something went wrong generating the report: " + e.getMessage(), "UTF-8"));
        }
    }
}
