package com.example.smartpillow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;


public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "smartpillow.db";
    static final int DATABASE_VERSION = 1;

static final String TABLE_NAME = "users";
static final String COLUMN_ID = "id";
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

private static final String CREATE_DB_QUERY = "CREATE TABLE " + DATABASE_NAME + "("
        + COLUMN_ID + "INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USERNAME + "TEXT,"
        + COLUMN_PASSWORD + "TEXT," + COLUMN_EMAIL + "TEXT," + COLUMN_PHONE + "TEXT," + COLUMN_GENDER + "TEXT," + COLUMN_AGE + "INTEGER," + COLUMN_HEIGHT + "INTEGER,"
        + COLUMN_WEIGHT + "INTEGER," + COLUMN_SLEEP_DURATION + "INTEGER," + COLUMN_SLEEP_QUALITY + "INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_QUERY);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

    }
}