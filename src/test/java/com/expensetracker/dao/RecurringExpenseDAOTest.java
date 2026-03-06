package com.expensetracker.dao;

import com.expensetracker.model.RecurringExpense;
import com.expensetracker.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecurringExpenseDAO Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecurringExpenseDAOTest {

    private static RecurringExpenseDAO dao;
    private static int testUserId;

    @BeforeAll
    static void setUp() throws Exception {
        dao = new RecurringExpenseDAO();
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM users WHERE email = 'recurring_test@test.com'").executeUpdate();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES ('recurring_tester', 'recurring_test@test.com', 'hash') RETURNING id");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testUserId = rs.getInt("id");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM recurring_expenses WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM expenses WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Add recurring expense successfully")
    void testAdd() {
        RecurringExpense re = new RecurringExpense();
        re.setUserId(testUserId);
        re.setTitle("Netflix");
        re.setCategory("Entertainment");
        re.setAmount(649.0);
        re.setFrequency("monthly");
        re.setNextDue(LocalDate.now().plusDays(5));
        re.setDescription("Streaming subscription");
        re.setActive(true);

        assertTrue(dao.add(re));
    }

    @Test
    @Order(2)
    @DisplayName("getAllByUser returns recurring expenses")
    void testGetAllByUser() {
        List<RecurringExpense> list = dao.getAllByUser(testUserId);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Netflix", list.get(0).getTitle());
        assertEquals("monthly", list.get(0).getFrequency());
        assertTrue(list.get(0).isActive());
    }

    @Test
    @Order(3)
    @DisplayName("Update recurring expense")
    void testUpdate() {
        List<RecurringExpense> list = dao.getAllByUser(testUserId);
        RecurringExpense re = list.get(0);
        re.setAmount(799.0);
        re.setTitle("Netflix Premium");

        assertTrue(dao.update(re));
        
        List<RecurringExpense> updated = dao.getAllByUser(testUserId);
        assertEquals(799.0, updated.get(0).getAmount(), 0.001);
        assertEquals("Netflix Premium", updated.get(0).getTitle());
    }

    @Test
    @Order(4)
    @DisplayName("toggleActive flips active status")
    void testToggleActive() {
        List<RecurringExpense> list = dao.getAllByUser(testUserId);
        int id = list.get(0).getId();
        boolean waActive = list.get(0).isActive();

        assertTrue(dao.toggleActive(id, testUserId));

        List<RecurringExpense> toggled = dao.getAllByUser(testUserId);
        assertNotEquals(waActive, toggled.get(0).isActive());

        // Toggle back
        dao.toggleActive(id, testUserId);
    }

    @Test
    @Order(5)
    @DisplayName("getMonthlyRecurringTotal returns active expense total")
    void testGetMonthlyRecurringTotal() {
        double total = dao.getMonthlyRecurringTotal(testUserId);
        assertEquals(799.0, total, 0.001);
    }

    @Test
    @Order(6)
    @DisplayName("Add second recurring expense with past due date")
    void testAddPastDue() {
        RecurringExpense re = new RecurringExpense();
        re.setUserId(testUserId);
        re.setTitle("Gym Membership");
        re.setCategory("Health");
        re.setAmount(2000.0);
        re.setFrequency("monthly");
        re.setNextDue(LocalDate.now().minusDays(1)); // Past due
        re.setDescription("Monthly gym");
        re.setActive(true);

        assertTrue(dao.add(re));
    }

    @Test
    @Order(7)
    @DisplayName("processDueExpenses creates expenses for past-due items")
    void testProcessDueExpenses() {
        int processed = dao.processDueExpenses(testUserId);
        assertTrue(processed >= 1, "Should process at least 1 due expense");

        // Verify actual expense was created
        ExpenseDAO expenseDAO = new ExpenseDAO();
        List<com.expensetracker.model.Expense> expenses = expenseDAO.getAllExpensesByUser(testUserId);
        boolean found = expenses.stream().anyMatch(e -> e.getTitle().contains("Gym Membership"));
        assertTrue(found, "Should have created an expense from recurring");
    }

    @Test
    @Order(8)
    @DisplayName("Delete recurring expense")
    void testDelete() {
        List<RecurringExpense> list = dao.getAllByUser(testUserId);
        int initialSize = list.size();
        int id = list.get(list.size() - 1).getId();

        assertTrue(dao.delete(id, testUserId));
        assertEquals(initialSize - 1, dao.getAllByUser(testUserId).size());
    }

    @Test
    @Order(9)
    @DisplayName("Non-existent user returns empty")
    void testNonExistentUser() {
        assertTrue(dao.getAllByUser(-99999).isEmpty());
        assertEquals(0.0, dao.getMonthlyRecurringTotal(-99999));
    }
}
