package com.example.bookingbusticket;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.Calendar;

public class MainActivity extends BaseActivity {

    Spinner fromSpinner,toSpinner;
    TextView depurtureDate,returnDate,userName;
    Calendar calendar;
    RadioGroup classRadioGroup;
    RadioButton ac,nonAc;
    Button searchButton;
    ChipNavigationBar navigationView;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.spinnerFrom);
        toSpinner = findViewById(R.id.spinnerTo);
        depurtureDate = findViewById(R.id.depurtureDateTxt);
        returnDate = findViewById(R.id.returnDateTxt);
        userName=findViewById(R.id.textViewUserName);
        searchButton=findViewById(R.id.searchBtn);
        ac=findViewById(R.id.radioButtonAC);
        nonAc=findViewById(R.id.radioButtonNonAC);
        navigationView = findViewById(R.id.bottomNavigationView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Get the email and extract username
            String email = user.getEmail();
            String username = email.split("@")[0].replaceAll("[^a-zA-Z]", "").toUpperCase();

            // Set username to TextView
            userName.setText("Welcome, " + username + "!");
        } else {
            // Handle the case where user is null (not logged in)
            userName.setText("User not logged in.");
        }


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.locations_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);


        calendar = Calendar.getInstance();
        depurtureDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(depurtureDate);
            }
        });
        returnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(returnDate);
            }
        });

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
            Intent intent=new Intent(MainActivity.this,SearchActivity.class);
            intent.putExtra("from", from);
            intent.putExtra("to", to);
            intent.putExtra("date", date);
            String classType;
            if(ac.isChecked())
            {
                classType="AC";
            }
            else
            {
                classType="Non AC";
            }
            intent.putExtra("classType",classType);
            startActivity(intent);
        });

        // navigation work
        navigationView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                if(i==R.id.profile){
                    loadFragment(new ProfileFragment());
                }else if(i==R.id.home){
                    findViewById(R.id.scrollView2).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragmentContainer).setVisibility(View.GONE);

                }
            }
        });





    }

    private void loadFragment(Fragment fragment) {
        // visible frameLayout view
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        // hide scrollview
        findViewById(R.id.scrollView2).setVisibility(View.GONE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    ///////////
    private void openDatePicker(final TextView dateTextView)
    {
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH);
        int day=calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog=new DatePickerDialog(
                MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
               // selectedMonth +=1;
                String selectedDate=selectedDay+" "+months[selectedMonth]+", "+selectedYear;
                dateTextView.setText(selectedDate);
            }
        },
                year,month,day
        );
        datePickerDialog.show();
    }
}