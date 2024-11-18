package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Course;

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
    private TextView classListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        dbHelper = new YogaDatabaseHelper(this);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseDayTextView = findViewById(R.id.courseDayTextView);
        courseTimeTextView = findViewById(R.id.courseTimeTextView);
        coursePriceTextView = findViewById(R.id.coursePriceTextView);
        courseDurationTextView = findViewById(R.id.courseDurationTextView);
        courseCapacityTextView = findViewById(R.id.courseCapacityTextView);
        courseTypeTextView = findViewById(R.id.courseTypeTextView);
        courseDescriptionTextView = findViewById(R.id.courseDescriptionTextView);
        classListTextView = findViewById(R.id.classListTextView);

        // Get the course ID passed from the MainActivity
        String courseIdString = getIntent().getStringExtra("courseId");
        Log.d("CourseDetailActivity", "Received courseId: " + courseIdString);

        if (courseIdString != null && !courseIdString.isEmpty()) {
            try {
                int courseId = Integer.parseInt(courseIdString);  // Convert String to int
                loadCourseDetails(courseId);  // Call method to load course details
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid course ID.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Course ID is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to load course details from the database and display them
    private void loadCourseDetails(int courseId) {
        Course course = dbHelper.getCourseById(courseId);  // Fetch the course details from the database

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

            // Fetch and display the classes under this course
            List<String> classes = dbHelper.getClassesForCourse(courseId);
            if (classes != null && !classes.isEmpty()) {
                StringBuilder classDetails = new StringBuilder();
                for (String classInfo : classes) {
                    classDetails.append(classInfo).append("\n");
                }
                classListTextView.setText(classDetails.toString());
            } else {
                classListTextView.setText("No classes available.");
            }
        } else {
            Toast.makeText(this, "Course not found.", Toast.LENGTH_SHORT).show();
        }
    }
}