package com.goluk.a6.control.dvr;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.goluk.a6.control.R;
import com.goluk.a6.control.dvr.model.ApnBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goluk_lium on 2018/1/30.
 */

public class ApnSettingFragment extends Fragment{


    private List<ApnBean> apnList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    private class ApnListAdapter extends BaseAdapter{

        private LayoutInflater layoutInflater;
        private List<ApnBean> apnBeen;
        public ApnListAdapter(Context context,List<ApnBean> apnBeen) {
            this.layoutInflater = LayoutInflater.from(context);
            this.apnBeen = apnBeen;
        }

        @Override
        public int getCount() {
            return apnBeen==null?0:apnBeen.size();
        }

        @Override
        public Object getItem(int position) {
            return apnBeen==null?null:apnBeen.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textApn;
            if (convertView == null){
                convertView = layoutInflater.inflate(R.layout.layout_apn_cell,parent,false);
                textApn = (TextView) convertView.findViewById(R.id.text_apnName);
                convertView.setTag(textApn);
            }else {
                textApn = (TextView) convertView.getTag();
            }
            textApn.setText(apnBeen.get(position).name);

            return convertView;
        }
    }


}
