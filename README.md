# Weather Alert App

## Tech Stack

- **Java**
- **H2 Database**
- **Spring Boot**
- **Apache Camel**
- **React + JavaScript**
- **OpenWeatherMap** (for weather information)
- **WeatherBit** (for severe weather alerts)

## Description

**Weather Alert App** is a Spring Boot application that fetches weather data from a public OpenWeatherMap API, processes the data, displays the information, and sends alerts for extreme weather conditions.

### Key Features:
- **Data Fetching**: Use Apache Camel to fetch weather data periodically.
- **Data Processing**: Obtain the geographical coordinates (latitude and longitude) for a given city, retrieve the weather information using those coordinates, and transform the fetched JSON data into a simplified format.
- **Database Storage**: Save the processed data to a local database.

### User Interaction Features:
- View Current Weather Data
- Search/Add/Delete a City
- Alerts for Extreme Weather Conditions

## References

- [H2 Database with Spring Boot](https://www.baeldung.com/spring-boot-h2-database)
- [Spring Boot with Apache Camel Setup](https://www.geeksforgeeks.org/using-spring-boot-with-apache-camel/)
- [Spring Boot with Thymeleaf Example](https://www.geeksforgeeks.org/spring-boot-thymeleaf-with-example/)
- [OpenWeatherMap API](https://openweathermap.org/current)
- [WebSocket with Spring Boot](https://www.geeksforgeeks.org/spring-boot-web-socket/)
- [WebSocketHandler API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/socket/WebSocketHandler.html)