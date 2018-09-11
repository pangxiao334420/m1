
package com.goluk.a6.control.util;

import android.os.Handler;
import android.os.HandlerThread;

public class HttpDownloadManager {
	
	private static final String TAG = "CarSvc_HttpDownloadManager";
	
	private HandlerThread mWorkThread;
	private Handler mWorkHandler;
	private static HttpDownloadManager sIns;
	private static DownloadTask downloadTask;

	private HttpDownloadManager(){
		mWorkThread = new HandlerThread("HttpDownload");
		mWorkThread.start();
		mWorkHandler = new Handler(mWorkThread.getLooper());
	}
	
	public static void create(){
		sIns =  new HttpDownloadManager();
	}

	//this method should invoke qiut app
	public static void destory(){

		downloadTask.cancelDownload();
		sIns.mWorkHandler.removeCallbacks(downloadTask);
		sIns.mWorkThread.quit();
	}

	public static void clear() {
		if (downloadTask != null) {
			downloadTask.cancelDownload();
			sIns.mWorkHandler.removeCallbacks(downloadTask);
		}
	}
	
	public static HttpDownloadManager instance(){
		return sIns;
	}
	
	public void requestDownload(DownloadTask task){
//		synchronized (mTaskLists) {
//			mTaskLists.add(task);
//		}
		this.downloadTask = task;
		mWorkHandler.post(task);
	}
	
	public void requestMultiDownload(DownloadTask task){
	}
	
	public void requestDownloadWithNoAddToList(DownloadTask task){
		mWorkHandler.post(task);
	}
	
	public void cancelDownload(DownloadTask task){
		task.cancelDownload();
		mWorkHandler.removeCallbacks(task);
		downloadTask = null;
	}
	
	public DownloadTask getDownloadTask(String filePath){
		if(downloadTask!=null&&downloadTask.getFilePath().equals(filePath))
			return downloadTask;
		return null;
	}
	
	//判断文件夹下面的文件有无正在下载的文件
	public boolean hasDownloadingFile(String filePath){

		return false;
	}
	
	//取消文件夹下面的所有文件下载
	public void cancelDownloadInDir(String filePath){

	}
	
	public interface OnDownloadListener{
		public void onDownloadStart(DownloadTask task);
		public void onDownloadEnd(DownloadTask task, boolean succeed);
		public void onDownloadProgress(DownloadTask task, int progress);
	}
}


