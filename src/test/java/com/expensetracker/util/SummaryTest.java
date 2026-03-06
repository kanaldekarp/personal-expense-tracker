package com.expensetracker.util;

import com.expensetracker.model.Expense;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Summary Utility Tests")
class SummaryTest {

    private Expense createExpense(String title, double amount, String category) {
        Expense e = new Expense();
        e.setTitle(title);
        e.setAmount(amount);
        e.setCategory(category);
        e.setDate(LocalDate.now());
        return e;
    }

    // ===== getTotalAmount =====

    @Test
    @DisplayName("getTotalAmount sums all expenses correctly")
    void testGetTotalAmount() {
        List<Expense> expenses = Arrays.asList(
            createExpense("A", 100.50, "Food"),
            createExpense("B", 200.75, "Transport"),
            createExpense("C", 50.25, "Food")
        );
        assertEquals(351.50, Summary.getTotalAmount(expenses), 0.001);
    }

    @Test
    @DisplayName("getTotalAmount with null list returns 0")
    void testGetTotalAmountNull() {
        assertEquals(0.0, Summary.getTotalAmount(null));
    }

    @Test
    @DisplayName("getTotalAmount with empty list returns 0")
    void testGetTotalAmountEmpty() {
        assertEquals(0.0, Summary.getTotalAmount(Collections.emptyList()));
    }

    @Test
    @DisplayName("getTotalAmount with single expense returns that amount")
    void testGetTotalAmountSingle() {
        List<Expense> expenses = Collections.singletonList(createExpense("A", 999.99, "Food"));
        assertEquals(999.99, Summary.getTotalAmount(expenses), 0.001);
    }

    // ===== getTopCategory =====

    @Test
    @DisplayName("getTopCategory returns category with highest total")
    void testGetTopCategory() {
        List<Expense> expenses = Arrays.asList(
            createExpense("A", 500, "Food"),
            createExpense("B", 200, "Transport"),
            createExpense("C", 300, "Food"),
            createExpense("D", 100, "Transport")
        );
        assertEquals("Food", Summary.getTopCategory(expenses));
    }

    @Test
    @DisplayName("getTopCategory with null list returns '-'")
    void testGetTopCategoryNull() {
        assertEquals("-", Summary.getTopCategory(null));
    }

    @Test
    @DisplayName("getTopCategory with empty list returns '-'")
    void testGetTopCategoryEmpty() {
        assertEquals("-", Summary.getTopCategory(Collections.emptyList()));
    }

    @Test
    @DisplayName("getTopCategory with single category returns that category")
    void testGetTopCategorySingle() {
        List<Expense> expenses = Collections.singletonList(createExpense("A", 100, "Bills"));
        assertEquals("Bills", Summary.getTopCategory(expenses));
    }

    // ===== getExpenseCount =====

    @Test
    @DisplayName("getExpenseCount returns correct count")
    void testGetExpenseCount() {
        List<Expense> expenses = Arrays.asList(
            createExpense("A", 100, "Food"),
            createExpense("B", 200, "Transport"),
            createExpense("C", 300, "Food")
        );
        assertEquals(3, Summary.getExpenseCount(expenses));
    }

    @Test
    @DisplayName("getExpenseCount with null returns 0")
    void testGetExpenseCountNull() {
        assertEquals(0, Summary.getExpenseCount(null));
    }

    @Test
    @DisplayName("getExpenseCount with empty list returns 0")
    void testGetExpenseCountEmpty() {
        assertEquals(0, Summary.getExpenseCount(Collections.emptyList()));
    }

    // ===== getRecentExpense =====

    @Test
    @DisplayName("getRecentExpense returns last expense with default currency")
    void testGetRecentExpense() {
        List<Expense> expenses = Arrays.asList(
            createExpense("Groceries", 500, "Food"),
            createExpense("Taxi", 200, "Transport")
        );
        String result = Summary.getRecentExpense(expenses);
        assertTrue(result.startsWith("Taxi - "));
        assertTrue(result.contains("200"));
    }

    @Test
    @DisplayName("getRecentExpense with custom currency symbol")
    void testGetRecentExpenseWithCurrency() {
        List<Expense> expenses = Collections.singletonList(createExpense("Coffee", 5.50, "Food"));
        String result = Summary.getRecentExpense(expenses, "$");
        assertTrue(result.startsWith("Coffee - $"));
    }

    @Test
    @DisplayName("getRecentExpense with null list returns N/A")
    void testGetRecentExpenseNull() {
        assertEquals("N/A", Summary.getRecentExpense(null));
    }

    @Test
    @DisplayName("getRecentExpense with empty list returns N/A")
    void testGetRecentExpenseEmpty() {
        assertEquals("N/A", Summary.getRecentExpense(Collections.emptyList()));
    }

    @Test
    @DisplayName("getRecentExpense with null currency uses default ₹")
    void testGetRecentExpenseNullCurrency() {
        List<Expense> expenses = Collections.singletonList(createExpense("Test", 100, "Food"));
        String result = Summary.getRecentExpense(expenses, null);
        assertTrue(result.contains("₹") || result.contains("\u20B9"));
    }
}
