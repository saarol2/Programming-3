package com.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Represents a Weather class that retrieves weather data.
 */
public class Weather {

    /**
     * Retrieves weather data from the server using the given coordinates
     *
     * @param latitude latitude
     * @param longitude longitude
     * @return Weather data in XML form
     * @throws IOException Error in data transmission
     */
    public static String getWeatherData(double latitude, double longitude) throws IOException {
        URL url = new URL("http://localhost:4001/weather");
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();

        connection.setReadTimeout(10000);
        connection.setConnectTimeout(20000);
        connection.setRequestMethod("POST");

        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.setRequestProperty("Content-Type", "application/xml");

        OutputStream outputStream = connection.getOutputStream();
        String xmlData = "<coordinates>\n" +
                "    <latitude>" + latitude + "</latitude>\n" +
                "    <longitude>" + longitude + "</longitude>\n" +
                "</coordinates>";
        outputStream.write(xmlData.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return xmlParser(response.toString());
    }
    
    /**
     * Parses the XML response and extract the temperature and unit from it
     *
     * @param xmlString XML-formatted response
     * @return Temperature and unit as a string
     */
    private static String xmlParser(String xmlString){
        String weather = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlString));

            String temperature = "";
            String unit = "";

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    if (elementName.equals("temperature")) {
                        temperature = reader.getElementText();
                    } else if (elementName.equals("Unit")) {
                        unit = reader.getElementText();
                    }
                }
            }

            weather = temperature + " " + unit;

            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return weather;
    }

}
