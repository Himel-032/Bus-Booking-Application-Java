package com.example.bookingbusticket;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingbusticket.Model.Trip;
import com.example.bookingbusticket.databinding.ActivityTicketDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCAdditionalInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCCustomerInfoInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCProductInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCShipmentInfoInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz;
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TicketDetailActivity extends BaseActivity implements SSLCTransactionResponseListener {


    private ActivityTicketDetailBinding binding;
    private Trip trip;
    private int amount;

    private  String email,username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityTicketDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        FirebaseUser  user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get the email and extract username
            email = user.getEmail();
            username = email.split("@")[0].replaceAll("[^a-zA-Z]", "").toUpperCase();
        }
        setVariable();

        Button downloadButton = findViewById(R.id.downloadTicketBtn);// chatpt
        View ticketView = findViewById(R.id.ticketLayout); // Replace with the ID of your ticket view
        Button paymentButton = findViewById(R.id.paymentBtn);





        downloadButton.setOnClickListener(v -> {
            checkPermission();
            downloadTicketAsPDF(ticketView);

        });//chatgpt
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SSLCommerzInitialization sslCommerzInitialization = new SSLCommerzInitialization
                        ("himel6741aa0d024ed","himel6741aa0d024ed@ssl", amount, SSLCCurrencyType.BDT,"123456789098765",
                                "yourProductType", SSLCSdkType.TESTBOX);

                final SSLCCustomerInfoInitializer customerInfoInitializer = new
                        SSLCCustomerInfoInitializer("customer name", "customer email",
                        "address", "dhaka", "1214", "Bangladesh","phoneNumber");

                final SSLCProductInitializer productInitializer = new SSLCProductInitializer ("food", "food",
                        new SSLCProductInitializer.ProductProfile.TravelVertical("Travel", "10",
                                "A", "12", "Dhk-Syl"));

                final SSLCShipmentInfoInitializer shipmentInfoInitializer = new SSLCShipmentInfoInitializer
                        ("Courier",
                                2, new SSLCShipmentInfoInitializer.ShipmentDetails("AA","Address 1",
                                "Dhaka","1000","BD"));

                final SSLCAdditionalInitializer additionalInitializer = new SSLCAdditionalInitializer ();
                additionalInitializer.setValueA("Value Option 1");
                additionalInitializer.setValueB("Value Option 1");
                additionalInitializer.setValueC("Value Option 1");
                additionalInitializer.setValueD("Value Option 1");

                IntegrateSSLCommerz
                        .getInstance(TicketDetailActivity.this)
                        .addSSLCommerzInitialization(sslCommerzInitialization)
                        .addCustomerInfoInitializer(customerInfoInitializer)
                        .addProductInitializer(productInitializer)
                        .buildApiCall(TicketDetailActivity.this);

            }

        });
    }

    @Override
    public void transactionSuccess(SSLCTransactionInfoModel sslcTransactionInfoModel) {
        binding.downloadTicketBtn.setVisibility(View.VISIBLE);
        binding.paymentBtn.setVisibility(View.GONE);
        Toast.makeText(TicketDetailActivity.this, "Transaction success", Toast.LENGTH_SHORT).show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("BookedSeats");
        String busName = trip.getBusCompanyName();
        String travelDate = trip.getDate();
        String passengerSeats = trip.getPassenger(); // Comma-separated seat numbers, e.g., "1A,2B,3C"
        String busVariation = busName + " " + trip.getClassSeat();
        int id= trip.getID();
        String busID=String.valueOf(id);


        if (busName != null && travelDate != null && passengerSeats != null) {
            // Split the passenger seats into an array
            String[] seats = passengerSeats.split(",");

            // Create a map for batch update
            Map<String, Object> updates = new HashMap<>();
            for (String seat : seats) {
                updates.put(seat.trim(), true);
            }

            // Perform the batch update
            databaseReference.child(busID).child(travelDate).updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(TicketDetailActivity.this, "Seats reserved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TicketDetailActivity.this, "Failed to reserve seats", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(TicketDetailActivity.this, "Invalid trip data.", Toast.LENGTH_SHORT).show();
        }

        FirebaseFirestore firestore=FirebaseFirestore.getInstance();
        FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null){
            String uid=currentUser.getUid();
            Map<String,Object> ticketData=new HashMap<>();
            ticketData.put("from",trip.getFrom());
            ticketData.put("to",trip.getTo());
            ticketData.put("date",trip.getDate());
            ticketData.put("departureTime",trip.getDepartureTime());
            ticketData.put("travelTime",trip.getTravelTime());
            ticketData.put("classSeat",trip.getClassSeat());
            ticketData.put("price",trip.getPrice());
            ticketData.put("busCompanyName",trip.getBusCompanyName());
            ticketData.put("passenger",trip.getPassenger());
            ticketData.put("start",trip.getStart());
            ticketData.put("end",trip.getEnd());
            ticketData.put("ID",trip.getID());
            ticketData.put("fromShort",trip.getFromShort());
            ticketData.put("toShort",trip.getToShort());


            firestore.collection("users").document(uid).collection("tickets")
                    .add(ticketData)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(TicketDetailActivity.this, "Ticket added successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e->
                            Toast.makeText(TicketDetailActivity.this, "Failed to add ticket", Toast.LENGTH_SHORT).show());


        }
    }


    @Override
    public void transactionFail(String s) {
 Toast.makeText(TicketDetailActivity.this,"Transaction failed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void merchantValidationError(String s) {

    }







    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
        binding.fromTxt.setText(trip.getFrom());
        binding.fromSmallTxt.setText(trip.getFrom());
        binding.fromShortTxt.setText(trip.getFromShort());
        binding.toTxt.setText(trip.getTo());
        binding.toShortTxt.setText(trip.getToShort());
        binding.toSmallTxt.setText(trip.getTo());
        binding.dateTxt.setText(trip.getDate());  // date
        binding.timeTxt.setText(trip.getDepartureTime());
        binding.arrivalTxt.setText(trip.getTravelTime());
        binding.classTxt.setText(trip.getClassSeat());
        binding.priceTxt.setText("BDT "+trip.getPrice());
        amount=trip.getPrice();
        binding.bus.setText(trip.getBusCompanyName());
        binding.seatsTxt.setText(trip.getPassenger());
        binding.nameTxt.setText("Name: "+username);
        binding.mailTxt.setText("E-mail: "+email);
        binding.departureTxt.setText(trip.getStart());
        binding.endingTxt.setText(trip.getEnd());
        binding.idTxt.setText(String.valueOf(trip.getID()));
        if(trip.getBusCompanyName().equals("Ena Bus")){
            binding.logo.setImageResource(R.drawable.ena_bus);
        }
        if(trip.getBusCompanyName().equals("Hanif Bus")){
            binding.logo.setImageResource(R.drawable.hanif_bus);
        }
        if(trip.getBusCompanyName().equals("Green Line Bus")){
            binding.logo.setImageResource(R.drawable.green_line_bus);
        }
        if(trip.getBusCompanyName().equals("Shohag Bus")){
            binding.logo.setImageResource(R.drawable.shohag_bus);
        }
    }

    private void getIntentExtra() {
        trip =(Trip) getIntent().getSerializableExtra("trip");
    }
    private void checkPermission() { if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); } }




    // generated by chatgpt to download the pdf of ticket

    public void downloadTicketAsPDF(View ticketView) {
        // Step 1: Create a Bitmap of the View
        ticketView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(ticketView.getDrawingCache());
        ticketView.setDrawingCacheEnabled(false);

        if (bitmap == null) {
            Toast.makeText(this, "Error capturing view as bitmap", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 2: Create a PdfDocument
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Draw the Bitmap onto the PDF page
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        pdfDocument.finishPage(page);

        // Step 3: Create the "BookingTicket" folder in external storage
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BookingTicket");
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs();
            if (!isCreated) {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
                pdfDocument.close();
                return;
            }
        }

        // Step 4: Generate a unique filename using a timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Ticket_Details_" + timeStamp + ".pdf";

        // Step 5: Create the PDF file
        File pdfFile = new File(directory, fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(outputStream);
            outputStream.close();

            // Notify the user about success
            Toast.makeText(this, "PDF saved at: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            // Close the PdfDocument
            pdfDocument.close();
        }
    }

    // sslcommerze




}