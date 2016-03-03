package com.example.magnusfinvik.weatherapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private WeatherDataSource dataSource = null;
    private ListView myListView;
    private boolean downloadInProgress = false;
    private ArrayList<WeatherData> weatherDataList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Button btnDownloadControl = (Button)this.getActivity().findViewById(R.id.btnDownloadController);
        btnDownloadControl.setOnClickListener(this);

        Button btnShowData = (Button)this.getActivity().findViewById(R.id.btnShowData);
        btnShowData.setOnClickListener(this);

        myListView = (ListView)this.getActivity().findViewById(R.id.myListView);
        myListView.setOnItemClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        dataSource = new WeatherDataSource(this);
        try {
            dataSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dataSource.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDownloadController:
                Toast toast = Toast.makeText(getContext(), "download", Toast.LENGTH_SHORT);
                toast.show();
                if(!downloadInProgress){
                    downloadInProgress = false;
                    startDownload();
                }
                break;
            case R.id.btnShowData:
                Toast toast2 = Toast.makeText(getContext(), "show Data", Toast.LENGTH_SHORT);
                toast2.show();
                break;
        }
    }

    private void startDownload() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                String urlString = "http://kark.hin.no/~wfa/fag/android/2016/weather/vdata.php";
                HttpURLConnection httpURLConnection;
                try {
                    URL url = new URL(urlString);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        String serverResponse = readServerResponse(httpURLConnection.getInputStream());
                        addToWeatherDataList(serverResponse);
                    }else {
                        Log.d(this.getClass().toString(), "error in httpconnection");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(run);
        thread.start();
        try{
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void addToWeatherDataList(String serverResponse) {
        Gson gson = new Gson();
        weatherDataList.add(gson.fromJson(serverResponse, WeatherData.class));
    }

    private String readServerResponse(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while((line = reader.readLine()) != null){
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error in reading server response";
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
