package com.goluk.a6.control.util;

import com.goluk.a6.control.R;

/**
 * Created by HuangJW on 2018/8/23 14:34.
 * Mail: 499655607@qq.com
 * Powered by Goluk
 */
public class SignalUtil {

    public static int getSimStrengthWhiteIconByLevel(int level) {
        int[] icons = {R.drawable.ic_sim_0, R.drawable.ic_sim_1, R.drawable.ic_sim_2, R.drawable.ic_sim_3, R.drawable.ic_sim_4};
        if (level >= 0 && level <= 4)
            return icons[level];
        return -1;
    }

    public static int getSimStrengthBlueIconByLevel(int level) {
        int[] icons = {R.drawable.icon_network_00, R.drawable.icon_network_01, R.drawable.icon_network_02, R.drawable.icon_network_03, R.drawable.icon_network_04};
        if (level >= 0 && level <= 4)
            return icons[level];
        return -1;
    }

}
