package com.universalyoga.yogaadminapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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
    private TextView advancedSearchText;
    private Spinner dayOfWeekSpinner;  // Spinner for Day of the Week filter
    private ArrayAdapter<Class> classAdapter;
    private List<Class> classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText);
        searchResultsListView = findViewById(R.id.searchResultsListView);
        advancedSearchText = findViewById(R.id.advancedSearchText);
        dayOfWeekSpinner = findViewById(R.id.dayOfWeekSpinner);

        // Set up the adapter for the ListView
        classList = new ArrayList<>();
        classAdapter = new ClassAdapter(this, classList);
        searchResultsListView.setAdapter(classAdapter);

        // Set up Day of Week spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(spinnerAdapter);

        // Initially load all classes
        loadAllClasses();

        // Handle "Advanced Search" click
        advancedSearchText.setOnClickListener(v -> {
            // Toggle the visibility of the spinner
            if (dayOfWeekSpinner.getVisibility() == View.GONE) {
                dayOfWeekSpinner.setVisibility(View.VISIBLE);  // Show Spinner
            } else {
                dayOfWeekSpinner.setVisibility(View.GONE);  // Hide Spinner
            }
        });

        // TextWatcher to filter classes based on teacher name
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this use case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Get the teacher name from the EditText
                String teacherName = charSequence.toString().trim();
                // Filter the classes based on user input and selected day of week
                filterClasses(teacherName, getSelectedDayOfWeek());
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

    // Method to filter classes by teacher name and day of week
    private void filterClasses(String teacherName, String dayOfWeek) {
        if (teacherName.isEmpty() && dayOfWeek.isEmpty()) {
            // If no input, show all classes
            loadAllClasses();
        } else {
            // Otherwise, filter classes based on teacher name and selected day of week
            List<Class> filteredClasses;
            if (!teacherName.isEmpty() && !dayOfWeek.isEmpty()) {
                filteredClasses = dbHelper.getClassesByTeacherNameAndDay(teacherName, dayOfWeek);
            } else if (!teacherName.isEmpty()) {
                filteredClasses = dbHelper.getClassesByTeacherName(teacherName);
            } else {
                filteredClasses = dbHelper.getClassesByDayOfWeek(dayOfWeek);
            }

            if (filteredClasses != null && !filteredClasses.isEmpty()) {
                classList.clear();
                classList.addAll(filteredClasses);
                classAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No classes found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to get selected day of the week from the spinner
    private String getSelectedDayOfWeek() {
        return (dayOfWeekSpinner.getVisibility() == View.VISIBLE) ?
                dayOfWeekSpinner.getSelectedItem().toString() : "";
    }
}