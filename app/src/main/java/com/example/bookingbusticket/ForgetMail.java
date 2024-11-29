package com.example.bookingbusticket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgetMail extends AppCompatActivity {

    Button reset;
    EditText resetMail;
    ImageView back;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_mail);
       // Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        reset=findViewById(R.id.buttonResetMailPassword);
        resetMail=findViewById(R.id.editTextEmailReset);
        back=findViewById(R.id.backBtn);
        back.setOnClickListener(v -> finish());

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail= resetMail.getText().toString();
                if(userEmail.isEmpty()){
                    resetMail.setError("Email account is required");
                    resetMail.requestFocus();
                    return;
                }
                auth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ForgetMail.this,"We sent an email to reset your password",Toast.LENGTH_LONG).show();
                                    Intent i=new Intent(ForgetMail.this,EmailSignIn.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
                finish();
            }
        });

    }

}