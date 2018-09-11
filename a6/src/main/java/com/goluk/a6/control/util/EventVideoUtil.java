package com.goluk.a6.control.util;

import android.text.TextUtils;

import com.goluk.a6.http.responsebean.EventVideoList.EventVideo;

/**
 * 事件视频Util
 */
public class EventVideoUtil {

    /**
     * 是否有后摄像头视频
     */
    public static boolean hasBackEventVideo(EventVideo eventVideo) {
        return eventVideo != null && !TextUtils.isEmpty(eventVideo.backVideoName);
    }

    /**
     * 是否所有视频都已经上传到云上
     */
    public static boolean isAllEventVideoOnCloud(EventVideo eventVideo) {
        boolean fore = (TextUtils.isEmpty(eventVideo.foreVideoName)
                || ((!TextUtils.isEmpty(eventVideo.foreVideoName) && !TextUtils.isEmpty(eventVideo.foreVideo))));
        boolean back = (TextUtils.isEmpty(eventVideo.backVideoName)
                || ((!TextUtils.isEmpty(eventVideo.backVideoName) && !TextUtils.isEmpty(eventVideo.backVideo))));

        return eventVideo != null && fore && back;
    }

    /**
     * 对应的摄像头视频是否已经上传到云上
     *
     * @param eventVideo    EventVideo
     * @param isFrontCamera 是否前置
     */
    public static boolean isCloud(EventVideo eventVideo, boolean isFrontCamera) {
        if (eventVideo == null)
            return false;

        if (isFrontCamera)
            return !TextUtils.isEmpty(eventVideo.foreVideoName) && !TextUtils.isEmpty(eventVideo.foreVideo);
        else
            return !TextUtils.isEmpty(eventVideo.backVideoName) && !TextUtils.isEmpty(eventVideo.backVideo);
    }

}
