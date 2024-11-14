# Server Application

This project is a server application that handles user registration, authentication, and weather data retrieval.

## Technologies Used

- **Java**: The primary programming language used for the server application.
- **Maven**: A build automation tool used for managing dependencies and building the project.
- **JUnit**: A testing framework for Java, used for unit testing.
- **SQLite**: A lightweight database engine used for storing user and message data.
- **HTTPS**: Secure communication protocol used by the server.
- **JSON**: Data format used for handling user and message data.
- **XML**: Data format used for retrieving and parsing weather data.
- **Commons Codec**: A library for encoding and decoding data, used for password hashing.

## Project Structure

The project is divided into three main packages: `server`, `database`, and `weather`.

### Server

The `server` package contains the main server application, which handles user registration, authentication, and weather data retrieval.

### Database

The `database` package contains classes for managing the SQLite database, including user and message data.

### Weather

The `weather` package contains classes for retrieving and parsing weather data from the OpenWeatherMap API.

### Configuration

- **Keystore**: The server uses a keystore file (`keystore.jks`) for SSL configuration.
- **Database**: The SQLite database file (`MessageDB`) is used to store user and message data.
- **Weather Server**: The weather data is retrieved from a local server running on port 4001.
