package com.goluk.a6.control.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goluk.a6.common.util.FileMediaType;
import com.goluk.a6.control.R;
import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import static com.goluk.a6.control.Config.REMOTE_CAPTURE_PATH;
import static com.goluk.a6.control.Config.REMOTE_LOCK_PATH;

public class FileListAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {
    private static final String TAG = "CarSvc.FileAdapter";

    private LayoutInflater mInflater;
    private List<FileInfo> mFileInfos = null;

    public static final int UNSELECT = 0;
    public static final int COLOR_UNSELECT = 0xFFFFFFFF;
    public static final int COLOR_SELECTED = 0xFFC71585;

    private Context mContext;
    private boolean mSelectMode = false;
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd EEEE");
    //是否是远程文件
    private boolean mRemote;
    private int mGridItemHeight;

    private int mCurrentPosition = -1;

    public FileListAdapter(Context context, List<FileInfo> fi, boolean remote) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        mFileInfos = fi;

        mRemote = remote;
        int screenWidth = ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth();
        mGridItemHeight = screenWidth / 3;
    }

    public void setSelectMode(boolean mode) {
        mSelectMode = mode;
        mCurrentPosition = -1;
        notifyDataSetChanged();
    }

    public void setCurrentPosition(int position) {
        mCurrentPosition = position;
        notifyDataSetChanged();
    }

    public int getCount() {
        return mFileInfos.size();
    }

    public Object getItem(int position) {
        return mFileInfos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup par) {

        FileInfo finfo = mFileInfos.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.grid_item, null);
            convertView.setLayoutParams(new GridView.LayoutParams(
                    GridView.LayoutParams.MATCH_PARENT, mGridItemHeight));
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();

        if (holder == null) {
            holder = setholder(convertView);
        }

        holder.f_container.setBackgroundResource(R.drawable.press_selector);

        if (mRemote) {
            try {
                String url = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" +
                        RemoteCameraConnectManager.HTTP_SERVER_PORT +
                        "/cgi-bin/Config.cgi?action=thumbnail&property=path&value=" +
                        URLEncoder.encode(finfo.getFullPath(), "UTF-8");
                Glide.with(mContext)
                        .load(url)
                        .placeholder(R.drawable.thumbnail_default)
                        .error(R.drawable.thumbnail_default)
                        .centerCrop()
                        .into(holder.f_icon);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            String url = finfo.getFullPath();
            url = "file://" + url;
            Glide.with(mContext)
                    .load(url)
                    .placeholder(R.drawable.thumbnail_default)
                    .error(R.drawable.thumbnail_default)
                    .centerCrop()
                    .into(holder.f_icon);
        }

        if (finfo.isDirectory) {
            holder.size_text.setText("" + finfo.sub);
            holder.f_progressbar.setVisibility(View.GONE);
            holder.f_type.setVisibility(View.GONE);

        } else {

            int type = FileMediaType.getMediaType(finfo.name);
            if (type == FileMediaType.VIDEO_TYPE) {
//            	holder.f_type.setVisibility(View.VISIBLE);
            } else {
                holder.f_type.setVisibility(View.GONE);
            }
            String infoStr = fileSizeMsg(finfo.lsize);
            holder.size_text.setText(infoStr);

            if (finfo.downloading) {
                holder.f_progressbar.setVisibility(View.VISIBLE);
                holder.f_progressbar.setProgress(finfo.downloadProgress);
                holder.progress_text.setVisibility(View.VISIBLE);
                if (finfo.downloadProgress == 0)
                    holder.progress_text.setText(R.string.wait_for_download);
                else
                    holder.progress_text.setText("" + finfo.downloadProgress + "%");
            } else {
                holder.f_progressbar.setVisibility(View.GONE);
                holder.progress_text.setVisibility(View.GONE);
            }
        }
        String camTye;
        if (finfo.name.startsWith("F")) {
            camTye = mContext.getString(R.string.front_cam);
        } else if (finfo.name.startsWith("B")) {
            camTye = mContext.getString(R.string.back_cam);
        } else {
            camTye = mContext.getString(R.string.back_cam);
        }
        String event ="";
        int color = 0;
        if (finfo.path.contains(REMOTE_LOCK_PATH)) {
            if (finfo.name.contains("PZ")) {
                event = mContext.getString(R.string.urgent);
                color = R.color.volume_stroke;
            } else {
                event = mContext.getString(R.string.capture_file);
                color = R.color.caputre;//modifed from b to green
            }
        } else if (finfo.path.contains(REMOTE_CAPTURE_PATH)) {
            event = mContext.getString(R.string.capture_file);
            color = R.color.caputre;
        } else {
            event = mContext.getString(R.string.loop_file);
            color = R.color.photo_color;
        }

        camTye = camTye + " " + event;
        holder.tv_flag.setText(camTye);
        holder.tv_flag.setBackgroundResource(color);
        // date
        String dateStr = Util.name2HourString(finfo.name);
        if (dateStr == null) {
            long time = finfo.modifytime;
            SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss");
            dateStr = sdf.format(new Date(time));
        }
        holder.date_text.setText(dateStr);

//		if(mCurrentPosition == position){
//			holder.size_text.setTextColor(0xFF5677FC);
//			holder.date_text.setTextColor(0xFF5677FC);
//		}else{
//			holder.size_text.setTextColor(0xFF333333);
//			holder.date_text.setTextColor(0xFF333333);
//		}

        if (mSelectMode) {
            holder.f_checkbox.setVisibility(View.VISIBLE);
            holder.f_checkbox.setChecked(finfo.selected);
        } else {
            holder.f_checkbox.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        FileInfo finfo = mFileInfos.get(position);
        return finfo.getHeaderId();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        FileInfo finfo = mFileInfos.get(position);
        HeaderViewHolder holder;
        if (convertView == null) {
            Log.d(TAG, "getHeaderView() convertView == null position = " + position);
            convertView = mInflater.inflate(R.layout.list_header, null);
            holder = new HeaderViewHolder();
            holder.mTitleview = (TextView) convertView.findViewById(R.id.title_date);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        Date date = new Date(finfo.modifytime);
        if (finfo.modifytime > DateUtils.getTodayBeginTimestamp() * 1000)
            holder.mTitleview.setText(R.string.post_view_time_insidetoday);
        else if (finfo.modifytime > DateUtils.getYesterdayBeginTimestamp() * 1000)
            holder.mTitleview.setText(R.string.post_view_time_insideyesterday);
        else
            holder.mTitleview.setText(mSdf.format(date));

        return convertView;
    }

    private ViewHolder setholder(View convertView) {
        ViewHolder holder = new ViewHolder();

        holder.f_container = convertView.findViewById(R.id.item_container);
        holder.date_text = ((TextView) convertView.findViewById(R.id.item_date));
        holder.tv_flag = ((TextView) convertView.findViewById(R.id.tv_flag));
        holder.size_text = ((TextView) convertView.findViewById(R.id.item_size));
        holder.progress_text = (TextView) convertView.findViewById(R.id.download_progress);
        holder.f_icon = ((ImageView) convertView.findViewById(R.id.item_img));
        holder.f_type = ((ImageView) convertView.findViewById(R.id.item_type));
        holder.f_checkbox = ((CheckBox) convertView.findViewById(R.id.select_box));
        holder.f_progressbar = (SquareProgressBar) convertView.findViewById(R.id.download_progressbar);
        convertView.setTag(holder);
        return holder;
    }

    private String fileSizeMsg(long length) {

        int sub_index = 0;
        String show = "";

        if (length >= 1073741824) {
            sub_index = (String.valueOf((float) length / 1073741824))
                    .indexOf(".");
            show = ((float) length / 1073741824 + "000").substring(0,
                    sub_index + 2) + "GB";
        } else if (length >= 1048576) {
            sub_index = (String.valueOf((float) length / 1048576))
                    .indexOf(".");
            show = ((float) length / 1048576 + "000").substring(0,
                    sub_index + 2) + "MB";
        } else if (length >= 1024) {
            sub_index = (String.valueOf((float) length / 1024))
                    .indexOf(".");
            show = ((float) length / 1024 + "000").substring(0,
                    sub_index + 2) + "KB";
        } else if (length < 1024) {
            show = String.valueOf(length) + "B";
        }

        return show;
    }

    private int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private class ViewHolder {
        View f_container;
        TextView size_text;
        TextView date_text;
        TextView tv_flag;
        TextView progress_text;
        ImageView f_icon;
        ImageView f_type;
        CheckBox f_checkbox;
        SquareProgressBar f_progressbar;
    }

    private class HeaderViewHolder {
        TextView mTitleview;
    }
}
