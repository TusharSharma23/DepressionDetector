package com.sharmatushar.depressiondetector.Model;

public class UserData {

    private String date;
    private String timestamp;
    private double activity;

    public UserData(String date, String timestamp, double activity) {
        this.date = date;
        this.timestamp = timestamp;
        this.activity = activity;
    }

    public String getDate() {
        return date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getActivity() {
        return activity;
    }
}
