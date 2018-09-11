package com.goluk.a6.control.util;

import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;

import java.util.List;
import java.util.Locale;

/**
 * 坐标转换为地址
 */
public class AddressConvert {

    private Handler mUiHandler;

    public AddressConvert() {
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public void convert(final double lat, final double lon, final AddressConvertCallback callback) {
        new Thread() {
            @Override
            public void run() {
                final String address = getAddress(lat, lon);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onAddressConverted(address);
                    }
                });
            }
        }.start();
    }

    public static String getAddress(double LATITUDE, double LONGITUDE) {
        String strAdd = CarControlApplication.getInstance().getString(R.string.unknown_address);
        Geocoder geocoder = new Geocoder(CarControlApplication.getInstance().getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            StringBuilder strReturnedAddress = new StringBuilder("");
            Address returnedAddress = null;
            if (addresses != null) {
                returnedAddress = addresses.get(0);
                if (returnedAddress.getMaxAddressLineIndex() > 0) {
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i));
                        break;
                    }
                } else {
                    strReturnedAddress.append(returnedAddress.getAddressLine(0));
                }
            }
            strAdd = strReturnedAddress.toString();

            strAdd = removeCountryAndPostcode(strAdd, returnedAddress);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(strAdd)) {
            strAdd = CarControlApplication.getInstance().getString(R.string.unknown_address);
        }
        return strAdd;
    }

    /**
     * 移除国家名称和邮政编码
     */
    private static String removeCountryAndPostcode(String addressStr, Address address) {
        if (address == null)
            return addressStr;
        String country = address.getCountryName();
        String postCode = address.getPostalCode();
        if (!TextUtils.isEmpty(country))
            addressStr = addressStr.replace(country, "");
        if (!TextUtils.isEmpty(postCode))
            addressStr = addressStr.replace(postCode, "");
        addressStr = addressStr.replace("邮政编码:", "");
        addressStr = addressStr.trim();
        addressStr = addressStr.replace(", ,", "");

        return addressStr;

    }

    public interface AddressConvertCallback {
        void onAddressConverted(String address);
    }

}
