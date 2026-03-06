package com.expensetracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SavingsGoal Model Tests")
class SavingsGoalTest {

    @Test
    @DisplayName("Default constructor sets icon & color defaults")
    void testDefaultConstructor() {
        SavingsGoal g = new SavingsGoal();
        assertEquals("fa-bullseye", g.getIcon());
        assertEquals("#4f46e5", g.getColor());
        assertEquals(0.0, g.getTargetAmount());
        assertEquals(0.0, g.getSavedAmount());
    }

    @Test
    @DisplayName("getProgressPercent calculates correctly")
    void testProgressPercent() {
        SavingsGoal g = new SavingsGoal();
        g.setTargetAmount(10000);
        g.setSavedAmount(2500);
        assertEquals(25.0, g.getProgressPercent(), 0.001);
    }

    @Test
    @DisplayName("getProgressPercent caps at 100%")
    void testProgressPercentCapped() {
        SavingsGoal g = new SavingsGoal();
        g.setTargetAmount(1000);
        g.setSavedAmount(1500);  // Over-saved
        assertEquals(100.0, g.getProgressPercent(), 0.001);
    }

    @Test
    @DisplayName("getProgressPercent with zero target returns 0")
    void testProgressPercentZeroTarget() {
        SavingsGoal g = new SavingsGoal();
        g.setTargetAmount(0);
        g.setSavedAmount(500);
        assertEquals(0.0, g.getProgressPercent(), 0.001);
    }

    @Test
    @DisplayName("getRemainingAmount calculates correctly")
    void testRemainingAmount() {
        SavingsGoal g = new SavingsGoal();
        g.setTargetAmount(10000);
        g.setSavedAmount(3000);
        assertEquals(7000.0, g.getRemainingAmount(), 0.001);
    }

    @Test
    @DisplayName("getRemainingAmount returns 0 when fully saved")
    void testRemainingAmountFullySaved() {
        SavingsGoal g = new SavingsGoal();
        g.setTargetAmount(5000);
        g.setSavedAmount(5000);
        assertEquals(0.0, g.getRemainingAmount(), 0.001);
    }

    @Test
    @DisplayName("getRemainingAmount returns 0 when over-saved")
    void testRemainingAmountOverSaved() {
        SavingsGoal g = new SavingsGoal();
        g.setTargetAmount(5000);
        g.setSavedAmount(7000);
        assertEquals(0.0, g.getRemainingAmount(), 0.001);
    }

    @Test
    @DisplayName("All getters and setters work")
    void testGettersSetters() {
        SavingsGoal g = new SavingsGoal();
        
        g.setId(1);
        assertEquals(1, g.getId());

        g.setUserId(42);
        assertEquals(42, g.getUserId());

        g.setName("Vacation Fund");
        assertEquals("Vacation Fund", g.getName());

        g.setTargetAmount(50000);
        assertEquals(50000, g.getTargetAmount(), 0.001);

        g.setSavedAmount(12000);
        assertEquals(12000, g.getSavedAmount(), 0.001);

        LocalDate deadline = LocalDate.of(2025, 12, 31);
        g.setDeadline(deadline);
        assertEquals(deadline, g.getDeadline());

        g.setIcon("fa-plane");
        assertEquals("fa-plane", g.getIcon());

        g.setColor("#ff6600");
        assertEquals("#ff6600", g.getColor());

        LocalDateTime now = LocalDateTime.now();
        g.setCreatedAt(now);
        assertEquals(now, g.getCreatedAt());
    }
}
