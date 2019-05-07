package com.example.administrator.lc_dvr.module.lc_report;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Config;
import com.MessageEvent;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.lc_help.ReportSuccess;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * Created by yangboru on 2018/1/24.
 * <p>
 * 在线定损
 */

public class OnlineSurvey2 extends BaseActivity {

    private View report_selector;
    private View reportPhoto_selector;
    private RelativeLayout report;
    private RelativeLayout reportPhoto;

    private FragmentManager v4FragmentManager;
    private HashMap<Object, Object> fragmentMap;
    private BaseFragment lastFragment;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;

    private TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);// 重新创建的时候不保存上次一次的内容

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消动态网络变化广播接收器的注册
        localBroadcastManager.unregisterReceiver(localReceiver);
        if (fragmentMap != null) {
            fragmentMap.clear();
            fragmentMap = null;
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.onlinesurvey2_layout;
    }

    @Override
    protected void findView() {
        report_selector = findViewById(R.id.report_selector);
        reportPhoto_selector = findViewById(R.id.reportPhoto_selector);
        report = (RelativeLayout) findViewById(R.id.report);
        reportPhoto = (RelativeLayout) findViewById(R.id.reportPhoto);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    protected void init() {

        if ("1".equals(PreferenceUtil.getString("isdemo", "0"))) {
            tv_title.setText("模拟报案");
        } else {
            tv_title.setText("在线定损");
        }
        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        //动态注册本地广播接收器
        intentFilter = new IntentFilter();
        intentFilter.addAction("OnlineSurvey2");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        v4FragmentManager = getSupportFragmentManager();
        fragmentMap = new HashMap<>();
        Intent intent = getIntent();

        //如果是点击列表进来这个页面的，就执行下面的代码
        if ("reportPhoto".equals(intent.getStringExtra("ImmediatelyReport"))) {
            reportPhoto.performClick();//默认点中这项，相当于手动点击
        } else {
            report.performClick();//默认点中这项，相当于手动点击
        }

    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void loadData() {

    }

    /**
     * 退出当前的界面
     *
     * @param view
     */
    public void closeOnlineSurvey2(View view) {
        finish();
    }

    /**
     * 点击选择器时的响应  选择显示哪个Fragment
     *
     * @param v
     */
    public void onlineSurveySelector(View v) {
        switch (v.getId()) {
            case R.id.report://此处也可以和id比较的
                report_selector.setVisibility(View.VISIBLE);
                reportPhoto_selector.setVisibility(View.GONE);
                break;
            case R.id.reportPhoto:
                report_selector.setVisibility(View.GONE);
                reportPhoto_selector.setVisibility(View.VISIBLE);
                break;
        }
        BaseFragment show = null;
        FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
        if (fragmentMap.containsKey(v.getId())) {
            if (lastFragment != null) {
                v4Transaction.hide(lastFragment);
            }
            show = (BaseFragment) fragmentMap.get(v.getId());
            lastFragment = show;
            v4Transaction.show(show);
        } else {
            switch (v.getId()) {
                case R.id.report://此处也可以和id比较的
                    show = new OnlineSurvey();//报案
                    break;
                case R.id.reportPhoto:
                    show = new ReportPhoto();//拍照
                    break;
            }
            v4Transaction.add(R.id.onlineSurvey2_content, show);
            fragmentMap.put(v.getId(), show);
            if (lastFragment != null) {
                v4Transaction.hide(lastFragment);
            }
            lastFragment = show;
        }
        v4Transaction.commitAllowingStateLoss();
    }

    /**
     * ReportPhoto页面拍照结束，跳转过来
     */
    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent bubbleIntent = new Intent(OnlineSurvey2.this, ReportSuccess.class);
            startActivity(bubbleIntent);
            Utils.doCallBackMethod();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
