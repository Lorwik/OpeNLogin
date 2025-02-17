/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.database;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@RequiredArgsConstructor
public class SQLite implements Database {

    @NonNull
    private final File file;
    private Connection connection;

    /**
     * Open the connection
     *
     * @throws SQLException on failure
     */
    public void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Failed to find class 'org.sqlite.JDBC'", e);
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new RuntimeException("Failed to create '" + parentFile + "'");
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.toString());
        }
    }

    /**
     * Close the connection
     *
     * @throws SQLException on failure
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    /**
     * Execute a update
     *
     * @param command the command to be executed
     * @throws SQLException on failure
     */
    public void update(String command) throws SQLException {
        openConnection();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(command);
        } catch (SQLException e) {
            throw new SQLException("Failed to execute update statement: '" + command + "'", e);
        }
    }

    /**
     * Executes a query
     *
     * @param command the command to be executed
     * @return returns an instance of {@link com.nickuc.openlogin.common.database.Database.Query}
     * @throws SQLException on failure
     */
    public Query query(String command) throws SQLException {
        openConnection();
        return new Query(connection, command);
    }
}
