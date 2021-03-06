package com.example.magnusfinvik.weatherapp;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

/**
 * Created by adrja on 03.03.2016.
 */
public class WeatherDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private MyListFragment parent;

    public String[] dataColoumns = {
            dbHelper.WEATHER_DATA_STATION_NAME,
            dbHelper.WEATHER_DATA_STATION_POSITION,
            dbHelper.WEATHER_DATA_TIMESTAMP,
            dbHelper.WEATHER_DATA_TEMPERATURE,
            dbHelper.WEATHER_DATA_PRESSURE,
            dbHelper.WEATHER_DATA_HUMIDITY
    };

    public WeatherDataSource(MyListFragment parentFragment) {
        parent = parentFragment;
        dbHelper = new MySQLiteHelper(parent.getActivity());
    }

    public WeatherDataSource(Activity activity){
        dbHelper = new MySQLiteHelper(activity);
    }

    public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public boolean CreateWeatherData(String stationName, String stationPosition, String timestamp, double temperature,
                                         double pressure, double humidity){
        ContentValues values = new ContentValues();
        values.put(dbHelper.WEATHER_DATA_STATION_NAME, stationName);
        values.put(dbHelper.WEATHER_DATA_STATION_POSITION, stationPosition);
        values.put(dbHelper.WEATHER_DATA_TIMESTAMP, timestamp);
        values.put(dbHelper.WEATHER_DATA_TEMPERATURE, temperature);
        values.put(dbHelper.WEATHER_DATA_PRESSURE, pressure);
        values.put(dbHelper.WEATHER_DATA_HUMIDITY, humidity);

        long insertId = database.insert(dbHelper.WEATHER_DATA_TABLE, null, values);
        if(insertId >= 0){
            return true;
        }else {
            return false;
        }
    }

    public void deleteAllContent(){
        database.execSQL("delete from " + dbHelper.WEATHER_DATA_TABLE);
    }

    public void deleteWeatherData(WeatherData weatherData){
        long id = weatherData.getId();
        database.delete(dbHelper.WEATHER_DATA_TABLE, dbHelper.WEATHER_DATA_ID + "=" + id, null);
    }

    public Cursor getAllContacts (){
        Cursor cursor = database.query(dbHelper.WEATHER_DATA_TABLE, dataColoumns, null, null, null, null, null);
        return cursor;
    }

}
