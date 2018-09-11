package com.goluk.a6.internation.upload;

import android.util.Log;

import com.goluk.a6.common.event.UploadEvent;
import com.goluk.a6.internation.bean.SignDataBean;
import com.tencent.upload.task.ITask;
import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.impl.PhotoUploadTask;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by leege100 on 2016/12/15.
 */

public class CPUploadPicTask {

    private static final String TAG = "CPUploadPicTask";
    PhotoUploadTask mPicUploadTask;
    SignDataBean mSignDataBean;
    String mPicPath;

    public CPUploadPicTask(SignDataBean signDataBean, String picPath) {
        this.mSignDataBean = signDataBean;
        this.mPicPath = picPath;
    }

    public void cancel() {
        if (mPicUploadTask != null) {
            QCloudHelper.getInstance().cancel(mPicUploadTask);
        }
    }

    /**
     * 创建腾讯云上传封面操作Observable
     *
     * @return
     */
    public void createUploadPicObservable() {

        mPicUploadTask = new PhotoUploadTask(mPicPath, new IUploadTaskListener() {

            @Override
            public void onUploadStateChange(ITask.TaskState state) {
            }

            @Override
            public void onUploadProgress(long totalSize, long sendSize) {
                long p = (long) ((sendSize * 100) / (totalSize * 1.0f));
                Log.i(TAG, "上传进度: " + p + "%");
            }

            @Override
            public void onUploadSucceed(com.tencent.upload.task.data.FileInfo fileInfo) {
                EventBus.getDefault().post(new UploadEvent(fileInfo, true));
            }

            @Override
            public void onUploadFailed(final int errorCode, final String errorMsg) {
                EventBus.getDefault().post(new UploadEvent(false));
            }
        });

        mPicUploadTask.setBucket(mSignDataBean.getEnv() + QCloudHelper.PHOTO_BUCKET);  // 设置 Bucket(可选)
        if (mPicPath.endsWith(".jpg") || mPicPath.endsWith(".jpeg") || mPicPath.endsWith(".JPG") || mPicPath.endsWith(".JPEG")) {
            mPicUploadTask.setFileId(mSignDataBean.getPhotopath() + mSignDataBean.getId() + ".jpg"); // 为图片自定义 FileID(可选)
        } else if (mPicPath.endsWith(".png") || mPicPath.endsWith(".PNG")) {
            mPicUploadTask.setFileId(mSignDataBean.getPhotopath() + mSignDataBean.getId() + ".png"); // 为图片自定义 FileID(可选)
        }

        mPicUploadTask.setAuth(mSignDataBean.getSign());
        if (!QCloudHelper.getInstance().upload(mPicUploadTask)) {
            EventBus.getDefault().post(new UploadEvent(false));
        }
        //开始上传
    }
}
