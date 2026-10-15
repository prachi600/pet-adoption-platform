package com.petadoption.util;

import com.petadoption.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Central place that opens JDBC connections using values from db.properties (or env vars). */
public final class DBConnection {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
            Class.forName(get("db.driver", "org.h2.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {
    }

    private static String get(String key, String defaultValue) {
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isEmpty()) {
            return env;
        }
        return PROPS.getProperty(key, defaultValue);
    }

    public static Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(
                    get("db.url", "jdbc:h2:file:./data/petadoption;MODE=MySQL"),
                    get("db.user", "sa"),
                    get("db.password", ""));
        } catch (SQLException e) {
            throw new DatabaseException("Could not connect to the database: " + e.getMessage(), e);
        }
    }
}
