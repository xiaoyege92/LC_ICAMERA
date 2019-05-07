package com.example.administrator.lc_dvr.module.login_registration;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.customview.SecurityCodeView;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.module.AppCaseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/01
 *   desc   :
 *  version :
 * </pre>
 */
public class UnitCode extends Activity implements SecurityCodeView.InputCompleteListener {

    // 关闭框
    private ImageView closeUnitCode;
    // 输入单位快捷码
    private SecurityCodeView scv_edittext;

    private String strUnitCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unit_code_activity);

        closeUnitCode = (ImageView) findViewById(R.id.closeUnitCode);
        scv_edittext = (SecurityCodeView) findViewById(R.id.scv_edittext);
        scv_edittext.setInputCompleteListener(this);
        closeUnitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void inputComplete() {
        //判断是否联网
        if (!NetUtils.isNetworkConnected(UnitCode.this)) {
            ToastUtils.showNomalShortToast(UnitCode.this, getString(R.string.network_off));
            return;
        }
        getInsuranceCompanyList(scv_edittext.getEditContent());
    }

    @Override
    public void deleteContent(boolean isDelete) {

    }

    /**
     * 单位信息查询
     */
    private void getInsuranceCompanyList(String str) {
        Map<String, String> map = new HashMap<>();
        map.put("shortcode", str);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().searchUnit(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        JSONObject datas = jsonObject.getJSONObject("datas");
                        JSONArray welcomepages = datas.getJSONArray("units");
                        // 事故专员
                        JSONObject serviceperson = datas.getJSONObject("ServicePerson");
                        String serviceName = serviceperson.getString("username");
                        String servicecode = serviceperson.getString("servicecode");
                        int workStatus = serviceperson.getInt("status");

                        PreferenceUtil.commitString("servicepersonname", serviceName);
                        PreferenceUtil.commitString("servicecode", servicecode);
                        if (workStatus == 0) {
                            PreferenceUtil.commitString("serviceworkstatus", "闲");
                        } else if (workStatus == 1) {
                            PreferenceUtil.commitString("serviceworkstatus", "忙");
                        } else if (workStatus == 2) {
                            PreferenceUtil.commitString("serviceworkstatus", "休");
                        }


                        for (int i = 0; i < welcomepages.length(); i++) {
                            JSONObject value = (JSONObject) welcomepages.get(i);
                            // 单位名称
                            String unitName = value.getString("unitname");
                            // 单位快捷码
                            String unitcode = value.getString("unitcode");
                            int unitkind = value.getInt("unitkind");

                            PreferenceUtil.commitString("unitName", unitName);
                            PreferenceUtil.commitString("unitcode", unitcode);
                            PreferenceUtil.commitString("unitCodeShortCude", scv_edittext.getEditContent());
                            PreferenceUtil.commitInt("unitkind", unitkind);

                            //   是保险公司才执行里面的代码
                            if (unitkind == 2) {
                                PreferenceUtil.commitString("insuranceCompany", unitName);
                                PreferenceUtil.commitString("insuranceCompanyMobile", value.getString("servicephone"));
                                String inscode = value.getString("inscode");
                                VLCApplication.insuranceCompanyDictionary.put(unitName, inscode);
                                AppCaseData.unitDetail.setUnitkind(2);
                            } else {
                                AppCaseData.unitDetail.setUnitkind(1);
                            }
                        }
                        finish();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "单位快捷码在系统中不存在，请重新输入", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        scv_edittext.clearEditText();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                scv_edittext.clearEditText();
            }
        });
    }

}
