package com.example.smarthealthfitnessmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FitnessDatabase.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ACTIVITY = "activity_records";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "record_date";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_DURATION = "duration";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ACTIVITY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_STEPS + " INTEGER, "
                + COLUMN_DURATION + " INTEGER)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITY);
        onCreate(db);
    }

    public void saveActivity(String date, int steps, int duration) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_DURATION, duration);

        db.insert(TABLE_ACTIVITY, null, values);
        db.close();
    }

    public String getAllActivityRecords() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_ACTIVITY,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        );

        StringBuilder records = new StringBuilder();

        if (cursor.getCount() == 0) {
            records.append("No activity records have been saved yet.");
        } else {
            while (cursor.moveToNext()) {
                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(COLUMN_DATE)
                );

                int steps = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_STEPS)
                );

                int duration = cursor.getInt(
                        cursor.getColumnIndexOrThrow(COLUMN_DURATION)
                );

                records.append("Date: ").append(date).append("\n");
                records.append("Steps: ").append(steps).append("\n");
                records.append("Activity time: ")
                        .append(duration)
                        .append(" minutes\n\n");
            }
        }

        cursor.close();
        db.close();

        return records.toString();
    }
}