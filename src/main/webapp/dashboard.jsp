<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.*, com.expensetracker.dao.*"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard — QuickExpense</title>
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
    String username = (String) s.getAttribute("username");
    String userEmail = (String) s.getAttribute("userEmail");
    String cs = (String) s.getAttribute("currencySymbol");
    if (cs == null) cs = "\u20B9";
    int userId = (int) s.getAttribute("userId");

    Object totalObj = s.getAttribute("totalAmount");
    double totalAmount = totalObj != null ? Double.parseDouble(totalObj.toString()) : 0;
    String topCategory = (String) s.getAttribute("topCategory");
    if (topCategory == null) topCategory = "-";
    Object countObj = s.getAttribute("expenseCount");
    int expenseCount = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
    Object recentObj = s.getAttribute("recentSpending");
    String recentSpending = recentObj != null ? recentObj.toString() : "N/A";

    List<Expense> expenses = (List<Expense>) s.getAttribute("expenseList");
    if (expenses == null) expenses = new ArrayList<>();

    // Category breakdown
    Map<String, Double> catMap = new LinkedHashMap<>();
    for (Expense e : expenses) {
        catMap.merge(e.getCategory(), e.getAmount(), Double::sum);
    }

    // Monthly breakdown (last 6 months)
    Map<String, Double> monthMap = new LinkedHashMap<>();
    java.time.LocalDate now = java.time.LocalDate.now();
    for (int i = 5; i >= 0; i--) {
        java.time.LocalDate m = now.minusMonths(i);
        String key = m.getMonth().toString().substring(0, 3) + " " + m.getYear();
        monthMap.put(key, 0.0);
    }
    for (Expense e : expenses) {
        if (e.getDate() != null) {
            java.time.LocalDate d = e.getDate();
            String key = d.getMonth().toString().substring(0, 3) + " " + d.getYear();
            if (monthMap.containsKey(key)) {
                monthMap.merge(key, e.getAmount(), Double::sum);
            }
        }
    }

    // Report info
    String reportPath = (String) s.getAttribute("reportPath");
    String reportFrom = (String) s.getAttribute("reportFromDate");
    String reportTo = (String) s.getAttribute("reportToDate");

    // Budget data for current month
    java.time.LocalDate nowDate = java.time.LocalDate.now();
    int curMonth = nowDate.getMonthValue();
    int curYear = nowDate.getYear();
    BudgetDAO budgetDAO = new BudgetDAO();
    double totalBudget = budgetDAO.getTotalBudget(userId, curMonth, curYear);
    Map<String, Double> budgetMap = budgetDAO.getBudgetMap(userId, curMonth, curYear);
    Budget overallBudgetEntry = budgetDAO.getOverallBudget(userId, curMonth, curYear);
    boolean hasOverallBudget = (overallBudgetEntry != null);

    // === NEW FEATURE DATA ===
    ExpenseDAO expDAO = new ExpenseDAO();
    IncomeDAO incomeDAO = new IncomeDAO();
    RecurringExpenseDAO reDAO = new RecurringExpenseDAO();
    SavingsGoalDAO goalDAO = new SavingsGoalDAO();

    // Income data
    double monthlyIncome = incomeDAO.getTotalIncomeByMonth(userId, curMonth, curYear);
    double totalIncome = incomeDAO.getTotalIncome(userId);

    // Recurring expense total
    double recurringTotal = reDAO.getMonthlyRecurringTotal(userId);

    // Savings goals
    List<SavingsGoal> goals = goalDAO.getAllByUser(userId);
    double totalSaved = 0;
    for (SavingsGoal g : goals) totalSaved += g.getSavedAmount();

    // Spending insights
    double avgDailySpending = expDAO.getAvgDailySpending(userId);
    double predictedSpending = expDAO.predictNextMonthSpending(userId);

    // Monthly spending for current month
    double currentMonthSpending = expDAO.getSpendingForMonth(userId, curMonth, curYear);
    double lastMonthSpending = expDAO.getSpendingForMonth(userId, curMonth > 1 ? curMonth - 1 : 12, curMonth > 1 ? curYear : curYear - 1);
    double lastYearSameMonth = expDAO.getSpendingForMonth(userId, curMonth, curYear - 1);
    double monthChange = lastMonthSpending > 0 ? ((currentMonthSpending - lastMonthSpending) / lastMonthSpending) * 100 : 0;

    // Daily spending heatmap
    Map<Integer, Double> dailySpending = expDAO.getDailySpending(userId, curMonth, curYear);
    double maxDaily = 0;
    for (double v : dailySpending.values()) if (v > maxDaily) maxDaily = v;

    // Monthly trend for line chart (12 months)
    Map<String, Double> monthlyTotals = expDAO.getMonthlyTotals(userId, 12);

    // Category totals for current month (drill-down)
    Map<String, Double> monthCatTotals = expDAO.getCategoryTotalsForMonth(userId, curMonth, curYear);

    // Budget alerts
    double budgetPctOverall = totalBudget > 0 ? (currentMonthSpending / totalBudget) * 100 : 0;
    boolean budgetWarning = budgetPctOverall > 80 && budgetPctOverall < 100;
    boolean budgetExceeded = budgetPctOverall >= 100;

    // Net balance
    double netBalance = totalIncome - totalAmount;

    // Process due recurring expenses automatically
    reDAO.processDueExpenses(userId);
%>

<!-- Onboarding Wizard (first-time user check) -->
<% if (expenseCount == 0 && monthlyIncome == 0) { %>
<div class="onboarding-banner" id="onboardingBanner">
    <div class="onboarding-content">
        <div class="onboarding-icon"><i class="fas fa-rocket"></i></div>
        <div>
            <h3>Welcome to QuickExpense, <%= username %>!</h3>
            <p>Get started by completing these steps to set up your financial dashboard:</p>
            <div class="onboarding-steps">
                <a href="addExpense.jsp" class="onboarding-step"><i class="fas fa-plus-circle"></i> Add your first expense</a>
                <a href="income.jsp" class="onboarding-step"><i class="fas fa-wallet"></i> Record your income</a>
                <a href="budget.jsp" class="onboarding-step"><i class="fas fa-piggy-bank"></i> Set a budget</a>
                <a href="goals.jsp" class="onboarding-step"><i class="fas fa-bullseye"></i> Create a savings goal</a>
            </div>
        </div>
        <button onclick="document.getElementById('onboardingBanner').style.display='none'" style="background:none;border:none;color:var(--text-muted);font-size:1.2rem;cursor:pointer;align-self:flex-start;width:auto;padding:4px;"><i class="fas fa-times"></i></button>
    </div>
</div>
<% } %>

<!-- Budget Alert Banner -->
<% if (budgetExceeded) { %>
<div class="alert alert-error" style="margin-bottom:16px;animation:fadeInUp 0.5s ease-out;">
    <i class="fas fa-exclamation-triangle"></i>
    <strong>Budget Exceeded!</strong> You've spent <%= cs %><%= String.format("%,.0f", currentMonthSpending) %> of your <%= cs %><%= String.format("%,.0f", totalBudget) %> budget (<%= String.format("%.0f", budgetPctOverall) %>%).
    <a href="budget.jsp" style="margin-left:8px;font-weight:600;">Review Budget &rarr;</a>
</div>
<% } else if (budgetWarning) { %>
<div class="alert alert-warning" style="margin-bottom:16px;animation:fadeInUp 0.5s ease-out;">
    <i class="fas fa-exclamation-circle"></i>
    <strong>Budget Alert!</strong> You've used <%= String.format("%.0f", budgetPctOverall) %>% of your monthly budget. Consider slowing down.
</div>
<% } %>

<!-- Page Header -->
<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-chart-line" style="color:var(--primary);margin-right:8px;"></i>Dashboard</h2>
        <p>Welcome back, <strong><%= username != null ? username : "User" %></strong>
        <% if (userEmail != null) { %><span style="color:var(--text-muted);font-size:0.8rem;margin-left:4px;">(<%= userEmail %>)</span><% } %></p>
    </div>
    <div class="page-header-actions">
        <a href="addExpense.jsp" class="btn btn-primary"><i class="fas fa-plus"></i> New Expense</a>
        <a href="income.jsp" class="btn btn-success" style="color:#fff;"><i class="fas fa-wallet"></i> Add Income</a>
        <a href="viewExpenses.jsp" class="btn btn-outline"><i class="fas fa-list"></i> All Expenses</a>
    </div>
</div>

<!-- Summary Cards (6 cards) -->
<div class="summary-cards" style="grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));">
    <div class="summary-card animate-card">
        <div class="summary-icon"><i class="fas fa-arrow-down"></i></div>
        <div class="summary-info">
            <h4>Total Spent</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.2f", totalAmount) %></div>
        </div>
    </div>
    <div class="summary-card animate-card">
        <div class="summary-icon" style="background:var(--success-light, rgba(16,185,129,0.1));color:var(--success);"><i class="fas fa-arrow-up"></i></div>
        <div class="summary-info">
            <h4>Total Income</h4>
            <div class="summary-value" style="color:var(--success);"><%= cs %><%= String.format("%,.2f", totalIncome) %></div>
        </div>
    </div>
    <div class="summary-card animate-card">
        <div class="summary-icon" style="background:<%= netBalance >= 0 ? "rgba(16,185,129,0.1)" : "rgba(239,68,68,0.1)" %>;color:<%= netBalance >= 0 ? "var(--success)" : "var(--danger)" %>;"><i class="fas fa-balance-scale"></i></div>
        <div class="summary-info">
            <h4>Net Balance</h4>
            <div class="summary-value" style="color:<%= netBalance >= 0 ? "var(--success)" : "var(--danger)" %>;"><%= cs %><%= String.format("%,.2f", netBalance) %></div>
        </div>
    </div>
    <div class="summary-card animate-card">
        <div class="summary-icon" style="background:rgba(245,158,11,0.1);color:var(--warning);"><i class="fas fa-trophy"></i></div>
        <div class="summary-info">
            <h4>Top Category</h4>
            <div class="summary-value"><%= topCategory %></div>
        </div>
    </div>
    <div class="summary-card animate-card">
        <div class="summary-icon" style="background:rgba(6,182,212,0.1);color:var(--accent, #06b6d4);"><i class="fas fa-receipt"></i></div>
        <div class="summary-info">
            <h4>Transactions</h4>
            <div class="summary-value"><%= expenseCount %></div>
        </div>
    </div>
    <div class="summary-card animate-card">
        <div class="summary-icon" style="background:rgba(79,70,229,0.1);color:var(--primary);"><i class="fas fa-chart-area"></i></div>
        <div class="summary-info">
            <h4>Avg Daily</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.0f", avgDailySpending) %></div>
        </div>
    </div>
</div>

<!-- Spending Insights Bar -->
<div class="insights-bar" style="display:flex;gap:16px;flex-wrap:wrap;background:var(--bg-secondary, #f8fafc);border:1px solid var(--border-color, #e2e8f0);border-radius:12px;padding:14px 20px;margin-bottom:20px;">
    <div class="insight-item" style="display:flex;align-items:center;gap:8px;font-size:0.85rem;">
        <i class="fas fa-arrow-trend-up" style="color:<%= monthChange > 0 ? "var(--danger)" : "var(--success)" %>;"></i>
        <span><strong><%= monthChange > 0 ? "+" : "" %><%= String.format("%.1f", monthChange) %>%</strong> vs last month</span>
    </div>
    <div class="insight-item" style="display:flex;align-items:center;gap:8px;font-size:0.85rem;">
        <i class="fas fa-sync-alt" style="color:var(--warning);"></i>
        <span>Recurring: <strong><%= cs %><%= String.format("%,.0f", recurringTotal) %></strong>/mo</span>
    </div>
    <div class="insight-item" style="display:flex;align-items:center;gap:8px;font-size:0.85rem;">
        <i class="fas fa-wand-magic-sparkles" style="color:var(--primary);"></i>
        <span>Predicted next month: <strong><%= cs %><%= String.format("%,.0f", predictedSpending) %></strong></span>
    </div>
    <% if (goals.size() > 0) { %>
    <div class="insight-item" style="display:flex;align-items:center;gap:8px;font-size:0.85rem;">
        <i class="fas fa-piggy-bank" style="color:var(--success);"></i>
        <span>Saved: <strong><%= cs %><%= String.format("%,.0f", totalSaved) %></strong> across <%= goals.size() %> goal<%= goals.size() != 1 ? "s" : "" %></span>
    </div>
    <% } %>
</div>

<!-- Dashboard Layout: Charts + Sidebar -->
<div class="dashboard-layout">
    <div>
        <!-- Budget Gauge + Spending Trends Row -->
        <div class="charts-container">
            <div class="chart-box">
                <h4><i class="fas fa-gauge-high" style="color:var(--primary);margin-right:6px;"></i>Budget Usage</h4>
                <% if (totalBudget > 0) { %>
                <canvas id="budgetGauge" style="max-height:220px;"></canvas>
                <div style="text-align:center;margin-top:8px;">
                    <span style="font-size:1.5rem;font-weight:800;color:<%= budgetExceeded ? "var(--danger)" : budgetWarning ? "var(--warning)" : "var(--success)" %>;"><%= String.format("%.0f", budgetPctOverall) %>%</span>
                    <p style="font-size:0.8rem;color:var(--text-muted);"><%= cs %><%= String.format("%,.0f", currentMonthSpending) %> of <%= cs %><%= String.format("%,.0f", totalBudget) %></p>
                </div>
                <% } else { %>
                <div class="empty-state" style="padding:40px 20px;">
                    <i class="fas fa-gauge-high"></i>
                    <p>Set a budget to see gauge</p>
                    <a href="budget.jsp" class="btn btn-primary btn-sm"><i class="fas fa-plus"></i> Set Budget</a>
                </div>
                <% } %>
            </div>
            <div class="chart-box">
                <h4><i class="fas fa-chart-line" style="color:var(--success);margin-right:6px;"></i>Spending Trend (12 Mo)</h4>
                <canvas id="trendChart"></canvas>
            </div>
        </div>

        <!-- Category Doughnut + Monthly Bar Row -->
        <div class="charts-container" style="margin-top:20px;">
            <div class="chart-box">
                <h4><i class="fas fa-chart-pie" style="color:var(--primary);margin-right:6px;"></i>By Category</h4>
                <canvas id="catChart"></canvas>
            </div>
            <div class="chart-box">
                <h4><i class="fas fa-chart-bar" style="color:var(--warning);margin-right:6px;"></i>Monthly Comparison</h4>
                <canvas id="monthChart"></canvas>
            </div>
        </div>

        <!-- Calendar Heatmap -->
        <div class="chart-box" style="margin-top:20px;">
            <h4><i class="fas fa-calendar-alt" style="color:var(--accent, #06b6d4);margin-right:6px;"></i>Daily Spending Heatmap &mdash; <%= java.time.Month.of(curMonth) %> <%= curYear %></h4>
            <div class="heatmap-grid" style="display:grid;grid-template-columns:repeat(7,1fr);gap:4px;margin-top:12px;">
                <%
                int daysInMonth = java.time.YearMonth.of(curYear, curMonth).lengthOfMonth();
                int firstDow = java.time.LocalDate.of(curYear, curMonth, 1).getDayOfWeek().getValue();
                for (int b = 1; b < firstDow; b++) { %>
                    <div style="aspect-ratio:1;"></div>
                <% }
                for (int day = 1; day <= daysInMonth; day++) {
                    double spent = dailySpending.getOrDefault(day, 0.0);
                    double intensity = maxDaily > 0 ? spent / maxDaily : 0;
                    String cellColor = spent == 0 ? "var(--gray-100, #f1f5f9)" :
                        intensity < 0.25 ? "#c7d2fe" :
                        intensity < 0.5 ? "#818cf8" :
                        intensity < 0.75 ? "#6366f1" : "#4f46e5";
                    boolean isToday = day == nowDate.getDayOfMonth();
                %>
                <div style="aspect-ratio:1;background:<%= cellColor %>;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:0.72rem;font-weight:600;<%= spent > 0 ? "color:#fff;" : "color:var(--text-muted);" %><%= isToday ? "outline:2px solid var(--primary);outline-offset:1px;" : "" %>cursor:default;"
                     title="Day <%= day %>: <%= cs %><%= String.format("%,.0f", spent) %>">
                    <%= day %>
                </div>
                <% } %>
            </div>
            <div style="display:flex;align-items:center;gap:6px;margin-top:10px;justify-content:center;">
                <span style="color:var(--text-muted);font-size:0.75rem;">Less</span>
                <div style="width:16px;height:16px;background:var(--gray-100, #f1f5f9);border-radius:3px;"></div>
                <div style="width:16px;height:16px;background:#c7d2fe;border-radius:3px;"></div>
                <div style="width:16px;height:16px;background:#818cf8;border-radius:3px;"></div>
                <div style="width:16px;height:16px;background:#6366f1;border-radius:3px;"></div>
                <div style="width:16px;height:16px;background:#4f46e5;border-radius:3px;"></div>
                <span style="color:var(--text-muted);font-size:0.75rem;">More</span>
            </div>
        </div>

        <!-- YoY Comparison -->
        <div class="chart-box" style="margin-top:20px;">
            <h4><i class="fas fa-code-compare" style="color:var(--primary);margin-right:6px;"></i>Year-over-Year Comparison</h4>
            <div style="display:grid;grid-template-columns:repeat(auto-fit, minmax(180px, 1fr));gap:12px;margin-top:12px;">
                <div style="background:var(--bg-secondary, #f8fafc);padding:14px;border-radius:10px;text-align:center;">
                    <div style="font-size:0.78rem;color:var(--text-muted);margin-bottom:4px;">This Month (<%= java.time.Month.of(curMonth).toString().substring(0,3) %> <%= curYear %>)</div>
                    <div style="font-size:1.2rem;font-weight:700;"><%= cs %><%= String.format("%,.2f", currentMonthSpending) %></div>
                </div>
                <div style="background:var(--bg-secondary, #f8fafc);padding:14px;border-radius:10px;text-align:center;">
                    <div style="font-size:0.78rem;color:var(--text-muted);margin-bottom:4px;">Last Month</div>
                    <div style="font-size:1.2rem;font-weight:700;"><%= cs %><%= String.format("%,.2f", lastMonthSpending) %></div>
                </div>
                <div style="background:var(--bg-secondary, #f8fafc);padding:14px;border-radius:10px;text-align:center;">
                    <div style="font-size:0.78rem;color:var(--text-muted);margin-bottom:4px;">Same Month Last Year</div>
                    <div style="font-size:1.2rem;font-weight:700;"><%= cs %><%= String.format("%,.2f", lastYearSameMonth) %></div>
                </div>
                <div style="background:var(--bg-secondary, #f8fafc);padding:14px;border-radius:10px;text-align:center;">
                    <div style="font-size:0.78rem;color:var(--text-muted);margin-bottom:4px;">YoY Change</div>
                    <% double yoyChange = lastYearSameMonth > 0 ? ((currentMonthSpending - lastYearSameMonth) / lastYearSameMonth) * 100 : 0; %>
                    <div style="font-size:1.2rem;font-weight:700;color:<%= yoyChange > 0 ? "var(--danger)" : "var(--success)" %>;"><%= yoyChange > 0 ? "+" : "" %><%= String.format("%.1f", yoyChange) %>%</div>
                </div>
            </div>
        </div>

        <!-- Category Drill-Down -->
        <div class="chart-box" style="margin-top:20px;">
            <h4><i class="fas fa-search-dollar" style="color:var(--primary);margin-right:6px;"></i>Category Drill-Down &mdash; This Month</h4>
            <% if (monthCatTotals.isEmpty()) { %>
            <div class="empty-state" style="padding:20px;"><p>No expenses this month</p></div>
            <% } else {
                double monthTotal = 0;
                for (double v : monthCatTotals.values()) monthTotal += v;
                for (Map.Entry<String, Double> entry : monthCatTotals.entrySet()) {
                    double pct = monthTotal > 0 ? (entry.getValue() / monthTotal) * 100 : 0;
                    String badge = entry.getKey().toLowerCase().replace(" ", "");
            %>
            <div style="display:flex;align-items:center;gap:10px;margin-bottom:10px;">
                <span class="badge badge-<%= badge %>" style="min-width:90px;text-align:center;"><%= entry.getKey() %></span>
                <div style="flex:1;background:var(--gray-100, #f1f5f9);border-radius:999px;height:8px;overflow:hidden;">
                    <div style="width:<%= pct %>%;height:100%;background:var(--primary);border-radius:999px;transition:width 0.5s;"></div>
                </div>
                <span style="font-size:0.82rem;color:var(--text-muted);min-width:40px;text-align:right;"><%= String.format("%.0f", pct) %>%</span>
                <span style="font-weight:700;white-space:nowrap;min-width:80px;text-align:right;"><%= cs %><%= String.format("%,.2f", entry.getValue()) %></span>
            </div>
            <% }} %>
        </div>

        <!-- Recent Expenses Table -->
        <div class="table-wrapper" style="margin-top:24px;">
            <div class="table-header">
                <h3><i class="fas fa-history" style="color:var(--primary);margin-right:6px;"></i>Recent Expenses</h3>
                <a href="viewExpenses.jsp" class="btn btn-outline btn-sm"><i class="fas fa-list"></i> View All</a>
            </div>
            <div class="table-responsive">
            <table>
                <thead>
                    <tr><th>Title</th><th>Category</th><th>Amount</th><th>Date</th></tr>
                </thead>
                <tbody>
                <% if (expenses.isEmpty()) { %>
                    <tr><td colspan="4" class="text-center text-muted" style="padding:32px;">No expenses yet — start tracking!</td></tr>
                <% } else {
                    int limit = Math.min(expenses.size(), 5);
                    List<Expense> sorted = new ArrayList<>(expenses);
                    sorted.sort((a, b) -> b.getDate().compareTo(a.getDate()));
                    for (int i = 0; i < limit; i++) {
                        Expense e = sorted.get(i);
                        String badge = e.getCategory() != null ? e.getCategory().toLowerCase().replace(" ", "") : "others";
                %>
                    <tr>
                        <td><strong><%= e.getTitle() %></strong></td>
                        <td><span class="badge badge-<%= badge %>"><%= e.getCategory() %></span></td>
                        <td class="amount-cell"><%= cs %><%= String.format("%,.2f", e.getAmount()) %></td>
                        <td><%= e.getDate() %></td>
                    </tr>
                <% }} %>
                </tbody>
            </table>
            </div>
        </div>
    </div>

    <!-- Sidebar -->
    <div>
        <!-- Filter / Report Section -->
        <div class="sidebar-section">
            <h3><i class="fas fa-filter"></i> Filter & Reports</h3>
            <form action="DashboardServlet" method="get">
                <div class="form-group">
                    <label>From Date</label>
                    <input type="date" name="fromDate" id="filterFrom">
                </div>
                <div class="form-group">
                    <label>To Date</label>
                    <input type="date" name="toDate" id="filterTo">
                </div>
                <div class="form-group">
                    <label>Category</label>
                    <select name="category">
                        <option value="">All Categories</option>
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
                <button type="submit" class="btn btn-primary btn-block"><i class="fas fa-filter"></i> Apply Filter</button>
            </form>
        </div>

        <!-- Generate Report -->
        <div class="sidebar-section">
            <h3><i class="fas fa-file-alt"></i> Generate Report</h3>
            <form action="ReportServlet" method="post">
                <div class="form-group">
                    <label>Report From</label>
                    <input type="date" name="fromDate" required>
                </div>
                <div class="form-group">
                    <label>Report To</label>
                    <input type="date" name="toDate" required>
                </div>
                <div class="form-group">
                    <label>Format</label>
                    <select name="format">
                        <option value="excel">📊 Excel (.xlsx)</option>
                        <option value="pdf">📄 PDF (.pdf)</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-success btn-block"><i class="fas fa-file-download"></i> Generate & Email Report</button>
            </form>
            <% if (reportPath != null) { %>
            <div class="download-section mt-2">
                <p style="font-size:0.82rem;color:var(--text-secondary);margin-bottom:6px;">
                    <i class="fas fa-check-circle" style="color:var(--success);"></i>
                    Report ready<% if (reportFrom != null && reportTo != null) { %> (<%= reportFrom %> to <%= reportTo %>)<% } %>
                </p>
                <a href="DownloadReportServlet"><i class="fas fa-download"></i> Download Report</a>
            </div>
            <% } %>
        </div>

        <!-- Budget Overview -->
        <div class="sidebar-section">
            <h3><i class="fas fa-piggy-bank"></i> Budget Overview</h3>
            <% if (totalBudget > 0) {
                double budgetUsed = currentMonthSpending;
                double budgetPct = (budgetUsed / totalBudget) * 100;
                String budgetColor = budgetPct > 100 ? "var(--danger)" : budgetPct > 80 ? "var(--warning)" : "var(--success)";
            %>
            <div style="margin-bottom:12px;">
                <div style="display:flex;justify-content:space-between;font-size:0.82rem;margin-bottom:4px;">
                    <span>Spent: <%= cs %><%= String.format("%,.0f", budgetUsed) %></span>
                    <span><%= hasOverallBudget ? "Limit" : "Budget" %>: <%= cs %><%= String.format("%,.0f", totalBudget) %></span>
                </div>
                <div style="background:var(--gray-100);border-radius:999px;height:10px;overflow:hidden;">
                    <div style="width:<%= Math.min(budgetPct, 100) %>%;height:100%;background:<%= budgetColor %>;border-radius:999px;transition:width 0.5s;"></div>
                </div>
                <div style="font-size:0.72rem;color:var(--text-muted);margin-top:4px;text-align:center;">
                    <%= String.format("%.1f", budgetPct) %>% used
                    <span style="margin-left:4px;">(<%= hasOverallBudget ? "overall limit" : "category sum" %>)</span>
                </div>
            </div>
            <% if (!budgetMap.isEmpty()) {
                for (Map.Entry<String, Double> be : budgetMap.entrySet()) {
                    double actual = catMap.getOrDefault(be.getKey(), 0.0);
                    double pct = be.getValue() > 0 ? (actual / be.getValue()) * 100 : 0;
                    String bColor = pct > 100 ? "var(--danger)" : pct > 80 ? "var(--warning)" : "var(--success)";
            %>
            <div style="margin-bottom:8px;">
                <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:var(--text-secondary);">
                    <span><%= be.getKey() %></span>
                    <span><%= cs %><%= String.format("%,.0f", actual) %> / <%= cs %><%= String.format("%,.0f", be.getValue()) %></span>
                </div>
                <div style="background:var(--gray-100);border-radius:999px;height:5px;overflow:hidden;">
                    <div style="width:<%= Math.min(pct, 100) %>%;height:100%;background:<%= bColor %>;border-radius:999px;"></div>
                </div>
            </div>
            <% }} %>
            <a href="budget.jsp" class="btn btn-outline btn-block btn-sm" style="margin-top:8px;"><i class="fas fa-cog"></i> Manage Budgets</a>
            <% } else { %>
            <div style="text-align:center;padding:16px 0;color:var(--text-muted);font-size:0.85rem;">
                <i class="fas fa-piggy-bank" style="font-size:1.5rem;display:block;margin-bottom:8px;opacity:0.4;"></i>
                No budgets set for this month
                <a href="budget.jsp" class="btn btn-primary btn-block btn-sm" style="margin-top:10px;"><i class="fas fa-plus"></i> Set Budgets</a>
            </div>
            <% } %>
        </div>

        <!-- Savings Goals Mini -->
        <div class="sidebar-section">
            <h3><i class="fas fa-bullseye"></i> Savings Goals</h3>
            <% if (goals.isEmpty()) { %>
            <div style="text-align:center;padding:12px 0;color:var(--text-muted);font-size:0.85rem;">
                <i class="fas fa-bullseye" style="font-size:1.5rem;display:block;margin-bottom:8px;opacity:0.4;"></i>
                No goals set yet
                <a href="goals.jsp" class="btn btn-primary btn-block btn-sm" style="margin-top:10px;"><i class="fas fa-plus"></i> Create Goal</a>
            </div>
            <% } else {
                int goalLimit = Math.min(goals.size(), 3);
                for (int gi = 0; gi < goalLimit; gi++) {
                    SavingsGoal g = goals.get(gi);
                    double gPct = g.getProgressPercent();
            %>
            <div style="margin-bottom:10px;">
                <div style="display:flex;justify-content:space-between;font-size:0.82rem;margin-bottom:4px;">
                    <span><i class="fas <%= g.getIcon() %>" style="color:<%= g.getColor() %>;margin-right:4px;"></i><%= g.getName() %></span>
                    <span><%= String.format("%.0f", gPct) %>%</span>
                </div>
                <div style="background:var(--gray-100);border-radius:999px;height:6px;overflow:hidden;">
                    <div style="width:<%= Math.min(gPct, 100) %>%;height:100%;background:<%= g.getColor() %>;border-radius:999px;"></div>
                </div>
                <div style="font-size:0.72rem;color:var(--text-muted);margin-top:2px;">
                    <%= cs %><%= String.format("%,.0f", g.getSavedAmount()) %> / <%= cs %><%= String.format("%,.0f", g.getTargetAmount()) %>
                </div>
            </div>
            <% }
            if (goals.size() > 3) { %>
            <p style="font-size:0.8rem;color:var(--text-muted);text-align:center;">+<%= goals.size() - 3 %> more goals</p>
            <% } %>
            <a href="goals.jsp" class="btn btn-outline btn-block btn-sm" style="margin-top:8px;"><i class="fas fa-bullseye"></i> All Goals</a>
            <% } %>
        </div>

        <!-- Quick Actions -->
        <div class="sidebar-section">
            <h3><i class="fas fa-bolt"></i> Quick Actions</h3>
            <div class="quick-actions">
                <a href="addExpense.jsp" class="quick-action-btn"><i class="fas fa-plus-circle"></i> Add Expense</a>
                <a href="income.jsp" class="quick-action-btn"><i class="fas fa-wallet"></i> Income</a>
                <a href="recurring.jsp" class="quick-action-btn"><i class="fas fa-sync-alt"></i> Recurring</a>
                <a href="goals.jsp" class="quick-action-btn"><i class="fas fa-bullseye"></i> Goals</a>
                <a href="budget.jsp" class="quick-action-btn"><i class="fas fa-piggy-bank"></i> Budgets</a>
                <a href="DashboardServlet" class="quick-action-btn"><i class="fas fa-sync"></i> Refresh</a>
            </div>
        </div>
    </div>
</div>

<%@ include file="includes/footer.jspf" %>

<!-- Charts Script -->
<script>
(function() {
    const CS = '<%= cs %>';
    const catLabels = [<% int ci = 0; for (Map.Entry<String,Double> en : catMap.entrySet()) { if (ci++ > 0) out.print(","); out.print("'" + en.getKey() + "'"); } %>];
    const catData = [<% int cv = 0; for (Map.Entry<String,Double> en : catMap.entrySet()) { if (cv++ > 0) out.print(","); out.print(en.getValue()); } %>];
    const catColors = ['#4f46e5','#10b981','#f59e0b','#ef4444','#06b6d4','#8b5cf6','#ec4899','#6366f1'];

    // Category Doughnut
    if (catLabels.length > 0) {
        new Chart(document.getElementById('catChart'), {
            type: 'doughnut',
            data: { labels: catLabels, datasets: [{ data: catData, backgroundColor: catColors.slice(0, catLabels.length), borderWidth: 2, borderColor: '#fff' }] },
            options: { responsive: true, plugins: { legend: { position: 'bottom', labels: { padding: 12, usePointStyle: true, font: { size: 11, family: 'Inter' } } } }, cutout: '65%' }
        });
    } else {
        document.getElementById('catChart').parentElement.innerHTML += '<div class="empty-state" style="padding:20px"><i class="fas fa-chart-pie"></i><p>No data to display</p></div>';
    }

    // Monthly Bar
    const monthLabels = [<% int mi = 0; for (Map.Entry<String,Double> en : monthMap.entrySet()) { if (mi++ > 0) out.print(","); out.print("'" + en.getKey() + "'"); } %>];
    const monthData = [<% int mv = 0; for (Map.Entry<String,Double> en : monthMap.entrySet()) { if (mv++ > 0) out.print(","); out.print(en.getValue()); } %>];

    new Chart(document.getElementById('monthChart'), {
        type: 'bar',
        data: {
            labels: monthLabels,
            datasets: [{
                label: 'Expenses (' + CS + ')',
                data: monthData,
                backgroundColor: 'rgba(79, 70, 229, 0.7)',
                borderColor: '#4f46e5',
                borderWidth: 1,
                borderRadius: 6,
                barPercentage: 0.6
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' }, ticks: { font: { size: 11, family: 'Inter' } } },
                x: { grid: { display: false }, ticks: { font: { size: 11, family: 'Inter' } } }
            }
        }
    });

    // Spending Trend Line Chart (12 months)
    const trendLabels = [<% int ti = 0; for (Map.Entry<String,Double> en : monthlyTotals.entrySet()) { if (ti++ > 0) out.print(","); out.print("'" + en.getKey() + "'"); } %>];
    const trendData = [<% int tv = 0; for (Map.Entry<String,Double> en : monthlyTotals.entrySet()) { if (tv++ > 0) out.print(","); out.print(en.getValue()); } %>];

    new Chart(document.getElementById('trendChart'), {
        type: 'line',
        data: {
            labels: trendLabels,
            datasets: [{
                label: 'Monthly Spending',
                data: trendData,
                borderColor: '#10b981',
                backgroundColor: 'rgba(16,185,129,0.1)',
                borderWidth: 2.5,
                pointBackgroundColor: '#10b981',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' }, ticks: { font: { size: 10, family: 'Inter' } } },
                x: { grid: { display: false }, ticks: { font: { size: 10, family: 'Inter' }, maxRotation: 45 } }
            }
        }
    });

    // Budget Gauge Chart
    <% if (totalBudget > 0) { %>
    const gaugeCanvas = document.getElementById('budgetGauge');
    if (gaugeCanvas) {
        const gaugeVal = Math.min(<%= budgetPctOverall %>, 100);
        new Chart(gaugeCanvas, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [gaugeVal, 100 - gaugeVal],
                    backgroundColor: [
                        gaugeVal >= 100 ? '#ef4444' : gaugeVal > 80 ? '#f59e0b' : '#10b981',
                        'rgba(0,0,0,0.05)'
                    ],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                circumference: 180,
                rotation: 270,
                cutout: '75%',
                plugins: { legend: { display: false }, tooltip: { enabled: false } }
            }
        });
    }
    <% } %>

    // Animated summary card entrance
    document.querySelectorAll('.animate-card').forEach(function(card, i) {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.5s ease ' + (i * 0.08) + 's, transform 0.5s ease ' + (i * 0.08) + 's';
        setTimeout(function() { card.style.opacity = '1'; card.style.transform = 'translateY(0)'; }, 50);
    });
})();
</script>
</body>
</html>
