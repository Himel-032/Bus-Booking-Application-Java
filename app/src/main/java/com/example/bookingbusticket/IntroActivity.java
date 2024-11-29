package com.example.bookingbusticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookingbusticket.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {
    Button getStarted,haveAnAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        getStarted=findViewById(R.id.startBtn);
        haveAnAccount=findViewById(R.id.logInBtn);

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(IntroActivity.this, EmailSignUp.class);
                startActivity(i);

            }
        });
        haveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(IntroActivity.this, EmailSignIn.class);
                startActivity(i);

            }
        });

    }
}