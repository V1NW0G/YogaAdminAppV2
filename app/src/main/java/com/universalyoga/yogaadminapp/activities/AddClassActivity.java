package com.universalyoga.yogaadminapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;
import com.universalyoga.yogaadminapp.network.APIService;
import com.universalyoga.yogaadminapp.network.RetrofitClient;
import com.universalyoga.yogaadminapp.utils.NetworkUtils;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddClassActivity extends AppCompatActivity {

    private EditText classIdEditText, dateEditText, teacherEditText, commentEditText;
    private Button saveClassButton, datePickerButton;
    private YogaDatabaseHelper dbHelper;
    private int courseId;

    // Variables for DatePicker
    private int year, month, dayOfMonth;
    private String courseDayOfWeek;

    // UI elements for day of the week warning
    private TextView classDayOfWeekTextView;

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
        datePickerButton = findViewById(R.id.datePickerButton);
        classDayOfWeekTextView = findViewById(R.id.classDayOfWeekTextView);

        // Get the course ID passed from the previous activity
        courseId = getIntent().getIntExtra("courseId", -1);

        // Get the course's day of the week from the database
        courseDayOfWeek = dbHelper.getCourseById(courseId).getDayOfWeek();

        // Set the hint on the date field to show "Pick Monday" or the actual day of the week for that course
        dateEditText.setHint("Pick " + courseDayOfWeek);

        // Set up DatePicker for button click
        dateEditText.setFocusable(false);
        dateEditText.setClickable(true);
        datePickerButton.setOnClickListener(v -> showDatePickerDialog());

        saveClassButton.setOnClickListener(v -> {
            // Validate the required fields
            String classIdText = classIdEditText.getText().toString().trim();
            String date = dateEditText.getText().toString().trim();
            String teacher = teacherEditText.getText().toString().trim();

            // Check if required fields are filled
            if (classIdText.isEmpty() || date.isEmpty() || teacher.isEmpty()) {
                Toast.makeText(AddClassActivity.this, "Class ID, Date, and Teacher are required.", Toast.LENGTH_SHORT).show();
                return; // Stop execution if fields are empty
            }

            // Parse class ID from input
            int classId = Integer.parseInt(classIdText);
            String comment = commentEditText.getText().toString().trim(); // Optional field

            // Add the class to the local SQLite database
            if (dbHelper.addClass(classId, courseId, date, teacher, comment)) {
                Toast.makeText(AddClassActivity.this, "Class added successfully!", Toast.LENGTH_SHORT).show();

                // Check if the device is connected to the internet
                if (NetworkUtils.isConnectedToInternet(AddClassActivity.this)) {
                    // Update the backend (MongoDB) if there is an internet connection
                    updateBackend(classId, courseId, date, teacher, comment);
                } else {
                    // If no internet connection, save the request to sync later
                    storePendingRequest(classId, courseId, date, teacher, comment);
                }

                finish();  // Close the activity after saving the class
            } else {
                Toast.makeText(AddClassActivity.this, "Error adding class.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Initialize the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddClassActivity.this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // Check if the selected day matches the course's day of the week
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth);
                    String selectedDay = getDayOfWeekString(selectedDate.get(Calendar.DAY_OF_WEEK));

                    if (selectedDay.equals(courseDayOfWeek)) {
                        // Set the selected date to the EditText if it's valid
                        String selectedDateString = String.format("%02d/%02d/%04d", selectedDayOfMonth, selectedMonth + 1, selectedYear);
                        dateEditText.setText(selectedDateString);

                        // Hide the warning message after a valid selection
                        classDayOfWeekTextView.setVisibility(View.GONE);
                    } else {
                        // Show the warning message if the selected day doesn't match the course day
                        classDayOfWeekTextView.setVisibility(View.VISIBLE);
                        classDayOfWeekTextView.setText("Day of Week: " + courseDayOfWeek);  // Show the course's required day

                        // Show a toast to inform the user
                        Toast.makeText(AddClassActivity.this, "Please select a " + courseDayOfWeek + ".", Toast.LENGTH_SHORT).show();
                    }
                },
                year, month, dayOfMonth
        );

        datePickerDialog.show();
    }

    private String getDayOfWeekString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            case Calendar.SUNDAY: return "Sunday";
            default: return "";
        }
    }

    private void updateBackend(int classid, int courseId, String date, String teacher, String comment) {
        Class newClass = new Class(classid, courseId, date, teacher, comment);

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
        Class newClass = new Class(classid, courseId, date, teacher, comment);
        dbHelper.storePendingRequest("addClass", newClass);
    }
}