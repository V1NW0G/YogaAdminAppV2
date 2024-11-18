package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.utils.NetworkUtils;
import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.network.APIService;
import com.universalyoga.yogaadminapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCourseActivity extends AppCompatActivity {

    private EditText courseIdEditText, timeEditText, durationEditText,
            capacityEditText, priceEditText, typeEditText, descriptionEditText;
    private Spinner dayOfWeekSpinner;  // Spinner for selecting the day of the week
    private Button saveButton;
    private YogaDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize UI elements
        courseIdEditText = findViewById(R.id.courseIdEditText);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);
        timeEditText = findViewById(R.id.timeEditText);
        durationEditText = findViewById(R.id.durationEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        typeEditText = findViewById(R.id.typeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveButton = findViewById(R.id.saveButton);

        // Populate the Spinner with days of the week
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, daysOfWeek);
        dayOfWeekSpinner.setAdapter(adapter);

        // Set up save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate the input fields
                if (validateInputs()) {
                    // Collect data from the user input
                    int courseid = Integer.parseInt(courseIdEditText.getText().toString());
                    String dayOfWeek = dayOfWeekSpinner.getSelectedItem().toString();  // Get the selected day
                    String time = timeEditText.getText().toString();
                    int duration = Integer.parseInt(durationEditText.getText().toString());
                    int capacity = Integer.parseInt(capacityEditText.getText().toString());
                    double price = Double.parseDouble(priceEditText.getText().toString());
                    String type = typeEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();

                    // Create a new Course object
                    Course newCourse = new Course(courseid, dayOfWeek, time, duration, capacity, price, type, description);

                    // Update the SQLite database first
                    dbHelper.addCourse(newCourse);

                    // Check if internet connection is available
                    if (NetworkUtils.isConnectedToInternet(AddCourseActivity.this)) {
                        // If connected, update the backend
                        updateBackend(newCourse);
                    } else {
                        // If not connected, notify the user and retry later
                        Toast.makeText(AddCourseActivity.this, "No internet connection. Will retry later.", Toast.LENGTH_LONG).show();
                        // Save to a retry queue or a retry table in SQLite, or handle retry logic
                        storePendingRequest(newCourse);
                    }

                    // Close the activity and return to MainActivity
                    finish();
                } else {
                    Toast.makeText(AddCourseActivity.this, "Please fill all the fields correctly.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        // Validate all fields before proceeding
        if (courseIdEditText.getText().toString().isEmpty() ||
                timeEditText.getText().toString().isEmpty() ||
                durationEditText.getText().toString().isEmpty() ||
                capacityEditText.getText().toString().isEmpty() ||
                priceEditText.getText().toString().isEmpty() ||
                typeEditText.getText().toString().isEmpty() ||
                descriptionEditText.getText().toString().isEmpty()) {
            return false;
        }
        return true;
    }

    private void updateBackend(Course newCourse) {
        // Use Retrofit to send the data to the backend API
        APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);

        Call<Course> call = apiService.addCourse(newCourse);
        call.enqueue(new Callback<Course>() {
            @Override
            public void onResponse(Call<Course> call, Response<Course> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddCourseActivity.this, "Course added to the backend successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddCourseActivity.this, "Failed to update the backend.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Course> call, Throwable t) {
                Toast.makeText(AddCourseActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storePendingRequest(Course newCourse) {
        // Store the course data in the pending requests table for later syncing
        dbHelper.storePendingRequest("addCourse", newCourse);
    }
}