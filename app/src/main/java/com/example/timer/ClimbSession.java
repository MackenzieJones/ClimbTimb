package com.example.timer;

public class ClimbSession {

    public boolean active;
    private boolean checkedActive;
    public boolean finished;
    public boolean checkedFinish;

    public EasyButton b1;
    public EasyButton b2;
    public EasyButton startButton;
    public EasyButton finishButton;

    private long runStartTime;
    private long runEndTime;
    private long runStartRealTime;

    private long finalRunTime;
    private long syncTime;

    public ClimbSession(){
        active = false;
        checkedActive = false;
        finished = false;
        checkedFinish = false;

        b1 = new EasyButton();
        b2 = new EasyButton();
    }

    public void startRun(EasyButton startButton, EasyButton finishButton){
        runStartRealTime = (System.nanoTime() / 1000000);
        runStartTime = startButton.curPressTime;
        this.startButton = startButton;
        this.finishButton = finishButton;
        active = true;
        System.out.println("Activated");
    }

    public void update(long time1, long pressTime1, long time2, long pressTime2){
        b1.updateTimes(time1, pressTime1);
        b2.updateTimes(time2, pressTime2);

        boolean didB1Press = b1.curPressTime > b1.pressInitTime;
        boolean didB2Press = b2.curPressTime > b2.pressInitTime;

        if (!active) {
            if (didB1Press && didB2Press) {
                b1.updateInitTimes();
                b2.updateInitTimes();
            } else if (didB1Press){
                startRun(b1, b2);
            } else if (didB2Press) {
                startRun(b2, b1);
            }
        } else {
            if (finishButton.curPressRealTime > runStartRealTime){
                finishRun();
            }
        }
    }

    public void synchronizeDevices(){
        long totalCount = 0;
        long totalDifference = 0;

        //Accounting for the button times being different
        for (int i = 0; i < finishButton.oldTimes.length; i++){
            if(finishButton.oldTimes[i] != -1) {
                totalCount++;
                totalDifference = totalDifference + (startButton.oldTimes[i] - finishButton.oldTimes[i]);
            }
        }
        syncTime = totalDifference / totalCount;
        System.out.println("SyncTime: " + syncTime);
    }

    public void finishRun(){
        runEndTime = finishButton.curPressTime;
        synchronizeDevices();
        finalRunTime = runEndTime - runStartTime + syncTime;
        active = false;
        finished = true;
    }

    public String getResultTime(){
        return timeToString(finalRunTime);
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
        System.out.println(System.nanoTime() / 1000000 - runStartRealTime);
        return timeToString(System.nanoTime() / 1000000 - runStartRealTime);
    }

    private String timeToString(long time){
        if (time > 86399000)
            return "0:00.000";
        String mins, secs, millis;

        millis = String.format("%03d", (time % 1000));
        time = time / 1000;
        secs = String.format("%02d", (time % 60));
        time = time / 60;
        mins = "" + time;

        return mins + ":" +  secs + "." + millis;
    }

}
