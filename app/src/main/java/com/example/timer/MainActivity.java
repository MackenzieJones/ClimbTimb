package com.example.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


// Climb timer alpha
// Mac Jones
// Jan 30, 2020
public class MainActivity extends AppCompatActivity {

    private URL url1;
    private URL url2;

    private JSONObject jo = null;
    private boolean getDataFromButtons = false;

    private ArrayList<String> timeList = new ArrayList<>();
    private String fakeList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button attemptButton = findViewById(R.id.attemptButton);
        attemptButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String firstUrl = "http://" + ((EditText)findViewById(R.id.ipField)).getText().toString() + "/";
                System.out.println("############# First: " + firstUrl);
                String secondUrl = "http://" + ((EditText)findViewById(R.id.ipField2)).getText().toString() + "/";
                try {
                    url1 = new URL(firstUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    url2 = new URL(secondUrl);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                getDataFromButtons = true;
                queryHandler.post(deviceQuery);
            }
        });

        final Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText name = findViewById(R.id.nameField);
                TextView time = findViewById(R.id.timeDisplayText);
                String newElement = name.getText().toString() + ": " + time.getText().toString();
                timeList.add(0, newElement);
                if (fakeList.length() > 0){
                    fakeList = "\n" + fakeList;
                }
                fakeList = newElement + fakeList;
                TextView list = findViewById(R.id.entryList);
                list.setText(fakeList);

                Log.d("##########", name.getText().toString() + time.getText().toString());
            }
        });

        final Button clearButton = findViewById(R.id.clearTimes);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fakeList = "";
                timeList = new ArrayList<>();

                TextView list = findViewById(R.id.entryList);
                list.setText(fakeList);
                Log.d("##########", " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH");
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopQuery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (url1 != null && getDataFromButtons)
            queryHandler.post(deviceQuery);
    }

    private void stopQuery(){
        queryHandler.removeCallbacks(deviceQuery);
    }

    private Handler queryHandler = new Handler();
    private Runnable deviceQuery = new Runnable() {
        @Override
        public void run() {
            GetDevices gd = new GetDevices();
            gd.execute();
            while(!gd.isCancelled()){}
            if (jo != null){
                try {
                    System.out.print("Time: " + jo.get("time"));
                    System.out.println(", Last Pressed: " + jo.get("lasttimepressed"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            queryHandler.postDelayed(this, 500);
        }
    };

    public class GetDevices extends AsyncTask <String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url1.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (urlConnection != null) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        String jsonBuffer = "";
                        int i;
                        while ((i = in.read()) != -1)
                            jsonBuffer += (char) i;
                        in.close();
                        jo = new JSONObject(jsonBuffer);
                    }
                } else {
                    stopQuery();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            this.cancel(true);
            return null;
        }
    }
}
