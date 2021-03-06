package com.example.magnusfinvik.weatherapp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class MyListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private WeatherDataSource dataSource = null;
    private boolean downloadInProgress = false;
    static String station_name = null;
    private static String urlStringStatic = "http://kark.hin.no/~wfa/fag/android/2016/weather/vdata.php?id=1";
    private static String urlWithoutStation = "http://kark.hin.no/~wfa/fag/android/2016/weather/vdata.php?id=";
    private static int downloadTime = 10000;
    private LineGraphSeries<DataPoint> series;

    public static void setStationUrl(int station) {
        urlStringStatic = urlWithoutStation;
        urlStringStatic += station;
        switch (station){
            case 0:
                setStationName("Nullgraderslia");
                break;
            case 1:
                setStationName("Iskaldtoppen");
                break;
            case 2:
                setStationName("Stranda");
                break;
            case 3:
                setStationName("Syden");
                break;
            case 4:
                setStationName("Nordpolen");
                break;
            default:
                setStationName(null);
        }

    }

    public static void setStationName(String stationName) {
        station_name = stationName;
    }

    public static void setDownloadTime(int numberOfSeconds) {
        downloadTime = numberOfSeconds*1000;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        if(dataSource != null){
            if(dataSource.getAllContacts() != null) {
                generateGraphView();
            }
        }
    }

    private void generateGraphView() {
        RadioButton radioButton;
        GraphView graph = (GraphView)this.getActivity().findViewById(R.id.graph);
        graph.removeAllSeries();
        radioButton = (RadioButton)getActivity().findViewById(R.id.radiobutton_temperature);
        if(radioButton.isChecked()) {
            series = generateLineGraphDataFromDB("temperature");
            graph.addSeries(series);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
        }
        radioButton = (RadioButton)getActivity().findViewById(R.id.radiobutton_humidity);
        if(radioButton.isChecked()){
            series = generateLineGraphDataFromDB("humidity");
            graph.addSeries(series);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
        }
        radioButton = (RadioButton)getActivity().findViewById(R.id.radiobutton_pressure);
        if(radioButton.isChecked()){
            series = generateLineGraphDataFromDB("pressure");
            graph.addSeries(series);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
        }
        graph.setTitle(station_name);
    }

    private LineGraphSeries<DataPoint> generateLineGraphDataFromDB(String dataType) {
        int count = 0;
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        Cursor cursor = dataSource.getAllContacts();

        while(cursor.moveToNext()) {
            if(station_name == null){
                station_name = cursor.getString(cursor.getColumnIndex("station_name"));
            }
            String test = cursor.getString(cursor.getColumnIndex("station_position"));
            Log.d("test", test);
            double temperature = cursor.getDouble(cursor.getColumnIndex(dataType));
            double x = count++;
            double y = temperature;
            DataPoint point = new DataPoint(x, y);
            dataPoints.add(point);
        }
        count = 0;
        DataPoint[] points = new DataPoint[dataPoints.size()];
        for (DataPoint point: dataPoints) {
            points[count] = point;
            count++;
        }
        LineGraphSeries<DataPoint> graphSeries = new LineGraphSeries<>(points);
        return graphSeries;
    }

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
        downloadInProgress = false;
        dataSource.close();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDownloadController:
                if(!downloadInProgress){
                    downloadInProgress = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long timeStart = System.currentTimeMillis();
                            long timeElapsed;
                            do {
                                downloadOneItem();
                                long timeEnd = System.currentTimeMillis();
                                timeElapsed = timeEnd - timeStart;
                            }while (downloadInProgress == true && timeElapsed < downloadTime);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Switch downloadSwitch = (Switch) getActivity().findViewById(R.id.btnDownloadController);
                                    if(downloadSwitch.isActivated())
                                    downloadSwitch.toggle();
                                }
                            });
                        }
                    }).start();

                }else{
                    downloadInProgress = false;
                }
                break;
            case R.id.btnShowData:
                generateGraphView();
                break;
        }
    }

    private void downloadOneItem() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection;
                try {
                    URL url = new URL(urlStringStatic);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    int responseCode = httpURLConnection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        String serverResponse = readServerResponse(httpURLConnection.getInputStream());
                        addToWeatherDataBase(serverResponse);

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
            thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addToWeatherDataBase(String serverResponse) {
        Gson gson = new Gson();
        WeatherData weatherData = gson.fromJson(serverResponse, WeatherData.class);
        boolean dataAddaedToDB = dataSource.CreateWeatherData(weatherData.getStation_name(), weatherData.getStation_position(),
                weatherData.getTimestamp(), weatherData.getTemperature(), weatherData.getPressure(), weatherData.getHumidity());
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
