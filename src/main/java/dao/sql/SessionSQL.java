package dao.sql;

import dao.ISessionDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
