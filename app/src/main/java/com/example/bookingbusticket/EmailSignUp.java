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

import java.util.Objects;

public class EmailSignUp extends AppCompatActivity {

    EditText mail;
    EditText password;
    Button signUp;
    ImageView back;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_sign_up);

       // Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mail=findViewById(R.id.EditTextUserMail);
        password=findViewById(R.id.EditTextUserPassword);
        signUp=findViewById(R.id.buttonMailSignUp);
        back=findViewById(R.id.backBtn);
        back.setOnClickListener(v -> finish());
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail=mail.getText().toString();
                if(userEmail.isEmpty()){
                    mail.setError("Email account is required");
                    mail.requestFocus();
                    return;
                }
                String userPassword=password.getText().toString();
                if(userPassword.isEmpty()){
                    password.setError("Password is required");
                    password.requestFocus();
                    return;
                }
                signUpFirebase(userEmail,userPassword);
            }
        });

    }

    public void signUpFirebase(String userEmail,String userPassword){
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Toast.makeText(EmailSignUp.this,"Your account has been created.",Toast.LENGTH_SHORT).show();
                            FirebaseUser user = auth.getCurrentUser();
                            sendEmailVerification(user);
                            //    Intent intent=new Intent(EmailSignUp.this,EmailSignIn.class);
                            //   startActivity(intent);
                            //  finish();

                        } else {
                            // If sign in fails, display a message to the user.
                           // Toast.makeText(EmailSignUp.this,"PIN must be 6 digits or more.",Toast.LENGTH_SHORT).show();
                            String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                            if (Objects.requireNonNull(task.getException()).getMessage().contains("The email address is already in use")) {
                                Toast.makeText(EmailSignUp.this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EmailSignUp.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Verification email sent to " + user.getEmail() + ". Verify email to Log in", Toast.LENGTH_LONG).show();

                                auth.signOut();

                                // Redirect to login screen or prompt user to verify
                                Intent intent = new Intent(EmailSignUp.this, EmailSignIn.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}