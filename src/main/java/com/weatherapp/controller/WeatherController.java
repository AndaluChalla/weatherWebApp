package com.weatherapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class WeatherController {

    private final WebClient.Builder webClientBuilder;

    @Value("${weather.api.key}")
    private String apiKey;  // Your OpenWeatherMap API key

    public WeatherController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam String city, Model model) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

        // Make the API call and retrieve the response
        String response = webClientBuilder.baseUrl(url)
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocking for simplicity here, you can modify it for async

        // Check if the response is null or contains an error
        if (response != null && !response.isEmpty()) {
            JsonNode jsonResponse = parseJson(response);  // Parse JSON response
            if (jsonResponse != null && jsonResponse.has("main") && jsonResponse.has("weather")) {
                model.addAttribute("city", city);
                model.addAttribute("temperature", jsonResponse.path("main").path("temp").asText());
                model.addAttribute("description", jsonResponse.path("weather").get(0).path("description").asText());
                model.addAttribute("iconUrl", getIconUrl(jsonResponse));  // Get the icon URL
            } else {
                model.addAttribute("error", "City not found or API error.");
            }
        } else {
            model.addAttribute("error", "City not found or API error.");
        }

        return "weather";  // Return the Thymeleaf template name
    }

    // Helper method to parse the JSON response
    private JsonNode parseJson(String response) {
        try {
            return new ObjectMapper().readTree(response);  // Parse JSON response
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getIconUrl(JsonNode jsonResponse) {
        // Get icon code from JSON and construct the icon URL
        String iconCode = jsonResponse.path("weather").get(0).path("icon").asText();
        return "http://openweathermap.org/img/wn/" + iconCode + ".png";  // Build icon URL
    }
}
