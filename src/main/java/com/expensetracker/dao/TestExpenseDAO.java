package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import java.time.LocalDate;
import java.util.Random;

public class TestExpenseDAO {
    public static void main(String[] args) {
        ExpenseDAO dao = new ExpenseDAO();
        Random random = new Random();

        String[] categories = { "Food", "Travel", "Shopping", "Utilities", "Entertainment", "Health" };
        String[] descriptions = {
                "Lunch at Subway", "Uber ride", "Amazon Order", "Electricity Bill", "Movie Ticket",
                "Doctor Visit", "Coffee", "Snacks", "Train Ticket", "Online Course"
        };

        // Ensure User ID 1 exists
        try (java.sql.Connection conn = com.expensetracker.util.DBConnection.getConnection();
                java.sql.PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (id, username, password) VALUES (1, 'testuser', 'hashedpassword') ON CONFLICT (id) DO NOTHING")) {
            stmt.executeUpdate();
            System.out.println("✅ Ensured User ID 1 exists.");
        } catch (Exception e) {
            System.out.println("⚠️ Could not ensure user 1 exists: " + e.getMessage());
        }

        int successCount = 0;
        // Simulate 50 expenses for user with userId 1
        for (int i = 1; i <= 50; i++) {
            double amount = 50 + random.nextInt(450); // Between 50 and 500
            String category = categories[random.nextInt(categories.length)];
            String description = descriptions[random.nextInt(descriptions.length)];
            LocalDate date = LocalDate.now().minusDays(random.nextInt(180)); // within past 6 months

            Expense expense = new Expense();
            expense.setUserId(1); // Ensure this user exists in your DB!
            expense.setTitle(category);
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDate(date);
            expense.setDescription(description);

            if (dao.addExpense(expense)) {
                successCount++;
            }
        }

        System.out.println("✅ " + successCount + "/50 random expenses added successfully!");
    }
}
