package com.universalyoga.yogaadminapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.universalyoga.yogaadminapp.models.Course;
import java.util.ArrayList;
import java.util.List;

public class YogaDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yoga_admin.db";
    private static final int DATABASE_VERSION = 14;

    // Table for courses
    public static final String TABLE_COURSES = "courses";
    public static final String COLUMN_COURSEID = "courseid";
    public static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DESCRIPTION = "description";

    public YogaDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create courses table
        String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
                + COLUMN_COURSEID + " INTEGER PRIMARY KEY,"
                + COLUMN_DAY_OF_WEEK + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_DURATION + " INTEGER,"
                + COLUMN_CAPACITY + " INTEGER,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT"
                + ")";
        db.execSQL(CREATE_COURSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(db);
    }

    // Check if the courses table is empty
    public boolean isCoursesTableEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COURSES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count == 0;
    }

    // Insert a course into the database
    public void addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSEID, course.getCourseid());
        values.put(COLUMN_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COLUMN_TIME, course.getTime());
        values.put(COLUMN_DURATION, course.getDuration());
        values.put(COLUMN_CAPACITY, course.getCapacity());
        values.put(COLUMN_PRICE, course.getPrice());
        values.put(COLUMN_TYPE, course.getType());
        values.put(COLUMN_DESCRIPTION, course.getDescription());

        db.insert(TABLE_COURSES, null, values);
        db.close();
    }

    // Get all courses
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSES, null);
        if (cursor.moveToFirst()) {
            do {
                int courseid = cursor.getInt(cursor.getColumnIndex(COLUMN_COURSEID));
                String dayOfWeek = cursor.getString(cursor.getColumnIndex(COLUMN_DAY_OF_WEEK));
                String time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME));
                int duration = cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION));
                int capacity = cursor.getInt(cursor.getColumnIndex(COLUMN_CAPACITY));
                double price = cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE));
                String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));

                Course course = new Course(courseid, dayOfWeek, time, duration, capacity, price, type, description);
                courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courses;
    }
}
