package server;

import com.sun.net.httpserver.HttpServer;
import dao.IUserDao;
import dao.sql.ConnectionPool;
import dao.sql.IConnectionPool;
import dao.sql.UserSQL;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerLoginForm {
    private ConnectionPool connectionPool;

    public ServerLoginForm(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        IUserDao userDao = new UserSQL(connectionPool);

        server.createContext("/", new LoginFormHandler(userDao));
        server.setExecutor(null);
        server.start();
    }
}
