package com.goluk.a6.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UpNameData {
	/**	结果代码  **/
	@JSONField(name="result")
	public String result;
	
	/**用户昵称 **/
	@JSONField(name="nickname")
	public String nickname;
	
}
