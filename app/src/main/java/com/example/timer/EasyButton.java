package com.example.timer;

import java.util.Arrays;

public class EasyButton {

    private int syncArraySize;
    public long[] oldTimes;
    private int tqp; // Time queue pointer
    public long[] oldRealTimes;
    private int rtqp; // Real time queue pointer

    public long curTime;
    public long curPressTime;
    public long curPressRealTime;
    public long pressInitTime;

    private boolean initEntry;

    public EasyButton(){
        syncArraySize = 50;
        oldTimes = new long[syncArraySize];
        oldRealTimes = new long[syncArraySize];
        Arrays.fill(oldTimes, -1);
        Arrays.fill(oldRealTimes, -1);
        tqp = 0;
        rtqp = 0;

        initEntry = true;
    }

    public void updateTimes(long time, long pressTime){
        if (initEntry){
            curTime = time;
            pressInitTime = pressTime;
        }
        if (pressTime > curPressTime) {
            curPressRealTime = System.nanoTime() / 1000000;
            curPressTime = pressTime;
        }
        addSyncTime(time);
    }

    public void updateInitTimes() {
        pressInitTime = curPressTime;
    }

    private void addSyncTime(long time) {
        oldTimes[tqp] = time;
        tqp = (tqp+1) % syncArraySize;
    }

    public void addSyncRealTime(long time) {
        oldRealTimes[rtqp] = time;
        rtqp = (rtqp+1) % syncArraySize;
    }
}
