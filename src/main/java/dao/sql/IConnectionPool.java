package dao.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IConnectionPool {
    Connection getConnection() throws SQLException;
    boolean releaseConnection(Connection connection);
    List<Connection> getConnectionPool();
    int getSize();
    String getURL();
    String getUSER();
    String getPASSWORD();
    void shutdown() throws SQLException;
}