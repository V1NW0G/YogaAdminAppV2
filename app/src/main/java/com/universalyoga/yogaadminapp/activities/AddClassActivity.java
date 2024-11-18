package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;  // Ensure this is the Class model for your backend
import com.universalyoga.yogaadminapp.network.APIService;
import com.universalyoga.yogaadminapp.network.RetrofitClient;
import com.universalyoga.yogaadminapp.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddClassActivity extends AppCompatActivity {

    private EditText classIdEditText, dateEditText, teacherEditText, commentEditText;
    private Button saveClassButton;
    private YogaDatabaseHelper dbHelper;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize UI elements
        classIdEditText = findViewById(R.id.classIdEditText);
        dateEditText = findViewById(R.id.dateEditText);
        teacherEditText = findViewById(R.id.teacherEditText);
        commentEditText = findViewById(R.id.commentEditText);
        saveClassButton = findViewById(R.id.saveClassButton);

        // Get the course ID passed from MainActivity
        courseId = getIntent().getIntExtra("courseId", -1);

        saveClassButton.setOnClickListener(v -> {
            // Collect data from the input fields
            int classid = Integer.parseInt(classIdEditText.getText().toString());
            String date = dateEditText.getText().toString();
            String teacher = teacherEditText.getText().toString();
            String comment = commentEditText.getText().toString();

            // Add the class to the local SQLite database
            if (dbHelper.addClass(classid, courseId, date, teacher, comment)) {
                Toast.makeText(AddClassActivity.this, "Class added successfully!", Toast.LENGTH_SHORT).show();

                // Check if the device is connected to the internet
                if (NetworkUtils.isConnectedToInternet(AddClassActivity.this)) {
                    // Update the backend (MongoDB) if there is an internet connection
                    updateBackend(classid, courseId, date, teacher, comment);
                } else {
                    // If no internet connection, save the request to sync later
                    storePendingRequest(classid, courseId, date, teacher, comment);
                }

                finish();  // Close the activity after saving the class
            } else {
                Toast.makeText(AddClassActivity.this, "Error adding class.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBackend(int classid, int courseId, String date, String teacher, String comment) {
        // Create a new Class object to send to the backend
        Class newClass = new Class(classid, courseId, date, teacher, comment);

        // Use Retrofit to send the data to the backend API
        APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);

        Call<Class> call = apiService.addClass(newClass);
        call.enqueue(new Callback<Class>() {
            @Override
            public void onResponse(Call<Class> call, Response<Class> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddClassActivity.this, "Class added to the backend successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddClassActivity.this, "Failed to update the backend.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Class> call, Throwable t) {
                Toast.makeText(AddClassActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storePendingRequest(int classid, int courseId, String date, String teacher, String comment) {
        // Store the class data in the pending requests table for later syncing
        Class newClass = new Class(classid, courseId, date, teacher, comment);
        dbHelper.storePendingRequest("addClass", newClass);
    }
}