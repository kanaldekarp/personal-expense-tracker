package com.expensetracker.dao;

import com.expensetracker.model.SavingsGoal;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavingsGoalDAO {

    public boolean add(SavingsGoal g) {
        String sql = "INSERT INTO savings_goals (user_id, name, target_amount, saved_amount, deadline, icon, color) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, g.getUserId());
            ps.setString(2, g.getName());
            ps.setDouble(3, g.getTargetAmount());
            ps.setDouble(4, g.getSavedAmount());
            ps.setDate(5, g.getDeadline() != null ? Date.valueOf(g.getDeadline()) : null);
            ps.setString(6, g.getIcon());
            ps.setString(7, g.getColor());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(SavingsGoal g) {
        String sql = "UPDATE savings_goals SET name=?, target_amount=?, saved_amount=?, deadline=?, icon=?, color=? WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setDouble(2, g.getTargetAmount());
            ps.setDouble(3, g.getSavedAmount());
            ps.setDate(4, g.getDeadline() != null ? Date.valueOf(g.getDeadline()) : null);
            ps.setString(5, g.getIcon());
            ps.setString(6, g.getColor());
            ps.setInt(7, g.getId());
            ps.setInt(8, g.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSavedAmount(int id, int userId, double amount) {
        String sql = "UPDATE savings_goals SET saved_amount = saved_amount + ? WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM savings_goals WHERE id=? AND user_id=?";
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

    public List<SavingsGoal> getAllByUser(int userId) {
        List<SavingsGoal> list = new ArrayList<>();
        String sql = "SELECT * FROM savings_goals WHERE user_id=? ORDER BY created_at DESC";
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

    public SavingsGoal getById(int id, int userId) {
        String sql = "SELECT * FROM savings_goals WHERE id=? AND user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SavingsGoal map(ResultSet rs) throws SQLException {
        SavingsGoal g = new SavingsGoal();
        g.setId(rs.getInt("id"));
        g.setUserId(rs.getInt("user_id"));
        g.setName(rs.getString("name"));
        g.setTargetAmount(rs.getDouble("target_amount"));
        g.setSavedAmount(rs.getDouble("saved_amount"));
        Date d = rs.getDate("deadline");
        if (d != null) g.setDeadline(d.toLocalDate());
        g.setIcon(rs.getString("icon"));
        g.setColor(rs.getString("color"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) g.setCreatedAt(ts.toLocalDateTime());
        return g;
    }
}
