package com.ev.Services;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    public boolean isBadWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;

        try {
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(url, String.class);

            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray weatherArray = obj.getJSONArray("weather");
            String mainCondition = weatherArray.getJSONObject(0).getString("main");

            return mainCondition.equalsIgnoreCase("Rain") ||
                    mainCondition.equalsIgnoreCase("Thunderstorm") ||
                    mainCondition.equalsIgnoreCase("Snow");
        } catch (Exception e) {
            e.printStackTrace();
            return false; // fallback: assume weather is good
        }
    }
}
