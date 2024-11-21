package com.universalyoga.yogaadminapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.universalyoga.yogaadminapp.R;
import com.universalyoga.yogaadminapp.helper.YogaDatabaseHelper;
import com.universalyoga.yogaadminapp.models.Class;
import com.universalyoga.yogaadminapp.adapter.ClassAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class SearchActivity extends AppCompatActivity {

    private YogaDatabaseHelper dbHelper;
    private EditText searchEditText;
    private ListView searchResultsListView;
    private TextView noClassesTextView;
    private Button clearButton;
    private Button datePickerButton;
    private TextView selectedDateTextView;
    private ArrayAdapter<Class> classAdapter;
    private List<Class> classList;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new YogaDatabaseHelper(this);

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText);
        searchResultsListView = findViewById(R.id.searchResultsListView);
        noClassesTextView = findViewById(R.id.noClassesTextView);
        clearButton = findViewById(R.id.clearButton);
        datePickerButton = findViewById(R.id.datePickerButton);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);

        // Set up the adapter for the ListView
        classList = new ArrayList<>();
        classAdapter = new ClassAdapter(this, classList);
        searchResultsListView.setAdapter(classAdapter);

        // Initially load all classes
        loadAllClasses();

        // TextWatcher to filter classes based on teacher name
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String teacherName = charSequence.toString().trim();
                filterClasses(teacherName, selectedDate);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Set the OnItemClickListener for the ListView
        searchResultsListView.setOnItemClickListener((parent, view, position, id) -> {
            Class clickedClass = classList.get(position);
            Intent intent = new Intent(SearchActivity.this, ClassDetailActivity.class);
            intent.putExtra("classId", clickedClass.getClassid());
            startActivity(intent);
        });

        // DatePicker button logic
        datePickerButton.setOnClickListener(v -> showDatePickerDialog());

        // Clear Filters button
        clearButton.setOnClickListener(v -> clearFilters());
    }

    // Method to toggle visibility of the advanced search section
    public void toggleAdvancedSearch(View view) {
        View advancedSearchLayout = findViewById(R.id.advancedSearchLayout);
        if (advancedSearchLayout.getVisibility() == View.VISIBLE) {
            advancedSearchLayout.setVisibility(View.GONE);
        } else {
            advancedSearchLayout.setVisibility(View.VISIBLE);
        }
    }

    // Method to load all classes from the database
    private void loadAllClasses() {
        List<Class> classes = dbHelper.getAllClasses();
        if (classes != null && !classes.isEmpty()) {
            classList.clear();
            classList.addAll(classes);
            classAdapter.notifyDataSetChanged();
            searchResultsListView.setVisibility(View.VISIBLE);
            noClassesTextView.setVisibility(View.GONE);
        } else {
            searchResultsListView.setVisibility(View.GONE);
            noClassesTextView.setVisibility(View.VISIBLE);
            noClassesTextView.setText("No classes available.");
        }
    }

    // Method to filter classes by teacher name and selected date
    private void filterClasses(String teacherName, String date) {
        List<Class> filteredClasses;

        if (teacherName.isEmpty() && date.isEmpty()) {
            filteredClasses = dbHelper.getAllClasses();
        } else if (!teacherName.isEmpty() && !date.isEmpty()) {
            filteredClasses = dbHelper.getClassesByTeacherNameAndDate(teacherName, date);
        } else if (!teacherName.isEmpty()) {
            filteredClasses = dbHelper.getClassesByTeacherName(teacherName);
        } else {
            filteredClasses = dbHelper.getClassesByDate(date);
        }

        // Update the list based on the filtered classes
        if (filteredClasses != null && !filteredClasses.isEmpty()) {
            classList.clear();
            classList.addAll(filteredClasses);
            classAdapter.notifyDataSetChanged();
            searchResultsListView.setVisibility(View.VISIBLE);
            noClassesTextView.setVisibility(View.GONE);
        } else {
            searchResultsListView.setVisibility(View.GONE);
            noClassesTextView.setVisibility(View.VISIBLE);
            noClassesTextView.setText("No classes found for the selected filter.");
        }
    }

    // Method to show DatePicker dialog and update the selected date
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SearchActivity.this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // Format the selected date
                    selectedDate = String.format("%02d/%02d/%04d", selectedDayOfMonth, selectedMonth + 1, selectedYear);
                    selectedDateTextView.setText("Selected Date: " + selectedDate);
                    filterClasses(searchEditText.getText().toString(), selectedDate);
                },
                year, month, dayOfMonth
        );
        datePickerDialog.show();
    }

    // Method to clear the filters
    private void clearFilters() {
        // Clear the search text and reset the date
        searchEditText.setText("");
        selectedDate = "";
        selectedDateTextView.setText("Selected Date: None");

        // Reload all classes (no filters applied)
        loadAllClasses();
    }
}