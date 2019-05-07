package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.AppCaseData;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/12
 *   desc   :
 *  version :
 * </pre>
 */
public class ReportSuccess extends BaseActivity implements View.OnClickListener {

    private Button btn_look_case; // 查看案件
    private Button btn_return_main;// 返回首页
    private TextView report_sucess_hint; // 报案成功提示
    private TextView tv_payment_tip; // 理赔小提示


    private boolean isDemo; // 是否模拟报案
    private boolean isPayment; // 是否理赔上传

    @Override
    protected int setViewId() {
        return R.layout.report_success_activity;
    }

    @Override
    protected void findView() {
        btn_look_case = (Button) findViewById(R.id.btn_look_case);
        btn_return_main = (Button) findViewById(R.id.btn_return_main);
        report_sucess_hint = (TextView) findViewById(R.id.report_sucess_hint);
        tv_payment_tip = (TextView) findViewById(R.id.tv_payment_tip);

    }

    @Override
    protected void init() {
        Intent intent = getIntent();
        isPayment = intent.getBooleanExtra("isPayment",false);
        isDemo = PreferenceUtil.getString("isdemo", "0").equals("1");
    }

    @Override
    protected void initEvents() {
        btn_look_case.setOnClickListener(this);
        btn_return_main.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        if(isDemo) {
            report_sucess_hint.setText("恭喜您，模拟报案已成功!");
            btn_look_case.setVisibility(View.GONE);
            tv_payment_tip.setVisibility(View.GONE);
        }else if(isPayment){
            report_sucess_hint.setText("您已成功提交理赔资料！");
        }else{
            report_sucess_hint.setText("恭喜您，报案成功！请保持您的电话畅通！");
        }
        if("0".equals(AppCaseData.paymentStatus) && !isDemo) {
            tv_payment_tip.setVisibility(View.VISIBLE);
        }else {
            tv_payment_tip.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.btn_look_case:// 进入报案快赔的索引页
               Intent intent = new Intent(ReportSuccess.this,FastCompensate.class);
               startActivity(intent);
               finish();
               break;
           case R.id.btn_return_main:// 返回首页
               finish();
               break;
       }

    }

}
