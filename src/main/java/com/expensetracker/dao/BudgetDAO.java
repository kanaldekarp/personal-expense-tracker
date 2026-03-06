package com.expensetracker.dao;

import com.expensetracker.model.Budget;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BudgetDAO {

    /** Special category key for overall/total budget */
    public static final String TOTAL_CATEGORY = "__TOTAL__";

    /**
     * Save or update a budget entry. Uses UPSERT (INSERT ... ON CONFLICT UPDATE).
     */
    public boolean saveBudget(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category, budget_amount, month, year) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (user_id, category, month, year) " +
                     "DO UPDATE SET budget_amount = EXCLUDED.budget_amount";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, budget.getUserId());
            ps.setString(2, budget.getCategory());
            ps.setDouble(3, budget.getBudgetAmount());
            ps.setInt(4, budget.getMonth());
            ps.setInt(5, budget.getYear());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all budgets for a user for a specific month/year.
     */
    public List<Budget> getBudgetsByMonth(int userId, int month, int year) {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT id, user_id, category, budget_amount, month, year " +
                     "FROM budgets WHERE user_id = ? AND month = ? AND year = ? AND category != '" + TOTAL_CATEGORY + "' ORDER BY category";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getInt("id"));
                b.setUserId(rs.getInt("user_id"));
                b.setCategory(rs.getString("category"));
                b.setBudgetAmount(rs.getDouble("budget_amount"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Get budget map (category -> budgetAmount) for a user for a specific month/year.
     */
    public Map<String, Double> getBudgetMap(int userId, int month, int year) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Budget b : getBudgetsByMonth(userId, month, year)) {
            map.put(b.getCategory(), b.getBudgetAmount());
        }
        return map;
    }

    /**
     * Get sum of category budgets for a user for a specific month/year (excludes __TOTAL__).
     */
    public double getCategoryBudgetSum(int userId, int month, int year) {
        String sql = "SELECT COALESCE(SUM(budget_amount), 0) as total FROM budgets WHERE user_id = ? AND month = ? AND year = ? AND category != '" + TOTAL_CATEGORY + "'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get the overall total budget entry (the __TOTAL__ row), or null if not set.
     */
    public Budget getOverallBudget(int userId, int month, int year) {
        String sql = "SELECT id, user_id, category, budget_amount, month, year FROM budgets WHERE user_id = ? AND month = ? AND year = ? AND category = '" + TOTAL_CATEGORY + "'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Budget b = new Budget();
                b.setId(rs.getInt("id"));
                b.setUserId(rs.getInt("user_id"));
                b.setCategory(rs.getString("category"));
                b.setBudgetAmount(rs.getDouble("budget_amount"));
                b.setMonth(rs.getInt("month"));
                b.setYear(rs.getInt("year"));
                return b;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get effective total budget: if overall budget is set, use it; otherwise sum of category budgets.
     */
    public double getTotalBudget(int userId, int month, int year) {
        Budget overall = getOverallBudget(userId, month, year);
        if (overall != null) return overall.getBudgetAmount();
        return getCategoryBudgetSum(userId, month, year);
    }

    /**
     * Delete a specific budget entry.
     */
    public boolean deleteBudget(int budgetId, int userId) {
        String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, budgetId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy budgets from one month to another (for quick setup).
     */
    public int copyBudgets(int userId, int fromMonth, int fromYear, int toMonth, int toYear) {
        List<Budget> source = getBudgetsByMonth(userId, fromMonth, fromYear);
        int count = 0;
        for (Budget b : source) {
            Budget newB = new Budget(userId, b.getCategory(), b.getBudgetAmount(), toMonth, toYear);
            if (saveBudget(newB)) count++;
        }
        return count;
    }
}
