package com.goluk.a6.cpstomp;

import java.io.Serializable;

/**
 * 提取视频进度反馈
 */
public class UploadVideoProgress implements Serializable {
    // 事件Id
    public String eventId;
    // 摄像头编号
    public int cameraNo;
    // 进度
    public int progress;
}
