package com.example.administrator.lc_dvr.module.lc_help;


import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.lc_report.ImmediatelyReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OneKeyHelpFragment extends BaseFragment {

    private TextView tv_unit_name; // 单位名称
    private TextView tv_unit_server_time; // 服务时间
    private TextView tv_servicephone; // 事故专员电话
    private TextView tv_servicename; // 事故人员姓名
    private TextView tv_lookmore; // 查看更多
    private LinearLayout ll_normal_report; // 正式报案
    private LinearLayout ll_take_phone; // 拨打电话
    private String servicePhone; // 电话

    @Override
    protected int setViewId() {
        return R.layout.activity_one_key_help;
    }

    @Override
    protected void findView(View view) {
        tv_unit_name = (TextView) view.findViewById(R.id.tv_unit_name);
        tv_unit_server_time = (TextView) view.findViewById(R.id.tv_unit_server_time);
        tv_servicephone = (TextView) view.findViewById(R.id.tv_servicephone);
        tv_servicename = (TextView) view.findViewById(R.id.tv_servicename);
        tv_lookmore = (TextView) view.findViewById(R.id.tv_lookmore);
        ll_normal_report = (LinearLayout) view.findViewById(R.id.ll_normal_report);
        ll_take_phone = (LinearLayout) view.findViewById(R.id.ll_take_phone);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void initEvents() {
        // 查看更多
        tv_lookmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.doChangeFragment();
            }
        });
        // 正式报案
        ll_normal_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //记录是否是模拟报案
                PreferenceUtil.commitString("isdemo", "0");
                Intent intent = new Intent(getContext(), ImmediatelyReport.class);
                startActivity(intent);
            }
        });
        // 拨打电话
        ll_take_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + servicePhone));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void loadData() {
        String user_mobile = PreferenceUtil.getString("user_mobile","");
        getPersonalInfo(user_mobile);
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
                        tv_unit_server_time.setText("服务时间："+jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicetime"));
                        //单位名称
                        tv_unit_name.setText(jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname"));
                        // 事故专员状态
                        int serviceStatus = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getInt("status");
                        servicePhone = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("mobile");
                        // 如果专员状态禁用或者专员电话为空,用单位的服务电话
                        if (serviceStatus == 0) { //
                            //服务单位号码
                            servicePhone = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicephone");
                            //事故专员名字为空
                            tv_servicename.setText("");
                            tv_servicephone.setText(servicePhone);
                            // 单位的头像
                        } else {
                            //事故专员的名字
                            tv_servicename.setText("事故专员："+jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("username"));
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

}
