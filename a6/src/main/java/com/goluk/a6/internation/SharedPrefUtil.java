package com.goluk.a6.internation;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.goluk.a6.control.CarControlApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 2012-10-09
 *
 * @author caoyingpeng
 */
public class SharedPrefUtil {
    public static final String USER_INFO = "user_info";

    public static String getUserInfo() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getString(USER_INFO, "");
    }

    public static void saveUserInfo(String user) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putString(USER_INFO, user).commit();
    }


    public static final String WELCOME = "welcome";

    public static boolean getWelcome() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getBoolean(WELCOME, false);
    }

    public static void saveWelcome(boolean value) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putBoolean(WELCOME, value).commit();
    }


    public static final String DAY = "day";

    public static long getDay() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getLong(DAY, 0);
    }

    public static void saveDay(long value) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putLong(DAY, value).commit();
    }


    public static final String USER_TOKEN = "user_token";

    public static String getUserToken() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getString(USER_TOKEN, "");
    }

    public static void saveUserToken(String token) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putString(USER_TOKEN, token).commit();
    }


    public static final String USER_PASSWORD = "user_password";

    public static void saveUserPwd(String pwd) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putString(USER_PASSWORD, pwd).commit();
    }

    public static String getUserPwd() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getString(USER_PASSWORD, "");
    }


    public static final String USER_SOS = "user_save_sos";

    public static void saveUserSos(boolean value) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putBoolean(USER_SOS, value).commit();
    }

    public static Boolean getUserSos() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getBoolean(USER_SOS, false);
    }


    public static final String IMEI = "imei";

    public static void saveImei(String imei) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putString(IMEI, imei).commit();
    }

    public static String getImei() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getString(IMEI, "");
    }
    public static final String ICCID = "iccid";

    public static void saveIccid(String iccid) {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        preference.edit().putString(ICCID, iccid).commit();
    }

    public static String getIccid() {
        SharedPreferences preference = CarControlApplication.getInstance().getSharedPreferences("GuideActivity", Activity.MODE_PRIVATE);
        return preference.getString(ICCID, "");
    }

    public static final String KEY_PRESERVER_SERIALNO = "key_preserver_serialno";

    public static void saveDevices(HashMap<String, String> list) {
        SharedPreferences.Editor editor = CarControlApplication.getInstance().getSharedPreferences(KEY_PRESERVER_SERIALNO, Activity.MODE_PRIVATE).edit();
        for (Map.Entry<String, String> entry : list.entrySet())
            editor.putString(entry.getKey(), entry.getValue());
        editor.commit();
    }

    public static HashMap<String, String> getDevices() {
        HashMap<String, String> list = new HashMap<>();
        SharedPreferences prefs = CarControlApplication.getInstance().getSharedPreferences(KEY_PRESERVER_SERIALNO, Activity.MODE_PRIVATE);
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            list.put(entry.getKey(), entry.getValue().toString());
        }
        return list;
    }
}
