package com.expensetracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Budget Model Tests")
class BudgetTest {

    @Test
    @DisplayName("Default constructor creates empty Budget")
    void testDefaultConstructor() {
        Budget b = new Budget();
        assertEquals(0, b.getId());
        assertEquals(0, b.getUserId());
        assertNull(b.getCategory());
        assertEquals(0.0, b.getBudgetAmount());
        assertEquals(0, b.getMonth());
        assertEquals(0, b.getYear());
    }

    @Test
    @DisplayName("Parameterized constructor sets all fields")
    void testParameterizedConstructor() {
        Budget b = new Budget(10, "Food", 5000.0, 6, 2024);
        assertEquals(10, b.getUserId());
        assertEquals("Food", b.getCategory());
        assertEquals(5000.0, b.getBudgetAmount(), 0.001);
        assertEquals(6, b.getMonth());
        assertEquals(2024, b.getYear());
    }

    @Test
    @DisplayName("Getters and setters work correctly")
    void testGettersSetters() {
        Budget b = new Budget();
        
        b.setId(1);
        assertEquals(1, b.getId());

        b.setUserId(42);
        assertEquals(42, b.getUserId());

        b.setCategory("Transport");
        assertEquals("Transport", b.getCategory());

        b.setBudgetAmount(3000.0);
        assertEquals(3000.0, b.getBudgetAmount(), 0.001);

        b.setMonth(12);
        assertEquals(12, b.getMonth());

        b.setYear(2025);
        assertEquals(2025, b.getYear());
    }

    @Test
    @DisplayName("Budget can represent overall total category")
    void testTotalCategory() {
        Budget b = new Budget(1, "__TOTAL__", 50000.0, 6, 2024);
        assertEquals("__TOTAL__", b.getCategory());
        assertEquals(50000.0, b.getBudgetAmount(), 0.001);
    }
}
