
package com.goluk.a6.control;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CarViewPager extends ViewPager{
	
	private boolean mSlideEnable = true; 
	
	public CarViewPager(Context context) {
		super(context);
	}

	public CarViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setSlideEnable(boolean enable){
		mSlideEnable = enable;
	}

	public boolean isSlideEnable() {
		return mSlideEnable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (mSlideEnable) {
			return super.onTouchEvent(arg0);
		} else {
			return false;
		}

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (mSlideEnable) {
			return super.onInterceptTouchEvent(arg0);
		} else {
			return false;
		}

	}
	
	
}
