package com.goluk.a6.control.dvr;

import com.goluk.a6.common.map.MapTrackView;
import com.media.tool.GPSData;
import com.goluk.a6.control.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class QuickTrackFragment extends RelativeLayout{

	private static final String TAG = "CarSvc_QuickTrackFragment";

	private MapTrackView mMapTrackView;
	private Handler mHandler = new Handler();

	public QuickTrackFragment(Context context) {
		super(context);
		initView();
	}

	public QuickTrackFragment(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public QuickTrackFragment(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public int setVideoLocation(GPSData data){

//		mMapTrackView.drawTrackCar(data);

		return 0;
	}

	public void onCreate(Bundle savedInstanceState) {
//		mMapTrackView.onCreate(savedInstanceState);
	}

	public void onPause(){
//		mHandler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				mMapTrackView.onPause();
//				if(getVisibility() == View.VISIBLE){
//					mMapTrackView.setLocationEnabled(false);
//				}
//			}
//		}, 500);


	}

	public void onResume(final boolean activity){
//		mHandler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				mMapTrackView.onResume();
//				if(getVisibility() == View.VISIBLE && activity){
//					mMapTrackView.setLocationEnabled(true);
//				}
//			}
//		}, 500);

	}

	public void onDestroy(){
//		mHandler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				mMapTrackView.onDestroy();
//			}
//		}, 500);
	}

	@Override
	public void setVisibility(int visibility) {
		if(getVisibility() == visibility)
			return;
		super.setVisibility(visibility);
//		if(visibility == View.VISIBLE){
//			mMapTrackView.setLocationEnabled(true);
//		}else{
//			mMapTrackView.setLocationEnabled(false);
//		}
	}

	private void initView(){
		LayoutInflater inflater=(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.quick_track_fragment, this);

//		mMapTrackView = MapTrackView.create(getContext());
//		addView(mMapTrackView);
	}
}
