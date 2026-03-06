package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import com.expensetracker.util.DBConnection;
import com.expensetracker.util.PasswordUtils;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            response.sendRedirect("index.jsp?error=Email and password are required");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT id, username, email, currency FROM users WHERE email = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, PasswordUtils.hashPassword(password));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String userEmail = rs.getString("email");
                String currency = rs.getString("currency");
                if (currency == null || currency.isEmpty()) currency = "INR";

                // Create session and set attributes
                HttpSession session = request.getSession();
                session.setAttribute("userId", userId);
                session.setAttribute("username", username);
                session.setAttribute("userEmail", userEmail);
                session.setAttribute("currency", currency);
                session.setAttribute("currencySymbol", CurrencyServlet.getSymbol(currency));
                session.setMaxInactiveInterval(30 * 60); // 30 minutes session timeout

                System.out.println("✅ User logged in: " + username + " (" + userEmail + ") [ID: " + userId + "]");

                // Redirect to DashboardServlet to load summary+expenses
                response.sendRedirect("DashboardServlet");
            } else {
                response.sendRedirect("index.jsp?error=" + java.net.URLEncoder.encode("Invalid email or password", "UTF-8"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("index.jsp?error=" + java.net.URLEncoder.encode("Database error. Please try again.", "UTF-8"));
        }
    }
}
