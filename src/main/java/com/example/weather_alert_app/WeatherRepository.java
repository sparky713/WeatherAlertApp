package com.example.weather_alert_app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Long> {
    boolean existsByLatitudeAndLongitude(double latitude, double longitude); // automatically implemented by JpaRepository
}