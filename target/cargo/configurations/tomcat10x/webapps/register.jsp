<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register — QuickExpense</title>
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
                <i class="fas fa-user-plus"></i>
            </div>
            <h2>Create Account</h2>
            <p class="auth-subtitle">Start tracking your expenses professionally</p>

            <% String error = request.getParameter("error"); %>
            <% if (error != null) { %>
                <div class="alert alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
            <% } %>

            <form action="RegisterServlet" method="post" id="registerForm" onsubmit="return validateForm();">
                <div class="form-group">
                    <label><i class="fas fa-user"></i> Full Name / Username</label>
                    <input type="text" name="username" placeholder="Choose a display name" required minlength="3" autocomplete="name">
                </div>
                <div class="form-group">
                    <label><i class="fas fa-envelope"></i> Email Address</label>
                    <input type="email" name="email" id="regEmail" placeholder="you@example.com" required autocomplete="email">
                    <small id="emailHint" class="field-hint">We'll send monthly expense reports to this email</small>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-lock"></i> Password</label>
                    <div class="password-wrapper">
                        <input type="password" id="password" name="password" placeholder="Min. 6 characters" required minlength="6" autocomplete="new-password">
                        <button type="button" class="password-toggle" onclick="togglePasswordVisibility('password', this)">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                    <div class="password-strength" id="strengthBar">
                        <div class="strength-fill" id="strengthFill"></div>
                    </div>
                    <small id="strengthText" class="field-hint"></small>
                </div>
                <div class="form-group">
                    <label><i class="fas fa-shield-alt"></i> Confirm Password</label>
                    <div class="password-wrapper">
                        <input type="password" id="confirmPassword" placeholder="Re-enter your password" required autocomplete="new-password">
                        <button type="button" class="password-toggle" onclick="togglePasswordVisibility('confirmPassword', this)">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                    <small id="passwordMatch" class="field-hint" style="display:none;"></small>
                </div>
                <button type="submit" class="btn btn-primary btn-block btn-lg">
                    <i class="fas fa-user-plus"></i> Create Account
                </button>
            </form>

            <div class="auth-divider"><span>or</span></div>
            <p class="auth-footer">Already have an account? <a href="index.jsp">Sign in →</a></p>
        </div>
    </div>

    <script>
    function togglePasswordVisibility(inputId, btn) {
        const input = document.getElementById(inputId);
        const icon = btn.querySelector('i');
        if (input.type === 'password') { input.type = 'text'; icon.className = 'fas fa-eye-slash'; }
        else { input.type = 'password'; icon.className = 'fas fa-eye'; }
    }

    // Password strength indicator
    const pwd = document.getElementById('password');
    const confirmPwd = document.getElementById('confirmPassword');
    const matchMsg = document.getElementById('passwordMatch');
    const strengthFill = document.getElementById('strengthFill');
    const strengthText = document.getElementById('strengthText');

    pwd.addEventListener('input', function() {
        const val = this.value;
        let strength = 0;
        if (val.length >= 6) strength++;
        if (val.length >= 10) strength++;
        if (/[A-Z]/.test(val)) strength++;
        if (/[0-9]/.test(val)) strength++;
        if (/[^A-Za-z0-9]/.test(val)) strength++;

        const colors = ['#ef4444', '#f59e0b', '#f59e0b', '#10b981', '#059669'];
        const labels = ['Weak', 'Fair', 'Fair', 'Strong', 'Excellent'];
        const widths = ['20%', '40%', '60%', '80%', '100%'];

        const idx = Math.min(strength, 4);
        strengthFill.style.width = val.length > 0 ? widths[idx] : '0%';
        strengthFill.style.background = colors[idx];
        strengthText.textContent = val.length > 0 ? labels[idx] : '';
        strengthText.style.color = colors[idx];
        checkMatch();
    });

    confirmPwd.addEventListener('input', checkMatch);

    function checkMatch() {
        if (confirmPwd.value.length === 0) { matchMsg.style.display = 'none'; return; }
        matchMsg.style.display = 'block';
        if (pwd.value === confirmPwd.value) {
            matchMsg.textContent = '✓ Passwords match';
            matchMsg.style.color = '#10b981';
            confirmPwd.style.borderColor = '#10b981';
        } else {
            matchMsg.textContent = '✗ Passwords do not match';
            matchMsg.style.color = '#ef4444';
            confirmPwd.style.borderColor = '#ef4444';
        }
    }

    function validateForm() {
        if (pwd.value !== confirmPwd.value) { alert('Passwords do not match!'); return false; }
        if (pwd.value.length < 6) { alert('Password must be at least 6 characters.'); return false; }
        const email = document.getElementById('regEmail').value;
        if (!email.includes('@') || !email.includes('.')) { alert('Please enter a valid email address.'); return false; }
        return true;
    }

    // Animated particles
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
