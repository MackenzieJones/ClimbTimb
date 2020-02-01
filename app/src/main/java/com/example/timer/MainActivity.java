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
import java.util.concurrent.ExecutionException;


// Climb timer alpha
// Mac device1nes
// Jan 30, 2020
public class MainActivity extends AppCompatActivity {

    private URL url1;
    private URL url2;

    private JSONObject device1 = null;
    private JSONObject device2 = null;

    private boolean deviceFailed = false;

    private ClimbSession currentRun;

    private ArrayList<String> timeList = new ArrayList<>();
    private String fakeList = "";

    private TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initButtons();

        timerText = findViewById(R.id.timeDisplayText);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void stopTimer(){
        currentRun = null;
        queryHandler.removeCallbacks(deviceQuery);
    }

    private void startTimer(){
        currentRun = new ClimbSession();
        queryHandler.post(deviceQuery);
    }

    //TODO: Fix deviceQuery
    //Right now deviceQuery runs as a runnable, but can't be stopped because the handler removes
    //callbacks while the run is waiting for the async task to finish. The runnable finishes the
    //task, then continues and sets up another callback, thus requesting data from the urls no
    //matter what. To fix this, use something else entirely to stop the UI from freezing while it
    //request the devices. I thought a runnable was supposed to create a new thread, but apparently
    //not.
    //whopps

    private Handler queryHandler = new Handler();
    private Runnable deviceQuery = new Runnable() {
        @Override
        public void run() {
            GetDevices gd = new GetDevices();
            System.out.println("live");
            try {
                gd.execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (deviceFailed){
                deviceFailed = true;
                stopTimer();
            }
            timerLogic();
            if (currentRun != null)
                queryHandler.postDelayed(this, 500);
        }
    };

    private void timerLogic() {
        if (device1 != null && device2 != null){
            try {
                int time1 = (int)(device1.get("time"));
                int pressedTime1 = (int)(device1.get("lasttimepressed"));
                int time2 = (int)(device2.get("time"));
                int pressedTime2 = (int)(device2.get("lasttimepressed"));
                currentRun.updateTimes(time1, pressedTime1, time2, pressedTime2);
                currentRun.updateLogic();
                if (currentRun.finished ){
                    if (!currentRun.didFirstFinishCheck()) {
                        timerText.setText(currentRun.getResultTime());
                        stopDisplayTiming();
                    }
                } else{
                    timerText.setText(currentRun.getCurrentApproxTime());
                    if (currentRun.didFirstStartCheck()) {
                        startDisplayTiming();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class GetDevices extends AsyncTask <String, String, String> {

        boolean device1Failed = false;
        boolean device2Failed = false;

        int time1 = 0, time2 = 0;

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            HttpURLConnection ur2Connection = null;

            // Connect to the devices, time how long it took to connect
            try {
                int startTime = (int)System.nanoTime()/1000000;
                urlConnection = (HttpURLConnection) url1.openConnection();
                time1 = (int)(System.nanoTime()/1000000 - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                int startTime = (int)System.nanoTime()/1000000;
                ur2Connection = (HttpURLConnection) url2.openConnection();
                time2 = (int)(System.nanoTime()/1000000 - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Get the json data of the button times
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (in != null) {
                    String jsonBuffer = "";
                    int i;
                    while ((i = in.read()) != -1)
                        jsonBuffer += (char) i;
                    in.close();
                    device1 = new JSONObject(jsonBuffer);
                }
            } catch (Exception e) {
                device1Failed = true;
                //e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            try {
                InputStream in = new BufferedInputStream(ur2Connection.getInputStream());
                if (in != null) {
                    String jsonBuffer = "";
                    int i;
                    while ((i = in.read()) != -1)
                        jsonBuffer += (char) i;
                    in.close();
                    device2 = new JSONObject(jsonBuffer);
                }
            } catch (Exception e) {
                device2Failed = true;
                //e.printStackTrace();
            } finally {
                if (ur2Connection != null)
                    ur2Connection.disconnect();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            // Status change for the device failing or succeeding
            if (device1Failed || device2Failed){
                failedConnection(device1Failed, device2Failed);
            } else {
                currentRun.addSyncRealTime(time1, time2);
                successfulConnection();
            }
        }
    }

    private void startDisplayTiming() {
        displayLoopHandler.post(displayRunner);
    }

    private void stopDisplayTiming() {
        displayLoopHandler.removeCallbacks(displayRunner);
    }

    private Handler displayLoopHandler = new Handler();
    private Runnable displayRunner = new Runnable() {

        @Override
        public void run() {
            System.out.println("spam");
            if (!deviceFailed)
                timerText.setText(currentRun.getCurrentApproxTime());
            else
                stopDisplayTiming();
        }
    };

    private void successfulConnection() {
        TextView status = findViewById(R.id.connectingText1);
        status.setText("Success");
        status = findViewById(R.id.connectingText2);
        status.setText("Success");

        Button b = findViewById(R.id.saveButton);
        b.setEnabled(timerText.getText().equals("0:00.000"));
    }

    private void failedConnection(boolean button1, boolean button2){
        TextView status1 = findViewById(R.id.connectingText1);
        if (button1){
            status1.setText("Failed");
        } else {
            status1.setText("Successful");
        }

        TextView status2 = findViewById(R.id.connectingText2);
        if (button2){
            status2.setText("Failed");
        } else {
            status2.setText("Successful");
        }

        timerText.setText("0:00.000");
        Button b = findViewById(R.id.resetButton);
        b.setEnabled(false);
    }

    private void initButtons() {
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

                currentRun = new ClimbSession();
                startTimer();
            }
        });

        final Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText name = findViewById(R.id.nameField);
                String newElement = name.getText().toString() + ": " + timerText.getText().toString();
                timeList.add(0, newElement);
                if (fakeList.length() > 0){
                    fakeList = "\n" + fakeList;
                }
                fakeList = newElement + fakeList;
                TextView list = findViewById(R.id.entryList);
                list.setText(fakeList);

                Log.d("##########", name.getText().toString() + timerText.getText().toString());
            }
        });

        final Button clearButton = findViewById(R.id.clearTimes);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fakeList = "";
                timeList = new ArrayList<>();

                TextView list = findViewById(R.id.entryList);
                list.setText(fakeList);
            }
        });

        final Button resetTimeButton = findViewById(R.id.resetButton);
        resetTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                timerText.setText("0:00.000");
            }
        });
    }
}
