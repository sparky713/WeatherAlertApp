package com.example.weather_alert_app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherRepository weatherRepository;
    @Autowired
    private WeatherWebSocketHandler weatherWebSocketHandler;
    @Autowired
    private ProducerTemplate producerTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    private static final String OPENWEATHERMAP_API_KEY = "4e066fb90d39e3a32c9ad1e6fc9933e7";
    private static final String WEATHERBIT_API_KEY = "9d6c826f506b4103a10102a92bd7efa0";
    private static final String RESULTS_LIMIT = "10";
    private static final String GEOCODING_API_URL = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s,%s&limit=" + RESULTS_LIMIT + "&appid=" + OPENWEATHERMAP_API_KEY;
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=" + OPENWEATHERMAP_API_KEY + "&units=metric";
    private static final String WEATHER_ALERTS_API_URL = "https://api.weatherbit.io/v2.0/alerts?lat=%f&lon=%f&key=" + WEATHERBIT_API_KEY;

    @GetMapping
    public List<Weather> getAllWeather() {
        return weatherRepository.findAll();
    }

    @PostMapping
    public Weather addCity(@RequestParam String cityName, @RequestParam String stateCode, @RequestParam String countryCode) {
        try {
            // get lat and lon
            Map<String, Object> coords = getCoordinates(cityName, stateCode, countryCode);
            if (coords == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found");
            }
            double lat = (double) coords.get("lat");
            double lon = (double) coords.get("lon");

            // check for duplicates in database
            if (weatherRepository.existsByLatitudeAndLongitude(lat, lon)) {
                System.out.println("This city has already been added!");
                return null;
            }

            // get weather info & save to database
            return weatherRepository.save(getWeather(lat, lon));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR while fetching weather data for " + cityName + ", " + countryCode);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Long id) {
        weatherRepository.deleteById(id);
    }

    public void updateWeatherData() {
        List<Weather> weathers = getAllWeather();

        for (Weather weather : weathers) {
            try {
                double lat = weather.getLatitude();
                double lon = weather.getLongitude();

                checkAndSendWeatherAlerts(weather, lat, lon);
                updateWeather(weather, lat, lon);
                weatherRepository.save(weather);  // update entry

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error updating weather for: " + weather.getCity());
            }
        }
    }

    private void updateWeather(Weather weather, double lat, double lon) throws JsonProcessingException {
        String weatherRequest = String.format(WEATHER_API_URL, lat, lon);
        String weatherJson = producerTemplate.requestBody("direct:fetchWeather", weatherRequest, String.class);
        Map weatherMap = mapper.readValue(weatherJson, Map.class);

        weather.setCountry((String) ((Map<String, Object>) weatherMap.get("sys")).getOrDefault("country", "Unknown"));
        weather.setCity((String) weatherMap.get("name"));
        weather.setTemperature((Double) ((Map<String, Object>) weatherMap.get("main")).get("temp"));
        weather.setDescription((String) ((List<Map<String, Object>>) weatherMap.get("weather")).get(0).get("description"));
    }

    public void checkAndSendWeatherAlerts(Weather weather, double lat, double lon) {
        String alertRequest = String.format(WEATHER_ALERTS_API_URL, lat, lon);

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(alertRequest, Map.class);
        List<Map<String, Object>> alerts = (List<Map<String, Object>>) response.get("alerts");

        if (alerts != null && !alerts.isEmpty()) {
            for (Map<String, Object> alert : alerts) {
                String title = (String) alert.get("title");
                String description = (String) alert.get("description");
                String alertMessage = "⚠\uFE0F Warning [" + weather.getCity() + ", " + weather.getCountry() + "]\n" + title + ": " + description;
                weatherWebSocketHandler.sendAlert(alertMessage);
            }
        }
    }

    public Map<String, Object> getCoordinates(String cityName, String stateCode, String countryCode) {
        String url = String.format(GEOCODING_API_URL, cityName, stateCode, countryCode);
        RestTemplate restTemplate = new RestTemplate();
        List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
        return response != null && !response.isEmpty() ? response.get(0) : null; // gets first result
    }

    public Weather getWeather(double lat, double lon) throws JsonProcessingException {
        String apiUrl = String.format(WEATHER_API_URL, lat, lon);
        String weatherJson = producerTemplate.requestBody("direct:fetchWeather", apiUrl, String.class);
        Map weatherMap = mapper.readValue(weatherJson, Map.class);

        String city = (String) weatherMap.get("name");
        String country = (String) ((Map<String, Object>) weatherMap.get("sys")).getOrDefault("country", "Unknown");
        double temperature = (Double) ((Map<String, Object>) weatherMap.get("main")).get("temp");
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) weatherMap.get("weather");
        String description = (String) weatherList.get(0).get("description");

        return new Weather(country, city, temperature, description, lat, lon);
    }
}
