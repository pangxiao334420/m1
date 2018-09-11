package com.goluk.a6.control.ui.track;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goluk.a6.api.ApiUtil;
import com.goluk.a6.api.Callback;
import com.goluk.a6.common.map.MyCarMapView;
import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.control.R2;
import com.goluk.a6.control.util.AddressConvert;
import com.goluk.a6.http.responsebean.TrackDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 轨迹详情页面
 */
public class TrackDetailActivity extends BaseActivity {

    @BindView(R2.id.title)
    TextView mTvTitle;
    @BindView(R2.id.tv_mileage)
    TextView mTvMileage;
    @BindView(R2.id.tv_total_time)
    TextView mTvTotalTime;
    @BindView(R2.id.tv_speed)
    TextView mTvSpeed;

    @BindView(R2.id.tv_date)
    TextView mTvDate;
    @BindView(R2.id.tv_time)
    TextView mTvTime;
    @BindView(R2.id.tv_address_start)
    TextView mTvAddressStart;
    @BindView(R2.id.tv_address_end)
    TextView mTvAddressEnd;

    @BindView(R2.id.map_container)
    RelativeLayout mMapContainer;

    private MyCarMapView mMap;

    private String mTrackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_detail);
        ButterKnife.bind(this);

        intiView();
        initData();
    }

    public void intiView() {
        mTvTitle.setText(R.string.track_detail);

        mMap = MyCarMapView.create(this);
        mMapContainer.addView(mMap);
    }

    private void initData() {
        mTrackId = getIntent().getStringExtra("trackId");
        queryTrackDetail();
    }

    private void queryTrackDetail() {
        if (TextUtils.isEmpty(mTrackId))
            return;

        ApiUtil.apiService().trackDetail(mTrackId, new Callback<TrackDetail>() {
            @Override
            protected void onError(int code, String msg) {

            }

            @Override
            public void onResponse(TrackDetail trackDetail) {
                onGetTrackDetail(trackDetail);
            }
        });
    }

    private void onGetTrackDetail(TrackDetail trackDetail) {
        if (trackDetail == null)
            return;

        // 里程/用时/速度
        mTvMileage.setText(String.format("%.01f", trackDetail.mileage));
        mTvTotalTime.setText(DateUtils.parseToMinite(trackDetail.endTime - trackDetail.startTime));
        mTvSpeed.setText(String.format("%.01f", trackDetail.speed));

        // 时间
        mTvDate.setText(DateUtils.getWeek(DateUtils.parseToDayStr(trackDetail.startTime)));
        mTvTime.setText(DateUtils.getDate(trackDetail.startTime) + " - " + DateUtils.getDate(trackDetail.endTime));

        // 起点/终点位置
        AddressConvert addressConvert = new AddressConvert();
        addressConvert.convert(trackDetail.startLocation.lat, trackDetail.startLocation.lon, new AddressConvert.AddressConvertCallback() {
            @Override
            public void onAddressConverted(String address) {
                mTvAddressStart.setText(address);
            }
        });
        addressConvert.convert(trackDetail.endLocation.lat, trackDetail.endLocation.lon, new AddressConvert.AddressConvertCallback() {
            @Override
            public void onAddressConverted(String address) {
                mTvAddressEnd.setText(address);
            }
        });

        // 地图轨迹
        if (mMap != null)
            mMap.drawTrackLine(trackDetail.points);
    }

    @OnClick({R2.id.btn_back})
    void onViewClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_back) {
            finish();
        }
    }

}
