package com.server;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.net.InetSocketAddress;

import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

/**
 * Represents the HTTPS server
 */
public class Server extends MessageHandler{

    private Server() {
    }

    /**
     * Creates and configures the SSL context for the server.
     *
     * @param keyStoreFile The file path of the keystore
     * @param passWord     The password for the keystore
     * @return SSLContext object for the server
     * @throws Exception if an error occurs during SSL context creation and configuration
     */
    private static SSLContext myServerSSLContext(String keyStoreFile, String passWord) throws Exception{
        char[] passphrase = passWord.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyStoreFile), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

    /**
     * Main method to start the HTTPS server.
     *
     * @param args command line arguments
     * @throws Exception if an error occurs during server initialization and execution
     */
    public static void main(String[] args) throws Exception {
        try{
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
            SSLContext sslContext = myServerSSLContext("keystore.jks", "dfgyui723");
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            UserAuthenticator authenticator = new UserAuthenticator();
            HttpContext infoContext = server.createContext("/info", new Server());
            server.createContext("/registration", new RegistrationHandler(authenticator));
            infoContext.setAuthenticator(authenticator);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start(); 
        } catch (FileNotFoundException e){
            System.out.println("Certificate not found!");
            e.printStackTrace();            
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
