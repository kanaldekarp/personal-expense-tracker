package com.expensetracker.servlets;

import com.expensetracker.dao.IncomeDAO;
import com.expensetracker.model.Income;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/IncomeServlet")
public class IncomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final IncomeDAO dao = new IncomeDAO();

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
            dao.deleteIncome(id, userId);
            response.sendRedirect("income.jsp?message=Income deleted successfully");
            return;
        }

        request.getRequestDispatcher("income.jsp").forward(request, response);
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
            
            if ("update".equals(action)) {
                Income income = new Income();
                income.setId(Integer.parseInt(request.getParameter("id")));
                income.setUserId(userId);
                income.setSource(request.getParameter("source"));
                income.setAmount(Double.parseDouble(request.getParameter("amount")));
                income.setDate(LocalDate.parse(request.getParameter("date")));
                income.setRecurring("on".equals(request.getParameter("recurring")));
                income.setNotes(request.getParameter("notes"));
                dao.updateIncome(income);
                response.sendRedirect("income.jsp?message=Income updated successfully");
            } else {
                Income income = new Income();
                income.setUserId(userId);
                income.setSource(request.getParameter("source"));
                income.setAmount(Double.parseDouble(request.getParameter("amount")));
                income.setDate(LocalDate.parse(request.getParameter("date")));
                income.setRecurring("on".equals(request.getParameter("recurring")));
                income.setNotes(request.getParameter("notes"));
                
                if (dao.addIncome(income)) {
                    response.sendRedirect("income.jsp?message=Income added successfully");
                } else {
                    response.sendRedirect("income.jsp?error=Failed to add income");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("income.jsp?error=Invalid input");
        }
    }
}
