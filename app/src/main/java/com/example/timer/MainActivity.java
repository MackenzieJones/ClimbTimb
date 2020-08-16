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

import org.json.JSONObject;

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

    private JSONObject device1 = null; //TODO: Devices should have their own object
    private JSONObject device2 = null;

    private ClimbSession currentRun; //TODO: current run should have a lot of the private variables here

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
        gd = new GetDevices(currentRun, url1, url2, device1, device2, timerText, this);
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

    public void successfulConnection() {
        TextView status = findViewById(R.id.connectingText1);
        status.setText("Success");
        status = findViewById(R.id.connectingText2);
        status.setText("Success");
    }

    public void failedConnection(boolean button1, boolean button2){
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

    public void initButtons() {
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
