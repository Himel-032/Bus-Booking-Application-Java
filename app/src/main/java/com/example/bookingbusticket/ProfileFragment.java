package com.example.bookingbusticket;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class ProfileFragment extends Fragment {

    private TextView userMail, userName;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userMail = view.findViewById(R.id.userMailTxt);
        userName = view.findViewById(R.id.userTxt);
        Button logoutButton = view.findViewById(R.id.logOutBtn);
        Button deleteButton = view.findViewById(R.id.deleteBtn);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String mail = user.getEmail();
            String userDetails = "Email: " + user.getEmail();
            userMail.setText(userDetails);
            String name = mail.split("@")[0];
            userName.setText("User Name: " + name);
        } else {
            userMail.setText("No user logged in");
        }

        // Handle Logout Button Click
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), EmailSignIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Handle Delete Account Button Click
        deleteButton.setOnClickListener(v -> {
            FirebaseUser userToDelete = auth.getCurrentUser();

            if (userToDelete != null) {
                // Step 1: Ask the user for their password to confirm
                EditText passwordEditText = new EditText(getActivity());
                passwordEditText.setHint("Enter your password");
                passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm Password")
                        .setMessage("You will loose all the previous booking history.Please enter your password to delete your account.")
                        .setView(passwordEditText)
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            String password = passwordEditText.getText().toString();
                            if (!password.isEmpty()) {
                                // Step 2: Reauthenticate the user with their password
                                reauthenticateAndDelete(userToDelete, password);
                            } else {
                                Toast.makeText(getActivity(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
        });

        return view;
    }

    private void reauthenticateAndDelete(FirebaseUser user, String password) {
        // Re-authenticate the user
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Step 3: Delete the user's data from Firestore
                        deleteUserDataFromFirestore(user.getUid());

                        // Step 4: Delete the user account after successful data deletion
                        user.delete()
                                .addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        // After account deletion, sign out and redirect to login
                                        auth.signOut();
                                        Intent intent = new Intent(getActivity(), EmailSignIn.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to delete user account", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to delete user", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUserDataFromFirestore(String uid) {
        WriteBatch batch = firestore.batch();  // Initialize Firestore batch

        // Delete the user's data from their 'users' document
        batch.delete(firestore.collection("users").document(uid));

        // Delete the user's tickets from 'tickets' collection (subcollection under the user document)
        firestore.collection("users").document(uid).collection("tickets")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No tickets to delete, just commit the batch
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getActivity(), "User data deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            for (DocumentSnapshot snapshot : task.getResult()) {
                                // Delete each ticket document
                                batch.delete(snapshot.getReference());
                            }

                            // Commit the batch after deleting all tickets
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getActivity(), "User data and tickets deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error deleting tickets: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error retrieving tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
