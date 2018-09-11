package com.goluk.a6.http.request;

import java.util.List;

/**
 * Created by goluk_lium on 2017/11/8.
 */

public class GprsPlanBean {

    public String dateInActive;

    public String dateActive;

    public String trafficMonth;

    public int dateAvailable;

    public List<String> trafficType ;

    public double packUsed;

    public String monthValid;

    public String pack;

    public String trafficTotal;

    public String status;

    public List<Addition> addition ;

    public static class  Addition{

        public String flow_month;

        public String act_month;

        public String priority;

        public String flow_total;
    }

}
