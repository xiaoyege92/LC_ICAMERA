package com.example.administrator.lc_dvr.module.login_registration;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
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

import io.vov.vitamio.utils.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2017/12/27.
 */

public class Register extends BaseActivity implements TextWatcher {

    private EditText shortcutCode;
    private String unitcode = "";
    private int unitkind = 1;
    private EditText phoneText;
    private String smscodeid = "";
    private TextView verificationButton;
    private Timer timer;
    private int second;
    private boolean isAgreement = true;
    private ImageView agreement;
    private EditText verificationCode;
    private Button registerBtn;

    private TextView btn_select_company;
    private Button btn_unable_register;
    private TextView tv_unit_name;
    private TextView tv_help_text;
    private TextView tv_select_text;

    private String selectIndex;

    @Override
    protected int setViewId() {
        return R.layout.register_layout;
    }

    @Override
    protected void findView() {
        // 选择投保公司
        btn_select_company = (TextView) findViewById(R.id.btn_select_company);
        // 选择字样
        tv_select_text = (TextView) findViewById(R.id.tv_select_text);
        // 帮助字样
        tv_help_text = (TextView) findViewById(R.id.tv_help_text);
        // 遇到问题，无法注册
        btn_unable_register = (Button) findViewById(R.id.btn_unable_register);
        // 单位名称
        tv_unit_name = (TextView) findViewById(R.id.tv_unit_name);
        // 二维码扫描
        shortcutCode = (EditText) findViewById(R.id.shortcutCode);
        // 电话
        phoneText = (EditText) findViewById(R.id.phoneText);
        // 验证码
        verificationCode = (EditText) findViewById(R.id.verificationCode);

        // 获取验证码
        verificationButton = (TextView) findViewById(R.id.verificationButton);
        // 已阅读并遵守
        agreement = (ImageView) findViewById(R.id.agreement);
        // 注册按键
        registerBtn = (Button) findViewById(R.id.registerBtn);
    }

    @Override
    protected void init() {

        // 注册按键不可点
        registerBtn.setEnabled(false);
        registerBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
        registerBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_land_unenable));
        //添加当前的activity
        VLCApplication.addActivity(this);
        //先清空一下保险公司列表，不然会出错
        PreferenceUtil.commitString("insuranceCompany", null);
        PreferenceUtil.commitString("unitName", null);
        PreferenceUtil.commitString("unitcode", null);
        Intent intent = getIntent();
        selectIndex = intent.getStringExtra("selectIndex");
    }

    @Override
    protected void initEvents() {
        // 设置不可输入单位快捷码
        shortcutCode.setKeyListener(null);
        // 监听验证码对话框
        verificationCode.addTextChangedListener(this);
        // 监听手机号
        phoneText.addTextChangedListener(this);
        // 监听单位快捷码
        shortcutCode.addTextChangedListener(this);
        shortcutCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, UnitCode.class);
                startActivity(intent);
            }
        });
        btn_select_company.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //是4S店时才跳转界面
                if (unitkind == 1) {
                    //判断是否联网
                    if (!NetUtils.isNetworkConnected(Register.this)) {
                        ToastUtils.showNomalShortToast(Register.this, getString(R.string.network_off));
                        return;
                    }
                    //跳转到保险公司列表界面
                    Intent intent = new Intent(Register.this, InsuranceCompanyList.class);
                    startActivity(intent);
                }
            }
        });
        btn_unable_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, TakePhone.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //获得保险公司名字
        if (!"".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
            btn_select_company.setText(PreferenceUtil.getString("insuranceCompany", null));
            tv_help_text.setVisibility(View.GONE);
            tv_select_text.setVisibility(View.GONE);
            if ("空".equals(PreferenceUtil.getString("insuranceCompany", ""))) {
                btn_select_company.setText("投保公司");
            }
        } else {
            btn_select_company.setText("投保公司");
            tv_help_text.setVisibility(View.VISIBLE);
            tv_select_text.setVisibility(View.VISIBLE);
        }
        // 单位名称
        if (!"".equals(PreferenceUtil.getString("unitName", ""))) {
            tv_unit_name.setText(PreferenceUtil.getString("unitName", ""));
            tv_unit_name.setVisibility(View.VISIBLE);
        } else {

        }
        // 单位类型，如果为保险公司则不可点击，如果是维修单位就可点击
        if (2 == PreferenceUtil.getInt("unitkind", 0)) {
            btn_select_company.setClickable(false);
            unitkind = 2;
        } else if (1 == PreferenceUtil.getInt("unitkind", 0)) {
            btn_select_company.setClickable(true);
            unitkind = 1;
        }
        // 单位ID
        if (!"".equals(PreferenceUtil.getString("unitcode", ""))) {
            unitcode = PreferenceUtil.getString("unitcode", "");
            shortcutCode.setText(PreferenceUtil.getString("unitCodeShortCude", ""));
        }
    }

    @Override
    protected void loadData() {

    }

    //timer的Handler
    Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 1:
                    verificationButton.setText("已发送(" + second + ")");
                    second -= 1;
                    if (second < 0) {
                        verificationButton.setEnabled(true);
                        verificationButton.setTextColor(getResources().getColor(R.color.primary_dark));
                        verificationButton.setText("获取验证码");
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
    public void verificationTap(View view) {

        if (!Utils.isMatchered(phoneText.getText())) {
            Toast.makeText(Register.this, R.string.register_mobile_err, Toast.LENGTH_SHORT).show();
            return;
        }

        if (shortcutCode.getText() == null || shortcutCode.getText().toString().equals("")) {
            ToastUtils.showNomalShortToast(Register.this, "请先输入或扫描单位快捷码");
            return;
        }
        if (!NetUtils.isNetworkConnected(Register.this)) {
            ToastUtils.showNomalShortToast(Register.this, getString(R.string.network_off));
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("mobile", phoneText.getText().toString());
        map.put("kind", "2");

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getSMSCode(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String msg = jsonObject.getString("msg");
                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        //先停止计时器，不然有时候计时器会发生错乱
                        if (timer != null) {
                            timer.cancel();
                        }
                        JSONObject datas = jsonObject.getJSONObject("datas");
                        String smscode = datas.getString("smscodeid");
                        if (smscode != null) {
                            smscodeid = smscode;
                        }
                        verificationButton.setEnabled(false);
                        verificationButton.setTextColor(ContextCompat.getColor(Register.this, R.color.xian_color));
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
                        ToastUtils.showNomalShortToast(Register.this, msg);
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
     * 点击协议按钮时的响应
     *
     * @param view
     */
    public void agreementTap(View view) {
        if (isAgreement) {
            agreement.setImageResource(R.mipmap.agreement_unselect);
            isAgreement = false;
        } else {
            agreement.setImageResource(R.mipmap.agreement_select);
            isAgreement = true;
        }
        if (shortcutCode.getText() != null && !"".equals(shortcutCode.getText().toString())//单位
                && phoneText.getText() != null && !"".equals(phoneText.getText().toString())//电话
                && verificationCode.getText() != null && !"".equals(verificationCode.getText().toString())
                && isAgreement) {//同意协议
            if (!registerBtn.isEnabled()) {
                registerBtn.setEnabled(true);
                registerBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
                registerBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.lc_radius));
            }
        } else {
            if (registerBtn.isEnabled()) {
                registerBtn.setEnabled(false);
                registerBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
                registerBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_land_unenable));
            }
        }
    }

    /**
     * 点击注册用户时的响应
     *
     * @param view
     */
    public void registeredUserTap(View view) throws PackageManager.NameNotFoundException {

        if (!NetUtils.isNetworkConnected(Register.this)) {
            ToastUtils.showNomalShortToast(Register.this, getString(R.string.network_off));
            return;
        }
        Map<String, String> map = new HashMap<>();

        //手机号码 //去除字符串中所有的空格，不然注册会出错
        map.put("mobile", phoneText.getText().toString().replaceAll(" ", ""));
        //短信验证码
        map.put("smscode", verificationCode.getText().toString());
        //短信验证码id
        map.put("smscodeid", smscodeid);
        //单位编号
        map.put("unitcode", unitcode);
        //安卓系统
        map.put("clientos", "android");
        //系统版本号
        map.put("osversion", android.os.Build.VERSION.RELEASE);
        //设备的具体型号
        map.put("brand", android.os.Build.MODEL);
        //软件版本号
        map.put("version", getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);

        if (unitkind == 1) {
            //保险公司编号
            map.put("inscode", VLCApplication.insuranceCompanyDictionary.get(PreferenceUtil.getString("insuranceCompany", "")));

        } else {
            //保险公司编号
            map.put("inscode", unitcode);
        }

        Call<ResponseBody> call = RetrofitManager.getInstance().create().appRegister(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        JSONObject datas = jsonObject.getJSONObject("datas");
                        JSONObject session = datas.getJSONObject("session");
                        String tokenid = session.getString("tokenid");
                        //保存授权码
                        PreferenceUtil.commitString("tokenid", tokenid);
                        //保存用户的手机号码
                        PreferenceUtil.commitString("user_mobile", phoneText.getText().toString());
                        // 车主code
                        PreferenceUtil.commitString("personcode", jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("personcode"));
                        //保存授权码
                        PreferenceUtil.commitString("tokenid", tokenid);

                        // 保存单位类型
                        PreferenceUtil.commitInt("unitkind", jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getInt("unitkind"));

                        AppCaseData.mobile = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("mobile"); // 电话
                        AppCaseData.username = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"); // 用户名
                        AppCaseData.personcode = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("personcode"); // 唯一识别码

                        AppCaseData.unitcode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitcode"); // 单位识别code
                        AppCaseData.unitname = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname"); // 单位名称
                        AppCaseData.unitkind = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getInt("unitkind"); // 单位类型
                        AppCaseData.shortcode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("shortcode"); // 单位快捷码

                        String unitName = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname");
                        String unitIconUrl = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("iconurl");
                        String address = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("address");
                        String serviceTime = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicetime");
                        String helpTip = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("helptip");
                        String helpURL = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("helpurl");

                        VLCApplication.unitName = unitName;
                        VLCApplication.unitIconUrl = unitIconUrl;
                        VLCApplication.unitAddress = address;
                        VLCApplication.unitServiceTime = serviceTime;
                        VLCApplication.unitHelpTip = helpTip;
                        VLCApplication.unitHelpURL = helpURL;

                        AppCaseData.nick_name = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("username"));
                        AppCaseData.user_remark = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("demo"));
                        String headUrl = jsonObject.getJSONObject("datas").getJSONObject("personinfo").getString("logoid");
                        if (!"".equals(headUrl) && null != headUrl && !"null".equals(headUrl)) {
                            AppCaseData.headURL = Config.QINIU_BASE_URL + headUrl;
                        }else {
                            AppCaseData.headURL = "";
                        }

                        // 打开注册成功页面
                        Intent intent = new Intent(Register.this, RightMark.class);
                        intent.putExtra("selectIndex", selectIndex);
                        startActivity(intent);

                    } else {
                        Toast.makeText(Register.this, msg, Toast.LENGTH_SHORT).show();
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
     * 获得服务器的保险公司
     *
     * @param view
     */
    public void getInsuranceCompany(View view) {
        //是4S店时才跳转界面
        if (unitkind == 1) {
            //判断是否联网
            if (!NetUtils.isNetworkConnected(Register.this)) {
                ToastUtils.showNomalShortToast(Register.this, getString(R.string.network_off));
                return;
            }
            //跳转到保险公司列表界面
            Intent intent = new Intent(this, InsuranceCompanyList.class);
            startActivity(intent);
        }
    }

    /**
     * 跳转到协议内容界面
     *
     * @param view
     */
    public void protocolContent(View view) {
        //判断是否联网
        if (!NetUtils.isNetworkConnected(Register.this)) {
            ToastUtils.showNomalShortToast(Register.this, getString(R.string.network_off));
        }
        //跳转到协议内容界面
        Intent intent = new Intent(this, ProtocolContent.class);
        startActivity(intent);
    }

    /**
     * 返回上一个界面
     *
     * @param view
     */
    public void registerBackTap(View view) {
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (shortcutCode.getText() != null && !"".equals(shortcutCode.getText().toString())//单位
                && phoneText.getText() != null && !"".equals(phoneText.getText().toString())//电话
//                && !"请选择投保公司".equals(insuranceCompanyName.getText())//投保公司
                && verificationCode.getText() != null && !"".equals(verificationCode.getText().toString())//验证码
                && isAgreement) {//同意协议
            if (!registerBtn.isEnabled()) {
                registerBtn.setEnabled(true);
                registerBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
                registerBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.lc_radius));
            }
        } else {
            if (registerBtn.isEnabled()) {
                registerBtn.setEnabled(false);
                registerBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
                registerBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_land_unenable));
            }
        }

    }

}
