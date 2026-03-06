package com.expensetracker.servlets;

import com.expensetracker.dao.TagDAO;
import com.expensetracker.model.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/TagServlet")
public class TagServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TagDAO dao = new TagDAO();

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
            dao.deleteTag(id, userId);
            response.sendRedirect("viewExpenses.jsp?message=Tag deleted");
        } else {
            response.sendRedirect("viewExpenses.jsp");
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
            Tag tag = new Tag();
            tag.setUserId(userId);
            tag.setName(request.getParameter("name"));
            tag.setColor(request.getParameter("color") != null ? request.getParameter("color") : "#6366f1");
            dao.addTag(tag);
            response.sendRedirect("viewExpenses.jsp?message=Tag created");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("viewExpenses.jsp?error=Failed to create tag");
        }
    }
}
