package com.expensetracker.dao;

import com.expensetracker.model.Tag;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {

    public boolean addTag(Tag tag) {
        String sql = "INSERT INTO tags (user_id, name, color) VALUES (?,?,?) ON CONFLICT (user_id, name) DO NOTHING";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, tag.getUserId());
            ps.setString(2, tag.getName());
            ps.setString(3, tag.getColor());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTag(int id, int userId) {
        String sql = "DELETE FROM tags WHERE id=? AND user_id=?";
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

    public List<Tag> getAllByUser(int userId) {
        List<Tag> list = new ArrayList<>();
        String sql = "SELECT * FROM tags WHERE user_id=? ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Tag t = new Tag();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setName(rs.getString("name"));
                t.setColor(rs.getString("color"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void tagExpense(int expenseId, int tagId) {
        String sql = "INSERT INTO expense_tags (expense_id, tag_id) VALUES (?,?) ON CONFLICT DO NOTHING";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, tagId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Tag> getTagsForExpense(int expenseId) {
        List<Tag> list = new ArrayList<>();
        String sql = "SELECT t.* FROM tags t JOIN expense_tags et ON t.id=et.tag_id WHERE et.expense_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Tag t = new Tag();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setName(rs.getString("name"));
                t.setColor(rs.getString("color"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
