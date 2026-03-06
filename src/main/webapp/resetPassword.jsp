<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password — QuickExpense</title>
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
                <i class="fas fa-lock-open"></i>
            </div>
            <h2>Reset Password</h2>
            <p class="auth-subtitle">Enter your new password below</p>

            <% String error = request.getParameter("error"); %>
            <% if (error != null) { %>
                <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
            <% } %>
            <% String message = request.getParameter("message"); %>
            <% if (message != null) { %>
                <div class="alert alert-success"><i class="fas fa-check-circle"></i> <%= message %></div>
            <% } %>

            <%
                String token = request.getParameter("token");
                if (token == null || token.isEmpty()) {
            %>
                <div class="alert alert-error"><i class="fas fa-exclamation-triangle"></i> Invalid or missing reset token. Please request a new password reset link.</div>
                <p class="auth-footer"><a href="forgotPassword.jsp"><i class="fas fa-redo"></i> Request New Link</a></p>
            <% } else { %>

            <form action="ResetPasswordServlet" method="post" id="resetForm" onsubmit="return validateForm()">
                <input type="hidden" name="token" value="<%= token %>">
                <div class="form-group">
                    <label><i class="fas fa-lock"></i> New Password</label>
                    <div class="password-wrapper">
                        <input type="password" name="password" id="newPassword" placeholder="Enter new password" required minlength="6" autocomplete="new-password">
                        <button type="button" class="password-toggle" onclick="togglePasswordVisibility('newPassword', this)">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                    <small class="field-hint">Minimum 6 characters</small>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-lock"></i> Confirm Password</label>
                    <div class="password-wrapper">
                        <input type="password" name="confirmPassword" id="confirmPassword" placeholder="Confirm new password" required minlength="6" autocomplete="new-password">
                        <button type="button" class="password-toggle" onclick="togglePasswordVisibility('confirmPassword', this)">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                </div>
                <button type="submit" class="btn btn-primary btn-block btn-lg">
                    <i class="fas fa-save"></i> Reset Password
                </button>
            </form>

            <% } %>

            <div class="auth-divider"><span>or</span></div>
            <p class="auth-footer"><a href="index.jsp"><i class="fas fa-arrow-left"></i> Back to Login</a></p>
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

    function validateForm() {
        var pw = document.getElementById('newPassword').value;
        var cpw = document.getElementById('confirmPassword').value;
        if (pw.length < 6) {
            alert('Password must be at least 6 characters long.');
            return false;
        }
        if (pw !== cpw) {
            alert('Passwords do not match.');
            return false;
        }
        return true;
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
