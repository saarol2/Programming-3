package com.server;

import java.sql.Statement;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents the message database.
 */
public class MessageDatabase {
 
    private static Connection dbConnection = null;
    private static MessageDatabase MessageDB = null;

    /**
     * Gets the instance of the MessageDatabase.
     *
     * @return The instance of MessageDatabase
     */
    public static synchronized MessageDatabase getInstance(){
        if (null == MessageDB){
            MessageDB = new MessageDatabase();
        }
        return MessageDB;
    }

    /**
     * Constructs the MessageDatabase.
     */
    private MessageDatabase(){
        try{
            open("MessageDB");
        } catch (SQLException e){
            System.out.println("Log - SQLexception");
        }
    }

    /**
     * Opens the database connection.
     *
     * @param dbName The name of the database
     * @throws SQLException if a SQL-related error occurs
     */
    public void open(String dbName) throws SQLException{
        boolean dataBaseExists = checkDataBaseExist(dbName);
        String database = "jdbc:sqlite:" + dbName;
        dbConnection = DriverManager.getConnection(database);
        if (!dataBaseExists){
            initializeDatabase();
        }
    }

    /**
     * Initializes the database schema.
     *
     * @return true if initialization is successful, otherwise false
     * @throws SQLException if a SQL-related error occurs
     */
    private boolean initializeDatabase() throws SQLException{
        if (null != dbConnection){
            String createUsersString = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), userNickName varchar(50) NOT NULL, primary key(username))";
            String createMessagesString = "create table messages (locationName varchar(50) NOT NULL, locationDescription varchar(50) NOT NULL, locationCity varchar(50) NOT NULL, locationCountry varchar(50) NOT NULL, locationStreetAddress varchar(50) NOT NULL, originalPoster varchar(50) NOT NULL,  originalPostingTime varChar(50) NOT NULL, latitude varchar(50), longitude varchar(50), weather varhcar(50))";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUsersString);
            createStatement.executeUpdate(createMessagesString);
            createStatement.close();
            System.out.println("DB successfully created");
            return true;
        }
        System.out.println("DB creation failed");
        return false;
    }

    /**
     * Checks if the database exists.
     *
     * @param dbName The name of the database
     * @return true if the database exists, otherwise false
     */
    private boolean checkDataBaseExist(String dbName){
        File file = new File(dbName);
        return file.exists() && !file.isDirectory();
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException if a SQL-related error occurs
     */
    public void closeDB() throws SQLException {
		if (null != dbConnection) {
			dbConnection.close();
            System.out.println("closing db connection");
			dbConnection = null;
		}
    }

    /**
     * Inserts a message into the database.
     *
     * @param message The message to be inserted
     * @throws SQLException              if a SQL-related error occurs
     * @throws DateTimeParseException    if a date parsing error occurs
     * @throws MalformedURLException    if a malformed URL error occurs
     * @throws IOException               if an I/O error occurs
     */
    public static void setMessage(JSONObject message) throws SQLException, DateTimeParseException, MalformedURLException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss.SSSX");
        String formattedTime = "";
        String text = message.getString("originalPostingTime");
        ZonedDateTime time = ZonedDateTime.parse(text, formatter);
        formattedTime = time.format(formatter);
        String setMessageString;
        if (message.has("latitude") && message.has("longitude")) {
            setMessageString = "INSERT INTO messages (locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPoster, originalPostingTime, latitude, longitude) VALUES ('" +
                    message.getString("locationName") + "','" +
                    message.getString("locationDescription") + "','" +
                    message.getString("locationCity") + "','" +
                    message.getString("locationCountry") + "','" +
                    message.getString("locationStreetAddress") + "','" +
                    message.getString("originalPoster") + "','" +
                    formattedTime + "','" +
                    message.getDouble("latitude") + "','" +
                    message.getDouble("longitude") + "')";
            if (message.has("weather")){
                setMessageString = "INSERT INTO messages (locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPoster, originalPostingTime, latitude, longitude, weather) VALUES ('" +
                    message.getString("locationName") + "','" +
                    message.getString("locationDescription") + "','" +
                    message.getString("locationCity") + "','" +
                    message.getString("locationCountry") + "','" +
                    message.getString("locationStreetAddress") + "','" +
                    message.getString("originalPoster") + "','" +
                    formattedTime + "','" +
                    message.getDouble("latitude") + "','" +
                    message.getDouble("longitude") + "','" +
                    message.getString("weather") + "')";
            }
        } else {
            setMessageString = "INSERT INTO messages (locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPoster, originalPostingTime) VALUES ('" +
                    message.getString("locationName") + "','" +
                    message.getString("locationDescription") + "','" +
                    message.getString("locationCity") + "','" +
                    message.getString("locationCountry") + "','" +
                    message.getString("locationStreetAddress") + "','" +
                    message.getString("originalPoster") + "','" +
                    formattedTime + "')";
        }
        Statement createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setMessageString);
        createStatement.close();
    }

    /**
     * Retrieves messages from the database.
     *
     * @return JSONArray of messages retrieved from the database
     * @throws SQLException if a SQL-related error occurs
     */
    public static JSONArray getMessages() throws SQLException{
        Statement queryStatement = null;
        JSONArray array = new JSONArray();
        String getMessagesString = "select locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPoster, originalPostingTime, latitude, longitude, weather from messages";
        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);
        try{
            while(rs.next()){
                JSONObject obj = new JSONObject();
                obj.put("locationName", rs.getString("locationName"));
                obj.put("locationDescription", rs.getString("locationDescription"));
                obj.put("locationCity", rs.getString("locationCity"));
                obj.put("locationCountry", rs.getString("locationCountry"));
                obj.put("locationStreetAddress", rs.getString("locationStreetAddress"));
                obj.put("originalPoster", rs.getString("originalPoster"));
                obj.put("originalPostingTime", rs.getString("originalPostingTime"));
                if (!rs.wasNull() && rs.getObject("latitude") != null && rs.getObject("longitude") != null) {
                    obj.put("latitude", rs.getDouble("latitude"));
                    obj.put("longitude", rs.getDouble("longitude"));
                    obj.put("weather", rs.getString("weather"));
                }
                array.put(obj);
            }
        } catch (JSONException e){
            System.out.println("json parsing error, faulty user json");
        }
        return array;
    }

    /**
     * Sets a user in the database.
     *
     * @param user The user to be set
     * @return true if the user is successfully set, otherwise false
     * @throws SQLException if a SQL-related error occurs
     */
    public boolean setUser(JSONObject user) throws SQLException{
        if (checkIfUserExists(user.getString("username"))){
            return false;
        }
        String pass = securePassWord(user.getString("password"));
        String setUserString = "insert into users " + "VALUES('" + user.getString("username") + "','" + pass + "','" + user.getString("email") + "','" + user.getString("userNickname") + "')";
        Statement createStatement;
        createStatement = dbConnection.createStatement();
		createStatement.executeUpdate(setUserString);
		createStatement.close();
        return true;
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param userName The username to be checked
     * @return true if the user exists, otherwise false
     * @throws SQLException if a SQL-related error occurs
     */
    private boolean checkIfUserExists(String userName) throws SQLException{
        Statement queryStatement = null;
        ResultSet rs;
        String checkUser = "select username from users where username = '" + userName + "'";
        System.out.println("Checking user");
        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(checkUser);
        if (rs.next()){
            System.out.println("user exists");
            return true;
        } else{
            return false;
        }
    }

    /**
     * Retrieves the nickname of a user from the database.
     *
     * @param principal The principal object
     * @return The nickname of the user
     * @throws SQLException if a SQL-related error occurs
     */
    public static String getNickNameDB(Principal principal) throws SQLException {
        String info = principal.getName();
        String[] parts = info.split(":");
        String username = parts[1];
        PreparedStatement statement = null;
        ResultSet rs = null;
        String nickname = null;
        try{
            String sql = "SELECT userNickName FROM users WHERE username = ?";
            statement = dbConnection.prepareStatement(sql);
            statement.setString(1, username);
            rs = statement.executeQuery();
            if (rs.next()){
                nickname = rs.getString("userNickName");
            }
        } catch (SQLException e){
            e.printStackTrace();
            }
            return nickname;
        }

    /**
     * Authenticates a user based on username and password.
     *
     * @param userName The username of the user
     * @param password The password of the user
     * @return true if the authentication is successful, otherwise false
     * @throws SQLException if a SQL-related error occurs
     */
    public boolean authenticateUser(String userName, String password) throws SQLException{
        Statement queryStatement = null;
        ResultSet rs;
        String getMessagesString = "Select userName, password from users where userName = '" + userName + "'";
        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(getMessagesString);
        if (rs.next() == false){
            System.out.println("cannot find such user");
            return false;
        } else{
            String pass = rs.getString("password");
            if (isSamePassword(password, pass)){
                return true;
            } else{
                return false;
            }
        }
    }

    /**
     * Secures a password using cryptography.
     *
     * @param passwd The password to be secured
     * @return The secured password
     */
    public String securePassWord(String passwd){
        SecureRandom rng = new SecureRandom();
        byte b[] = new byte[13];
        rng.nextBytes(b);
        String saltedB = new String(Base64.getEncoder().encode(b));
        String salt =  "$6$" + saltedB;
        String securePassword = Crypt.crypt(passwd, salt);
        return securePassword;
        
    }

    /**
     * Checks if a plain password matches the salted and hashed password.
     *
     * @param plain          The plain password
     * @param saltedHashed   The salted and hashed password
     * @return true if the passwords match, otherwise false
     */
    public static boolean isSamePassword(String plain, String saltedHashed){
        if (saltedHashed.equals(Crypt.crypt(plain, saltedHashed))) {
            return true;
        } else {
            return false;
        }
    }


}