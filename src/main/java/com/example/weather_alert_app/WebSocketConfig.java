package com.example.weather_alert_app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WeatherWebSocketHandler weatherWebSocketHandler;

    public WebSocketConfig(WeatherWebSocketHandler weatherWebSocketHandler) {
        this.weatherWebSocketHandler = weatherWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(weatherWebSocketHandler, "/weather-alerts").setAllowedOrigins("*");
    }
}
