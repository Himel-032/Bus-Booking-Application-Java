package com.example.bookingbusticket;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LAST_NOTIFICATION_TIMESTAMP_KEY = "lastNotificationTimestamp";
    private long lastNotificationTimestamp;


    private static final String CHANNEL_ID = "BusesNotifications";
    private ImageView notificationsImg;
    private ArrayList<String> notifications = new ArrayList<>();
    private long appLaunchTime;

    // UI components
    Spinner fromSpinner, toSpinner;
    TextView depurtureDate, returnDate, userName;
    Calendar calendar;
    RadioGroup classRadioGroup;
    RadioButton ac, nonAc;
    Button searchButton;
    ChipNavigationBar navigationView;
    DatabaseReference databaseReference;
    ImageView weather;

    @Override
    protected void onStart() {
        super.onStart();

        // Clear notifications when app starts
        notifications.clear();

        // Track the app launch time
        appLaunchTime = System.currentTimeMillis();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        lastNotificationTimestamp = sharedPreferences.getLong(LAST_NOTIFICATION_TIMESTAMP_KEY, 0);



        // Call method to remove past dates from the database (if needed)
        deletePastDates();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        fromSpinner = findViewById(R.id.spinnerFrom);
        toSpinner = findViewById(R.id.spinnerTo);
        depurtureDate = findViewById(R.id.depurtureDateTxt);
        returnDate = findViewById(R.id.returnDateTxt);
        userName = findViewById(R.id.textViewUserName);
        searchButton = findViewById(R.id.searchBtn);
        ac = findViewById(R.id.radioButtonAC);
        nonAc = findViewById(R.id.radioButtonNonAC);
        navigationView = findViewById(R.id.bottomNavigationView);
        notificationsImg = findViewById(R.id.notificationsImg);
        weather = findViewById(R.id.weatherImg);

        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,WeatherActivity.class);
                startActivity(i);
            }
        });

        // Create notification channel
        createNotificationChannel();

        // Set up Firebase listener for new buses added
        FirebaseDatabase.getInstance().getReference("Buses")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                        long currentTime = System.currentTimeMillis();

                        // Only show notifications for buses added after the last notification timestamp
                        if (currentTime >= appLaunchTime && currentTime > lastNotificationTimestamp) {
                            String busName = snapshot.child("busCompanyName").getValue(String.class);
                            String route = snapshot.child("from").getValue(String.class) + " to " +
                                    snapshot.child("to").getValue(String.class);

                            String message = "New Bus: " + busName + " (" + route + ")";
                            notifications.add(message); // Save notification for later display

                            // Change the notification icon's background to indicate a new notification
                            notificationsImg.setImageResource(R.drawable.bell_icon); // Update to an active icon

                            showNotification("New Bus Added", message);

                            // Update the last notification timestamp in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong(LAST_NOTIFICATION_TIMESTAMP_KEY, currentTime);
                            editor.apply();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });


        // When the notification icon is clicked, show the notifications
        notificationsImg.setOnClickListener(view -> {
            showNotificationsDialog();

            // After reading the notifications, reset the icon
            notificationsImg.setImageResource(R.drawable.bell_icon); // Revert to default icon
        });

        // Set up user details if logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String username = email.split("@")[0].replaceAll("[^a-zA-Z]", "").toUpperCase();
            userName.setText("Welcome, " + username + "!");
        } else {
            userName.setText("User not logged in.");
        }

        // Setup Spinners for locations
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.locations_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        // Date Picker for departure and return dates
        calendar = Calendar.getInstance();
        depurtureDate.setOnClickListener(v -> openDatePicker(depurtureDate));
        returnDate.setOnClickListener(v -> openDatePicker(returnDate));

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String from = fromSpinner.getSelectedItem().toString();
            String to = toSpinner.getSelectedItem().toString();
            String date = depurtureDate.getText().toString();

            if (date == null || date.isEmpty()) {
                depurtureDate.setError("Date is required");
                depurtureDate.requestFocus();
                Toast.makeText(MainActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("from", from);
            intent.putExtra("to", to);
            intent.putExtra("date", date);
            String classType = ac.isChecked() ? "AC" : "Non AC";
            intent.putExtra("classType", classType);
            startActivity(intent);
        });

        // Handle bottom navigation
        navigationView.setOnItemSelectedListener(i -> {
            if (i == R.id.profile) {
                loadFragment(new ProfileFragment());
            } else if (i == R.id.home) {
                findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
            } else if (i == R.id.bookmark) {
                loadFragment(new BookmarkFragment());
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollView2).setVisibility(View.GONE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    ///////////
    private void openDatePicker(final TextView dateTextView) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            String selectedDate = selectedDay + " " + months[selectedMonth] + ", " + selectedYear;
            dateTextView.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void deletePastDates() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Reference to the BookedSeats node
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("BookedSeats");

        // Query the database for all bus variations (bus IDs and dates)
        databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate over all bus names
                for (DataSnapshot busSnapshot : dataSnapshot.getChildren()) {
                    // Iterate over all dates for the bus
                    for (DataSnapshot dateSnapshot : busSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();  // Date in the format "dd MMM, yyyy"

                        // Compare dates
                        if (isPastDate(date, currentDate)) {
                            // Remove the past date from the database
                            dateSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error deleting past dates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPastDate(String dateStr, String currentDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            Date date = dateFormat.parse(dateStr);
            Date current = dateFormat.parse(currentDate);
            return date != null && date.before(current);  // Return true if the date is before current date
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Buses Notifications";
            String description = "Notifications for new buses added";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.bell_icon) // Replace with your app's icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void showNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notifications");

        if (notifications.isEmpty()) {
            builder.setMessage("No new notifications.");
        } else {
            StringBuilder message = new StringBuilder();
            for (String notification : notifications) {
                message.append(notification).append("\n\n");
            }
            builder.setMessage(message.toString());
        }

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
