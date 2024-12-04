package com.example.bookingbusticket.Model;

import java.io.Serializable;

public class Trip implements Serializable {
  //  private String busCompanyLogo;
    private int ID;
    private String passenger;
    private String busCompanyName;
    private String classSeat;
    private String date;
    private String departureTime;
    private String from;
    private String fromShort;
    private Integer price;
    private String reservedSeats;
    private String to;
    private String toShort;
    private String arriveTime;

    private int totalSeats;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    private String travelTime;
    private String start;
    private String end;








    public Trip() {
    }
    @Override
    public String toString(){
        return from;
    }
  /* public String getBusCompanyLogo() {
        return busCompanyLogo;
    }

    public void setBusCompanyLogo(String busCompanyLogo) {
        this.busCompanyLogo = busCompanyLogo;
    }
    */
    public int getID(){ return ID; }
    public void setID(int ID){ this.ID = ID; }

    public String getBusCompanyName() {
        return busCompanyName;
    }

    public void setBusCompanyName(String busCompanyName) {
        this.busCompanyName = busCompanyName;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getClassSeat() {
        return classSeat;
    }

    public void setClassSeat(String classSeat) {
        this.classSeat = classSeat;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFromShort() {
        return fromShort;
    }

    public void setFromShort(String fromShort) {
        this.fromShort = fromShort;
    }



    public Integer getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }



    public String getReservedSeats() {
        return reservedSeats;
    }

    public void setReservedSeats(String reservedSeats) {
        this.reservedSeats = reservedSeats;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void ArriveTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getToShort() {
        return toShort;
    }

    public void setToShort(String toShort) {
        this.toShort = toShort;
    }


    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    public String getPassenger()
    {
        return  passenger;
    }
    public void setPassenger(String passenger)
    {
        this.passenger=passenger;
    }

}
