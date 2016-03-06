package com.example.magnusfinvik.weatherapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    WeatherDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dataSource = new WeatherDataSource(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if(id == R.id.action_changedownloadtime){

        }
            if(id == R.id.downloadtime_10){
                 MyListFragment.setDownloadTime(10);
            }
            if(id == R.id.downloadtime_20){
                MyListFragment.setDownloadTime(20);
            }
            if(id == R.id.downloadtime_30){
                MyListFragment.setDownloadTime(30);
            }
            if(id == R.id.downloadtime_60){
                MyListFragment.setDownloadTime(60);
            }

        if(id == R.id.action_changeweatherstation){
        }
            if(id == R.id.weatherstation_1){
                MyListFragment.setStationUrl(0);
                emptyDatabase();

            }
            if(id == R.id.weatherstation_2){
                MyListFragment.setStationUrl(1);
                emptyDatabase();

            }
            if(id == R.id.weatherstation_3){
                MyListFragment.setStationUrl(2);
                emptyDatabase();
            }
            if(id == R.id.weatherstation_4){
                MyListFragment.setStationUrl(3);
                emptyDatabase();
            }
            if(id == R.id.weatherstation_5){
                MyListFragment.setStationUrl(4);
                emptyDatabase();
            }
        if(id == R.id.action_emptydatabase){
            emptyDatabase();
        }
        if (id == R.id.action_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void emptyDatabase() {
        try {
            dataSource.open();
            dataSource.deleteAllContent();
            dataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
