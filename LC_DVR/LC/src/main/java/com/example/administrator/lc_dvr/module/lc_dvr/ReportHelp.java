package com.example.administrator.lc_dvr.module.lc_dvr;

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
import com.example.administrator.lc_dvr.bean.Contact;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2018/2/23.
 */

public class ReportHelp extends Activity {

    private TextView serviceTime;
    private TextView serviceAddress;
    private TextView serviceName;
    private TextView tv_servicephone;
//    private ImageView serviceIcon;

    private KProgressHUD kProgressHUD;
    // 事故专员账号状态  1启用/ 0禁用
    private int serviceStatus = 0;
    // 事故专员是否被删除 1已删除/0未删除
    private int serviceDeleteFlag;
    // 单位状态 1启用/0禁用
    private int unitStatus;
    //服务号码
    private String servicePhone = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.report_help_layout);// 设置布局内容

        //服务时间
        serviceTime = (TextView) findViewById(R.id.serviceTime);
        //服务的地址
        serviceAddress = (TextView) findViewById(R.id.serviceAddress);
        //服务员的名字
        serviceName = (TextView) findViewById(R.id.serviceName);
        tv_servicephone = (TextView) findViewById(R.id.tv_servicephone);
        //服务员的头像
//        serviceIcon = (ImageView) findViewById(R.id.serviceIcon);0

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        Intent intent = getIntent();
        // 如果是一键求助界面跳转过来的
        if (intent.getBooleanExtra("isSeekHelp", false)) {
            String phone = intent.getStringExtra("phoneText");
            // 获取人员详细信息
            getPersonalInfo(phone);
        } else if (intent.getIntExtra("isUnitDetail", 0) == 2) {
            serviceAddress.setText(intent.getStringExtra("unitName"));
            serviceTime.setText(intent.getStringExtra("unitServiceTime"));
            servicePhone = intent.getStringExtra("servicePhone");
            tv_servicephone.setText(servicePhone);
            serviceName.setText(intent.getStringExtra("serviceName"));
        } else {
            //获得事故ID的具体内容
            getCasedetail();
        }

    }

    /**
     * 根据手机号获取人员信息
     *
     * @param user_mobile
     */
    private void getPersonalInfo(String user_mobile) {
        Map<String, String> map = new HashMap<>();
        // 设置用户电话
        map.put("mobile", user_mobile);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getPersonInfoByMobile(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        //服务时间
                        serviceTime.setText(jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicetime"));
                        //单位名称
                        serviceAddress.setText(jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname"));
                        // 事故专员状态
                        serviceStatus = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getInt("status");
                        servicePhone = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("mobile");
                        // 如果专员状态禁用或者专员电话为空,用单位的服务电话
                        if (serviceStatus == 0) { //
                            //服务单位号码
                            servicePhone = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicephone");
                            //事故专员名字为空
                            serviceName.setText("");
                            tv_servicephone.setText(servicePhone);
                            // 单位的头像
                        } else {
                            //事故专员的名字
                            serviceName.setText(jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("username"));
                            tv_servicephone.setText(servicePhone);
                        }

                    } else {
                    }
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
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
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        Map<String, String> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseDetail(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {

                        //服务时间
                        serviceTime.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("unitservicetime"));
                        //单位名称
                        serviceAddress.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("unitname"));
                        // 事故专员电话
                        servicePhone = jsonObject.getJSONObject("datas").getJSONObject("case").getString("servicemobile");
                        // 事故专员状态
                        serviceStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getInt("servicestatus");
                        // 事故专员是否删除标志
                        serviceDeleteFlag = jsonObject.getJSONObject("datas").getJSONObject("case").getInt("serviceDeleteFlag");
                        // 如果专员状态删除、禁用或者专员电话为空，用单位服务电话
                        if (1 == serviceDeleteFlag || 0 == serviceStatus) { //
                            //服务单位号码
                            servicePhone = jsonObject.getJSONObject("datas").getJSONObject("case").getString("unitservicephone");
                            serviceName.setText("");
                            tv_servicephone.setText(servicePhone);
                        } else {
                            // 事故专员名字
                            serviceName.setText(jsonObject.getJSONObject("datas").getJSONObject("case").getString("servicename"));
                            // 事故专员电话
                            servicePhone = jsonObject.getJSONObject("datas").getJSONObject("case").getString("servicemobile");
                            tv_servicephone.setText(servicePhone);
                        }
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
     * 退出当前的页面
     *
     * @param view
     */
    public void closeReportHelp(View view) {
        finish();
    }

    /**
     * 点击拨打电话号码时的响应
     *
     * @param view
     */
    public void takeServicePhone(View view) throws JSONException {

        addContact();
    }

    /**
     * 新增拨打电话记录
     */
    private void addContact() throws JSONException {

        Gson gson = new Gson();
        Contact contact = new Contact();
        contact.setCaseid(PreferenceUtil.getString("caseid", null));
        contact.setMobile(servicePhone);
        contact.setUsertype(2);// 车主
        contact.setUserid(PreferenceUtil.getString("personcode", ""));
        contact.setContacttype(1);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().addContact(NetUtils.getHeaders(), contact);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                    }
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + servicePhone));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        finish();
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


}
