package com.universalyoga.yogaadminapp.network;

import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.models.Class;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIService {

    @GET("courses")
    Call<List<Course>> getCourses();  // Endpoint to get all courses

    @GET("classes")
    Call<List<Class>> getClasses();  // Endpoint to get all classes
}