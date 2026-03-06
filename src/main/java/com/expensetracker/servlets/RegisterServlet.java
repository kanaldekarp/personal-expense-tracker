package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.expensetracker.util.DBConnection;
import com.expensetracker.util.PasswordUtils;
import com.expensetracker.util.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Server-side validation
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.length() < 6) {
            response.sendRedirect("register.jsp?error=" + java.net.URLEncoder.encode("All fields are required. Password must be at least 6 characters.", "UTF-8"));
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check if email already exists
            PreparedStatement checkEmail = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
            checkEmail.setString(1, email.trim().toLowerCase());
            ResultSet rsEmail = checkEmail.executeQuery();
            if (rsEmail.next()) {
                response.sendRedirect("register.jsp?error=" + java.net.URLEncoder.encode("This email is already registered. Please login instead.", "UTF-8"));
                return;
            }

            // Check if username already exists
            PreparedStatement checkUser = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            checkUser.setString(1, username.trim());
            ResultSet rsUser = checkUser.executeQuery();
            if (rsUser.next()) {
                response.sendRedirect("register.jsp?error=" + java.net.URLEncoder.encode("Username already taken. Please choose another.", "UTF-8"));
                return;
            }

            // Insert new user
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES (?, ?, ?)");
            stmt.setString(1, username.trim());
            stmt.setString(2, email.trim().toLowerCase());
            stmt.setString(3, PasswordUtils.hashPassword(password));
            stmt.executeUpdate();

            System.out.println("✅ New user registered: " + username + " (" + email + ")");

            // Send welcome email (async to not block the response)
            final String userEmail = email.trim().toLowerCase();
            final String userName = username.trim();
            new Thread(() -> {
                try {
                    EmailService.sendWelcomeEmail(userEmail, userName);
                } catch (Exception e) {
                    System.out.println("⚠️ Welcome email failed: " + e.getMessage());
                }
            }).start();

            response.sendRedirect("index.jsp?message=" + java.net.URLEncoder.encode("Registration successful! Welcome email sent. Please login.", "UTF-8"));

        } catch (SQLException e) {
            e.printStackTrace();
            String errorMsg = e.getMessage().contains("duplicate key")
                ? "Username or email already exists"
                : "Registration failed. Please try again.";
            response.sendRedirect("register.jsp?error=" + java.net.URLEncoder.encode(errorMsg, "UTF-8"));
        }
    }
}
