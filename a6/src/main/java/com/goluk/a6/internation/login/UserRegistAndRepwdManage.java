package com.goluk.a6.internation.login;

import android.text.TextUtils;

import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.IPageNotifyFn;
import com.goluk.a6.internation.JsonUtil;
import com.goluk.a6.internation.UserRegistAndRepwdInterface;
import com.goluk.a6.internation.UserRegistRequest;
import com.goluk.a6.internation.UserRepwdRequest;
import com.goluk.a6.internation.bean.CheckVcodeBean;
import com.goluk.a6.internation.bean.UserRegistBean;
import com.goluk.a6.internation.bean.UserRepwdBean;

import org.json.JSONObject;

public class UserRegistAndRepwdManage implements IRequestResultListener {

    private static final String TAG = "lily";
    private CarControlApplication mApp = null;
    private UserRegistAndRepwdInterface mInterface = null;

    private static final int BIND_PHONE_SUCCESS = 0;
    private static final int BIND_PHONE_PARAM_ERROR = 1;
    private static final int BIND_PHONE_UNKNOWN_EXCEPTION = 2;
    private static final int BIND_PHONE_VCODE_MISMATCH = 3;
    private static final int BIND_PHONE_VCODE_TIMEOUT = 4;

    /**
     * 用于二次验证
     **/
    public String mStep2Code = "";

    public UserRegistAndRepwdManage(CarControlApplication mApp) {
        super();
        this.mApp = mApp;
    }

    public void setUserRegistAndRepwd(UserRegistAndRepwdInterface mInterface) {
        this.mInterface = mInterface;
    }

    public void registAndRepwdStatusChange(int status) {
        mApp.registStatus = status;
        if (null != mInterface) {
            mInterface.registAndRepwdInterface();
        }
    }

    /**
     * 注册/重置密码请求
     *
     * @param phone
     * @param password
     * @param vCode
     * @return
     */
    public boolean registAndRepwd(boolean b, String phone, String password, String vCode) {
        String jsonStr = JsonUtil.registAndRepwdJson(phone, password, vCode);
        // TODO 判断获取验证码的次数，判断输入的验证码格式
        if (b) {
            UserRegistRequest urr = new UserRegistRequest(IPageNotifyFn.PageType_Register, this);
            urr.get(phone, password, vCode, "");
            return true;
//			return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
//					IPageNotifyFn.PageType_Register, jsonStr);
        } else {
            UserRepwdRequest urr = new UserRepwdRequest(IPageNotifyFn.PageType_ModifyPwd, this);
            urr.get(phone, password, vCode, "");
            return true;
//			return mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
//					IPageNotifyFn.PageType_ModifyPwd, jsonStr);
        }
    }

    /**
     * 注册/重置密码请求
     *
     * @param phone
     * @param password
     * @param vCode
     * @return
     */
    public boolean registAndRepwd(boolean b, String phone, String password, String vCode, String zone) {
        String jsonStr = JsonUtil.registAndRepwdJson(phone, password, vCode);
        // TODO 判断获取验证码的次数，判断输入的验证码格式
        if (b) {
//			UserRegistRequest urr = new UserRegistRequest(IPageNotifyFn.PageType_Register,this);
//			urr.get(phone,password,vCode,zone);
//			return true;
            //检查验证码
            InternationCheckVcodeRequest checkVcode = new InternationCheckVcodeRequest(IPageNotifyFn.PageType_InternationalCheckvcode, this);
            return checkVcode.get(phone, vCode, zone);
        } else {
            UserRepwdRequest urr = new UserRepwdRequest(IPageNotifyFn.PageType_ModifyPwd, this);
            urr.get(phone, password, vCode, zone);
            return true;
        }
    }


    public void bindPhoneNumCallback(int success, Object outTime, Object obj) {
        int codeOut = (Integer) outTime;
        if (1 == success) {
            try {
                String result = (String) obj;
                JSONObject json = new JSONObject(result);
                JSONObject data = json.optJSONObject("data");

                if (data != null) {
                    String status = data.optString("result");
                    if (TextUtils.isDigitsOnly(status)) {
                        int code = Integer.valueOf(status);
                        switch (code) {
                            case 0:
                                registAndRepwdStatusChange(2);
                                break;
                            case 1:
                            case 2:
                                registAndRepwdStatusChange(3);
                                break;
                            case 3:
                                registAndRepwdStatusChange(6);
                                break;
                            case 4:
                                registAndRepwdStatusChange(7);
                                break;
                            default:
                                registAndRepwdStatusChange(9);
                                break;

                        }
                    }
                } else {
                    registAndRepwdStatusChange(3);
                }
            } catch (Exception e) {
                registAndRepwdStatusChange(3);
                e.printStackTrace();
            }
        } else {
            switch (codeOut) {
                case 1:
                case 2:
                case 3:
                default:
                    registAndRepwdStatusChange(9);
                    break;
            }
        }
    }

    /**
     * 注册/重置密码请求回调
     *
     * @param success
     * @param outTime
     * @param obj
     */
    public void registAndRepwdCallback(int success, Object outTime, Object obj) {
        int codeOut = (Integer) outTime;
        if (1 == success) {
            try {
                String data = (String) obj;
                JSONObject json = new JSONObject(data);
                int code = json.getInt("code");
                switch (code) {
                    case 200:
                        registAndRepwdStatusChange(2);
                        break;
                    case 500:
                        registAndRepwdStatusChange(4);
                        break;
                    case 405:
                        registAndRepwdStatusChange(5);
                        break;
                    case 406:
                        registAndRepwdStatusChange(6);
                        break;
                    case 407:
                        registAndRepwdStatusChange(7);
                        break;
                    case 480:
                        registAndRepwdStatusChange(8);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 网络超时当重试按照3、6、9、10s的重试机制，当网络链接超时时
            switch (codeOut) {
                case 1:
                case 2:
                case 3:
                default:
                    registAndRepwdStatusChange(9);
                    break;
            }
        }
    }


    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_BindInfo) {

        } else if (requestType == IPageNotifyFn.PageType_ModifyPwd) {
            UserRepwdBean urr = (UserRepwdBean) result;
            if (urr == null) {
                registAndRepwdStatusChange(3);
                return;
            }
            int code = Integer.parseInt(urr.code);
            try {
                switch (code) {
                    case 0:
                        registAndRepwdStatusChange(2);
                        break;
                    case 20010:
                        registAndRepwdStatusChange(4);
                        break;
                    case 20001:
                        registAndRepwdStatusChange(5);
                        break;
//						case 406:
//							registAndRepwdStatusChange(6);
//							break;
                    case 20011:
                    case 20012:
                        registAndRepwdStatusChange(7);
                        break;
//						case 480:
//							registAndRepwdStatusChange(8);
//							break;
                    default:
                        registAndRepwdStatusChange(3);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                registAndRepwdStatusChange(3);
            }
        } else if (requestType == IPageNotifyFn.PageType_Register) {
            UserRegistBean urr = (UserRegistBean) result;
            if (urr == null) {
                return;
            }
            int code = Integer.parseInt(urr.code);
            try {
                switch (code) {
                    case 200:
                        registAndRepwdStatusChange(2);
                        break;
                    case 500:
                        registAndRepwdStatusChange(4);
                        break;
                    case 405:
                        registAndRepwdStatusChange(5);
                        break;
                    case 406:
                        registAndRepwdStatusChange(6);
                        break;
                    case 407:
                        registAndRepwdStatusChange(7);
                        break;
                    case 480:
                        registAndRepwdStatusChange(8);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestType == IPageNotifyFn.PageType_InternationalCheckvcode) {
            CheckVcodeBean bean = (CheckVcodeBean) result;
            if (null == bean) {
                registAndRepwdStatusChange(9);
                return;
            }
            int code = bean.code;
            if (code == 0) {
                if (null != bean.data) {
                    mStep2Code = bean.data.step2code;
                }
                registAndRepwdStatusChange(2);
            } else if (code == 20010) {//错误
                registAndRepwdStatusChange(6);
            } else if (code == 21001) {//超时
                registAndRepwdStatusChange(7);
            } else if (code == 12010) {//超出限制
                registAndRepwdStatusChange(10);
            } else {
                registAndRepwdStatusChange(9);
            }
        }
    }
}
