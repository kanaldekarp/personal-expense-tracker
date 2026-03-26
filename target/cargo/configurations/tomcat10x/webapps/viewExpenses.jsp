<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.Expense, com.expensetracker.dao.ExpenseDAO"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>View Expenses — QuickExpense</title>
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
    ExpenseDAO dao = new ExpenseDAO();
    List<Expense> expenses = dao.getAllExpensesByUser(userId);
    double total = 0;
    for (Expense e : expenses) total += e.getAmount();
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-list-alt" style="color:var(--primary);margin-right:8px;"></i>All Expenses</h2>
        <p><%= expenses.size() %> transaction<%= expenses.size() != 1 ? "s" : "" %> — Total: <%= cs %><%= String.format("%,.2f", total) %></p>
    </div>
    <div class="page-header-actions">
        <button class="btn btn-outline" onclick="document.getElementById('csvModal').style.display='flex'"><i class="fas fa-file-csv"></i> Import CSV</button>
        <a href="addExpense.jsp" class="btn btn-primary"><i class="fas fa-plus"></i> Add New</a>
        <a href="DashboardServlet" class="btn btn-outline"><i class="fas fa-chart-line"></i> Dashboard</a>
    </div>
</div>

<div class="table-wrapper">
    <div class="table-header">
        <h3><i class="fas fa-receipt" style="color:var(--primary);margin-right:6px;"></i>Expense Records</h3>
        <div class="table-search">
            <i class="fas fa-search"></i>
            <input type="text" id="searchInput" placeholder="Search expenses..." onkeyup="filterTable()">
        </div>
    </div>
    <div class="table-responsive">
    <table id="expenseTable">
        <thead>
            <tr>
                <th onclick="sortTable(0)" style="cursor:pointer;"># <i class="fas fa-sort"></i></th>
                <th onclick="sortTable(1)" style="cursor:pointer;">Title <i class="fas fa-sort"></i></th>
                <th onclick="sortTable(2)" style="cursor:pointer;">Category <i class="fas fa-sort"></i></th>
                <th onclick="sortTable(3)" style="cursor:pointer;">Amount <i class="fas fa-sort"></i></th>
                <th onclick="sortTable(4)" style="cursor:pointer;">Date <i class="fas fa-sort"></i></th>
                <th>Description</th>
                <th>Tags</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <% if (expenses.isEmpty()) { %>
            <tr><td colspan="8">
                <div class="empty-state">
                    <i class="fas fa-inbox"></i>
                    <h3>No expenses found</h3>
                    <p>Start by adding your first expense or import from CSV</p>
                    <a href="addExpense.jsp" class="btn btn-primary btn-sm"><i class="fas fa-plus"></i> Add Expense</a>
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
                <td class="description-cell" title="<%= e.getDescription() != null ? e.getDescription() : "" %>">
                    <%= e.getDescription() != null && !e.getDescription().isEmpty() ? e.getDescription() : "<span class='text-muted'>—</span>" %>
                </td>
                <td>
                    <% if (e.getTags() != null && !e.getTags().isEmpty()) {
                        String[] tags = e.getTags().split(",");
                        for (String t : tags) { %>
                        <span style="display:inline-block;background:rgba(79,70,229,0.1);color:var(--primary);padding:2px 8px;border-radius:999px;font-size:0.72rem;margin:1px;"><%= t.trim() %></span>
                    <% }} else { %>
                        <span class="text-muted">—</span>
                    <% } %>
                </td>
                <td>
                    <div class="table-actions">
                        <a href="updateExpense.jsp?id=<%= e.getId() %>&title=<%= java.net.URLEncoder.encode(e.getTitle(), "UTF-8") %>&amount=<%= e.getAmount() %>&category=<%= java.net.URLEncoder.encode(e.getCategory(), "UTF-8") %>&date=<%= e.getDate() %>&description=<%= e.getDescription() != null ? java.net.URLEncoder.encode(e.getDescription(), "UTF-8") : "" %>"
                           class="action-edit" title="Edit"><i class="fas fa-pen"></i> Edit</a>
                        <a href="DeleteExpenseServlet?id=<%= e.getId() %>"
                           class="action-delete" title="Delete"
                           onclick="return confirm('Delete this expense?');"><i class="fas fa-trash"></i> Delete</a>
                    </div>
                </td>
            </tr>
        <% }} %>
        </tbody>
        <% if (!expenses.isEmpty()) { %>
        <tfoot>
            <tr class="total-row">
                <td colspan="3" style="text-align:right;"><strong>Total</strong></td>
                <td class="amount-cell"><strong><%= cs %><%= String.format("%,.2f", total) %></strong></td>
                <td colspan="4"></td>
            </tr>
        </tfoot>
        <% } %>
    </table>
    </div>
</div>

<script>
function filterTable() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    const rows = document.querySelectorAll('#expenseTable tbody tr');
    rows.forEach(function(row) {
        row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
    });
}

var sortDir = {};
function sortTable(col) {
    var table = document.getElementById('expenseTable');
    var rows = Array.from(table.tBodies[0].rows);
    sortDir[col] = !sortDir[col];
    rows.sort(function(a, b) {
        var x = a.cells[col].textContent.trim();
        var y = b.cells[col].textContent.trim();
        var xn = parseFloat(x.replace(/[^0-9.\-]/g, ''));
        var yn = parseFloat(y.replace(/[^0-9.\-]/g, ''));
        if (!isNaN(xn) && !isNaN(yn)) {
            return sortDir[col] ? xn - yn : yn - xn;
        }
        return sortDir[col] ? x.localeCompare(y) : y.localeCompare(x);
    });
    rows.forEach(function(r) { table.tBodies[0].appendChild(r); });
}
</script>

<!-- CSV Import Modal -->
<div id="csvModal" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);z-index:9999;align-items:center;justify-content:center;">
    <div style="background:var(--bg-primary, #fff);border-radius:16px;padding:32px;max-width:500px;width:90%;box-shadow:0 20px 60px rgba(0,0,0,0.3);">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
            <h3 style="margin:0;"><i class="fas fa-file-csv" style="color:var(--primary);margin-right:8px;"></i>Import from CSV</h3>
            <button onclick="document.getElementById('csvModal').style.display='none'" style="background:none;border:none;font-size:1.2rem;cursor:pointer;color:var(--text-muted);width:auto;padding:4px;"><i class="fas fa-times"></i></button>
        </div>
        <p style="font-size:0.85rem;color:var(--text-muted);margin-bottom:16px;">Upload a CSV file with columns: <strong>title, amount, category, date, description</strong></p>
        <form action="CSVImportServlet" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <input type="file" name="csvFile" accept=".csv" required style="padding:10px;">
            </div>
            <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:16px;">
                <button type="button" class="btn btn-outline" onclick="document.getElementById('csvModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary"><i class="fas fa-upload"></i> Import</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
