package com.example.smartpillow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Version 4: Added raw_sensor_data table for Sadeh Algorithm
    static final String DATABASE_NAME = "smartpillow.db";
    static final int DATABASE_VERSION = 4;

    // USERS TABLE
    static final String TABLE_NAME = "users";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_USERNAME = "username";
    static final String COLUMN_PASSWORD = "password";
    static final String COLUMN_EMAIL = "email";
    static final String COLUMN_PHONE = "phone";
    static final String COLUMN_GENDER = "gender";
    static final String COLUMN_AGE = "age";
    static final String COLUMN_HEIGHT = "height";
    static final String COLUMN_WEIGHT = "weight";
    static final String COLUMN_SLEEP_DURATION = "sleep_duration";
    static final String COLUMN_SLEEP_QUALITY = "sleep_quality";
    static final String COLUMN_SLEEP_SCORE = "sleep_score";

    // SESSIONS TABLE
    static final String TABLE_SESSIONS = "sleep_sessions";
    static final String COLUMN_SESSION_ID = "session_id";
    static final String COLUMN_SESSION_USER_ID = "user_id";
    static final String COLUMN_SESSION_DURATION = "duration_minutes";
    static final String COLUMN_SESSION_QUALITY = "sleep_quality";
    static final String COLUMN_SESSION_SCORE = "sleep_score";
    static final String COLUMN_SESSION_TIMESTAMP = "timestamp";

    // NEW: RAW SENSOR DATA TABLE (The Pipeline Staging Area)
    static final String TABLE_RAW_DATA = "raw_sensor_data";
    static final String COLUMN_RAW_ID = "id";
    static final String COLUMN_RAW_SESSION_ID = "session_id";
    static final String COLUMN_RAW_TIMESTAMP = "timestamp";
    static final String COLUMN_RAW_TYPE = "sensor_type"; // 1=Accel, 2=Gyro
    static final String COLUMN_RAW_MAGNITUDE = "magnitude";

    // SQL QUERIES
    private static final String CREATE_USERS_QUERY = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_USERNAME + " TEXT, "
            + COLUMN_PASSWORD + " TEXT, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_PHONE + " TEXT, "
            + COLUMN_GENDER + " TEXT, "
            + COLUMN_AGE + " INTEGER, "
            + COLUMN_HEIGHT + " INTEGER, "
            + COLUMN_WEIGHT + " INTEGER, "
            + COLUMN_SLEEP_DURATION + " INTEGER, "
            + COLUMN_SLEEP_QUALITY + " INTEGER, "
            + COLUMN_SLEEP_SCORE + " INTEGER)";

    private static final String CREATE_SESSIONS_QUERY = "CREATE TABLE " + TABLE_SESSIONS + " ("
            + COLUMN_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_SESSION_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_SESSION_DURATION + " INTEGER, "
            + COLUMN_SESSION_QUALITY + " INTEGER, "
            + COLUMN_SESSION_SCORE + " INTEGER, "
            + COLUMN_SESSION_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (" + COLUMN_SESSION_USER_ID + ") REFERENCES " + TABLE_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE)";

    private static final String CREATE_RAW_DATA_QUERY = "CREATE TABLE " + TABLE_RAW_DATA + " ("
            + COLUMN_RAW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_RAW_SESSION_ID + " INTEGER, "
            + COLUMN_RAW_TIMESTAMP + " LONG, "
            + COLUMN_RAW_TYPE + " INTEGER, "
            + COLUMN_RAW_MAGNITUDE + " REAL, "
            + "FOREIGN KEY(" + COLUMN_RAW_SESSION_ID + ") REFERENCES " + TABLE_SESSIONS + "(" + COLUMN_SESSION_ID + ") ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_QUERY);
        db.execSQL(CREATE_SESSIONS_QUERY);
        db.execSQL(CREATE_RAW_DATA_QUERY);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Must enable foreign keys to make ON DELETE CASCADE work
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " TEXT");
        }
        if (oldVersion < 4) {
            // Adds the raw data table for users who already had the app installed
            db.execSQL(CREATE_RAW_DATA_QUERY);
        }
    }
}