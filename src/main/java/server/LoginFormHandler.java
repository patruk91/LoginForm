package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.IUserDao;

import java.io.IOException;

public class LoginFormHandler implements HttpHandler {
    private IUserDao userDao;

    public LoginFormHandler(IUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
