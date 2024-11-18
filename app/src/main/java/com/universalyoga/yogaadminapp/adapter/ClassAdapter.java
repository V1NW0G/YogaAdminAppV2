package com.universalyoga.yogaadminapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.models.Class;

import java.util.List;

public class ClassAdapter extends ArrayAdapter<Class> {

    private Context context;
    private List<Class> classes;

    public ClassAdapter(Context context, List<Class> classes) {
        super(context, 0, classes);
        this.context = context;
        this.classes = classes;
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

        // Set the class details
        if (currentClass != null) {
            classIdTextView.setText("Class ID: " + currentClass.getClassid());
            classDateTextView.setText("Date: " + currentClass.getDate());
            classTeacherTextView.setText("Teacher: " + currentClass.getTeacher());
            classCommentTextView.setText("Comment: " + currentClass.getComment());
        }

        return convertView;
    }
}