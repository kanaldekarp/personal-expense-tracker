<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Add Expense — QuickExpense</title>
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
    String cs = (String) s.getAttribute("currencySymbol");
    if (cs == null) cs = "\u20B9";
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-plus-circle" style="color:var(--primary);margin-right:8px;"></i>Add New Expense</h2>
        <p>Record a new transaction to keep your finances accurate</p>
    </div>
</div>

<div class="form-container">
    <div class="form-card">
        <form action="AddExpenseServlet" method="post" id="addExpenseForm" enctype="multipart/form-data">
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fas fa-tag"></i> Title <span class="required">*</span></label>
                    <input type="text" name="title" placeholder="e.g. Grocery Shopping" required maxlength="100">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-coins"></i> Amount (<%= cs %>) <span class="required">*</span></label>
                    <input type="number" name="amount" step="0.01" min="0.01" placeholder="0.00" required>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fas fa-folder"></i> Category <span class="required">*</span></label>
                    <select name="category" required>
                        <option value="">Select Category</option>
                        <option value="Food">🍔 Food</option>
                        <option value="Travel">✈️ Travel</option>
                        <option value="Shopping">🛍️ Shopping</option>
                        <option value="Bills">📄 Bills</option>
                        <option value="Entertainment">🎬 Entertainment</option>
                        <option value="Health">💊 Health</option>
                        <option value="Education">📚 Education</option>
                        <option value="Others">📦 Others</option>
                    </select>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-calendar"></i> Date <span class="required">*</span></label>
                    <input type="date" name="date" required>
                </div>
            </div>
            <div class="form-group">
                <label><i class="fas fa-align-left"></i> Description</label>
                <textarea name="description" rows="3" placeholder="Optional notes about this expense..."></textarea>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label><i class="fas fa-tags"></i> Tags</label>
                    <input type="text" name="tags" placeholder="e.g. groceries, weekly, essential" maxlength="200">
                    <small style="color:var(--text-muted);font-size:0.75rem;">Comma-separated labels for categorization</small>
                </div>
            </div>
            <div class="form-actions">
                <a href="DashboardServlet" class="btn btn-outline"><i class="fas fa-arrow-left"></i> Cancel</a>
                <button type="submit" class="btn btn-primary"><i class="fas fa-check"></i> Save Expense</button>
            </div>
        </form>
    </div>
</div>

<script>
// Set default date to today
document.querySelector('input[name="date"]').valueAsDate = new Date();
</script>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
