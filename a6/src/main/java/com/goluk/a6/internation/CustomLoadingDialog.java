package com.goluk.a6.internation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

import com.goluk.a6.control.R;


public class CustomLoadingDialog {
	ForbidBack forbidInterface;
	private ProgressDialog mDialog;
	private String mMessage;

	public CustomLoadingDialog(Context context) {
		this(context, "");
	}

	public CustomLoadingDialog(Context context, String txt) {
		mDialog = new ProgressDialog(context);
		if (!"".equals(txt) && txt != null) {
			mMessage = txt;
		}else{
			mMessage = context.getResources().getString(R.string.str_loading_text);
		}
		mDialog.setMessage(mMessage);
	}

	public void show(){
		if(mDialog != null){
			if(!mDialog.isShowing()){
				mDialog.show();
			}
		}
	}

	public boolean isShowing() {
		if (null != mDialog) {
			return mDialog.isShowing();
		}
		return false;
	}

	public void close() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
		}
	}

	public void setListener(final ForbidBack forbidInterface) {
		if (null != forbidInterface) {
			this.forbidInterface = forbidInterface;
			mDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
					if (arg2.getAction() == KeyEvent.ACTION_UP) {
						setData(1);
					}
					return false;
				}
			});
			this.mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					forbidInterface.forbidBackKey(1);
				}
			});
		}
	}


	private void setData(int key) {
		if (null != forbidInterface) {
			forbidInterface.forbidBackKey(key);
		}
	}

	public interface ForbidBack {
		public static final int BACK_OK = 1;
		void forbidBackKey(int backKey);
	}


	public void setCancel(boolean isCan) {
		if (null != mDialog) {
			mDialog.setCancelable(isCan);
		}
	}

	public void setCurrentMessage(String title) {
		mDialog.setMessage(title);
    }

}
