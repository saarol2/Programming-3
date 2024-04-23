package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


/**
 * Represents a Registration Handler class for handling user registration requests.
 */
public class RegistrationHandler implements HttpHandler {

    private UserAuthenticator authenticator;

    /**
     * Constructs a RegistrationHandler object.
     *
     * @param authenticator The UserAuthenticator object used for user authentication
     */
    public RegistrationHandler(UserAuthenticator authenticator){
        this.authenticator = authenticator;
    }

    /**
     * Handles the HTTP request.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException  If an I/O error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                handlePost(exchange);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
           handleNotSupported(exchange);
        }
    }

    /**
     * Handles the unsupported request.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException  If an I/O error occurs
     */
    private void handleNotSupported(HttpExchange exchange) throws IOException{
        String responseString = "Not supported";
        byte[] bytes = responseString.getBytes("UTF-8");
        exchange.sendResponseHeaders(400, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseString.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Handles the POST request.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException  If an I/O error occurs
     * @throws SQLException If a SQL-related error occurs
     */
    private void handlePost(HttpExchange exchange) throws IOException, SQLException{
        Headers headers = exchange.getRequestHeaders();
        String contentType = headers.get("Content-Type").get(0);
        if (contentType.equalsIgnoreCase("application/json")){
            InputStream inputStream = exchange.getRequestBody();
            String newUser = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            inputStream.close();
            if (newUser == null || newUser.length() == 0){
                exchange.sendResponseHeaders(412, 0);
                return;
            }
            try{
                JSONObject userObject = new JSONObject(newUser);
                if (userObject.getString("username").length() == 0 || userObject.getString("password").length() == 0 || userObject.getString("email").length() == 0 || userObject.getString("userNickname").length() == 0){
                    exchange.sendResponseHeaders(413, 0);
                    return;
                }
                else{
                    boolean success = authenticator.addUser(userObject.getString("username"), userObject.getString("password"), userObject.getString("email"), userObject.getString("userNickname"));
                    if (success){
                        exchange.sendResponseHeaders(200, -1);
                    } else{
                        exchange.sendResponseHeaders(403, 0);
                    }
                }
            } catch (JSONException e){
                System.out.println("json parsing error, faulty user json");
            }
        }
        else{
            exchange.sendResponseHeaders(407, 0);
            System.out.println("Incorrect content type");
        }
    }


}
