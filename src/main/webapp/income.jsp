<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.Income, com.expensetracker.dao.IncomeDAO"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Income Tracking — QuickExpense</title>
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
    IncomeDAO incomeDAO = new IncomeDAO();
    List<Income> incomes = incomeDAO.getAllByUser(userId);
    double totalIncome = incomeDAO.getTotalIncome(userId);
    int month = java.time.LocalDate.now().getMonthValue();
    int year = java.time.LocalDate.now().getYear();
    double monthlyIncome = incomeDAO.getTotalIncomeByMonth(userId, month, year);
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-wallet" style="color:var(--success);margin-right:8px;"></i>Income Tracking</h2>
        <p>Track all your income sources in one place</p>
    </div>
</div>

<!-- Income Summary Cards -->
<div class="summary-cards" style="grid-template-columns: repeat(3, 1fr);">
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--success-light);color:var(--success);"><i class="fas fa-arrow-trend-up"></i></div>
        <div class="summary-info">
            <h4>Total Income</h4>
            <div class="summary-value" style="color:var(--success);"><%= cs %><%= String.format("%,.2f", totalIncome) %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--primary-100);color:var(--primary);"><i class="fas fa-calendar-check"></i></div>
        <div class="summary-info">
            <h4>This Month</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.2f", monthlyIncome) %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--warning-light);color:var(--warning);"><i class="fas fa-list-ol"></i></div>
        <div class="summary-info">
            <h4>Total Entries</h4>
            <div class="summary-value"><%= incomes.size() %></div>
        </div>
    </div>
</div>

<!-- Add Income Form -->
<div class="form-container" style="max-width:100%;margin-bottom:24px;">
    <div class="form-card">
        <h3 style="margin-bottom:16px;display:flex;align-items:center;gap:8px;">
            <i class="fas fa-plus-circle" style="color:var(--success);"></i> Add New Income
        </h3>
        <form action="IncomeServlet" method="post">
            <div class="form-row" style="grid-template-columns:repeat(4,1fr);">
                <div class="form-group">
                    <label><i class="fas fa-tag"></i> Source <span class="required">*</span></label>
                    <input type="text" name="source" placeholder="e.g. Salary, Freelance" required maxlength="100">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-coins"></i> Amount (<%= cs %>) <span class="required">*</span></label>
                    <input type="number" name="amount" step="0.01" min="0.01" placeholder="0.00" required>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-calendar"></i> Date <span class="required">*</span></label>
                    <input type="date" name="date" required>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-sync-alt"></i> Recurring?</label>
                    <div style="display:flex;align-items:center;gap:8px;padding-top:6px;">
                        <input type="checkbox" name="recurring" id="recurringCheck" style="width:auto;">
                        <label for="recurringCheck" style="margin:0;font-size:0.85rem;color:var(--text-primary);">Yes, recurring</label>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label><i class="fas fa-sticky-note"></i> Notes</label>
                <input type="text" name="notes" placeholder="Optional notes...">
            </div>
            <button type="submit" class="btn btn-success" style="width:auto;"><i class="fas fa-check"></i> Add Income</button>
        </form>
    </div>
</div>

<!-- Income Table -->
<div class="table-wrapper">
    <div class="table-header">
        <h3><i class="fas fa-list" style="color:var(--success);margin-right:6px;"></i>Income Records</h3>
        <div class="table-search">
            <i class="fas fa-search"></i>
            <input type="text" id="searchInput" placeholder="Search income..." onkeyup="filterTable()">
        </div>
    </div>
    <div class="table-responsive">
    <table id="incomeTable">
        <thead>
            <tr><th>#</th><th>Source</th><th>Amount</th><th>Date</th><th>Recurring</th><th>Notes</th><th>Actions</th></tr>
        </thead>
        <tbody>
        <% if (incomes.isEmpty()) { %>
            <tr><td colspan="7">
                <div class="empty-state">
                    <i class="fas fa-wallet"></i>
                    <h3>No income recorded yet</h3>
                    <p>Start by adding your first income source above</p>
                </div>
            </td></tr>
        <% } else {
            int idx = 1;
            for (Income inc : incomes) { %>
            <tr>
                <td><%= idx++ %></td>
                <td><strong><%= inc.getSource() %></strong></td>
                <td style="color:var(--success);font-weight:700;"><%= cs %><%= String.format("%,.2f", inc.getAmount()) %></td>
                <td><%= inc.getDate() %></td>
                <td><%= inc.isRecurring() ? "<span class='badge' style='background:var(--success-light);color:var(--success);'>Yes</span>" : "<span class='badge' style='background:var(--gray-100);color:var(--gray-500);'>No</span>" %></td>
                <td class="description-cell"><%= inc.getNotes() != null && !inc.getNotes().isEmpty() ? inc.getNotes() : "<span class='text-muted'>—</span>" %></td>
                <td>
                    <div class="table-actions">
                        <a href="IncomeServlet?action=delete&id=<%= inc.getId() %>" class="action-delete" onclick="return confirm('Delete this income entry?');"><i class="fas fa-trash"></i></a>
                    </div>
                </td>
            </tr>
        <% }} %>
        </tbody>
        <% if (!incomes.isEmpty()) { %>
        <tfoot>
            <tr class="total-row">
                <td colspan="2" style="text-align:right;"><strong>Total</strong></td>
                <td style="color:var(--success);font-weight:700;"><strong><%= cs %><%= String.format("%,.2f", totalIncome) %></strong></td>
                <td colspan="4"></td>
            </tr>
        </tfoot>
        <% } %>
    </table>
    </div>
</div>

<script>
// Set today's date as default
document.querySelector('input[name="date"]').valueAsDate = new Date();

function filterTable() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    document.querySelectorAll('#incomeTable tbody tr').forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
    });
}
</script>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
