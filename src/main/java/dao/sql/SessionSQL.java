package dao.sql;

import dao.ISessionDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionSQL implements ISessionDao {
    private ConnectionPool connectionPool;

    public SessionSQL(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void insertSessionData(String session, int userId) {
        try {
            Connection connection = connectionPool.getConnection();
            addUserSessionToDatabase(connection, session, userId);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void addUserSessionToDatabase(Connection connection, String session, int userId) throws SQLException {
        try (PreparedStatement stmtInsertUserSessionData = connection.prepareStatement(
                "INSERT INTO sessions(session, user_id) VALUES(?, ?)")) {
            insertUserSessionData(stmtInsertUserSessionData, session, userId);
        }
    }

    private void insertUserSessionData(PreparedStatement stmtInsertUserSessionData, String session, int userId) throws SQLException {
        stmtInsertUserSessionData.setString(1, session);
        stmtInsertUserSessionData.setInt(2, userId);
        stmtInsertUserSessionData.executeUpdate();
    }

    @Override
    public void deleteSessionData(String session) {
        try {
            Connection connection = connectionPool.getConnection();
            deleteUserSessionFromDatabase(connection, session);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
    }

    private void deleteUserSessionFromDatabase(Connection connection, String session) throws SQLException {
        try (PreparedStatement stmtInsertUserSessionData = connection.prepareStatement(
                "DELETE FROM sessions WHERE session = ?")) {
            stmtInsertUserSessionData.setString(1, session);
            stmtInsertUserSessionData.executeUpdate();
        }
    }


    @Override
    public boolean isCurrentSession(String session) {
        boolean exists = false;
        try {
            Connection connection = connectionPool.getConnection();
            exists = checkIfSessionExists(connection, session);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        return exists;
    }

    private boolean checkIfSessionExists(Connection connection, String session) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement(
                "SELECT exists(SELECT 'exists' FROM sessions WHERE session = ?) AS result")) {
            stmt.setString(1, session);
            return isExists(stmt);
        }
    }

    private boolean isExists(PreparedStatement stmt) throws SQLException {
        boolean result = false;
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result = rs.getBoolean("result");
            }
        }
        return result;
    }

    @Override
    public int getUserIdBySession(String session) {
        int userId = 0;
        try {
            Connection connection = connectionPool.getConnection();
            getUserId(connection, session, userId);
            connectionPool.releaseConnection(connection);
            return userId;
        } catch (SQLException e) {
            System.err.println("SQLException in selectUserIdBySessionId: " + e.getMessage());
        }
        throw new RuntimeException("No user_Id by that session_id");
    }

    private int getUserId(Connection connection, String session, int userId) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement("SELECT user_id FROM sessions WHERE session = ?")) {
            stmt.setString(1, session);
            return getUserIdFromDatabase(stmt, userId);
        }
    }

    private int getUserIdFromDatabase(PreparedStatement stmt, int userId) throws SQLException {
        try (ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                userId = resultSet.getInt("user_id");
            }
        }
        return userId;
    }


}
