package com.example.smartpillow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "smartpillow.db";
    static final int DATABASE_VERSION = 3; // Updated from 2 to 3

    static final String TABLE_NAME = "users";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name"; // Added: User's full name
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

    static final String TABLE_SESSIONS = "sleep_sessions";
    static final String COLUMN_SESSION_ID = "session_id";
    static final String COLUMN_SESSION_USER_ID = "user_id";
    static final String COLUMN_SESSION_DURATION = "duration_minutes";
    static final String COLUMN_SESSION_QUALITY = "sleep_quality";
    static final String COLUMN_SESSION_SCORE = "sleep_score";
    static final String COLUMN_SESSION_TIMESTAMP = "timestamp";

    // Updated CREATE_USERS_QUERY to include name column
    private static final String CREATE_USERS_QUERY = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT, "  // Added name column
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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_QUERY);
        db.execSQL(CREATE_SESSIONS_QUERY);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades
        if (oldVersion < 3) {
            // Add name column to existing users table
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_NAME + " TEXT");
        }
        // If you need to handle other upgrades, add more conditions here
    }
}