package com.example.magnusfinvik.weatherapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.net.CookieHandler;
import java.net.CookieManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        int downloadTime;
        String weatherStation;

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_changedownloadtime){

        }
            if(id == R.id.downloadtime_10){
                downloadTime = 10;
            }
            if(id == R.id.downloadtime_20){
                downloadTime = 20;
            }
            if(id == R.id.downloadtime_30){
                downloadTime = 30;
            }
            if(id == R.id.downloadtime_60){
                downloadTime = 60;
            }

        if(id == R.id.action_changeweatherstation){
            // do something here
        }
            if(id == R.id.weatherstation_1){
                weatherStation = "Nullgraderslia";
            }
            if(id == R.id.weatherstation_2){
                weatherStation = "Iskaldtoppen";
            }
            if(id == R.id.weatherstation_3){
                weatherStation = "Stranda";
            }
            if(id == R.id.weatherstation_4){
                weatherStation = "Syden";
            }
            if(id == R.id.weatherstation_5){
                weatherStation = "Nordpolen";
            }
        if(id == R.id.action_emptydatabase){
            // TODO: 05.03.2016 tøm databasen når knappen trykkes
        }
        if (id == R.id.action_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
