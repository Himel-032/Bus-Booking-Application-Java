package com.example.bookingbusticket;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        // Initialize UI elements
        userMail = view.findViewById(R.id.userMailTxt);
        userName = view.findViewById(R.id.userTxt);
        Button logoutButton = view.findViewById(R.id.logOutBtn);
        Button deleteButton = view.findViewById(R.id.deleteBtn);

        // Initialize FirebaseAuth and Firestore
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current user details
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String mail = user.getEmail();
            String userDetails = "Email: " + mail;
            userMail.setText(userDetails);
            String name = mail.split("@")[0];  // Get name from email
            userName.setText("User Name: " + name);
        } else {
            userMail.setText("No user logged in");
            userName.setText("Guest");
        }

        // Handle Logout Button Click
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(requireActivity(), EmailSignIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Handle Delete Account Button Click
        deleteButton.setOnClickListener(v -> {
            FirebaseUser userToDelete = auth.getCurrentUser();

            if (userToDelete != null) {
                // Step 1: Ask the user for their password to confirm
                EditText passwordEditText = new EditText(requireActivity());
                passwordEditText.setHint("Enter your password");
                passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());

                new AlertDialog.Builder(requireActivity())
                        .setTitle("Confirm Password")
                        .setMessage("You will lose all the previous booking history. Please enter your password to delete your account.")
                        .setView(passwordEditText)
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            String password = passwordEditText.getText().toString().trim();
                            if (!password.isEmpty()) {
                                // Step 2: Reauthenticate the user with their password
                                reauthenticateAndDelete(userToDelete, password);
                            } else {
                                Toast.makeText(requireActivity(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
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
                                        if (isAdded()) {  // Check if fragment is attached
                                            auth.signOut();
                                            Intent intent = new Intent(requireActivity(), EmailSignIn.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    } else {
                                        if (isAdded()) {
                                            Log.e("DeleteAccountError", "Error deleting user account: " + deleteTask.getException());
                                            Toast.makeText(requireActivity(), "Failed to delete user account", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) {
                                        Log.e("DeleteAccountError", "Error deleting user: " + e.getMessage());
                                        Toast.makeText(requireActivity(), "Failed to delete user", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        if (isAdded()) {
                            Log.e("ReauthenticationError", "Authentication failed: " + task.getException());
                            Toast.makeText(requireActivity(), "Authentication failed, please try again", Toast.LENGTH_SHORT).show();
                        }
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
                    if (isAdded()) {  // Ensure the fragment is still attached
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // No tickets to delete, just commit the batch
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            if (isAdded()) {
                                                Toast.makeText(requireActivity(), "User data deleted successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (isAdded()) {
                                                Log.e("DeleteUserDataError", "Error deleting user data: " + e.getMessage());
                                                Toast.makeText(requireActivity(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                for (DocumentSnapshot snapshot : task.getResult()) {
                                    // Delete each ticket document
                                    batch.delete(snapshot.getReference());
                                }

                                // Commit the batch after deleting all tickets
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            if (isAdded()) {
                                                Toast.makeText(requireActivity(), "User data and tickets deleted successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (isAdded()) {
                                                Log.e("DeleteTicketsError", "Error deleting tickets: " + e.getMessage());
                                                Toast.makeText(requireActivity(), "Failed to delete tickets", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            if (isAdded()) {
                                Log.e("FirestoreError", "Error retrieving tickets: " + task.getException());
                                Toast.makeText(requireActivity(), "Error retrieving tickets", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Log.e("FirestoreError", "Error retrieving tickets: " + e.getMessage());
                        Toast.makeText(requireActivity(), "Error retrieving tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
