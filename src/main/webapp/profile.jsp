<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.expensetracker.dao.ExpenseDAO, com.expensetracker.dao.IncomeDAO"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Profile — QuickExpense</title>
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
    String username = (String) s.getAttribute("username");
    String userEmail = (String) s.getAttribute("userEmail");
    String currency = (String) s.getAttribute("currency");
    String cs = (String) s.getAttribute("currencySymbol");
    if (cs == null) cs = "\u20B9";
    
    ExpenseDAO dao = new ExpenseDAO();
    IncomeDAO incomeDAO = new IncomeDAO();
    int totalExpenses = dao.countExpenses(userId);
    double totalSpent = 0;
    for (com.expensetracker.model.Expense e : dao.getAllExpensesByUser(userId)) totalSpent += e.getAmount();
    double totalIncome = incomeDAO.getTotalIncome(userId);
    String memberSince = dao.getUserCreatedAt(userId);
    String initial = username != null ? username.substring(0, 1).toUpperCase() : "U";
%>

<div class="page-header">
    <div class="page-header-content">
        <h2><i class="fas fa-user-circle" style="color:var(--primary);margin-right:8px;"></i>Profile</h2>
        <p>Manage your account settings and preferences</p>
    </div>
</div>

<div class="profile-layout">
    <!-- Profile Card -->
    <div class="profile-card">
        <div class="profile-avatar">
            <div class="avatar-large"><%= initial %></div>
            <h3><%= username %></h3>
            <p style="color:var(--text-muted);font-size:0.9rem;"><%= userEmail %></p>
        </div>
        <div class="profile-stats">
            <div class="profile-stat">
                <span class="stat-value"><%= totalExpenses %></span>
                <span class="stat-label">Transactions</span>
            </div>
            <div class="profile-stat">
                <span class="stat-value"><%= cs %><%= String.format("%,.0f", totalSpent) %></span>
                <span class="stat-label">Total Spent</span>
            </div>
            <div class="profile-stat">
                <span class="stat-value"><%= cs %><%= String.format("%,.0f", totalIncome) %></span>
                <span class="stat-label">Total Income</span>
            </div>
        </div>
        <div style="padding:16px 20px;font-size:0.85rem;color:var(--text-muted);border-top:1px solid var(--border-color);">
            <i class="fas fa-calendar-alt" style="margin-right:4px;"></i> Member since: <%= memberSince %>
            <br><i class="fas fa-coins" style="margin-right:4px;"></i> Currency: <%= currency != null ? currency : "INR" %>
        </div>
    </div>

    <!-- Settings -->
    <div>
        <!-- Update Profile -->
        <div class="form-card" style="margin-bottom:20px;">
            <h3 style="margin-bottom:16px;display:flex;align-items:center;gap:8px;">
                <i class="fas fa-user-edit" style="color:var(--primary);"></i> Update Profile
            </h3>
            <form action="ProfileServlet" method="post">
                <input type="hidden" name="action" value="updateProfile">
                <div class="form-row">
                    <div class="form-group">
                        <label><i class="fas fa-user"></i> Username</label>
                        <input type="text" name="username" value="<%= username %>" required maxlength="50">
                    </div>
                    <div class="form-group">
                        <label><i class="fas fa-envelope"></i> Email</label>
                        <input type="email" name="email" value="<%= userEmail %>" required maxlength="100">
                    </div>
                </div>
                <button type="submit" class="btn btn-primary" style="width:auto;"><i class="fas fa-save"></i> Save Changes</button>
            </form>
        </div>

        <!-- Change Password -->
        <div class="form-card" style="margin-bottom:20px;">
            <h3 style="margin-bottom:16px;display:flex;align-items:center;gap:8px;">
                <i class="fas fa-lock" style="color:var(--warning);"></i> Change Password
            </h3>
            <form action="ProfileServlet" method="post">
                <input type="hidden" name="action" value="changePassword">
                <div class="form-group">
                    <label><i class="fas fa-key"></i> Current Password</label>
                    <input type="password" name="currentPassword" required>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label><i class="fas fa-lock"></i> New Password</label>
                        <input type="password" name="newPassword" required minlength="6">
                    </div>
                    <div class="form-group">
                        <label><i class="fas fa-lock"></i> Confirm Password</label>
                        <input type="password" name="confirmPassword" required minlength="6">
                    </div>
                </div>
                <button type="submit" class="btn btn-warning" style="width:auto;color:#fff;"><i class="fas fa-key"></i> Change Password</button>
            </form>
        </div>

        <!-- Account Info -->
        <div class="form-card">
            <h3 style="margin-bottom:16px;display:flex;align-items:center;gap:8px;">
                <i class="fas fa-info-circle" style="color:var(--accent);"></i> Account Summary
            </h3>
            <div class="account-info-grid">
                <div class="info-item">
                    <i class="fas fa-receipt" style="color:var(--primary);"></i>
                    <div><span class="info-label">Total Transactions</span><span class="info-value"><%= totalExpenses %></span></div>
                </div>
                <div class="info-item">
                    <i class="fas fa-arrow-down" style="color:var(--danger);"></i>
                    <div><span class="info-label">Total Spent</span><span class="info-value"><%= cs %><%= String.format("%,.2f", totalSpent) %></span></div>
                </div>
                <div class="info-item">
                    <i class="fas fa-arrow-up" style="color:var(--success);"></i>
                    <div><span class="info-label">Total Income</span><span class="info-value"><%= cs %><%= String.format("%,.2f", totalIncome) %></span></div>
                </div>
                <div class="info-item">
                    <i class="fas fa-balance-scale" style="color:<%= (totalIncome - totalSpent) >= 0 ? "var(--success)" : "var(--danger)" %>;"></i>
                    <div><span class="info-label">Net Balance</span><span class="info-value" style="color:<%= (totalIncome - totalSpent) >= 0 ? "var(--success)" : "var(--danger)" %>;"><%= cs %><%= String.format("%,.2f", totalIncome - totalSpent) %></span></div>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
.profile-layout { display: grid; grid-template-columns: 320px 1fr; gap: 24px; }
.profile-card {
    background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--border-radius);
    box-shadow: var(--shadow-sm); overflow: hidden; height: fit-content; position: sticky; top: calc(var(--nav-height) + 24px);
}
.profile-avatar { text-align: center; padding: 32px 20px 20px; background: linear-gradient(135deg, var(--primary-50), rgba(6,182,212,0.05)); }
.avatar-large {
    width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 12px;
    background: linear-gradient(135deg, var(--primary), var(--accent));
    color: white; font-size: 2rem; font-weight: 800;
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 4px 12px rgba(79,70,229,0.3);
}
.profile-stats { display: grid; grid-template-columns: repeat(3, 1fr); border-top: 1px solid var(--border-color); }
.profile-stat {
    text-align: center; padding: 16px 8px;
    border-right: 1px solid var(--border-color);
}
.profile-stat:last-child { border-right: none; }
.stat-value { display: block; font-size: 1rem; font-weight: 800; color: var(--text-primary); }
.stat-label { display: block; font-size: 0.72rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.3px; }
.account-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.info-item {
    display: flex; align-items: center; gap: 12px; padding: 12px;
    background: var(--gray-50); border-radius: var(--border-radius-sm);
}
.info-item i { font-size: 1.2rem; }
.info-label { display: block; font-size: 0.75rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.3px; }
.info-value { display: block; font-size: 1rem; font-weight: 700; color: var(--text-primary); }
.btn-warning { background: var(--warning); }
.btn-warning:hover { background: #d97706; }
@media (max-width: 768px) {
    .profile-layout { grid-template-columns: 1fr; }
    .profile-card { position: static; }
    .account-info-grid { grid-template-columns: 1fr; }
}
</style>

<%@ include file="includes/footer.jspf" %>
</body>
</html>
