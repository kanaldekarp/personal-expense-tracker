package com.expensetracker.dao;

import com.expensetracker.model.Tag;
import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TagDAO Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagDAOTest {

    private static TagDAO dao;
    private static ExpenseDAO expenseDAO;
    private static int testUserId;
    private static int testExpenseId;
    private static int testTagId;

    @BeforeAll
    static void setUp() throws Exception {
        dao = new TagDAO();
        expenseDAO = new ExpenseDAO();
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM users WHERE email = 'tag_test@test.com'").executeUpdate();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, email, password) VALUES ('tag_tester', 'tag_test@test.com', 'hash') RETURNING id");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testUserId = rs.getInt("id");

            // Create a test expense
            PreparedStatement expPs = con.prepareStatement(
                "INSERT INTO expenses (user_id, title, amount, category, date) VALUES (?, 'Tag Test Expense', 100, 'Test', CURRENT_DATE) RETURNING id");
            expPs.setInt(1, testUserId);
            ResultSet expRs = expPs.executeQuery();
            if (expRs.next()) testExpenseId = expRs.getInt("id");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.prepareStatement("DELETE FROM expense_tags WHERE expense_id = " + testExpenseId).executeUpdate();
            con.prepareStatement("DELETE FROM tags WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM expenses WHERE user_id = " + testUserId).executeUpdate();
            con.prepareStatement("DELETE FROM users WHERE id = " + testUserId).executeUpdate();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Add tag successfully")
    void testAddTag() {
        Tag tag = new Tag(0, testUserId, "Urgent", "#ff0000");
        assertTrue(dao.addTag(tag));
    }

    @Test
    @Order(2)
    @DisplayName("getAllByUser returns the added tag")
    void testGetAllByUser() {
        List<Tag> tags = dao.getAllByUser(testUserId);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("Urgent", tags.get(0).getName());
        assertEquals("#ff0000", tags.get(0).getColor());
        testTagId = tags.get(0).getId();
    }

    @Test
    @Order(3)
    @DisplayName("Duplicate tag is ignored (ON CONFLICT DO NOTHING)")
    void testDuplicateTag() {
        Tag dup = new Tag(0, testUserId, "Urgent", "#ff0000");
        // addTag returns false because ON CONFLICT DO NOTHING -> executeUpdate() returns 0
        assertFalse(dao.addTag(dup));
        // Still only one tag
        assertEquals(1, dao.getAllByUser(testUserId).size());
    }

    @Test
    @Order(4)
    @DisplayName("Add another tag")
    void testAddSecondTag() {
        Tag tag = new Tag(0, testUserId, "Business", "#00ff00");
        assertTrue(dao.addTag(tag));
        assertEquals(2, dao.getAllByUser(testUserId).size());
    }

    @Test
    @Order(5)
    @DisplayName("tagExpense links tag to expense")
    void testTagExpense() {
        dao.tagExpense(testExpenseId, testTagId);
        List<Tag> tags = dao.getTagsForExpense(testExpenseId);
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("Urgent", tags.get(0).getName());
    }

    @Test
    @Order(6)
    @DisplayName("getTagsForExpense returns all linked tags")
    void testMultipleTags() {
        List<Tag> allTags = dao.getAllByUser(testUserId);
        int secondTagId = allTags.stream()
            .filter(t -> "Business".equals(t.getName()))
            .findFirst().get().getId();
        
        dao.tagExpense(testExpenseId, secondTagId);
        
        List<Tag> expenseTags = dao.getTagsForExpense(testExpenseId);
        assertEquals(2, expenseTags.size());
    }

    @Test
    @Order(7)
    @DisplayName("deleteTag removes tag")
    void testDeleteTag() {
        List<Tag> tags = dao.getAllByUser(testUserId);
        int id = tags.get(tags.size() - 1).getId();
        assertTrue(dao.deleteTag(id, testUserId));
    }

    @Test
    @Order(8)
    @DisplayName("Non-existent user returns empty")
    void testNonExistentUser() {
        assertTrue(dao.getAllByUser(-99999).isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("getTagsForExpense for non-existent expense returns empty")
    void testNonExistentExpense() {
        assertTrue(dao.getTagsForExpense(-99999).isEmpty());
    }
}
