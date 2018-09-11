
package com.goluk.a6.control;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.util.FileMediaType;
import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.browser.FileListAdapter;
import com.goluk.a6.control.browser.FileScanner;
import com.goluk.a6.control.browser.PhotoActivity;
import com.goluk.a6.control.browser.VideoActivity;
import com.goluk.a6.internation.GolukUtils;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import likly.dollar.$;

import static com.goluk.a6.control.Config.CARDVR_LOCK_PATH;
import static com.goluk.a6.control.Config.CARDVR_LOOP_PATH;
import static com.goluk.a6.control.browser.FileScanner.SORY_BY_TIME_DOWN;
import static com.goluk.a6.control.browser.FileScanner.generateTimeHeaderId;

//本地文件列表
@SuppressLint("NewApi")
public class PhoneFilesView extends IPagerView implements View.OnClickListener {

    private static final String TAG = "CarSvc_PhoneFilesView";

    private static final int REQUEST_CODE_PREVIEW = 1;

    private static final String LOCK_PATH = CARDVR_LOCK_PATH;
    private static final String CAPTURE_PATH = Config.CARDVR_CAPTURE_PATH;
    private static final String LOOP_PATH = CARDVR_LOOP_PATH;
    private static final String EDIT_PATH = Config.CARDVR_EDIT_PATH;
    // 未加锁，所有对mFileList的操作需在UI线程
    private List<FileInfo> mFileList = new ArrayList<FileInfo>();
    private List<FileInfo> mSelectFileList = new ArrayList<FileInfo>();
    private List<FileInfo> mDeleteSuccessPart = new ArrayList<>();

    private FileScanner mFileScanner;
    private FileListAdapter mAdapter;
    private StickyGridHeadersGridView mGridView;

    private String mCurrentPath = LOCK_PATH;

    private boolean mSelectMode = false;
    private boolean isSharing = false;

    private ProgressDialog mProgressDialog;
    private DeleteThread mDeleteThread;
    private RadioGroup mTabRadioGroup;
    private TextView mNoFile;
    private CarAssistMainView parentView;
    private SwipeRefreshLayout mswLayout;
    private View mTitleView;
    private TextView mBtnLeft, mTvTitle, mBtnRightTop;

    private View mCurPhoneFilesView = null;

    public PhoneFilesView(Context context) {
        super(context);
        initView();
    }

    public PhoneFilesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PhoneFilesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void initPhoneFilesView(CarAssistMainView carAssistMainView) {
        parentView = carAssistMainView;
    }

    @Override
    public boolean onCreateOptionsMenu(MenuInflater mi, Menu menu) {
//		Log.d(TAG, "isSharing=" + isSharing);
//		if (isSharing) {
//			if (mCurPhoneFilesView != null){
//				mi.inflate(R.menu.phone_files_share, menu);
//			}else{
//				mi.inflate(R.menu.phone_files, menu);
//			}
//			return true;
//		}
//		if (!mSelectMode) {
//			mi.inflate(R.menu.phone_files, menu);
//		} else {
//			mi.inflate(R.menu.phone_files_mutiple, menu);
//			MenuItem item = menu.findItem(R.id.phone_file_select);
//			if (isSelectAll())
//				item.setIcon(R.drawable.unselect_all);
//			else
//				item.setIcon(R.drawable.select_all);
//		}
        return true;
    }

    public void showMenu() {
        if (mTitleView == null) {
            mTitleView = LayoutInflater.from(getContext()).inflate(R.layout.home_title_record, null);
            mBtnLeft = (TextView) mTitleView.findViewById(R.id.btn_left);
            mBtnRightTop = (TextView) mTitleView.findViewById(R.id.btn_right);
            mTvTitle = (TextView) mTitleView.findViewById(R.id.title);
            mBtnLeft.setOnClickListener(this);
            mBtnRightTop.setOnClickListener(this);
        }
        ((BaseActivity) getContext()).getActionBar().setCustomView(mTitleView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                if (mSelectMode) {
                    return;
                }
                Intent intent = new Intent(getContext(), CarPreviewActivity.class);
                ((Activity) getContext()).startActivityForResult(intent, REQUEST_CODE_PREVIEW);
                break;
            case R.id.btn_right:
                if (mFileList == null || mFileList.size() == 0) {
                    Toast.makeText(getContext(), R.string.no_video, Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleEditMode();
                break;
        }
    }

    public PhoneFilesView.IOperation mIOperationListener = new IOperation() {
        @Override
        public void onClickFirst(View view) {
            if (isSelectAll()) {
                for (FileInfo info : mFileList)
                    info.selected = false;
                mSelectFileList.clear();
                parentView.enableDelete(false);
                parentView.showAllView(true);
                mAdapter.setSelectMode(mSelectMode);
            } else {
                mSelectFileList.clear();
                for (FileInfo info : mFileList) {
                    if (info.name.equals(".."))
                        continue;
                    info.selected = true;
                    mSelectFileList.add(info);
                }
                parentView.showAllView(false);
                mAdapter.setSelectMode(mSelectMode);
                if (mSelectFileList.size() != 0 && mSelectMode) {
                    parentView.enableDelete(true);
                }
            }
        }

        @Override
        public void onClickSecond(View view) {
            String msg = getContext().getString(R.string.delete_video_count, mSelectFileList.size());
            AlertDialog formatDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.hint)
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<FileInfo> list = new ArrayList<>();
                            list.addAll(mSelectFileList);
                            mDeleteThread = new DeleteThread(list);
                            mDeleteThread.start();
                            toggleEditMode();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            formatDialog.show();
        }
    };

    public void toggleEditMode() {
        if (!mSelectMode) {
            mSelectMode = true;
            mTvTitle.setText(R.string.choose_video);
            mSelectFileList.clear();
            for (FileInfo info : mFileList)
                info.selected = false;
            mAdapter.setSelectMode(mSelectMode);
            mBtnRightTop.setText(R.string.cancel);
            parentView.showOperation(true);
            parentView.enableDelete(false);
            mswLayout.setEnabled(false);
        } else {
            mTvTitle.setText(R.string.tab_phone_file);
            mSelectMode = false;
            mSelectFileList.clear();
            for (FileInfo info : mFileList)
                info.selected = false;
            mAdapter.setSelectMode(mSelectMode);
            mBtnRightTop.setText(R.string.multiple);
            parentView.showAllView(true);
            parentView.showOperation(false);
            mswLayout.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.multiple) {
//            return true;
//        }
        return false;
    }

    @Override
    public void onActivate() {
        Log.i(TAG, "onActivate()");
//        mHandler.removeMessages(REFRESH_PAHT);
//        mHandler.sendEmptyMessageDelayed(REFRESH_PAHT, 500);
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
//        boolean show = sp.getBoolean(CarAssistMainView.KEY_PHONE_CLING, true);
//        if (show)
//            ((CarControlActivity) getContext()).initCling(R.id.phone_cling, null, false, 0);
    }

    @Override
    public void onDeactivate() {
        Log.i(TAG, "onDeactivate()");
        mSelectMode = false;
        mSelectFileList.clear();
        for (FileInfo info : mFileList)
            info.selected = false;
        mAdapter.setSelectMode(mSelectMode);
        if (Build.VERSION.SDK_INT >= 14)
            ((CarControlActivity) getContext()).invalidateOptionsMenu();
    }

    @Override
    public void onActivityCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPause() {
        Log.i(TAG, "onActivityPause()");
    }

    @Override
    public void onAcitvityResume() {
        if (!mSelectMode) {
            refresh();
        }
    }

    @Override
    public void onActivityStart() {
        Log.i(TAG, "onActivityStart()");
    }

    @Override
    public void onActivityStop() {
        Log.i(TAG, "onActivityStop()");
//        mSelectMode = false;
//        mSelectFileList.clear();
//        for (FileInfo info : mFileList)
//            info.selected = false;
//        mAdapter.setSelectMode(mSelectMode);
//        if (Build.VERSION.SDK_INT >= 14)
//            ((CarControlActivity) PhoneFilesView.this.getContext()).invalidateOptionsMenu();
    }

    @Override
    public void onActivityDestroy() {
        Log.i(TAG, "onActivityDestroy()");
    }

    @Override
    public boolean onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        if (isSharing) {
            isSharing = false;
        }
        if (mSelectMode) {
            toggleEditMode();
            return true;
        }
        return false;
    }

    @Override
    public void refresh() {
        mFileList.clear();
        mAdapter.notifyDataSetChanged();
        runFileList(CARDVR_LOCK_PATH, FileScanner.RESULT_TYPE_SCANNER);
        runFileList(CAPTURE_PATH, FileScanner.RESULT_TYPE_SCANNER);
        runFileList(CARDVR_LOOP_PATH, FileScanner.RESULT_TYPE_SCANNER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PREVIEW) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, 1500);
        }
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.phone_files_list, this);
        mNoFile = (TextView) findViewById(R.id.phone_no_file);
        mGridView = (StickyGridHeadersGridView) findViewById(R.id.gridview);
        mGridView.setOnItemClickListener(new ItemClickListener());
        mGridView.setOnItemSelectedListener(new ItemSelectedListener());
//        mGridView.setOnItemLongClickListener(new ItemLongClickListener());
        mswLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        mAdapter = new FileListAdapter(getContext(), mFileList, false);
        mGridView.setAdapter(mAdapter);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(getContext().getString(R.string.deleting_files));
        mProgressDialog.setCancelable(false);

        mTabRadioGroup = (RadioGroup) findViewById(R.id.phone_file_fragmen_tab);
        ((RadioButton) findViewById(R.id.phone_file_capture)).setChecked(true);
        mTabRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.phone_file_lock:
                        runFileList(LOCK_PATH, FileScanner.RESULT_TYPE_SCANNER);
                        break;
                    case R.id.phone_file_capture:
                        runFileList(CAPTURE_PATH, FileScanner.RESULT_TYPE_SCANNER);
                        break;
                    case R.id.phone_file_loop:
                        runFileList(LOOP_PATH, FileScanner.RESULT_TYPE_SCANNER);
                        break;
                    case R.id.phone_file_edit:
                        runFileList(EDIT_PATH, FileScanner.RESULT_TYPE_SCANNER);
                        break;
                }
            }
        });
        mswLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mswLayout.setRefreshing(false);
                refresh();
            }
        });
    }

    private boolean runFileList(String filePath) {
        return runFileList(filePath, FileScanner.RESULT_TYPE_SCANNER);
    }

    private boolean runFileList(String filePath, int type) {
        if (TextUtils.isEmpty(filePath))
            return false;

        Log.d(TAG, "runFileList, path=" + filePath);
        if (mFileScanner == null) {
            mFileScanner = new FileScanner() {
                @Override
                public void onResult(int type, String scanPath, ArrayList<FileInfo> fileList) {
                    mCurrentPath = scanPath;
                    Message msg = mHandler.obtainMessage(SCAN_FINISHED, fileList);
                    mHandler.sendMessage(msg);
                }
            };
        }

        if (type == FileScanner.RESULT_TYPE_SCANNER) {
            mFileScanner.startScanner(filePath, false);
        }

        return true;
    }

    private void openFile(FileInfo finfo) {

        File f = new File(finfo.getFullPath());
        Intent intent = null;

        if (finfo.fileType == FileMediaType.IMAGE_TYPE) {
            intent = new Intent(getContext(), PhotoActivity.class);
            intent.putExtra(PhotoActivity.KEY_PHOTO_PATH, mCurrentPath);
            intent.putExtra(PhotoActivity.KEY_PHOTO_CURRENT, finfo.name);
        } else if (finfo.fileType == FileMediaType.VIDEO_TYPE) {
            intent = new Intent(getContext(), VideoActivity.class);
            intent.setDataAndType(Uri.fromFile(f), FileMediaType.getOpenMIMEType(finfo.fileType));
        } else {
            intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(f), FileMediaType.getOpenMIMEType(finfo.fileType));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            getContext().startActivity(intent);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCurPhoneFilesView(View view) {
        mCurPhoneFilesView = view;
        if (view != null) {
            if (Build.VERSION.SDK_INT >= 14)
                ((CarControlActivity) getContext()).invalidateOptionsMenu();
        }
    }

    public void enableShare(boolean enabled) {
        isSharing = enabled;
    }

    private boolean shareFile(List<FileInfo> list) {
        if (list.size() == 0) {
            Toast.makeText(getContext(), R.string.share_file_mimetype_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        ArrayList<Uri> uris = new ArrayList<Uri>();
        FileInfo finfo;
        String mimeType = null;
        boolean sameType = true;
        for (int i = 0; i < list.size(); i++) {
            finfo = list.get(i);
            File file = new File(finfo.getFullPath());
            String type = FileMediaType.getOpenMIMEType(finfo.fileType);
            if (mimeType != null && !mimeType.equals(type)) {
                sameType = false;
            }
            mimeType = type;
            Uri u = Uri.fromFile(file);
            uris.add(u);
        }

        if (!sameType) {
            Toast.makeText(getContext(), R.string.share_file_mimetype_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        boolean multiple = uris.size() > 1;
        if (multiple && mimeType.equals(FileMediaType.getOpenMIMEType(FileMediaType.VIDEO_TYPE))) {
            Toast.makeText(getContext(), R.string.share_file_mimetype_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isSelectAll() {
        return (mFileList.size() != 0 && mFileList.size() == mSelectFileList.size());
    }

    private boolean isUnSelectAll() {
        return mSelectFileList.size() == 0;
    }

    public static final int SCAN_FINISHED = 998;
    public static final int SHOW_PROGRESS_DIALOG = 999;
    public static final int DISMISS_PROGRESS_DIALOG = 1000;
    public static final int REFRESH_PAHT = 1001;
    public static final int DELETE_FILE_SUCCESS = 10002;
    public static final int DELETE_FILE_FAILED = 10003;
    final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_FINISHED:
                    List<FileInfo> list = (List<FileInfo>) msg.obj;
//					switch (mTabRadioGroup.getCheckedRadioButtonId()) {
//					case R.id.phone_file_lock:
//						mNoFile.setText(getContext().getString(R.string.no_lock_files));
//						break;
//					case R.id.phone_file_capture:
//						mNoFile.setText(getContext().getString(R.string.no_capture_files));
//						break;
//					case R.id.phone_file_loop:
//						mNoFile.setText(getContext().getString(R.string.no_loop_files));
//						break;
//					case R.id.phone_file_edit:
//						mNoFile.setText(getContext().getString(R.string.no_edit_files));
//						break;
//					default:
//						mNoFile.setText(getContext().getString(R.string.no_file));
//					}

                    if (mFileList.size() == 0) {
                        mFileList.addAll(list);
                    } else {
                        mFileList.addAll(list);
                        Collections.sort(mFileList, FileScanner.getDirSortComp(SORY_BY_TIME_DOWN));
                    }
                    mSelectMode = false;
                    mSelectFileList.clear();
                    for (FileInfo info : mFileList)
                        info.selected = false;
                    mAdapter.setSelectMode(mSelectMode);
                    if (mFileList.size() > 0) {
                        mNoFile.setVisibility(GONE);
                    } else {
                        mNoFile.setVisibility(VISIBLE);
                    }
                    generateTimeHeaderId(mFileList);
                    mAdapter.notifyDataSetChanged();
                    if (Build.VERSION.SDK_INT >= 14)
                        ((CarControlActivity) getContext()).invalidateOptionsMenu();
                    break;
                case SHOW_PROGRESS_DIALOG:
                    mProgressDialog.show();
                    break;
                case DISMISS_PROGRESS_DIALOG:
                    mProgressDialog.dismiss();
                    break;
                case REFRESH_PAHT:
                    refresh();
                    break;
                case DELETE_FILE_SUCCESS:
                    for (FileInfo fileInfo : mDeleteSuccessPart) {
                        if (mFileList.contains(fileInfo)) {
                            mFileList.remove(fileInfo);
                        }
                    }
                    mSelectFileList.clear();
                    mDeleteSuccessPart.clear();
                    mAdapter.notifyDataSetChanged();

                    if (mFileList.size() <= 0)
                        mNoFile.setVisibility(VISIBLE);

                    $.toast().text(R.string.tip_delete_success).show();
                    break;
                case DELETE_FILE_FAILED:
                    GolukUtils.showToast(null, getContext().getResources().getString(R.string.tip_delete_fail));
                    toggleEditMode();
                    break;
            }
        }
    };

    private class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position < 0 || position >= mFileList.size())
                return;
            FileInfo finfo = mFileList.get(position);
            if (!mSelectMode) {
                if (finfo.name.equals("..")) {
                    runFileList(new File(mCurrentPath).getParent());
                    return;
                }
                String path = finfo.getFullPath();
                File f = new File(path);
                if (f.isDirectory()) {
                    runFileList(path);
                } else {
                    openFile(finfo);
                    mAdapter.setCurrentPosition(position);
                }
            } else {
                if (mSelectFileList.contains(finfo)) {
                    finfo.selected = false;
                    mSelectFileList.remove(finfo);
                    mAdapter.notifyDataSetChanged();
                } else {
                    finfo.selected = true;
                    mSelectFileList.add(finfo);
                    mAdapter.notifyDataSetChanged();
                }
                parentView.enableDelete(mSelectFileList.size() != 0);
                boolean now = isSelectAll();
                parentView.showAllView(!now);
//                boolean unselect = isUnSelectAll();
//                if (unselect) {
//                    parentView.showAllView(true);
//                }
            }

        }
    }

    private class ItemSelectedListener implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class ItemLongClickListener implements OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position < 0 || position >= mFileList.size())
                return true;
            FileInfo finfo = mFileList.get(position);
            if (mSelectMode) {
                mSelectMode = false;
                mSelectFileList.clear();
                for (FileInfo info : mFileList)
                    info.selected = false;
                mAdapter.setSelectMode(mSelectMode);
                if (Build.VERSION.SDK_INT >= 14)
                    ((CarControlActivity) getContext()).invalidateOptionsMenu();
            } else {
                mSelectMode = true;
                mSelectFileList.clear();
                for (FileInfo info : mFileList)
                    info.selected = false;
                mAdapter.setSelectMode(mSelectMode);
                finfo.selected = true;
                mSelectFileList.add(finfo);
                mAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT >= 14)
                    ((CarControlActivity) getContext()).invalidateOptionsMenu();
            }
            return true;
        }
    }

    public class DeleteThread extends Thread {

        private List<FileInfo> mDeleteList;
        private boolean mCancel = false;

        public DeleteThread(List<FileInfo> list) {
            mDeleteList = list;
        }

        @Override
        public void run() {
            mHandler.removeMessages(SHOW_PROGRESS_DIALOG);
            mHandler.sendEmptyMessage(SHOW_PROGRESS_DIALOG);
            boolean success = true;
            for (FileInfo info : mDeleteList) {
                if (mCancel) {
                    success = false;
                    break;
                }

                if (info.name.equals("..")) {
                    continue;
                }

                String path = info.getFullPath();
                File file = new File(path);
                if (file.exists() && file.isDirectory() && !delDir(file)) {
                    success = false;
                    break;
                } else if (file.exists() && file.isFile() && !delFile(file)) {
                    success = false;
                    break;
                }
                if (!mDeleteSuccessPart.contains(info)) mDeleteSuccessPart.add(info);
            }

            mHandler.removeMessages(DISMISS_PROGRESS_DIALOG);
            mHandler.sendEmptyMessage(DISMISS_PROGRESS_DIALOG);

//            final boolean s = success;
//            mHandler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (s) {
//                        Toast.makeText(getContext(), R.string.tip_delete_success, Toast.LENGTH_SHORT).show();
//                        refresh();
//                    } else
//                        Toast.makeText(getContext(), R.string.tip_delete_success, Toast.LENGTH_SHORT).show();
//                }
//
//            });
            if (success) {
                mHandler.sendEmptyMessage(DELETE_FILE_SUCCESS);
            } else {
                mHandler.sendEmptyMessage(DELETE_FILE_FAILED);
            }
        }

        public void setCancel(boolean cancel) {
            mCancel = cancel;
        }

        private boolean delFile(File f) {

            if (mCancel)
                return false;

            boolean ret = true;
            try {
                if (f.exists()) {
                    ret = f.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return ret;
        }

        private boolean delDir(File f) {
            boolean ret = true;
            try {
                if (f.exists()) {
                    File[] files = f.listFiles();
                    for (int i = 0; i < files.length; i++) {

                        if (mCancel)
                            return false;

                        if (files[i].exists() && files[i].isDirectory()) {
                            if (!delDir(files[i])) {
                                return false;
                            }
                        } else {
                            if (files[i].exists() && !files[i].delete()) {
                                return false;
                            }
                        }
                    }

                    if (mCancel)
                        return false;

                    if (f.exists())
                        ret = f.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return ret;
        }

    }

    public interface IOperation {
        void onClickFirst(View view);

        void onClickSecond(View view);
    }
}
