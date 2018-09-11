package com.goluk.a6.control.flux.widget;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;

/**
 * Created by goluk_lium on 2017/11/7.
 */

public class DayAxisValueFormatter implements IAxisValueFormatter {

    private final static String TODAY = "今天";

    private LineChart chart;
    private int todayOfMonth;
    private int month,lastMonth;
    private int year;
    private static final int START_DAY = 1;
    public DayAxisValueFormatter(LineChart chart) {
        this.chart = chart;
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        todayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        if (month>0) lastMonth = month-1;else lastMonth = 11;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int index = (int) value;

        int result = index;
//        int lastMonthAvailableDays = getDaysForMonth(lastMonth,year)-START_DAY+1;
//        if (todayOfMonth>=START_DAY){
//            result = todayOfMonth;
//        }else {
//            if (index>lastMonthAvailableDays) result = index -lastMonthAvailableDays;
//            else result = START_DAY+index-1;
//        }
        return result==todayOfMonth?TODAY:(result+"");
    }


    private int getDaysForMonth(int month, int year) {
        // month is 0-based
        // 31天 1，3，5，7，8，10，12
        //=> 0,2,4,6,7,9,11
        //30天 4，6，9，11
        //=> 3,5,8,10
        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
            //闰月：能被400整除，或者不能被100整除且可以被4整除
            //      is29Feb = year%400==0||(year%100!=0&&year%4==0);
            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }
}
