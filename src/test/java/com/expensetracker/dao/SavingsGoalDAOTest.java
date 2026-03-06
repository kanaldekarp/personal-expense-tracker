package com.expensetracker.dao;

import com.expensetracker.model.SavingsGoal;
import com.expensetracker.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SavingsGoalDAO Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SavingsGoalDAOTest {

    private static SavingsGoalDAO dao;
    private static int testUserId;

    @BeforeAll
    static void setUp() throws Exception {
        dao = new SavingsGoalDAO();
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM users WHERE email = 'goals_test@test.com'").executeUpdate();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES ('goals_tester', 'goals_test@test.com', 'hash') RETURNING id");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testUserId = rs.getInt("id");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM savings_goals WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Add savings goal successfully")
    void testAdd() {
        SavingsGoal g = new SavingsGoal();
        g.setUserId(testUserId);
        g.setName("Vacation Fund");
        g.setTargetAmount(100000);
        g.setSavedAmount(0);
        g.setDeadline(LocalDate.of(2025, 12, 31));
        g.setIcon("fa-plane");
        g.setColor("#ff6600");

        assertTrue(dao.add(g));
    }

    @Test
    @Order(2)
    @DisplayName("getAllByUser returns the added goal")
    void testGetAllByUser() {
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        assertNotNull(goals);
        assertEquals(1, goals.size());
        assertEquals("Vacation Fund", goals.get(0).getName());
        assertEquals(100000, goals.get(0).getTargetAmount(), 0.001);
        assertEquals(0.0, goals.get(0).getProgressPercent(), 0.001);
    }

    @Test
    @Order(3)
    @DisplayName("getById returns the correct goal")
    void testGetById() {
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        int id = goals.get(0).getId();
        SavingsGoal g = dao.getById(id, testUserId);
        assertNotNull(g);
        assertEquals("Vacation Fund", g.getName());
        assertEquals("fa-plane", g.getIcon());
        assertEquals("#ff6600", g.getColor());
    }

    @Test
    @Order(4)
    @DisplayName("addSavedAmount increases saved amount")
    void testAddSavedAmount() {
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        int id = goals.get(0).getId();

        assertTrue(dao.addSavedAmount(id, testUserId, 25000));
        
        SavingsGoal updated = dao.getById(id, testUserId);
        assertEquals(25000, updated.getSavedAmount(), 0.001);
        assertEquals(25.0, updated.getProgressPercent(), 0.001);
    }

    @Test
    @Order(5)
    @DisplayName("addSavedAmount accumulates over multiple calls")
    void testAddSavedAmountCumulative() {
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        int id = goals.get(0).getId();

        dao.addSavedAmount(id, testUserId, 15000);
        
        SavingsGoal updated = dao.getById(id, testUserId);
        assertEquals(40000, updated.getSavedAmount(), 0.001);
        assertEquals(40.0, updated.getProgressPercent(), 0.001);
        assertEquals(60000, updated.getRemainingAmount(), 0.001);
    }

    @Test
    @Order(6)
    @DisplayName("Update savings goal")
    void testUpdate() {
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        SavingsGoal g = goals.get(0);
        g.setName("Dream Vacation");
        g.setTargetAmount(150000);

        assertTrue(dao.update(g));

        SavingsGoal updated = dao.getById(g.getId(), testUserId);
        assertEquals("Dream Vacation", updated.getName());
        assertEquals(150000, updated.getTargetAmount(), 0.001);
    }

    @Test
    @Order(7)
    @DisplayName("Add goal with null deadline")
    void testAddNullDeadline() {
        SavingsGoal g = new SavingsGoal();
        g.setUserId(testUserId);
        g.setName("Emergency Fund");
        g.setTargetAmount(50000);
        g.setSavedAmount(5000);
        // no deadline set

        assertTrue(dao.add(g));
        
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        SavingsGoal emergencyGoal = goals.stream()
            .filter(goal -> "Emergency Fund".equals(goal.getName()))
            .findFirst().orElse(null);
        assertNotNull(emergencyGoal);
        assertNull(emergencyGoal.getDeadline());
    }

    @Test
    @Order(8)
    @DisplayName("Delete savings goal")
    void testDelete() {
        List<SavingsGoal> goals = dao.getAllByUser(testUserId);
        int initialSize = goals.size();
        int id = goals.get(goals.size() - 1).getId();

        assertTrue(dao.delete(id, testUserId));
        assertEquals(initialSize - 1, dao.getAllByUser(testUserId).size());
    }

    @Test
    @Order(9)
    @DisplayName("getById for non-existent goal returns null")
    void testGetByIdNotFound() {
        assertNull(dao.getById(-99999, testUserId));
    }

    @Test
    @Order(10)
    @DisplayName("Non-existent user returns empty list")
    void testNonExistentUser() {
        assertTrue(dao.getAllByUser(-99999).isEmpty());
    }
}
