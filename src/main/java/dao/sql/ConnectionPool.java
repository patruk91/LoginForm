package dao.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool implements IConnectionPool {
    private final String URL;
    private final String USER;
    private final String PASSWORD;
    private final List<Connection> connectionPool;
    private final List<Connection> usedConnections = new ArrayList<>();
    private static final int INITIAL_POOL_SIZE = 10;
    private final int MAX_POOL_SIZE = 20;

    private ConnectionPool(String URL, String USER, String PASSWORD, List<Connection> connectionPool) {
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
        this.connectionPool = connectionPool;
    }

    public static ConnectionPool create(String URL, String USER, String PASSWORD) throws SQLException {
        List<Connection> pool = new ArrayList<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            pool.add(createConnection(URL, USER, PASSWORD));
        }
        return new ConnectionPool(URL, USER, PASSWORD, pool);
    }

    private static Connection createConnection(String URL, String USER, String PASSWORD) throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    @Override
    public Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            if(usedConnections.size() < MAX_POOL_SIZE) {
                connectionPool.add(createConnection(URL, USER, PASSWORD));
            } else {
                throw new RuntimeException("Maximum pool size reached, no available connections!");
            }
        }
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    @Override
    public List<Connection> getConnectionPool() {
        return connectionPool;
    }

    @Override
    public int getSize() {
        return connectionPool.size() + usedConnections.size();
    }

    @Override
    public String getURL() {
        return URL;
    }

    @Override
    public String getUSER() {
        return USER;
    }

    @Override
    public String getPASSWORD() {
        return PASSWORD;
    }

    @Override
    public boolean releaseConnection(Connection connection) {
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }

    @Override
    public void shutdown() throws SQLException {
        usedConnections.forEach(this::releaseConnection);
        for (Connection connection : connectionPool) {
            connection.close();
        }
        connectionPool.clear();
    }
}
