package dao.sql;

import dao.ILoginDataDao;
import model.User;

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

    @Override
    public User getUserByLogin(String login) {
        try {
            Connection connection = connectionPool.getConnection();
            User user = getSingleUser(connection, login);
            connectionPool.releaseConnection(connection);
            return user;
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        throw new RuntimeException("No user by that login");
    }

    private User getSingleUser(Connection connection, String login) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM usercredentials WHERE login = ?")) {
            stmt.setString(1, login);
            return getSingleUserData(stmt);
        }
    }

    private User getSingleUserData(PreparedStatement stmt) throws SQLException {
        User user = null;
        try (ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                String name = resultSet.getString("login");
                String password = resultSet.getString("hashed_password");
                int id = resultSet.getInt("id");
                user = new User(id, name, password);
            }
            return user;
        }
    }


    @Override
    public String getSaltByLogin(String login) {
        String salt = "";
        try {
            Connection connection = connectionPool.getConnection();
            salt = getSalt(connection, login, salt);
            connectionPool.releaseConnection(connection);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage()
                    + "\nSQLState: " + e.getSQLState()
                    + "\nVendorError: " + e.getErrorCode());
        }
        return salt;
    }

    private String getSalt(Connection connection, String login, String salt) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT salt FROM usercredentials WHERE login = ?")) {
            stmt.setString(1, login);
            return getSaltFromDatabase(stmt, salt);
        }
    }

    private String getSaltFromDatabase(PreparedStatement stmt, String salt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                salt = rs.getString("salt");
            }
        }
        return salt;
    }


    @Override
    public User getUserById(int userId) {
        String query = "";
        User user = null;
        try {
            Connection connection = connectionPool.getConnection();
            user = getUser(userId, connection, query);
            connectionPool.releaseConnection(connection);
            return user;
        } catch (SQLException e) {
            System.err.println("SQLException in getUserById: " + e.getMessage());
        }
        throw new RuntimeException("No user by that id");
    }

    private User getUser(int userId, Connection connection, String query) throws SQLException {
        User user = null;
        try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM usercredentials WHERE id = ?")) {
            stmt.setInt(1, userId);
            return getUserData(stmt);
        }
    }

    private User getUserData(PreparedStatement stmt) throws SQLException {
        User user = null;
        try (ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String name = resultSet.getString("name");
                String type = resultSet.getString("type");
                return user = new User(userId, name, type);
            }
        }
        throw new IllegalArgumentException("No user in database");
    }

}
