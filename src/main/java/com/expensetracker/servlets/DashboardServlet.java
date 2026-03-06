package com.expensetracker.servlets;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;
import com.expensetracker.util.Summary;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        ExpenseDAO dao = new ExpenseDAO();

        // Read filter parameters from request
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String category = request.getParameter("category");

        List<Expense> expenseList;

        // Apply filters if parameters are present
        if (fromDate != null && toDate != null && !fromDate.isEmpty() && !toDate.isEmpty()) {
            if (category != null && !category.isEmpty()) {
                // Filter by date AND category
                expenseList = dao.getFilteredExpensesByDateAndCategory(userId, fromDate, toDate, category);
            } else {
                // Filter by date only
                expenseList = dao.getFilteredExpensesByDate(userId, fromDate, toDate);
            }
        } else {
            // No filters — get all expenses
            expenseList = dao.getAllExpensesByUser(userId);
        }

        // Store the list in session
        session.setAttribute("expenseList", expenseList);

        // Update summary data
        if (expenseList != null && !expenseList.isEmpty()) {
            String cs = (String) session.getAttribute("currencySymbol");
            session.setAttribute("totalAmount", Summary.getTotalAmount(expenseList));
            session.setAttribute("topCategory", Summary.getTopCategory(expenseList));
            session.setAttribute("expenseCount", Summary.getExpenseCount(expenseList));
            session.setAttribute("recentSpending", Summary.getRecentExpense(expenseList, cs));
        } else {
            session.setAttribute("totalAmount", 0.0);
            session.setAttribute("topCategory", "-");
            session.setAttribute("expenseCount", 0);
            session.setAttribute("recentSpending", "N/A");
        }

        // Go back to dashboard
        response.sendRedirect("dashboard.jsp");
    }
}
