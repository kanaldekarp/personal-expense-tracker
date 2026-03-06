package com.expensetracker.util;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.*;

/**
 * MonthlyReportScheduler - Sends automated monthly expense analysis emails
 * on the 1st of every month to all users with email addresses.
 */
public class MonthlyReportScheduler {

    private ScheduledExecutorService scheduler;
    private static MonthlyReportScheduler instance;

    private MonthlyReportScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MonthlyReportScheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public static synchronized MonthlyReportScheduler getInstance() {
        if (instance == null) {
            instance = new MonthlyReportScheduler();
        }
        return instance;
    }

    /**
     * Start the scheduler. Runs once per day, checks if it's the 1st.
     */
    public void start() {
        // Run daily at approximately 8:00 AM — check if it's the 1st of the month
        long initialDelay = calculateInitialDelay();
        long period = TimeUnit.HOURS.toSeconds(24);

        scheduler.scheduleAtFixedRate(this::runMonthlyCheck, initialDelay, period, TimeUnit.SECONDS);
        System.out.println("✅ MonthlyReportScheduler started. Will check daily for 1st-of-month reports.");
    }

    /**
     * Stop the scheduler gracefully.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("⏹ MonthlyReportScheduler stopped.");
        }
    }

    /**
     * Calculate seconds until next 8:00 AM.
     */
    private long calculateInitialDelay() {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 8);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (now.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        return (target.getTimeInMillis() - now.getTimeInMillis()) / 1000;
    }

    /**
     * Check if today is the 1st, and if so, send monthly reports.
     */
    private void runMonthlyCheck() {
        try {
            Calendar cal = Calendar.getInstance();
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

            if (dayOfMonth == 1) {
                System.out.println("📅 1st of the month detected! Generating monthly reports...");
                sendMonthlyReportsToAllUsers();
            } else {
                System.out.println("📅 MonthlyReportScheduler: Day " + dayOfMonth + " — not 1st, skipping.");
            }
        } catch (Exception e) {
            System.out.println("❌ MonthlyReportScheduler error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send monthly analysis emails to all registered users with email.
     */
    public void sendMonthlyReportsToAllUsers() {
        ExpenseDAO dao = new ExpenseDAO();
        List<Map<String, Object>> users = dao.getAllUsersWithEmail();

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        YearMonth twoMonthsAgo = YearMonth.now().minusMonths(2);
        String monthName = lastMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + lastMonth.getYear();

        System.out.println("📧 Sending monthly reports to " + users.size() + " users for " + monthName);

        for (Map<String, Object> user : users) {
            try {
                int userId = (int) user.get("id");
                String username = (String) user.get("username");
                String email = (String) user.get("email");

                if (email == null || email.isEmpty()) continue;

                // Get expenses for last month and the month before
                List<Expense> lastMonthExpenses = dao.getExpensesByMonth(userId, lastMonth);
                List<Expense> prevMonthExpenses = dao.getExpensesByMonth(userId, twoMonthsAgo);

                double lastMonthTotal = lastMonthExpenses.stream().mapToDouble(Expense::getAmount).sum();
                double prevMonthTotal = prevMonthExpenses.stream().mapToDouble(Expense::getAmount).sum();

                // Category breakdown
                Map<String, Double> categoryTotals = ExpenseDAO.getCategoryTotals(lastMonthExpenses);
                String topCategory = "-";
                double topCategoryAmount = 0;
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    if (entry.getValue() > topCategoryAmount) {
                        topCategory = entry.getKey();
                        topCategoryAmount = entry.getValue();
                    }
                }

                // Category-specific amounts for tips
                double foodSpend = categoryTotals.getOrDefault("Food", 0.0);
                double entertainmentSpend = categoryTotals.getOrDefault("Entertainment", 0.0);
                double shoppingSpend = categoryTotals.getOrDefault("Shopping", 0.0);

                // Generate tips
                String tipsHtml = EmailService.generateTips(lastMonthTotal, prevMonthTotal,
                    topCategory, foodSpend, entertainmentSpend, shoppingSpend);

                // Send the email
                boolean sent = EmailService.sendMonthlyAnalysis(email, username, monthName,
                    lastMonthTotal, prevMonthTotal, topCategory, topCategoryAmount,
                    lastMonthExpenses.size(), tipsHtml);

                if (sent) {
                    System.out.println("  ✅ Sent to: " + email);
                } else {
                    System.out.println("  ❌ Failed for: " + email);
                }

            } catch (Exception e) {
                System.out.println("  ❌ Error processing user: " + e.getMessage());
            }
        }

        System.out.println("📧 Monthly report batch complete.");
    }
}
