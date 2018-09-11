package com.goluk.a6.internation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goluk.a6.common.event.ImeiUpdateEvent;
import com.goluk.a6.common.event.ShowMoreNewEvent;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.ChangeMailActivity;
import com.goluk.a6.control.ChangePhoneActivity;
import com.goluk.a6.control.ChangePwdActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.util.PermissionUtils;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.bean.CancelResult;
import com.goluk.a6.internation.bean.UserInfo;
import com.goluk.a6.internation.login.UserCancelBeanRequest;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.view.View.GONE;


/**
 * 个人资料
 *
 * @author mobnote
 */
public class UserPersonalInfoActivity extends BaseActivity implements OnClickListener, IRequestResultListener,
        EasyPermissions.PermissionCallbacks{

    /**
     * context
     **/
    private Context mContext = null;
    /**
     * title
     **/
    private ImageButton backBtn = null;

    private TextView rightBtn = null;
    private TextView mTextCenter = null;
    /**
     * 头像
     **/
    private ImageView mImageHead = null;
    private TextView mTextName = null;
    /**
     * 个性签名
     **/
    private TextView mTextSign = null;
    private CustomLoadingDialog mCustomProgressDialog = null;
    /**
     * xinxi
     **/
    private String head = null;
    private String name = null;
    private String sign = null;
    private String sex = null;
    private String customavatar = null;
    private Button mExit;

    /**
     * 头像
     **/
    private RelativeLayout mHeadLayout = null;
    /**
     * 昵称
     **/
    private RelativeLayout mNameLayout = null;
    /**
     * 个性签名
     **/
    private LinearLayout mSignLayout = null;
    // 修改密码
    private RelativeLayout mLayoutChangePwd;

    /**
     * 请求接口格式化数据
     **/
    private String newName = "";
    private String newSign = "";

    public SettingImageView siv = new SettingImageView(UserPersonalInfoActivity.this);

    private static final int REQUEST_CODE_NIKCNAME = 1000;
    private static final int REQUEST_CODE_SIGN = REQUEST_CODE_NIKCNAME + 1;
    private static final int REQUEST_CODE_SYSTEMHEAD = REQUEST_CODE_NIKCNAME + 2;
    private static final int REQUEST_CODE_CHANGE_PHONE = REQUEST_CODE_NIKCNAME + 3;
    private static final int REQUEST_CODE_CHANGE_EMAIL = REQUEST_CODE_NIKCNAME + 4;
    private static final int REQUEST_CODE_PHOTO = 5000;
    private static final int REQUEST_CODE_CAMERA = 6000;
    private static final int REQUEST_CODE_CLIP = 7000;
    private TextView mTextSignDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_personal_info);
        mContext = this;
        // 获得GolukApplication对象
        intiView();
        // 设置title
        mTextCenter.setText(R.string.user_personal_edit_info);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    public void intiView() {
        // title
        backBtn = (ImageButton) findViewById(R.id.back_btn);
        rightBtn = (TextView) findViewById(R.id.user_title_right);
        mTextCenter = (TextView) findViewById(R.id.user_title_text);
        // 头像
        mImageHead = (ImageView) findViewById(R.id.user_personal_info_head);
        mTextName = (TextView) findViewById(R.id.user_personal_info_name);
        // 个性签名
        mTextSign = (TextView) findViewById(R.id.user_personal_info_sign);
        mTextSignDesc = (TextView) findViewById(R.id.user_personal_text);
        mHeadLayout = (RelativeLayout) findViewById(R.id.user_personal_info_head_layout);
        mNameLayout = (RelativeLayout) findViewById(R.id.user_personal_info_name_layout);
        mSignLayout = (LinearLayout) findViewById(R.id.user_personal_info_sign_layout);
        mLayoutChangePwd = (RelativeLayout) findViewById(R.id.layout_change_pwd);
        mExit = (Button) findViewById(R.id.btn_exit);
        mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.com_facebook_loginview_log_out_button));
        // 监听
        backBtn.setOnClickListener(this);
        rightBtn.setVisibility(GONE);
        mHeadLayout.setOnClickListener(this);
        mNameLayout.setOnClickListener(this);
        mSignLayout.setOnClickListener(this);
        mLayoutChangePwd.setOnClickListener(this);
        mExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.back_btn) {
            exit();
        } else if (id == R.id.user_personal_info_head_layout) {
            settingHeadOptions();
        } else if (id == R.id.user_personal_info_name_layout) {
            Intent itName = new Intent(UserPersonalInfoActivity.this, UserPersonalNameActivity.class);
            itName.putExtra("intentNameText", name);
            startActivityForResult(itName, REQUEST_CODE_NIKCNAME);
        } else if (id == R.id.user_personal_info_sign_layout) {
            if (isEmailType()) {
                Intent intentEmail = new Intent(this, ChangeMailActivity.class);
                startActivityForResult(intentEmail, REQUEST_CODE_CHANGE_EMAIL);
            } else {
                Intent intentEmail = new Intent(this, ChangePhoneActivity.class);
                startActivityForResult(intentEmail, REQUEST_CODE_CHANGE_PHONE);
            }
        } else if (id == R.id.btn_exit) {
            if (!GolukUtils.isNetworkConnected(this)) {
                GolukUtils.showToast(this, getResources().getString(R.string.user_net_unavailable));
                return;
            }
            UserCancelBeanRequest userCancelBeanRequest = new UserCancelBeanRequest(IPageNotifyFn.PageType_SignOut, this);
            userCancelBeanRequest.get(mApp.getMyInfo().uid);
            mCustomProgressDialog.show();
        } else if(id == R.id.layout_change_pwd) {
            Intent intent = new Intent(this, ChangePwdActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 打开头像设置菜单选择
     */
    public void settingHeadOptions() {
        final AlertDialog ad = new AlertDialog.Builder(mContext, R.style.CustomDialog).create();
        Window window = ad.getWindow();
        window.setGravity(Gravity.BOTTOM);
        ad.show();
        ad.getWindow().setContentView(R.layout.user_center_setting_head);
        ad.getWindow().findViewById(R.id.camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
                if (PermissionUtils.hasCameraPermission(UserPersonalInfoActivity.this)) {
                    boolean isSucess = siv.getCamera();
                    if (!isSucess) {
                        GolukUtils.showToast(UserPersonalInfoActivity.this,
                                mContext.getResources().getString(R.string.str_start_camera_fail));
                    }
                } else {
                    PermissionUtils.requestCameraPermission(UserPersonalInfoActivity.this);
                }
            }
        });

        ad.getWindow().findViewById(R.id.photo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
                boolean isS = siv.getPhoto();
                if (!isS) {
                    GolukUtils.showToast(UserPersonalInfoActivity.this,
                            mContext.getResources().getString(R.string.str_start_album_fail));
                }
            }
        });

//        ad.getWindow().findViewById(R.id.system).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ad.dismiss();
//                Intent itHead = new Intent(UserPersonalInfoActivity.this, UserPersonalHeadActivity.class);
//                Bundle bundle = new Bundle();
//
//                bundle.putString("intentHeadText", head);
//                bundle.putString("customavatar", customavatar);
//                itHead.putExtras(bundle);
//                startActivityForResult(itHead, REQUEST_CODE_SYSTEMHEAD);
//            }
//        });

        ad.getWindow().findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

    }

    /**
     * 初始化用户信息
     */
    public void initData() {
        UserInfo info = mApp.getMyInfo();
        try {
            if (info == null) {
                return;
            }
            head = info.avatar;
            name = info.name;
            sign = info.description;
            sex = String.valueOf(info.sex);
            customavatar = info.avatar;
            if (customavatar != null && !"".equals(customavatar)) {
                GlideUtils.loadNetHead(this, mImageHead, customavatar, R.drawable.usercenter_head_default);
            } else {
                showHead(mImageHead, head);
            }
            mTextName.setText(name);
            mTextSign.setText(sign);
            if (TextUtils.isEmpty(mApp.getMyInfo().platform)) {
                mSignLayout.setVisibility(GONE);
            } else if ("email".equals(mApp.getMyInfo().platform)) {
                mTextSign.setText(mApp.getMyInfo().email);
                mTextSignDesc.setText(R.string.email_account);
            } else if ("phone".equals(mApp.getMyInfo().platform)) {
                mTextSignDesc.setText(R.string.phone_account);
                mTextSign.setText(mApp.getMyInfo().phone);
            }

            if (isThirdAccount())
                mLayoutChangePwd.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否是三方账号登录
     */
    private boolean isThirdAccount() {
        return !TextUtils.equals("email", mApp.getMyInfo().platform)
                && !TextUtils.equals("phone", mApp.getMyInfo().platform);
    }

    private boolean isEmailType() {
        return TextUtils.equals("email", mApp.getMyInfo().platform);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 分别获取从修改昵称、修改个性签名和修改头像页面的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 5000:
                if (requestCode == siv.CANCELED_CODE) {
                    return;
                }
                Uri imageUri = data.getData();
                Intent intent = new Intent(this, ImageClipActivity.class);
                intent.putExtra("imageuri", imageUri.toString());
                this.startActivityForResult(intent, 7000);
                // iv_head.setImageURI(imageUri);
                break;
            case 6000:
                if (requestCode == siv.CANCELED_CODE) {
                    siv.deleteUri();
                }
                Intent it = new Intent(this, ImageClipActivity.class);
                if (siv.mCameraUri == null) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Bitmap photo = (Bitmap) bundle.get("data"); // get bitmap
                            if (photo == null) {
                            }
                            it.putExtra("imagebitmap", photo);
                        }
                    }
                } else {
                    it.putExtra("imageuri", siv.mCameraUri.toString());
                }

                this.startActivityForResult(it, 7000);
                // iv_head.setImageURI(mCameraUri);
                break;
            case 7000:
                Bundle b = data.getExtras();
                String imagepach = b.getString("imagepath");
                customavatar = imagepach;
                GlideUtils.loadNetHead(this, mImageHead, customavatar, R.drawable.usercenter_head_default);
                siv.deleteUri();
                break;
            case REQUEST_CODE_NIKCNAME:
                Bundle bundle = data.getExtras();
                name = bundle.getString("itName");
                mTextName.setText(name);
                break;
            // 修改个性签名
            case REQUEST_CODE_SIGN:
                Bundle bundle2 = data.getExtras();
                sign = bundle2.getString("itSign");
                mTextSign.setText(sign);
                break;
            // 修改头像
            case REQUEST_CODE_SYSTEMHEAD:
                Bundle bundle3 = data.getExtras();
                head = bundle3.getString("intentSevenHead");
                customavatar = "";
                showHead(mImageHead, head);

                if (head.equals("1") || head.equals("2") || head.equals("3")) {
                    sex = "1";
                } else if (head.equals("4") || head.equals("5") || head.equals("6")) {
                    sex = "2";
                } else if (head.equals("7")) {
                    sex = "0";
                }
                break;
            case REQUEST_CODE_CHANGE_EMAIL:
                String email = data.getStringExtra("email");
                mTextSign.setText(mApp.getMyInfo().email);
                break;
            case REQUEST_CODE_CHANGE_PHONE:
                String phone = data.getStringExtra("phone");
                mTextSign.setText(mApp.getMyInfo().phone);
                break;
            default:
                break;
        }

    }

    private void showHead(ImageView view, String headportrait) {
        GlideUtils.loadLocalHead(this, view, R.drawable.usercenter_head_default);
    }

    private void exit() {
        this.finish();
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        mCustomProgressDialog.close();
        CancelResult cancelResult = (CancelResult) result;
        if (cancelResult != null && cancelResult.code == 0) {
            SharedPrefUtil.saveUserInfo("");
            SharedPrefUtil.saveUserPwd("");
            SharedPrefUtil.saveUserToken("");
            logoutSucess();
        } else {
            GolukUtils.showToast(this, this.getResources().getString(R.string.str_loginout_fail));
        }
    }


    private void logoutSucess() {
        // 注销成功
        mApp.isUserLoginSucess = false;
        mApp.loginoutStatus = true;// 注销成功
        mApp.registStatus = 3;// 注册失败
        mApp.autoLoginStatus = 3;
        mApp.loginStatus = 3;
        mApp.setImei("");
        mApp.setIccid("");
        mApp.setUserInfo(null);
        RemoteCameraConnectManager.instance().needUploadImei();
        GolukUtils.showToast(mContext, this.getResources().getString(R.string.str_loginout_success));
        EventBus.getDefault().post(new ShowMoreNewEvent(false));
        EventBus.getDefault().post(new ImeiUpdateEvent());
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (PermissionUtils.hasCameraPermission(this)) {
            siv.getCamera();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // TODO
    }

}
