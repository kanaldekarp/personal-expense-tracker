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
                System.out.println("⚠️ db.properties not found, using environment variables");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get environment variable with fallback to db.properties value.
     */
    private static String getConfig(String envKey, String propKey, String defaultVal) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.isEmpty()) return envVal;
        return properties.getProperty(propKey, defaultVal);
    }

    public static Connection getConnection() {
        Connection cn = null;
        try {
            Class.forName(getConfig("DB_DRIVER", "db.driver", "org.postgresql.Driver"));

            String url = getConfig("DATABASE_URL", "db.url", null);
            String user = getConfig("DB_USER", "db.user", null);
            String password = getConfig("DB_PASSWORD", "db.password", null);

            if (url != null && url.startsWith("postgresql://")) {
                // Convert Neon/Render-style URL: postgresql://user:pass@host/db
                url = "jdbc:" + url;
                if (!url.contains("sslmode")) {
                    url += (url.contains("?") ? "&" : "?") + "sslmode=require";
                }
                cn = DriverManager.getConnection(url);
            } else {
                cn = DriverManager.getConnection(url, user, password);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver Not Found! " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Connection Failed! " + e.getMessage());
        }
        return cn;
    }
}
