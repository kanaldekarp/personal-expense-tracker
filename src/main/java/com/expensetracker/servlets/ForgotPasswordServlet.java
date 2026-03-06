package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.expensetracker.util.DBConnection;
import com.expensetracker.util.EmailService;

/**
 * Handles the "Forgot Password" form submission.
 * Generates a unique token, stores it in password_reset_tokens table,
 * and sends an email with the reset link via SMTP.
 */
@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            response.sendRedirect("forgotPassword.jsp?error=" +
                java.net.URLEncoder.encode("Please enter your email address", "UTF-8"));
            return;
        }

        email = email.trim().toLowerCase();

        try (Connection con = DBConnection.getConnection()) {
            // Check if email exists
            PreparedStatement checkPs = con.prepareStatement("SELECT id, username FROM users WHERE email = ?");
            checkPs.setString(1, email);
            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                // Don't reveal whether email exists — still show success for security
                response.sendRedirect("forgotPassword.jsp?message=" +
                    java.net.URLEncoder.encode("If that email is registered, you'll receive a reset link shortly.", "UTF-8"));
                return;
            }

            int userId = rs.getInt("id");
            String username = rs.getString("username");

            // Invalidate any existing unused tokens for this user
            PreparedStatement invalidatePs = con.prepareStatement(
                "UPDATE password_reset_tokens SET used = TRUE WHERE user_id = ? AND used = FALSE");
            invalidatePs.setInt(1, userId);
            invalidatePs.executeUpdate();

            // Generate a secure random token
            String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

            // Store token with 30-minute expiry
            PreparedStatement insertPs = con.prepareStatement(
                "INSERT INTO password_reset_tokens (user_id, token, expires_at) VALUES (?, ?, NOW() + INTERVAL '30 minutes')");
            insertPs.setInt(1, userId);
            insertPs.setString(2, token);
            insertPs.executeUpdate();

            // Build reset URL
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            String resetUrl = scheme + "://" + serverName;
            if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
                resetUrl += ":" + serverPort;
            }
            resetUrl += contextPath + "/resetPassword.jsp?token=" + token;

            // Send reset email via SMTP
            boolean emailSent = EmailService.sendPasswordResetEmail(email, username, resetUrl);

            if (emailSent) {
                System.out.println("✅ Password reset email sent to: " + email);
            } else {
                System.out.println("❌ Failed to send password reset email to: " + email);
            }

            // Always show success message (security: don't reveal if email exists)
            response.sendRedirect("forgotPassword.jsp?message=" +
                java.net.URLEncoder.encode("If that email is registered, you'll receive a reset link shortly. Check your inbox and spam folder.", "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("forgotPassword.jsp?error=" +
                java.net.URLEncoder.encode("Something went wrong. Please try again.", "UTF-8"));
        }
    }
}
