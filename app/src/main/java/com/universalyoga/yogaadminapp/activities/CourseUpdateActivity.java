package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.List;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;
import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.network.APIService;
import com.universalyoga.yogaadminapp.network.RetrofitClient;
import com.universalyoga.yogaadminapp.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseUpdateActivity extends AppCompatActivity {

    private YogaDatabaseHelper dbHelper;
    private EditText courseIdEditText, timeEditText, durationEditText, capacityEditText, priceEditText, typeEditText, descriptionEditText;
    private Button updateDeleteButton, saveClassButton;
    private LinearLayout classFormsContainer;
    private Spinner dayOfWeekSpinner;
    private int courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_update);

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize views
        courseIdEditText = findViewById(R.id.courseIdEditText);
        timeEditText = findViewById(R.id.timeEditText);
        durationEditText = findViewById(R.id.durationEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        typeEditText = findViewById(R.id.typeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        updateDeleteButton = findViewById(R.id.updateDeleteButton);
        saveClassButton = findViewById(R.id.saveClassButton);
        classFormsContainer = findViewById(R.id.classFormsContainer);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);

        // Set up Spinner for Days of the Week
        String[] daysOfWeek = new String[] {
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        // Set a default value (for example, Monday)
        dayOfWeekSpinner.setSelection(0);  // 0 represents Monday

        // Get the course ID passed from the previous activity
        courseId = getIntent().getIntExtra("courseId", -1);

        if (courseId != -1) {
            loadCourseDetails(courseId);  // Load course details for editing
            loadClassList(courseId);  // Load classes under this course

            updateDeleteButton.setOnClickListener(v -> {
                // Perform update or delete depending on action
                if (updateDeleteButton.getText().toString().equals("Delete Course")) {
                    deleteCourseAndClasses();
                } else {
                    updateCourse();
                }
            });
        } else {
            Toast.makeText(this, "Course ID is missing.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to load course details from the database and display them
    private void loadCourseDetails(int courseId) {
        Course course = dbHelper.getCourseById(courseId);  // Fetch course details from the database

        if (course != null) {
            courseIdEditText.setText(String.valueOf(course.getCourseid()));
            timeEditText.setText(course.getTime());
            durationEditText.setText(String.valueOf(course.getDuration()));
            capacityEditText.setText(String.valueOf(course.getCapacity()));
            priceEditText.setText(String.valueOf(course.getPrice()));
            typeEditText.setText(course.getType());
            descriptionEditText.setText(course.getDescription());

            // Set the spinner based on the day of the week from the course data
            String[] daysOfWeek = new String[] {
                    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
            };

            // Find the index of the day of the week in the daysOfWeek array
            int dayOfWeekIndex = -1;
            for (int i = 0; i < daysOfWeek.length; i++) {
                if (daysOfWeek[i].equals(course.getDayOfWeek())) {
                    dayOfWeekIndex = i;
                    break;
                }
            }

            if (dayOfWeekIndex != -1) {
                dayOfWeekSpinner.setSelection(dayOfWeekIndex);  // Set the spinner to the day from the course
            }

            // Change button text to "Delete Course" to allow deletion
            updateDeleteButton.setText("Delete Course");
        } else {
            Toast.makeText(this, "Course not found.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to load the list of classes under this course
    private void loadClassList(int courseId) {
        List<Class> classes = dbHelper.getClassesForCourse(courseId);  // Fetch classes for the given course

        // Clear the existing class forms before adding new ones
        classFormsContainer.removeAllViews();

        if (classes != null && !classes.isEmpty()) {
            // Dynamically create forms for each class and populate with class data
            for (Class cls : classes) {
                // Create a new LinearLayout for each class form
                LinearLayout classFormContainer = new LinearLayout(this);
                classFormContainer.setOrientation(LinearLayout.VERTICAL);
                classFormContainer.setPadding(16, 16, 16, 16);

                // Add a title for the class (Class ID)
                TextView classTitleTextView = new TextView(this);
                classTitleTextView.setText("Class ID: " + cls.getClassid());  // Display class ID as the title
                classTitleTextView.setTextSize(18);
                classTitleTextView.setPadding(0, 16, 0, 8);
                classFormContainer.addView(classTitleTextView);

                // Create EditText fields for each class attribute
                EditText dateEditText = new EditText(this);
                dateEditText.setHint("Date");
                dateEditText.setText(cls.getDate());
                classFormContainer.addView(dateEditText);

                EditText teacherEditText = new EditText(this);
                teacherEditText.setHint("Teacher");
                teacherEditText.setText(cls.getTeacher());
                classFormContainer.addView(teacherEditText);

                EditText commentEditText = new EditText(this);
                commentEditText.setHint("Comment");
                commentEditText.setText(cls.getComment());
                classFormContainer.addView(commentEditText);

                // Add a delete button for each class record
                Button deleteClassButton = new Button(this);
                deleteClassButton.setText("Delete Class");
                deleteClassButton.setOnClickListener(v -> deleteClass(cls.getCourseid(), cls.getClassid()));  // Pass courseid and classid
                classFormContainer.addView(deleteClassButton);

                // Add this class form to the container
                classFormsContainer.addView(classFormContainer);
            }
        } else {
            Toast.makeText(this, "No classes available.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update the course
    private void updateCourse() {
        // Create updated course object from input fields
        Course updatedCourse = new Course(
                Integer.parseInt(courseIdEditText.getText().toString()),
                dayOfWeekSpinner.getSelectedItem().toString(),
                timeEditText.getText().toString(),
                Integer.parseInt(durationEditText.getText().toString()),
                Integer.parseInt(capacityEditText.getText().toString()),
                Double.parseDouble(priceEditText.getText().toString()),
                typeEditText.getText().toString(),
                descriptionEditText.getText().toString()
        );

        // Check for network availability before proceeding with the update
        if (NetworkUtils.isConnectedToInternet(this)) {
            updateBackend(updatedCourse);
        } else {
            storePendingRequest(updatedCourse);
            Toast.makeText(this, "No internet connection. Course update saved for later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBackend(Course updatedCourse) {
        // Use Retrofit to update the course in the backend
        APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);
        Call<Course> call = apiService.updateCourse(updatedCourse.getCourseid(), updatedCourse);

        call.enqueue(new Callback<Course>() {
            @Override
            public void onResponse(Call<Course> call, Response<Course> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CourseUpdateActivity.this, "Course updated successfully.", Toast.LENGTH_SHORT).show();
                    finish();  // Navigate back to CourseDetailActivity
                } else {
                    Toast.makeText(CourseUpdateActivity.this, "Failed to update course.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Course> call, Throwable t) {
                Toast.makeText(CourseUpdateActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storePendingRequest(Course updatedCourse) {
        // Store the course update in the pending requests table for later syncing
        dbHelper.storePendingRequest("updateCourse", updatedCourse);
    }

    // Method to delete the course and its associated classes
    private void deleteCourseAndClasses() {
        // Check for network availability before proceeding with the delete
        if (NetworkUtils.isConnectedToInternet(this)) {
            deleteCourseFromBackend();
        } else {
            storePendingRequestForDelete(courseId);
            Toast.makeText(this, "No internet connection. Course delete saved for later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCourseFromBackend() {
        Log.d("CourseUpdateActivity", "Attempting to delete course with ID: " + courseId);

        // Use Retrofit to delete the course in the backend
        APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);
        Call<Void> call = apiService.deleteCourse(courseId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("CourseUpdateActivity", "Delete response code: " + response.code());  // Log response code
                if (response.isSuccessful()) {
                    Toast.makeText(CourseUpdateActivity.this, "Course and its classes deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish();  // Navigate back to CourseListActivity
                } else {
                    Toast.makeText(CourseUpdateActivity.this, "Failed to delete course: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("CourseUpdateActivity", "Error deleting course: " + response.message());  // Log the error message
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("CourseUpdateActivity", "Error: " + t.getMessage());  // Log the error message
                Toast.makeText(CourseUpdateActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storePendingRequestForDelete(int courseId) {
        // Store the delete request in the pending requests table for later syncing
        dbHelper.storePendingRequest("deleteCourse", courseId);
    }

    // Method to delete a specific class
    private void deleteClass(int courseid, int classid) {
        APIService apiService = RetrofitClient.getRetrofitInstance().create(APIService.class);
        Call<Void> call = apiService.deleteClass(courseid, classid);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CourseUpdateActivity.this, "Class deleted successfully.", Toast.LENGTH_SHORT).show();
                    loadClassList(courseid);  // Refresh the class list after deletion
                } else {
                    Toast.makeText(CourseUpdateActivity.this, "Failed to delete class.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CourseUpdateActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}