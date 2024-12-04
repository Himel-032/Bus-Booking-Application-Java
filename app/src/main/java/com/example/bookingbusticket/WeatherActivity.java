package com.example.bookingbusticket;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";
    private static final int LOCATION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView weatherTextView;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize views and location services
        weatherTextView = findViewById(R.id.weatherTxt);
        back = findViewById(R.id.buttonBack);
        back.setOnClickListener(v -> {
            finish();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    // Handle location permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            Log.e(TAG, "Location permission denied.");
            weatherTextView.setText("Location permission is required to display weather information.");
        }
    }

    // Fetch the user's current location
    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted.");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d(TAG, "Location: " + latitude + ", " + longitude);

                // Fetch weather data based on location
                fetchWeatherData(latitude, longitude);
            } else {
                Log.e(TAG, "Unable to retrieve location.");
                weatherTextView.setText("Unable to fetch location. Please ensure location services are enabled.");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            weatherTextView.setText("Error fetching location.");
        });
    }

    // Fetch weather data using OpenWeatherMap API
    private void fetchWeatherData(double latitude, double longitude) {
        String apiKey = "2ae6b323bf66383f2965744de6c6f664"; // Replace with your OpenWeatherMap API key
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse and display weather data
                        parseWeatherData(response);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                        weatherTextView.setText("Error parsing weather data.");
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.getMessage());
                    weatherTextView.setText("Error fetching weather data.");
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    // Parse the weather data and display it
    private void parseWeatherData(JSONObject response) throws JSONException {
        // Extract main weather information
        JSONObject main = response.getJSONObject("main");
        double temperature = main.getDouble("temp") - 273.15; // Convert Kelvin to Celsius
        int humidity = main.getInt("humidity");
        int pressure = main.getInt("pressure");

        // Extract weather conditions (description)
        JSONArray weatherArray = response.getJSONArray("weather");
        String description = weatherArray.getJSONObject(0).getString("description");

        // Extract wind information
        JSONObject wind = response.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");
        double windDeg = wind.getDouble("deg");

        // Extract cloudiness
        JSONObject clouds = response.getJSONObject("clouds");
        int cloudiness = clouds.getInt("all");

        // Extract city name
        String cityName = response.getString("name");

        // Extract sunrise and sunset times (convert from UNIX timestamp to human-readable time)
        JSONObject sys = response.getJSONObject("sys");
        long sunrise = sys.getLong("sunrise");
        long sunset = sys.getLong("sunset");
        String sunriseTime = convertUnixToTime(sunrise);
        String sunsetTime = convertUnixToTime(sunset);

        // Format the weather information
        String weatherInfo = "City: " + cityName + "\n" +
                "Temperature: " + String.format("%.2f", temperature) + "°C\n" +
                "Humidity: " + humidity + "%\n" +
                "Pressure: " + pressure + " hPa\n" +
                "Condition: " + description + "\n" +
                "Wind Speed: " + windSpeed + " m/s\n" +
                "Wind Direction: " + windDeg + "°\n" +
                "Cloudiness: " + cloudiness + "%\n" +
                "Sunrise: " + sunriseTime + "\n" +
                "Sunset: " + sunsetTime;

        // Display the weather info in the TextView
        weatherTextView.setText(weatherInfo);
        Log.d(TAG, "Weather Info: " + weatherInfo);
    }

    // Helper method to convert Unix timestamp to human-readable time
    private String convertUnixToTime(long unixTime) {
        // Convert Unix timestamp to milliseconds
        long milliseconds = unixTime * 1000;
        java.util.Date date = new java.util.Date(milliseconds);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        return sdf.format(date);
    }

}
