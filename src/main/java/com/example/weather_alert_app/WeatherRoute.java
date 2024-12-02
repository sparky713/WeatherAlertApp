package com.example.weather_alert_app;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeatherRoute extends RouteBuilder {
    @Autowired
    private WeatherController weatherController;

    @Override
    public void configure() {
        // get weather data
        from("direct:fetchWeather")
                .toD("${body}")
                .convertBodyTo(String.class)
                .log("Fetched weather data: ${body}");

        // update weather every 5 seconds
        from("timer:weatherUpdate?period=5000")
                .log("Updating the weather...")
                .bean(weatherController, "updateWeatherData");
    }
}
