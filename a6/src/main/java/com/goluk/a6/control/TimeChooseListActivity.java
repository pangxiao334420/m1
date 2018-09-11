package com.goluk.a6.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by goluk_lium on 2018/4/10.
 */

public class TimeChooseListActivity extends BaseActivity implements
        AdapterView.OnItemClickListener {

    public static final int PAGE_SLEEP_SETTING = 101;
    public static final int PAGE_SHARE_SETTING = 201;

    private ListView mListView;
    private TextView mTextDesc;

    private Integer[] timeSleep = new Integer[]{R.string.number_10,R.string.number_20,R.string.number_30,R.string.negative_number_1};
    private Integer[] timeShare = new Integer[]{R.string.number_2,R.string.number_6,R.string.number_12,R.string.number_24,R.string.negative_number_1};
    private String mCurrentChoose;
    private String mOldChoose;
    private int currentActivity;
    private ChooseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_time_list);
        obtainIntentData();
        showBack(true);
        initView();
    }

    private void obtainIntentData(){
        Intent intent = getIntent();
        if (intent!=null){
            currentActivity = intent.getIntExtra("currentActivity",-1);
            mCurrentChoose = intent.getStringExtra("currentValue");
            mOldChoose = mCurrentChoose;
        }
        if (currentActivity==PAGE_SHARE_SETTING){
            setTitle(getString(R.string.str_share_validity));
            mAdapter = new ChooseAdapter(Arrays.asList(timeShare),this,PAGE_SHARE_SETTING);
        }else if (currentActivity==PAGE_SLEEP_SETTING){
            setTitle(getString(R.string.str_sleep_time));
            mAdapter = new ChooseAdapter(Arrays.asList(timeSleep),this,PAGE_SLEEP_SETTING);
        }else {

        }
    }

    private void initView(){
        mListView = (ListView) findViewById(R.id.lv_sleep_times);
        mTextDesc = (TextView) findViewById(R.id.tv_choose_desc);
        mListView.setOnItemClickListener(this);
        if (mCurrentChoose!=null)mListView.setAdapter(mAdapter);
        if (currentActivity==PAGE_SHARE_SETTING){
            mTextDesc.setText(R.string.hint_setting_slare);
        }else if (currentActivity==PAGE_SLEEP_SETTING){
            mTextDesc.setText(R.string.hint_setting_sleep);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentActivity==PAGE_SHARE_SETTING){
            mCurrentChoose = getString(timeShare[position]);
        }else if (currentActivity==PAGE_SLEEP_SETTING){
            mCurrentChoose = getString(timeSleep[position]);
        }
        mAdapter.notifyDataSetChanged();

        if (mCurrentChoose != null && !mCurrentChoose.equals(mOldChoose)) {
            Intent intent = new Intent();
            intent.putExtra("result", mCurrentChoose);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    private class ChooseAdapter extends BaseAdapter{

        private List<Integer> mDatas;
        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private int mPageType;
        public ChooseAdapter(List<Integer> datas,Context context,int type) {
            mDatas = datas;
            mContext = context;
            mLayoutInflater = LayoutInflater.from(mContext);
            mPageType = type;
        }

        @Override
        public int getCount() {
            return mDatas!=null?mDatas.size():0;
        }

        @Override
        public Object getItem(int position) {
            return mDatas!=null?mDatas.get(position):null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView!=null){
                holder = (ViewHolder) convertView.getTag();
            }else {
                holder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.activity_sleep_time_list_item,parent,false);
                holder.mTvValues = (TextView) convertView.findViewById(R.id.tv_sleep_time_value);
                holder.mSelectTag = (ImageView) convertView.findViewById(R.id.iv_sleep_time_select);
                convertView.setTag(holder);
            }
            if (mContext.getResources().getString(mDatas.get(position)).equals(mCurrentChoose)){
                holder.mSelectTag.setVisibility(View.VISIBLE);
            }else {
                holder.mSelectTag.setVisibility(View.GONE);
            }
            if (mDatas.get(position)==R.string.negative_number_1){
                if (mPageType==TimeChooseListActivity.PAGE_SLEEP_SETTING){
                    holder.mTvValues.setText(mContext.getResources().getString(R.string.str_never));
                }else if (mPageType==TimeChooseListActivity.PAGE_SHARE_SETTING){
                    holder.mTvValues.setText(mContext.getResources().getString(R.string.str_longtime));
                }
            }else {
                if (mPageType == TimeChooseListActivity.PAGE_SLEEP_SETTING) {
                    holder.mTvValues.setText(
                            mContext.getResources().getString(mDatas.get(position))
                                    + " " + mContext.getResources().getString(R.string.min));
                } else if (mPageType == TimeChooseListActivity.PAGE_SHARE_SETTING) {
                    holder.mTvValues.setText(
                            mContext.getResources().getString(mDatas.get(position))
                                    + " " + mContext.getResources().getString(R.string.str_unit_hour));
                }
            }
            return convertView;
        }
        private class ViewHolder {
            TextView mTvValues;
            ImageView mSelectTag;
        }


    }
}
