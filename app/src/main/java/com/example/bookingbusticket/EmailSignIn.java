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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailSignIn extends AppCompatActivity {

    EditText userEmailLogIn;
    EditText userEmailPinLogIn;
    Button emailSignIn;
    Button emailSignUp;
    Button forgotPassword;
    ImageView back;

    FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_in);
        userEmailLogIn=findViewById(R.id.editTextEmailSignIn);
        userEmailPinLogIn=findViewById(R.id.editTextPasswordEmailSignIn);
        emailSignIn=findViewById(R.id.buttonEmailSignIn);
        emailSignUp=findViewById(R.id.buttonEmailSignUp);
        forgotPassword=findViewById(R.id.buttonMailPasswordReset);
        back=findViewById(R.id.backBtn);
        back.setOnClickListener(v -> finish());

        emailSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail=userEmailLogIn.getText().toString();
                if(userEmail.isEmpty()){
                    userEmailLogIn.setError("Email account is required");
                    userEmailLogIn.requestFocus();
                    return;
                }
                String userPassword=userEmailPinLogIn.getText().toString();
                if(userPassword.isEmpty()){
                    userEmailPinLogIn.setError("Password is required");
                    userEmailPinLogIn.requestFocus();
                    return;
                }
                signInFirebase(userEmail,userPassword);


            }
        });

        emailSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EmailSignIn.this,EmailSignUp.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(EmailSignIn.this,ForgetMail.class);
                startActivity(intent);
            }
        });


    }

    public void signInFirebase(String userEmail,String userPassword){
        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Email is verified, grant access
                                Toast.makeText(EmailSignIn.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(EmailSignIn.this, MainActivity.class);
                                startActivity(intent);
                             //   finish();
                            } else {
                                // Email not verified, prompt user to verify
                                Toast.makeText(EmailSignIn.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                auth.signOut();  // Sign out unverified user
                            }
                        } else {
                            // Authentication failed
                            Toast.makeText(EmailSignIn.this, "Login failed: " + "Wrong email or password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null){
            Intent intent=new Intent(EmailSignIn.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


}