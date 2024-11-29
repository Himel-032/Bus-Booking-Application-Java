package com.example.bookingbusticket;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingbusticket.Adapter.TripAdapter;
import com.example.bookingbusticket.Model.Trip;
import com.example.bookingbusticket.databinding.ActivityIntroBinding;
import com.example.bookingbusticket.databinding.ActivitySearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SearchActivity extends BaseActivity {
    private ActivitySearchBinding binding;
    private String from,to,date,classType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.layout.activity_search);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getIntentExtra();
        initList();
        setVarable();

    }

    private void setVarable() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initList() {

        DatabaseReference myRef=database.getReference("Buses");
        ArrayList<Trip> list=new ArrayList<>();

        Log.d("SearchActivity", "Querying buses from Firebase...");

        Query query=myRef.orderByChild("from").equalTo(from);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        try {
                        Trip trip = issue.getValue(Trip.class);
                      //  String tripDate=trip.getDate();
                            // chatgpt
                            trip.setDate(date);

                            SimpleDateFormat sdf=new SimpleDateFormat("dd MMM, yyyy");
                            Date parsedDate = sdf.parse(date);
                          //  Date parsedTripDate=sdf.parse(tripDate);
                           // SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy");

                            // Set the time zone to UTC or a specific time zone
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Example: "GMT", "UTC", or your desired zone

                            // Get the current date
                            Date currentDate = new Date();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(currentDate);
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            Date truncatedCurrentDate = calendar.getTime();

                            // Format the current date
                            String formattedDate = sdf.format(truncatedCurrentDate);
                            Date parsedformattedDate=sdf.parse(formattedDate);

                            boolean isDateValid=true;
                            if(parsedDate.before(parsedformattedDate)){
                                isDateValid=false;
                            }


                        if(trip.getTo().equals(to) && trip.getClassSeat().equals(classType) && isDateValid){
                            list.add(trip);
                        }
                        // To filter with date ,uncomment below lines and comment to line
                       /* if (trip.getTo().equals(to) && trip.getDate().equals(date)) {
                            list.add(trip);
                        }*/
                        } catch (Exception e) {
                            Log.e("SearchActivity", "Error parsing trip: " + e.getMessage());
                        }
                    }
                    if (!list.isEmpty()) {
                        binding.searchView.setLayoutManager(new LinearLayoutManager(SearchActivity.this, LinearLayoutManager.VERTICAL, false));
                        binding.searchView.setAdapter(new TripAdapter(list));
                    } else {
                        Toast.makeText(SearchActivity.this, "No trips found,", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(SearchActivity.this, "No trips found for this location", Toast.LENGTH_SHORT).show();
                    }
                binding.progressBarSearch.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchActivity", "Firebase query cancelled: " + error.getMessage());
            }
        });
    }

    private void getIntentExtra() {
        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date = getIntent().getStringExtra("date");
        classType = getIntent().getStringExtra("classType");

        Log.d("SearchActivity", "From: " + from + ", To: " + to + ", Date: " + date);
    }
}