package com.goluk.a6.control.ui.event;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.control.R;
import com.goluk.a6.control.R2;
import com.goluk.a6.control.ui.base.BaseViewHolder;
import com.goluk.a6.http.responsebean.EventVideoList.EventVideo;
import com.goluk.a6.internation.GlideUtils;

import butterknife.BindView;

public class EventViewHolder extends BaseViewHolder<EventVideo> {

    @BindView(R2.id.iv_thumb)
    ImageView mIvThumb;
    @BindView(R2.id.tv_time)
    TextView mTime;
    @BindView(R2.id.tv_event_desc)
    TextView mEventDesc;
    @BindView(R2.id.tv_event_type)
    TextView mEventType;

    @Override
    protected void onBindData(EventVideo message) {
        super.onBindData(message);

        mTime.setText(DateUtils.parseMillesToTimeString(message.time));
        String picture = !TextUtils.isEmpty(message.forePicture) ? message.forePicture : message.backPicture;
        GlideUtils.loadImage(getContext(), mIvThumb, picture, R.drawable.thumbnail_default);
        int[] resIds = getResIdByType(message.type);
        mEventDesc.setText(resIds[0]);
        mEventType.setText(resIds[1]);
        mEventType.setCompoundDrawablesWithIntrinsicBounds(resIds[2], 0, 0, 0);
    }

    @Override
    protected int getViewHolderLayout() {
        return R.layout.viewholder_event_video_item;
    }

    private int[] getResIdByType(int type) {
        int[] resIds = new int[3];
        int result = type / 100;
        if (result == 1) {
            if (type == 104) {
                // SOS 事件
                resIds[0] = R.string.event_sos_desc;
                resIds[1] = R.string.event_type_sos;
                resIds[2] = R.drawable.icon_capture;
            } else {
                // 行车紧急事件
                resIds[0] = R.string.event_collision_desc;
                resIds[1] = R.string.event_type_collision;
                resIds[2] = R.drawable.icon_emergency;
            }
        } else if (result == 2) {
            // 停车异常事件
            resIds[0] = R.string.event_parking_desc;
            resIds[1] = R.string.event_type_parking;
            resIds[2] = R.drawable.icon_abnormalparking;
        } else if (result == 4) {
            // 抓拍事件
            resIds[0] = R.string.event_capture_desc;
            resIds[1] = R.string.event_type_capture;
            resIds[2] = R.drawable.icon_capture;
        }

        return resIds;
    }

}
