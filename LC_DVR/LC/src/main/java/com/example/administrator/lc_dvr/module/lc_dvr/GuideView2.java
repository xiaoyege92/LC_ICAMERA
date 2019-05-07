package com.example.administrator.lc_dvr.module.lc_dvr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Config;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.administrator.lc_dvr.GuideViewAdapter;
import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.login_registration.Landing;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangboru on 2018/2/23.
 */

public class GuideView2 extends AppCompatActivity {
    private ViewPager viewPage;
    private List<View> list;
    private List<String> urlList;
    // 底部小点的图片
    private LinearLayout llPoint;
    //立即进入按钮
    private TextView textView;
    private ImageView skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_view_acitivyt);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initview();

        //初始化urlList
        urlList = new ArrayList<>();

        //进入到欢迎界面
        getWelcomeImage();

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

        JSONObject jsonObject = new JSONObject(map);
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "https://cloud.linkersoft.com/jap/ins/api/welcomeinfo", jsonObject, new Listener<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray welcomepages = datas.getJSONArray("welcomepages");
                    for (int i = 0; i < welcomepages.length(); i++) {
                        JSONObject value = (JSONObject) welcomepages.get(i);
                        String piclink = value.getString("piclink");
                        urlList.add(piclink);
                    }

                    //添加广告图
                    addView();
                    initoper();
                    addPoint();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                return headers;
            }
        };jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                Config.TIME_OUT,//默认超时时间，应设置一个稍微大点儿的
                Config.REQUEST_TIME,//默认最大尝试次数
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        VLCApplication.queue.add(jsonRequest);
    }


    private void initoper() {
        // 进入按钮
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //进入主界面
                Intent intent = new Intent(GuideView2.this, MainActivity.class);
                intent.putExtra("selectIndex", "lc_dvr");
                startActivity(intent);
                finish();
            }
        });

        // 2.监听当前显示的页面，将对应的小圆点设置为选中状态，其它设置为未选中状态
        viewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                monitorPoint(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }

    private void initview() {
        viewPage = (ViewPager) findViewById(R.id.viewpage);
        llPoint = (LinearLayout) findViewById(R.id.llPoint);
        textView = (TextView) findViewById(R.id.guideTv);
        skip = (ImageView) findViewById(R.id.skip);

    }

    /**
     * 添加图片到view
     */
    private void addView() {

        //要显示广告图时才显示跳过按钮
        skip.setVisibility(View.VISIBLE);

        list = new ArrayList<View>();
        // 将imageview添加到view
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        for (int i = 0; i < urlList.size(); i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder() //
                    .cacheOnDisc(true)
                    .resetViewBeforeLoading(true)//设置下载的图片下载前是否重置，复位
                    .cacheInMemory(true)//设置下载图片是否缓存到内存
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置图片解码类型
                    .displayer(new FadeInBitmapDisplayer(300))//设置用户加载图片的task(这里是渐现)
                    .build();
            ImageLoader.getInstance().displayImage(urlList.get(i), iv, defaultDisplayImageOptions);
            list.add(iv);
        }
        // 加入适配器
        viewPage.setAdapter(new GuideViewAdapter(list));

    }

    /**
     * 添加小圆点
     */
    private void addPoint() {
        // 1.根据图片多少，添加多少小圆点
        for (int i = 0; i < urlList.size(); i++) {
            LinearLayout.LayoutParams pointParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i < 1) {
                pointParams.setMargins(0, 0, 0, 0);
            } else {
                pointParams.setMargins(10, 0, 0, 0);
            }
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(pointParams);
            iv.setBackgroundResource(R.mipmap.pagecontrol_normal);
            llPoint.addView(iv);
        }
        llPoint.getChildAt(0).setBackgroundResource(R.mipmap.pagecontrol_current);

    }

    /**
     * 判断小圆点
     *
     * @param position
     */
    private void monitorPoint(int position) {
        for (int i = 0; i < urlList.size(); i++) {
            if (i == position) {
                llPoint.getChildAt(position).setBackgroundResource(
                        R.mipmap.pagecontrol_current);
            } else {
                llPoint.getChildAt(i).setBackgroundResource(
                        R.mipmap.pagecontrol_normal);
            }
        }
        // 3.当滑动到最后一个添加按钮点击进入，
        if (position == urlList.size() - 1) {
            textView.setVisibility(View.VISIBLE);
            skip.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击跳过按钮时的响应
     *
     * @param view
     */
    public void skipTap(View view) {
        //进入主界面
        Intent intent = new Intent(GuideView2.this, MainActivity.class);
        intent.putExtra("selectIndex", "lc_dvr");
        startActivity(intent);
        finish();
    }
}
