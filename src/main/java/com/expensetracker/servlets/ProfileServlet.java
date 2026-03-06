package com.expensetracker.servlets;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.util.PasswordUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ExpenseDAO dao = new ExpenseDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }
        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }
        int userId = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("updateProfile".equals(action)) {
                String username = request.getParameter("username");
                String email = request.getParameter("email");
                if (username != null && !username.trim().isEmpty() && email != null && !email.trim().isEmpty()) {
                    dao.updateUserProfile(userId, username.trim(), email.trim().toLowerCase());
                    session.setAttribute("username", username.trim());
                    session.setAttribute("userEmail", email.trim().toLowerCase());
                    response.sendRedirect("profile.jsp?message=Profile updated successfully");
                } else {
                    response.sendRedirect("profile.jsp?error=Username and email are required");
                }
            } else if ("changePassword".equals(action)) {
                String currentPassword = request.getParameter("currentPassword");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");

                if (newPassword == null || newPassword.length() < 6) {
                    response.sendRedirect("profile.jsp?error=Password must be at least 6 characters");
                    return;
                }
                if (!newPassword.equals(confirmPassword)) {
                    response.sendRedirect("profile.jsp?error=Passwords do not match");
                    return;
                }

                String hashedCurrent = PasswordUtils.hashPassword(currentPassword);
                if (!dao.verifyPassword(userId, hashedCurrent)) {
                    response.sendRedirect("profile.jsp?error=Current password is incorrect");
                    return;
                }

                String hashedNew = PasswordUtils.hashPassword(newPassword);
                dao.updateUserPassword(userId, hashedNew);
                response.sendRedirect("profile.jsp?message=Password changed successfully");
            } else {
                response.sendRedirect("profile.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("profile.jsp?error=An error occurred");
        }
    }
}
