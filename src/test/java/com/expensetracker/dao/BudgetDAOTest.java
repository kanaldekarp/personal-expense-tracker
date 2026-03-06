package com.expensetracker.dao;

import com.expensetracker.model.Budget;
import com.expensetracker.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BudgetDAO Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BudgetDAOTest {

    private static BudgetDAO dao;
    private static int testUserId;
    private static final int TEST_MONTH = 1;  // January - unlikely to conflict
    private static final int TEST_YEAR = 2099; // Far future year for isolation

    @BeforeAll
    static void setUp() throws Exception {
        dao = new BudgetDAO();
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM users WHERE email = 'budget_test@test.com'").executeUpdate();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES ('budget_tester', 'budget_test@test.com', 'hash') RETURNING id");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testUserId = rs.getInt("id");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM budgets WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Save budget successfully")
    void testSaveBudget() {
        Budget b = new Budget(testUserId, "Food", 5000.0, TEST_MONTH, TEST_YEAR);
        assertTrue(dao.saveBudget(b));
    }

    @Test
    @Order(2)
    @DisplayName("Save budget with upsert updates existing")
    void testSaveBudgetUpsert() {
        Budget b = new Budget(testUserId, "Food", 7000.0, TEST_MONTH, TEST_YEAR);
        assertTrue(dao.saveBudget(b));

        Map<String, Double> map = dao.getBudgetMap(testUserId, TEST_MONTH, TEST_YEAR);
        assertEquals(7000.0, map.get("Food"), 0.001);
    }

    @Test
    @Order(3)
    @DisplayName("Save multiple category budgets")
    void testSaveMultipleBudgets() {
        dao.saveBudget(new Budget(testUserId, "Transport", 3000.0, TEST_MONTH, TEST_YEAR));
        dao.saveBudget(new Budget(testUserId, "Entertainment", 2000.0, TEST_MONTH, TEST_YEAR));
    }

    @Test
    @Order(4)
    @DisplayName("getBudgetsByMonth returns all category budgets")
    void testGetBudgetsByMonth() {
        List<Budget> budgets = dao.getBudgetsByMonth(testUserId, TEST_MONTH, TEST_YEAR);
        assertNotNull(budgets);
        assertEquals(3, budgets.size());
    }

    @Test
    @Order(5)
    @DisplayName("getBudgetMap returns correct map")
    void testGetBudgetMap() {
        Map<String, Double> map = dao.getBudgetMap(testUserId, TEST_MONTH, TEST_YEAR);
        assertEquals(3, map.size());
        assertEquals(7000.0, map.get("Food"), 0.001);
        assertEquals(3000.0, map.get("Transport"), 0.001);
        assertEquals(2000.0, map.get("Entertainment"), 0.001);
    }

    @Test
    @Order(6)
    @DisplayName("getCategoryBudgetSum returns correct sum")
    void testGetCategoryBudgetSum() {
        double sum = dao.getCategoryBudgetSum(testUserId, TEST_MONTH, TEST_YEAR);
        assertEquals(12000.0, sum, 0.001);
    }

    @Test
    @Order(7)
    @DisplayName("getOverallBudget returns null when not set")
    void testGetOverallBudgetNull() {
        Budget overall = dao.getOverallBudget(testUserId, TEST_MONTH, TEST_YEAR);
        assertNull(overall, "No overall budget set yet");
    }

    @Test
    @Order(8)
    @DisplayName("getTotalBudget falls back to category sum when no overall")
    void testGetTotalBudgetFallback() {
        double total = dao.getTotalBudget(testUserId, TEST_MONTH, TEST_YEAR);
        assertEquals(12000.0, total, 0.001);
    }

    @Test
    @Order(9)
    @DisplayName("Save overall budget (__TOTAL__)")
    void testSaveOverallBudget() {
        Budget b = new Budget(testUserId, BudgetDAO.TOTAL_CATEGORY, 20000.0, TEST_MONTH, TEST_YEAR);
        assertTrue(dao.saveBudget(b));
    }

    @Test
    @Order(10)
    @DisplayName("getOverallBudget returns __TOTAL__ budget")
    void testGetOverallBudget() {
        Budget overall = dao.getOverallBudget(testUserId, TEST_MONTH, TEST_YEAR);
        assertNotNull(overall);
        assertEquals(20000.0, overall.getBudgetAmount(), 0.001);
        assertEquals(BudgetDAO.TOTAL_CATEGORY, overall.getCategory());
    }

    @Test
    @Order(11)
    @DisplayName("getTotalBudget uses overall when set")
    void testGetTotalBudgetWithOverall() {
        double total = dao.getTotalBudget(testUserId, TEST_MONTH, TEST_YEAR);
        assertEquals(20000.0, total, 0.001);
    }

    @Test
    @Order(12)
    @DisplayName("copyBudgets copies to a new month")
    void testCopyBudgets() {
        int copied = dao.copyBudgets(testUserId, TEST_MONTH, TEST_YEAR, 2, TEST_YEAR);
        assertEquals(3, copied, "Should copy 3 category budgets (not __TOTAL__)");

        List<Budget> febBudgets = dao.getBudgetsByMonth(testUserId, 2, TEST_YEAR);
        assertEquals(3, febBudgets.size());
        // Clean up the copied budgets
        for (Budget b : febBudgets) {
            dao.deleteBudget(b.getId(), testUserId);
        }
    }

    @Test
    @Order(13)
    @DisplayName("deleteBudget removes specific budget")
    void testDeleteBudget() {
        List<Budget> budgets = dao.getBudgetsByMonth(testUserId, TEST_MONTH, TEST_YEAR);
        int initialSize = budgets.size();
        int id = budgets.get(budgets.size() - 1).getId();

        assertTrue(dao.deleteBudget(id, testUserId));
        assertEquals(initialSize - 1, dao.getBudgetsByMonth(testUserId, TEST_MONTH, TEST_YEAR).size());
    }

    @Test
    @Order(14)
    @DisplayName("Non-existent user returns empty and zero")
    void testNonExistentUser() {
        assertTrue(dao.getBudgetsByMonth(-99999, 1, 2024).isEmpty());
        assertEquals(0.0, dao.getTotalBudget(-99999, 1, 2024));
        assertEquals(0.0, dao.getCategoryBudgetSum(-99999, 1, 2024));
    }
}
