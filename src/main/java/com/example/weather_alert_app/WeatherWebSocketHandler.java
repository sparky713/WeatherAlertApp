package com.example.weather_alert_app;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import java.util.Set;
import java.util.HashSet;

@Component
public class WeatherWebSocketHandler extends TextWebSocketHandler {

    private Set<WebSocketSession> sessions = new HashSet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("New connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Connection closed: " + session.getId());
    }

    public void sendAlert(String alertMessage) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(alertMessage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
