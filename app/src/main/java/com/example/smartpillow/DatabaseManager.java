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

    // --- USER MANAGEMENT ---

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
        contentValues.put(DatabaseHelper.COLUMN_SLEEP_SCORE, 0);
        db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

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
                DatabaseHelper.COLUMN_SLEEP_SCORE
        };
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

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

    // --- ANA: RELATIONAL SESSION MANAGEMENT (NEW) ---

    /**
     * Records a new sleep session in the 'sleep_sessions' table.
     * This fulfills the 30% Demo requirement for Data Persistence and Relational Schema.
     */
    public long insertSleepSession(long userId, int durationMinutes, int qualityRating) {
        // 1. Calculate the score using the weighted formula (70/30)
        int finalScore = calculateScoreLogic(durationMinutes, qualityRating);

        // 2. Prepare values for the NEW sleep_sessions table
        ContentValues values = new ContentValues();
        values.put("user_id", userId); // Link to User Table (Foreign Key)
        values.put("duration_minutes", durationMinutes);
        values.put("sleep_quality", qualityRating);
        values.put("sleep_score", finalScore);
        // Note: timestamp is added automatically by SQLite

        // 3. Insert as a NEW record
        long sessionId = db.insert("sleep_sessions", null, values);

        Log.d("SleepData", "New Session ID " + sessionId + " created for User " + userId);

        // Also update the main user table so the 'latest' score shows on profile
        updateUserLatestScore(userId, durationMinutes, qualityRating, finalScore);

        return sessionId;
    }

    /**
     * Fetches all sessions for a specific user to show history.
     */
    public Cursor fetchUserSessions(long userId) {
        return db.query("sleep_sessions", null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "timestamp DESC");
    }

    /**
     * The 70/30 Weighted Algorithm Logic
     */
    private int calculateScoreLogic(int durationMinutes, int qualityRating) {
        double durationScore = (durationMinutes / 480.0) * 100; // 480 mins = 8 hours
        if (durationScore > 100) durationScore = 100;
        return (int) ((durationScore * 0.7) + (qualityRating * 10 * 0.3));
    }

    private void updateUserLatestScore(long userId, int duration, int quality, int score) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SLEEP_DURATION, duration);
        values.put(DatabaseHelper.COLUMN_SLEEP_QUALITY, quality);
        values.put(DatabaseHelper.COLUMN_SLEEP_SCORE, score);
        db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
    }

    // --- CLEANUP ---

    public void delete(long id) {
        db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=" + id, null);
    }
}