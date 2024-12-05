package com.example.bookingbusticket;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String LAST_NOTIFICATION_TIMESTAMP_KEY = "lastNotificationTimestamp";
    private long lastNotificationTimestamp;


    private static final String CHANNEL_ID = "BusesNotifications";
    private ImageView notificationsImg;
    private ArrayList<String> notifications = new ArrayList<>();
    private long appLaunchTime;
    ImageView showFlyerBtn;

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

        showFlyerBtn=findViewById(R.id.menuImg);
        showFlyerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFlyer();
            }
        });

        weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,WeatherActivity.class);
                startActivity(i);
            }
        });

        // Create notification channel
       // createNotificationChannel();
/*
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
*/

        // When the notification icon is clicked, show the notifications
        notificationsImg.setOnClickListener(view -> {
            showUpcomingTripsDialog();

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
/*
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

 */
    private void showUpcomingTripsDialog() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Define date and time format
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault());
        Date now = new Date(); // Current date and time
        String currentDateTime = dateTimeFormat.format(now);

        firestore.collection("users").document(uid).collection("tickets")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> upcomingTrips = new ArrayList<>();

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            String tripDate = snapshot.getString("date"); // e.g., "16 Nov, 2024"
                            String departureTime = snapshot.getString("departureTime"); // e.g., "16:30"

                            if (tripDate != null && departureTime != null) {
                                try {
                                    // Combine date and time for comparison
                                    String tripDateTime = tripDate + " " + departureTime;

                                    // Parse both dates for comparison
                                    Date tripDateTimeParsed = dateTimeFormat.parse(tripDateTime);
                                    Date currentDateTimeParsed = dateTimeFormat.parse(currentDateTime);

                                    if (tripDateTimeParsed != null && currentDateTimeParsed != null) {
                                        // Check if the trip is upcoming
                                        if (tripDateTimeParsed.compareTo(currentDateTimeParsed) >= 0) {
                                            String from = snapshot.getString("from");
                                            String to = snapshot.getString("to");

                                            upcomingTrips.add(from + " to " + to + " at " + departureTime + " on " + tripDate);
                                        }
                                    }
                                } catch (ParseException e) {
                                    Toast.makeText(this, "Error parsing date/time: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        if (upcomingTrips.isEmpty()) {
                            Toast.makeText(this, "No upcoming trips.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Show trips in a dialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Upcoming Trips");

                            StringBuilder message = new StringBuilder();
                            for (String trip : upcomingTrips) {
                                message.append(trip).append("\n\n");
                            }

                            builder.setMessage(message.toString());
                            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                            builder.show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch trips.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showFlyer() {
       // View flyerView = inflater.inflate(R.layout.flyer_layout, null);

        View flyerView = LayoutInflater.from(this).inflate(R.layout.flyer_layout, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;  // Get screen height

        // 70% of the screen width
        int flyerWidth = (int) (screenWidth * 0.7);  // 70% of screen width
        int flyerHeight = screenHeight;  // Use the full screen height

        // Inflate the flyer layout
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);


        // Create PopupWindow for the flyer
        PopupWindow flyerPopup = new PopupWindow(flyerView, flyerWidth, flyerHeight);

        // Start slide-in animation for the flyer
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left);
        flyerView.startAnimation(slideIn);

        // Set background drawable for PopupWindow to allow outside touch detection
        flyerPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set the PopupWindow properties
        flyerPopup.setOutsideTouchable(true);
        flyerPopup.setFocusable(true);

        // Show the PopupWindow (flyer) at the left side
        flyerPopup.showAtLocation(findViewById(R.id.main), Gravity.LEFT, 0, 0);

        // Set touch interceptor to dismiss the flyer when clicking outside
        flyerPopup.setTouchInterceptor((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                flyerPopup.dismiss();
                return true;
            }
            return false;
        });



        TextView  userName = flyerView.findViewById(R.id.nameTxtFlyer);

        Button logoutButton = flyerView.findViewById(R.id.logOutBtnFlyer);
        Button deleteButton = flyerView.findViewById(R.id.deleteBtnFlyer);
        Button forgotButton=flyerView.findViewById(R.id.resetBtnFlyer);
         firestore = FirebaseFirestore.getInstance();
         auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String mail = user.getEmail();

            String name = mail.split("@")[0].replaceAll("[^a-zA-Z]", "").toUpperCase();

            userName.setText(name);
        } else {
            userName.setText("No user logged in");
        }

        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(MainActivity.this,ForgetMail.class);
                startActivity(i);
            }
        });

        // Handle Logout Button Click
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, EmailSignIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });



        // Handle Delete Account Button Click
       deleteButton.setOnClickListener(v -> {
            FirebaseUser userToDelete = auth.getCurrentUser();

            if (userToDelete != null) {
                // Step 1: Ask the user for their password to confirm
                EditText passwordEditText = new EditText(MainActivity.this);
                passwordEditText.setHint("Enter your password");
                passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Password")
                        .setMessage("You will loose all the previous booking history.Please enter your password to delete your account.")
                        .setView(passwordEditText)
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            String password = passwordEditText.getText().toString();
                            if (!password.isEmpty()) {
                                // Step 2: Reauthenticate the user with their password
                                reauthenticateAndDelete(userToDelete, password);
                            } else {
                                Toast.makeText(MainActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
        });


    }
    private void reauthenticateAndDelete(FirebaseUser user, String password) {
        // Re-authenticate the user
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                        // Step 3: Delete the user's data from Firestore
                        deleteUserDataFromFirestore(uid);

                        // Step 4: Delete the user account after successful data deletion
                        user.delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        // After account deletion, sign out and redirect to login
                                        auth.signOut();
                                        Intent intent = new Intent(MainActivity.this, EmailSignIn.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to delete user account", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUserDataFromFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        // Delete main user document
        batch.delete(db.collection("users").document(userId));

        // Delete all documents in "tickets" subcollection
        db.collection("users").document(userId).collection("tickets")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot ticket : task.getResult()) {
                            batch.delete(ticket.getReference());
                        }

                        // Commit the batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(), "User and all tickets deleted successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreDelete", "Batch commit failed: " + e.getMessage());
                                    Toast.makeText(getApplicationContext(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("FirestoreDelete", "Failed to retrieve tickets: " + task.getException());
                        Toast.makeText(getApplicationContext(), "Error retrieving tickets", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDelete", "Error accessing tickets: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Error accessing tickets", Toast.LENGTH_SHORT).show();
                });
    }



}
