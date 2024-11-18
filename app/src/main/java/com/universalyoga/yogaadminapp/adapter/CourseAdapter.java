package com.universalyoga.yogaadminapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.activities.AddClassActivity;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Course;

import java.util.List;

public class CourseAdapter extends ArrayAdapter<Course> {

    private Context context;
    private List<Course> courses;
    private YogaDatabaseHelper dbHelper;

    public CourseAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
        this.context = context;
        this.courses = courses;
        this.dbHelper = new YogaDatabaseHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.course_item, parent, false);
        }

        Course currentCourse = getItem(position);

        TextView courseIdTextView = convertView.findViewById(R.id.courseIdTextView);
        Button addClassButton = convertView.findViewById(R.id.addClassButton);
        TextView noClassesTextView = convertView.findViewById(R.id.noClassesTextView);
        TextView classesTextView = convertView.findViewById(R.id.classesTextView);

        // Set the course ID text
        if (currentCourse != null) {
            courseIdTextView.setText("Course ID: " + currentCourse.getCourseid());
        }

        // Check if there are any classes under this course
        List<String> classes = dbHelper.getClassesForCourse(currentCourse.getCourseid());
        if (classes != null && !classes.isEmpty()) {
            // Classes are available, hide no classes text and show class details
            noClassesTextView.setVisibility(View.GONE);
            StringBuilder classDetails = new StringBuilder();
            for (String classInfo : classes) {
                classDetails.append(classInfo).append("\n");
            }
            classesTextView.setText(classDetails.toString());
            classesTextView.setVisibility(View.VISIBLE);
        } else {
            // No classes for this course
            noClassesTextView.setVisibility(View.VISIBLE);
            classesTextView.setVisibility(View.GONE);
        }

        // Set up the "Add Class" button click listener
        addClassButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddClassActivity.class);
            intent.putExtra("courseId", currentCourse.getCourseid());  // Pass course ID to AddClassActivity
            context.startActivity(intent);
        });

        return convertView;
    }
}