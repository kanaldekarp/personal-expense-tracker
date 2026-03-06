package com.expensetracker.servlets;

import com.expensetracker.util.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/DownloadReportServlet")
public class DownloadReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("reportPath") == null) {
            response.sendRedirect("dashboard.jsp?error=No report available. Generate a report first.");
            return;
        }

        String reportPath = (String) session.getAttribute("reportPath");
        String fullPath = getServletContext().getRealPath("/") + reportPath;

        File file = new File(fullPath);

        if (!file.exists() || !file.canRead()) {
            response.sendRedirect("dashboard.jsp?error=Report file not found. Please generate a new report.");
            return;
        }

        // Set response headers for download
        String fileName = file.getName();
        String contentType = "application/octet-stream";
        if (fileName.endsWith(".xlsx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xls")) {
            contentType = "application/vnd.ms-excel";
        }
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLengthLong(file.length());

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                OutputStream out = response.getOutputStream()) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
        }

        // Also email the report if the user has an email address
        String userEmail = (String) session.getAttribute("userEmail");
        String username = (String) session.getAttribute("username");
        String fromDate = (String) session.getAttribute("reportFromDate");
        String toDate = (String) session.getAttribute("reportToDate");

        if (userEmail != null && !userEmail.isEmpty()) {
            final String email = userEmail;
            final String uname = username != null ? username : "User";
            final String from = fromDate != null ? fromDate : "N/A";
            final String to = toDate != null ? toDate : "N/A";
            final File rf = file;
            new Thread(() -> {
                try {
                    EmailService.sendReportEmail(email, uname, from, to, rf);
                } catch (Exception e) {
                    System.out.println("⚠️ Download report email failed: " + e.getMessage());
                }
            }).start();
        }
    }
}
