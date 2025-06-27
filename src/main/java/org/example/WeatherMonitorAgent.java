package org.example;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class WeatherMonitorAgent extends Agent {
    private volatile boolean running = true;
    private String cityWithCountry;
    private int updateInterval;
    private final String apiKey = "7282264a1403030a05e99b30173d9498";

    @Override
    protected void setup() {
        Object[] args = getArguments();
        this.cityWithCountry = (String) args[0];
        this.updateInterval = (Integer) args[1] * 1000;

        String displayCity = cityWithCountry.split(",")[0];
        System.out.printf("[WeatherMonitorAgent] Monitoring %s (%s) every %ds\n",
                displayCity,
                cityWithCountry.split(",")[1],
                updateInterval / 1000);

        addBehaviour(new TickerBehaviour(this, updateInterval) {
            @Override
            protected void onTick() {
                if (!running) {
                    stop();
                    return;
                }

                try {
                    WeatherData data = fetchWeather(cityWithCountry);
                    System.out.printf("[Weather] %s: %.1f°C, %.0f%% humidity\n",
                            displayCity, data.temp, data.humidity);

                    if (shouldTriggerAlert(data)) {
                        sendAlert(data, displayCity);
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                }
            }
        });
    }

    private WeatherData fetchWeather(String cityWithCountry) throws Exception {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric",
                cityWithCountry, apiKey);

        //System.out.println("[DEBUG] Calling URL: " + url.replace(apiKey, "***"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

       // System.out.println("[DEBUG] Response Status: " + response.statusCode());
        if (response.statusCode() != 200) {
            JSONObject error = new JSONObject(response.body());
            throw new RuntimeException("API Error: " + error.getString("message"));
        }

        JSONObject json = new JSONObject(response.body());
        JSONObject main = json.getJSONObject("main");

        return new WeatherData(
                main.getDouble("temp"),
                main.getDouble("humidity")
        );
    }

    private boolean shouldTriggerAlert(WeatherData data) {
        return data.temp > 35 || data.humidity > 80;
    }

    private void sendAlert(WeatherData data, String displayCity) {
        ACLMessage alert = new ACLMessage(ACLMessage.INFORM);
        alert.addReceiver(getAID("AlertAgent"));
        alert.setContent(String.format(
                "ALERT! %s: %.1f°C, %.0f%% humidity",
                displayCity, data.temp, data.humidity));
        send(alert);
        //System.out.println("[DEBUG] Alert sent to AlertAgent");
    }

    @Override
    protected void takeDown() {
        running = false;
        System.out.println("Weather monitoring stopped.");
    }

    private static class WeatherData {
        double temp;
        double humidity;
        public WeatherData(double temp, double humidity) {
            this.temp = temp;
            this.humidity = humidity;
        }
    }
}