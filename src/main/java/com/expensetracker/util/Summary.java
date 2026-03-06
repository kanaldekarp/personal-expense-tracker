package com.expensetracker.util;

import com.expensetracker.model.Expense;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class Summary {

    /** Total of all expenses in the list */
    public static double getTotalAmount(List<Expense> expenseList) {
        double total = 0;
        if (expenseList != null) {
            for (Expense expense : expenseList) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    /** Category with the highest total spend */
    public static String getTopCategory(List<Expense> expenseList) {
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        if (expenseList != null) {
            for (Expense expense : expenseList) {
                categoryTotals.merge(
                    expense.getCategory(),
                    expense.getAmount(),
                    Double::sum
                );
            }
        }

        String topCategory = "-";
        double maxAmount = 0;
        for (Map.Entry<String, Double> e : categoryTotals.entrySet()) {
            if (e.getValue() > maxAmount) {
                topCategory = e.getKey();
                maxAmount = e.getValue();
            }
        }
        return topCategory;
    }

    /** Number of expenses in the list */
    public static int getExpenseCount(List<Expense> expenseList) {
        return (expenseList != null) ? expenseList.size() : 0;
    }

    /** Most recent expense detail formatted as title and amount */
    public static String getRecentExpense(List<Expense> expenseList) {
        return getRecentExpense(expenseList, "\u20B9");
    }

    /** Most recent expense detail formatted with given currency symbol */
    public static String getRecentExpense(List<Expense> expenseList, String currencySymbol) {
        if (expenseList == null || expenseList.isEmpty()) {
            return "N/A";
        }
        if (currencySymbol == null) currencySymbol = "\u20B9";
        Expense recent = expenseList.get(expenseList.size() - 1);
        return recent.getTitle() + " - " + currencySymbol + String.format("%,.2f", recent.getAmount());
    }
}
