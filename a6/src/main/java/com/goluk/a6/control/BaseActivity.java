package com.goluk.a6.control;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.internation.CustomLoadingDialog;

/**
 * Created by devel on 16/12/13.
 */

public class BaseActivity extends FragmentActivity {
    public boolean backGround;
    public CarControlApplication mApp = null;
    public boolean noActionBar = false;
    protected boolean mActivityDestroyed = false;
    private CustomLoadingDialog mLoadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = CarControlApplication.getInstance();
        if (getActionBar() == null) {
            noActionBar = true;
        }
        if (noActionBar) {
            return;
        }
        getActionBar().setHomeButtonEnabled(false); // disable the button
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setCustomView(R.layout.control_title);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getActionBar().getCustomView().findViewById(R.id.tv_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Window window = getWindow();
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                getActionBar().setElevation(0);
////                Toolbar parent = (Toolbar) getActionBar().getCustomView().getParent();
////                parent.setContentInsetsAbsolute(0, 0);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void showLoaing() {
        this.showLoaing("");
    }

    public void showLoaing(String text) {
        mLoadingDialog = new CustomLoadingDialog(this, text);
        mLoadingDialog.show();
    }

    public void closeLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.close();
            mLoadingDialog = null;
        }
    }


    public void setTitle(int titleRes) {
        setTitle(getString(titleRes));
    }


    public void setTitle(String title) {
        if (noActionBar) {
            return;
        }
        TextView tv = (TextView) getActionBar().getCustomView().findViewById(R.id.tv_center);
        tv.setText(title);
    }


    public void showBack(boolean value) {
        if (noActionBar) {
            return;
        }
        if (value) {
            getActionBar().getCustomView().findViewById(R.id.tv_left).setVisibility(View.VISIBLE);
        } else {
            getActionBar().getCustomView().findViewById(R.id.tv_left).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showToast(int res) {
        showToast(getResources().getString(res));
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        backGround = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        backGround = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityDestroyed = true;
    }
}
