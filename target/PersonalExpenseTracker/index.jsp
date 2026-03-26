<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — QuickExpense</title>
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
                <i class="fas fa-wallet"></i>
            </div>
            <h2>Welcome Back</h2>
            <p class="auth-subtitle">Sign in to your QuickExpense account</p>

            <% String error = request.getParameter("error"); %>
            <% if (error != null) { %>
                <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
            <% } %>
            <% String message = request.getParameter("message"); %>
            <% if (message != null) { %>
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> <%= message %></div>
            <% } %>

            <form action="LoginServlet" method="post" id="loginForm">
                <div class="form-group">
                    <label><i class="fas fa-envelope"></i> Email Address</label>
                    <input type="email" name="email" placeholder="you@example.com" required autocomplete="email" autofocus>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-lock"></i> Password</label>
                    <div class="password-wrapper">
                        <input type="password" name="password" id="loginPassword" placeholder="Enter your password" required autocomplete="current-password">
                        <button type="button" class="password-toggle" onclick="togglePasswordVisibility('loginPassword', this)">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                </div>
                <button type="submit" class="btn btn-primary btn-block btn-lg">
                    <i class="fas fa-sign-in-alt"></i> Sign In
                </button>
            </form>

            <p style="text-align:right;margin-top:10px;"><a href="forgotPassword.jsp" style="color:var(--primary);font-size:0.85rem;text-decoration:none;"><i class="fas fa-key"></i> Forgot Password?</a></p>

            <div class="auth-divider"><span>or</span></div>
            <p class="auth-footer">Don't have an account? <a href="register.jsp">Create one free →</a></p>
        </div>
    </div>

    <script>
    function togglePasswordVisibility(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            icon.className = 'fas fa-eye-slash';
        } else {
            input.type = 'password';
            icon.className = 'fas fa-eye';
        }
    }

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
    </script>
</body>
</html>
