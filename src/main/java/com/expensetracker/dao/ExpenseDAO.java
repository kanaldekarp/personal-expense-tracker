package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseDAO {

    public boolean addExpense(Expense expense) {
        String sql = "INSERT INTO expenses (user_id, title, category, amount, date, description, tags) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, expense.getUserId());
            stmt.setString(2, expense.getTitle());
            stmt.setString(3, expense.getCategory());
            stmt.setDouble(4, expense.getAmount());
            stmt.setDate(5, Date.valueOf(expense.getDate()));
            stmt.setString(6, expense.getDescription());
            stmt.setString(7, expense.getTags());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    expense.setId(keys.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Expense> getFilteredExpensesByDate(int userId, String fromDate, String toDate) {
        return getFilteredExpenses(userId, fromDate, toDate, null);
    }

    public List<Expense> getFilteredExpensesByDateAndCategory(int userId, String fromDate, String toDate, String category) {
        return getFilteredExpenses(userId, fromDate, toDate, category);
    }

    public List<Expense> getFilteredExpenses(int userId, String fromDate, String toDate, String category) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT id, title, category, amount, date, description, tags " +
                     "FROM expenses WHERE user_id = ? AND date >= ? AND date <= ?";

        if (category != null && !category.isEmpty()) {
            sql += " AND category = ?";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(fromDate));
            ps.setDate(3, Date.valueOf(toDate));
            if (category != null && !category.isEmpty()) {
                ps.setString(4, category);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expense e = new Expense();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setCategory(rs.getString("category"));
                e.setAmount(rs.getDouble("amount"));
                e.setDate(rs.getDate("date").toLocalDate());
                e.setDescription(rs.getString("description"));
                e.setTags(rs.getString("tags"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public List<Expense> getAllExpensesByUser(int userId) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT id, title, category, amount, date, description, tags " +
                     "FROM expenses WHERE user_id = ? ORDER BY date";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expense e = new Expense();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setCategory(rs.getString("category"));
                e.setAmount(rs.getDouble("amount"));
                e.setDate(rs.getDate("date").toLocalDate());
                e.setDescription(rs.getString("description"));
                e.setTags(rs.getString("tags"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    // ==================== NEW METHODS ====================

    /**
     * Get expenses for a specific month and user.
     */
    public List<Expense> getExpensesByMonth(int userId, YearMonth yearMonth) {
        List<Expense> list = new ArrayList<>();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        String sql = "SELECT id, title, category, amount, date, description, tags " +
                     "FROM expenses WHERE user_id = ? AND date >= ? AND date <= ? ORDER BY date";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expense e = new Expense();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setCategory(rs.getString("category"));
                e.setAmount(rs.getDouble("amount"));
                e.setDate(rs.getDate("date").toLocalDate());
                e.setDescription(rs.getString("description"));
                e.setTags(rs.getString("tags"));
                list.add(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Get category-wise totals for a list of expenses.
     */
    public static Map<String, Double> getCategoryTotals(List<Expense> expenses) {
        Map<String, Double> totals = new LinkedHashMap<>();
        if (expenses != null) {
            for (Expense e : expenses) {
                totals.merge(e.getCategory(), e.getAmount(), Double::sum);
            }
        }
        return totals;
    }

    /**
     * Get user email by user ID.
     */
    public String getUserEmail(int userId) {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get username by user ID.
     */
    public String getUsername(int userId) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all users who have an email set (for scheduled reports).
     * Returns Map of userId -> {username, email}
     */
    public List<Map<String, Object>> getAllUsersWithEmail() {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, username, email FROM users WHERE email IS NOT NULL AND email != ''";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Get daily spending map for a given month (for calendar heatmap).
     */
    public Map<Integer, Double> getDailySpending(int userId, int month, int year) {
        Map<Integer, Double> dailyMap = new TreeMap<>();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        String sql = "SELECT EXTRACT(DAY FROM date)::int AS day, SUM(amount) AS total FROM expenses WHERE user_id=? AND date>=? AND date<=? GROUP BY day ORDER BY day";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dailyMap.put(rs.getInt("day"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailyMap;
    }

    /**
     * Get monthly totals for the last N months (for trend line chart & predictions).
     */
    public Map<String, Double> getMonthlyTotals(int userId, int months) {
        Map<String, Double> totals = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            String key = m.getYear() + "-" + String.format("%02d", m.getMonthValue());
            totals.put(key, 0.0);
        }
        String sql = "SELECT TO_CHAR(date, 'YYYY-MM') AS month, SUM(amount) AS total FROM expenses WHERE user_id=? AND date >= ? GROUP BY month ORDER BY month";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(now.minusMonths(months - 1).withDayOfMonth(1)));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String month = rs.getString("month");
                if (totals.containsKey(month)) {
                    totals.put(month, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    /**
     * Get average daily spending for the current month.
     */
    public double getAvgDailySpending(int userId) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        int daysElapsed = now.getDayOfMonth();
        String sql = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE user_id=? AND date>=? AND date<=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(now));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1) / Math.max(daysElapsed, 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get spending for same month last year (YoY comparison).
     */
    public double getSpendingForMonth(int userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        String sql = "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE user_id=? AND date>=? AND date<=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get category totals for a specific month (for category drill-down).
     */
    public Map<String, Double> getCategoryTotalsForMonth(int userId, int month, int year) {
        Map<String, Double> totals = new LinkedHashMap<>();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        String sql = "SELECT category, SUM(amount) AS total FROM expenses WHERE user_id=? AND date>=? AND date<=? GROUP BY category ORDER BY total DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                totals.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totals;
    }

    /**
     * Predict next month spending using simple moving average of last 3 months.
     */
    public double predictNextMonthSpending(int userId) {
        Map<String, Double> totals = getMonthlyTotals(userId, 3);
        double sum = 0;
        int count = 0;
        for (double val : totals.values()) {
            if (val > 0) { sum += val; count++; }
        }
        return count > 0 ? sum / count : 0;
    }

    /**
     * Get top N expenses for a user.
     */
    public List<Expense> getTopExpenses(int userId, int limit) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT id, title, category, amount, date, description, tags FROM expenses WHERE user_id=? ORDER BY amount DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Expense e = new Expense();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setCategory(rs.getString("category"));
                e.setAmount(rs.getDouble("amount"));
                e.setDate(rs.getDate("date").toLocalDate());
                e.setDescription(rs.getString("description"));
                e.setTags(rs.getString("tags"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Bulk import expenses (for CSV import).
     */
    public int bulkAddExpenses(List<Expense> expenses) {
        String sql = "INSERT INTO expenses (user_id, title, category, amount, date, description) VALUES (?, ?, ?, ?, ?, ?)";
        int count = 0;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            con.setAutoCommit(false);
            for (Expense e : expenses) {
                ps.setInt(1, e.getUserId());
                ps.setString(2, e.getTitle());
                ps.setString(3, e.getCategory());
                ps.setDouble(4, e.getAmount());
                ps.setDate(5, Date.valueOf(e.getDate()));
                ps.setString(6, e.getDescription());
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return count;
    }

    /**
     * Update user profile info.
     */
    public boolean updateUserProfile(int userId, String username, String email) {
        String sql = "UPDATE users SET username=?, email=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update user password.
     */
    public boolean updateUserPassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify user's current password.
     */
    public boolean verifyPassword(int userId, String hashedPassword) {
        String sql = "SELECT id FROM users WHERE id=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user creation date.
     */
    public String getUserCreatedAt(int userId) {
        String sql = "SELECT created_at FROM users WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                return ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "N/A";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    /**
     * Count total expenses for a user.
     */
    public int countExpenses(int userId) {
        String sql = "SELECT COUNT(*) FROM expenses WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
