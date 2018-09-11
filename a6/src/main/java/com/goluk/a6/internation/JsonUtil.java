package com.goluk.a6.internation;


import com.alibaba.fastjson.JSONObject;
import com.goluk.a6.internation.bean.UserInfo;

public class JsonUtil {
    public static UserInfo parseSingleUserInfoJson(JSONObject rootObj) {

        return null;
    }

    public static boolean getJsonBooleanValue(org.json.JSONObject json_Channel, String key, boolean defaultValue) {
        try {
            if (!json_Channel.has(key)) {
                return defaultValue;
            }
            if (json_Channel.isNull(key)) {
                return defaultValue;
            }
            return json_Channel.getBoolean(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static String getJsonStringValue(String jsonData, String key, String defaultValue) {
        try {
            return getJsonStringValue(new org.json.JSONObject(jsonData), key, defaultValue);
        } catch (Exception e) {

        }
        return defaultValue;
    }

    private static String getJsonStringValue(org.json.JSONObject json_Channel, String key, String defaultValue) {
        try {
            if (!json_Channel.has(key)) {
                return defaultValue;
            }
            if (json_Channel.isNull(key)) {
                return defaultValue;
            }
            return json_Channel.getString(key);
        } catch (Exception e) {

        }
        return defaultValue;
    }

    private static double getJsonDoubleValue(org.json.JSONObject json_Channel, String key, double defaultValue) {
        try {
            if (!json_Channel.has(key)) {
                return defaultValue;
            }
            if (json_Channel.isNull(key)) {
                return defaultValue;
            }
            return json_Channel.getDouble(key);
        } catch (Exception e) {

        }
        return defaultValue;
    }

    public static int getJsonIntValue(String message, String key, int defaultValue) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(message);
            return getJsonIntValue(obj, key, defaultValue);
        } catch (Exception e) {

        }

        return defaultValue;
    }

    public static int getJsonIntValue(org.json.JSONObject json_Channel, String key, int defaultValue) {
        try {
            if (!json_Channel.has(key)) {
                return defaultValue;
            }
            if (json_Channel.isNull(key)) {
                return defaultValue;
            }
            return json_Channel.getInt(key);
        } catch (Exception e) {

        }
        return defaultValue;
    }


    public static String registAndRepwdJson(String phoneNumber, String password, String vCode) {
        try {
            // {PNumber：“13054875692”，Password：“xxx”，VCode：“1234”}
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("PNumber", phoneNumber);
            obj.put("Password", password);
            obj.put("VCode", vCode);
            obj.put("tag", "android");
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
