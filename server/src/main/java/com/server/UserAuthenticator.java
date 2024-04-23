package com.server;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;

/**
 * Represents a User Authenticator class that authenticates users.
 */
public class UserAuthenticator extends BasicAuthenticator {
    
    private MessageDatabase db = null;

    /**
     * Constructs a UserAuthenticator object.
     */
    public UserAuthenticator(){
        super("info");
        db = MessageDatabase.getInstance();
    }

    /**
     * Checks the credentials of a user.
     *
     * @param username The username
     * @param password The password
     * @return true if the credentials are valid, otherwise false
     */
    @Override
    public boolean checkCredentials(String username, String password) {
        boolean isValidUser;
        try{
            isValidUser = db.authenticateUser(username, password);
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return isValidUser;
    }
    /**
     * Adds a new user to the database.
     *
     * @param userName     The username of the new user
     * @param password     The password of the new user
     * @param email        The email of the new user
     * @param userNickname The nickname of the new user
     * @return true if the user is successfully registered, otherwise false
     * @throws JSONException if an error occurs while handling JSON data
     * @throws SQLException  if a SQL-related error occurs
     */
    public boolean addUser(String userName, String password, String email, String userNickname) throws JSONException, SQLException {
        JSONObject obj = new JSONObject().put("username", userName).put("password", password).put("email", email).put("userNickname", userNickname);
        boolean result = db.setUser(obj);
        if (!result){
            System.out.println("cannot register user");
            return false;
        }
        System.out.println(userName + "registered");
        return true;
    }

}
