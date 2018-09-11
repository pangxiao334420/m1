package com.goluk.a6.http.responsebean;

import java.util.List;

/**
 * 历史轨迹列表
 */
public class TrackList {

    public int size;
    public List<Track> tracks;

    public static class Track {
        public int index;
        public long addtime;
        public long edittime;
        public String trackId;
        public String imei;
        public int state;
        public TrackPoint startLocation;
        public TrackPoint endLocation;
        public long startTime;
        public long endTime;
        public float mileage;
        public float speed;
        public int emergency;
        public String picture;
        public String addDate;
        // 分组名(日期)
        public String gourpName;

    }

}
