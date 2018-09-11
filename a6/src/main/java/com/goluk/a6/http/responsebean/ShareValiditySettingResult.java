package com.goluk.a6.http.responsebean;

import java.util.List;

/**
 * Created by goluk_lium on 2018/4/10.
 */

public class ShareValiditySettingResult {
    public int code;
    public String msg;
    public ShareValiditySettingBean data;
    public static class ShareValiditySettingBean{
        public int emergencyAlert;//紧急事件提醒0: 关闭, 1: 打开
        public int exceptionAlert;//异常事件提醒0: 关闭, 1: 打开
        public int snapAlert;//抓拍事件提醒0: 关闭, 1: 打开
        public int sharedLinkTime;//分享链接有效时间  分钟数
    }

}
