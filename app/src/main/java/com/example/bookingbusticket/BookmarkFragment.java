package com.example.bookingbusticket;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bookingbusticket.Adapter.BookmarkAdapter;
import com.example.bookingbusticket.Model.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class BookmarkFragment extends Fragment {


    private RecyclerView recyclerView;
    private BookmarkAdapter adapter;
    private ArrayList<Trip> tripList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        recyclerView = view.findViewById(R.id.historyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tripList = new ArrayList<>();
        adapter = new BookmarkAdapter(tripList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        fetchTrips();

        return view;
    }

    private void fetchTrips() {
         String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .collection("tickets")  // Assuming trips are stored in a sub-collection like 'bookmarkedTrips'
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        tripList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Trip trip = document.toObject(Trip.class);  // Convert Firestore document to Trip object
                            tripList.add(trip);  // Add the trip to the list
                        }
                        adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                });
    }
}