package com.goluk.a6.api;

import likly.reverse.Reverse;

/**
 * @author Created by likly on 2017/3/24.
 * @version 1.0
 */

public class ApiUtil {

    /* 是否显示了请求等待框 */
    public static boolean isShowLoading;

    public static ApiService apiService() {
        return Reverse.service(ApiService.class);
    }

}
