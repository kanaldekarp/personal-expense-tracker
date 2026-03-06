package com.expensetracker.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * EmailService - Professional SMTP email utility for QuickExpense.
 * Supports HTML emails, file attachments, and customizable templates.
 */
public class EmailService {

    private static final Properties config = new Properties();
    private static Session mailSession;

    /**
     * Get config value: environment variable > db.properties > default.
     */
    private static String getConfig(String envKey, String propKey, String defaultVal) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isEmpty()) return envVal;
        return config.getProperty(propKey, defaultVal);
    }

    static {
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                config.load(input);
            } else {
                System.out.println("EmailService: db.properties not found, using environment variables");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Configure SMTP session (env vars override db.properties)
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.host", getConfig("SMTP_HOST", "mail.smtp.host", "smtp.gmail.com"));
        smtpProps.put("mail.smtp.port", getConfig("SMTP_PORT", "mail.smtp.port", "587"));
        smtpProps.put("mail.smtp.auth", getConfig("SMTP_AUTH", "mail.smtp.auth", "true"));
        smtpProps.put("mail.smtp.starttls.enable", getConfig("SMTP_STARTTLS", "mail.smtp.starttls", "true"));
        smtpProps.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        smtpProps.put("mail.smtp.connectiontimeout", "10000");
        smtpProps.put("mail.smtp.timeout", "10000");

        final String senderEmail = getConfig("SMTP_EMAIL", "mail.sender.email", "");
        final String senderPassword = getConfig("SMTP_PASSWORD", "mail.sender.password", "");

        mailSession = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    /**
     * Send a plain text email.
     */
    public static boolean sendEmail(String toEmail, String subject, String body) {
        return sendHtmlEmail(toEmail, subject, body, false);
    }

    /**
     * Send an HTML email.
     */
    public static boolean sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        return sendHtmlEmail(toEmail, subject, htmlBody, true);
    }

    private static boolean sendHtmlEmail(String toEmail, String subject, String body, boolean isHtml) {
        try {
            Message message = new MimeMessage(mailSession);
            String senderName = getConfig("SMTP_SENDER_NAME", "mail.sender.name", "QuickExpense");
            String senderEmail = getConfig("SMTP_EMAIL", "mail.sender.email", "");
            message.setFrom(new InternetAddress(senderEmail, senderName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }

            Transport.send(message);
            System.out.println("✅ Email sent to: " + toEmail + " | Subject: " + subject);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Email failed to: " + toEmail + " | Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send an HTML email with a file attachment.
     */
    public static boolean sendEmailWithAttachment(String toEmail, String subject, String htmlBody, File attachment) {
        try {
            Message message = new MimeMessage(mailSession);
            String senderName = getConfig("SMTP_SENDER_NAME", "mail.sender.name", "QuickExpense");
            String senderEmail = getConfig("SMTP_EMAIL", "mail.sender.email", "");
            message.setFrom(new InternetAddress(senderEmail, senderName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // Create multipart message
            Multipart multipart = new MimeMultipart();

            // HTML body part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Attachment part
            if (attachment != null && attachment.exists()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(attachment);
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("✅ Email with attachment sent to: " + toEmail);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Email with attachment failed to: " + toEmail + " | Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===================== EMAIL TEMPLATES =====================

    /**
     * Password reset email with a secure token link.
     */
    public static boolean sendPasswordResetEmail(String toEmail, String username, String resetUrl) {
        String subject = "Reset Your QuickExpense Password \uD83D\uDD12";
        String html = getEmailWrapper("Password Reset Request",
            "<p style='font-size:16px;color:#374151;'>Hi <strong>" + escapeHtml(username) + "</strong>,</p>"
            + "<p style='color:#6b7280;'>We received a request to reset your QuickExpense password. Click the button below to set a new password:</p>"
            + "<div style='text-align:center;margin:28px 0;'>"
            + "  <a href='" + resetUrl + "' style='display:inline-block;background:linear-gradient(135deg,#4f46e5,#06b6d4);color:white;padding:14px 40px;border-radius:10px;text-decoration:none;font-weight:700;font-size:16px;box-shadow:0 4px 14px rgba(79,70,229,0.4);'>Reset Password</a>"
            + "</div>"
            + "<div style='background:#fef3c7;border:1px solid #fcd34d;border-radius:8px;padding:16px;margin:20px 0;'>"
            + "  <p style='color:#92400e;font-weight:600;margin:0;'>\u26A0\uFE0F This link expires in 30 minutes</p>"
            + "  <p style='color:#a16207;margin:4px 0 0;font-size:14px;'>If you didn't request this reset, you can safely ignore this email. Your password will remain unchanged.</p>"
            + "</div>"
            + "<p style='color:#9ca3af;font-size:13px;'>If the button doesn't work, copy and paste this URL into your browser:</p>"
            + "<p style='color:#6b7280;font-size:12px;word-break:break-all;background:#f3f4f6;padding:10px;border-radius:6px;'>" + escapeHtml(resetUrl) + "</p>"
        );
        return sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Welcome email sent after registration.
     */
    public static boolean sendWelcomeEmail(String toEmail, String username) {
        String subject = "Welcome to QuickExpense! 🎉";
        String html = getEmailWrapper("Welcome to QuickExpense!",
            "<p style='font-size:16px;color:#374151;'>Hi <strong>" + escapeHtml(username) + "</strong>,</p>"
            + "<p style='color:#6b7280;'>Your account has been created successfully. You can now start tracking your expenses and take control of your finances.</p>"
            + "<div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:8px;padding:16px;margin:20px 0;'>"
            + "  <p style='color:#166534;font-weight:600;margin:0;'>✅ What you can do:</p>"
            + "  <ul style='color:#166534;margin:8px 0 0 0;padding-left:20px;'>"
            + "    <li>Add and track daily expenses</li>"
            + "    <li>View category-wise spending analysis</li>"
            + "    <li>Generate and download reports</li>"
            + "    <li>Receive monthly expense insights via email</li>"
            + "  </ul>"
            + "</div>"
            + "<a href='#' style='display:inline-block;background:#4f46e5;color:white;padding:12px 32px;border-radius:8px;text-decoration:none;font-weight:600;margin-top:10px;'>Start Tracking →</a>"
        );
        return sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Report email sent when user downloads a report.
     */
    public static boolean sendReportEmail(String toEmail, String username, String fromDate, String toDate, File reportFile) {
        String subject = "📊 Your Expense Report (" + fromDate + " to " + toDate + ")";
        String html = getEmailWrapper("Your Expense Report",
            "<p style='font-size:16px;color:#374151;'>Hi <strong>" + escapeHtml(username) + "</strong>,</p>"
            + "<p style='color:#6b7280;'>Your expense report for the period <strong>" + fromDate + "</strong> to <strong>" + toDate + "</strong> has been generated and is attached to this email.</p>"
            + "<div style='background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:16px;margin:20px 0;'>"
            + "  <p style='color:#1e40af;font-weight:600;margin:0;'>📎 Report attached as a text file</p>"
            + "  <p style='color:#3b82f6;margin:4px 0 0;font-size:14px;'>Open the attachment to view your detailed expense breakdown.</p>"
            + "</div>"
            + "<p style='color:#9ca3af;font-size:13px;'>You can also download reports anytime from your QuickExpense dashboard.</p>"
        );
        return sendEmailWithAttachment(toEmail, subject, html, reportFile);
    }

    /**
     * Monthly analysis email with comparison and tips.
     */
    public static boolean sendMonthlyAnalysis(String toEmail, String username,
            String monthName, double thisMonthTotal, double lastMonthTotal,
            String topCategory, double topCategoryAmount,
            int transactionCount, String analysisHtml) {

        double changePercent = lastMonthTotal > 0 ? ((thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100 : 0;
        boolean increased = thisMonthTotal > lastMonthTotal;
        String changeColor = increased ? "#ef4444" : "#10b981";
        String changeIcon = increased ? "📈" : "📉";
        String changeText = increased ? "increased" : "decreased";

        String subject = "📊 Your Monthly Expense Report — " + monthName;
        String html = getEmailWrapper("Monthly Expense Analysis",
            "<p style='font-size:16px;color:#374151;'>Hi <strong>" + escapeHtml(username) + "</strong>,</p>"
            + "<p style='color:#6b7280;'>Here's your expense summary for <strong>" + monthName + "</strong>:</p>"

            // Summary cards
            + "<div style='display:flex;gap:12px;margin:20px 0;flex-wrap:wrap;'>"
            + "  <div style='flex:1;min-width:140px;background:#f9fafb;border:1px solid #e5e7eb;border-radius:12px;padding:16px;text-align:center;'>"
            + "    <p style='font-size:13px;color:#6b7280;margin:0;'>This Month</p>"
            + "    <p style='font-size:24px;font-weight:800;color:#111827;margin:4px 0;'>₹" + String.format("%,.0f", thisMonthTotal) + "</p>"
            + "  </div>"
            + "  <div style='flex:1;min-width:140px;background:#f9fafb;border:1px solid #e5e7eb;border-radius:12px;padding:16px;text-align:center;'>"
            + "    <p style='font-size:13px;color:#6b7280;margin:0;'>Last Month</p>"
            + "    <p style='font-size:24px;font-weight:800;color:#111827;margin:4px 0;'>₹" + String.format("%,.0f", lastMonthTotal) + "</p>"
            + "  </div>"
            + "  <div style='flex:1;min-width:140px;background:#f9fafb;border:1px solid #e5e7eb;border-radius:12px;padding:16px;text-align:center;'>"
            + "    <p style='font-size:13px;color:#6b7280;margin:0;'>Change</p>"
            + "    <p style='font-size:24px;font-weight:800;color:" + changeColor + ";margin:4px 0;'>" + changeIcon + " " + String.format("%.1f", Math.abs(changePercent)) + "%</p>"
            + "  </div>"
            + "</div>"

            // Key insights
            + "<div style='background:#faf5ff;border:1px solid #e9d5ff;border-radius:12px;padding:20px;margin:20px 0;'>"
            + "  <p style='font-weight:700;color:#581c87;margin:0 0 12px;font-size:15px;'>🔍 Key Insights</p>"
            + "  <table style='width:100%;border-collapse:collapse;'>"
            + "    <tr><td style='padding:6px 0;color:#6b7280;'>Top Category</td><td style='padding:6px 0;text-align:right;font-weight:600;color:#374151;'>" + escapeHtml(topCategory) + " (₹" + String.format("%,.0f", topCategoryAmount) + ")</td></tr>"
            + "    <tr><td style='padding:6px 0;color:#6b7280;'>Total Transactions</td><td style='padding:6px 0;text-align:right;font-weight:600;color:#374151;'>" + transactionCount + "</td></tr>"
            + "    <tr><td style='padding:6px 0;color:#6b7280;'>Spending " + changeText + " by</td><td style='padding:6px 0;text-align:right;font-weight:600;color:" + changeColor + ";'>" + String.format("%.1f", Math.abs(changePercent)) + "%</td></tr>"
            + "    <tr><td style='padding:6px 0;color:#6b7280;'>Avg per transaction</td><td style='padding:6px 0;text-align:right;font-weight:600;color:#374151;'>₹" + (transactionCount > 0 ? String.format("%,.0f", thisMonthTotal / transactionCount) : "0") + "</td></tr>"
            + "  </table>"
            + "</div>"

            // Tips
            + analysisHtml

            + "<p style='color:#9ca3af;font-size:13px;margin-top:24px;'>This is an automated monthly report from QuickExpense. Log in to your dashboard for detailed analytics.</p>"
        );

        return sendHtmlEmail(toEmail, subject, html);
    }

    // ===================== HELPER METHODS =====================

    /**
     * Wraps email content in a professional HTML template.
     */
    private static String getEmailWrapper(String title, String content) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'></head>"
            + "<body style='margin:0;padding:0;background:#f0f2f5;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,sans-serif;'>"
            + "<div style='max-width:600px;margin:0 auto;padding:20px;'>"
            + "  <div style='text-align:center;padding:24px 0;'>"
            + "    <div style='display:inline-block;background:linear-gradient(135deg,#4f46e5,#06b6d4);padding:10px 14px;border-radius:12px;'>"
            + "      <span style='color:white;font-size:20px;font-weight:800;letter-spacing:-0.5px;'>QuickExpense</span>"
            + "    </div>"
            + "  </div>"
            + "  <div style='background:white;border-radius:16px;padding:32px;box-shadow:0 4px 6px rgba(0,0,0,0.07);'>"
            + "    <h2 style='margin:0 0 20px;font-size:22px;color:#111827;'>" + title + "</h2>"
            + content
            + "  </div>"
            + "  <div style='text-align:center;padding:20px;'>"
            + "    <p style='color:#9ca3af;font-size:12px;margin:0;'>© 2026 QuickExpense — Professional Expense Management</p>"
            + "    <p style='color:#d1d5db;font-size:11px;margin:4px 0 0;'>This is an automated email. Please do not reply.</p>"
            + "  </div>"
            + "</div></body></html>";
    }

    /**
     * Generate smart financial tips based on spending patterns.
     */
    public static String generateTips(double thisMonth, double lastMonth, String topCategory, double foodSpend, double entertainmentSpend, double shoppingSpend) {
        StringBuilder tips = new StringBuilder();
        tips.append("<div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:12px;padding:20px;margin:20px 0;'>");
        tips.append("<p style='font-weight:700;color:#166534;margin:0 0 12px;font-size:15px;'>💡 Smart Tips for Better Management</p>");
        tips.append("<ul style='margin:0;padding-left:20px;color:#15803d;line-height:1.8;'>");

        if (thisMonth > lastMonth && lastMonth > 0) {
            double increase = ((thisMonth - lastMonth) / lastMonth) * 100;
            if (increase > 30) {
                tips.append("<li><strong>High spending alert!</strong> Your expenses jumped by " + String.format("%.0f", increase) + "%. Review non-essential purchases and set a monthly budget cap.</li>");
            } else if (increase > 10) {
                tips.append("<li>Your spending increased by " + String.format("%.0f", increase) + "%. Consider setting category-wise limits to stay on track.</li>");
            }
        } else if (thisMonth < lastMonth && lastMonth > 0) {
            tips.append("<li>Great job! 🎉 You reduced spending this month. Keep maintaining this discipline.</li>");
        }

        if (foodSpend > thisMonth * 0.4 && thisMonth > 0) {
            tips.append("<li><strong>Food expenses are over 40%</strong> of your total. Try meal prepping and cooking at home to save more.</li>");
        }
        if (entertainmentSpend > thisMonth * 0.25 && thisMonth > 0) {
            tips.append("<li><strong>Entertainment spending is high.</strong> Look for free or low-cost alternatives like outdoor activities or library resources.</li>");
        }
        if (shoppingSpend > thisMonth * 0.3 && thisMonth > 0) {
            tips.append("<li><strong>Shopping takes a big chunk.</strong> Try the 24-hour rule — wait a day before any non-essential purchase.</li>");
        }

        tips.append("<li>Use the 50/30/20 rule: 50% needs, 30% wants, 20% savings & debt repayment.</li>");
        tips.append("<li>Review your subscriptions — cancel any you haven't used in the past 30 days.</li>");
        tips.append("<li>Set up an emergency fund if you haven't already. Aim for 3-6 months of expenses.</li>");
        tips.append("</ul></div>");
        return tips.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
