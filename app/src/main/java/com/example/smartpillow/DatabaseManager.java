package com.example.smartpillow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class DatabaseManager {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // UPDATED: Added COLUMN_SLEEP_SCORE to initialization (defaults to 0 or -1)
    public void insert(String username, String password, String email, String phone, String gender, int age, int height, int weight, int sleep_duration, int sleep_quality) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_USERNAME, username);
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        contentValues.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);
        contentValues.put(DatabaseHelper.COLUMN_EMAIL, email);
        contentValues.put(DatabaseHelper.COLUMN_PHONE, phone);
        contentValues.put(DatabaseHelper.COLUMN_GENDER, gender);
        contentValues.put(DatabaseHelper.COLUMN_AGE, age);
        contentValues.put(DatabaseHelper.COLUMN_HEIGHT, height);
        contentValues.put(DatabaseHelper.COLUMN_WEIGHT, weight);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_DURATION, sleep_duration);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_QUALITY, sleep_quality);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_SCORE, 0); // Initialize at 0
        db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    // UPDATED: Added COLUMN_SLEEP_SCORE to the query array
    public Cursor fetch() {
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
                DatabaseHelper.COLUMN_SLEEP_QUALITY,
                DatabaseHelper.COLUMN_SLEEP_SCORE // Added for demo
        };
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    // UPDATED: Added score to main update method
    public int update(long id, String username, String password, String email, String phone, String gender, int age, int height, int weight, int sleep_duration, int sleep_quality, int sleep_score) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_USERNAME, username);
        if (password != null && !password.isEmpty()) {
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            contentValues.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);
        }
        contentValues.put(DatabaseHelper.COLUMN_EMAIL, email);
        contentValues.put(DatabaseHelper.COLUMN_PHONE, phone);
        contentValues.put(DatabaseHelper.COLUMN_GENDER, gender);
        contentValues.put(DatabaseHelper.COLUMN_AGE, age);
        contentValues.put(DatabaseHelper.COLUMN_HEIGHT, height);
        contentValues.put(DatabaseHelper.COLUMN_WEIGHT, weight);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_DURATION, sleep_duration);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_QUALITY, sleep_quality);
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_SCORE, sleep_score);
        return db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + "=" + id, null);
    }

    public int updateSleepDuration(long id, int sleep_duration) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_DURATION, sleep_duration);
        return db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + "=" + id, null);
    }

    public void delete(long id) {
        db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=" + id, null);
    }

    public boolean checkPassword(String username, String password) {
        String[] columns = {DatabaseHelper.COLUMN_PASSWORD};
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int passwordColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD);
            if (passwordColumnIndex != -1) {
                String hashedPassword = cursor.getString(passwordColumnIndex);
                cursor.close();
                BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
                return result.verified;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    // --- ANA: SCORING SYSTEM LOGIC ---

    public int calculateAndSaveSleepScore(long userId, int durationMinutes, int qualityRating) {
        // 1. The Scoring Algorithm (Weighted Heuristics)
        double durationScore = (durationMinutes / 480.0) * 100;
        if (durationScore > 100) durationScore = 100;

        // Calculate final weighted score (70/30 split)
        int finalScore = (int) ((durationScore * 0.7) + (qualityRating * 10 * 0.3));

        // 2. Prepare Data for Database
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SLEEP_DURATION, durationMinutes);
        values.put(DatabaseHelper.COLUMN_SLEEP_QUALITY, qualityRating);
        values.put(DatabaseHelper.COLUMN_SLEEP_SCORE, finalScore);

        // 3. Update the record for the specific user
        int rowsAffected = db.update(DatabaseHelper.TABLE_NAME, values,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)});

        Log.d("SleepScoring", "Calculated Score for User " + userId + ": " + finalScore);

        return finalScore;
    }
}