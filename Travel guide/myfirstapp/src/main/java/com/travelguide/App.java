package com.travelguide;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

public class App {

    // Trust all certificates (development only, insecure)
    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0]; // Return an empty array, not null
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    // Replace API_KEY with your OpenRouteService API key
    private static final String API_KEY = "5b3ce3597851110001cf62489b7117417287452f8920a13471ffcc00";

    public static void main(String[] args) {
        try {
            // Example locations (use addresses or lat,lng values)
            List<String> locations = Arrays.asList(
                "New York, NY", "Boston, MA", "Philadelphia, PA", "Washington, DC"
            );
            FindLoc("New York");

            // Fetch and print distances between sequential cities
            for (int i = 0; i < locations.size() - 1; i++) {
                String origin = locations.get(i);
                String destination = locations.get(i + 1);
                double distance = getDistance(origin, destination);
                System.out.println("Distance from " + origin + " to " + destination + " is: " + distance + " km");
            }

        } catch (Exception e) {
            e.printStackTrace();  // Improved exception handling
        }
    }

    // Method to fetch the distance between two locations from OpenRouteService API
    public static double getDistance(String origin, String destination) throws Exception {
        // Convert city names to latitude,longitude format
        String originCoordinates = getCoordinatesForLocation(origin);
        String destinationCoordinates = getCoordinatesForLocation(destination);

        String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + API_KEY + "&start=" + originCoordinates + "&end=" + destinationCoordinates;

        OkHttpClient client = getUnsafeOkHttpClient(); // Use our unsafe client
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("Failed to fetch distance: " + response);
        }

        String responseData = response.body().string();
        return parseDistance(responseData);
    }

    // Convert city name to lat,long (for this example, return hardcoded values)
    private static String getCoordinatesForLocation(String location) {
        switch (location) {
            case "New York, NY":
                return "40.7128,-74.0060";
            case "Boston, MA":
                return "42.3601,-71.0589";
            case "Philadelphia, PA":
                return "39.9526,-75.1652";
            case "Washington, DC":
                return "38.9072,-77.0369";
            default:
                return "0.0,0.0"; // Default in case of unknown location
        }
    }

    // Parse the response to extract the distance in kilometers
    private static double parseDistance(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray features = jsonObject.getJSONArray("features");

        // Get the first feature object
        JSONObject feature = features.getJSONObject(0);

        // Navigate to the 'properties' object within the feature
        JSONObject properties = feature.getJSONObject("properties");

        // Extract the 'summary' object from 'properties'
        JSONObject summary = properties.getJSONObject("summary");

        // Get the distance from the 'summary' object
        double distance = summary.getDouble("distance");

        // Convert distance from meters to kilometers
        return distance / 1000.0;
    }

    public static void FindLoc(String loc) {
        String url = "https://nominatim.openstreetmap.org/search?q=" + loc.replace(" ", "+") + "&format=json&addressdetails=1&limit=1";

        // Initialize OkHttpClient
        OkHttpClient client = getUnsafeOkHttpClient();

        try {
            // Create HTTP request
            Request request = new Request.Builder().url(url).build();

            // Execute the request
            Response response = client.newCall(request).execute();

            // Read the response
            String responseBody = response.body().string();

            // Parse the JSON response
            System.out.println(responseBody);
            JSONArray jsonArray = new JSONArray(responseBody);

            if (jsonArray.length() > 0) {
                JSONObject locationData = jsonArray.getJSONObject(0);
                double lat = locationData.getDouble("lat");
                double lon = locationData.getDouble("lon");

                // Print latitude and longitude
                System.out.println("Latitude: " + lat);
                System.out.println("Longitude: " + lon);
            } else {
                System.out.println("No results found for the location: " + loc);
            }

        } catch (Exception e) {
            e.printStackTrace();  // Improved exception handling
        }
    }
}
