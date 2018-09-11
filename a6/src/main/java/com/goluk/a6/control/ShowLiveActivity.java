package com.goluk.a6.control;

import android.os.Bundle;

import com.goluk.a6.http.responsebean.FamilyCheckResult;

public class ShowLiveActivity extends BaseActivity {
    public static String LIVE_DATA_BEAN = "livedata";
    private MyCarView myCarView;
    FamilyCheckResult.FamilyUserBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        setTitle(R.string.live);
        showBack(true);
        bean = (FamilyCheckResult.FamilyUserBean) getIntent().getSerializableExtra(LIVE_DATA_BEAN);
        myCarView = (MyCarView) findViewById(R.id.myCarView);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCarView != null)
            myCarView.onActivityDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myCarView != null) {
//            myCarView.setLiveData(bean);
            myCarView.onAcitvityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myCarView != null)
            myCarView.onActivityPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (myCarView != null)
            myCarView.onActivityStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (myCarView != null)
            myCarView.onActivityStop();
    }


    @Override
    public void onBackPressed() {
//        myCarView.closeLive();
    }

}
