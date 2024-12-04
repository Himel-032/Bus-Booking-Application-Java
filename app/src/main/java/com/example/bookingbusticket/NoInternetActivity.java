package com.example.bookingbusticket;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class NoInternetActivity extends AppCompatActivity {

    private LottieAnimationView noInternetAnimation;
    private Button retryButton;
    private TextView noInternetTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);


        noInternetAnimation = findViewById(R.id.no_internet_connection);
        retryButton = findViewById(R.id.retry_button);
        noInternetTextView = findViewById(R.id.no_internet_connection_textview);


        retryButton.setOnClickListener(v -> {
            if (isInternetAvailable()) {

                startActivity(new Intent(NoInternetActivity.this, MainActivity.class));
                finish();
            } else {

                noInternetTextView.setText("Still No Internet Connection");
            }
        });


        if (isInternetAvailable()) {
            startActivity(new Intent(NoInternetActivity.this, MainActivity.class));
            finish();
        }
    }

    // Method to check internet connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
