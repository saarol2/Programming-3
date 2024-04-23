package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

/**
 * Handles HTTP requests and responses for message-related operations.
 */
public class MessageHandler implements HttpHandler{
    
    /**
     * Handles incoming HTTP requests.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException      if an I/O error occurs
     * @throws SQLException     if a SQL-related error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                handleResponsePOST(exchange);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                handleResponseGET(exchange);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            // Send a 400 Bad Request response for unsupported methods
            exchange.sendResponseHeaders(400, "Not supported".length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write("Not supported".getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
            handleNotSupported(exchange);
        }
    }

    /**
     * Handles POST requests.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException              if an I/O error occurs
     * @throws SQLException             if a SQL-related error occurs
     * @throws DateTimeParseException   if a date parsing error occurs
     */
    private void handleResponsePOST(HttpExchange exchange) throws IOException, SQLException, DateTimeParseException {
        Headers headers = exchange.getRequestHeaders();
        String contentType = headers.get("Content-Type").get(0);
        if (contentType.equalsIgnoreCase("application/json")){
            InputStream inputStream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            inputStream.close();
            if (text == null || text.length() == 0){
                exchange.sendResponseHeaders(412, 0);
                return;
            }
            try{
                JSONObject msgObject = new JSONObject(text);
                Principal principal = exchange.getPrincipal();
                String nick = MessageDatabase.getNickNameDB(principal);
                msgObject.put("originalPoster", nick);
                if (msgObject.has("latitude") && msgObject.has("longitude") && msgObject.has("weather")){
                    String weather = Weather.getWeatherData(msgObject.getDouble("latitude"), msgObject.getDouble("longitude"));
                    msgObject.put("weather", weather);
                }
                if (msgObject.getString("locationName").length() == 0 || msgObject.getString("locationDescription").length() == 0 || msgObject.getString("locationCity").length() == 0 || msgObject.getString("originalPoster").length() == 0 || msgObject.getString("locationCountry").length() == 0 || msgObject.getString("locationStreetAddress").length() == 0){
                    exchange.sendResponseHeaders(413, 0);
                    return;
                }
                MessageDatabase.setMessage(msgObject);
                exchange.sendResponseHeaders(200, -1);
            } catch(JSONException e){
                exchange.sendResponseHeaders(408, 0);
                System.out.println("json parsing error, faulty message json");
            } catch (DateTimeParseException e){
                exchange.sendResponseHeaders(410, 0);
                System.out.println("Wrong timestamp format");
            }
        } else{
            exchange.sendResponseHeaders(407, 0);
            System.out.println("Incorrect content type");
        }
    }

    /**
     * Handles GET requests.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException      if an I/O error occurs
     * @throws SQLException     if a SQL-related error occurs
     */
    private void handleResponseGET(HttpExchange exchange) throws IOException, SQLException{
            try{
                JSONArray msgArray = MessageDatabase.getMessages();
                String responseString = msgArray.toString();
                //String responseString = responseMessages.toString();
                byte[] bytes = responseString.getBytes("UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(responseString.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();
            } catch (DateTimeParseException e){
                exchange.sendResponseHeaders(410, 0);
                System.out.println("Wrong timestamp format");
            }
    }

    /**
     * Handles unsupported HTTP methods.
     *
     * @param exchange The HTTP exchange object
     * @throws IOException if an I/O error occurs
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

}
