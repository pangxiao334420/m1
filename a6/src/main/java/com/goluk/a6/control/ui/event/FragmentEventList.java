package com.goluk.a6.control.ui.event;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goluk.a6.api.ApiUtil;
import com.goluk.a6.api.Callback;
import com.goluk.a6.common.event.util.Event;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;
import com.goluk.a6.control.R2;
import com.goluk.a6.control.util.CollectionUtils;
import com.goluk.a6.http.responsebean.EventVideoList;
import com.goluk.a6.http.responsebean.EventVideoList.EventVideo;
import com.goluk.a6.internation.GolukUtils;
import com.ksy.statlibrary.util.NetworkUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import likly.dollar.$;
import likly.view.repeat.OnHolderClickListener;
import likly.view.repeat.RecyclerRefreshLayout;
import likly.view.repeat.RepeatView;

/**
 * 事件列表Fragment
 */
public class FragmentEventList extends Fragment implements OnHolderClickListener<EventViewHolder>, RecyclerRefreshLayout.SuperRefreshLayoutListener {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final String PARAM_TYPE = "type";
    // 事件列表
    public static final int TYPE_EVENT_LIST = 1;
    // 收藏的事件列表
    public static final int TYPE_EVENT_COLLECTION_LIST = 2;

    private int mType = TYPE_EVENT_LIST;

    @BindView(R2.id.refreshLayout)
    RecyclerRefreshLayout mRefreshLayout;
    @BindView(R2.id.repeatView)
    RepeatView<EventVideo, EventViewHolder> mRepeatView;
    private List<EventVideo> mEvents;

    private int mIndex;

    public static FragmentEventList newInstance(int type) {
        FragmentEventList fragment = new FragmentEventList();
        Bundle args = new Bundle();
        args.putInt(PARAM_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mType = arguments.getInt(PARAM_TYPE);
        }

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        ButterKnife.bind(this, view);
        initData(view);
        return view;
    }

    private void initData(View view) {
        mRepeatView.onClick(this);
        mRefreshLayout.setSuperRefreshLayoutListener(this);
        mEvents = new ArrayList<>();

        queryMessages(false);
    }

    private void queryMessages(boolean showLoading) {
        if (!NetworkUtil.isNetworkAvailable(getContext())) {
            $.toast().text(R.string.user_net_unavailable).show();
            mRefreshLayout.onComplete();
            return;
        }

        int operation = CollectionUtils.isEmpty(mEvents) ? 0 : 2;

        if (mType == TYPE_EVENT_LIST) {
            String imei = CarControlApplication.getInstance().serverImei;
            if (!TextUtils.isEmpty(imei)) {
                ApiUtil.apiService().getEventVideoList(imei, operation, mIndex, DEFAULT_PAGE_SIZE,
                        new Callback<EventVideoList>() {
                            @Override
                            protected void onError(int code, String msg) {
                            }

                            @Override
                            public void onResponse(EventVideoList response) {
                                super.onResponse(response);

                                onGetEvents(response);
                            }
                        });
            } else {
                mRepeatView.layoutAdapterManager().showEmptyView();
            }
        } else if (mType == TYPE_EVENT_COLLECTION_LIST) {
            ApiUtil.apiService().getCollectionEventVideoList(operation, mIndex, DEFAULT_PAGE_SIZE,
                    new Callback<EventVideoList>() {
                        @Override
                        protected void onError(int code, String msg) {
                        }

                        @Override
                        public void onResponse(EventVideoList response) {
                            super.onResponse(response);

                            onGetEvents(response);
                        }
                    });
        }
    }

    public void onGetEvents(EventVideoList eventList) {
        mRefreshLayout.onComplete();

        if (eventList == null || CollectionUtils.isEmpty(eventList.events)) {
            if (CollectionUtils.isEmpty(mEvents))
                mRepeatView.layoutAdapterManager().showEmptyView();
            mRepeatView.setHasMore(false);
            return;
        }

        if (eventList.size >= DEFAULT_PAGE_SIZE) {
            // 还有数据
            mRepeatView.setHasMore(true);
        } else {
            // 没有更多数据
            mRepeatView.setHasMore(false);
        }
        mIndex = eventList.events.get(eventList.size - 1).index;

        mEvents.addAll(eventList.events);
        mRepeatView.viewManager().bind(mEvents);
        mRepeatView.layoutAdapterManager().showRepeatView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (EventUtil.isDefaultDeviceChangedEvent(event)) {
            onRefreshing();
        }
    }

    @Override
    public void onRefreshing() {
        mEvents = new ArrayList<>();
        mIndex = 0;
        queryMessages(false);
    }

    @Override
    public void onLoadMore() {
        queryMessages(false);
    }

    @Override
    public void onHolderClick(EventViewHolder holder) {
        if (!GolukUtils.isNetworkConnected(getContext())) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }
        // 跳转到事件详情
        EventVideo eventVideo = holder.getData();
        Intent intent = new Intent(getContext(), EventDetailActivity.class);
        intent.putExtra("eventId", eventVideo.eventId);
        getContext().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
