package com.universalyoga.yogaadminapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.models.Class;
import com.universalyoga.yogaadminapp.adapter.ClassAdapter;
import com.universalyoga.yogaadminapp.network.APIService;
import com.universalyoga.yogaadminapp.network.RetrofitClient;
import com.universalyoga.yogaadminapp.utils.NetworkUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CourseDetailActivity extends AppCompatActivity {

        private YogaDatabaseHelper dbHelper;
        private TextView courseTitleTextView;
        private TextView courseDayTextView;
        private TextView courseTimeTextView;
        private TextView coursePriceTextView;
        private TextView courseDurationTextView;
        private TextView courseCapacityTextView;
        private TextView courseTypeTextView;
        private TextView courseDescriptionTextView;
        private ListView classListView;
        private Button updateDeleteButton;  // Update/Delete button to navigate to the update page

        private int courseId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_course_detail);

            dbHelper = new YogaDatabaseHelper(this);

            // Initialize views
            courseTitleTextView = findViewById(R.id.courseTitleTextView);
            courseDayTextView = findViewById(R.id.courseDayTextView);
            courseTimeTextView = findViewById(R.id.courseTimeTextView);
            coursePriceTextView = findViewById(R.id.coursePriceTextView);
            courseDurationTextView = findViewById(R.id.courseDurationTextView);
            courseCapacityTextView = findViewById(R.id.courseCapacityTextView);
            courseTypeTextView = findViewById(R.id.courseTypeTextView);
            courseDescriptionTextView = findViewById(R.id.courseDescriptionTextView);
            classListView = findViewById(R.id.classListView);
            updateDeleteButton = findViewById(R.id.updateDeleteButton);  // Initialize the button

            // Get the course ID passed from the previous activity
            String courseIdString = getIntent().getStringExtra("courseId");
            Log.d("CourseDetailActivity", "Received courseId: " + courseIdString);

            if (courseIdString != null && !courseIdString.isEmpty()) {
                try {
                    courseId = Integer.parseInt(courseIdString);  // Convert String to int
                    loadCourseDetails(courseId);  // Load course details
                    loadClassList(courseId);  // Load classes under this course

                    // Set up the Update/Delete button click listener
                    updateDeleteButton.setOnClickListener(v -> {
                        // Perform update or delete depending on action
                        if (updateDeleteButton.getText().toString().equals("Delete Course")) {
                            deleteCourseAndClasses();
                        } else {
                            // Start CourseUpdateActivity for result
                            Intent intent = new Intent(CourseDetailActivity.this, CourseUpdateActivity.class);
                            intent.putExtra("courseId", courseId);  // Pass the course ID to the update page
                            startActivityForResult(intent, 1);  // Request code 1
                        }
                    });

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid course ID.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Course ID is missing.", Toast.LENGTH_SHORT).show();
            }
        }

        private void deleteCourseAndClasses() {
            if (NetworkUtils.isConnectedToInternet(this)) {
                deleteCourseFromBackend();
            } else {
                storePendingRequestForDelete(courseId);
                Toast.makeText(this, "No internet connection. Course delete saved for later.", Toast.LENGTH_SHORT).show();
            }
        }
        private void deleteCourseFromBackend() {
            Log.d("CourseDetailActivity", "Attempting to delete course with ID: " + courseId);

            APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);
            Call<Void> call = apiService.deleteCourse(courseId);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d("CourseDetailActivity", "Delete response code: " + response.code());
                    if (response.isSuccessful()) {
                        dbHelper.deleteCourse(courseId);
                        dbHelper.deleteClassesForCourse(courseId);
                        Toast.makeText(CourseDetailActivity.this, "Course and its classes deleted successfully.", Toast.LENGTH_SHORT).show();
                        // Navigate back to the main activity
                        Intent intent = new Intent(CourseDetailActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CourseDetailActivity.this, "Failed to delete course.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(CourseDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void storePendingRequestForDelete(int courseId) {
            dbHelper.storePendingRequest("deleteCourse", courseId);
        }

        // Method to load course details from the database and display them
        private void loadCourseDetails(int courseId) {
            Course course = dbHelper.getCourseById(courseId);  // Fetch course details from the database

            if (course != null) {
                // Set the course details in the UI
                courseTitleTextView.setText("Course: " + course.getDayOfWeek() + " - " + course.getTime());
                courseDayTextView.setText("Day: " + course.getDayOfWeek());
                courseTimeTextView.setText("Time: " + course.getTime());
                coursePriceTextView.setText("Price: $" + course.getPrice());
                courseDurationTextView.setText("Duration: " + course.getDuration() + " minutes");
                courseCapacityTextView.setText("Capacity: " + course.getCapacity() + " people");
                courseTypeTextView.setText("Type: " + course.getType());
                courseDescriptionTextView.setText("Description: " + course.getDescription());
            } else {
                Toast.makeText(this, "Course not found.", Toast.LENGTH_SHORT).show();
            }
        }

        // Method to load the list of classes under this course
        private void loadClassList(int courseId) {
            List<Class> classes = dbHelper.getClassesForCourse(courseId);  // Fetch classes for the given course
            if (classes != null && !classes.isEmpty()) {
                ClassAdapter classAdapter = new ClassAdapter(this, classes);  // Use the ClassAdapter to display classes
                classListView.setAdapter(classAdapter);
            } else {
                Toast.makeText(this, "No classes available.", Toast.LENGTH_SHORT).show();
            }
        }

        // Handle the result from the CourseUpdateActivity (e.g., after deleting a class)
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Check if the update was successful
            if (requestCode == 1 && resultCode == RESULT_OK) {
                // Refresh the course details from the database
                loadCourseDetails(courseId);
                loadClassList(courseId);  // Reload classes as well
            }
        }

}