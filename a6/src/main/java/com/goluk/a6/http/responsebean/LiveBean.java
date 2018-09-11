package com.goluk.a6.http.responsebean;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

public class LiveBean {
    public int code;
    public String msg;
    public String data;
    public LiveDataBean beanData;

    public static class LiveDataBean {
        public String clientId;
        public String cmd;
        public String session;
        public int code;
        public String data;
        public LiveDataInfoBean beanData;
    }

    public static class LiveDataInfoBean {
        public int cameraNo;//
        public int duration;//
        public String liveId;//
        public String liveUrl;//
        public String rtmpUrl;
        public int bufferTime;//
        public int preCameraNo;
        public String rateRtmpUrl;
        public String rateSuffix;
        public int state;
    }


    public void genInstanceAgain() {
        if (!TextUtils.isEmpty(data)) {
            beanData = JSON.parseObject(data, LiveDataBean.class);
            if (beanData != null && !"ONLINE".equals(beanData.cmd)
                    && !"CLOSE".equals(beanData.cmd)&& !TextUtils.isEmpty(beanData.data)) {
                beanData.beanData = JSON.parseObject(beanData.data, LiveDataInfoBean.class);
            }
        }
    }
}
