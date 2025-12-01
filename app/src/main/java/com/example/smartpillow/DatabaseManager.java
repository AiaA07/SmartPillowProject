package com.example.smartpillow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLDataException;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    // FIX 1: Store the context in constructor
    public DatabaseManager(Context context) {
        this.context = context;  // ← STORE THE CONTEXT!
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);  // Now context is available
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // FIX 2: Remove the null ContentValues
    public void insert(String username, String password, String email, String phone,
                       String gender, int age, int height, int weight,
                       int sleep_duration, int sleep_quality) {

        // Make sure dbHelper is initialized
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // FIXED: Initialize ONE ContentValues object, not three
        ContentValues contentValues = new ContentValues();  // ← NOT NULL!

        // Add ALL fields, not just username and password
        contentValues.put(DatabaseHelper.COLUMN_USERNAME, username);
        contentValues.put(DatabaseHelper.COLUMN_PASSWORD, password);
        contentValues.put(DatabaseHelper.COLUMN_EMAIL, email);
        contentValues.put(DatabaseHelper.COLUMN_PHONE, phone);
        contentValues.put(DatabaseHelper.COLUMN_GENDER, gender);
        contentValues.put(DatabaseHelper.COLUMN_AGE, age);
        contentValues.put(DatabaseHelper.COLUMN_HEIGHT, height);
        contentValues.put(DatabaseHelper.COLUMN_WEIGHT, weight);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_DURATION, sleep_duration);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_QUALITY, sleep_quality);

        // Insert and check result
        long result = database.insert(DatabaseHelper.TABLE_NAME, null, contentValues);

        if (result == -1) {
            android.util.Log.e("DatabaseManager", "Insert failed for user: " + username);
        } else {
            android.util.Log.d("DatabaseManager", "Insert successful, row id: " + result);
        }
    }

    public Cursor fetch() {
        // Make sure dbHelper is initialized
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
        }

        String[] columns = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_EMAIL,
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_GENDER,
                DatabaseHelper.COLUMN_AGE,
                DatabaseHelper.COLUMN_HEIGHT,
                DatabaseHelper.COLUMN_WEIGHT,
                DatabaseHelper.COLUMN_SLEEP_DURATION,
                DatabaseHelper.COLUMN_SLEEP_QUALITY
        };

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns,
                null, null, null, null, null);

        // Don't moveToFirst() here - let the caller handle cursor positioning
        return cursor;
    }

    public int update(long id, String username, String password, String email,
                      String phone, String gender, int age, int height, int weight,
                      int sleep_duration, int sleep_quality) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_USERNAME, username);
        contentValues.put(DatabaseHelper.COLUMN_PASSWORD, password);
        contentValues.put(DatabaseHelper.COLUMN_EMAIL, email);
        contentValues.put(DatabaseHelper.COLUMN_PHONE, phone);
        contentValues.put(DatabaseHelper.COLUMN_GENDER, gender);
        contentValues.put(DatabaseHelper.COLUMN_AGE, age);
        contentValues.put(DatabaseHelper.COLUMN_HEIGHT, height);
        contentValues.put(DatabaseHelper.COLUMN_WEIGHT, weight);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_DURATION, sleep_duration);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_QUALITY, sleep_quality);

        // FIX: Use parameterized query to prevent SQL injection
        int ret = db.update(DatabaseHelper.TABLE_NAME, contentValues,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        return ret;
    }

    public void delete(long id) {
        // FIX: Use parameterized query
        db.delete(DatabaseHelper.TABLE_NAME,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // ADD THIS HELPER METHOD: Check if user exists
    public boolean checkUser(String username, String password) {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(context);
        }

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? AND " +
                DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME,
                new String[]{DatabaseHelper.COLUMN_ID},
                selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}