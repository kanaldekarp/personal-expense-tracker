package com.expensetracker.servlets;

import com.expensetracker.dao.RecurringExpenseDAO;
import com.expensetracker.model.RecurringExpense;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/RecurringExpenseServlet")
public class RecurringExpenseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RecurringExpenseDAO dao = new RecurringExpenseDAO();

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
            response.sendRedirect("recurring.jsp?message=Recurring expense deleted");
        } else if ("toggle".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            dao.toggleActive(id, userId);
            response.sendRedirect("recurring.jsp?message=Status toggled");
        } else if ("process".equals(action)) {
            int count = dao.processDueExpenses(userId);
            response.sendRedirect("recurring.jsp?message=" + count + " due expenses processed");
        } else {
            request.getRequestDispatcher("recurring.jsp").forward(request, response);
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
            RecurringExpense re = new RecurringExpense();
            re.setUserId(userId);
            re.setTitle(request.getParameter("title"));
            re.setCategory(request.getParameter("category"));
            re.setAmount(Double.parseDouble(request.getParameter("amount")));
            re.setFrequency(request.getParameter("frequency"));
            re.setNextDue(LocalDate.parse(request.getParameter("nextDue")));
            re.setDescription(request.getParameter("description"));
            re.setActive(true);

            if ("update".equals(action)) {
                re.setId(Integer.parseInt(request.getParameter("id")));
                dao.update(re);
                response.sendRedirect("recurring.jsp?message=Recurring expense updated");
            } else {
                if (dao.add(re)) {
                    response.sendRedirect("recurring.jsp?message=Recurring expense added");
                } else {
                    response.sendRedirect("recurring.jsp?error=Failed to add");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("recurring.jsp?error=Invalid input");
        }
    }
}
