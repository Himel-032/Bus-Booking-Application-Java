package com.example.bookingbusticket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class RouteFragment extends Fragment {

    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        // Initialize OSMDroid
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());

        // Initialize MapView
        mapView = view.findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);  // Allow pinch to zoom and other gestures

        // Set zoom and center the map
        GeoPoint startPoint = new GeoPoint(23.8103, 90.4125);  // Example: Dhaka, Bangladesh
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(12.0);

        // Add a marker at the start point
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setTitle("Dhaka");
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);

        return view;
    }
}
