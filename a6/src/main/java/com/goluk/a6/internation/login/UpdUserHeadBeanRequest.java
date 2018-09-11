package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GlideUtils;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UpHeadResult;

import java.util.HashMap;


public class UpdUserHeadBeanRequest extends GolukFastjsonRequest<UpHeadResult> {

	public UpdUserHeadBeanRequest(int requestType, IRequestResultListener listener) {
		super(requestType, UpHeadResult.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcRegister/modifyUserInfo.htm";
	}

	@Override
	protected String getMethod() {
		return "modifyHead";
	}

	public void get(String uid,String phone,String channel,String urlhead,String head) {
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		headers.put("commuid", uid);
		headers.put("tag", "android");
		headers.put("mid", "" + GolukUtils.getMobileId());
		headers.put("method", "modifyHead");
		headers.put("channel", channel);
		headers.put("urlhead", urlhead);
		headers.put("head", head);
		headers.put("phone", phone);
		get();
	}
	
}
