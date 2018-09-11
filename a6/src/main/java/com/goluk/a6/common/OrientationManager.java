package com.goluk.a6.common;

import android.content.Context;
import android.view.OrientationEventListener;

public class OrientationManager {
	private Context mContext = null;
	private OrientationEventListener mOrientationListener; // 屏幕方向改变监听器

	private IOrientationFn mListener = null;

	public interface IOrientationFn {
		public void landscape();

		public void landscape_left();

		public void portrait();
	}

	public OrientationManager(Context context, IOrientationFn fn) {
		mContext = context;
		mListener = fn;

		startListener();
	}

	/**
	 * 开启监听器
	 */
	private final void startListener() {
		mOrientationListener = new OrientationEventListener(mContext) {
			@Override
			public void onOrientationChanged(int rotation) {
				// 设置竖屏
				if (rotation <= 30 || rotation > 330) {
					if (null != mListener) {
						mListener.portrait();
					}
				} else if ((rotation > 60 && rotation < 120)) {
					if (null != mListener) {
						mListener.landscape_left();
					}
				} else if (rotation > 150 && rotation < 210) {
					if (null != mListener) {
						mListener.portrait();
					}
				} else if (((rotation >= 240) && (rotation <= 300))) {
					// 设置横屏
					if (null != mListener) {
						mListener.landscape();
					}
				}
			}
		};
		mOrientationListener.enable();
	}

	public void clearListener() {
		if (null != mOrientationListener) {
			mOrientationListener.disable();
		}
	}
}
