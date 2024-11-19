package com.universalyoga.yogaadminapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.activities.AddClassActivity;
import com.universalyoga.yogaadminapp.activities.CourseDetailActivity;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.models.Class;

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
        Button viewDetailsButton = convertView.findViewById(R.id.viewDetailsButton);
        TextView noClassesTextView = convertView.findViewById(R.id.noClassesTextView);
        TextView classesTextView = convertView.findViewById(R.id.classesTextView);

        // Set the course ID text
        if (currentCourse != null) {
            courseIdTextView.setText("Course ID: " + currentCourse.getCourseid());
        }

        // Fetch the list of classes for the course (List<Class>)
        List<Class> classes = dbHelper.getClassesForCourse(currentCourse.getCourseid());  // Now using List<Class>

        if (classes != null && !classes.isEmpty()) {
            noClassesTextView.setVisibility(View.GONE);  // Hide "No classes available" message
            StringBuilder classDetails = new StringBuilder();
            for (Class aClass : classes) {
                // Append class details (Class ID, Date, Teacher, Comment)
                classDetails.append("Class ID: ").append(aClass.getClassid())
                        .append("\nDate: ").append(aClass.getDate())
                        .append("\nTeacher: ").append(aClass.getTeacher())
                        .append("\nComment: ").append(aClass.getComment())
                        .append("\n\n");  // Separate each class details with a new line
            }
            classesTextView.setText(classDetails.toString());
            classesTextView.setVisibility(View.VISIBLE);  // Show the class details
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

        // Set up the "View Details" button click listener
        viewDetailsButton.setOnClickListener(v -> {
            if (currentCourse != null) {
                Intent intent = new Intent(context, CourseDetailActivity.class);
                int courseId = currentCourse.getCourseid();
                Log.d("CourseActivity", "Passing courseId: " + courseId);  // Log the courseId to debug
                intent.putExtra("courseId", String.valueOf(courseId));  // Pass courseId as a String
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}