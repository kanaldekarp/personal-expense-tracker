package com.expensetracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecurringExpense Model Tests")
class RecurringExpenseTest {

    @Test
    @DisplayName("Default constructor sets active=true")
    void testDefaultConstructor() {
        RecurringExpense re = new RecurringExpense();
        assertTrue(re.isActive(), "RecurringExpense should default to active=true");
        assertEquals(0, re.getId());
        assertNull(re.getTitle());
    }

    @Test
    @DisplayName("All getters and setters work correctly")
    void testGettersSetters() {
        RecurringExpense re = new RecurringExpense();
        
        re.setId(1);
        assertEquals(1, re.getId());

        re.setUserId(10);
        assertEquals(10, re.getUserId());

        re.setTitle("Netflix Subscription");
        assertEquals("Netflix Subscription", re.getTitle());

        re.setCategory("Entertainment");
        assertEquals("Entertainment", re.getCategory());

        re.setAmount(649.0);
        assertEquals(649.0, re.getAmount(), 0.001);

        re.setFrequency("monthly");
        assertEquals("monthly", re.getFrequency());

        LocalDate nextDue = LocalDate.of(2024, 8, 1);
        re.setNextDue(nextDue);
        assertEquals(nextDue, re.getNextDue());

        re.setDescription("Monthly streaming");
        assertEquals("Monthly streaming", re.getDescription());

        re.setActive(false);
        assertFalse(re.isActive());
    }

    @Test
    @DisplayName("Frequency can be daily, weekly, monthly, yearly")
    void testFrequencyValues() {
        RecurringExpense re = new RecurringExpense();
        
        for (String freq : new String[]{"daily", "weekly", "monthly", "yearly"}) {
            re.setFrequency(freq);
            assertEquals(freq, re.getFrequency());
        }
    }
}
