package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.expensetracker.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/UpdateExpenseServlet")
public class UpdateExpenseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Fetch expense data from the form
            int expenseId = Integer.parseInt(request.getParameter("id"));  // ID from form
            String title = request.getParameter("title");
            double amount = Double.parseDouble(request.getParameter("amount"));
            String category = request.getParameter("category");
            java.sql.Date date = java.sql.Date.valueOf(request.getParameter("date"));  // Date conversion
            String description = request.getParameter("description");

            // Validate expenseId
            if (expenseId <= 0) {
                response.sendRedirect("viewExpenses.jsp?error=Invalid expense ID");
                return;
            }

            // SQL query to update the expense
            String sql = "UPDATE expenses SET title = ?, amount = ?, category = ?, date = ?, description = ? WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, title);
                stmt.setDouble(2, amount);
                stmt.setString(3, category);
                stmt.setDate(4, date);
                stmt.setString(5, description);
                stmt.setInt(6, expenseId);

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    response.sendRedirect("viewExpenses.jsp?message=Expense updated successfully");
                } else {
                    response.sendRedirect("viewExpenses.jsp?error=Failed to update expense");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.sendRedirect("viewExpenses.jsp?error=" + e.getMessage());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect("viewExpenses.jsp?error=Invalid data format");
        }
    }
}
