package com.example.bookingbusticket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteFragment extends Fragment {

    private static final String ROUTE_API_URL = "https://api.geoapify.com/v1/routing?waypoints=50.96209827745463%2C4.414458883409225%7C50.429137079078345%2C5.00088081232559&mode=drive&apiKey=e430066290e74b3982fd6db2f3c8b2a1";
    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        // Configure OSMDroid
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Initialize MapView
        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        // Set initial map center
        GeoPoint startPoint = new GeoPoint(50.9621, 4.4145); // Starting point
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(10.0);

        // Fetch and display route
        fetchRouteAndDisplay();

        return view;
    }

    private void fetchRouteAndDisplay() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(ROUTE_API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray coordinates = jsonObject.getJSONArray("features")
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONArray("coordinates");

                        // Parse route points
                        List<GeoPoint> routePoints = new ArrayList<>();
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray point = coordinates.getJSONArray(i);
                            double lon = point.getDouble(0);
                            double lat = point.getDouble(1);
                            routePoints.add(new GeoPoint(lat, lon));
                        }

                        // Display route on map
                        displayRoute(routePoints);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void displayRoute(List<GeoPoint> routePoints) {
        getActivity().runOnUiThread(() -> {
            // Draw polyline for the route
            Polyline polyline = new Polyline();
            polyline.setPoints(routePoints);
            mapView.getOverlayManager().add(polyline);

            // Add markers for start and end points
            if (!routePoints.isEmpty()) {
                addMarker(routePoints.get(0), "Start Point");
                addMarker(routePoints.get(routePoints.size() - 1), "End Point");
            }
        });
    }

    private void addMarker(GeoPoint location, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
