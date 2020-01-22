package com.example.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private URL url1;
    private URL url2;

    private boolean ready = true;
    private JSONObject jo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button attemptButton = findViewById(R.id.attemptButton);
        attemptButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String firstUrl = "http://" + ((EditText)findViewById(R.id.ipText)).getText().toString() + "/";
                System.out.println("############# First: " + firstUrl);
                String secondUrl = "http://" + ((EditText)findViewById(R.id.ipText2)).getText().toString() + "/";
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
                queryHandler.post(deviceQuery);
            }
        });
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
            ready = false;
            while(!ready){}
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
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String jsonBuffer = "";
                int i;
                while ((i = in.read()) != -1)
                    jsonBuffer += (char)i;
                in.close();
                jo = new JSONObject(jsonBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            ready = true;
            return null;
        }
    }
}
