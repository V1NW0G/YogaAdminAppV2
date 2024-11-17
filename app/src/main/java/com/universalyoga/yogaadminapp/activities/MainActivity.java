package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.database.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Course;

import com.universalyoga.yogaadminapp.network.APIService;
import com.universalyoga.yogaadminapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private YogaDatabaseHelper dbHelper;
    private TextView coursesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new YogaDatabaseHelper(this);
        coursesTextView = findViewById(R.id.coursesTextView);

        // Check if the local database is empty
        if (dbHelper.isCoursesTableEmpty()) {
            // Fetch courses from the API and store them locally
            fetchCoursesFromAPI();
        } else {
            // Load the courses from the local database
            loadCoursesFromDatabase();
        }
    }

    private void fetchCoursesFromAPI() {
        APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);
        Call<List<Course>> call = apiService.getCourses();

        call.enqueue(new Callback<List<Course>>() {
            @Override
            public void onResponse(Call<List<Course>> call, Response<List<Course>> response) {
                if (response.isSuccessful()) {
                    List<Course> courses = response.body();
                    if (courses != null) {
                        // Store courses in SQLite
                        for (Course course : courses) {
                            dbHelper.addCourse(course);
                        }
                        // Update the UI
                        loadCoursesFromDatabase();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Course>> call, Throwable t) {
                Log.e("MainActivity", "Error fetching courses: " + t.getMessage());
            }
        });
    }

    private void loadCoursesFromDatabase() {
        List<Course> courses = dbHelper.getAllCourses();
        StringBuilder coursesText = new StringBuilder();

        // Loop through the courses and add all the details to the StringBuilder
        for (Course course : courses) {
            coursesText.append("Course ID: ").append(course.getCourseid()).append("\n")
                    .append("Day: ").append(course.getDayOfWeek()).append("\n")
                    .append("Time: ").append(course.getTime()).append("\n")
                    .append("Duration: ").append(course.getDuration()).append(" minutes\n")
                    .append("Capacity: ").append(course.getCapacity()).append(" people\n")
                    .append("Price: $").append(course.getPrice()).append("\n")
                    .append("Type: ").append(course.getType()).append("\n")
                    .append("Description: ").append(course.getDescription()).append("\n\n");
        }

        // Set the text to the TextView
        coursesTextView.setText(coursesText.toString());
    }
}