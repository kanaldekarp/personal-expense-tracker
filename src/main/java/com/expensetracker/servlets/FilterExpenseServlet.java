package com.expensetracker.servlets;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;

@WebServlet("/FilterExpenseServlet")
public class FilterExpenseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        String category = request.getParameter("category");

        ArrayList<Expense> expenseList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT * FROM expenses WHERE user_id = ? AND date >= ? AND date <= ?");
            
            if (category != null && !category.isEmpty()) {
                sql.append(" AND category = ?");
            }

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            if (category != null && !category.isEmpty()) {
                ps.setString(4, category);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expense exp = new Expense();
                exp.setId(rs.getInt("user_id"));
                exp.setAmount(rs.getDouble("amount"));
                exp.setCategory(rs.getString("category"));
                exp.setDate(rs.getDate("date").toLocalDate());
                exp.setDescription(rs.getString("description"));
                expenseList.add(exp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        session.setAttribute("expenseList", expenseList);  // Set filtered list in session
        response.sendRedirect("DashboardServlet?filter=true"); // Optional query param for clarity
    }
}
