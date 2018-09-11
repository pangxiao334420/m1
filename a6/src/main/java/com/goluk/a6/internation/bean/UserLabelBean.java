package com.goluk.a6.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class UserLabelBean implements Serializable {

	/** */
	private static final long serialVersionUID = 1L;

	/** 蓝V描述 **/
	@JSONField(name = "approve")
	public String approve;

	/** 蓝V标识 **/
	@JSONField(name = "approvelabel")
	public String approvelabel;

	/** 达人认证 **/
	@JSONField(name = "tarento")
	public String tarento;

	/** 黄v标识 **/
	@JSONField(name = "headplusv")
	public String headplusv;

	/** 黄V描述 **/
	@JSONField(name = "headplusvdes")
	public String headplusvdes;

}
