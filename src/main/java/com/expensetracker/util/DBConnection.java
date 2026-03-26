package com.expensetracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConnection {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("❌ Sorry, unable to find db.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() {
        Connection cn = null;
        try {
            Class.forName(properties.getProperty("db.driver"));
            cn = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
            );
            // System.out.println("✅ Database Connected Successfully!"); // Uncomment for debug
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver Not Found! " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Connection Failed! " + e.getMessage());
        }
        return cn;
    }
}
