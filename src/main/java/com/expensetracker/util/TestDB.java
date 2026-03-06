package com.expensetracker.util;

import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("✅ Connection Successful!");
        } else {
            System.out.println("❌ Connection Failed!");
        }
    }
}

