/**
 * Copyright(C) 2015 LightInTheBox All rights reserved.
 *
 * Original Author: zengpeiyu@lightinthebox.com, 2015/3/13
 */
package com.goluk.a6.http;

/**
 * Request callback listener
 */
public interface IRequestResultListener {
    public void onLoadComplete(int requestType, Object result);
}
