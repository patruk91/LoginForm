package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ILoginDataDao;
import dao.ISessionDao;
import helper.PasswordHasher;
import model.User;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginFormHandler implements HttpHandler {
    private ILoginDataDao loginData;
    private ISessionDao sessionDao;

    public LoginFormHandler(ILoginDataDao loginData, ISessionDao sessionDao) {
        this.loginData = loginData;
        this.sessionDao = sessionDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        String method = exchange.getRequestMethod();
        String cookieStr = exchange.getRequestHeaders().getFirst("Cookie");
        HttpCookie cookie;

        if(cookieStr != null) {
            cookie = HttpCookie.parse(cookieStr).get(0);
            if(sessionDao.isCurrentSession(cookie.getValue())) {
                response = handleExistingSession(exchange, method, cookieStr, response);
            } else {
                response = "<html><body>" +
                        "<p>Error at existing session<p>" +
                        "</body></html>";
            }
        } else {
            response = handleNewSession(exchange, response, method);
        }

        exchange.sendResponseHeaders(200, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    private String handleExistingSession(HttpExchange exchange, String method, String cookieStr, String response) {
        HttpCookie cookie = HttpCookie.parse(cookieStr).get(0);
        cookie.setVersion(1);
        if (method.equals("GET")) {
            User user = loginData.getUserById(sessionDao.getUserIdBySession(cookie.getValue()));
            response = "<html><body>\n" +
                    "<p>Hello "+ user.getName() +" </p>" +
                    createLogoutButton() +
                    "</body></html>";
        }

        if (method.equals("POST")) {
            sessionDao.deleteSessionData(cookie.getValue());
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            exchange.getResponseHeaders().add("Set-Cookie", String.format("%s=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; max-age=0", cookie.toString()));
            response = "<html><body>\n" +
                    "Logged out!" +
                    "    <p>Sign in</p>\n" +
                    createLoginForm() +
                    "</body></html>";
        }
        return response;
    }

    private String handleNewSession(HttpExchange exchange, String response, String method) throws IOException {
        if (method.equals("GET")) {
            response = "<html><body>\n" +
                    "    <p>Sign in</p>\n" +
                    createLoginForm() +
                    "</body></html>";
        }

        if (method.equals("POST")) {
            InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String formData = bufferedReader.readLine();

            System.out.println(formData);
            Map<String, String> inputs = parseFormData(formData);
            String login = inputs.get("login");
            String password = inputs.get("password");
            response = checkUserCredentials(login, password, exchange);
        }
        return response;
    }

    private String checkUserCredentials(String login, String password, HttpExchange exchange) {
        String response = "";
        if (loginData.checkIfLoginIsCorrect(login)) {
            response = getResponseIfPasswordIsCorrect(login, password, exchange);
        } else {
            response = "<html><body>\n" +
                    "    <p>Sign in</p>\n" +
                    createLoginForm() +
                    "</body></html>";
        }
        return response;
    }

    private String getResponseIfPasswordIsCorrect(String login, String password, HttpExchange exchange) {
        String response = "";
        String salt = loginData.getSaltByLogin(login);
        PasswordHasher passwordHasher = new PasswordHasher();
        String hashedPassword = passwordHasher.hashPassword(salt + password);
        if (loginData.checkIfPasswordIsCorrect(login, hashedPassword)) {
            createSessionForUser(exchange, login);
            User user = loginData.getUserByLogin(login);
            response = "<html><body>\n" +
                    "<p>Hello "+ user.getName() +" </p>" +
                    createLogoutButton() +
                    "</body></html>";
        } else {
            response = "<html><body>\n" +
                    "    <p>Incorrect password!</p>\n" +
                    createLoginForm() +
                    "</body></html>";
        }

        return response;
    }

    private void createSessionForUser(HttpExchange exchange, String login) {
        UUID uuid = UUID.randomUUID();
        HttpCookie cookie = new HttpCookie("sessionId", String.valueOf(uuid));
        exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
        int userId = loginData.getUserByLogin(login).getId();
        sessionDao.insertSessionData(uuid.toString(), userId);
    }


    private Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }
        return map;
    }

    private String createLoginForm() {
        return  "<form method=\"POST\">\n" +
                "    <label for=\"login\">Login</label><br>\n" +
                "    <input id=\"login\" type=\"text\" name=\"login\" placeholder=\"Login...\"><br><br>\n" +
                "    <label for=\"password\">Password</label><br>\n" +
                "    <input id=\"password\" type=\"text\" name=\"password\" placeholder=\"Password...\"><br><br>\n" +
                "    <input type=\"submit\" value=\"Submit\">\n" +
                "</form>";
    }

    private String createLogoutButton() {
        return  "<form action=\"login\" method=\"POST\">\n" +
                "    <input type=\"submit\" value=\"Logout\">\n" +
                "</form>";
    }
}
