package com.goluk.a6.http.responsebean;

/**
 * Created by goluk_lium on 2018/4/10.
 */

public class DeviceSleepSettingResult {

    public int code;
    public String msg;
    public DeviceSleepSettingBean data;

    public static class DeviceSleepSettingBean{
        public int autoUploadEventVideo;//自动上传事件视频开关0: 关闭, 1: 打开
        public int dormancyTime;//休眠时间  分钟数
    }

}
