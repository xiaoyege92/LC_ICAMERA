package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.module.AppCaseData;

public class UserInfoActivity extends Activity {

    private TextView tv_cancel; // 取消
    private TextView tv_ok; // 确定

    private EditText et_nick_name; // 用户昵称
    private EditText et_user_remark; // 用户备注

    private String nick_name; // 用户昵称
    private String user_remark; // 用户备注

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_user_info);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        findView();
        init();
        initEvents();
        loadData();
    }

    private void findView() {
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        et_nick_name = (EditText) findViewById(R.id.et_nick_name);
        et_user_remark = (EditText) findViewById(R.id.et_user_remark);
    }

    private void init() {
        nick_name = AppCaseData.nick_name;
        user_remark = AppCaseData.user_remark;
    }

    private void initEvents() {
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCaseData.nick_name = et_nick_name.getText().toString();
                AppCaseData.user_remark = et_user_remark.getText().toString();
                finish();
            }
        });
    }

    private void loadData() {
        et_nick_name.setText(nick_name);
        et_user_remark.setText(user_remark);
    }

}
