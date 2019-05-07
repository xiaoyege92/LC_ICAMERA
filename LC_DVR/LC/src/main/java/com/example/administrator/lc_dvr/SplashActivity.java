package com.example.administrator.lc_dvr;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.Config;
import com.baidu.mobstat.StatService;
import com.example.administrator.lc_dvr.bean.BannerPicture;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.vov.vitamio.utils.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/08/05
 *   desc   :
 *  version :
 * </pre>
 */
public class SplashActivity extends AppCompatActivity {

    private ImageView mImgStart;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        StatService.start(this);

        File file2 = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");
        file2.mkdir();
        initView();
        File file = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/demo.mp4");
        if (!file.exists() && !PreferenceUtil.getBoolean("isFirstInAPP", false)) {//如果文件不存在并且未删除过
            new CopyFileTask().execute();// 开启拷贝文件的异步任务
        }

        // 缓存保险公司logo
        getInsuranceCompanyList();
        // 缓存行车记录失败页面图片
        getWelcomeImage();

        // 缓存单位信息 TODO
        getUnitInfo(PreferenceUtil.getString("user_mobile", ""));

//        String testDeviceID = StatService.getTestDeviceId(this);
//        Log.e("设备名称："+ android.os.Build.DEVICE+"   设备ID："+testDeviceID);

    }

    private void initView() {
        mImgStart = (ImageView) findViewById(R.id.id_img_start);
        iniImage();
    }

    private void iniImage() {

        mImgStart.setImageResource(R.mipmap.start);

        ScaleAnimation scaleAnim = new ScaleAnimation(
                1.0f,
                1.2f,
                1.0f,
                1.2f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        scaleAnim.setFillAfter(true);
        scaleAnim.setDuration(3000);
        scaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //在这里做一些初始化的操作
                //跳转到指定的Activity
                startActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mImgStart.startAnimation(scaleAnim);
    }

    private void startActivity() {
        Intent intent = new Intent(SplashActivity.this, GuideViewActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private class CopyFileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Utils.copyFilesFassets(getApplicationContext(), "demo.mp4", BitmapUtils.getSDPath() + "/VOC/ACCIDENT");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            File file = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/demo.mp4");
            if (file.exists()) {
                PreferenceUtil.commitBoolean("isFirstInAPP", true);
            }
        }
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

                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray units = datas.getJSONArray("units");
                    VLCApplication.insuranceCompanyDictionary.clear();
                    VLCApplication.insuranceCompanyDictionary.put("空", "");
                    for (int i = 0; i < units.length(); i++) {
                        JSONObject value = (JSONObject) units.get(i);
                        String unitname = value.getString("unitname");
                        String unitcode = value.getString("unitcode");
                        String logo = value.getString("logo");

                        PreferenceUtil.commitString(unitcode, logo);
                        VLCApplication.insuranceCompanyDictionary.put(unitname, unitcode);
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
     * 获得服务器的欢迎图，并判断是否要进入到欢迎界面
     */
    private void getWelcomeImage() {
        SimpleDateFormat formatterdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String format = formatterdate.format(curDate);

        Map<String, String> map = new HashMap<>();
        map.put("fromdate", format);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getWelcomInfo(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    VLCApplication.bannerPictureList.clear();
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray ads = datas.getJSONArray("ads");
                    for (int i = 0; i < ads.length(); i++) {
                        JSONObject value = (JSONObject) ads.get(i);
                        String piclink = value.getString("piclink");
                        String title = value.getString("title");
                        String url = value.getString("url");
                        BannerPicture bannerPicture = new BannerPicture(title, piclink, url);
                        VLCApplication.bannerPictureList.add(bannerPicture);
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
                        // 如果单位被删除，显示公司的电话
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
