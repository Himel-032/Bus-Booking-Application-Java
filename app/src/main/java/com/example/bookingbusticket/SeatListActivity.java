package com.example.bookingbusticket;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingbusticket.Adapter.SeatAdapter;
import com.example.bookingbusticket.Model.Seat;
import com.example.bookingbusticket.Model.Trip;
import com.example.bookingbusticket.databinding.ActivitySeatListBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatListActivity extends BaseActivity {
    private ActivitySeatListBinding binding;
    private Trip trip;
    private Double price=0.0;
    private int num=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySeatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initSeatList();
        setVariable();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.confirmBtn.setOnClickListener(v -> {
            if(num>0){

                trip.setPassenger(binding.nameSeatSelectedTxt.getText().toString());
                trip.setPrice(price.intValue());
                Intent intent=new Intent(SeatListActivity.this,TicketDetailActivity.class);
                intent.putExtra("trip",trip);
                startActivity(intent);



            }else{
                Toast.makeText(SeatListActivity.this, "Please select your seat", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /*
        private void initSeatList() {
          GridLayoutManager gridLayoutManager= new GridLayoutManager(this,7);
          gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
              @Override
              public int getSpanSize(int position) {
                  return (position%7 == 3 )?1:1;
              }
          });
          binding.seatRecyclerview.setLayoutManager(gridLayoutManager);
    
          List<Seat> seatList=new ArrayList<>();
          int row=0;
          int numberSeat=trip.getTotalSeats()+ (trip.getTotalSeats() / 7) +1;
    
          Map<Integer,String> seatAlphabetMap=new HashMap<>() ;
          seatAlphabetMap.put(0,"A");
          seatAlphabetMap.put(1,"B");
          seatAlphabetMap.put(2,"C");
          seatAlphabetMap.put(3,"D");
          seatAlphabetMap.put(4,"E");
          seatAlphabetMap.put(5,"F");
          for(int i=0;i<numberSeat;i++)
          {
              if(i%7==0){
                  row++;
              }
              if(i%7==3){
                  seatList.add(new Seat(Seat.SeatStatus.EMPTY,String.valueOf(row)));
              }else{
                  String seatName=seatAlphabetMap.get(i%7)+row;
                  Seat.SeatStatus seatStatus=trip.getReservedSeats().contains(seatName)?Seat.SeatStatus.UNAVAILABLE:Seat.SeatStatus.AVAILABLE;
                  seatList.add(new Seat(seatStatus,seatName));
              }
          }
            SeatAdapter seatAdapter = new SeatAdapter(seatList,this, (selectedName, num) -> {
              binding.numberSelectedTxt.setText(num+" Seat Selected");
              binding.nameSeatSelectedTxt.setText(selectedName);
                DecimalFormat df=new DecimalFormat("#.##");
                price=(Double.valueOf(df.format(num*trip.getPrice())));
                this.num=num;
                binding.priceTxt.setText("BDT "+price);
            });
          binding.seatRecyclerview.setAdapter(seatAdapter);
          binding.seatRecyclerview.setNestedScrollingEnabled(false);
        }
        */
private void initSeatList() {


    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4); // 4 columns
    binding.seatRecyclerview.setLayoutManager(gridLayoutManager);

    // List to store seat objects
    List<Seat> seatList = new ArrayList<>();
    int totalSeats = 40; // Total seats (10 rows * 4 seats per row)
    int seatsPerRow = 4; // 4 seats per row

    // Map for column letters
    Map<Integer, String> seatAlphabetMap = new HashMap<>();
    seatAlphabetMap.put(0, "A");
    seatAlphabetMap.put(1, "B");
    seatAlphabetMap.put(2, "C");
    seatAlphabetMap.put(3, "D");

    // Generate seat list
    for (int i = 0; i < totalSeats; i++) {
        int row = (i / seatsPerRow) + 1; // Row number (1-based)
        int column = i % seatsPerRow; // Column index
        String seatName = row + seatAlphabetMap.get(column); // e.g., 1A, 1B, etc.

        // Determine if seat is reserved
        Seat.SeatStatus seatStatus = trip.getReservedSeats().contains(seatName)
                ? Seat.SeatStatus.UNAVAILABLE
                : Seat.SeatStatus.AVAILABLE;

        // Add seat to the list
        seatList.add(new Seat(seatStatus, seatName));
    }




    // Set up adapter
    SeatAdapter seatAdapter = new SeatAdapter(seatList, this, (selectedName, num) -> {
        binding.numberSelectedTxt.setText(num + " Seat Selected");
        binding.nameSeatSelectedTxt.setText(selectedName);

        DecimalFormat df = new DecimalFormat("#.##");
        price = Double.valueOf(df.format(num * trip.getPrice()));
        this.num = num;

        binding.priceTxt.setText("BDT " + price);
    });

    binding.seatRecyclerview.setAdapter(seatAdapter);
    binding.seatRecyclerview.setNestedScrollingEnabled(false);
}



    private void getIntentExtra() {
        trip=(Trip) getIntent().getSerializableExtra("trip");



    }
}


