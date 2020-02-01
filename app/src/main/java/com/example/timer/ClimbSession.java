package com.example.timer;

import java.util.Arrays;

public class ClimbSession {

    private int[] oldDev1Times = new int[50];
    private int[] oldDev2Times = new int[50];
    private int tqp; // Time queue pointer
    private int[] oldDev1RealTimes = new int[50];
    private int[] oldDev2RealTimes = new int[50];
    private int rtqp; // Real time queue pointer

    public boolean active;
    private boolean checkedActive;
    public boolean finished;
    public boolean checkedFinish;
    public int startButton;
    public int runStartTime;
    public int runEndTime;
    public int finalTime;

    public int dev1time;
    public int dev2time;
    public int dev1PressTime;
    public int dev2PressTime;
    public int dev1PressRealTime;
    public int dev2PressRealTime;

    private int syncTime;

    public ClimbSession(){
        Arrays.fill(oldDev1Times, -1);
        Arrays.fill(oldDev2Times, -1);
        Arrays.fill(oldDev1RealTimes, -1);
        Arrays.fill(oldDev2RealTimes, -1);
        tqp = 0;
        rtqp = 0;

        dev1PressTime = 0;
        dev2PressTime = 0;

        checkedActive = false;
        finished = false;

        checkedFinish = true;
    }

    public void startRun(int time, int button){
        runStartTime = time;
        startButton = button;
        active = true;
    }

    public void updateTimes(int time1, int pressTime1, int time2, int pressTime2){
        addSyncTime(time1, time2);
        if (pressTime1 > dev1PressTime){
            dev1PressRealTime = (int)(System.nanoTime() / 1000000);
            dev1PressTime = pressTime1;
        }
        if (pressTime2 > dev2PressTime){
            dev2PressRealTime = (int)(System.nanoTime() / 1000000);
            dev2PressTime = pressTime2;
        }
    }

    public void updateLogic(){
        //Activate run if there's no current run, if the last press isn't at time 0, and if the time between current time and pressed time is less than 3 seconds
        if (!active) {
            if (dev1PressTime != 0 && dev1time - dev1PressTime < 3000) {
                startRun(dev1PressTime, 1);
            }
            if (dev2PressTime != 0 && dev2time - dev2PressTime < 3000) {
                startRun(dev2PressTime, 2);
            }
        } else {
            //When the run is started with button1, end the run if the real time of button2 press is after the button1 press
            if (startButton == 1 && dev2PressRealTime > dev1PressRealTime){
                finishRun(dev2PressTime);
            }
            //When the run is started with button2, end the run if the real time of button1 press is after the button2 press
            else if (startButton == 2 && dev1PressRealTime > dev2PressRealTime){
                finishRun(dev1PressTime);
            }
        }
    }

    private void addSyncTime(int time1, int time2){
        oldDev1Times[tqp] = time1;
        oldDev2Times[tqp] = time2;
        tqp = (tqp+1) % 50;
    }

    public void addSyncRealTime(int time1, int time2) {
        oldDev1RealTimes[rtqp] = time1;
        oldDev2RealTimes[rtqp] = time2;
        rtqp = (rtqp+1) % 50;
    }

    public void synchronizeDevices(){
        //TODO: Not sure if I should deal with real-time outliers, they may crash the run anyway

        int totalCount = 0;
        int totalDifference = 0;

        //Accounting for the button times being different
        for (int i = 0; i < 50; i++){
            if(oldDev1Times[i] != -1) {
                totalCount++;
                totalDifference = totalDifference + oldDev1Times[i] - oldDev2Times[i];
            }
        }
        syncTime = totalDifference / totalCount;

        //Accounting for the time difference between accessing the buttons' data
        for (int i = 0; i < 50; i++){
            if(oldDev1RealTimes[i] != -1) {
                totalCount++;
                totalDifference = totalDifference + oldDev1RealTimes[i] - oldDev2RealTimes[i];
            }
        }

        //TODO: Maybe combine? It will work the same, but I don't think I want it in that form
        syncTime += totalDifference / totalCount;
    }

    public void finishRun(int time){
        runEndTime = time;
        synchronizeDevices();
        finalTime = runEndTime - runStartTime + syncTime;
        active = false;
        finished = true;
    }

    public String getResultTime(){
        return timeToString(runEndTime);
    }

    public boolean didFirstStartCheck(){
        boolean temp = checkedActive;
        checkedActive = true;
        return temp;
    }

    public boolean didFirstFinishCheck(){
        boolean temp = checkedFinish;
        checkedFinish = true;
        return temp;
    }

    public String getCurrentApproxTime(){
        if (startButton == 1){
            return timeToString((int)(System.nanoTime() / 1000000) - dev1PressRealTime);
        } else {
            return timeToString((int)(System.nanoTime() / 1000000) - dev2PressRealTime);

        }
    }

    private String timeToString(int time){
        String mins, secs, millis;

        millis = String.format("%03d", (int)(time % 1000));
        time = time / 1000;
        secs = String.format("%02d", (int)(time % 60));
        time = time / 60;
        mins = "" + time;

        return mins + ":" +  secs + "." + millis;
    }
}
