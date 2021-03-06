package com.example.timer;

import android.app.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
// Mac device1nes
// Jan 30, 2020
//^((?!isSBSettingEnabled|identical|pointer).)*$
public class MainActivity extends Activity {

    private URL url1;
    private URL url2;

    private JSONObject device1 = null;
    private JSONObject device2 = null;

    private ClimbSession currentRun;

    private ArrayList<String> timeList = new ArrayList<>();
    private String fakeList = "";

    private TextView timerText;
    private GetDevices gd;
    private DisplayTimer dt;

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

    public void startTimer(){
        if (gd != null)
            stopTimer();
        gd = new GetDevices();
        currentRun = new ClimbSession();
        gd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stopTimer(){
        if (gd != null) {
            gd.cancel(true);
            gd = null;
            currentRun = null;
        } else {
            System.out.println("####################### Tried to delete gd when null");
        }
    }

    public void startDisplayTiming(){
        if (dt != null)
            stopDisplayTiming();
        dt = new DisplayTimer();
        dt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stopDisplayTiming(){
        if (dt != null) {
            dt.cancel(true);
            dt = null;
        } else {
            System.out.println("####################### Tried to delete dt when null");
        }
    }

    public class GetDevices extends AsyncTask <String, String, String> {

        boolean device1Failed = false;
        boolean device2Failed = false;

        int time1, time2;

        @Override
        protected String doInBackground(String... strings) {
            long lastTimeActivated = 0;
            while(currentRun != null) {
                time1 = 0;
                time2 = 0;
                if (System.nanoTime() - lastTimeActivated > 500 * 1000 * 1000) {
                    lastTimeActivated = System.nanoTime();

                    receiveDeviceData();

                    if (currentRun != null) {
                        timerLogic();
                    }

                    publishProgress();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Status change for the device failing or succeeding
            if (device1Failed || device2Failed){
                failedConnection(device1Failed, device2Failed);
                stopTimer();
            } else {
                //currentRun.addSyncRealTime(time1, time2);
                successfulConnection();
            }

            if (currentRun != null) {
                if (currentRun.finished) {
                    if (!currentRun.didFirstFinishCheck()) {
                        stopDisplayTiming();
                        timerText.setText(currentRun.getResultTime());
                    }
                } else if (currentRun.active){
                    if (!currentRun.didFirstStartCheck()) {
                        startDisplayTiming();
                    }
                }
            }
        }

        private void receiveDeviceData(){
            HttpURLConnection urlConnection = null;
            HttpURLConnection ur2Connection = null;

            // Connect to the devices, time how long it took to connect
            try {
                urlConnection = (HttpURLConnection) url1.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                ur2Connection = (HttpURLConnection) url2.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Get the json data of the button times
            try {
                long startTime = System.nanoTime() / 1000000;
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                if (in != null) {
                    String jsonBuffer = "";
                    int i;
                    while ((i = in.read()) != -1)
                        jsonBuffer += (char) i;
                    in.close();
                    device1 = new JSONObject(jsonBuffer);
                }
                time1 = (int) (System.nanoTime() / 1000000 - startTime);

            } catch (Exception e) {
                device1Failed = true;
                //e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            try {
                long startTime = System.nanoTime() / 1000000;
                InputStream in = new BufferedInputStream(ur2Connection.getInputStream());
                if (in != null) {
                    String jsonBuffer = "";
                    int i;
                    while ((i = in.read()) != -1)
                        jsonBuffer += (char) i;
                    in.close();
                    device2 = new JSONObject(jsonBuffer);
                }
                time2 = (int) (System.nanoTime() / 1000000 - startTime);
            } catch (Exception e) {
                device2Failed = true;
                //e.printStackTrace();
            } finally {
                if (ur2Connection != null)
                    ur2Connection.disconnect();
            }
        }

        private void timerLogic() {
            if (device1 != null && device2 != null){
                try {
                    int time1 = (int) (device1.get("time"));
                    int pressedTime1 = (int) (device1.get("lasttimepressed"));
                    int time2 = (int) (device2.get("time"));
                    int pressedTime2 = (int) (device2.get("lasttimepressed"));

                    currentRun.update(time1, pressedTime1, time2, pressedTime2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public class DisplayTimer extends AsyncTask <String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            long lastTime = 0;
            while(currentRun != null) {
                if (System.nanoTime() - lastTime > 1000 * 1000 * 11) {
                    lastTime = System.nanoTime();
                    publishProgress();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (currentRun != null) {
                if (!currentRun.finished) {
                    String time = currentRun.getCurrentApproxTime();
                    timerText.setText(time);
                }
            } else {
                timerText.setText("0:00.000");
            }
        }
    }

    private void successfulConnection() {
        TextView status = findViewById(R.id.connectingText1);
        status.setText("Success");
        status = findViewById(R.id.connectingText2);
        status.setText("Success");
    }

    private void failedConnection(boolean button1, boolean button2){
        System.out.println("Failed Connecting");
        TextView status1 = findViewById(R.id.connectingText1);
        if (button1){
            status1.setText("Failed");
        } else {
            status1.setText("Success");
        }

        TextView status2 = findViewById(R.id.connectingText2);
        if (button2){
            status2.setText("Failed");
        } else {
            status2.setText("Success");
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
                startTimer();
            }
        });

        final Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText name = findViewById(R.id.nameField);
                String newElement = name.getText().toString();
                if (newElement.length() == 0)
                    newElement += ": ";
                newElement += timerText.getText().toString();
                timeList.add(0, newElement);
                if (fakeList.length() > 0){
                    fakeList = "\n" + fakeList;
                }
                fakeList = newElement + fakeList;
                TextView list = findViewById(R.id.entryList);
                list.setText(fakeList);
            }
        });
        saveButton.setEnabled(false);

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
                stopDisplayTiming();
                stopTimer();
                timerText.setText("0:00.000");
                startTimer();
            }
        });

        final TextView timeText = findViewById(R.id.timeDisplayText);
        timeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (currentRun != null && currentRun.finished){
                    saveButton.setEnabled(true);
                } else {
                    saveButton.setEnabled(false);
                }
            }
        });
    }
}
