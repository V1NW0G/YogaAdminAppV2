package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;
import com.universalyoga.yogaadminapp.adapter.ClassAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private YogaDatabaseHelper dbHelper;
    private EditText searchEditText;
    private ListView searchResultsListView;
    private List<Class> classList;
    private ClassAdapter classAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search); // Define this layout

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText);
        searchResultsListView = findViewById(R.id.searchResultsListView);

        // Set up the adapter for the ListView
        classList = new ArrayList<>();
        classAdapter = new ClassAdapter(this, classList);
        searchResultsListView.setAdapter(classAdapter);

        // Initially load all classes
        loadAllClasses();

        // TextWatcher to filter classes based on user input
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this use case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Get the teacher name from the EditText
                String teacherName = charSequence.toString().trim();
                // Filter the classes based on user input
                filterClassesByTeacher(teacherName);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this use case
            }
        });
    }

    // Method to load all classes from the database
    private void loadAllClasses() {
        List<Class> classes = dbHelper.getAllClasses(); // Assuming you have a method for this
        if (classes != null && !classes.isEmpty()) {
            classList.clear();
            classList.addAll(classes);
            classAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No classes available.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to filter classes by teacher name
    private void filterClassesByTeacher(String teacherName) {
        if (teacherName.isEmpty()) {
            // If no input, show all classes
            loadAllClasses();
        } else {
            // Otherwise, filter classes based on the teacher name
            List<Class> filteredClasses = dbHelper.getClassesByTeacherName(teacherName);
            if (filteredClasses != null && !filteredClasses.isEmpty()) {
                classList.clear();
                classList.addAll(filteredClasses);
                classAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No classes found for this teacher", Toast.LENGTH_SHORT).show();
            }
        }
    }
}