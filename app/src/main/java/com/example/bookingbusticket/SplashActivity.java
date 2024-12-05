package com.example.bookingbusticket;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottieAnimation=findViewById(R.id.lottieAnimationView);
        lottieAnimation.setAnimation("animation_splash.json");
        lottieAnimation.setRepeatCount(0);
        lottieAnimation.playAnimation();
        lottieAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);

             /*   if (isInternetAvailable()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, NoInternetActivity.class));
                }
                finish();

              */

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && isInternetAvailable()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));

                } else if(user==null && isInternetAvailable()) {
                    startActivity(new Intent(SplashActivity.this, IntroActivity.class));

                }else{
                    startActivity(new Intent(SplashActivity.this, NoInternetActivity.class));

                }
                finish();


            }
        });






        // Delay to show splash screen before navigating to the next activity
     /*   new Handler().postDelayed(() -> {
            // Check if internet is available before moving to the next activity
            if (isInternetAvailable()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, NoInternetActivity.class));
            }
            finish();
        }, 2000); // 2-second delay
        */

    }
    // Method to check internet connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return cm.getActiveNetwork() != null;
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }

}
