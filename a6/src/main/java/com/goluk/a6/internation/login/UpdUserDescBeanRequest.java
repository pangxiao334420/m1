package com.goluk.a6.internation.login;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UpDescResult;

import java.util.HashMap;

public class UpdUserDescBeanRequest extends GolukFastjsonRequest<UpDescResult> {

	public UpdUserDescBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UpDescResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/modifyUserInfo.htm";
	}

	@Override
	protected String getMethod() {
		return "modifyDesc";
	}

	public void get(String uid,String phone,String desc) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("commuid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + GolukUtils.getMobileId());
		headers.put("method", "userCancel");
		headers.put("desc", desc);
		headers.put("phone", phone);
		get();
	}
	
}
