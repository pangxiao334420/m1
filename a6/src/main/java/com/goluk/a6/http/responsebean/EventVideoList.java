package com.goluk.a6.http.responsebean;

import java.io.Serializable;
import java.util.List;

/**
 * 事件视频
 */

public class EventVideoList {

    public int size;
    public List<EventVideo> events;

    public static class EventVideo implements Serializable {

        public int index;
        public long addtime;
        public long edittime;
        public String eventId;
        public String imei;
        public String trackId;
        public int type;
        public long time;
        public String forePicture;
        public String backPicture;
        public String foreVideo;
        public String backVideo;
        public String foreVideoName;
        public String backVideoName;
        public int isCloud;
        public double lon;
        public double lat;
        public String location;
        public String uid;

    }

}
