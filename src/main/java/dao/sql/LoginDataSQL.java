package dao.sql;

import dao.ILoginDataDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDataSQL implements ILoginDataDao {
    private ConnectionPool connectionPool;

    public LoginDataSQL(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }


    @Override
    public boolean checkIfLoginIsCorrect(String login) {
        boolean exists = false;
        try {
            Connection connection = connectionPool.getConnection();
            exists = checkIfLoginExists(connection, login);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }

        return exists;
    }

    private boolean checkIfLoginExists(Connection connection, String login) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement(
                "SELECT exists(SELECT 'exists' FROM usercredentials WHERE login = ?) AS result")) {
            stmt.setString(1, login);
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
    public boolean checkIfPasswordIsCorrect(String login, String password) {
        boolean check = false;
        try {
            Connection connection = connectionPool.getConnection();
            check = checkPassword(connection, login, password);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }

        return check;
    }
    private boolean checkPassword(Connection connection, String login, String password) throws SQLException {
        try(PreparedStatement stmt = connection.prepareStatement(
                "SELECT exists(SELECT 'exists' FROM usercredentials WHERE login = ? AND hashed_password = ?) AS result")) {
            stmt.setString(1, login);
            stmt.setString(2, password);
            return isExists(stmt);
        }
    }
}
