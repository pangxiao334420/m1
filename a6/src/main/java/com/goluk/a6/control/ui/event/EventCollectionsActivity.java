package com.goluk.a6.control.ui.event;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;

/**
 * 收藏的事件列表页面
 */
public class EventCollectionsActivity extends BaseActivity implements OnClickListener {

    private ImageView mBtnBack;
    private TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_collections);

        intiView();
    }

    public void intiView() {
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mTvTitle = (TextView) findViewById(R.id.title);

        mTvTitle.setText(R.string.message_list_my);
        mBtnBack.setOnClickListener(this);

        FragmentEventList mFragmentEventList = FragmentEventList.newInstance(FragmentEventList.TYPE_EVENT_COLLECTION_LIST);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mFragmentEventList).commit();
    }

    @Override
    public void onClick(View View) {
        if (View.getId() == R.id.btn_back) {
            finish();
        }
    }

}
