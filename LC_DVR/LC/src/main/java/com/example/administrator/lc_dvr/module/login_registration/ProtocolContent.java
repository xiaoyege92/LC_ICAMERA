package com.example.administrator.lc_dvr.module.login_registration;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by yangboru on 2017/12/30.
 */

public class ProtocolContent extends BaseActivity {

    private TextView agreement;

    @Override
    protected int setViewId() {
        return R.layout.protocol_content;
    }

    @Override
    protected void findView() {
        agreement = (TextView) findViewById(R.id.agreement);
    }

    @Override
    protected void init() {
        //获得协议内容
        getLcAgreement();
    }

    /**
     * 获得协议内容
     */
    private void getLcAgreement() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getAgreement(NetUtils.getHeaders());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONObject agreement1 = datas.getJSONObject("agreement");
                    String content = agreement1.getString("content");
                    CharSequence charSequence = Html.fromHtml(content);
                    agreement.setText(charSequence);
                    //该语句在设置后必加，不然没有任何效果
                    agreement.setMovementMethod(LinkMovementMethod.getInstance());

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


    @Override
    protected void initEvents() {

    }

    @Override
    protected void loadData() {

    }


    /**
     * 关闭当前的页面
     *
     * @param view
     */
    public void closeProtocolContent(View view) {
        finish();
    }
}
