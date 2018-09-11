package com.goluk.a6.control.ui.track;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.goluk.a6.api.ApiUtil;
import com.goluk.a6.api.Callback;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.control.R2;
import com.goluk.a6.control.util.CollectionUtils;
import com.goluk.a6.http.responsebean.TrackList;
import com.goluk.a6.http.responsebean.TrackList.Track;
import com.ksy.statlibrary.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import likly.dollar.$;
import likly.view.repeat.OnHolderClickListener;
import likly.view.repeat.RecyclerRefreshLayout;
import likly.view.repeat.RepeatView;

/**
 * 历史轨迹列表页面
 */
public class TrackListActivity extends BaseActivity implements RecyclerRefreshLayout.SuperRefreshLayoutListener, OnHolderClickListener<TrackViewHolder> {

    private static final int DEFAULT_PAGE_SIZE = 20;

    @BindView(R2.id.title)
    TextView mTvTitle;
    @BindView(R2.id.refreshLayout)
    RecyclerRefreshLayout mRefreshLayout;
    @BindView(R2.id.repeatView)
    RepeatView<Track, TrackViewHolder> mRepeatView;
    private List<Track> mTracks;
    private int mIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list);
        ButterKnife.bind(this);

        intiView();
    }

    public void intiView() {
        mTvTitle.setText(R.string.track_list);

        mRepeatView.onClick(this);
        mRefreshLayout.setSuperRefreshLayoutListener(this);
        mTracks = new ArrayList<>();

        queryTrackList(true);
    }

    @OnClick({R2.id.btn_back})
    void onViewClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_back) {
            finish();
        }
    }

    private void queryTrackList(boolean showLoading) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            $.toast().text(R.string.user_net_unavailable).show();
            mRefreshLayout.onComplete();
            return;
        }

        int operation = CollectionUtils.isEmpty(mTracks) ? 0 : 2;

        ApiUtil.apiService().trackList(mApp.serverImei, operation, mIndex, DEFAULT_PAGE_SIZE,
                new Callback<TrackList>() {
                    @Override
                    protected void onError(int code, String msg) {
                    }

                    @Override
                    public void onResponse(TrackList response) {
                        super.onResponse(response);
                        onGetTrackList(response);
                    }
                });
    }

    public void onGetTrackList(TrackList trackList) {
        mRefreshLayout.onComplete();

        if (trackList == null || CollectionUtils.isEmpty(trackList.tracks)) {
            if (CollectionUtils.isEmpty(mTracks))
                mRepeatView.layoutAdapterManager().showEmptyView();
            mRepeatView.setHasMore(false);
            return;
        }

        if (trackList.size >= DEFAULT_PAGE_SIZE) {
            // 还有数据
            mRepeatView.setHasMore(true);
        } else {
            // 没有更多数据
            mRepeatView.setHasMore(false);
        }
        mIndex = trackList.tracks.get(trackList.size - 1).index;

        // 日期分组
        boolean isSame = isDateSame(mTracks, trackList.tracks);
        parseTrackData(isSame, trackList.tracks);

        mTracks.addAll(trackList.tracks);
        mRepeatView.viewManager().bind(mTracks);
        mRepeatView.layoutAdapterManager().showRepeatView();
    }

    /**
     * 比较现有列表最后一条和待新加的列表第一条的日期是否相同
     *
     * @param trackCurrent 现有列表
     * @param trackNew     待新加列表
     */
    private boolean isDateSame(List<Track> trackCurrent, List<Track> trackNew) {
        if (CollectionUtils.isEmpty(trackCurrent) || CollectionUtils.isEmpty(trackNew))
            return false;

        return TextUtils.equals(trackCurrent.get(trackCurrent.size() - 1).addDate, trackNew.get(0).addDate);
    }

    /**
     * 按日期分组
     */
    private void parseTrackData(boolean isSame, List<Track> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Track track = list.get(i);
            if (i == 0) {
                if (!isSame)
                    track.gourpName = track.addDate;
                continue;
            }

            Track trackPrevious = list.get(i - 1);
            if (TextUtils.equals(track.addDate, trackPrevious.addDate)) {
                continue;
            } else {
                track.gourpName = track.addDate;
            }
        }
    }

    @Override
    public void onRefreshing() {
        mTracks = new ArrayList<>();
        mIndex = 0;
        queryTrackList(false);
    }

    @Override
    public void onLoadMore() {
        queryTrackList(false);
    }

    @Override
    public void onHolderClick(TrackViewHolder holder) {
        Track track = holder.getData();
        if (track != null) {
            Intent intent = new Intent(this, TrackDetailActivity.class);
            intent.putExtra("trackId", track.trackId);
            startActivity(intent);
        }
    }

}
