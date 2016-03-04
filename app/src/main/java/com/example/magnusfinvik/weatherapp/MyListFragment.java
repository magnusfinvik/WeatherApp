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
import android.widget.Toast;

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
    private ArrayList<WeatherData> weatherDataList = new ArrayList<>();
    private GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    private ArrayList<DataPoint> dataPoints;
    private int numberOfPoints = 100;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    private void generateGraphView() {
/**        graphView = (GraphView)this.getActivity().findViewById(R.id.graph);
        generateDataFromDB();
        if(!dataPoints.isEmpty()) {
            series = new LineGraphSeries<DataPoint>();
            series.setDrawDataPoints(true);
            graphView.addSeries(series);
            graphView.setHorizontalScrollBarEnabled(true);
        }else{
            Toast toast2 = Toast.makeText(getContext(), "show Data", Toast.LENGTH_SHORT);
            toast2.show();
        }
    }
 */
        GraphView graph = (GraphView)this.getActivity().findViewById(R.id.graph);
    LineGraphSeries<DataPoint> series = generateLineGraphDataFromDB();
        graph.addSeries(series);
    }

    private LineGraphSeries<DataPoint> generateLineGraphDataFromDB() {
            int count = 0;
            ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
            Cursor cursor = dataSource.getAllContacts();
            while(cursor.moveToNext()) {
                double temperature = cursor.getDouble(cursor.getColumnIndex("temperature"));
                double x = count++;
                double y = temperature;
                DataPoint point = new DataPoint(x, y);
                dataPoints.add(point);
            }
        LineGraphSeries<DataPoint> graphSeries = new LineGraphSeries<DataPoint>((DataPoint[]) dataPoints.toArray());
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
        dataSource.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDownloadController:
                Toast toast = Toast.makeText(getContext(), "download", Toast.LENGTH_SHORT);
                toast.show();
                if(!downloadInProgress){
                    downloadInProgress = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            do {
                                downloadOneItem();
                            }while (downloadInProgress == true);
                        }
                    }).start();

                    // TODO: 03.03.2016 kontunuerlig nedlasting til hvist tidspunkt
                }else{
                    downloadInProgress = false;
                }
                break;
            case R.id.btnShowData:
                // TODO: 03.03.2016 vis i graf eller noe annet
                generateGraphView();
                showDataFromDataBase();
                break;
        }
    }


    private void showDataFromDataBase() {
        Cursor cursor = dataSource.getAllContacts();
        if(cursor.moveToFirst()) {
            String test = cursor.getString(cursor.getColumnIndex("station_name"));
            Toast toast2 = Toast.makeText(getContext(), test, Toast.LENGTH_SHORT);
            toast2.show();
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
