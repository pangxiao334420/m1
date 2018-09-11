package com.goluk.a6.control.ui.track;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.control.R;
import com.goluk.a6.control.R2;
import com.goluk.a6.control.ui.base.BaseViewHolder;
import com.goluk.a6.http.responsebean.TrackList.Track;

import butterknife.BindView;

public class TrackViewHolder extends BaseViewHolder<Track> {

    @BindView(R2.id.tv_group)
    TextView mTvGroup;
    @BindView(R2.id.tv_time)
    TextView mTvTime;
    @BindView(R2.id.tv_mileage)
    TextView mTvMileage;
    @BindView(R2.id.tv_total_time)
    TextView mTvTotalTime;
    @BindView(R2.id.tv_speed)
    TextView mTvSpeed;

    @Override
    protected void onBindData(Track track) {
        super.onBindData(track);

        if (!TextUtils.isEmpty(track.gourpName)) {
            mTvGroup.setText(DateUtils.getWeek(track.gourpName));
            mTvGroup.setVisibility(View.VISIBLE);
        } else {
            mTvGroup.setVisibility(View.GONE);
        }
        mTvTime.setText(DateUtils.getDate(track.startTime) + " - " + DateUtils.getDate(track.endTime));
        mTvMileage.setText(String.format("%.01f", track.mileage));
        mTvTotalTime.setText(DateUtils.parseToMinite(track.endTime - track.startTime));
        mTvSpeed.setText(String.format("%.01f", track.speed));
    }

    @Override
    protected int getViewHolderLayout() {
        return R.layout.viewholder_track_item;
    }

}
