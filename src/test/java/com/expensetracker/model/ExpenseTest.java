package com.expensetracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Expense Model Tests")
class ExpenseTest {

    @Test
    @DisplayName("Default constructor creates empty Expense")
    void testDefaultConstructor() {
        Expense e = new Expense();
        assertEquals(0, e.getId());
        assertEquals(0, e.getUserId());
        assertNull(e.getTitle());
        assertEquals(0.0, e.getAmount());
        assertNull(e.getCategory());
        assertNull(e.getDate());
        assertNull(e.getDescription());
        assertNull(e.getTags());
    }

    @Test
    @DisplayName("Parameterized constructor sets fields correctly")
    void testParameterizedConstructor() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        Expense e = new Expense(1, "Groceries", 150.50, "Food", date, "Weekly shopping");
        
        assertEquals(1, e.getId());
        assertEquals("Groceries", e.getTitle());
        assertEquals(150.50, e.getAmount(), 0.001);
        assertEquals("Food", e.getCategory());
        assertEquals(date, e.getDate());
        assertEquals("Weekly shopping", e.getDescription());
    }

    @Test
    @DisplayName("Getters and setters work correctly")
    void testGettersSetters() {
        Expense e = new Expense();
        
        e.setId(10);
        assertEquals(10, e.getId());

        e.setUserId(5);
        assertEquals(5, e.getUserId());

        e.setTitle("Electric Bill");
        assertEquals("Electric Bill", e.getTitle());

        e.setAmount(2500.75);
        assertEquals(2500.75, e.getAmount(), 0.001);

        e.setCategory("Utilities");
        assertEquals("Utilities", e.getCategory());

        LocalDate date = LocalDate.of(2024, 3, 10);
        e.setDate(date);
        assertEquals(date, e.getDate());

        e.setDescription("March electricity bill");
        assertEquals("March electricity bill", e.getDescription());

        e.setTags("urgent,home");
        assertEquals("urgent,home", e.getTags());
    }

    @Test
    @DisplayName("Amount can be zero")
    void testZeroAmount() {
        Expense e = new Expense();
        e.setAmount(0);
        assertEquals(0.0, e.getAmount());
    }

    @Test
    @DisplayName("Negative amount is allowed (model has no validation)")
    void testNegativeAmount() {
        Expense e = new Expense();
        e.setAmount(-100.0);
        assertEquals(-100.0, e.getAmount());
    }
}
