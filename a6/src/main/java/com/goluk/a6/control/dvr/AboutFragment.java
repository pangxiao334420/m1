
package com.goluk.a6.control.dvr;

import com.goluk.a6.control.R;
import com.goluk.a6.control.browser.RemoteFileActivity;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutFragment extends RelativeLayout implements OnClickListener{
	
	private static final String TAG = "TAG_AboutFragment";

	CameraPreviewView mCameraPreviewView;
	ImageView  mRecordingView, mSimReady;
	TextView mNetworkType,mDvrFile, mDvrSetting, mSatellite, mRecordingStatus;
	private Handler mHandler = new Handler();
	
	
	public AboutFragment(Context context) {
		super(context);
		initView();
	}

	public AboutFragment(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public AboutFragment(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}
	
    public static String formatUnit(int i) {
        return String.format("%02d", i);
    }	
	
    public static String secondsToTime(int seconds) {
        if (seconds < 1)
            return "00:00:00";
        if (seconds >= 360000 )
            return "99:59:59";

        StringBuilder sb = new StringBuilder();
        int hour = seconds / 3600;
        int min  = (seconds % 3600) / 60;
        int sec  = (seconds % 3600) % 60;

        return formatUnit(hour)+":"+formatUnit(min)+":"+formatUnit(sec);
    }
	
	public void setRecordingStatus(final boolean started, final int num, final int time) {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				if (started) {
					mRecordingView.setVisibility(View.VISIBLE);
					int resId = R.string.record_front;
					if (num == 1) resId = R.string.record_rear;
					else if (num == 2) resId = R.string.record_both;
					mRecordingStatus.setText(getResources().getString(resId) + secondsToTime(time));
				} else {
					mRecordingView.setVisibility(View.INVISIBLE);
					mRecordingStatus.setText(R.string.record_stop);
				}
			}
		});
	}
	
	public void setNetworkType(final boolean ready, final boolean connected, final int type) {

	}
	
	public void setSatellites(final int num) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mSatellite.setText("" + num);
			}
		});
	}
	
	public void setCameraPreviewView(CameraPreviewView cpv) {
		mCameraPreviewView = cpv;
	}
	
	private void initView() {
		LayoutInflater inflater=(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.about_fragment, this);
        mDvrFile = (TextView)findViewById(R.id.record_file);
        mDvrSetting = (TextView)findViewById(R.id.dvr_setting);
        mDvrFile.setOnClickListener(this);
        mDvrSetting.setOnClickListener(this);
        
        mRecordingStatus = (TextView)findViewById(R.id.recording_status);
        mRecordingStatus.setText(R.string.record_stop);

        mSimReady = (ImageView)findViewById(R.id.mobile_signal_view);
        mRecordingView = (ImageView)findViewById(R.id.recording_view);
        mNetworkType = (TextView)findViewById(R.id.mobile_4g_view);
        mNetworkType.setVisibility(View.INVISIBLE);
        mRecordingView.setVisibility(View.INVISIBLE);
        mSatellite = (TextView)findViewById(R.id.satellite_num);
	}
	
	public void setActionBarMidtitleAndUpIndicator(int midtitleRes, int upRes){
		ActionBar bar = ((Activity) getContext()).getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		if( Build.VERSION.SDK_INT >= 18)
			bar.setHomeAsUpIndicator(upRes);
		bar.setTitle(R.string.tab_preview);
		bar.setDisplayShowTitleEnabled(true);
		bar.setDisplayShowHomeEnabled(false);
		TextView textview = new TextView(getContext());
		textview.setText(midtitleRes);
		textview.setTextColor(Color.WHITE);
		textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.title_size));
		bar.setCustomView(textview, new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		bar.setDisplayShowCustomEnabled(true);
	}	

	@Override
	public void onClick(View v) {
		if (v == mDvrFile) {
			if(RemoteCameraConnectManager.getCurrentServerInfo() == null){
				Toast.makeText(getContext(), R.string.no_connect, Toast.LENGTH_SHORT).show();
				return;
			}
			
			int type = RemoteFileActivity.TYPE_REMOTE_FILE_CAPTURE;
			Intent intent = new Intent(getContext(), RemoteFileActivity.class);
			intent.putExtra(RemoteFileActivity.KEY_TYPE_REMOTE_FILE, type);
			getContext().startActivity(intent);
		} else if (v == mDvrSetting) {
			if(RemoteCameraConnectManager.getCurrentServerInfo() == null){
				Toast.makeText(getContext(), R.string.no_connect, Toast.LENGTH_SHORT).show();
				return;
			}			
			if (mCameraPreviewView != null)
				mCameraPreviewView.showDvrSetting(true);
			((Activity) getContext()).invalidateOptionsMenu();
			setActionBarMidtitleAndUpIndicator(R.string.dvrset, R.drawable.back);			
		}		
	}
}
