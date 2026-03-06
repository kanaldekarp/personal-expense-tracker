package com.expensetracker.dao;

import com.expensetracker.model.Income;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class IncomeDAO {

    public boolean addIncome(Income income) {
        String sql = "INSERT INTO income (user_id, source, amount, date, is_recurring, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, income.getUserId());
            ps.setString(2, income.getSource());
            ps.setDouble(3, income.getAmount());
            ps.setDate(4, Date.valueOf(income.getDate()));
            ps.setBoolean(5, income.isRecurring());
            ps.setString(6, income.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateIncome(Income income) {
        String sql = "UPDATE income SET source=?, amount=?, date=?, is_recurring=?, notes=? WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, income.getSource());
            ps.setDouble(2, income.getAmount());
            ps.setDate(3, Date.valueOf(income.getDate()));
            ps.setBoolean(4, income.isRecurring());
            ps.setString(5, income.getNotes());
            ps.setInt(6, income.getId());
            ps.setInt(7, income.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteIncome(int id, int userId) {
        String sql = "DELETE FROM income WHERE id=? AND user_id=?";
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

    public List<Income> getAllByUser(int userId) {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id=? ORDER BY date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapIncome(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Income> getByMonth(int userId, int month, int year) {
        List<Income> list = new ArrayList<>();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        String sql = "SELECT * FROM income WHERE user_id=? AND date>=? AND date<=? ORDER BY date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapIncome(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getTotalIncomeByMonth(int userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        String sql = "SELECT COALESCE(SUM(amount),0) FROM income WHERE user_id=? AND date>=? AND date<=?";
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

    public double getTotalIncome(int userId) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM income WHERE user_id=?";
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

    private Income mapIncome(ResultSet rs) throws SQLException {
        Income i = new Income();
        i.setId(rs.getInt("id"));
        i.setUserId(rs.getInt("user_id"));
        i.setSource(rs.getString("source"));
        i.setAmount(rs.getDouble("amount"));
        i.setDate(rs.getDate("date").toLocalDate());
        i.setRecurring(rs.getBoolean("is_recurring"));
        i.setNotes(rs.getString("notes"));
        return i;
    }
}
