package com.expensetracker.servlets;

import com.expensetracker.dao.SavingsGoalDAO;
import com.expensetracker.model.SavingsGoal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/GoalsServlet")
public class GoalsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final SavingsGoalDAO dao = new SavingsGoalDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            dao.delete(id, userId);
            response.sendRedirect("goals.jsp?message=Goal deleted");
        } else if ("addFunds".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            double amount = Double.parseDouble(request.getParameter("amount"));
            dao.addSavedAmount(id, userId, amount);
            response.sendRedirect("goals.jsp?message=Funds added to goal");
        } else {
            request.getRequestDispatcher("goals.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        try {
            String action = request.getParameter("action");
            SavingsGoal goal = new SavingsGoal();
            goal.setUserId(userId);
            goal.setName(request.getParameter("name"));
            goal.setTargetAmount(Double.parseDouble(request.getParameter("targetAmount")));
            String deadline = request.getParameter("deadline");
            if (deadline != null && !deadline.isEmpty()) {
                goal.setDeadline(LocalDate.parse(deadline));
            }
            goal.setIcon(request.getParameter("icon") != null ? request.getParameter("icon") : "fa-bullseye");
            goal.setColor(request.getParameter("color") != null ? request.getParameter("color") : "#4f46e5");

            if ("update".equals(action)) {
                goal.setId(Integer.parseInt(request.getParameter("id")));
                goal.setSavedAmount(Double.parseDouble(request.getParameter("savedAmount")));
                dao.update(goal);
                response.sendRedirect("goals.jsp?message=Goal updated");
            } else {
                if (dao.add(goal)) {
                    response.sendRedirect("goals.jsp?message=Goal created successfully");
                } else {
                    response.sendRedirect("goals.jsp?error=Failed to create goal");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("goals.jsp?error=Invalid input");
        }
    }
}
