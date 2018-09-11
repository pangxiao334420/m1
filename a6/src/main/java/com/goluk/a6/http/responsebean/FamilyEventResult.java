package com.goluk.a6.http.responsebean;

import android.content.Context;

import com.goluk.a6.control.R;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class FamilyEventResult {
    public int code;
    public String msg;
    public FamilyEventBean data;

    public static class FamilyEventBean {
        public List<FamilyEventDetailBean> events;
        public int size;
        public long index;
    }

    public static class FamilyEventDetailBean {
        public int type;
        public long index;
        public FamilyEventDetailInfoBean event;
        public FamilyEventTrackBean track;
        public FamilyUserBean user;


    }

    public static class FamilyEventDetailInfoBean implements Serializable {
        public int type;
        public String eventId;
        public double lon;
        public double lat;
        public long time;
        public String imei;
        public String userName;
        public String deviceName;
        public String userPic;
        public String mail;
        public String speed;
        public String trackTime;
        public String forePicture;
        public long index;
        public String backPicture;
        public int getEventStringRes() {
            switch (type) {
                case 100:
                    return R.string.event_100;
                case 101:
                    return R.string.event101;
                case 102:
                    return R.string.event102;
                case 103:
                    return R.string.event103;
                case 104:
                    return R.string.event104;
                case 105:
                    return R.string.urgent_hint;
                case 200:
                    return R.string.event_200;
                case 201:
                    return R.string.event_201;
                case 202:
                    return R.string.event_202;
                case 203:
                    return R.string.event_203;
                case 204:
                    return R.string.event_204;
                case 300:
                    return R.string.event_300;
                case 301:
                    return R.string.event_301;
                case 302:
                    return R.string.event_302;
                case 400:
                    return R.string.event_400;
                default:
                    return 0;

            }
        }
    }


    public static class FamilyEventTrackBean {
        public String trackId;
        public String imei;
        public String picture;
        public Date starttime;
        public Date addDate;
        public Date endtime;
        public int state;
        public FamilyLocationBean startLocation;
        public FamilyLocationBean endLocation;
        public double speed;
        public double mileage;
        public int emergency;
        public long index;
        public int type;


    }

    public static class FamilyLocationBean {
        public double lon;
        public double lat;
        public double type;
    }


    public static class FamilyUserBean {
        public String uid;
        public String name;
        public String avatar;
    }
}
