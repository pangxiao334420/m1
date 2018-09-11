package com.goluk.a6.common.event;

import com.tencent.upload.task.data.FileInfo;

public class UploadEvent {
    public  FileInfo fileInfo;
    public boolean succ;
    public UploadEvent(boolean b) {
        succ = b;
    }

    public UploadEvent(FileInfo fileInfo, boolean b) {
        succ = b;
        this.fileInfo = fileInfo;
    }
}
