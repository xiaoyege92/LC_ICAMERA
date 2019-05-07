package com.example.administrator.lc_dvr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

public class GuideViewActivity extends AppCompatActivity {
    private ViewPager viewPage;
    private List<View> list;
    // 导航图
    private List<String> urlList;
    //底部小点的图片
    private LinearLayout llPoint;
    //立即进入按钮
    private TextView textView;
    private ImageView skip;
    private List<String> urlArray;
    private ListDataSave dataSave;
    private List<String> showedPicArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_view_acitivyt);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initview();
        //初始化urlList
        urlList = new ArrayList<>();

        //保存未展示过的图片url
        urlArray = new ArrayList<>();
        // 保存已经显示过得url
        showedPicArr = new ArrayList<>();
        //用来保存list到本地
        dataSave = new ListDataSave(this, "baiyu");
        if (PreferenceUtil.getBoolean("isUpdateDic", false)) {
            //获得服务器的所有字典
            getConfigs();
        }
        //如果是连接上了dvr的wifi就直接进入主界面
        if (getDvrWifiName() != null) {
            //进入主界面
            Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            //获得服务器的欢迎图，并判断是否要进入到欢迎界面
            showedPicArr = dataSave.getDataList("showedPicArr");
            getWelcomeImage();
        }
    }

    /**
     * 获得服务器的所有字典
     */
    private void getConfigs() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getConfigs(NetUtils.getHeaders());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray dicts = datas.getJSONArray("dicts");
                    //临时保存configs的数组
                    Map<String, String> configsArr = new HashMap();
                    for (int i = 0; i < dicts.length(); i++) {
                        JSONObject value = (JSONObject) dicts.get(i);
                        configsArr.put(value.getString("keyname"), value.getString("keyvalue"));
                    }
                    // 数据字典保存到APPlication的静态变量中
                    VLCApplication.configsDictionary.clear();
                    VLCApplication.configsDictionary.putAll(configsArr);
                    //把configsArr保存到本地
                    List<Map<String, String>> configsList = new ArrayList();
                    configsList.add(configsArr);
                    dataSave.setDataList("configsList", configsList);
                    ToastUtils.showNomalShortToast(GuideViewActivity.this, "您已成功更新数据字典至本地");
                    PreferenceUtil.commitBoolean("isUpdateDic", true);

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

                    JSONObject datas = jsonObject.getJSONObject("datas");
                    // 获取公司欢迎图
                    JSONArray welcomepages = datas.getJSONArray("welcomepages");
                    for (int i = 0; i < welcomepages.length(); i++) {
                        JSONObject value = (JSONObject) welcomepages.get(i);
                        String piclink = value.getString("piclink");
                        urlList.add(piclink);
                    }

                    for (int i = 0; i < urlList.size(); i++) {
                        // 如果保存过的url里面不包含这个字符串
                        if (!showedPicArr.contains(urlList.get(i))) {
                            //保存未展示过的图片
                            urlArray.add(urlList.get(i));
                            //判断是否要进入到欢迎页面，设置为false就是要进入
                            PreferenceUtil.commitBoolean("isInWelcome", true);
                        }

                        //判断是不是最后一个
                        if (i == urlList.size() - 1) {
                            if (PreferenceUtil.getBoolean("isInWelcome", true)) {
                                if (urlArray.size() > 0) {
                                    urlList.clear();
                                    //最多显示5幅欢迎图片
                                    if (urlArray.size() > 5) {
                                        for (int j = 0; j < 5; j++) {
                                            urlList.add(urlArray.get(j));
                                        }
                                    } else {
                                        for (int j = 0; j < urlArray.size(); j++) {
                                            urlList.add(urlArray.get(j));
                                        }
                                    }
                                }
                                //判断是否要进入到欢迎页面，设置为false就是要进入
                                PreferenceUtil.commitBoolean("isInWelcome", false);
                                //添加广告图
                                addView();
                                initoper();
                                addPoint();
                                //结束方法
                                return;
                            } else {
                                //进入记录仪先导界面
                                Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    //进入记录仪先导界面
                    Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    //进入记录仪先导界面
                    Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //进入记录仪先导界面
                Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initoper() {
        // 进入按钮
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
                //进入记录仪先导界面
                Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
                intent.putExtra("selectIndex", "lv_dvr");
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

        if (urlList.size() > 1) {
            //要显示广告图时才显示跳过按钮
            skip.setVisibility(View.VISIBLE);
        } else {
            //显示立即体验按钮
            textView.setVisibility(View.VISIBLE);
        }

        list = new ArrayList<View>();
        // 将imageview添加到view
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        for (int i = 0; i < urlList.size(); i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder() //
                    .cacheOnDisc(true)//是否缓存的本地true 就是缓存到本地
                    .resetViewBeforeLoading(true)//设置下载的图片下载前是否重置，复位
                    .cacheInMemory(true)//设置下载图片是否缓存到内存
                    .imageScaleType(ImageScaleType.NONE)
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置图片解码类型
                    .displayer(new FadeInBitmapDisplayer(300))//设置用户加载图片的task(这里是渐现)
                    .build();
            ImageLoader.getInstance().displayImage(urlList.get(i), iv, defaultDisplayImageOptions);
            list.add(iv);
            showedPicArr.add(urlList.get(i));
        }
        // 保存已经显示过的url
        dataSave.setDataList("showedPicArr", showedPicArr);
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
        if (llPoint.getChildAt(0) != null) {
            llPoint.getChildAt(0).setBackgroundResource(R.mipmap.pagecontrol_current);
        }

        // 如果只有一个图片  小圆点隐藏
        if (urlList.size() == 1) {
            llPoint.setVisibility(View.GONE);
        }

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
        if (TimeUtils.isFastClick()) {
            return;
        }
        //进入记录仪先导界面
        Intent intent = new Intent(GuideViewActivity.this, MainActivity.class);
        intent.putExtra("selectIndex", "lv_dvr");
        startActivity(intent);
        finish();
    }

}