package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.expensetracker.util.DBConnection;
import com.expensetracker.util.PasswordUtils;

/**
 * Handles the actual password reset after the user clicks the email link
 * and submits a new password.
 */
@WebServlet("/ResetPasswordServlet")
public class ResetPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate inputs
        if (token == null || token.isEmpty()) {
            response.sendRedirect("forgotPassword.jsp?error=" +
                java.net.URLEncoder.encode("Invalid reset token. Please request a new link.", "UTF-8"));
            return;
        }

        if (password == null || password.length() < 6) {
            response.sendRedirect("resetPassword.jsp?token=" + token + "&error=" +
                java.net.URLEncoder.encode("Password must be at least 6 characters long.", "UTF-8"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            response.sendRedirect("resetPassword.jsp?token=" + token + "&error=" +
                java.net.URLEncoder.encode("Passwords do not match.", "UTF-8"));
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Find valid, unused, non-expired token
            PreparedStatement ps = con.prepareStatement(
                "SELECT user_id FROM password_reset_tokens WHERE token = ? AND used = FALSE AND expires_at > NOW()");
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendRedirect("forgotPassword.jsp?error=" +
                    java.net.URLEncoder.encode("This reset link has expired or already been used. Please request a new one.", "UTF-8"));
                return;
            }

            int userId = rs.getInt("user_id");

            // Update password using hashed version
            String hashedPassword = PasswordUtils.hashPassword(password);
            PreparedStatement updatePs = con.prepareStatement("UPDATE users SET password = ? WHERE id = ?");
            updatePs.setString(1, hashedPassword);
            updatePs.setInt(2, userId);
            int updated = updatePs.executeUpdate();

            if (updated > 0) {
                // Mark token as used
                PreparedStatement markUsed = con.prepareStatement(
                    "UPDATE password_reset_tokens SET used = TRUE WHERE token = ?");
                markUsed.setString(1, token);
                markUsed.executeUpdate();

                System.out.println("✅ Password reset successful for user ID: " + userId);

                response.sendRedirect("index.jsp?message=" +
                    java.net.URLEncoder.encode("Password reset successful! You can now log in with your new password.", "UTF-8"));
            } else {
                response.sendRedirect("resetPassword.jsp?token=" + token + "&error=" +
                    java.net.URLEncoder.encode("Failed to reset password. Please try again.", "UTF-8"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("forgotPassword.jsp?error=" +
                java.net.URLEncoder.encode("Something went wrong. Please try again.", "UTF-8"));
        }
    }
}
