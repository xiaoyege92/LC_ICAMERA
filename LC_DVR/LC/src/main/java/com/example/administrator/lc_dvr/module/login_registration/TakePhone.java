package com.example.administrator.lc_dvr.module.login_registration;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;

import org.videolan.libvlc.VLCApplication;

/**
 *
 */

public class TakePhone extends Activity {

    private TextView servicePhone;
    private TextView serviceTime;
    private String phoneText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.unable_to_land);// 设置布局内容

        servicePhone = (TextView) findViewById(R.id.servicePhone);
        serviceTime = (TextView) findViewById(R.id.serviceTime);

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //添加当前的activity
        VLCApplication.addActivity(this);

        Intent intent = getIntent();
        // 如果是直接点击单位详情页面的电话，则直接进行弹窗
        if (intent.getIntExtra("isUnitDetail", 0) == 1) {
            servicePhone.setText(intent.getStringExtra("unitServicePhone"));
            serviceTime.setText(intent.getStringExtra("unitServiceTime"));
        } else {
            if (VLCApplication.configsDictionary.get("APP-B-031") != null) {
                servicePhone.setText(VLCApplication.configsDictionary.get("APP-B-031"));
            } else {
                servicePhone.setText("021-80270398");
            }

            if (VLCApplication.configsDictionary.get("APP-B-026") != null) {
                serviceTime.setText(VLCApplication.configsDictionary.get("APP-B-026"));
            } else {
                serviceTime.setText("尊敬的用户：我们的服务时间为工作日09:00-18:00，欢迎与我们联系！");
            }
        }

    }

    /**
     * 关闭当前页面
     *
     * @param view
     */
    public void closePHelpPage(View view) {
        finish();
    }

    /**
     * 拨打电话
     *
     * @param view
     */
    public void goDialing(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + servicePhone.getText().toString()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

}
