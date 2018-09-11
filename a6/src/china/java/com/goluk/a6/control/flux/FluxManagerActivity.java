package com.goluk.a6.control.flux;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;
import com.goluk.a6.control.flux.widget.DayAxisValueFormatter;
import com.goluk.a6.control.flux.widget.FluxLineChart;
import com.goluk.a6.control.flux.widget.WaterWaveView;
import com.goluk.a6.http.request.FluxCurrentStatusRequest;
import com.goluk.a6.http.request.GprsMonthInfoBean;
import com.goluk.a6.http.request.GprsPlanBean;
import com.goluk.a6.http.request.WXPayEntity;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by goluk_lium on 2017/11/2.
 */

public class FluxManagerActivity extends BaseActivity
        implements FluxCurrentStatusRequest.FluxRequestListener, View.OnClickListener {
    public static final String WEB_PAGE_URL = "web_page_url";
    public static final String WEB_PAGE_TITLE = "web_page_title";
    private CarControlApplication mApp;
    private WaterWaveView mWaterView;
    private FluxLineChart mChartFlux;
    private TextView textDateAvailable;
    private TextView gprsDetail,fluxCharge,chargeRecord;
    private TextView textPhoneNumber;
    private ImageButton iBack,iBackCard;
    private Intent toH5Intent;
    private String webUrl;
    private int titleStringRes;
    private boolean isFeiMaoSimCard = true;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flux_manager);
        mApp = CarControlApplication.getInstance();
        iccid = mApp.defaultDeviceIccid;
        code = getIntent().getStringExtra("simCode");
        isFeiMaoSimCard = getIntent().getBooleanExtra("simCardType",true);
        rubbishRequestEnginer = new FluxCurrentStatusRequest(this);
        toH5Intent = new Intent(this,FluxChargeH5Activity.class);
        initView();
        if (isFeiMaoSimCard) requestData();
    }

    private RelativeLayout mLayoutOwnFlux,mLayoutOtherFlux;
    private void initView(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在打开...");
        mLayoutOwnFlux = (RelativeLayout) findViewById(R.id.layout_flux_manager);
        mLayoutOtherFlux = (RelativeLayout) findViewById(R.id.layout_flux_card);
        mWaterView = (WaterWaveView) findViewById(R.id.water_wave);
        mChartFlux = (FluxLineChart) findViewById(R.id.chart_flux);
        textDateAvailable = (TextView)findViewById(R.id.textDateAvailable);
        gprsDetail = (TextView) findViewById(R.id.gprs_detail);
        fluxCharge = (TextView) findViewById(R.id.flux_charge);
        chargeRecord = (TextView) findViewById(R.id.charge_recorde);
        iBack = (ImageButton) findViewById(R.id.ib_back);
        iBackCard = (ImageButton) findViewById(R.id.ib_back_card);
        String format = getString(R.string.help_phone);
        textPhoneNumber = (TextView) mLayoutOtherFlux.findViewById(R.id.text_phone);
        final String number = "400-969-1800";
        textPhoneNumber.setText(String.format(format,number));
        textPhoneNumber.setOnClickListener(this);
        gprsDetail.setOnClickListener(this);
        fluxCharge.setOnClickListener(this);
        chargeRecord.setOnClickListener(this);
        iBack.setOnClickListener(this);
        iBackCard.setOnClickListener(this);
        mChartFlux.setClickable(false);
        initXAxisValueFormat();
        if (isFeiMaoSimCard){
            mLayoutOtherFlux.setVisibility(View.GONE);
            mLayoutOwnFlux.setVisibility(View.VISIBLE);
        }else {
            mLayoutOtherFlux.setVisibility(View.VISIBLE);
            mLayoutOwnFlux.setVisibility(View.GONE);
        }

    }

    private void showOnSuccess(GprsPlanBean planBean){
        float remainderMonth = Float.parseFloat(planBean.monthValid)
                - Float.parseFloat(planBean.trafficMonth);
        if (remainderMonth<0) remainderMonth = 0;
        mWaterView.setmValues(Float.parseFloat(planBean.trafficTotal),
                                remainderMonth);
        textDateAvailable.setText(String.format(getResources().getString(R.string.remaind_days),planBean.dateAvailable));
    }
    private void initXAxisValueFormat(){
        IAxisValueFormatter dayAxisValueFormatter = new DayAxisValueFormatter(mChartFlux);
        XAxis xAxis = mChartFlux.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(dayAxisValueFormatter);
    }

    private void showCurrentMonthGprsChart(List<GprsMonthInfoBean> gprsList){
        addFluxChartData(gprsList);
        mChartFlux.setLastValueHighlight();
    }

    private void addFluxChartData(List<GprsMonthInfoBean> gprsMonthInfoBeanList) {
        LineDataSet dataSet = new LineDataSet(handleMonthlyGprsData(gprsMonthInfoBeanList), "");
        dataSet.setDrawIcons(false);
        dataSet.setColor(getResources().getColor(R.color.flux_chart_gray));
        dataSet.setCircleColor(getResources().getColor(R.color.flux_chart_gray));
        dataSet.setLineWidth(1.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(false);
        dataSet.setDrawValues(false);
        dataSet.setHighlightLineWidth(1);
        dataSet.setDrawVerticalHighlightIndicator(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSet);
        LineData data = new LineData(dataSets);
        mChartFlux.setData(data);
        mChartFlux.setVisibleXRangeMaximum(9);
        mChartFlux.setScaleEnabled(false);
        mChartFlux.moveViewToX(Float.MAX_VALUE);
    }
    private ArrayList<Entry> handleMonthlyGprsData(List<GprsMonthInfoBean> gprsMonthInfoBeanList){
        ArrayList<Entry> values = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String dateTmp ;
        float flow ;
        //取出月信息中的date,放在list中，他们有相同的索引值
        List<String> dates = new ArrayList<>();
        for (GprsMonthInfoBean bean:gprsMonthInfoBeanList)dates.add(bean.date);
        int start = 1;
        if (day>=start){
            for (int index = start;index<=day;index++){
                calendar.set(year,month,index);
                dateTmp = format.format(calendar.getTime());
                if (dates.contains(dateTmp)){
                    flow = gprsMonthInfoBeanList.get(dates.indexOf(dateTmp)).flow;
                }else {
                    flow = .0f;
                }
                values.add(new Entry(index,flow));
            }
        }else {
            int lastMonth;
            if (month>0) lastMonth = month-1;else lastMonth = 11;
            for (int index = start;index<=getDaysForMonth(lastMonth,year);index++){
                calendar.set(year,month,index);
                dateTmp = format.format(calendar.getTime());
                if (dates.contains(dateTmp)){
                    flow = gprsMonthInfoBeanList.get(dates.indexOf(dateTmp)).flow;
                }else {
                    flow = .0f;
                }
                values.add(new Entry(index-start+1,flow));
            }
            for (int index = 1;index<=day;index++){
                calendar.set(year,month,index);
                dateTmp = format.format(calendar.getTime());
                if (dates.contains(dateTmp)){
                    flow = gprsMonthInfoBeanList.get(dates.indexOf(dateTmp)).flow;
                }else {
                    flow = .0f;
                }
                values.add(new Entry(index+getDaysForMonth(lastMonth,year)-start+1,flow));
            }
        }
        return values;
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

    @Override
    public void onGprsPlanInfo(GprsPlanBean t) {
        showOnSuccess(t);
    }

    @Override
    public void onGprsMonthInfo(List<GprsMonthInfoBean> gprsMonthInfoBeanList) {
        showCurrentMonthGprsChart(gprsMonthInfoBeanList);
    }

    @Override
    public void onSimCode(String code) {
        mProgressDialog.dismiss();
        toH5Intent.putExtra(WEB_PAGE_TITLE,titleStringRes);
        toH5Intent.putExtra(WEB_PAGE_URL, webUrl);
        startActivity(toH5Intent);
    }

    @Override
    public void onFailure(int errCode) {
        mProgressDialog.dismiss();
        switch (errCode){
            case Config.SERVER_TOKEN_DEVICE_INVALID:
                GolukUtils.showToast(this,this.getResources().getString(R.string.server_token_device_invalid));
                break;
            case Config.SERVER_TOKEN_EXPIRED:
            case Config.SERVER_TOKEN_INVALID:
                GolukUtils.showToast(this,this.getResources().getString(R.string.server_token_expired));
                break;
            case Config.CODE_VOLLEY_NETWORK_ERROR:
                GolukUtils.showToast(this,this.getResources().getString(R.string.network_invalid));
                break;
            default:
                GolukUtils.showToast(this,"未知错误，请稍后再试");
                break;
        }

    }

    @Override
    public void onGenerateOrder(WXPayEntity payEntity) {}

    private String iccid;
    private String code;
    FluxCurrentStatusRequest rubbishRequestEnginer;
    private void requestData(){
        if (!GolukUtils.isNetworkConnected(this)) {
            GolukUtils.showToast(this, getResources().getString(R.string.user_net_unavailable));
            return;
        }
        rubbishRequestEnginer.setIccid(iccid);
        rubbishRequestEnginer.setCode(code);
        getGprsPlanSync(iccid,code);
        getMonthGprsInfoSync(iccid,code);
    }

    /**
     * 作为判断token是否过期，因为该界面不会使用这个接口
     */
    private void getSimCodeSync(String iccid){
        if (!GolukUtils.isNetworkConnected(this)) {
            GolukUtils.showToast(this, getResources().getString(R.string.user_net_unavailable));
            return;
        }
        mProgressDialog.show();
        rubbishRequestEnginer.requestSimCode(iccid);
    }

    private void getGprsPlanSync(String iccid,String code){
        rubbishRequestEnginer.requestGprsPlan(iccid,code);
    }
    private void getMonthGprsInfoSync(String iccid,String code){
        rubbishRequestEnginer.requestMonthGprsInfo(iccid,code);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.gprs_detail:
                webUrl = rubbishRequestEnginer.buildWebUrl(iccid,code,HTML_GPRS_DETAIL_PATH);
                titleStringRes = R.string.flux_package_detail;
                break;
            case R.id.flux_charge:
                webUrl = rubbishRequestEnginer.buildWebUrl(iccid,code,HTML_FLUX_CHARGE_PATH);
                titleStringRes = R.string.package_recharge;
                break;
            case R.id.charge_recorde:
                webUrl = rubbishRequestEnginer.buildWebUrl(iccid,code,HTML_CHARGE_RECORD_PATH);
                titleStringRes = R.string.recharge_records;
                break;
            case R.id.ib_back:
            case R.id.ib_back_card:
                onBackPressed();
                return;
            case R.id.text_phone:
                Intent intent=new Intent("android.intent.action.CALL", Uri.parse("tel:"+textPhoneNumber.getText()));
                startActivity(intent);
                return;
            default:
                break;
        }

        getSimCodeSync(iccid);

    }

    private final static String HTML_GPRS_DETAIL_PATH = "/m1c/user-set-meal.html";
    private final static String HTML_FLUX_CHARGE_PATH = "/m1c/flow-recharge.html";
    private final static String HTML_CHARGE_RECORD_PATH = "/m1c/recharge-record.html";

}
