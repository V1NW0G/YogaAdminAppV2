package com.universalyoga.yogaadminapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.models.Class;
import com.universalyoga.yogaadminapp.adapter.ClassAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private YogaDatabaseHelper dbHelper;
    private ListView coursesListView;
    private Button addButton;
    private Button editButton;  // Edit button to toggle edit mode
    private TextView noCoursesTextView;
    private CourseAdapter courseAdapter;  // Custom adapter to handle courses and "Add Class" button
    private View actionButtonsLayout;  // Layout containing the "Add", "Update", "Delete" buttons
    private boolean isEditMode = false;  // Flag to indicate if in edit mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new YogaDatabaseHelper(this);
        coursesListView = findViewById(R.id.coursesListView);
        addButton = findViewById(R.id.addButton);
        editButton = findViewById(R.id.editButton);
        noCoursesTextView = findViewById(R.id.noCoursesTextView);
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout);  // The layout containing Add, Update, Delete buttons

        // Load and display courses when MainActivity is created
        loadCoursesFromDatabase();

        // Navigate to AddCourseActivity when Add Button is clicked
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddCourseActivity.class);
            startActivity(intent);
        });

        // Handle the "Edit" button click to toggle edit mode
        editButton.setOnClickListener(v -> toggleEditMode());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload the courses whenever MainActivity comes back to the foreground
        loadCoursesFromDatabase();
    }

    // Load all courses from the database and display in ListView
    private void loadCoursesFromDatabase() {
        List<Course> courses = dbHelper.getAllCourses();

        if (courses != null && !courses.isEmpty()) {
            // Use the custom adapter to display courses and the "Add Class" button
            courseAdapter = new CourseAdapter(this, courses);
            coursesListView.setAdapter(courseAdapter);
            coursesListView.setVisibility(ListView.VISIBLE);
            noCoursesTextView.setVisibility(TextView.GONE); // Hide the "no courses" message
        } else {
            // Show the no courses message and hide the ListView
            coursesListView.setVisibility(ListView.GONE);
            noCoursesTextView.setVisibility(TextView.VISIBLE);
        }
    }

    // Custom adapter for courses
    public class CourseAdapter extends ArrayAdapter<Course> {

        public CourseAdapter(MainActivity context, List<Course> courses) {
            super(context, 0, courses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.course_item, parent, false);
            }

            Course currentCourse = getItem(position);

            TextView courseIdTextView = convertView.findViewById(R.id.courseIdTextView);
            Button addClassButton = convertView.findViewById(R.id.addClassButton);  // "Add Class" button
            Button viewDetailsButton = convertView.findViewById(R.id.viewDetailsButton);  // "View Details" button
            TextView noClassesTextView = convertView.findViewById(R.id.noClassesTextView); // TextView for no classes message
            ListView classesListView = convertView.findViewById(R.id.classesListView);  // ListView for classes

            // Set the course ID text
            if (currentCourse != null) {
                courseIdTextView.setText("Course ID: " + currentCourse.getCourseid());
            }

            // Fetch the list of classes for the course (List<Class>)
            List<Class> classes = dbHelper.getClassesForCourse(currentCourse.getCourseid());  // Now using List<Class>

            if (classes != null && !classes.isEmpty()) {
                // Classes are available, hide "No classes available" message and show class details
                noClassesTextView.setVisibility(View.GONE);
                ClassAdapter classAdapter = new ClassAdapter(MainActivity.this, classes);
                classesListView.setAdapter(classAdapter);
                classesListView.setVisibility(View.VISIBLE);
            } else {
                // No classes for this course
                noClassesTextView.setVisibility(View.VISIBLE);
                classesListView.setVisibility(View.GONE);
            }

            // Set up the "Add Class" button click listener
            addClassButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddClassActivity.class);
                intent.putExtra("courseId", currentCourse.getCourseid());  // Pass course ID to AddClassActivity
                startActivity(intent);
            });

            // Set up the "View Details" button click listener
            viewDetailsButton.setOnClickListener(v -> {
                if (currentCourse != null) {
                    Intent intent = new Intent(MainActivity.this, CourseDetailActivity.class);
                    int courseId = currentCourse.getCourseid();
                    Log.d("MainActivity", "Passing courseId: " + courseId);  // Log the courseId to debug
                    intent.putExtra("courseId", String.valueOf(courseId));  // Pass courseId as a String
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    // Toggle the visibility of the Add, Update, Delete buttons and update the Edit button text
    private void toggleEditMode() {
        if (isEditMode) {
            // If currently in edit mode, hide the action buttons
            actionButtonsLayout.setVisibility(View.GONE);
            editButton.setText("Edit");  // Change text back to Edit
        } else {
            // If not in edit mode, show the action buttons
            actionButtonsLayout.setVisibility(View.VISIBLE);
            editButton.setText("Cancel Edit");  // Change text to Cancel Edit
        }
        isEditMode = !isEditMode;  // Toggle the flag
    }
}