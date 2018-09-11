package com.goluk.a6.http.responsebean;

/**
 * Created by goluk_lium on 2017/11/2.
 */

public class FluxCurrentStatusResponse {
    public final static int SUCCESS = 1;
    public final static int FAILURE = 0;
    public final static String FIELD_STATUS = "status";
    public final static String FIELD_INFO = "info";
    public int status;
    public CurrentFluxInfo info;

    public static class CurrentFluxInfo{
        public String pack;//套餐类型和流量
        public String dateActive;//套餐开始日期（年.月.日）
        public String dateInActive;//套餐结束日期（年.月.日）
        public String trafficMonth;//当月已用流量（M）
        public String monthValid;//当月可用流量（M）
        public String trafficTotal;//当前套餐总流量（M）
        public String trafficType;//流量卡类型
        public String dateAvailable;//当前套餐距离结束日期剩余天数
        public int status;//当前卡状态（1已激活，0未开通，-1已注销）
        public String addition;//未启用套餐

        @Override
        public String toString() {
            return "CurrentFluxInfo{" +
                    "pack='" + pack + '\'' +
                    ", dateActive='" + dateActive + '\'' +
                    ", dateInActive='" + dateInActive + '\'' +
                    ", trafficMonth='" + trafficMonth + '\'' +
                    ", monthValid='" + monthValid + '\'' +
                    ", trafficTotal='" + trafficTotal + '\'' +
                    ", trafficType='" + trafficType + '\'' +
                    ", dateAvailable='" + dateAvailable + '\'' +
                    ", status=" + status +
                    ", addition='" + addition + '\'' +
                    '}';
        }
    }

}
