package com.goluk.a6.control;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.event.ShowMoreNewEvent;
import com.goluk.a6.common.util.H5Util;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.FluxCurrentStatusRequest;
import com.goluk.a6.http.request.GprsMonthInfoBean;
import com.goluk.a6.http.request.GprsPlanBean;
import com.goluk.a6.http.request.WXPayEntity;
import com.goluk.a6.internation.GlideUtils;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.IPageNotifyFn;
import com.goluk.a6.internation.SharedPrefUtil;
import com.goluk.a6.internation.UserInfohomeRequest;
import com.goluk.a6.internation.UserPersonalInfoActivity;
import com.goluk.a6.internation.UserSosActivity;
import com.goluk.a6.internation.bean.CancelResult;
import com.goluk.a6.internation.bean.UserInfo;
import com.goluk.a6.internation.bean.UserinfohomeRetBean;
import com.goluk.a6.internation.bean.UserinfohomeUserBean;
import com.goluk.a6.internation.login.InternationUserLoginActivity;
import com.goluk.a6.internation.login.UserCancelBeanRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import static com.goluk.a6.internation.IPageNotifyFn.PageType_UserinfoHome;

public class MoreView extends IPagerView implements View.OnClickListener, IRequestResultListener {
    private TextView mTvProduct;
    private TextView mTv2;
    private TextView mTv3;
    private TextView mTv4;
    private RelativeLayout mLayoutSos;
    private LinearLayout mLayoutDeviceManage;
    private LinearLayout mLayoutFamilyShare;
    private CarControlApplication mApp;
    private RelativeLayout mUserCenterItem;
    private ImageView mImageHead;
    private ImageView mImageAuthentication;
    private TextView mTextName;
    private TextView mTextIntroduction;
    private View mNew;

    public MoreView(Context context) {
        super(context);
        initView();
    }

    public MoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MoreView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        EventBus.getDefault().register(this);
        mApp = CarControlApplication.getInstance();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.more_views, this);
        mTvProduct = (TextView) findViewById(R.id.tv_product);
        mTv2 = (TextView) findViewById(R.id.tv_help);
        mNew = findViewById(R.id.v_new);
        mLayoutSos = (RelativeLayout) findViewById(R.id.layout_sos);
        mLayoutDeviceManage = (LinearLayout) findViewById(R.id.tv_manage);
        mLayoutFamilyShare = (LinearLayout) findViewById(R.id.tv_family);
        mTv3 = (TextView) findViewById(R.id.tv_call);
        mTv4 = (TextView) findViewById(R.id.tv_version);
        mTvProduct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WebviewActivity.class);
                intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, getContext().getString(R.string.app_about));
                intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getProduct());
                getContext().startActivity(intent);
            }
        });
        mTv2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WebviewActivity.class);
                intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, getContext().getString(R.string.help));
                intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getHelp());
                getContext().startActivity(intent);
            }
        });
        mTv3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (BuildConfig.BRANCH_CHINA) {
                        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:400-969-1800"));
                        getContext().startActivity(intent);
                    } else {
                        Intent data = new Intent(Intent.ACTION_SENDTO);
                        data.setData(Uri.parse("mailto:service@goluk.com"));
                        data.putExtra(Intent.EXTRA_SUBJECT, getContext().getString(R.string.feedback));
                        data.putExtra(Intent.EXTRA_TEXT, "");
                        getContext().startActivity(data);
                    }
                } catch (Exception ex) {
                }
            }
        });
        mTv4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(getContext(), AppVersionActivity.class);
                getContext().startActivity(loginIntent);
            }
        });
        mLayoutSos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isUserLoginToServerSuccess()) {
                    Intent loginIntent = new Intent(MoreView.this.getContext(), InternationUserLoginActivity.class);
                    MoreView.this.getContext().startActivity(loginIntent);
                    return;
                }
                Intent loginIntent = new Intent(getContext(), UserSosActivity.class);
                getContext().startActivity(loginIntent);
            }
        });

        mLayoutFamilyShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isUserLoginToServerSuccess()) {
                    Intent loginIntent = new Intent(MoreView.this.getContext(), InternationUserLoginActivity.class);
                    MoreView.this.getContext().startActivity(loginIntent);
                    return;
                }
                Intent loginIntent = new Intent(getContext(), FamilyActivity.class);
                getContext().startActivity(loginIntent);
            }
        });
        mLayoutDeviceManage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isUserLoginToServerSuccess()) {
                    Intent loginIntent = new Intent(MoreView.this.getContext(), InternationUserLoginActivity.class);
                    MoreView.this.getContext().startActivity(loginIntent);
                    return;
                }
                Intent loginIntent = new Intent(getContext(), DevicesActivity.class);
                getContext().startActivity(loginIntent);
            }
        });
        patch(findViewById(R.id.manager_flux));
        initUser();
    }

    private void initUser() {
        mUserCenterItem = (RelativeLayout) findViewById(R.id.user_center_item);
        mImageHead = (ImageView) findViewById(R.id.user_center_head);
        mImageAuthentication = (ImageView) findViewById(R.id.im_user_center_head_authentication);
        mTextName = (TextView) findViewById(R.id.user_center_name_text);
        mTextIntroduction = (TextView) findViewById(R.id.user_center_introduction_text);
        mUserCenterItem.setOnClickListener(this);
    }


    @Override
    public void onActivityCreate(Bundle savedInstanceState) {
    }


    private void resetLoginState() {
        if (mApp.isUserLoginToServerSuccess()) {
            if (!TextUtils.isEmpty(mApp.getMyInfo().avatar) && !mApp.getMyInfo().avatar.contains("default")) {
                GlideUtils.loadNetHead(getContext(), mImageHead, mApp.getMyInfo().avatar, R.drawable.usercenter_head_default);
            }
            mTextName.setText(mApp.getMyInfo().name);
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            mTextIntroduction.setText(mApp.getMyInfo().description);
            sendGetUserHomeRequest();
        } else {
            mImageAuthentication.setVisibility(View.GONE);
            GlideUtils.loadLocalHead(getContext(), mImageHead,
                    R.drawable.usercenter_head_default);
            mTextName.setText(getContext().getResources().getString(
                    R.string.str_click_to_login));
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            mTextIntroduction.setText(getContext().getResources().getString(
                    R.string.str_login_tosee_usercenter));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(MenuInflater mi, Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onActivate() {
        resetLoginState();
    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onAcitvityResume() {
        resetLoginState();
    }

    @Override
    public void onActivityDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityStart() {

    }

    @Override
    public void onActivityStop() {

    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void sendGetUserHomeRequest() {
        UserInfohomeRequest request = new UserInfohomeRequest(PageType_UserinfoHome, this);
        request.get("100", mApp.getMyInfo().uid);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.user_center_item ||
                view.getId() == R.id.user_center_head ||
                view.getId() == R.id.im_user_center_head_authentication ||
                view.getId() == R.id.user_center_name_text ||
                view.getId() == R.id.user_center_introduction_text) {
            clickLogin();
        }
    }

    private void clickLogin() {
        if (!mApp.isUserLoginToServerSuccess()) {
            Intent loginIntent = null;
            loginIntent = new Intent(getContext(), InternationUserLoginActivity.class);
            getContext().startActivity(loginIntent);
        } else {
            Intent loginIntent = null;
            loginIntent = new Intent(getContext(), UserPersonalInfoActivity.class);
            getContext().startActivity(loginIntent);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        UserinfohomeRetBean bean = (UserinfohomeRetBean) result;
        if (bean == null || bean.code != 0 || bean.data == null) {
            return;
        }
        UserinfohomeUserBean userbean = bean.data;
        if (!TextUtils.isEmpty(userbean.avatar) && !userbean.avatar.contains("default")) {
            GlideUtils.loadNetHead(getContext(), mImageHead, userbean.avatar, R.drawable.usercenter_head_default);
        }
        if (mApp.getMyInfo() == null) {
            return;
        }
        UserInfo userInfo = mApp.getMyInfo();
        userInfo.avatar = userbean.avatar;
        userInfo.description = userbean.description;
        try {
            userInfo.name = URLDecoder.decode(userbean.name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            userInfo.name = String.valueOf(userbean.name);
        }
        userInfo.avatar = userbean.avatar;
        userInfo.avatar = userbean.avatar;
        userInfo.sex = userbean.sex;
        mTextName.setText(userInfo.name);

        if ("email".equals(mApp.getMyInfo().platform)) {
            userInfo.email = userbean.account.email;
            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            mTextIntroduction.setText(userInfo.email);
        } else if ("phone".equals(mApp.getMyInfo().platform)) {
            String phone = userbean.account.phone;
            // 格式转换
            if (!TextUtils.isEmpty(phone) && phone.contains("-")) {
                String[] data = phone.split("-");
                phone = "+" + data[0] + " " + data[1];
            }
            userInfo.phone = phone;

            mTextIntroduction.setTextColor(Color.rgb(128, 138, 135));
            mTextIntroduction.setText(userInfo.phone);
        } else {
            mTextIntroduction.setVisibility(GONE);
        }

        SharedPrefUtil.saveUserInfo(com.alibaba.fastjson.JSONObject.toJSONString(userInfo));
        if (!TextUtils.isEmpty(bean.data.emgContactPhone)) {
            EventBus.getDefault().post(new ShowMoreNewEvent(false));
        } else {
            EventBus.getDefault().post(new ShowMoreNewEvent(true));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowMoreNewEvent event) {
        mNew.setVisibility(event.value ? VISIBLE : GONE);
    }

    private boolean isChinaVersion = BuildConfig.BRANCH_CHINA;
    private boolean isFeiMaoSimCard = true;
    private String simCode;
    private LinearLayout textFluxManager;
    private ProgressDialog mProgressDialog = new ProgressDialog(getContext());

    private void patch(View layout) {
        if (!isChinaVersion) {
            layout.setVisibility(View.GONE);
        } else {
            mProgressDialog.setMessage("获取SIM卡信息...");
            textFluxManager = (LinearLayout) layout.findViewById(R.id.manager_flux);
            textFluxManager.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mApp.isUserLoginToServerSuccess()) {
                        Intent loginIntent = new Intent(MoreView.this.getContext(), InternationUserLoginActivity.class);
                        MoreView.this.getContext().startActivity(loginIntent);
                        return;
                    }

                    if (!hasDevice()) {
                        Toast.makeText(getContext(), "请检查设备是否绑定", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!hasDeviceSimIccid()) {
                        Toast.makeText(getContext(), "该设备没有SIM卡信息", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    verifySimCode(mApp.defaultDeviceIccid);

                }
            });
        }
    }

    private void openFluxManagerAty() {
        try {
            Intent fluxIntent = new Intent(getContext(), Class.forName("com.goluk.a6.control.flux.FluxManagerActivity"));
            fluxIntent.putExtra("simCode", simCode);
            fluxIntent.putExtra("simCardType", isFeiMaoSimCard);
            getContext().startActivity(fluxIntent);
        } catch (ClassNotFoundException ex) {
            //国际版使用反射启动，减小国际版本的apk文件大小
        }
    }

    private void verifySimCode(String iccid) {
        if (!GolukUtils.isNetworkConnected(getContext())) {
            GolukUtils.showToast(getContext(), getResources().getString(R.string.user_net_unavailable));
            return;
        }
        simCode = null;
        FluxCurrentStatusRequest.FluxRequestListener listener = new FluxCurrentStatusRequest.FluxRequestListener() {
            @Override
            public void onGprsPlanInfo(GprsPlanBean gprsPlanBean) {
            }

            @Override
            public void onGprsMonthInfo(List<GprsMonthInfoBean> gprsMonthInfoBeanList) {
            }

            @Override
            public void onFailure(int errCode) {
                Log.e("More VIew onFailure", "onFailure: " + errCode);
                mProgressDialog.dismiss();
                switch (errCode) {
                    case Config.SERVER_TOKEN_DEVICE_INVALID:
                        logout();
                        GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.server_token_device_invalid));
                        break;
                    case Config.SERVER_TOKEN_EXPIRED:
                    case Config.SERVER_TOKEN_INVALID:
                        logout();
                        GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.server_token_expired));
                        break;
                    case Config.CODE_VOLLEY_NETWORK_ERROR:
                        GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.network_invalid));
                        break;
                    case Config.SERVER_INVALID:
                        isFeiMaoSimCard = false;
                        openFluxManagerAty();
                        break;
                    default:
                        GolukUtils.showToast(getContext(), "未知错误，请稍后再试");
                        break;
                }
            }

            @Override
            public void onGenerateOrder(WXPayEntity entity) {
            }

            @Override
            public void onSimCode(String code) {
                mProgressDialog.dismiss();
                if (code != null && code.equals(FluxCurrentStatusRequest.IS_NOT_FEIMAO_CARD)) {
                    isFeiMaoSimCard = false;
                    openFluxManagerAty();
                    return;
                }

                if (code != null && code.length() > 0) {
                    isFeiMaoSimCard = true;
                    simCode = code;
                    openFluxManagerAty();
                }
            }
        };
        FluxCurrentStatusRequest request = new FluxCurrentStatusRequest(listener);
        request.requestSimCode(iccid);
        mProgressDialog.show();

    }

    private boolean hasDeviceSimIccid() {
        String defaultDeviceIccid = mApp.defaultDeviceIccid;
        return (defaultDeviceIccid == null || defaultDeviceIccid == "") ? false : true;
    }

    private boolean hasDevice() {
        if (mApp.bindListBean == null || mApp.serverImei.equals("")) return false;
        return mApp.bindListBean.list != null &&
                mApp.bindListBean.list.size() > 0;
    }

    private void logout() {
        if (!GolukUtils.isNetworkConnected(getContext())) {
            GolukUtils.showToast(getContext(), getResources().getString(R.string.user_net_unavailable));
            return;
        }
        UserCancelBeanRequest userCancelBeanRequest = new UserCancelBeanRequest(IPageNotifyFn.PageType_SignOut,
                new IRequestResultListener() {
                    @Override
                    public void onLoadComplete(int requestType, Object result) {
                        CancelResult cancelResult = (CancelResult) result;
                        if (cancelResult != null && cancelResult.code == 0) {
                            SharedPrefUtil.saveUserInfo("");
                            SharedPrefUtil.saveUserPwd("");
                            SharedPrefUtil.saveUserToken("");
                            logoutSucess();
                        } else {
                            GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.str_loginout_fail));
                        }
                    }
                });
        userCancelBeanRequest.get(mApp.getMyInfo().uid);
    }

    private void logoutSucess() {
        // 注销成功
        RemoteCameraConnectManager.instance().needUploadImei();
        mApp.isUserLoginSucess = false;
        mApp.loginoutStatus = true;// 注销成功
        mApp.registStatus = 3;// 注册失败
        mApp.autoLoginStatus = 3;
        mApp.loginStatus = 3;
        mApp.userLiveValidity = 2;
        mApp.setImei("");
        mApp.setIccid("");
        EventBus.getDefault().post(new ShowMoreNewEvent(false));
        resetLoginState();
    }

    private View mTitleView;

    public void showMenu() {
        if (mTitleView == null) {
            mTitleView = LayoutInflater.from(getContext()).inflate(R.layout.home_title_more, null);
        }
        ((BaseActivity) getContext()).getActionBar().setCustomView(mTitleView);
    }

}
