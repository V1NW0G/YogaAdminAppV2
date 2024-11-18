package com.universalyoga.yogaadminapp.network;

import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.models.Class;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface APIService {

    // GET all courses
    @GET("courses")
    Call<List<Course>> getCourses();  // Endpoint to get all courses

    // GET all classes
    @GET("classes")
    Call<List<Class>> getClasses();  // Endpoint to get all classes

    // POST a new course
    @POST("courses")
    Call<Course> addCourse(@Body Course course);  // Endpoint to add a new course

    @POST("classes")
    Call<Class> addClass(@Body Class newClass);

    // PUT (update) an existing course
    @PUT("courses/{courseid}")
    Call<Course> updateCourse(@Path("courseid") int courseid, @Body Course course);  // Endpoint to update a course

    // DELETE a course by course ID
    @DELETE("courses/{courseid}")
    Call<Void> deleteCourse(@Path("courseid") int courseid);  // Endpoint to delete a course
}