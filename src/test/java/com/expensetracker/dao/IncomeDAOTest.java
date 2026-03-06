package com.expensetracker.dao;

import com.expensetracker.model.Income;
import com.expensetracker.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IncomeDAO Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IncomeDAOTest {

    private static IncomeDAO dao;
    private static int testUserId;
    private static int addedIncomeId;

    @BeforeAll
    static void setUp() throws Exception {
        dao = new IncomeDAO();
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM users WHERE email = 'income_test@test.com'").executeUpdate();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES ('income_tester', 'income_test@test.com', 'hash') RETURNING id");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testUserId = rs.getInt("id");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM income WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Add income successfully")
    void testAddIncome() {
        Income income = new Income();
        income.setUserId(testUserId);
        income.setSource("Salary");
        income.setAmount(50000.0);
        income.setDate(LocalDate.now());
        income.setRecurring(true);
        income.setNotes("Monthly salary");

        assertTrue(dao.addIncome(income));
    }

    @Test
    @Order(2)
    @DisplayName("getAllByUser returns added income")
    void testGetAllByUser() {
        List<Income> list = dao.getAllByUser(testUserId);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Salary", list.get(0).getSource());
        assertEquals(50000.0, list.get(0).getAmount(), 0.001);
        assertTrue(list.get(0).isRecurring());
        addedIncomeId = list.get(0).getId();
    }

    @Test
    @Order(3)
    @DisplayName("Update income successfully")
    void testUpdateIncome() {
        List<Income> list = dao.getAllByUser(testUserId);
        Income income = list.get(0);
        income.setAmount(55000.0);
        income.setNotes("Updated salary");

        assertTrue(dao.updateIncome(income));
        
        List<Income> updated = dao.getAllByUser(testUserId);
        assertEquals(55000.0, updated.get(0).getAmount(), 0.001);
        assertEquals("Updated salary", updated.get(0).getNotes());
    }

    @Test
    @Order(4)
    @DisplayName("Add more income for aggregation tests")
    void testAddMoreIncome() {
        Income i2 = new Income();
        i2.setUserId(testUserId);
        i2.setSource("Freelance");
        i2.setAmount(15000.0);
        i2.setDate(LocalDate.now());
        i2.setRecurring(false);
        i2.setNotes("Project work");

        assertTrue(dao.addIncome(i2));
    }

    @Test
    @Order(5)
    @DisplayName("getTotalIncome sums all income")
    void testGetTotalIncome() {
        double total = dao.getTotalIncome(testUserId);
        assertEquals(70000.0, total, 0.01);
    }

    @Test
    @Order(6)
    @DisplayName("getTotalIncomeByMonth returns correct monthly total")
    void testGetTotalIncomeByMonth() {
        LocalDate today = LocalDate.now();
        double monthTotal = dao.getTotalIncomeByMonth(testUserId, today.getMonthValue(), today.getYear());
        assertEquals(70000.0, monthTotal, 0.01);
    }

    @Test
    @Order(7)
    @DisplayName("getByMonth returns income for current month")
    void testGetByMonth() {
        LocalDate today = LocalDate.now();
        List<Income> list = dao.getByMonth(testUserId, today.getMonthValue(), today.getYear());
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    @Order(8)
    @DisplayName("deleteIncome removes income")
    void testDeleteIncome() {
        List<Income> list = dao.getAllByUser(testUserId);
        int id = list.get(list.size() - 1).getId();
        assertTrue(dao.deleteIncome(id, testUserId));
        assertEquals(list.size() - 1, dao.getAllByUser(testUserId).size());
    }

    @Test
    @Order(9)
    @DisplayName("Non-existent user returns empty list and zero total")
    void testNonExistentUser() {
        List<Income> list = dao.getAllByUser(-99999);
        assertTrue(list.isEmpty());
        assertEquals(0.0, dao.getTotalIncome(-99999));
    }
}
