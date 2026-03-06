package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpenseDAO Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExpenseDAOTest {

    private static ExpenseDAO dao;
    private static int testUserId;

    @BeforeAll
    static void setUp() throws Exception {
        dao = new ExpenseDAO();
        // Create a test user
        try (Connection con = DBConnection.getConnection()) {
            // Clean up any leftover test user
            PreparedStatement del = con.prepareStatement("DELETE FROM users WHERE email = 'dao_test@test.com'");
            del.executeUpdate();

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES ('dao_tester', 'dao_test@test.com', 'hashed_pass') RETURNING id");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                testUserId = rs.getInt("id");
            }
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        // Clean up test data
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM expenses WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Add expense successfully")
    void testAddExpense() {
        Expense e = new Expense();
        e.setUserId(testUserId);
        e.setTitle("Test Grocery");
        e.setAmount(250.50);
        e.setCategory("Food");
        e.setDate(LocalDate.now());
        e.setDescription("Test grocery purchase");

        assertTrue(dao.addExpense(e), "addExpense should return true");
    }

    @Test
    @Order(2)
    @DisplayName("Get all expenses by user returns added expense")
    void testGetAllExpensesByUser() {
        List<Expense> expenses = dao.getAllExpensesByUser(testUserId);
        assertNotNull(expenses);
        assertFalse(expenses.isEmpty(), "Should have at least one expense");
        assertEquals("Test Grocery", expenses.get(0).getTitle());
    }

    @Test
    @Order(3)
    @DisplayName("Add more expenses for aggregation tests")
    void testAddMultipleExpenses() {
        Expense e1 = new Expense();
        e1.setUserId(testUserId);
        e1.setTitle("Bus Fare");
        e1.setAmount(50);
        e1.setCategory("Transport");
        e1.setDate(LocalDate.now());

        Expense e2 = new Expense();
        e2.setUserId(testUserId);
        e2.setTitle("Restaurant");
        e2.setAmount(800);
        e2.setCategory("Food");
        e2.setDate(LocalDate.now());

        assertTrue(dao.addExpense(e1));
        assertTrue(dao.addExpense(e2));
    }

    @Test
    @Order(4)
    @DisplayName("getCategoryTotals calculates correct totals")
    void testGetCategoryTotals() {
        List<Expense> expenses = dao.getAllExpensesByUser(testUserId);
        Map<String, Double> totals = ExpenseDAO.getCategoryTotals(expenses);
        
        assertNotNull(totals);
        assertTrue(totals.containsKey("Food"));
        assertTrue(totals.containsKey("Transport"));
        assertEquals(1050.50, totals.get("Food"), 0.01);
        assertEquals(50.0, totals.get("Transport"), 0.01);
    }

    @Test
    @Order(5)
    @DisplayName("getExpensesByMonth returns expenses for current month")
    void testGetExpensesByMonth() {
        YearMonth currentMonth = YearMonth.now();
        List<Expense> expenses = dao.getExpensesByMonth(testUserId, currentMonth);
        assertNotNull(expenses);
        assertTrue(expenses.size() >= 3, "Should have at least 3 expenses this month");
    }

    @Test
    @Order(6)
    @DisplayName("getDailySpending returns data for current month")
    void testGetDailySpending() {
        LocalDate today = LocalDate.now();
        Map<Integer, Double> daily = dao.getDailySpending(testUserId, today.getMonthValue(), today.getYear());
        assertNotNull(daily);
        assertTrue(daily.containsKey(today.getDayOfMonth()), "Today should have spending");
    }

    @Test
    @Order(7)
    @DisplayName("getMonthlyTotals returns data")
    void testGetMonthlyTotals() {
        Map<String, Double> monthly = dao.getMonthlyTotals(testUserId, 6);
        assertNotNull(monthly);
        assertFalse(monthly.isEmpty(), "Should have at least current month");
    }

    @Test
    @Order(8)
    @DisplayName("getSpendingForMonth returns correct total")
    void testGetSpendingForMonth() {
        LocalDate today = LocalDate.now();
        double total = dao.getSpendingForMonth(testUserId, today.getMonthValue(), today.getYear());
        assertTrue(total >= 1100.50, "Total should be at least 1100.50");
    }

    @Test
    @Order(9)
    @DisplayName("getCategoryTotalsForMonth returns data")
    void testGetCategoryTotalsForMonth() {
        LocalDate today = LocalDate.now();
        Map<String, Double> catTotals = dao.getCategoryTotalsForMonth(testUserId, today.getMonthValue(), today.getYear());
        assertNotNull(catTotals);
        assertTrue(catTotals.containsKey("Food"));
    }

    @Test
    @Order(10)
    @DisplayName("getTopExpenses returns limited results")
    void testGetTopExpenses() {
        List<Expense> top = dao.getTopExpenses(testUserId, 2);
        assertNotNull(top);
        assertTrue(top.size() <= 2, "Should return at most 2 top expenses");
        // First one should be highest amount
        if (top.size() == 2) {
            assertTrue(top.get(0).getAmount() >= top.get(1).getAmount());
        }
    }

    @Test
    @Order(11)
    @DisplayName("countExpenses returns correct count")
    void testCountExpenses() {
        int count = dao.countExpenses(testUserId);
        assertTrue(count >= 3, "Should have at least 3 expenses");
    }

    @Test
    @Order(12)
    @DisplayName("bulkAddExpenses adds multiple expenses")
    void testBulkAddExpenses() {
        List<Expense> bulk = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Expense e = new Expense();
            e.setUserId(testUserId);
            e.setTitle("Bulk Item " + i);
            e.setAmount(10.0 * (i + 1));
            e.setCategory("Misc");
            e.setDate(LocalDate.now());
            e.setDescription("Bulk test item");
            bulk.add(e);
        }
        int added = dao.bulkAddExpenses(bulk);
        assertEquals(5, added, "Should add all 5 expenses");
    }

    @Test
    @Order(13)
    @DisplayName("getFilteredExpenses filters by date range")
    void testGetFilteredExpenses() {
        String from = LocalDate.now().minusDays(1).toString();
        String to = LocalDate.now().plusDays(1).toString();
        List<Expense> filtered = dao.getFilteredExpenses(testUserId, from, to, null);
        assertNotNull(filtered);
        assertFalse(filtered.isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("getFilteredExpenses filters by date and category")
    void testGetFilteredExpensesByCategory() {
        String from = LocalDate.now().minusDays(1).toString();
        String to = LocalDate.now().plusDays(1).toString();
        List<Expense> filtered = dao.getFilteredExpenses(testUserId, from, to, "Food");
        assertNotNull(filtered);
        assertTrue(filtered.stream().allMatch(e -> "Food".equals(e.getCategory())));
    }

    @Test
    @Order(15)
    @DisplayName("getUserEmail returns correct email")
    void testGetUserEmail() {
        String email = dao.getUserEmail(testUserId);
        assertEquals("dao_test@test.com", email);
    }

    @Test
    @Order(16)
    @DisplayName("getUsername returns correct username")
    void testGetUsername() {
        String username = dao.getUsername(testUserId);
        assertEquals("dao_tester", username);
    }

    @Test
    @Order(17)
    @DisplayName("updateUserProfile updates username and email")
    void testUpdateUserProfile() {
        assertTrue(dao.updateUserProfile(testUserId, "dao_tester_updated", "dao_test@test.com"));
        assertEquals("dao_tester_updated", dao.getUsername(testUserId));
        // Revert
        dao.updateUserProfile(testUserId, "dao_tester", "dao_test@test.com");
    }

    @Test
    @Order(18)
    @DisplayName("updateUserPassword and verifyPassword work")
    void testUpdateAndVerifyPassword() {
        String newHash = "new_hashed_password_123";
        assertTrue(dao.updateUserPassword(testUserId, newHash));
        assertTrue(dao.verifyPassword(testUserId, newHash));
        assertFalse(dao.verifyPassword(testUserId, "wrong_hash"));
    }

    @Test
    @Order(19)
    @DisplayName("getUserCreatedAt returns non-null date string")
    void testGetUserCreatedAt() {
        String createdAt = dao.getUserCreatedAt(testUserId);
        assertNotNull(createdAt);
    }

    @Test
    @Order(20)
    @DisplayName("getAvgDailySpending returns non-negative value")
    void testGetAvgDailySpending() {
        double avg = dao.getAvgDailySpending(testUserId);
        assertTrue(avg >= 0);
    }

    @Test
    @Order(21)
    @DisplayName("predictNextMonthSpending returns non-negative value")
    void testPredictNextMonthSpending() {
        double prediction = dao.predictNextMonthSpending(testUserId);
        assertTrue(prediction >= 0);
    }

    @Test
    @Order(22)
    @DisplayName("getAllUsersWithEmail returns list with test user")
    void testGetAllUsersWithEmail() {
        List<Map<String, Object>> users = dao.getAllUsersWithEmail();
        assertNotNull(users);
        boolean found = users.stream()
            .anyMatch(u -> "dao_test@test.com".equals(u.get("email")));
        assertTrue(found, "Should contain our test user");
    }

    @Test
    @Order(23)
    @DisplayName("Expenses for non-existent user returns empty list")
    void testNonExistentUser() {
        List<Expense> expenses = dao.getAllExpensesByUser(-99999);
        assertNotNull(expenses);
        assertTrue(expenses.isEmpty());
    }
}
