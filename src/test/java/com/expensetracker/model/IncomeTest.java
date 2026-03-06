package com.expensetracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Income Model Tests")
class IncomeTest {

    @Test
    @DisplayName("Default constructor creates empty Income")
    void testDefaultConstructor() {
        Income i = new Income();
        assertEquals(0, i.getId());
        assertEquals(0, i.getUserId());
        assertNull(i.getSource());
        assertEquals(0.0, i.getAmount());
        assertNull(i.getDate());
        assertFalse(i.isRecurring());
        assertNull(i.getNotes());
    }

    @Test
    @DisplayName("Full-args constructor sets all fields")
    void testFullConstructor() {
        LocalDate date = LocalDate.of(2024, 5, 1);
        Income i = new Income(1, 100, "Salary", 50000.0, date, true, "Monthly salary");
        
        assertEquals(1, i.getId());
        assertEquals(100, i.getUserId());
        assertEquals("Salary", i.getSource());
        assertEquals(50000.0, i.getAmount(), 0.001);
        assertEquals(date, i.getDate());
        assertTrue(i.isRecurring());
        assertEquals("Monthly salary", i.getNotes());
    }

    @Test
    @DisplayName("Getters and setters work correctly")
    void testGettersSetters() {
        Income i = new Income();
        
        i.setId(5);
        assertEquals(5, i.getId());

        i.setUserId(42);
        assertEquals(42, i.getUserId());

        i.setSource("Freelance");
        assertEquals("Freelance", i.getSource());

        i.setAmount(1500.50);
        assertEquals(1500.50, i.getAmount(), 0.001);

        LocalDate date = LocalDate.of(2024, 7, 15);
        i.setDate(date);
        assertEquals(date, i.getDate());

        i.setRecurring(true);
        assertTrue(i.isRecurring());

        i.setNotes("Contract work");
        assertEquals("Contract work", i.getNotes());
    }
}
