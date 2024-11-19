package com.universalyoga.yogaadminapp.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.universalyoga.yogaadminapp.models.Course;
import com.universalyoga.yogaadminapp.models.Class;  // Ensure that Class model is imported for the class details
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class YogaDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "yoga.db";
    private static final int DATABASE_VERSION = 6;  // Incremented version for adding classes table

    // Courses Table
    private static final String TABLE_COURSES = "courses";
    private static final String KEY_COURSE_ID = "courseid";  // Primary Key for courses table
    private static final String KEY_DAY_OF_WEEK = "day_of_week";
    private static final String KEY_TIME = "time";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_CAPACITY = "capacity";
    private static final String KEY_PRICE = "price";
    private static final String KEY_TYPE = "type";
    private static final String KEY_DESCRIPTION = "description";

    // Classes Table
    private static final String TABLE_CLASSES = "classes";
    private static final String KEY_CLASS_ID = "classid";  // Primary Key for classes table
    private static final String KEY_COURSE_FOREIGN_KEY = "courseid";  // Foreign Key referencing courses
    private static final String KEY_DATE = "date";
    private static final String KEY_TEACHER = "teacher";
    private static final String KEY_COMMENT = "comment";

    // Pending Requests Table (for retry mechanism)
    private static final String TABLE_PENDING_REQUESTS = "pending_requests";
    private static final String KEY_PENDING_ID = "id";
    private static final String KEY_REQUEST_ACTION = "action";  // "add", "update", "delete"
    private static final String KEY_COURSE_DATA = "course_data";  // Store the course data as a JSON string (or serialized format)

    public YogaDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Courses Table
        String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + " ("
                + KEY_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_DAY_OF_WEEK + " TEXT, "
                + KEY_TIME + " TEXT, "
                + KEY_DURATION + " INTEGER, "
                + KEY_CAPACITY + " INTEGER, "
                + KEY_PRICE + " REAL, "
                + KEY_TYPE + " TEXT, "
                + KEY_DESCRIPTION + " TEXT)";
        db.execSQL(CREATE_COURSES_TABLE);

        // Create Classes Table
        String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES + " ("
                + KEY_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_COURSE_FOREIGN_KEY + " INTEGER, "
                + KEY_DATE + " TEXT, "
                + KEY_TEACHER + " TEXT, "
                + KEY_COMMENT + " TEXT, "
                + "FOREIGN KEY(" + KEY_COURSE_FOREIGN_KEY + ") REFERENCES " + TABLE_COURSES + "(" + KEY_COURSE_ID + "))";
        db.execSQL(CREATE_CLASSES_TABLE);

        // Create Pending Requests Table
        String CREATE_PENDING_REQUESTS_TABLE = "CREATE TABLE " + TABLE_PENDING_REQUESTS + " ("
                + KEY_PENDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_REQUEST_ACTION + " TEXT, "
                + KEY_COURSE_DATA + " TEXT)";
        db.execSQL(CREATE_PENDING_REQUESTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // If upgrading to version 2
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PENDING_REQUESTS + " ("
                    + KEY_PENDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_REQUEST_ACTION + " TEXT, "
                    + KEY_COURSE_DATA + " TEXT)");
        }
    }

    public Course getCourseById(int courseId) {
        Course course = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_COURSES + " WHERE " + KEY_COURSE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(courseId)});

        if (cursor != null && cursor.moveToFirst()) {
            course = new Course(
                    cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_DAY_OF_WEEK)),
                    cursor.getString(cursor.getColumnIndex(KEY_TIME)),
                    cursor.getInt(cursor.getColumnIndex(KEY_DURATION)),
                    cursor.getInt(cursor.getColumnIndex(KEY_CAPACITY)),
                    cursor.getDouble(cursor.getColumnIndex(KEY_PRICE)),
                    cursor.getString(cursor.getColumnIndex(KEY_TYPE)),
                    cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION))
            );
            cursor.close();
        }

        db.close();
        return course;
    }

    // Add a new course to the courses table
    public void addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COURSE_ID, course.getCourseid());
        values.put(KEY_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(KEY_TIME, course.getTime());
        values.put(KEY_DURATION, course.getDuration());
        values.put(KEY_CAPACITY, course.getCapacity());
        values.put(KEY_PRICE, course.getPrice());
        values.put(KEY_TYPE, course.getType());
        values.put(KEY_DESCRIPTION, course.getDescription());
        db.insert(TABLE_COURSES, null, values);

        // Store the course update in pending requests (if offline)
        storePendingRequest("add", course);
        db.close();
    }

    // Add a new class to the classes table
    public boolean addClass(int classid, int courseId, String date, String teacher, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if classid already exists
        String query = "SELECT * FROM " + TABLE_CLASSES + " WHERE " + KEY_CLASS_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(classid)});

        if (cursor.getCount() > 0) {
            cursor.close();
            return false;  // Class ID already exists
        }

        cursor.close();

        // Insert the new class if classid is unique
        ContentValues values = new ContentValues();
        values.put(KEY_CLASS_ID, classid);  // Now you can insert classid
        values.put(KEY_COURSE_FOREIGN_KEY, courseId);
        values.put(KEY_DATE, date);
        values.put(KEY_TEACHER, teacher);
        values.put(KEY_COMMENT, comment);

        long result = db.insert(TABLE_CLASSES, null, values);
        db.close();

        return result != -1;
    }

    // Store pending requests (e.g., for retry mechanism)
    // In YogaDatabaseHelper.java
    public void storePendingRequest(String action, Object object) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REQUEST_ACTION, action);

        if (object instanceof Course) {
            values.put(KEY_COURSE_DATA, courseToJson((Course) object));  // Serialize Course
        } else if (object instanceof Class) {
            values.put(KEY_COURSE_DATA, classToJson((Class) object));  // Serialize Class
        }

        db.insert(TABLE_PENDING_REQUESTS, null, values);
        db.close();
    }

    // Helper method to convert Course object to JSON
    private String courseToJson(Course course) {
        return new Gson().toJson(course);
    }

    // Helper method to convert Class object to JSON
    private String classToJson(Class aClass) {
        return new Gson().toJson(aClass);
    }

    // Retrieve all pending requests
    public List<String> getPendingRequests() {
        List<String> pendingRequests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_PENDING_REQUESTS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String courseData = cursor.getString(cursor.getColumnIndex(KEY_COURSE_DATA));
                pendingRequests.add(courseData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return pendingRequests;
    }

    // Delete a pending request after it is successfully processed
    public void deletePendingRequest(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PENDING_REQUESTS, KEY_PENDING_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Get all courses from the database
    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COURSES;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Course course = new Course(
                        cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_DAY_OF_WEEK)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(KEY_CAPACITY)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_PRICE)),
                        cursor.getString(cursor.getColumnIndex(KEY_TYPE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION))
                );
                courseList.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return courseList;
    }

    // Get all classes for a specific course
    public List<Class> getClassesForCourse(int courseId) {
        List<Class> classList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CLASSES + " WHERE " + KEY_COURSE_FOREIGN_KEY + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(courseId)});

        if (cursor.moveToFirst()) {
            do {
                Class aClass = new Class(
                        cursor.getInt(cursor.getColumnIndex(KEY_CLASS_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_COURSE_FOREIGN_KEY)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_TEACHER)),
                        cursor.getString(cursor.getColumnIndex(KEY_COMMENT))
                );
                classList.add(aClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return classList;
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete all classes first (if any)
        db.delete(TABLE_CLASSES, null, null);

        // Delete all courses
        db.delete(TABLE_COURSES, null, null);

        db.close();
    }
}