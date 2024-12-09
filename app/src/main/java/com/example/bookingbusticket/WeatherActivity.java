package com.example.bookingbusticket;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final String API_KEY = "2ae6b323bf66383f2965744de6c6f664"; // Replace with your OpenWeatherMap API key
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private FusedLocationProviderClient fusedLocationClient;
    private TextView cityNameTextView, temperatureTextView, humidityTextView, pressureTextView, conditionTextView,
            windSpeedTextView, windDirectionTextView, cloudinessTextView, sunriseTextView, sunsetTextView;
    private ProgressBar progressBar;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize views
        cityNameTextView = findViewById(R.id.cityName);
        temperatureTextView = findViewById(R.id.temperature);
        humidityTextView = findViewById(R.id.humidity);
        pressureTextView = findViewById(R.id.pressure);
        conditionTextView = findViewById(R.id.condition);
        windSpeedTextView = findViewById(R.id.windSpeed);
        windDirectionTextView = findViewById(R.id.windDirection);
        cloudinessTextView = findViewById(R.id.cloudness);
        sunriseTextView = findViewById(R.id.sunrise);
        sunsetTextView = findViewById(R.id.sunset);
        back = findViewById(R.id.buttonBack);
        progressBar = findViewById(R.id.progressBar);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Back button click listener
        back.setOnClickListener(v -> finish());

        // Request location permission if not already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted.");
            return;
        }

        // Show progress bar while loading
        progressBar.setVisibility(View.VISIBLE);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d(TAG, "Location obtained: " + latitude + ", " + longitude);

                // Fetch weather data for the obtained location
                fetchWeatherData(latitude, longitude);
            } else {
                Log.e(TAG, "Location is null.");
                Toast.makeText(WeatherActivity.this, "Failed to get location.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get location: " + e.getMessage());
            Toast.makeText(WeatherActivity.this, "Failed to get location.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch the location
                fetchCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchWeatherData(double latitude, double longitude) {
        // Construct the URL for the weather API request
        String urlString = BASE_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";

        new Thread(() -> {
            try {
                // Make a request to the weather API
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlString).openConnection();
                urlConnection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                runOnUiThread(() -> updateUI(jsonResponse));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data", e);
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "Failed to load weather data", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void updateUI(JSONObject weatherData) {
        try {
            // Get the main weather data
            JSONObject main = weatherData.getJSONObject("main");
            String temperature = main.getString("temp") + "°C";
            String humidity = main.getString("humidity") + "%";
            String pressure = main.getString("pressure") + " hPa";

            // Get the weather condition
            JSONObject weather = weatherData.getJSONArray("weather").getJSONObject(0);
            String condition = weather.getString("description");

            // Get the wind data
            JSONObject wind = weatherData.getJSONObject("wind");
            String windSpeed = wind.getString("speed") + " m/s";
            String windDirection = wind.getString("deg") + "°";

            // Get the cloudiness
            JSONObject clouds = weatherData.getJSONObject("clouds");
            String cloudiness = clouds.getString("all") + "%";

            // Get the sunrise and sunset times
            JSONObject sys = weatherData.getJSONObject("sys");
            String sunrise = convertUnixToTime(sys.getLong("sunrise"));
            String sunset = convertUnixToTime(sys.getLong("sunset"));

            // Get the city name
            String cityName = weatherData.getString("name");

            // Set the weather data to the UI
            cityNameTextView.setText("City: " + cityName);
            temperatureTextView.setText("Temperature: " + temperature);
            humidityTextView.setText("Humidity: " + humidity);
            pressureTextView.setText("Pressure: " + pressure);
            conditionTextView.setText("Condition: " + condition);
            windSpeedTextView.setText("Wind Speed: " + windSpeed);
            windDirectionTextView.setText("Wind Direction: " + windDirection);
            cloudinessTextView.setText("Cloudiness: " + cloudiness);
            sunriseTextView.setText("Sunrise: " + sunrise);
            sunsetTextView.setText("Sunset: " + sunset);

            // Hide progress bar after data is loaded
            progressBar.setVisibility(View.GONE);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing weather data", e);
            Toast.makeText(WeatherActivity.this, "Failed to parse weather data", Toast.LENGTH_SHORT).show();
        }
    }

    // Convert Unix timestamp to time
    private String convertUnixToTime(long unixTimestamp) {
        java.util.Date date = new java.util.Date(unixTimestamp * 1000L);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
        return sdf.format(date);
    }
}
