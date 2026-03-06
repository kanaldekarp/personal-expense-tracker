package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.expensetracker.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/DeleteExpenseServlet")
public class DeleteExpenseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        int expenseId = Integer.parseInt(request.getParameter("id")); // Get expense ID from request

        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM expenses WHERE id = ?")) {
            stmt.setInt(1, expenseId);
            int rowsDeleted = stmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                response.sendRedirect("dashboard.jsp?message=Expense deleted successfully");
            } else {
                response.sendRedirect("dashboard.jsp?error=Failed to delete expense");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("dashboard.jsp?error=Database error occurred");
        }
    }
}
