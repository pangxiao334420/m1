package com.goluk.a6.control.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 倒计时Util
 */
public class TimerCountUtil {

    private static final int MAX_COUNT = 60;
    private static final int MSG_TYPE_TIMER = 0;

    private int mCount = 0;
    private Handler mHandler;
    private TimerCallback mCallback;

    public TimerCountUtil(TimerCallback callback) {
        mCallback = callback;

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE_TIMER) {
                    if (mCount <= MAX_COUNT) {
                        if (mCallback != null)
                            mCallback.onTimerCount(MAX_COUNT - mCount);
                        mCount++;
                        sendEmptyMessageDelayed(MSG_TYPE_TIMER, 1000);
                    } else {
                        mCount = 0;
                        if (mCallback != null)
                            mCallback.onTimeCountEnd();
                    }
                }
            }
        };
    }

    public void startTimer() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_TYPE_TIMER);
            mHandler.sendEmptyMessage(0);
            if (mCallback != null)
                mCallback.onTimerStart();
        }
    }

    public void stopTimer() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_TYPE_TIMER);
            mHandler = null;
        }
        if (mCallback != null)
            mCallback = null;
    }

    public interface TimerCallback {
        void onTimerStart();

        void onTimerCount(int count);

        void onTimeCountEnd();
    }

}
