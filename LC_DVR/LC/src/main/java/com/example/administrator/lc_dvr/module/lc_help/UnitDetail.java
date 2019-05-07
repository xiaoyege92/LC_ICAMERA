package com.example.administrator.lc_dvr.module.lc_help;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.Config;
import com.bumptech.glide.Glide;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.module.lc_dvr.GlideImageLoader;
import com.example.administrator.lc_dvr.module.login_registration.TakePhone;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.makeramen.roundedimageview.RoundedImageView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
 *   time   : 2018/11/07
 *   desc   :
 *  version :
 * </pre>
 */
public class UnitDetail extends BaseActivity implements View.OnClickListener {

    private RadioButton rb_back; // 返回
    private Banner unit_banner; // 轮播图
    private ImageView iv_unit_icon; // 单位头像
    private TextView tv_unit_name; // 单位名称
    private TextView tv_unit_address; // 单位地址
    private ImageView iv_unit_phone_call; // 单位电话
    private TextView tv_unit_server_time; // 单位服务时间
    private TextView tv_unit_code; // 单位快捷码
    private RoundedImageView riv_service_icon;// 事故专员头像
    private TextView tv_service_name; // 事故专员名称
    private ImageView iv_service_phone_call; // 事故专员电话
    private TextView tv_service_work_status; // 事故专员工作状态
    private TextView tv_notice; // 单位活动与公告

    private String unitName;
    private String unitServiceTime;
    private String unitServicePhone;
    private String servicePhone;
    private String serviceName;

    private KProgressHUD kProgressHUD;

    @Override
    protected int setViewId() {
        return R.layout.unit_detail_activity;
    }

    @Override
    protected void findView() {
        rb_back = (RadioButton) findViewById(R.id.rb_back);
        unit_banner = (Banner) findViewById(R.id.unit_banner);
        iv_unit_icon = (ImageView) findViewById(R.id.iv_unit_icon);
        tv_unit_name = (TextView) findViewById(R.id.tv_unit_name);
        tv_unit_address = (TextView) findViewById(R.id.tv_unit_address);
        iv_unit_phone_call = (ImageView) findViewById(R.id.iv_unit_phone_call);
        tv_unit_server_time = (TextView) findViewById(R.id.tv_unit_server_time);
        tv_unit_code = (TextView) findViewById(R.id.tv_unit_code);
        riv_service_icon = (RoundedImageView) findViewById(R.id.riv_service_icon);
        tv_service_name = (TextView) findViewById(R.id.tv_service_name);
        iv_service_phone_call = (ImageView) findViewById(R.id.iv_service_phone_call);
        tv_service_work_status = (TextView) findViewById(R.id.tv_service_work_status);
        tv_notice = (TextView) findViewById(R.id.tv_notice);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void initEvents() {
        rb_back.setOnClickListener(this);
        iv_unit_phone_call.setOnClickListener(this);
        iv_service_phone_call.setOnClickListener(this);
        iv_unit_icon.setOnClickListener(this);
        //点击轮播图时的响应
        unit_banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
            }
        });
    }

    @Override
    protected void loadData() {
        getUnitInfo(PreferenceUtil.getString("user_mobile", ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                finish();
                break;
            case R.id.iv_unit_phone_call:
                Intent intent = new Intent(UnitDetail.this, TakePhone.class);
                intent.putExtra("unitServiceTime", unitServiceTime);
                intent.putExtra("unitServicePhone", unitServicePhone);
                intent.putExtra("isUnitDetail", 1);
                startActivity(intent);
                break;
            case R.id.iv_service_phone_call:
                Intent intent2 = new Intent(UnitDetail.this, TakePhone.class);
                intent2.putExtra("unitServiceTime", unitServiceTime);
                intent2.putExtra("unitServicePhone", servicePhone);
                intent2.putExtra("isUnitDetail", 1);
                startActivity(intent2);
                break;
        }
    }

    /**
     * 获取单位信息
     */
    private void getUnitInfo(String user_mobile) {
        Map<String, String> map = new HashMap<>();
        // 设置用户电话
        map.put("mobile", user_mobile);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getPersonInfoByMobile(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        unitName = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitname");
                        String unitIconUrl = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("iconurl");
                        unitServiceTime = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicetime");
                        String unitCode = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("shortcode");
                        String unitNotice = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("notice");
                        unitServicePhone = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("servicephone");

                        tv_unit_name.setText(unitName);
                        tv_unit_address.setText(jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("address"));

                        tv_unit_server_time.setText(unitServiceTime);
                        tv_unit_code.setText(unitCode);
                        tv_notice.setText(unitNotice);
                        if (unitIconUrl != null && !"".equals(unitIconUrl)) {
                            Glide.with(UnitDetail.this).load(unitIconUrl).into(iv_unit_icon);// 加载单位头像
                        }

                        serviceName = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("username");
                        int workStatus = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getInt("workstatus");
                        String serviceIconUrl = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("logourl");
                        servicePhone = jsonObject.getJSONObject("datas").getJSONObject("servicepersoninfo").getString("mobile");

                        tv_service_name.setText(serviceName);
                        if (workStatus == 0) {
                            tv_service_work_status.setText("闲");
                        } else if (workStatus == 1) {
                            tv_service_work_status.setText("忙");
                        } else if (workStatus == 2) {
                            tv_service_work_status.setText("休");
                        }
                        if (null != serviceIconUrl && !"".equals(serviceIconUrl)) {
                            Glide.with(UnitDetail.this).load(serviceIconUrl).into(riv_service_icon);// 加载事故专员头像
                        }

                        String unitPicUrl = jsonObject.getJSONObject("datas").getJSONObject("unitinfo").getString("unitpic");
                        if (null != unitPicUrl && !"".equals(unitPicUrl)) {
                            //设置图片加载器
                            unit_banner.setImageLoader(new GlideImageLoader());

                            //设置指示器位置（当banner模式中有指示器时）
                            unit_banner.setIndicatorGravity(BannerConfig.RIGHT);

                            ArrayList<String> picArr = new ArrayList();
                            String[] strs = unitPicUrl.split(",");
                            for (int i = 0, len = strs.length; i < len; i++) {
                                picArr.add(Config.QINIU_BASE_URL + strs[i].toString());
                            }
                            unit_banner.setImages(picArr);
                            //banner设置方法全部调用完毕时最后调用
                            unit_banner.start();
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
                ToastUtils.showNomalShortToast(UnitDetail.this, "数据加载异常，请稍后再试");
            }
        });
    }

}
