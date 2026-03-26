<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Update Expense — QuickExpense</title>
    <link rel="stylesheet" href="css/style.css">
    <%@ include file="includes/header.jspf" %>
</head>
<body>
<%
    HttpSession s = request.getSession(false);
    if (s == null || s.getAttribute("userId") == null) {
        response.sendRedirect("index.jsp?error=Please login first");
        return;
    }
    String id = request.getParameter("id");
    String title = request.getParameter("title");
    String amount = request.getParameter("amount");
    String category = request.getParameter("category");
    String date = request.getParameter("date");
    String description = request.getParameter("description");

    if (id == null || id.isEmpty()) {
        response.sendRedirect("viewExpenses.jsp?error=No expense selected");
        return;
    }
    String cs = (String) s.getAttribute("currencySymbol");
    if (cs == null) cs = "\u20B9";
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-edit" style="color:var(--primary);margin-right:8px;"></i>Update Expense</h2>
        <p>Modify the details below and save changes</p>
    </div>
</div>

<div class="form-container">
    <div class="form-card">
        <form action="UpdateExpenseServlet" method="post">
            <input type="hidden" name="id" value="<%= id %>">
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fas fa-tag"></i> Title <span class="required">*</span></label>
                    <input type="text" name="title" value="<%= title != null ? title : "" %>" required maxlength="100">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-coins"></i> Amount (<%= cs %>) <span class="required">*</span></label>
                    <input type="number" name="amount" step="0.01" min="0.01" value="<%= amount != null ? amount : "" %>" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fas fa-folder"></i> Category <span class="required">*</span></label>
                    <select name="category" required>
                        <option value="">Select Category</option>
                        <%
                            String[] cats = {"Food","Travel","Shopping","Bills","Entertainment","Health","Education","Others"};
                            String[] emojis = {"🍔","✈️","🛍️","📄","🎬","💊","📚","📦"};
                            for (int i = 0; i < cats.length; i++) {
                                String sel = cats[i].equals(category) ? "selected" : "";
                        %>
                        <option value="<%= cats[i] %>" <%= sel %>><%= emojis[i] %> <%= cats[i] %></option>
                        <% } %>
                    </select>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-calendar"></i> Date <span class="required">*</span></label>
                    <input type="date" name="date" value="<%= date != null ? date : "" %>" required>
                </div>
            </div>
            <div class="form-group">
                <label><i class="fas fa-align-left"></i> Description</label>
                <textarea name="description" rows="3" placeholder="Optional notes..."><%= description != null ? description : "" %></textarea>
            </div>
            <div class="form-actions">
                <a href="viewExpenses.jsp" class="btn btn-outline"><i class="fas fa-arrow-left"></i> Cancel</a>
                <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Update Expense</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
