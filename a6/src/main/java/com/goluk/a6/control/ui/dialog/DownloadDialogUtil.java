package com.goluk.a6.control.ui.dialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.goluk.a6.control.R;

/**
 * 下载视频对话框Util
 */
public class DownloadDialogUtil {

    private Context mContext;

    private ProgressDialog mDownloadProgressView;
    private AlertDialog mCancelTaskDialog;

    private OnDownloadCancelListener mListener;

    public DownloadDialogUtil(Context context, OnDownloadCancelListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void showDownloadDialog() {
        mDownloadProgressView = new ProgressDialog(mContext);
        mDownloadProgressView.setMessage(mContext.getString(R.string.str_downloading));
        mDownloadProgressView.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    requestCancelDownload();
                    return true;
                }
                return false;
            }
        });
        mDownloadProgressView.setCancelable(false);
        mDownloadProgressView.show();
    }

    public void onProgressUpdate(int progress) {
        if (mDownloadProgressView != null)
            mDownloadProgressView.setMessage(mContext.getString(R.string.str_downloading) + "  " + progress + "%");
    }

    private void requestCancelDownload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        mCancelTaskDialog = builder.setTitle(R.string.hint)
                .setMessage(R.string.str_hint_quit_download)
                .setPositiveButton(R.string.cling_dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloadProgressView.dismiss();
                        if (mListener != null)
                            mListener.onDownloadCanceled();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
        mCancelTaskDialog.show();
    }

    public void dismiss() {
        if (mDownloadProgressView != null && mDownloadProgressView.isShowing())
            mDownloadProgressView.dismiss();
        if (mCancelTaskDialog != null && mCancelTaskDialog.isShowing())
            mCancelTaskDialog.dismiss();
        mListener = null;
    }

    public interface OnDownloadCancelListener {
        void onDownloadCanceled();
    }

}
