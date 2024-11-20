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

import java.util.List;


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
                        Intent intent = new Intent(CourseDetailActivity.this, CourseUpdateActivity.class);
                        intent.putExtra("courseId", courseId);  // Pass the course ID to the update page
                        startActivityForResult(intent, 1);  // Start activity for result
                    });

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid course ID.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Course ID is missing.", Toast.LENGTH_SHORT).show();
            }
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

            if (requestCode == 1 && resultCode == RESULT_OK) {
                // Reload the class list when the user returns from the update page
                loadClassList(courseId);
            }
        }

}