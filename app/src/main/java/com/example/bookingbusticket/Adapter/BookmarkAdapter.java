package com.example.bookingbusticket.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingbusticket.Model.Trip;
import com.example.bookingbusticket.R;
import com.example.bookingbusticket.databinding.ViewholderTripBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private String date;
    private final ArrayList<Trip> trips;
    private Context context;

    public BookmarkAdapter(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public BookmarkAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTripBinding binding=ViewholderTripBinding.inflate(LayoutInflater.from(context),parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.ViewHolder holder, int position) {
        Trip trip=trips.get(position);
      /*  Glide.with(context)
               .load(trip.getBusCompanyLogo())
                .into(holder.binding.logo);
                */
        if(trip.getBusCompanyName().equals("Ena Bus")){
            holder.binding.logo.setImageResource(R.drawable.ena_bus);
        }
        if(trip.getBusCompanyName().equals("Hanif Bus")){
            holder.binding.logo.setImageResource(R.drawable.hanif_bus);
        }
        if(trip.getBusCompanyName().equals("Green Line Bus")){
            holder.binding.logo.setImageResource(R.drawable.green_line_bus);
        }
        if(trip.getBusCompanyName().equals("Shohag Bus")){
            holder.binding.logo.setImageResource(R.drawable.shohag_bus);
        }

        holder.binding.fromTxt.setText(trip.getFrom());
        holder.binding.fromShortTxt.setText(trip.getFromShort());
        holder.binding.toTxt.setText(trip.getTo());
        holder.binding.toShortTxt.setText(trip.getToShort());
        holder.binding.arrivalTxt.setText("Time:"+trip.getDepartureTime());
        holder.binding.classTxt.setText(trip.getClassSeat());
        holder.binding.priceTxt.setText("BDT "+trip.getPrice().toString());
        holder.binding.travleTimeTxt.setText(trip.getTravelTime());
        holder.binding.idText.setText(String.valueOf(trip.getID()));
        holder.binding.depurturePlaceTxt.setText(trip.getStart());
        holder.binding.endingPlaceTxt.setText(trip.getEnd());
        holder.binding.seatsTxt.setVisibility(View.VISIBLE);
        holder.binding.seatsTxt.setText("Seats: "+trip.getPassenger());
        holder.binding.dateTxt.setVisibility(View.VISIBLE);
        holder.binding.dateTxt.setText("Date: "+trip.getDate());


/*
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Cancel Ticket");
                builder.setMessage("Please enter your phone number to cancel the ticket.");

                final EditText phoneInput = new EditText(context);
                phoneInput.setHint("Enter your phone number");
                phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);

                builder.setView(phoneInput);

                builder.setPositiveButton("Cancel Ticket", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = phoneInput.getText().toString().trim();
                        if (!phoneNumber.isEmpty()) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            if (currentUser != null) {
                                String userId = currentUser.getUid();
                                String date = trip.getDate(); // Ensure this matches "9 Dec, 2024" format
                                int busId = trip.getID(); // Keep as integer
                                String seatString = trip.getPassenger(); // Match full string (e.g., "4B,5B,6B,7B,8B")

                                // Debugging Logs
                                Log.d("Query", "Date: " + date + ", ID: " + busId + ", Passenger: " + seatString);

                                db.collection("users")
                                        .document(userId)
                                        .collection("tickets")
                                        .whereEqualTo("date", date)
                                        .whereEqualTo("ID", busId) // Use integer
                                        .whereEqualTo("passenger", seatString) // Match exact string
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                Log.d("Firestore", "Matching tickets found: " + queryDocumentSnapshots.size());
                                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                    document.getReference().delete()
                                                            .addOnSuccessListener(aVoid ->
                                                                    Toast.makeText(context, "Ticket canceled successfully!", Toast.LENGTH_SHORT).show())
                                                            .addOnFailureListener(e ->
                                                                    Toast.makeText(context, "Failed to cancel ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                }
                                            } else {
                                                Log.d("Firestore", "No matching tickets found");
                                                Toast.makeText(context, "No matching ticket found.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("Firestore", "Error fetching tickets: " + e.getMessage());
                                            Toast.makeText(context, "Error fetching tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(context, "Phone number is required!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });
/*
 */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Cancel Ticket");
                builder.setMessage("Please enter your phone number to cancel the ticket.");

                final EditText phoneInput = new EditText(context);
                phoneInput.setHint("Enter your phone number");
                phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);

                builder.setView(phoneInput);

                builder.setPositiveButton("Cancel Ticket", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phoneNumber = phoneInput.getText().toString().trim();

                        if (!phoneNumber.isEmpty()) {

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            if (currentUser != null) {

                                String userId = currentUser.getUid();
                                String dateString = trip.getDate(); // The date of the trip as string
                                int busId = trip.getID();
                                String seatString = trip.getPassenger();

                                try {
                                    // Adjusted date format to "dd MMM, yyyy"
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);
                                    Date tripDate = dateFormat.parse(dateString);

                                    Date currentDate = new Date(); // Current date & time

                                    if (tripDate != null && tripDate.after(currentDate)) {


                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BookedSeats");
                                        String busName = trip.getBusCompanyName();
                                        String travelDate = trip.getDate();
                                        String passengerSeats = trip.getPassenger(); // Comma-separated seat numbers, e.g., "1A,2B,3C"
                                        String busID = String.valueOf(trip.getID());

                                        if (busName != null && travelDate != null && passengerSeats != null) {
                                            // Split the passenger seats into an array
                                            String[] seats = passengerSeats.split(",");

                                            // Loop through the seats to remove them
                                            for (String seat : seats) {
                                                databaseReference.child(busID).child(travelDate).child(seat.trim()).removeValue()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(context, "Seat " + seat + " is now available.", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Failed to remove seat " + seat + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }

                                        // Only allow cancellation for trips scheduled in the future
                                        db.collection("users")
                                                .document(userId)
                                                .collection("tickets")
                                                .whereEqualTo("date", dateString)
                                                .whereEqualTo("ID", busId)
                                                .whereEqualTo("passenger", seatString)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                                    if (!queryDocumentSnapshots.isEmpty()) {
                                                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                            document.getReference().delete()
                                                                    .addOnSuccessListener(aVoid ->
                                                                            Toast.makeText(context, "Ticket canceled successfully!", Toast.LENGTH_SHORT).show())
                                                                    .addOnFailureListener(e ->
                                                                            Toast.makeText(context, "Failed to cancel ticket: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "No matching ticket found.", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Error fetching tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Trip date is not in the future
                                        Toast.makeText(context, "Cancellation not possible for past trips", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ParseException e) {
                                    Log.e("DateError", "Error parsing date: " + dateString, e);
                                    Toast.makeText(context, "Error with trip date format.", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            Toast.makeText(context, "Phone number is required!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();

            }
        });










    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewholderTripBinding binding;
        public ViewHolder(ViewholderTripBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
