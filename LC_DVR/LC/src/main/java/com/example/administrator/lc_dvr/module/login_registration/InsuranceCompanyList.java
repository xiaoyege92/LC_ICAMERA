package com.example.administrator.lc_dvr.module.login_registration;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.AppCaseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2017/12/29.
 */

public class InsuranceCompanyList extends Activity {

    private ListView insuranceCompany;
    private ArrayList<String> insuranceCompanyArray;
    private ArrayList<String> insuranceMobileArr;
    private CommonAdapter adapter;
    private String insuranceName;
    private String insuranceMobile;
    private String inscode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.company_list);// 设置布局内容

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        insuranceCompany = (ListView) findViewById(R.id.insuranceCompany);

        insuranceCompanyArray = new ArrayList<>();
        insuranceMobileArr = new ArrayList<>();

        adapter = new CommonAdapter(this, insuranceCompanyArray, R.layout.company_list_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {

                //设置保险公司名字
                helper.setText(R.id.insurance_name, insuranceCompanyArray.get(position));

            }
        };

        //设置适配器
        insuranceCompany.setAdapter(adapter);

        //获得服务器的保险公司列表
        getInsuranceCompanyList();

        insuranceCompany.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //保存保险公司名字
                insuranceName = insuranceCompanyArray.get(position);
                insuranceMobile = insuranceMobileArr.get(position);
                //下面的代码是实现了点击item时，改变item的背景色
                for (int i = 0; i < insuranceCompanyArray.size(); i++) {
                    if (i == position) {
                        View childAt = insuranceCompany.getChildAt(i - insuranceCompany.getFirstVisiblePosition());
                        if (childAt != null) {
                            childAt.setBackgroundColor(getResources().getColor(R.color.gray_back));
                        }
                    } else {
                        View childAt = insuranceCompany.getChildAt(i - insuranceCompany.getFirstVisiblePosition());
                        if (childAt != null) {
                            childAt.setBackgroundColor(getResources().getColor(R.color.white));
                        }
                    }
                }
            }
        });
    }

    /**
     * 点击取消按钮时的响应
     *
     * @param view
     */
    public void cancel(View view) {
        finish();
    }


    /**
     * 点击确定按钮时的响应
     *
     * @param view
     */
    public void Determine(View view) {
        finish();
        AppCaseData.inscode = VLCApplication.insuranceCompanyDictionary.get(PreferenceUtil.getString("insuranceCompany", ""));
        PreferenceUtil.commitString("insuranceCompany", insuranceName);
        PreferenceUtil.commitString("insuranceCompanyMobile", insuranceMobile);

    }

    /**
     * 获得服务器的保险公司列表
     */
    private void getInsuranceCompanyList() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getInsuranceCompanyList(NetUtils.getHeaders());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    VLCApplication.insuranceCompanyDictionary.clear();
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray units = datas.getJSONArray("units");
                    insuranceCompanyArray.add("空");
                    insuranceMobileArr.add("");
                    VLCApplication.insuranceCompanyDictionary.put("空", "");
                    for (int i = 0; i < units.length(); i++) {
                        JSONObject value = (JSONObject) units.get(i);
                        String unitname = value.getString("unitname");
                        String unitcode = value.getString("unitcode");
                        insuranceCompanyArray.add(unitname);
                        insuranceMobileArr.add(value.getString("companymobile"));
                        VLCApplication.insuranceCompanyDictionary.put(unitname, unitcode);
                    }
                    //刷新列表
                    adapter.notifyDataSetChanged();

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
