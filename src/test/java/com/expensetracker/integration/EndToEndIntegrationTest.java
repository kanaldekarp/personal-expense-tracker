package com.expensetracker.integration;

import com.expensetracker.dao.*;
import com.expensetracker.model.*;
import com.expensetracker.util.DBConnection;
import com.expensetracker.util.PasswordUtils;
import com.expensetracker.util.Summary;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndIntegrationTest {

    private static int testUserId;
    private static final String TEST_EMAIL = "e2e_integration@test.com";
    private static final String TEST_USERNAME = "e2e_tester";
    private static final String TEST_PASSWORD = "SecurePass123";

    @BeforeAll
    static void setUp() throws Exception {
        // Clean up any leftover test data
        try (Connection con = DBConnection.getConnection()) {
            // First find user id if exists to clean related data
            PreparedStatement findUser = con.prepareStatement("SELECT id FROM users WHERE email = ?");
            findUser.setString(1, TEST_EMAIL);
            ResultSet findRs = findUser.executeQuery();
            if (findRs.next()) {
                int oldUserId = findRs.getInt("id");
                cleanUserData(con, oldUserId);
            }
            con.prepareStatement("DELETE FROM users WHERE email = '" + TEST_EMAIL + "'").executeUpdate();
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            cleanUserData(con, testUserId);
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    private static void cleanUserData(Connection con, int userId) throws SQLException {
        // Clean in correct order for foreign keys
        con.prepareStatement("DELETE FROM expense_tags WHERE expense_id IN (SELECT id FROM expenses WHERE user_id = " + userId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM tags WHERE user_id = " + userId).executeUpdate();
        con.prepareStatement("DELETE FROM expenses WHERE user_id = " + userId).executeUpdate();
        con.prepareStatement("DELETE FROM income WHERE user_id = " + userId).executeUpdate();
        con.prepareStatement("DELETE FROM recurring_expenses WHERE user_id = " + userId).executeUpdate();
        con.prepareStatement("DELETE FROM savings_goals WHERE user_id = " + userId).executeUpdate();
        con.prepareStatement("DELETE FROM budgets WHERE user_id = " + userId).executeUpdate();
    }

    // ===== 1. USER REGISTRATION =====

    @Test
    @Order(1)
    @DisplayName("Step 1: Register a new user")
    void testUserRegistration() throws Exception {
        String hashedPassword = PasswordUtils.hashPassword(TEST_PASSWORD);

        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES (?, ?, ?) RETURNING id");
            ps.setString(1, TEST_USERNAME);
            ps.setString(2, TEST_EMAIL);
            ps.setString(3, hashedPassword);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "User should be created");
            testUserId = rs.getInt("id");
            assertTrue(testUserId > 0, "User ID should be positive");
        }
    }

    // ===== 2. USER LOGIN VERIFICATION =====

    @Test
    @Order(2)
    @DisplayName("Step 2: Verify login credentials")
    void testLoginVerification() throws Exception {
        String hashedPassword = PasswordUtils.hashPassword(TEST_PASSWORD);
        
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, username FROM users WHERE email = ? AND password = ?");
            ps.setString(1, TEST_EMAIL);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Should find user with correct credentials");
            assertEquals(testUserId, rs.getInt("id"));
            assertEquals(TEST_USERNAME, rs.getString("username"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("Step 2b: Wrong password fails login")
    void testLoginWrongPassword() throws Exception {
        String wrongHash = PasswordUtils.hashPassword("WrongPassword");
        
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM users WHERE email = ? AND password = ?");
            ps.setString(1, TEST_EMAIL);
            ps.setString(2, wrongHash);
            ResultSet rs = ps.executeQuery();
            assertFalse(rs.next(), "Should NOT find user with wrong password");
        }
    }

    // ===== 3. ADD EXPENSES =====

    @Test
    @Order(4)
    @DisplayName("Step 3: Add multiple expenses")
    void testAddExpenses() {
        ExpenseDAO dao = new ExpenseDAO();
        
        // Food expenses
        Expense e1 = new Expense();
        e1.setUserId(testUserId);
        e1.setTitle("Groceries");
        e1.setAmount(1500.0);
        e1.setCategory("Food");
        e1.setDate(LocalDate.now());
        e1.setDescription("Weekly groceries");
        assertTrue(dao.addExpense(e1));

        Expense e2 = new Expense();
        e2.setUserId(testUserId);
        e2.setTitle("Restaurant Dinner");
        e2.setAmount(2000.0);
        e2.setCategory("Food");
        e2.setDate(LocalDate.now());
        assertTrue(dao.addExpense(e2));

        // Transport expense
        Expense e3 = new Expense();
        e3.setUserId(testUserId);
        e3.setTitle("Uber Ride");
        e3.setAmount(350.0);
        e3.setCategory("Transport");
        e3.setDate(LocalDate.now());
        assertTrue(dao.addExpense(e3));

        // Entertainment expense
        Expense e4 = new Expense();
        e4.setUserId(testUserId);
        e4.setTitle("Movie Tickets");
        e4.setAmount(500.0);
        e4.setCategory("Entertainment");
        e4.setDate(LocalDate.now());
        assertTrue(dao.addExpense(e4));

        assertEquals(4, dao.countExpenses(testUserId));
    }

    // ===== 4. DASHBOARD SUMMARY =====

    @Test
    @Order(5)
    @DisplayName("Step 4: Verify dashboard summary calculations")
    void testDashboardSummary() {
        ExpenseDAO dao = new ExpenseDAO();
        List<Expense> expenses = dao.getAllExpensesByUser(testUserId);

        assertEquals(4, Summary.getExpenseCount(expenses));
        assertEquals(4350.0, Summary.getTotalAmount(expenses), 0.01);
        assertEquals("Food", Summary.getTopCategory(expenses));
        
        String recent = Summary.getRecentExpense(expenses, "₹");
        assertNotNull(recent);
        assertNotEquals("N/A", recent);
    }

    // ===== 5. CATEGORY ANALYSIS =====

    @Test
    @Order(6)
    @DisplayName("Step 5: Category totals are correct")
    void testCategoryAnalysis() {
        ExpenseDAO dao = new ExpenseDAO();
        List<Expense> expenses = dao.getAllExpensesByUser(testUserId);
        Map<String, Double> totals = ExpenseDAO.getCategoryTotals(expenses);

        assertEquals(3500.0, totals.get("Food"), 0.01);
        assertEquals(350.0, totals.get("Transport"), 0.01);
        assertEquals(500.0, totals.get("Entertainment"), 0.01);
    }

    // ===== 6. INCOME TRACKING =====

    @Test
    @Order(7)
    @DisplayName("Step 6: Add and track income")
    void testIncomeTracking() {
        IncomeDAO dao = new IncomeDAO();
        
        Income salary = new Income();
        salary.setUserId(testUserId);
        salary.setSource("Salary");
        salary.setAmount(75000.0);
        salary.setDate(LocalDate.now());
        salary.setRecurring(true);
        salary.setNotes("Monthly salary");
        assertTrue(dao.addIncome(salary));

        Income freelance = new Income();
        freelance.setUserId(testUserId);
        freelance.setSource("Freelance");
        freelance.setAmount(15000.0);
        freelance.setDate(LocalDate.now());
        freelance.setRecurring(false);
        assertTrue(dao.addIncome(freelance));

        assertEquals(90000.0, dao.getTotalIncome(testUserId), 0.01);
        assertEquals(2, dao.getAllByUser(testUserId).size());
    }

    // ===== 7. BUDGET MANAGEMENT =====

    @Test
    @Order(8)
    @DisplayName("Step 7: Set and verify budgets")
    void testBudgetManagement() {
        BudgetDAO dao = new BudgetDAO();
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        // Set category budgets
        dao.saveBudget(new Budget(testUserId, "Food", 5000.0, month, year));
        dao.saveBudget(new Budget(testUserId, "Transport", 2000.0, month, year));
        dao.saveBudget(new Budget(testUserId, "Entertainment", 3000.0, month, year));

        Map<String, Double> budgetMap = dao.getBudgetMap(testUserId, month, year);
        assertEquals(3, budgetMap.size());
        assertEquals(10000.0, dao.getCategoryBudgetSum(testUserId, month, year), 0.01);

        // Set overall budget
        dao.saveBudget(new Budget(testUserId, BudgetDAO.TOTAL_CATEGORY, 15000.0, month, year));
        assertEquals(15000.0, dao.getTotalBudget(testUserId, month, year), 0.01);
    }

    // ===== 8. BUDGET vs SPENDING =====

    @Test
    @Order(9)
    @DisplayName("Step 8: Budget vs Spending comparison")
    void testBudgetVsSpending() {
        ExpenseDAO expenseDAO = new ExpenseDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        double totalSpending = expenseDAO.getSpendingForMonth(testUserId, month, year);
        double totalBudget = budgetDAO.getTotalBudget(testUserId, month, year);

        assertTrue(totalSpending > 0, "Should have spending");
        assertTrue(totalBudget > 0, "Should have budget");
        assertTrue(totalSpending < totalBudget, "Spending should be within budget");

        // Category-level check
        Map<String, Double> catSpending = expenseDAO.getCategoryTotalsForMonth(testUserId, month, year);
        Map<String, Double> catBudget = budgetDAO.getBudgetMap(testUserId, month, year);
        
        for (Map.Entry<String, Double> entry : catSpending.entrySet()) {
            Double budget = catBudget.get(entry.getKey());
            if (budget != null) {
                assertTrue(entry.getValue() <= budget,
                    entry.getKey() + " spending " + entry.getValue() + " should be within budget " + budget);
            }
        }
    }

    // ===== 9. SAVINGS GOALS =====

    @Test
    @Order(10)
    @DisplayName("Step 9: Create and fund savings goals")
    void testSavingsGoals() {
        SavingsGoalDAO dao = new SavingsGoalDAO();

        SavingsGoal vacay = new SavingsGoal();
        vacay.setUserId(testUserId);
        vacay.setName("Vacation");
        vacay.setTargetAmount(50000);
        vacay.setSavedAmount(0);
        vacay.setDeadline(LocalDate.of(2025, 12, 31));
        assertTrue(dao.add(vacay));

        // Fund the goal
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        int goalId = goals.get(0).getId();
        dao.addSavedAmount(goalId, testUserId, 10000);
        dao.addSavedAmount(goalId, testUserId, 5000);

        SavingsGoal updated = dao.getById(goalId, testUserId);
        assertEquals(15000, updated.getSavedAmount(), 0.01);
        assertEquals(30.0, updated.getProgressPercent(), 0.01);
        assertEquals(35000, updated.getRemainingAmount(), 0.01);
    }

    // ===== 10. RECURRING EXPENSES =====

    @Test
    @Order(11)
    @DisplayName("Step 10: Set up recurring expenses")
    void testRecurringExpenses() {
        RecurringExpenseDAO dao = new RecurringExpenseDAO();

        RecurringExpense netflix = new RecurringExpense();
        netflix.setUserId(testUserId);
        netflix.setTitle("Netflix");
        netflix.setCategory("Entertainment");
        netflix.setAmount(649);
        netflix.setFrequency("monthly");
        netflix.setNextDue(LocalDate.now().plusDays(15));
        netflix.setActive(true);
        assertTrue(dao.add(netflix));

        RecurringExpense gym = new RecurringExpense();
        gym.setUserId(testUserId);
        gym.setTitle("Gym");
        gym.setCategory("Health");
        gym.setAmount(1500);
        gym.setFrequency("monthly");
        gym.setNextDue(LocalDate.now().plusDays(5));
        gym.setActive(true);
        assertTrue(dao.add(gym));

        double recurringTotal = dao.getMonthlyRecurringTotal(testUserId);
        assertEquals(2149.0, recurringTotal, 0.01);
        assertEquals(2, dao.getAllByUser(testUserId).size());
    }

    // ===== 11. TAGS =====

    @Test
    @Order(12)
    @DisplayName("Step 11: Create tags and tag expenses")
    void testTagging() {
        TagDAO tagDAO = new TagDAO();
        ExpenseDAO expenseDAO = new ExpenseDAO();

        // Create tags
        assertTrue(tagDAO.addTag(new com.expensetracker.model.Tag(0, testUserId, "Essential", "#22c55e")));
        assertTrue(tagDAO.addTag(new com.expensetracker.model.Tag(0, testUserId, "Luxury", "#ef4444")));

        List<com.expensetracker.model.Tag> tags = tagDAO.getAllByUser(testUserId);
        assertEquals(2, tags.size());

        // Tag an expense
        List<Expense> expenses = expenseDAO.getAllExpensesByUser(testUserId);
        assertFalse(expenses.isEmpty());
        int expenseId = expenses.get(0).getId();
        int essentialTagId = tags.stream().filter(t -> "Essential".equals(t.getName())).findFirst().get().getId();

        tagDAO.tagExpense(expenseId, essentialTagId);
        
        List<com.expensetracker.model.Tag> expenseTags = tagDAO.getTagsForExpense(expenseId);
        assertEquals(1, expenseTags.size());
        assertEquals("Essential", expenseTags.get(0).getName());
    }

    // ===== 12. SPENDING ANALYTICS =====

    @Test
    @Order(13)
    @DisplayName("Step 12: Verify spending analytics")
    void testSpendingAnalytics() {
        ExpenseDAO dao = new ExpenseDAO();
        LocalDate today = LocalDate.now();

        // Daily spending
        Map<Integer, Double> daily = dao.getDailySpending(testUserId, today.getMonthValue(), today.getYear());
        assertNotNull(daily);
        assertTrue(daily.containsKey(today.getDayOfMonth()));

        // Monthly totals
        Map<String, Double> monthly = dao.getMonthlyTotals(testUserId, 6);
        assertNotNull(monthly);
        assertFalse(monthly.isEmpty());

        // Average daily spending
        double avg = dao.getAvgDailySpending(testUserId);
        assertTrue(avg >= 0);

        // Top expenses
        List<Expense> top = dao.getTopExpenses(testUserId, 3);
        assertNotNull(top);
        assertTrue(top.size() <= 3);
        if (top.size() >= 2) {
            assertTrue(top.get(0).getAmount() >= top.get(1).getAmount());
        }

        // Prediction
        double prediction = dao.predictNextMonthSpending(testUserId);
        assertTrue(prediction >= 0);
    }

    // ===== 13. PROFILE MANAGEMENT =====

    @Test
    @Order(14)
    @DisplayName("Step 13: Update user profile")
    void testProfileManagement() {
        ExpenseDAO dao = new ExpenseDAO();

        // Verify current profile
        assertEquals(TEST_USERNAME, dao.getUsername(testUserId));
        assertEquals(TEST_EMAIL, dao.getUserEmail(testUserId));

        // Update profile
        assertTrue(dao.updateUserProfile(testUserId, "e2e_updated", TEST_EMAIL));
        assertEquals("e2e_updated", dao.getUsername(testUserId));

        // Revert
        dao.updateUserProfile(testUserId, TEST_USERNAME, TEST_EMAIL);

        // Update password
        String newHash = PasswordUtils.hashPassword("NewPassword456");
        assertTrue(dao.updateUserPassword(testUserId, newHash));
        assertTrue(dao.verifyPassword(testUserId, newHash));
        assertFalse(dao.verifyPassword(testUserId, PasswordUtils.hashPassword(TEST_PASSWORD)));
    }

    // ===== 14. DATA FILTERING =====

    @Test
    @Order(15)
    @DisplayName("Step 14: Filter expenses by date and category")
    void testDataFiltering() {
        ExpenseDAO dao = new ExpenseDAO();
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();

        // Filter by date range
        List<Expense> filtered = dao.getFilteredExpenses(testUserId, yesterday, tomorrow, null);
        assertFalse(filtered.isEmpty());

        // Filter by date + category
        List<Expense> foodOnly = dao.getFilteredExpenses(testUserId, yesterday, tomorrow, "Food");
        assertTrue(foodOnly.stream().allMatch(e -> "Food".equals(e.getCategory())));
        assertEquals(2, foodOnly.size());

        // Non-existent category
        List<Expense> none = dao.getFilteredExpenses(testUserId, yesterday, tomorrow, "NonExistent");
        assertTrue(none.isEmpty());
    }

    // ===== 15. MULTI-CURRENCY SUPPORT =====

    @Test
    @Order(16)
    @DisplayName("Step 15: Currency symbol formatting works")
    void testCurrencySupport() {
        ExpenseDAO dao = new ExpenseDAO();
        List<Expense> expenses = dao.getAllExpensesByUser(testUserId);

        // Test with different currency symbols
        String inr = Summary.getRecentExpense(expenses, "₹");
        assertTrue(inr.contains("₹"));

        String usd = Summary.getRecentExpense(expenses, "$");
        assertTrue(usd.contains("$"));

        String eur = Summary.getRecentExpense(expenses, "€");
        assertTrue(eur.contains("€"));
    }

    // ===== 16. BULK OPERATIONS =====

    @Test
    @Order(17)
    @DisplayName("Step 16: Bulk add expenses (CSV import simulation)")
    void testBulkOperations() {
        ExpenseDAO dao = new ExpenseDAO();
        int initialCount = dao.countExpenses(testUserId);

        java.util.ArrayList<Expense> bulk = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Expense e = new Expense();
            e.setUserId(testUserId);
            e.setTitle("Bulk Item " + (i + 1));
            e.setAmount(100.0 + i * 10);
            e.setCategory("Misc");
            e.setDate(LocalDate.now());
            e.setDescription("Bulk imported");
            bulk.add(e);
        }

        int added = dao.bulkAddExpenses(bulk);
        assertEquals(10, added);
        assertEquals(initialCount + 10, dao.countExpenses(testUserId));
    }

    // ===== 17. COMPREHENSIVE FINAL CHECK =====

    @Test
    @Order(18)
    @DisplayName("Step 17: Final comprehensive data integrity check")
    void testFinalDataIntegrity() {
        ExpenseDAO expenseDAO = new ExpenseDAO();
        IncomeDAO incomeDAO = new IncomeDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        RecurringExpenseDAO recurringDAO = new RecurringExpenseDAO();
        SavingsGoalDAO goalsDAO = new SavingsGoalDAO();
        TagDAO tagDAO = new TagDAO();

        // Verify all data exists
        assertTrue(expenseDAO.countExpenses(testUserId) >= 14, "Should have at least 14 expenses");
        assertEquals(2, incomeDAO.getAllByUser(testUserId).size(), "Should have 2 income entries");
        assertTrue(budgetDAO.getBudgetsByMonth(testUserId, LocalDate.now().getMonthValue(), LocalDate.now().getYear()).size() >= 3, "Should have at least 3 category budgets");
        assertEquals(2, recurringDAO.getAllByUser(testUserId).size(), "Should have 2 recurring expenses");
        assertEquals(1, goalsDAO.getAllByUser(testUserId).size(), "Should have 1 savings goal");
        assertTrue(tagDAO.getAllByUser(testUserId).size() >= 1, "Should have at least 1 tag");

        // Income > Expenses check
        double totalIncome = incomeDAO.getTotalIncome(testUserId);
        double totalExpenses = Summary.getTotalAmount(expenseDAO.getAllExpensesByUser(testUserId));
        System.out.println("  Total Income: " + totalIncome);
        System.out.println("  Total Expenses: " + totalExpenses);
        System.out.println("  Net Savings: " + (totalIncome - totalExpenses));
    }
}
