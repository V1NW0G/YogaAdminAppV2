package com.universalyoga.yogaadminapp.models;

import com.google.gson.annotations.SerializedName;

public class Course {
    private int courseid;

    @SerializedName("day_of_week")
    private String dayOfWeek;

    private String time;
    private int duration;
    private int capacity;
    private double price;
    private String type;
    private String description;

    // Constructor
    public Course(int courseid, String dayOfWeek, String time, int duration, int capacity, double price, String type, String description) {
        this.courseid = courseid;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.duration = duration;
        this.capacity = capacity;
        this.price = price;
        this.type = type;
        this.description = description;
    }

    // Getter and Setter methods
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getCourseid() {
        return courseid;
    }

    public void setCourseid(int courseid) {
        this.courseid = courseid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Override toString to display course information in a readable format
    @Override
    public String toString() {
        return "Course ID: " + courseid + "\n" +
                "Day: " + dayOfWeek + "\n" +
                "Time: " + time + "\n" +
                "Duration: " + duration + " minutes\n" +
                "Capacity: " + capacity + " people\n" +
                "Price: $" + price + "\n" +
                "Type: " + type + "\n" +
                "Description: " + description;
    }
}