<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password — QuickExpense</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="auth-wrapper">
        <div class="auth-particles" id="particles"></div>
        <div class="auth-box">
            <div class="auth-logo">
                <i class="fas fa-key"></i>
            </div>
            <h2>Forgot Password</h2>
            <p class="auth-subtitle">Enter your email and we'll send you a reset link</p>

            <% String error = request.getParameter("error"); %>
            <% if (error != null) { %>
                <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
            <% } %>
            <% String message = request.getParameter("message"); %>
            <% if (message != null) { %>
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> <%= message %></div>
            <% } %>

            <form action="ForgotPasswordServlet" method="post" id="forgotForm">
                <div class="form-group">
                    <label><i class="fas fa-envelope"></i> Email Address</label>
                    <input type="email" name="email" placeholder="you@example.com" required autocomplete="email" autofocus>
                </div>
                <button type="submit" class="btn btn-primary btn-block btn-lg" id="resetBtn">
                    <i class="fas fa-paper-plane"></i> Send Reset Link
                </button>
            </form>

            <div class="auth-divider"><span>or</span></div>
            <p class="auth-footer"><a href="index.jsp"><i class="fas fa-arrow-left"></i> Back to Login</a></p>
        </div>
    </div>

    <script>
    // Animated background particles
    (function() {
        const container = document.getElementById('particles');
        if (!container) return;
        for (let i = 0; i < 30; i++) {
            const p = document.createElement('div');
            p.className = 'particle';
            p.style.left = Math.random() * 100 + '%';
            p.style.top = Math.random() * 100 + '%';
            p.style.animationDelay = Math.random() * 6 + 's';
            p.style.animationDuration = (4 + Math.random() * 8) + 's';
            container.appendChild(p);
        }
    })();

    document.getElementById('forgotForm').addEventListener('submit', function() {
        var btn = document.getElementById('resetBtn');
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
    });
    </script>
</body>
</html>
