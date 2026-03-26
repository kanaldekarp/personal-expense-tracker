<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, com.expensetracker.model.SavingsGoal, com.expensetracker.dao.SavingsGoalDAO"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Savings Goals — QuickExpense</title>
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
    SavingsGoalDAO goalDAO = new SavingsGoalDAO();
    List<SavingsGoal> goals = goalDAO.getAllByUser(userId);
    double totalSaved = 0, totalTarget = 0;
    for (SavingsGoal g : goals) { totalSaved += g.getSavedAmount(); totalTarget += g.getTargetAmount(); }
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-bullseye" style="color:var(--primary);margin-right:8px;"></i>Savings Goals</h2>
        <p>Set targets and track your savings progress</p>
    </div>
</div>

<!-- Summary Cards -->
<div class="summary-cards" style="grid-template-columns: repeat(3, 1fr);">
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--success-light);color:var(--success);"><i class="fas fa-piggy-bank"></i></div>
        <div class="summary-info">
            <h4>Total Saved</h4>
            <div class="summary-value" style="color:var(--success);"><%= cs %><%= String.format("%,.2f", totalSaved) %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--primary-100);color:var(--primary);"><i class="fas fa-flag-checkered"></i></div>
        <div class="summary-info">
            <h4>Total Target</h4>
            <div class="summary-value"><%= cs %><%= String.format("%,.2f", totalTarget) %></div>
        </div>
    </div>
    <div class="summary-card">
        <div class="summary-icon" style="background:var(--warning-light);color:var(--warning);"><i class="fas fa-bullseye"></i></div>
        <div class="summary-info">
            <h4>Active Goals</h4>
            <div class="summary-value"><%= goals.size() %></div>
        </div>
    </div>
</div>

<!-- Add Goal Form -->
<div class="form-container" style="max-width:100%;margin-bottom:24px;">
    <div class="form-card">
        <h3 style="margin-bottom:16px;display:flex;align-items:center;gap:8px;">
            <i class="fas fa-plus-circle" style="color:var(--primary);"></i> Create New Goal
        </h3>
        <form action="GoalsServlet" method="post">
            <div class="form-row" style="grid-template-columns:repeat(4,1fr);">
                <div class="form-group">
                    <label><i class="fas fa-tag"></i> Goal Name <span class="required">*</span></label>
                    <input type="text" name="name" placeholder="e.g. Emergency Fund" required maxlength="100">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-coins"></i> Target Amount (<%= cs %>) <span class="required">*</span></label>
                    <input type="number" name="targetAmount" step="0.01" min="1" placeholder="0.00" required>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-calendar"></i> Deadline</label>
                    <input type="date" name="deadline">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-palette"></i> Color</label>
                    <input type="color" name="color" value="#4f46e5" style="height:38px;padding:4px;">
                </div>
            </div>
            <button type="submit" class="btn btn-primary" style="width:auto;"><i class="fas fa-check"></i> Create Goal</button>
        </form>
    </div>
</div>

<!-- Goals Grid -->
<div class="goals-grid">
<% if (goals.isEmpty()) { %>
    <div class="form-card" style="text-align:center;padding:48px;">
        <div class="empty-state">
            <i class="fas fa-bullseye"></i>
            <h3>No savings goals yet</h3>
            <p>Create your first goal and start saving!</p>
        </div>
    </div>
<% } else {
    for (SavingsGoal g : goals) {
        double pct = g.getProgressPercent();
        String statusColor = pct >= 100 ? "var(--success)" : pct > 50 ? "var(--primary)" : pct > 25 ? "var(--warning)" : "var(--danger)";
        boolean completed = pct >= 100;
%>
    <div class="goal-card" style="border-left:4px solid <%= g.getColor() %>;">
        <div class="goal-card-header">
            <div>
                <h4 style="display:flex;align-items:center;gap:8px;">
                    <i class="fas <%= g.getIcon() %>" style="color:<%= g.getColor() %>;"></i>
                    <%= g.getName() %>
                    <% if (completed) { %><span class="badge" style="background:var(--success-light);color:var(--success);">✓ Complete</span><% } %>
                </h4>
                <% if (g.getDeadline() != null) { %>
                <p style="font-size:0.8rem;color:var(--text-muted);margin-top:4px;">
                    <i class="fas fa-calendar"></i> Deadline: <%= g.getDeadline() %>
                    <% if (!completed && g.getDeadline().isBefore(java.time.LocalDate.now())) { %>
                    <span style="color:var(--danger);font-weight:600;"> — Overdue!</span>
                    <% } %>
                </p>
                <% } %>
            </div>
            <div class="table-actions">
                <a href="GoalsServlet?action=delete&id=<%= g.getId() %>" class="action-delete" onclick="return confirm('Delete this goal?');"><i class="fas fa-trash"></i></a>
            </div>
        </div>
        <div style="margin:16px 0;">
            <div style="display:flex;justify-content:space-between;font-size:0.85rem;margin-bottom:6px;">
                <span style="color:var(--text-secondary);">Saved: <strong style="color:var(--success);"><%= cs %><%= String.format("%,.2f", g.getSavedAmount()) %></strong></span>
                <span style="color:var(--text-secondary);">Target: <strong><%= cs %><%= String.format("%,.2f", g.getTargetAmount()) %></strong></span>
            </div>
            <div style="background:var(--gray-100);border-radius:999px;height:12px;overflow:hidden;">
                <div style="width:<%= Math.min(pct, 100) %>%;height:100%;background:<%= statusColor %>;border-radius:999px;transition:width 0.8s ease;"></div>
            </div>
            <div style="text-align:center;font-size:0.8rem;color:var(--text-muted);margin-top:4px;">
                <%= String.format("%.1f", pct) %>% — <%= cs %><%= String.format("%,.2f", g.getRemainingAmount()) %> remaining
            </div>
        </div>
        <% if (!completed) { %>
        <form action="GoalsServlet" method="get" style="display:flex;gap:8px;align-items:center;">
            <input type="hidden" name="action" value="addFunds">
            <input type="hidden" name="id" value="<%= g.getId() %>">
            <input type="number" name="amount" step="0.01" min="0.01" placeholder="Add funds..." required
                   style="flex:1;padding:8px 12px;border:1.5px solid var(--border-color);border-radius:var(--border-radius-sm);font-size:0.85rem;background:var(--bg-card);color:var(--text-primary);">
            <button type="submit" class="btn btn-success btn-sm" style="width:auto;white-space:nowrap;"><i class="fas fa-plus"></i> Add</button>
        </form>
        <% } %>
    </div>
<% }} %>
</div>

<style>
.goals-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 16px; }
.goal-card {
    background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--border-radius);
    padding: 20px; box-shadow: var(--shadow-sm); transition: all var(--transition-base);
}
.goal-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.goal-card-header { display: flex; justify-content: space-between; align-items: flex-start; }
@media (max-width: 480px) { .goals-grid { grid-template-columns: 1fr; } }
</style>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
