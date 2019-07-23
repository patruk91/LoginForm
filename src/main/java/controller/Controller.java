package controller;

import dao.sql.ConnectionPool;
import server.ServerLoginForm;

import java.io.IOException;
import java.sql.SQLException;

public class Controller {
    private final String URL = "jdbc:postgresql://localhost:5432/loginform";
    private final String USER = "pl";
    private final String PASSWORD = "postgres";

    public void run() {
        ConnectionPool connectionPool = getConnectionPool();
        startServer(connectionPool);
    }

    private ConnectionPool getConnectionPool() {
        try {
            return ConnectionPool.create(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error: connection failed");
    }

    private void startServer(ConnectionPool connectionPool) {
        ServerLoginForm serverLoginForm = new ServerLoginForm(connectionPool);
        try {
            serverLoginForm.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
