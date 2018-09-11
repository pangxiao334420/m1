package com.goluk.a6.internation;

import com.goluk.a6.control.CarControlApplication;

import java.util.Timer;
import java.util.TimerTask;

public class TimerManage {

    private CarControlApplication mApp = null;
    private Timer mTimer = null;
    private int count = 0;
    public boolean flag = true;

    public TimerManage(CarControlApplication mApp) {
        super();
        this.mApp = mApp;
    }

    public void timerCount() {
        timerCancel();
        flag = false;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                count++;
                if (count >= 60) {
                    timerCancel();
                }
            }
        }, 0, 1000);
    }

    public void timerCancel() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            flag = true;
            count = 0;
        }
    }

}
