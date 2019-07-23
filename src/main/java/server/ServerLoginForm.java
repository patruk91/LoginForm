package server;

import com.sun.net.httpserver.HttpServer;
import dao.ILoginDataDao;
import dao.ISessionDao;
import dao.sql.ConnectionPool;
import dao.sql.LoginDataSQL;
import dao.sql.SessionSQL;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerLoginForm {
    private ConnectionPool connectionPool;

    public ServerLoginForm(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        ILoginDataDao userDao = new LoginDataSQL(connectionPool);
        ISessionDao sessionDao = new SessionSQL(connectionPool);

        server.createContext("/login", new LoginFormHandler(userDao, sessionDao));
        server.setExecutor(null);
        server.start();
    }
}
