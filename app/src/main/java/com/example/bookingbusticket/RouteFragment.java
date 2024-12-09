package com.example.bookingbusticket;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteFragment extends Fragment {

    private static final String GEOAPIFY_GEOCODE_URL = "https://api.geoapify.com/v1/geocode/search?text=%s&apiKey=e430066290e74b3982fd6db2f3c8b2a1";
    private static final String GEOAPIFY_ROUTING_URL = "https://api.geoapify.com/v1/routing?waypoints=%f,%f|%f,%f&mode=drive&apiKey=e430066290e74b3982fd6db2f3c8b2a1";

    private MapView mapView;
    private EditText searchBar;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean initialLocationFetched = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        // Initialize OSMDroid
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());

        // Initialize MapView
        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        // Initialize Search Bar and Button
        searchBar = view.findViewById(R.id.search_bar);
        Button searchButton = view.findViewById(R.id.search_button);

        // Initialize Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        // Default location (Dhaka) before fetching current location
        GeoPoint defaultLocation = new GeoPoint(23.8103, 90.4125); // Coordinates for Dhaka
        updateMap(defaultLocation);

        // Get current location on start
        fetchCurrentLocation();

        searchButton.setOnClickListener(v -> {
            String query = searchBar.getText().toString();
            if (!query.isEmpty()) {
                fetchLocation(query);  // Trigger location search
            }
        });

        return view;
    }

    // Fetch current location and update the map
    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && !initialLocationFetched) {
                                // Update map with current location
                                GeoPoint currentGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                                updateMap(currentGeoPoint);
                                initialLocationFetched = true; // Flag to prevent resetting on location fetch
                            }
                        }
                    });
        } else {
            // If permission is not granted, request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    // Fetch location from Geoapify Geocoding API
    private void fetchLocation(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            String url = String.format(GEOAPIFY_GEOCODE_URL, encodedLocation);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e("Geoapify", "API request failed");
                    showToast("Failed to fetch location.");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String jsonResponse = response.body().string();
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            JSONArray results = jsonObject.getJSONArray("features");

                            if (results.length() > 0) {
                                JSONObject feature = results.getJSONObject(0);
                                JSONObject geometry = feature.getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                // Update map UI with new location
                                updateMap(new GeoPoint(lat, lon));

                                // Example route request (starting from the searched location)
                                fetchRoute(new GeoPoint(lat, lon), new GeoPoint(23.8103, 90.4125));  // Example: Dhaka coordinates
                            } else {
                                showToast("Location not found!");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            showToast("Failed to parse response.");
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to encode location.");
        }
    }

    // Fetch route from Geoapify Routing API
    private void fetchRoute(GeoPoint start, GeoPoint end) {
        String url = String.format(GEOAPIFY_ROUTING_URL, start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("Geoapify", "Routing API request failed");
                showToast("Failed to fetch route.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray features = jsonObject.getJSONArray("features");

                        if (features.length() > 0) {
                            JSONObject route = features.getJSONObject(0);
                            JSONArray geometry = route.getJSONObject("geometry").getJSONArray("coordinates");
                            Polyline routePolyline = new Polyline(mapView);

                            // Convert coordinates into GeoPoint and add to polyline
                            for (int i = 0; i < geometry.length(); i++) {
                                JSONArray point = geometry.getJSONArray(i);
                                double lon = point.getDouble(0);
                                double lat = point.getDouble(1);
                                routePolyline.addPoint(new GeoPoint(lat, lon));
                            }

                            getActivity().runOnUiThread(() -> mapView.getOverlays().add(routePolyline));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showToast("Failed to parse route data.");
                    }
                }
            }
        });
    }

    // Update map with the new location
    private void updateMap(GeoPoint geoPoint) {
        getActivity().runOnUiThread(() -> {
            mapView.getController().setCenter(geoPoint);
            mapView.getController().setZoom(12.0);
            addMarker(geoPoint);
        });
    }

    // Add marker to the map
    private void addMarker(GeoPoint location) {
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Location");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    // Show toast message
    private void showToast(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the location
                fetchCurrentLocation();
            } else {
                // Permission denied
                showToast("Permission denied. Location access is needed.");
            }
        }
    }
}
