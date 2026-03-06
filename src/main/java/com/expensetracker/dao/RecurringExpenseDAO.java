package com.expensetracker.dao;

import com.expensetracker.model.RecurringExpense;
import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseDAO {

    public boolean add(RecurringExpense re) {
        String sql = "INSERT INTO recurring_expenses (user_id, title, category, amount, frequency, next_due, description, is_active) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, re.getUserId());
            ps.setString(2, re.getTitle());
            ps.setString(3, re.getCategory());
            ps.setDouble(4, re.getAmount());
            ps.setString(5, re.getFrequency());
            ps.setDate(6, Date.valueOf(re.getNextDue()));
            ps.setString(7, re.getDescription());
            ps.setBoolean(8, re.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(RecurringExpense re) {
        String sql = "UPDATE recurring_expenses SET title=?, category=?, amount=?, frequency=?, next_due=?, description=?, is_active=? WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, re.getTitle());
            ps.setString(2, re.getCategory());
            ps.setDouble(3, re.getAmount());
            ps.setString(4, re.getFrequency());
            ps.setDate(5, Date.valueOf(re.getNextDue()));
            ps.setString(6, re.getDescription());
            ps.setBoolean(7, re.isActive());
            ps.setInt(8, re.getId());
            ps.setInt(9, re.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM recurring_expenses WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleActive(int id, int userId) {
        String sql = "UPDATE recurring_expenses SET is_active = NOT is_active WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RecurringExpense> getAllByUser(int userId) {
        List<RecurringExpense> list = new ArrayList<>();
        String sql = "SELECT * FROM recurring_expenses WHERE user_id=? ORDER BY is_active DESC, next_due ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Process due recurring expenses: create actual expenses and advance next_due */
    public int processDueExpenses(int userId) {
        int count = 0;
        String sql = "SELECT * FROM recurring_expenses WHERE user_id=? AND is_active=true AND next_due <= CURRENT_DATE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            ExpenseDAO expenseDAO = new ExpenseDAO();
            while (rs.next()) {
                RecurringExpense re = map(rs);
                // Create the actual expense
                Expense exp = new Expense();
                exp.setUserId(userId);
                exp.setTitle(re.getTitle() + " (Recurring)");
                exp.setCategory(re.getCategory());
                exp.setAmount(re.getAmount());
                exp.setDate(re.getNextDue());
                exp.setDescription(re.getDescription() != null ? re.getDescription() : "Auto-generated from recurring expense");
                if (expenseDAO.addExpense(exp)) {
                    // Advance next_due based on frequency
                    LocalDate newDue = advanceDate(re.getNextDue(), re.getFrequency());
                    String updateSql = "UPDATE recurring_expenses SET next_due=? WHERE id=?";
                    try (PreparedStatement ups = con.prepareStatement(updateSql)) {
                        ups.setDate(1, Date.valueOf(newDue));
                        ups.setInt(2, re.getId());
                        ups.executeUpdate();
                    }
                    count++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private LocalDate advanceDate(LocalDate date, String frequency) {
        return switch (frequency != null ? frequency.toLowerCase() : "monthly") {
            case "daily" -> date.plusDays(1);
            case "weekly" -> date.plusWeeks(1);
            case "yearly" -> date.plusYears(1);
            default -> date.plusMonths(1);
        };
    }

    public double getMonthlyRecurringTotal(int userId) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM recurring_expenses WHERE user_id=? AND is_active=true";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private RecurringExpense map(ResultSet rs) throws SQLException {
        RecurringExpense re = new RecurringExpense();
        re.setId(rs.getInt("id"));
        re.setUserId(rs.getInt("user_id"));
        re.setTitle(rs.getString("title"));
        re.setCategory(rs.getString("category"));
        re.setAmount(rs.getDouble("amount"));
        re.setFrequency(rs.getString("frequency"));
        re.setNextDue(rs.getDate("next_due").toLocalDate());
        re.setDescription(rs.getString("description"));
        re.setActive(rs.getBoolean("is_active"));
        return re;
    }
}
