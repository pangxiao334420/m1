package com.goluk.a6.http.responsebean;

import java.util.List;

/**
 * 轨迹详情
 */
public class TrackDetail {

    public String trackId;
    public String imei;
    public int state;
    public TrackPoint startLocation;
    public TrackPoint endLocation;
    public long startTime;
    public long endTime;
    public float mileage;
    public float speed;
    public float direction;
    public int offset;
    public int emergency;
    public String picture;
    public List<TrackPoint> points;

}
