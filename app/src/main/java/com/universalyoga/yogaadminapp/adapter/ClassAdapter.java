package com.universalyoga.yogaadminapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;

import java.util.List;

public class ClassAdapter extends ArrayAdapter<Class> {

    private Context context;
    private List<Class> classes;
    private YogaDatabaseHelper dbHelper;

    public ClassAdapter(Context context, List<Class> classes) {
        super(context, 0, classes);
        this.context = context;
        this.classes = classes;
        this.dbHelper = new YogaDatabaseHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.class_item, parent, false);
        }

        // Get the current class object
        Class currentClass = getItem(position);

        TextView classIdTextView = convertView.findViewById(R.id.classIdTextView);
        TextView classDateTextView = convertView.findViewById(R.id.classDateTextView);
        TextView classTeacherTextView = convertView.findViewById(R.id.classTeacherTextView);
        TextView classCommentTextView = convertView.findViewById(R.id.classCommentTextView);
        TextView classDayOfWeekTextView = convertView.findViewById(R.id.classDayOfWeekTextView);

        // Set the class details
        if (currentClass != null) {
            classIdTextView.setText("Class ID: " + currentClass.getClassid());
            // Fetch the course related to this class and get the day of the week
            String dayOfWeek = dbHelper.getCourseById(currentClass.getCourseid()).getDayOfWeek();
            classDayOfWeekTextView.setText(dayOfWeek);
            classDateTextView.setText("Date: " + currentClass.getDate());
            classTeacherTextView.setText("Teacher: " + currentClass.getTeacher());
            classCommentTextView.setText("Comment: " + currentClass.getComment());
        }

        return convertView;
    }
}