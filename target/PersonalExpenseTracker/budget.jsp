<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.Budget, com.expensetracker.model.Expense,
                 com.expensetracker.dao.BudgetDAO, com.expensetracker.dao.ExpenseDAO"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Budget — QuickExpense</title>
    <link rel="stylesheet" href="css/style.css">
    <%@ include file="includes/header.jspf" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
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
    if (cs == null) cs = "₹";

    // Current month/year
    java.time.LocalDate now = java.time.LocalDate.now();
    String selMonthParam = request.getParameter("viewMonth");
    String selYearParam = request.getParameter("viewYear");
    int viewMonth = selMonthParam != null ? Integer.parseInt(selMonthParam) : now.getMonthValue();
    int viewYear = selYearParam != null ? Integer.parseInt(selYearParam) : now.getYear();

    BudgetDAO budgetDAO = new BudgetDAO();
    ExpenseDAO expenseDAO = new ExpenseDAO();

    // Category budgets (excludes __TOTAL__)
    List<Budget> budgets = budgetDAO.getBudgetsByMonth(userId, viewMonth, viewYear);
    Map<String, Double> budgetMap = budgetDAO.getBudgetMap(userId, viewMonth, viewYear);

    // Overall total budget entry
    Budget overallBudget = budgetDAO.getOverallBudget(userId, viewMonth, viewYear);
    boolean hasOverallBudget = (overallBudget != null);
    double overallBudgetAmt = hasOverallBudget ? overallBudget.getBudgetAmount() : 0;

    // Get actual spending for the selected month
    java.time.YearMonth ym = java.time.YearMonth.of(viewYear, viewMonth);
    List<Expense> monthExpenses = expenseDAO.getExpensesByMonth(userId, ym);
    Map<String, Double> actualMap = ExpenseDAO.getCategoryTotals(monthExpenses);

    double categoryBudgetSum = 0, totalActual = 0;
    for (Budget b : budgets) categoryBudgetSum += b.getBudgetAmount();
    for (double v : actualMap.values()) totalActual += v;

    // Effective total budget
    double effectiveBudget = hasOverallBudget ? overallBudgetAmt : categoryBudgetSum;

    String[] categories = {"Food","Travel","Shopping","Bills","Entertainment","Health","Education","Others"};
    String[] emojis = {"🍔","✈️","🛍️","📄","🎬","💊","📚","📦"};
    String monthName = java.time.Month.of(viewMonth).toString();
    monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-piggy-bank" style="color:var(--primary);margin-right:8px;"></i>Budget Manager</h2>
        <p>Set spending limits — <%= monthName %> <%= viewYear %></p>
    </div>
    <div class="page-header-actions">
        <a href="BudgetServlet?copyFrom=prev" class="btn btn-outline" title="Copy last month's budgets"><i class="fas fa-copy"></i> Copy Last Month</a>
        <a href="DashboardServlet" class="btn btn-primary"><i class="fas fa-chart-line"></i> Dashboard</a>
    </div>
</div>

<!-- Month Selector -->
<div class="card mb-2" style="padding:14px 20px;">
    <form action="budget.jsp" method="get" style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
        <label style="font-weight:600;font-size:0.85rem;color:var(--text-secondary);">View Month:</label>
        <select name="viewMonth" style="padding:8px 12px;">
            <% for (int m = 1; m <= 12; m++) {
                String mn = java.time.Month.of(m).toString();
                mn = mn.charAt(0) + mn.substring(1).toLowerCase();
            %>
            <option value="<%= m %>" <%= m == viewMonth ? "selected" : "" %>><%= mn %></option>
            <% } %>
        </select>
        <select name="viewYear" style="padding:8px 12px;">
            <% for (int y = now.getYear() - 2; y <= now.getYear() + 1; y++) { %>
            <option value="<%= y %>" <%= y == viewYear ? "selected" : "" %>><%= y %></option>
            <% } %>
        </select>
        <button type="submit" class="btn btn-primary btn-sm"><i class="fas fa-eye"></i> View</button>
    </form>
</div>

<!-- Budget Overview Cards -->
<div class="summary-cards" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 24px;">
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-wallet"></i></div>
        <div class="summary-info">
            <h4>Total Budget</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.2f", effectiveBudget) %></div>
            <div style="font-size:0.7rem;color:var(--text-muted);"><%= hasOverallBudget ? "Overall limit" : (budgets.isEmpty() ? "Not set" : "Sum of categories") %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-shopping-cart"></i></div>
        <div class="summary-info">
            <h4>Actual Spent</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.2f", totalActual) %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-<%= (effectiveBudget > 0 && effectiveBudget - totalActual >= 0) ? "check-circle" : (effectiveBudget > 0 ? "exclamation-triangle" : "minus-circle") %>"></i></div>
        <div class="summary-info">
            <h4>Remaining</h4>
            <div class="summary-value" style="color:<%= (effectiveBudget > 0 && effectiveBudget - totalActual >= 0) ? "var(--success)" : (effectiveBudget > 0 ? "var(--danger)" : "var(--text-muted)") %>">
                <% if (effectiveBudget > 0) { %>
                    <%= cs %><%= String.format("%,.2f", Math.abs(effectiveBudget - totalActual)) %>
                    <%= (effectiveBudget - totalActual) < 0 ? " over" : "" %>
                <% } else { %>—<% } %>
            </div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon"><i class="fas fa-tags"></i></div>
        <div class="summary-info">
            <h4>Categories Set</h4>
            <div class="summary-value"><%= budgets.size() %> / <%= categories.length %></div>
        </div>
    </div>
</div>

<div class="dashboard-layout">
    <div>
        <!-- Overall Budget Bar (if set) -->
        <% if (hasOverallBudget) {
            double overPct = overallBudgetAmt > 0 ? (totalActual / overallBudgetAmt) * 100 : 0;
            String overColor = overPct > 100 ? "var(--danger)" : overPct > 80 ? "var(--warning)" : "var(--success)";
        %>
        <div class="card mb-2" style="padding:18px 24px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
                <h4 style="margin:0;font-size:0.95rem;">
                    <i class="fas fa-bullseye" style="color:var(--primary);margin-right:6px;"></i>Overall Budget Limit
                </h4>
                <div style="font-size:0.85rem;">
                    <span style="font-weight:600;"><%= cs %><%= String.format("%,.2f", totalActual) %></span>
                    <span style="color:var(--text-muted);"> / <%= cs %><%= String.format("%,.2f", overallBudgetAmt) %></span>
                    <a href="BudgetServlet?delete=<%= overallBudget.getId() %>" class="action-delete" style="margin-left:8px;font-size:0.75rem;"
                       onclick="return confirm('Remove overall budget limit?');"><i class="fas fa-times"></i></a>
                </div>
            </div>
            <div style="background:var(--gray-100);border-radius:999px;height:12px;overflow:hidden;">
                <div style="width:<%= Math.min(overPct, 100) %>%;height:100%;background:<%= overColor %>;border-radius:999px;transition:width 0.5s;"></div>
            </div>
            <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:var(--text-muted);margin-top:4px;">
                <span><%= String.format("%.1f", overPct) %>% used</span>
                <span><%= cs %><%= String.format("%,.2f", Math.max(0, overallBudgetAmt - totalActual)) %> remaining</span>
            </div>
        </div>
        <% } %>

        <!-- Budget vs Actual Table (Category-wise) -->
        <div class="table-wrapper">
            <div class="table-header">
                <h3><i class="fas fa-balance-scale" style="color:var(--primary);margin-right:6px;"></i>Category Budgets — <%= monthName %> <%= viewYear %></h3>
            </div>
            <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Category</th>
                        <th>Budget</th>
                        <th>Actual</th>
                        <th>Remaining</th>
                        <th>Usage</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <% if (budgets.isEmpty()) { %>
                    <tr><td colspan="6">
                        <div class="empty-state">
                            <i class="fas fa-tags"></i>
                            <h3>No category budgets set</h3>
                            <p>Set budgets per category using the form, or set an overall total budget</p>
                        </div>
                    </td></tr>
                <% } else {
                    for (Budget b : budgets) {
                        double actual = actualMap.getOrDefault(b.getCategory(), 0.0);
                        double remaining = b.getBudgetAmount() - actual;
                        double pct = b.getBudgetAmount() > 0 ? (actual / b.getBudgetAmount()) * 100 : 0;
                        String barColor = pct > 100 ? "var(--danger)" : pct > 80 ? "var(--warning)" : "var(--success)";
                %>
                    <tr>
                        <td><strong><%= b.getCategory() %></strong></td>
                        <td class="amount-cell"><%= cs %><%= String.format("%,.2f", b.getBudgetAmount()) %></td>
                        <td class="amount-cell"><%= cs %><%= String.format("%,.2f", actual) %></td>
                        <td class="amount-cell" style="color:<%= remaining >= 0 ? "var(--success)" : "var(--danger)" %>">
                            <%= remaining >= 0 ? "" : "-" %><%= cs %><%= String.format("%,.2f", Math.abs(remaining)) %>
                        </td>
                        <td style="min-width:120px;">
                            <div style="background:var(--gray-100);border-radius:999px;height:8px;overflow:hidden;">
                                <div style="width:<%= Math.min(pct, 100) %>%;height:100%;background:<%= barColor %>;border-radius:999px;transition:width 0.5s;"></div>
                            </div>
                            <span style="font-size:0.75rem;color:var(--text-muted);"><%= String.format("%.0f", pct) %>%</span>
                        </td>
                        <td>
                            <a href="BudgetServlet?delete=<%= b.getId() %>" class="action-delete btn-sm"
                               onclick="return confirm('Delete this budget?');"><i class="fas fa-trash"></i></a>
                        </td>
                    </tr>
                <% }} %>
                </tbody>
            </table>
            </div>
        </div>

        <!-- Chart -->
        <% if (!budgets.isEmpty()) { %>
        <div class="chart-box mt-3">
            <h4><i class="fas fa-chart-bar" style="color:var(--primary);margin-right:6px;"></i>Budget vs Actual</h4>
            <canvas id="budgetChart" style="max-height:300px;"></canvas>
        </div>
        <% } %>
    </div>

    <!-- Sidebar: Budget Forms -->
    <div>
        <!-- Budget Type Toggle -->
        <div class="sidebar-section">
            <h3><i class="fas fa-sliders-h"></i> Set Budget</h3>
            <!-- Toggle Tabs -->
            <div style="display:flex;gap:0;margin-bottom:16px;border-radius:8px;overflow:hidden;border:1px solid var(--gray-200);">
                <button onclick="switchBudgetTab('total')" id="tabTotal"
                        style="flex:1;padding:10px 8px;font-size:0.82rem;font-weight:600;border:none;cursor:pointer;transition:all 0.2s;background:var(--primary);color:#fff;">
                    <i class="fas fa-bullseye"></i> Total Budget
                </button>
                <button onclick="switchBudgetTab('category')" id="tabCategory"
                        style="flex:1;padding:10px 8px;font-size:0.82rem;font-weight:600;border:none;cursor:pointer;transition:all 0.2s;background:var(--bg-secondary);color:var(--text-secondary);">
                    <i class="fas fa-tags"></i> Per Category
                </button>
            </div>

            <!-- Total Budget Form -->
            <div id="formTotal">
                <form action="BudgetServlet" method="post">
                    <input type="hidden" name="budgetType" value="total">
                    <input type="hidden" name="month" value="<%= viewMonth %>">
                    <input type="hidden" name="year" value="<%= viewYear %>">
                    <div style="background:var(--gray-50);border-radius:8px;padding:12px;margin-bottom:12px;font-size:0.82rem;color:var(--text-secondary);">
                        <i class="fas fa-info-circle" style="color:var(--primary);"></i>
                        Set a single overall spending limit for the entire month. This tracks your total spending regardless of category.
                    </div>
                    <div class="form-group">
                        <label><i class="fas fa-coins"></i> Monthly Budget (<%= cs %>)</label>
                        <input type="number" name="budgetAmount" step="0.01" min="1" placeholder="e.g. 50000"
                               value="<%= hasOverallBudget ? String.format("%.2f", overallBudgetAmt) : "" %>" required>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">
                        <i class="fas fa-save"></i> <%= hasOverallBudget ? "Update" : "Set" %> Total Budget
                    </button>
                </form>
                <% if (hasOverallBudget) { %>
                <div style="margin-top:8px;padding:10px;background:var(--gray-50);border-radius:8px;font-size:0.8rem;color:var(--text-secondary);">
                    <i class="fas fa-check-circle" style="color:var(--success);"></i>
                    Current total budget: <strong><%= cs %><%= String.format("%,.2f", overallBudgetAmt) %></strong>
                </div>
                <% } %>
            </div>

            <!-- Category Budget Form -->
            <div id="formCategory" style="display:none;">
                <form action="BudgetServlet" method="post">
                    <input type="hidden" name="budgetType" value="category">
                    <input type="hidden" name="month" value="<%= viewMonth %>">
                    <input type="hidden" name="year" value="<%= viewYear %>">
                    <div style="background:var(--gray-50);border-radius:8px;padding:12px;margin-bottom:12px;font-size:0.82rem;color:var(--text-secondary);">
                        <i class="fas fa-info-circle" style="color:var(--primary);"></i>
                        Set individual spending limits per category for granular control over where your money goes.
                    </div>
                    <div class="form-group">
                        <label><i class="fas fa-folder"></i> Category</label>
                        <select name="category" required>
                            <option value="">Select Category</option>
                            <% for (int i = 0; i < categories.length; i++) {
                                boolean alreadySet = budgetMap.containsKey(categories[i]);
                            %>
                            <option value="<%= categories[i] %>"><%= emojis[i] %> <%= categories[i] %><%= alreadySet ? " ✓" : "" %></option>
                            <% } %>
                        </select>
                    </div>
                    <div class="form-group">
                        <label><i class="fas fa-coins"></i> Budget Amount (<%= cs %>)</label>
                        <input type="number" name="budgetAmount" step="0.01" min="1" placeholder="e.g. 5000" required>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block"><i class="fas fa-save"></i> Save Category Budget</button>
                </form>
            </div>
        </div>

        <!-- Tips -->
        <div class="sidebar-section">
            <h3><i class="fas fa-lightbulb"></i> Budget Tips</h3>
            <div style="font-size:0.85rem;color:var(--text-secondary);line-height:1.7;">
                <p>💡 <strong>Total Budget</strong> sets one overall limit for the month.</p>
                <p>📊 <strong>Category Budget</strong> lets you control spending per area.</p>
                <p>🔄 Use <strong>Copy Last Month</strong> to quickly replicate budgets.</p>
                <p>💰 You can use both! Overall limit + category breakdowns.</p>
                <% if (effectiveBudget > 0 && totalActual > effectiveBudget) { %>
                <p style="color:var(--danger);"><strong>⚠️ You've exceeded your budget by <%= cs %><%= String.format("%,.2f", totalActual - effectiveBudget) %>!</strong></p>
                <% } else if (effectiveBudget > 0) { %>
                <p style="color:var(--success);">✅ Within budget. <%= cs %><%= String.format("%,.2f", effectiveBudget - totalActual) %> remaining.</p>
                <% } %>
            </div>
        </div>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>

<script>
// Tab switching
function switchBudgetTab(tab) {
    const totalBtn = document.getElementById('tabTotal');
    const catBtn = document.getElementById('tabCategory');
    const totalForm = document.getElementById('formTotal');
    const catForm = document.getElementById('formCategory');

    if (tab === 'total') {
        totalBtn.style.background = 'var(--primary)'; totalBtn.style.color = '#fff';
        catBtn.style.background = 'var(--bg-secondary)'; catBtn.style.color = 'var(--text-secondary)';
        totalForm.style.display = 'block'; catForm.style.display = 'none';
    } else {
        catBtn.style.background = 'var(--primary)'; catBtn.style.color = '#fff';
        totalBtn.style.background = 'var(--bg-secondary)'; totalBtn.style.color = 'var(--text-secondary)';
        catForm.style.display = 'block'; totalForm.style.display = 'none';
    }
}

// Chart
(function() {
    const labels = [<% for (int i = 0; i < budgets.size(); i++) { if (i > 0) out.print(","); out.print("'" + budgets.get(i).getCategory() + "'"); } %>];
    const budgetData = [<% for (int i = 0; i < budgets.size(); i++) { if (i > 0) out.print(","); out.print(budgets.get(i).getBudgetAmount()); } %>];
    const actualData = [<% for (int i = 0; i < budgets.size(); i++) { if (i > 0) out.print(","); double actual = actualMap.getOrDefault(budgets.get(i).getCategory(), 0.0); out.print(actual); } %>];

    if (labels.length > 0 && document.getElementById('budgetChart')) {
        new Chart(document.getElementById('budgetChart'), {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    { label: 'Budget', data: budgetData, backgroundColor: 'rgba(79,70,229,0.6)', borderColor: '#4f46e5', borderWidth: 1, borderRadius: 4 },
                    { label: 'Actual', data: actualData, backgroundColor: 'rgba(239,68,68,0.6)', borderColor: '#ef4444', borderWidth: 1, borderRadius: 4 }
                ]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: 'top', labels: { usePointStyle: true, font: { size: 11, family: 'Inter' } } } },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    }
})();
</script>
</body>
</html>
