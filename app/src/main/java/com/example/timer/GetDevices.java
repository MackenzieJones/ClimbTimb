package com.example.timer;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetDevices extends AsyncTask<String, String, String> {

    boolean device1Failed = false;
    boolean device2Failed = false;
    int time1, time2;

    MainActivity mainAct; //TODO: putting this here for now before fixing how failedConnection and successfulConnection should work
    ClimbSession currentRun;
    URL url1, url2;
    JSONObject device1, device2;
    TextView timerText;

    GetDevices(ClimbSession currentRun, URL url1, URL url2, JSONObject device1, JSONObject device2, TextView timerText, MainActivity mainAct){
        this.currentRun = currentRun;
        this.url1 = url1;
        this.url2 = url2;
        this.device1 = device1;
        this.device2 = device2;
        this.timerText = timerText;
        this.mainAct = mainAct;
    }

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
            mainAct.failedConnection(device1Failed, device2Failed);
            mainAct.stopTimer();
        } else {
            //currentRun.addSyncRealTime(time1, time2);
            mainAct.successfulConnection();
        }

        if (currentRun != null) {
            if (currentRun.finished) {
                if (!currentRun.didFirstFinishCheck()) {
                    mainAct.stopDisplayTiming();
                    timerText.setText(currentRun.getResultTime());
                }
            } else if (currentRun.active){
                if (!currentRun.didFirstStartCheck()) {
                    mainAct.startDisplayTiming();
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
