package com.example.administrator.lc_dvr.module.login_registration;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2017/12/27.
 */

public class Landing extends BaseActivity implements TextWatcher {

    private EditText phoneText_dl;
    private TextView verificationButton_dl;
    private Timer timer;
    private String smscodeid = "";
    private int second;
    private Button signInBtn;
    private EditText verificationText_dl;
    private long lastTime;
    private boolean isFirst = true;

    private String selectIndex;

    @Override
    protected int setViewId() {
        return R.layout.landing_layout;
    }

    @Override
    protected void findView() {
        phoneText_dl = (EditText) findViewById(R.id.phoneText_dl);
        verificationText_dl = (EditText) findViewById(R.id.verificationText_dl);
        verificationButton_dl = (TextView) findViewById(R.id.verificationButton_dl);
        signInBtn = (Button) findViewById(R.id.signInBtn);

    }

    @Override
    protected void init() {
        //添加当前的activity
        VLCApplication.addActivity(this);

        //设置登录按钮不能点击
        signInBtn.setEnabled(false);
        signInBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
        signInBtn.setBackground(ContextCompat.getDrawable(Landing.this, R.drawable.btn_land_unenable));

        Intent intent = getIntent();
        selectIndex = intent.getStringExtra("selectIndex");
    }

    @Override
    protected void initEvents() {
        verificationText_dl.addTextChangedListener(this);
    }

    @Override
    protected void loadData() {

    }

    /**
     * 进入到注册界面
     *
     * @param view
     */
    public void inRegister(View view) {
        Intent intent = new Intent(Landing.this, Register.class);
        intent.putExtra("selectIndex", selectIndex);
        startActivity(intent);
    }

    //timer的Handler
    Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 1:
                    verificationButton_dl.setText("已发送(" + second + ")");
                    second -= 1;
                    if (second < 0) {
                        verificationButton_dl.setEnabled(true);
                        verificationButton_dl.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark));
                        verificationButton_dl.setText("获取验证码");
                        timer.cancel();
                    }
                    break;
            }
        }
    };

    /**
     * 点击验证码按钮时的响应
     *
     * @param view
     */
    public void verificationTap2(View view) {
        // 如果手机号为空
        if (!Utils.isMatchered(phoneText_dl.getText())) {
            Toast.makeText(Landing.this, R.string.register_mobile_err, Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("mobile", phoneText_dl.getText().toString());
        map.put("kind", "1");
        verificationButton_dl.setEnabled(false);
        Call<ResponseBody> call = RetrofitManager.getInstance().create().getSMSCode(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (msg.equals("成功")) {

                        //先停止计时器，不然有时候计时器会发生错乱
                        if (timer != null) {
                            timer.cancel();
                        }
                        JSONObject datas = jsonObject.getJSONObject("datas");
                        String smscode = datas.getString("smscodeid");
                        if (smscode != null) {
                            smscodeid = smscode;
                        }

                        verificationButton_dl.setEnabled(false);
                        verificationButton_dl.setTextColor(ContextCompat.getColor(Landing.this, R.color.xian_color));
                        //60秒后才能再次获取验证码
                        second = 60;
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                timerHandler.sendEmptyMessage(1);
                            }
                        }, 1000, 1000);
                    } else {
                        ToastUtils.showNomalShortToast(Landing.this, msg);
                        verificationButton_dl.setEnabled(true);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                verificationButton_dl.setEnabled(true);
            }
        });
    }

    /**
     * 点击登录按钮时的响应
     *
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void signInTap(View view) throws PackageManager.NameNotFoundException {
        // 判断是否联网
        if (!NetUtils.isNetworkConnected(Landing.this)) {
            ToastUtils.showNomalShortToast(Landing.this, getString(R.string.network_off));
            return;
        }
        Map<String, String> map = new HashMap<>();
        //手机号码
        map.put("mobile", phoneText_dl.getText().toString());
        //短信验证码
        map.put("smscode", verificationText_dl.getText().toString());
        //短信验证码id
        map.put("smscodeid", smscodeid);
        //安卓系统
        map.put("clientos", "android");
        //系统版本号
        map.put("osversion", android.os.Build.VERSION.RELEASE);
        //设备的具体型号
        map.put("brand", android.os.Build.MODEL);
        //软件版本号
        map.put("version", getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().appLogin(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        JSONObject datas = jsonObject.getJSONObject("datas");
                        JSONObject session = datas.getJSONObject("session");
                        String tokenid = session.getString("tokenid");
                        //保存授权码
                        PreferenceUtil.commitString("tokenid", tokenid);
                        //保存用户的手机号码
                        PreferenceUtil.commitString("user_mobile", phoneText_dl.getText().toString());
                        // 车主code
                        PreferenceUtil.commitString("personcode", jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("personcode"));
                        // 保存单位类型
                        PreferenceUtil.commitInt("unitkind", jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getInt("unitkind"));

                        AppCaseData.mobile = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("mobile"); // 电话
                        AppCaseData.username = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"); // 用户名
                        AppCaseData.personcode = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("personcode"); // 唯一识别码

                        AppCaseData.unitcode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitcode"); // 单位识别code
                        AppCaseData.unitname = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname"); // 单位名称
                        AppCaseData.unitkind = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getInt("unitkind"); // 单位类型
                        AppCaseData.shortcode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("shortcode"); // 单位快捷码

                        AppCaseData.nick_name = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"));
                        AppCaseData.user_remark = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo"));
                        String headUrl = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid");
                        if (!"".equals(headUrl) && null != headUrl && !"null".equals(headUrl)) {
                            AppCaseData.headURL = Config.QINIU_BASE_URL + headUrl;
                        }else {
                            AppCaseData.headURL = "";
                        }
                        //移除所有的activity
                        VLCApplication.removeALLActivity();
                        //进入主界面
                        Intent intent = new Intent(Landing.this, MainActivity.class);
                        intent.putExtra("selectIndex", selectIndex);
                        startActivity(intent);

                    } else {
                        Toast.makeText(Landing.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 遇到问题无法登陆
     *
     * @param view
     */
    public void getHelp(View view) {
        // 判断是否联网
        if (!NetUtils.isNetworkConnected(Landing.this)) {
            ToastUtils.showNomalShortToast(Landing.this, getString(R.string.network_off));
            return;
        }

        Intent intent = new Intent(this, TakePhone.class);
        startActivity(intent);
    }

    /**
     * 返回上一个界面
     *
     * @param view
     */
    public void landBackTap(View view) {
        //进入主界面
        Intent intent = new Intent(Landing.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 作用是：按返回键进入主页面
     */
    public void onBackPressed() {
        if (isFirst) {
            Toast.makeText(this, R.string.zaiantuichu, Toast.LENGTH_SHORT).show();
            lastTime = System.currentTimeMillis();
            isFirst = false;
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime <= 2000) {
                System.exit(1);//用这个相当于系统把后台完全杀死了，finish()做不到这个效果
            } else {
                Toast.makeText(this, R.string.zaiantuichu, Toast.LENGTH_SHORT).show();
                lastTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (verificationText_dl.getText() != null && !"".equals(verificationText_dl.getText().toString())
                && phoneText_dl.getText() != null && !"".equals(phoneText_dl.getText().toString())) {
            if (!signInBtn.isEnabled()) {
                signInBtn.setEnabled(true);
                signInBtn.setTextColor(ContextCompat.getColor(Landing.this, R.color.white));
                signInBtn.setBackground(ContextCompat.getDrawable(Landing.this, R.drawable.lc_radius));
            }
        } else {
            if (signInBtn.isEnabled()) {
                //设置登录按钮不能点击
                signInBtn.setEnabled(false);
                signInBtn.setTextColor(ContextCompat.getColor(Landing.this, R.color.unenable_btn_text));
                signInBtn.setBackground(ContextCompat.getDrawable(Landing.this, R.drawable.btn_land_unenable));
            }
        }
    }

}
