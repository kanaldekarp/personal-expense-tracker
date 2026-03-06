package com.expensetracker.servlets;

import java.io.IOException;
import java.time.LocalDate;

import com.expensetracker.model.Expense;
import com.expensetracker.dao.ExpenseDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/AddExpenseServlet")
public class AddExpenseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        Object userIdObj = session.getAttribute("userId");
        int userId = 0;
        try {
            userId = Integer.parseInt(userIdObj.toString());
        } catch (Exception e) {
            response.sendRedirect("index.jsp?error=Invalid user session");
            return;
        }

        try {
            String title = request.getParameter("title");
            double amount = Double.parseDouble(request.getParameter("amount"));
            String category = request.getParameter("category");
            LocalDate date = LocalDate.parse(request.getParameter("date"));
            String description = request.getParameter("description");
            String tags = request.getParameter("tags");

            Expense expense = new Expense();
            expense.setUserId(userId);
            expense.setTitle(title);
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDate(date);
            expense.setDescription(description);
            expense.setTags(tags);

            ExpenseDAO expenseDAO = new ExpenseDAO();
            boolean isAdded = expenseDAO.addExpense(expense);

            if (isAdded) {
                response.sendRedirect("DashboardServlet");
            } else {
                response.sendRedirect("addExpense.jsp?error=Failed to add expense");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("addExpense.jsp?error=Invalid input data");
        }
    }
}
