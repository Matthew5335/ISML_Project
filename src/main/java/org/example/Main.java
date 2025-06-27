package org.example;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.Scanner;

public class Main {
    private static AgentContainer container;
    private static AgentController weatherAgent;
    private static boolean isMonitoring = false;

    public static void main(String[] args) {
        String[][] cities = {
                {"Timisoara", "RO", "Timisoara"},
                {"Cluj", "RO", "Cluj-Napoca"},
                {"Bucuresti", "RO", "Bucharest"},
                {"Paris", "FR", "Paris"},
                {"London", "GB", "London"},
                {"Tokyo", "JP", "Tokyo"},
                {"Sydney", "AU", "Sydney"},
                {"Hamburg", "DE", "Hamburg"},
                {"California", "US", "Sacramento"},
                {"Brasilia", "BR", "Brasilia"}
        };
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (!isMonitoring) {
                showCityMenu(cities);
            }

            System.out.print(isMonitoring ?
                    "Monitoring active. Press ENTER to change city or 'stop' to exit: " :
                    "Enter city number (1-" + cities.length + "): ");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("stop")) {
                shutdown();
                break;
            }

            if (isMonitoring && input.isEmpty()) {
                stopCurrentMonitoring();
                isMonitoring = false;
                continue;
            }

            if (!isMonitoring) {
                try {
                    int cityChoice = Integer.parseInt(input) - 1;
                    if (cityChoice < 0 || cityChoice >= cities.length) {
                        System.out.println("Invalid city number. Please try again.");
                        continue;
                    }

                    System.out.print("Update interval (seconds): ");
                    int interval = Integer.parseInt(scanner.nextLine());

                    startMonitoring(cities[cityChoice][2] + "," + cities[cityChoice][1], interval);
                    isMonitoring = true;
                    System.out.println("Monitoring started...");
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid number.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        scanner.close();
    }

    private static void showCityMenu(String[][] cities) {
        System.out.println("\nSelect a city to monitor:");
        for (int i = 0; i < cities.length; i++) {
            System.out.printf("%2d. %-12s (%s)\n", i+1, cities[i][0], cities[i][1]);
        }
    }

    private static void startMonitoring(String city, int interval) throws StaleProxyException {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "false");

        if (container == null) {
            container = rt.createMainContainer(p);

            AgentController alertAgent = container.createNewAgent(
                    "AlertAgent",
                    "org.example.AlertAgent",
                    new Object[]{}
            );
            alertAgent.start();
        }

        stopCurrentMonitoring();

        weatherAgent = container.createNewAgent(
                "WeatherMonitorAgent",
                "org.example.WeatherMonitorAgent",
                new Object[]{city, interval}
        );
        weatherAgent.start();
    }

    private static void stopCurrentMonitoring() {
        if (weatherAgent != null) {
            try {
                weatherAgent.kill();
                System.out.println("Stopped current monitoring.");
            } catch (StaleProxyException e) {
                System.out.println("Warning: Couldn't cleanly stop previous monitoring");
            }
        }
    }

    private static void shutdown() {
        try {
            stopCurrentMonitoring();
            if (container != null) {
                container.kill();
            }
            System.out.println("System shut down successfully.");
        } catch (Exception e) {
            System.out.println("Error during shutdown: " + e.getMessage());
        }
    }
}