<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.RecurringExpense, com.expensetracker.dao.RecurringExpenseDAO"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Recurring Expenses — QuickExpense</title>
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
    int userId = (int) s.getAttribute("userId");
    String cs = (String) s.getAttribute("currencySymbol");
    if (cs == null) cs = "\u20B9";
    RecurringExpenseDAO reDAO = new RecurringExpenseDAO();
    List<RecurringExpense> items = reDAO.getAllByUser(userId);
    double monthlyTotal = reDAO.getMonthlyRecurringTotal(userId);
    int activeCount = 0, pausedCount = 0;
    for (RecurringExpense re : items) { if (re.isActive()) activeCount++; else pausedCount++; }
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-sync-alt" style="color:var(--warning);margin-right:8px;"></i>Recurring Expenses</h2>
        <p>Manage your automatic recurring payments and subscriptions</p>
    </div>
    <div class="page-header-actions">
        <a href="RecurringExpenseServlet?action=process" class="btn btn-success"><i class="fas fa-play"></i> Process Due</a>
    </div>
</div>

<!-- Summary Cards -->
<div class="summary-cards" style="grid-template-columns: repeat(3, 1fr);">
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--warning-light);color:var(--warning);"><i class="fas fa-sync-alt"></i></div>
        <div class="summary-info">
            <h4>Monthly Recurring</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.2f", monthlyTotal) %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--success-light);color:var(--success);"><i class="fas fa-check-circle"></i></div>
        <div class="summary-info">
            <h4>Active</h4>
            <div class="summary-value"><%= activeCount %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--gray-100);color:var(--gray-500);"><i class="fas fa-pause-circle"></i></div>
        <div class="summary-info">
            <h4>Paused</h4>
            <div class="summary-value"><%= pausedCount %></div>
        </div>
    </div>
</div>

<!-- Add Recurring Expense Form -->
<div class="form-container" style="max-width:100%;margin-bottom:24px;">
    <div class="form-card">
        <h3 style="margin-bottom:16px;display:flex;align-items:center;gap:8px;">
            <i class="fas fa-plus-circle" style="color:var(--warning);"></i> Add Recurring Expense
        </h3>
        <form action="RecurringExpenseServlet" method="post">
            <div class="form-row" style="grid-template-columns:repeat(3,1fr);">
                <div class="form-group">
                    <label><i class="fas fa-tag"></i> Title <span class="required">*</span></label>
                    <input type="text" name="title" placeholder="e.g. Netflix, Rent" required maxlength="100">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-coins"></i> Amount (<%= cs %>) <span class="required">*</span></label>
                    <input type="number" name="amount" step="0.01" min="0.01" placeholder="0.00" required>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-folder"></i> Category <span class="required">*</span></label>
                    <select name="category" required>
                        <option value="">Select Category</option>
                        <option value="Bills">📄 Bills</option>
                        <option value="Entertainment">🎬 Entertainment</option>
                        <option value="Food">🍔 Food</option>
                        <option value="Health">💊 Health</option>
                        <option value="Education">📚 Education</option>
                        <option value="Shopping">🛍️ Shopping</option>
                        <option value="Travel">✈️ Travel</option>
                        <option value="Others">📦 Others</option>
                    </select>
                </div>
            </div>
            <div class="form-row" style="grid-template-columns:repeat(3,1fr);">
                <div class="form-group">
                    <label><i class="fas fa-clock"></i> Frequency <span class="required">*</span></label>
                    <select name="frequency" required>
                        <option value="daily">Daily</option>
                        <option value="weekly">Weekly</option>
                        <option value="monthly" selected>Monthly</option>
                        <option value="yearly">Yearly</option>
                    </select>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-calendar"></i> Next Due Date <span class="required">*</span></label>
                    <input type="date" name="nextDue" required>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-align-left"></i> Description</label>
                    <input type="text" name="description" placeholder="Optional details...">
                </div>
            </div>
            <button type="submit" class="btn btn-primary" style="width:auto;"><i class="fas fa-check"></i> Add Recurring</button>
        </form>
    </div>
</div>

<!-- Recurring Expenses Table -->
<div class="table-wrapper">
    <div class="table-header">
        <h3><i class="fas fa-sync-alt" style="color:var(--warning);margin-right:6px;"></i>Recurring Expenses</h3>
    </div>
    <div class="table-responsive">
    <table>
        <thead>
            <tr><th>#</th><th>Title</th><th>Category</th><th>Amount</th><th>Frequency</th><th>Next Due</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
        <% if (items.isEmpty()) { %>
            <tr><td colspan="8">
                <div class="empty-state">
                    <i class="fas fa-sync-alt"></i>
                    <h3>No recurring expenses</h3>
                    <p>Add your subscriptions and bills above</p>
                </div>
            </td></tr>
        <% } else {
            int idx = 1;
            for (RecurringExpense re : items) {
                String badge = re.getCategory() != null ? re.getCategory().toLowerCase().replace(" ", "") : "others";
                boolean overdue = re.isActive() && re.getNextDue().isBefore(java.time.LocalDate.now());
        %>
            <tr style="<%= !re.isActive() ? "opacity:0.5;" : "" %>">
                <td><%= idx++ %></td>
                <td><strong><%= re.getTitle() %></strong><%= overdue ? " <span style='color:var(--danger);font-size:0.75rem;'>(OVERDUE)</span>" : "" %></td>
                <td><span class="badge badge-<%= badge %>"><%= re.getCategory() %></span></td>
                <td class="amount-cell"><%= cs %><%= String.format("%,.2f", re.getAmount()) %></td>
                <td><span class="badge" style="background:var(--primary-50);color:var(--primary);text-transform:capitalize;"><%= re.getFrequency() %></span></td>
                <td><%= re.getNextDue() %></td>
                <td>
                    <% if (re.isActive()) { %>
                        <span class="badge" style="background:var(--success-light);color:var(--success);">Active</span>
                    <% } else { %>
                        <span class="badge" style="background:var(--gray-100);color:var(--gray-500);">Paused</span>
                    <% } %>
                </td>
                <td>
                    <div class="table-actions">
                        <a href="RecurringExpenseServlet?action=toggle&id=<%= re.getId() %>" class="action-edit" title="<%= re.isActive() ? "Pause" : "Resume" %>">
                            <i class="fas fa-<%= re.isActive() ? "pause" : "play" %>"></i>
                        </a>
                        <a href="RecurringExpenseServlet?action=delete&id=<%= re.getId() %>" class="action-delete" onclick="return confirm('Delete this recurring expense?');">
                            <i class="fas fa-trash"></i>
                        </a>
                    </div>
                </td>
            </tr>
        <% }} %>
        </tbody>
    </table>
    </div>
</div>

<script>
document.querySelector('input[name="nextDue"]').valueAsDate = new Date();
</script>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
