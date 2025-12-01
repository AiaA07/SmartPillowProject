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

public DatabaseManager(Context context) {
}

public DatabaseManager open() throws SQLDataException {
    dbHelper = new DatabaseHelper(context);
    db = dbHelper.getWritableDatabase();
    return this;
    }
    public void close() {
        dbHelper.close();
    }
public void insert (String username, String password, String email, String phone, String gender, int age, int height, int weight, int sleep_duration, int sleep_quality) {
    SQLiteDatabase database = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    ContentValues contentvalues = new ContentValues();
    ContentValues contentValues = null;
    contentValues.put(DatabaseHelper.COLUMN_USERNAME, username);
    contentValues.put(DatabaseHelper.COLUMN_PASSWORD, password);
    database.insert(DatabaseHelper.TABLE_NAME, null, contentValues);

    }
public Cursor fetch () {
    String [] columns = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_PASSWORD, DatabaseHelper.COLUMN_EMAIL, DatabaseHelper.COLUMN_PHONE, DatabaseHelper.COLUMN_GENDER,
            DatabaseHelper.COLUMN_AGE, DatabaseHelper.COLUMN_HEIGHT, DatabaseHelper.COLUMN_WEIGHT, DatabaseHelper.COLUMN_SLEEP_DURATION, DatabaseHelper.COLUMN_SLEEP_QUALITY};
    SQLiteDatabase database = dbHelper.getReadableDatabase();
    Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
    if (cursor != null) {
        cursor.moveToFirst();
    }
    return cursor;
}
public int update (long id, String username, String password, String email, String phone, String gender, int age, int height, int weight, int sleep_duration, int sleep_quality) {
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
int ret = db.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper.COLUMN_ID + "=" + id, null);
return ret;
}
public void delete (long id) {
db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=" + id, null);
}
}

