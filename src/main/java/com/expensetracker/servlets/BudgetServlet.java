package com.expensetracker.servlets;

import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.model.Budget;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/BudgetServlet")
public class BudgetServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        // Delete action
        String deleteId = request.getParameter("delete");
        if (deleteId != null && !deleteId.isEmpty()) {
            int userId = (int) session.getAttribute("userId");
            BudgetDAO dao = new BudgetDAO();
            dao.deleteBudget(Integer.parseInt(deleteId), userId);
            response.sendRedirect("budget.jsp?message=Budget+entry+deleted");
            return;
        }

        // Copy from previous month
        String copyFrom = request.getParameter("copyFrom");
        if ("prev".equals(copyFrom)) {
            int userId = (int) session.getAttribute("userId");
            LocalDate now = LocalDate.now();
            LocalDate prev = now.minusMonths(1);
            BudgetDAO dao = new BudgetDAO();
            int count = dao.copyBudgets(userId, prev.getMonthValue(), prev.getYear(),
                                        now.getMonthValue(), now.getYear());
            String msg = count > 0 ? count + " budgets copied from last month" : "No budgets found in last month to copy";
            response.sendRedirect("budget.jsp?message=" + java.net.URLEncoder.encode(msg, "UTF-8"));
            return;
        }

        response.sendRedirect("budget.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String category = request.getParameter("category");
        String amountStr = request.getParameter("budgetAmount");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("year");
        String budgetType = request.getParameter("budgetType"); // "total" or "category"

        if (amountStr == null || amountStr.isEmpty()) {
            response.sendRedirect("budget.jsp?error=Budget+amount+is+required");
            return;
        }

        // If budgetType is "total", use the special __TOTAL__ category
        if ("total".equals(budgetType)) {
            category = BudgetDAO.TOTAL_CATEGORY;
        }

        if (category == null || category.isEmpty()) {
            response.sendRedirect("budget.jsp?error=Category+is+required");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);

            if (amount <= 0) {
                response.sendRedirect("budget.jsp?error=Budget+amount+must+be+positive");
                return;
            }

            Budget budget = new Budget(userId, category, amount, month, year);
            BudgetDAO dao = new BudgetDAO();

            if (dao.saveBudget(budget)) {
                String msg = BudgetDAO.TOTAL_CATEGORY.equals(category) ? "Total budget saved" : "Budget saved for " + category;
                response.sendRedirect("budget.jsp?message=" + java.net.URLEncoder.encode(msg, "UTF-8"));
            } else {
                response.sendRedirect("budget.jsp?error=Failed+to+save+budget");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("budget.jsp?error=Invalid+number+format");
        }
    }
}
