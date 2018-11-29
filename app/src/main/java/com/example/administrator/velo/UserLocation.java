package com.example.administrator.velo;

/**
 * Created by Administrator on 28/11/2018.
 */

public class UserLocation {
    public String name;
    public double latitude;
    public double longitude;
    public String address;
    public Integer number;

    public UserLocation(){}

    public UserLocation (double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public UserLocation (String name, double latitude, double longitude, String address, Integer number){
        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
        this.address=address;
        this.number=number;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
