<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.Expense"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Filtered Expenses — QuickExpense</title>
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
    List<Expense> expenses = (List<Expense>) s.getAttribute("expenseList");
    if (expenses == null) expenses = new ArrayList<>();
    String cs = (String) s.getAttribute("currencySymbol");
    if (cs == null) cs = "\u20B9";
    double total = 0;
    Map<String, Double> catMap = new LinkedHashMap<>();
    for (Expense e : expenses) {
        total += e.getAmount();
        catMap.merge(e.getCategory(), e.getAmount(), Double::sum);
    }
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-filter" style="color:var(--primary);margin-right:8px;"></i>Filtered Results</h2>
        <p><%= expenses.size() %> expense<%= expenses.size() != 1 ? "s" : "" %> found — Total: <%= cs %><%= String.format("%,.2f", total) %></p>
    </div>
    <div class="page-header-actions">
        <a href="DashboardServlet" class="btn btn-outline"><i class="fas fa-arrow-left"></i> Back to Dashboard</a>
        <a href="viewExpenses.jsp" class="btn btn-primary"><i class="fas fa-list"></i> All Expenses</a>
    </div>
</div>

<!-- Summary mini-cards for filtered data -->
<% if (!expenses.isEmpty()) { %>
<div class="summary-cards" style="grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));">
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-indian-rupee-sign"></i></div>
        <div class="summary-info"><h4>Total</h4><div class="summary-value"><%= cs %><%= String.format("%,.2f", total) %></div></div>
    </div>
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-receipt"></i></div>
        <div class="summary-info"><h4>Count</h4><div class="summary-value"><%= expenses.size() %></div></div>
    </div>
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-calculator"></i></div>
        <div class="summary-info"><h4>Average</h4><div class="summary-value"><%= cs %><%= String.format("%,.2f", total / expenses.size()) %></div></div>
    </div>
</div>
<% } %>

<div class="table-wrapper">
    <div class="table-header">
        <h3><i class="fas fa-table" style="color:var(--primary);margin-right:6px;"></i>Filtered Expenses</h3>
        <div class="table-search">
            <i class="fas fa-search"></i>
            <input type="text" id="searchInput" placeholder="Search..." onkeyup="filterTable()">
        </div>
    </div>
    <div class="table-responsive">
    <table id="expenseTable">
        <thead>
            <tr><th>#</th><th>Title</th><th>Category</th><th>Amount</th><th>Date</th><th>Description</th></tr>
        </thead>
        <tbody>
        <% if (expenses.isEmpty()) { %>
            <tr><td colspan="6">
                <div class="empty-state">
                    <i class="fas fa-search"></i>
                    <h3>No matching expenses</h3>
                    <p>Try adjusting your filter criteria</p>
                    <a href="DashboardServlet" class="btn btn-primary btn-sm"><i class="fas fa-arrow-left"></i> Back</a>
                </div>
            </td></tr>
        <% } else {
            int idx = 1;
            for (Expense e : expenses) {
                String badge = e.getCategory() != null ? e.getCategory().toLowerCase().replace(" ", "") : "others";
        %>
            <tr>
                <td><%= idx++ %></td>
                <td><strong><%= e.getTitle() %></strong></td>
                <td><span class="badge badge-<%= badge %>"><%= e.getCategory() %></span></td>
                <td class="amount-cell"><%= cs %><%= String.format("%,.2f", e.getAmount()) %></td>
                <td><%= e.getDate() %></td>
                <td class="description-cell"><%= e.getDescription() != null && !e.getDescription().isEmpty() ? e.getDescription() : "<span class='text-muted'>—</span>" %></td>
            </tr>
        <% }} %>
        </tbody>
        <% if (!expenses.isEmpty()) { %>
        <tfoot>
            <tr class="total-row">
                <td colspan="3" style="text-align:right;"><strong>Total</strong></td>
                <td class="amount-cell"><strong><%= cs %><%= String.format("%,.2f", total) %></strong></td>
                <td colspan="2"></td>
            </tr>
        </tfoot>
        <% } %>
    </table>
    </div>
</div>

<script>
function filterTable() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    document.querySelectorAll('#expenseTable tbody tr').forEach(function(row) {
        row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
    });
}
</script>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
