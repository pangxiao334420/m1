package com.goluk.a6.control.dvr.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by goluk_lium on 2018/1/30.
 */

public class ApnBean implements Parcelable {
    public String name;
    public String apn;
    public String mcc;
    public String mnc;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.apn);
        dest.writeString(this.mcc);
        dest.writeString(this.mnc);
    }

    public ApnBean() {
    }

    protected ApnBean(Parcel in) {
        this.name = in.readString();
        this.apn = in.readString();
        this.mcc = in.readString();
        this.mnc = in.readString();
    }

    public static final Parcelable.Creator<ApnBean> CREATOR = new Parcelable.Creator<ApnBean>() {
        @Override
        public ApnBean createFromParcel(Parcel source) {
            return new ApnBean(source);
        }

        @Override
        public ApnBean[] newArray(int size) {
            return new ApnBean[size];
        }
    };
}
