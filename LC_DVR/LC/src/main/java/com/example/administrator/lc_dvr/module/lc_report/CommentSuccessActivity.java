package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.module.lc_help.FastCompensate;
import com.example.administrator.lc_dvr.module.lc_help.ReportSuccess;

public class CommentSuccessActivity extends BaseActivity implements View.OnClickListener {
    private Button btn_look_case; // 查看案件
    private Button btn_return_main;// 返回首页
    private TextView report_sucess_hint; // 评价成功提示

    @Override
    protected int setViewId() {
        return R.layout.activity_comment_success;
    }

    @Override
    protected void findView() {
        btn_look_case = (Button) findViewById(R.id.btn_look_case);
        btn_return_main = (Button) findViewById(R.id.btn_return_main);
        report_sucess_hint = (TextView) findViewById(R.id.report_sucess_hint);
    }

    @Override
    protected void init() {
        btn_look_case.setOnClickListener(this);
        btn_return_main.setOnClickListener(this);
    }

    @Override
    protected void initEvents() {
        btn_look_case.setOnClickListener(this);
        btn_return_main.setOnClickListener(this);
    }

    @Override
    protected void loadData() {
        Intent intent = getIntent();
        if("2".equals(intent.getStringExtra("CommentTime")) ) {
            report_sucess_hint.setText("您已经成功进行第1次评价");
        }else if("3".equals(intent.getStringExtra("CommentTime"))) {
            report_sucess_hint.setText("您已经成功进行第2次评价");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_look_case:// 进入报案快赔的索引页
                Intent intent = new Intent(CommentSuccessActivity.this,FastCompensate.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_return_main:// 返回首页
                finish();
                break;
        }
    }
}
