package com.example.magnusfinvik.weatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by adrja on 03.03.2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "Weather_data.db";
    private static final int DATABASE_VERSION = 1;

    private static final String WEATHER_DATA_TABLE = "WeatherDatas";
    private static final String WEATHER_DATA_ID = "id";
    private static final String WEATHER_DATA_STATION_NAME = "station_name";
    private static final String WEATHER_DATA_STATION_POSITION = "station_position";
    private static final String WEATHER_DATA_TIMESTAMP = "timestamp";
    private static final String WEATHER_DATA_TEMPERATURE = "temperature";
    private static final String WEATHER_DATA_PRESSURE = "pressure";
    private static final String WEATHER_DATA_HUMIDITY = "humidity";


    private static final String WEATHER_DATA_TABLE_CREATE = "create table " + WEATHER_DATA_TABLE + "("
            + WEATHER_DATA_ID + " INTEGER PRIMARY KEY, " + WEATHER_DATA_STATION_NAME + " TEXT, "
            + WEATHER_DATA_STATION_POSITION + " TEXT, " + WEATHER_DATA_TIMESTAMP + " TEXT, "
            + WEATHER_DATA_TEMPERATURE + " TEXT, " + WEATHER_DATA_PRESSURE + " TEXT, "
            + WEATHER_DATA_HUMIDITY + " TEXT)";

    public MySQLiteHelper(Context context){ super(context, DATABASE_NAME, null, DATABASE_VERSION);}

    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WEATHER_DATA_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + WEATHER_DATA_TABLE);
        onCreate(db);
    }
}
