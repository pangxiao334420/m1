package com.goluk.a6.control.util;

import android.util.Log;

import com.goluk.a6.common.util.ZipUtil;
import com.media.tool.GPSData;

import java.util.ArrayList;
import java.util.List;

public class GPSFile {

    private static String TAG = "GPSFile";

    private static final int byteToInt(byte b1, byte b2, byte b3, byte b4, boolean isLittleEndian) {
        int ret;
        if (isLittleEndian) {
            ret = ((b4 << 24) & 0xFF000000) | ((b3 << 16) & 0x00FF0000) | ((b2 << 8) & 0x0000FF00)
                    | ((b1 << 0) & 0x000000FF);
        } else {
            ret = ((b1 << 24) & 0xFF000000) | ((b2 << 16) & 0x00FF0000) | ((b3 << 8) & 0x0000FF00)
                    | ((b4 << 0) & 0x000000FF);
        }
        return ret;
    }

    public static List<GPSData> parseGPSList(byte original[],
                                             boolean zip, boolean isLittleEndian, boolean ignoreRepeat) {
        byte data[];
        if (zip)
            data = ZipUtil.unZip(original);
        else
            data = original;
        if (data == null || data.length == 0 || (data.length % 16) != 0) {
            Log.e(GPSFile.TAG, "wrong GPS data, not enough data");
            if (data != null) {
                Log.e(GPSFile.TAG, "data length = " + data.length);
            }
            return null;
        }
        int gpsDataLength = data.length;
        final List<GPSData> list = new ArrayList<GPSData>();
        GPSData prev = null;
        int ignore = 0;
        for (int i = 0; i < gpsDataLength; i += 16) {
            int ilatitude = GPSFile.byteToInt(data[i + 4], data[i + 5], data[i + 6], data[i + 7], isLittleEndian);
            int ilongitude = GPSFile.byteToInt(data[i + 8], data[i + 9], data[i + 10], data[i + 11], isLittleEndian);
            if (ilatitude == 0xffffe890 || ilongitude == 0xffffe69c) {
                //"ignore bad data"
                ignore++;
                continue;
            }

            GPSData d = new GPSData();
            d.time = GPSFile.byteToInt(data[i + 0], data[i + 1], data[i + 2], data[i + 3], isLittleEndian);
            d.latitude = ilatitude / 1e6;
            d.longitude = ilongitude / 1e6;
            int ext = GPSFile.byteToInt(data[i + 12], data[i + 13], data[i + 14], data[i + 15], isLittleEndian);
            //（16bit:海拔，9bit:角度，7bit：速度）
            d.coordType = ext >>> 30;
            d.altitude = ((ext << 2) & 0xFFFFFFFF) >> 18;
            d.angle = ((ext & 0xFFFF) >>> 7);
            d.speed = (int) ((ext & 0x7F) * 3.6);

            if (prev != null && prev.latitude == d.latitude && prev.longitude == d.longitude) {
                if (!ignoreRepeat)
                    list.add(d);
            } else {
                list.add(d);
            }
            prev = d;
        }
        Log.d(GPSFile.TAG, "GPS list size = " + list.size() + ",ignore=" + ignore);
        return list;
    }


    public static int avgSpeed(List<GPSData> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
//        int total = 0;
//        for (GPSData data :
//                list) {
//            total += data.speed;
//        }
//        return total / list.size();
        int mail = (int) (totalMailslength(list) * 1000);
        int time = totalTime(list);
        if (time == 0) {
            return 0;
        }
        return (int) (mail / time * 3.6);
    }

    public static float avgSpeed(List<GPSData> list, int time) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        int mail = (int) (totalMailslength(list) * 1000);
        if (time == 0)
            return 0;

        return (float) mail / time * 3.6F;
    }


    public static int currentSpeed(GPSData data) {
        return data == null ? 0 : (int) (data.speed * 3.6);
    }


    public static int totalTime(List<GPSData> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        int start = 0;
        for (GPSData data : list) {
            if (data.time != 0) {
                start = data.time;
                break;
            }
        }
        int end = 0;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).time != 0) {
                end = list.get(i).time;
                break;
            }
        }
        return (end - start) + 1;
    }


    public static final double MAX_LENGTH_PER_SECOND = 66.67; // 240KM/h

    public static double totalMailslength(List<GPSData> list) {
        if (list == null || list.size() <= 1) {
            return 0;
        }
        float[] results = new float[1];
        int dis = 0;
        GPSData lastValidData = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (lastValidData.latitude == 0) {
                lastValidData = list.get(i);
                continue;
            }
            if (list.get(i).latitude == 0) {
                continue;
            }
            android.location.Location.distanceBetween(lastValidData.latitude, lastValidData.longitude,
                    list.get(i).latitude, list.get(i).longitude, results);
            if (results[0] > MAX_LENGTH_PER_SECOND)
                continue;
            dis += results[0];
            lastValidData = list.get(i);
        }
        return dis / 1000.0f;
    }


    public static String totalMails(List<GPSData> list) {
        double length = totalMailslength(list);
        if (length == 0)
            return "0.0";
        else return String.format("%.1f", length);
    }
}
