package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;

/**
 * 报案信息详情弹出框页面
 */
public class ReportInfoActivity extends Activity implements View.OnClickListener {

    private TextView tv_cancel; // 取消
    private TextView tv_ok; // 确定
    private EditText geographicalPosition; // 出险地点
    private ImageView startPositioning; // 开始定位
    private EditText et_plateNumber; // 车牌号
    private EditText et_label_models; // 厂牌车型
    private EditText et_other_plate_number; // 对方车牌
    private EditText et_other_label_models; // 对方车厂牌车型
    private EditText et_other_name; // 对方姓名
    private EditText et_other_phone_number; // 对方手机号
    private LinearLayout ll_other_info; // 对方信息

    private GpsStatusReceiver gpsStatuesReceiver;
    private boolean currentGPSState;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private String position;
    private String plateNumber;
    private String label_models;
    private String other_plate_number;
    private String other_label_models;
    private String other_name;
    private String other_phone_number;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setViewId());
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findView();
        init();
        initEvents();
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销获取GPS状态广播
        unRegisterReceiver(this);
        mLocationClient.onDestroy();
    }

    protected int setViewId() {
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        return R.layout.activity_report_info;
    }

    protected void findView() {
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        geographicalPosition = (EditText) findViewById(R.id.geographicalPosition);
        startPositioning = (ImageView) findViewById(R.id.startPositioning);
        et_plateNumber = (EditText) findViewById(R.id.et_plateNumber);
        et_label_models = (EditText) findViewById(R.id.et_label_models);
        et_other_plate_number = (EditText) findViewById(R.id.et_other_plate_number);
        et_other_label_models = (EditText) findViewById(R.id.et_other_label_models);
        et_other_name = (EditText) findViewById(R.id.et_other_name);
        et_other_phone_number = (EditText) findViewById(R.id.et_other_phone_number);

        ll_other_info = (LinearLayout) findViewById(R.id.ll_other_info);
    }

    protected void init() {
        // 初始化数据
        position = AppCaseData.geographicalPosition;
        plateNumber = AppCaseData.plateNumber;
        label_models = AppCaseData.label_models;
        other_plate_number = AppCaseData.other_plate_number;
        other_label_models = AppCaseData.other_label_models;
        other_name = AppCaseData.other_name;
        other_phone_number = AppCaseData.other_phone_number;

        geographicalPosition.setText(Utils.parseStr(position));
        et_plateNumber.setText(Utils.parseStr(plateNumber));
        et_label_models.setText(Utils.parseStr(label_models));
        et_other_plate_number.setText(Utils.parseStr(other_plate_number));
        et_other_label_models.setText(Utils.parseStr(other_label_models));
        et_other_name.setText(Utils.parseStr(other_name));
        et_other_phone_number.setText(Utils.parseStr(other_phone_number));

        //注册获取gps状态广播
        gpsStatuesReceiver = new GpsStatusReceiver();
        readyRegisterReceiver(this);

        // 获取当前GPS状态
        currentGPSState = getGPSState(getApplicationContext());
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //初始化自定义监听
        mLocationListener = new MyLocationListener();
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);
        if (null != mLocationClient) {
            mLocationClient.setLocationOption(mLocationOption);
            if (currentGPSState) {//如果当前GPS打开则高精度定位
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            } else {
                //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            }
        }
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);

        if (!AppCaseData.caseIsEdit) {
            geographicalPosition.setEnabled(false);
            et_plateNumber.setEnabled(false);
            et_label_models.setEnabled(false);
            et_other_plate_number.setEnabled(false);
            et_other_label_models.setEnabled(false);
            et_other_name.setEnabled(false);
            et_other_phone_number.setEnabled(false);
            startPositioning.setVisibility(View.GONE);

            geographicalPosition.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
            et_plateNumber.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
            et_label_models.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
            et_other_plate_number.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
            et_other_label_models.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
            et_other_name.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
            et_other_phone_number.setTextColor(ContextCompat.getColor(ReportInfoActivity.this,R.color.bottom_text));
        } else {
            geographicalPosition.setEnabled(true);
            et_plateNumber.setEnabled(true);
            et_label_models.setEnabled(true);
            et_other_plate_number.setEnabled(true);
            et_other_label_models.setEnabled(true);
            et_other_name.setEnabled(true);
            et_other_phone_number.setEnabled(true);
            startPositioning.setVisibility(View.VISIBLE);
            // 如果定位地址为空，则自动定位，否则，手动定位
            if ("".equals(AppCaseData.geographicalPosition) || null==AppCaseData.geographicalPosition) {
                //启动定位 手动点击定位
                mLocationClient.startLocation();
            } else {
                geographicalPosition.setText(AppCaseData.geographicalPosition);
            }
        }
        if (AppCaseData.carCount == 1) {
            ll_other_info.setVisibility(View.INVISIBLE);
        } else {
            ll_other_info.setVisibility(View.VISIBLE);
        }
    }

    protected void initEvents() {
        tv_cancel.setOnClickListener(this);
        tv_ok.setOnClickListener(this);

        //点击定位图标时的响应
        startPositioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentGPSState) {//如果当前GPS打开则高精度定位
                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                } else {
                    //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                }
                mLocationClient.setLocationOption(mLocationOption);
                //开始定位
                mLocationClient.startLocation();
            }
        });
    }

    protected void loadData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_ok:

                AppCaseData.plateNumber = et_plateNumber.getText().toString();
                AppCaseData.geographicalPosition = geographicalPosition.getText().toString();
                AppCaseData.plateNumber = et_plateNumber.getText().toString();
                AppCaseData.label_models = et_label_models.getText().toString();
                AppCaseData.other_plate_number = et_other_plate_number.getText().toString();
                AppCaseData.other_label_models = et_other_label_models.getText().toString();
                AppCaseData.other_name = et_other_name.getText().toString();
                AppCaseData.other_phone_number = et_other_phone_number.getText().toString();

                finish();
                break;
        }
    }

    /**
     * 高德地图的监听
     */
    public class MyLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    String addr = aMapLocation.getAddress();//获取详细地址信息
                    if (addr == null) {
                        ToastUtils.showNomalShortToast(ReportInfoActivity.this, getString(R.string.locationFail));
                    } else if ("".equals(addr)) {
                        ToastUtils.showNomalShortToast(ReportInfoActivity.this, "请先开启您的位置服务权限");
                    } else {
                        geographicalPosition.setText(addr);
                    }
                } else {
                    ToastUtils.showNomalShortToast(ReportInfoActivity.this, getString(R.string.locationFail));
                }
            }
            mLocationClient.stopLocation();
        }
    }

    /**
     * 获取ＧＰＳ当前状态
     *
     * @param context
     * @return
     */
    private boolean getGPSState(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean on = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return on;
    }

    /**
     * 注册监听GPS状态广播
     *
     * @param context
     */
    public void readyRegisterReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        context.registerReceiver(gpsStatuesReceiver, filter);
    }

    /**
     * 注销监听GPS广播
     *
     * @param context
     */
    public void unRegisterReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        context.unregisterReceiver(gpsStatuesReceiver);
    }

    /**
     * 监听GPS 状态变化广播
     */
    private class GpsStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                currentGPSState = getGPSState(context);
                if (currentGPSState) {//如果当前GPS打开则高精度定位
                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                } else {
                    //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
                    mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                }
                mLocationClient.setLocationOption(mLocationOption);
            }
        }
    }
}
