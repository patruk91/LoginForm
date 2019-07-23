package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ILoginDataDao;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginFormHandler implements HttpHandler {
    private ILoginDataDao loginData;

    public LoginFormHandler(ILoginDataDao loginData) {
        this.loginData = loginData;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        String method = exchange.getRequestMethod();
        String cookieStr = exchange.getRequestHeaders().getFirst("Cookie");
        HttpCookie cookie;

        if(cookieStr != null) {
            cookie = HttpCookie.parse(cookieStr).get(0);
            //handle current session
        } else {
            response = handleNewSession(exchange, response, method);
        }

        exchange.sendResponseHeaders(200, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
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
            response = checkUserCredentials(login, password, response);
        }
        return response;
    }

    private String checkUserCredentials(String login, String password, String response) {
        if (loginData.checkIfLoginIsCorrect()) {
            response = getResponseIfPasswordIsCorrect();
        } else {
            response = "<html><body>\n" +
                    "    <p>Sign in</p>\n" +
                    createLoginForm() +
                    "</body></html>";
        }
        return response;
    }

    private String getResponseIfPasswordIsCorrect() {
        String response;
        if (loginData.checkIfPasswordIsCorrect()) {
            //createSessionForUserOrCookie?
            response = "<html><body>\n" +
                    "    <p>Incorrect password!</p>\n" +
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
        return  "<form method=\"POST\">\n" +
                "    <input type=\"submit\" value=\"Logout\">\n" +
                "</form>";
    }
}
