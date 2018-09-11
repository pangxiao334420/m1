package com.goluk.a6.internation.login;

import android.content.res.Resources;

import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;

import java.util.ArrayList;

public class CountryCodeMobUtils {

    public static final int[] countryArray = {R.array.smssdk_country_group_a, R.array.smssdk_country_group_b,
            R.array.smssdk_country_group_c, R.array.smssdk_country_group_d, R.array.smssdk_country_group_e,
            R.array.smssdk_country_group_f, R.array.smssdk_country_group_g, R.array.smssdk_country_group_h,
            R.array.smssdk_country_group_i, R.array.smssdk_country_group_j, R.array.smssdk_country_group_k,
            R.array.smssdk_country_group_l, R.array.smssdk_country_group_m, R.array.smssdk_country_group_n,
            R.array.smssdk_country_group_o, R.array.smssdk_country_group_p, R.array.smssdk_country_group_q,
            R.array.smssdk_country_group_r, R.array.smssdk_country_group_s, R.array.smssdk_country_group_t,
            R.array.smssdk_country_group_u, R.array.smssdk_country_group_v, R.array.smssdk_country_group_w,
            R.array.smssdk_country_group_x, R.array.smssdk_country_group_y, R.array.smssdk_country_group_z,};

    public static ArrayList<CountryBean> getCountryList() {
        final ArrayList<CountryBean> list = new ArrayList<CountryBean>();
        final Resources resources = CarControlApplication.getInstance().getResources();
        final int length = countryArray.length;
        for (int i = 0; i < length; i++) {
            try {
                String[] itemArray = resources.getStringArray(countryArray[i]);
                if (null != itemArray && itemArray.length > 0) {
                    final int itemLength = itemArray.length;
                    for (int j = 0; j < itemLength; j++) {
                        CountryBean bean = getCountryBean(itemArray[j]);
                        if (null != bean) {
                            list.add(bean);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return list;
    }

    private static CountryBean getCountryBean(String str) {
        try {
            int nameIndex = str.indexOf(",");
            String name = str.substring(0, nameIndex);
            int areaIndex = str.indexOf(",", nameIndex + 1);
            String area = str.substring(nameIndex + 1, areaIndex);
            int codeIndex = str.indexOf(",", areaIndex + 1);
            String code = str.substring(areaIndex + 1, codeIndex);
            CountryBean bean = new CountryBean();
            bean.name = name;
            bean.code = code;
            bean.area = area;

            return bean;
        } catch (Exception e) {

        }
        return null;
    }

}
