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
        db.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    public Cursor fetch() {
        String[] columns = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_PASSWORD, DatabaseHelper.COLUMN_EMAIL, DatabaseHelper.COLUMN_PHONE, DatabaseHelper.COLUMN_GENDER,
                DatabaseHelper.COLUMN_AGE, DatabaseHelper.COLUMN_HEIGHT, DatabaseHelper.COLUMN_WEIGHT, DatabaseHelper.COLUMN_SLEEP_DURATION, DatabaseHelper.COLUMN_SLEEP_QUALITY};
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long id, String username, String password, String email, String phone, String gender, int age, int height, int weight, int sleep_duration, int sleep_quality) {
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
}
