package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;

public class ClassDetailActivity extends AppCompatActivity {

    private YogaDatabaseHelper dbHelper;
    private TextView classIdTextView, courseIdTextView, classDateTextView, classTeacherTextView, classCommentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize UI elements
        classIdTextView = findViewById(R.id.classIdTextView);
        courseIdTextView = findViewById(R.id.courseIdTextView);
        classDateTextView = findViewById(R.id.classDateTextView);
        classTeacherTextView = findViewById(R.id.classTeacherTextView);
        classCommentTextView = findViewById(R.id.classCommentTextView);

        // Get the classId passed from the previous activity
        int classId = getIntent().getIntExtra("classId", -1);

        if (classId != -1) {
            // Fetch class details by classId
            Class clickedClass = dbHelper.getClassById(classId);

            if (clickedClass != null) {
                // Display class details
                classIdTextView.setText("Class ID: " + clickedClass.getClassid());
                courseIdTextView.setText("Course ID: " + clickedClass.getCourseid());
                classDateTextView.setText("Date: " + clickedClass.getDate());
                classTeacherTextView.setText("Teacher: " + clickedClass.getTeacher());
                classCommentTextView.setText("Comment: " + clickedClass.getComment());
            } else {
                // Show a message if class not found
                Toast.makeText(this, "Class not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid class ID", Toast.LENGTH_SHORT).show();
        }
    }
}