package com.example.bookingbusticket.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingbusticket.Model.Trip;
import com.example.bookingbusticket.R;
import com.example.bookingbusticket.SearchActivity;
import com.example.bookingbusticket.SeatListActivity;
import com.example.bookingbusticket.databinding.ViewholderTripBinding;


import java.util.ArrayList;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private String date;
    private final ArrayList<Trip> trips;
    private Context context;

    public TripAdapter(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    @NonNull
    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTripBinding binding=ViewholderTripBinding.inflate(LayoutInflater.from(context),parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TripAdapter.ViewHolder holder, int position) {
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


        holder.itemView.setOnClickListener(v -> {
            Intent intent =new Intent(context, SeatListActivity.class);
            intent.putExtra("trip", trip);
            // new added
            intent.putExtra("busCompany", trip.getBusCompanyName());
            intent.putExtra("date",trip.getDate());
            //new added
            context.startActivity(intent);
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
