package com.goluk.a6.internation.upload;

/**
 * Created by leege100 on 2016/9/28.
 */

import com.goluk.a6.control.CarControlApplication;
import com.tencent.upload.Const;
import com.tencent.upload.UploadManager;
import com.tencent.upload.task.CommandTask;
import com.tencent.upload.task.UploadTask;

/**
 * @描述 云服务请求接口实现
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-09
 * @版本 1.0
 */
public class QCloudHelper  {
    private static class DefaultConfig {
        private static final String APPID = "10002984";
        private static final String FILE_BUCKET = "pfile";
        private static final String PHOTO_BUCKET = "pphoto";
        private static final String VIDEO_BUCKET = "pvideo";
    }

    /******************* 业务配置 **************************/
    public static final String APPID = DefaultConfig.APPID;
    public static String VIDEO_SIGN = "";
    public static String VIDEO_BUCKET = DefaultConfig.VIDEO_BUCKET;
    public static String PHOTO_BUCKET = DefaultConfig.PHOTO_BUCKET;

    /******************* 通用 **************************/
    private static QCloudHelper instance = null;
    private UploadManager mFileUploadManager;
    private UploadManager mPhotoUploadManager;
    private UploadManager mVideoUploadManager;

    public QCloudHelper() {init();}


    /**
     * 单例支持
     * @return
     */
    public static synchronized QCloudHelper getInstance() {
        if (instance == null) {
            synchronized (QCloudHelper.class) {
                if (instance == null) {
                    instance = new QCloudHelper();
                }
            }
        }
        return instance;
    }

    private void init() {
        mVideoUploadManager = new UploadManager(CarControlApplication.getInstance(), QCloudHelper.APPID, Const.FileType.Video, null);
        mPhotoUploadManager = new UploadManager(CarControlApplication.getInstance(), QCloudHelper.APPID, Const.FileType.Photo, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 云存储服务
    /**
     * 上传
     * @param task
     * @return
     */
    public boolean upload(UploadTask task) {
        if (task == null) {
            return false;
        }

        switch (task.getFileType()) {
            case File:
                return mFileUploadManager.upload(task);

            case Photo:
                return mPhotoUploadManager.upload(task);

            case Video:
                return mVideoUploadManager.upload(task);
        }

        return false;
    }

    public boolean resume(UploadTask task) {
        if (task == null) {
            return false;
        }

        switch (task.getFileType()) {
            case File:
                return mFileUploadManager.resume(task.getTaskId());

            case Photo:
                return mPhotoUploadManager.resume(task.getTaskId());

            case Video:
                return mVideoUploadManager.resume(task.getTaskId());
        }

        return false;
    }

    public boolean pause(UploadTask task) {
        if (task == null) {
            return false;
        }

        switch (task.getFileType()) {
            case File:
                return mFileUploadManager.pause(task.getTaskId());

            case Photo:
                return mPhotoUploadManager.pause(task.getTaskId());

            case Video:
                return mVideoUploadManager.pause(task.getTaskId());
        }

        return false;
    }

    public boolean cancel(UploadTask task) {
        if (task == null) {
            return false;
        }

        switch (task.getFileType()) {
            case File:
                return mFileUploadManager.cancel(task.getTaskId());

            case Photo:
                return mPhotoUploadManager.cancel(task.getTaskId());

            case Video:
                return mVideoUploadManager.cancel(task.getTaskId());
        }

        return false;
    }

    public boolean sendCommand(CommandTask task) {

        if (task == null) {
            return false;
        }

        switch (task.getFileType()) {
            case File:
                return mFileUploadManager.sendCommand(task);

            case Photo:
                return mPhotoUploadManager.sendCommand(task);

            case Video:
                return mVideoUploadManager.sendCommand(task);
        }

        return false;
    }

    public void uploadManagerClose(Const.FileType fileType) {
        switch (fileType) {
            case File:
                mFileUploadManager.close();
                break;

            case Photo:
                mPhotoUploadManager.close();
                break;

            case Video:
                mVideoUploadManager.close();
                break;
        }
    }

    public boolean uploadManagerClear(Const.FileType fileType) {
        switch (fileType) {
            case File:
                return mFileUploadManager.clear();

            case Photo:
                return mPhotoUploadManager.clear();

            case Video:
                return mVideoUploadManager.clear();
        }

        return false;
    }
}