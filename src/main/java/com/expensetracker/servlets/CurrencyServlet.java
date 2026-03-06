package com.expensetracker.servlets;

import com.expensetracker.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/CurrencyServlet")
public class CurrencyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Supported currencies: symbol, code, name
    public static final String[][] CURRENCIES = {
        {"₹", "INR", "Indian Rupee"},
        {"$", "USD", "US Dollar"},
        {"€", "EUR", "Euro"},
        {"£", "GBP", "British Pound"},
        {"¥", "JPY", "Japanese Yen"},
        {"A$", "AUD", "Australian Dollar"},
        {"C$", "CAD", "Canadian Dollar"},
        {"CHF", "CHF", "Swiss Franc"},
        {"¥", "CNY", "Chinese Yuan"},
        {"₿", "BTC", "Bitcoin"}
    };

    public static String getSymbol(String code) {
        if (code == null) return "₹";
        for (String[] c : CURRENCIES) {
            if (c[1].equals(code)) return c[0];
        }
        return code;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("index.jsp?error=Please login first");
            return;
        }

        String currency = request.getParameter("currency");
        if (currency == null || currency.isEmpty()) {
            currency = "INR";
        }

        // Validate
        boolean valid = false;
        for (String[] c : CURRENCIES) {
            if (c[1].equals(currency)) { valid = true; break; }
        }
        if (!valid) currency = "INR";

        int userId = (int) session.getAttribute("userId");

        // Update in database
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE users SET currency = ? WHERE id = ?");
            ps.setString(1, currency);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update session
        session.setAttribute("currency", currency);
        session.setAttribute("currencySymbol", getSymbol(currency));

        // Redirect back to referring page or dashboard
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect("DashboardServlet");
        }
    }
}
