package com.codechallenge.codechallenge;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class SqliteDB {

    /**
     * Connect to the test.db database
     * @return the Connection object
     */
    public static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:./test.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

}
