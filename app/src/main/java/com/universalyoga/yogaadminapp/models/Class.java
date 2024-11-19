package com.universalyoga.yogaadminapp.models;

public class Class {
    private int classid;
    private int courseid;
    private String date;
    private String teacher;
    private String comment;

    // Constructor
    public Class(int classid, int courseid, String date, String teacher, String comment) {
        this.classid = classid;
        this.courseid = courseid;
        this.date = date;
        this.teacher = teacher;
        this.comment = comment;
    }

    // Getters and Setters
    public int getClassid() {
        return classid;
    }

    public void setClassid(int classid) {
        this.classid = classid;
    }

    public int getCourseid() {
        return courseid;
    }

    public void setCourseid(int courseid) {
        this.courseid = courseid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Class ID: " + classid + "\nTeacher: " + teacher + "\nDate: " + date;
    }
}