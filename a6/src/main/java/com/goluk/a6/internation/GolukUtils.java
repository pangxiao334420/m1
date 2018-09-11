package com.goluk.a6.internation;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import likly.dollar.$;

public class GolukUtils {
    private static Toast mToast = null;

    public static void showToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(CarControlApplication.getInstance(), text,
                    Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }

        mToast.show();
    }


    public static String getDefaultZone() {
        String current = getLanguageAndCountry();
        if (current.equals("zh_CN") || BuildConfig.BRANCH_CHINA) {
            return "CN +86";
        } else {
            return "US +1";
        }
    }

    public static String getLanguageAndCountry() {
        final String realZone = getLanguage() + "_" + getCountry();
        String[] allZone = CarControlApplication.getInstance()
                .getApplicationContext().getResources()
                .getStringArray(R.array.zone_array);
        if (null == allZone || allZone.length <= 0) {
            return realZone;
        }
        final int length = allZone.length;
        for (int i = 0; i < length; i++) {
            if (realZone.equals(allZone[i])) {
                return allZone[i];
            }
        }
        return realZone;
    }

    /**
     * 获取保存的国家代码信息
     *
     * @return 如: CN +86
     */
    public static String getSavedCountryZone() {
        return $.config().getString("CountryZone");
    }


    public static String getLanguageAndCountryWeb() {
        String language = getLanguage();
        String displayName = Locale.getDefault().toString();
        String area = "";
        if (displayName.contains("Hans") || displayName.equals("zh_CN")) {
            area = "CN";
            language = "zh_Hans";
        } else if (displayName.contains("Hant") || displayName.equals("zh_TW") || displayName.equals("zh_HK")) {
            area = "Hant";
            language = "zh_Hant";
        } else {
            area = getCountry();
        }
        language = language.replace('-', '_');
        final String realZone = language + "-" + area;
        String[] allZone = CarControlApplication.getInstance()
                .getApplicationContext().getResources()
                .getStringArray(R.array.zone_array);
        if (null == allZone || allZone.length <= 0) {
            return realZone;
        }
        final int length = allZone.length;
        for (int i = 0; i < length; i++) {
            if (realZone.equals(allZone[i])) {
                return allZone[i];
            }
        }
        return realZone;
    }


    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private static String getCountry() {
        return Locale.getDefault().getCountry();
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static String sha256Encrypt(String strSrc) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");// 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }


    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    public static String getMobileId() {
        return android.provider.Settings.Secure.getString(CarControlApplication.getInstance().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
    }


    public static String compute32(byte[] content) {
        StringBuffer buf = new StringBuffer("");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try {
                md.update(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            byte b[] = md.digest();
            int i;
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }


    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


    public static boolean isTokenValid(String result) {
        if (!TextUtils.isEmpty(result) &&
                (String.valueOf(Config.SERVER_TOKEN_DEVICE_INVALID).equals(result) ||
                        String.valueOf(Config.SERVER_TOKEN_INVALID).equals(result) ||
                        String.valueOf(Config.SERVER_TOKEN_EXPIRED).equals(result))) {
            return false;
        }
        return true;
    }

    public static String getAppVersion(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static boolean todayChecked() {
        long his = SharedPrefUtil.getDay();
        if (his == 0) {
            return false;
        }
        Date date = Calendar.getInstance().getTime();
        long today = date.getTime() / (24 * 60 * 60 * 1000);
        return today >= SharedPrefUtil.getDay();
    }

    public static String encodeHmacSHA256(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        return byte2hex(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }

    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }


    private static long lastClickTime = 0;
    public static final int MIN_CLICK_DELAY_TIME = 1000;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (Math.abs(time - lastClickTime) < MIN_CLICK_DELAY_TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static String getMobSdkStringFromCode(Context context, int code) {
        switch (code) {
            case 460:
                return context.getString(R.string.smssdk_error_detail_460);
            case 461:
                return context.getString(R.string.smssdk_error_detail_461);
            case 462:
                return context.getString(R.string.smssdk_error_detail_462);
            case 463:
                return context.getString(R.string.smssdk_error_detail_463);
            case 464:
                return context.getString(R.string.smssdk_error_detail_464);
            case 465:
                return context.getString(R.string.smssdk_error_detail_465);
            case 466:
                return context.getString(R.string.smssdk_error_detail_466);
            case 467:
                return context.getString(R.string.smssdk_error_detail_467);
            case 468:
                return context.getString(R.string.smssdk_error_detail_468);
            case 469:
                return context.getString(R.string.smssdk_error_detail_469);
            case 470:
                return context.getString(R.string.smssdk_error_detail_470);
            case 471:
                return context.getString(R.string.smssdk_error_detail_471);
            case 472:
                return context.getString(R.string.smssdk_error_detail_472);
            case 473:
                return context.getString(R.string.smssdk_error_detail_473);
            case 474:
                return context.getString(R.string.smssdk_error_detail_474);
            case 475:
                return context.getString(R.string.smssdk_error_detail_475);
            case 476:
                return context.getString(R.string.smssdk_error_detail_476);
            case 477:
                return context.getString(R.string.smssdk_error_detail_477);
            case 478:
                return context.getString(R.string.smssdk_error_detail_478);
            case 600:
                return context.getString(R.string.smssdk_error_detail_600);
            case 601:
                return context.getString(R.string.smssdk_error_detail_601);
            case 602:
                return context.getString(R.string.smssdk_error_detail_602);
            case 603:
                return context.getString(R.string.smssdk_error_detail_603);
            case 604:
                return context.getString(R.string.smssdk_error_detail_604);
            case 605:
                return context.getString(R.string.smssdk_error_detail_605);
            default:
                return context.getString(R.string.user_getidentify_fail);
        }
    }

    public static String getLocaleStringResource(Locale requestedLocale, int resourceId, Context context) {
        String result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // use latest api
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(requestedLocale);
            result = context.createConfigurationContext(config).getText(resourceId).toString();
        } else { // support older android versions
            Resources resources = context.getResources();
            Configuration conf = resources.getConfiguration();
            Locale savedLocale = conf.locale;
            conf.locale = requestedLocale;
            resources.updateConfiguration(conf, null);

            // retrieve resources from desired locale
            result = resources.getString(resourceId);

            // restore original locale
            conf.locale = savedLocale;
            resources.updateConfiguration(conf, null);
        }

        return result;
    }


    @NonNull
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getStringByLocal(Context context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private static final String FORMAT_F_UNIT = "%1$-1.2f%2$s";
    private static final String FORMAT_F = "%1$-1.2f";
    private static final String[] UNITS = new String[]{
            "B","KB","MB","GB","TB","PB","**"
    };
    private static final int LAST_IDX = UNITS.length-1;
    public static String converter(int unit, float size) {
        int unitIdx = unit;
        while (size > 1024) {
            unitIdx++;
            size /= 1024;
        }
        int idx = unitIdx < LAST_IDX ? unitIdx : LAST_IDX;
        return String.format(FORMAT_F_UNIT, size, UNITS[idx]);
    }

    public static String converterWithoutUnit(int unit, float size) {
        int unitIdx = unit;
        while (size > 1024) {
            unitIdx++;
            size /= 1024;
        }
        int idx = unitIdx < LAST_IDX ? unitIdx : LAST_IDX;
        return String.format(FORMAT_F, size, UNITS[idx]);
    }

    /**
     * 正则表达式：验证手机号
     * 移动号码段:13[4-9],15[0-2,7-9],170,178,18[2-4,7-8]
     * 联通号码段:130,131,132,145,155,156,170,171,175,176,185,186
     * 电信号码段:133,149,153,170,173,177,180,181,189
     *
     * 包含虚拟号段: 170[1700/1701/1702(电信)、1703/1705/1706(移动)、1704/1707/1708/1709(联通)]、171（联通）
     */
    public static final String REGEX_MOBILE = "^((13[0-9])|(14[5|7|9])|(15[^4,\\D])|(17[0,1,6,7,8])|(18[0-9]))\\d{8}$";
    /**
     * 校验手机号
     *
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isMobile(String mobile) {
        return Pattern.matches(REGEX_MOBILE, mobile);
    }


    /**
     * 针对 格式为 V###.###.###的
     * @param stableVersion
     * @param currentVersion
     * @return
     */
    public static boolean compareVersion(String stableVersion, String currentVersion) {
        if (!stableVersion.startsWith("V") || !currentVersion.startsWith("V")) {
            return false;
        }

        String[] stableVersionStringArray = stableVersion.substring(1).split("\\.");
        String[] currentVersionStringArray = currentVersion.substring(1).split("\\.");
        int[] stableVersionArray = new int[stableVersionStringArray.length];
        int[] currentVersionArray = new int[stableVersionStringArray.length];
        for (int i = 0; i < stableVersionArray.length; i++) {
            stableVersionArray[i] = Integer.parseInt(stableVersionStringArray[i]);
            currentVersionArray[i] = Integer.parseInt(currentVersionStringArray[i]);
        }
        boolean result = false;
        for (int i = 0; i < stableVersionArray.length; i++) {
            if (stableVersionArray[i] == currentVersionArray[i])
                continue;
            result = stableVersionArray[i] < currentVersionArray[i];
            if (result)
                return true;
        }
        return result;
    }

}
