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
import android.widget.CheckBox;
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
// TODO: 03.03.2016 lag toolbar og tingene som skal være inne i den
/**
 * A placeholder fragment containing a simple view.
 */
public class MyListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private WeatherDataSource dataSource = null;
    private boolean downloadInProgress = false;
    String station_name = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    private void generateGraphView() {
        RadioButton radionButton;
        GraphView graph = (GraphView)this.getActivity().findViewById(R.id.graph);
        graph.removeAllSeries();
        radionButton = (RadioButton)getActivity().findViewById(R.id.radiobutton_temperature);
        if(radionButton.isChecked()) {
            LineGraphSeries<DataPoint> series = generateLineGraphDataFromDB("temperature");
            graph.addSeries(series);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
        }
        radionButton = (RadioButton)getActivity().findViewById(R.id.radiobutton_humidity);
        if(radionButton.isChecked()){
            LineGraphSeries<DataPoint> series = generateLineGraphDataFromDB("humidity");
            graph.addSeries(series);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
        }
        radionButton = (RadioButton)getActivity().findViewById(R.id.radiobutton_pressure);
        if(radionButton.isChecked()){
            LineGraphSeries<DataPoint> series = generateLineGraphDataFromDB("pressure");
            graph.addSeries(series);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(10);
        }
        graph.setTitle(station_name);
    }

    private LineGraphSeries<DataPoint> generateLineGraphDataFromDB(String dataType) {
        int count = 0;
        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
        Cursor cursor = dataSource.getAllContacts();

        while(cursor.moveToNext()) {
            if(station_name == null){
                station_name = cursor.getString(cursor.getColumnIndex("station_name"));
            }
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
        LineGraphSeries<DataPoint> graphSeries = new LineGraphSeries<DataPoint>(points);
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
        super.onStop();
        dataSource.deleteAllContent();
        dataSource.close();
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
                            }while (downloadInProgress == true && timeElapsed < 5000);
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
                String urlString = "http://kark.hin.no/~wfa/fag/android/2016/weather/vdata.php";
                HttpURLConnection httpURLConnection;
                try {
                    URL url = new URL(urlString);
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
