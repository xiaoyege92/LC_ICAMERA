package com.example.administrator.lc_dvr.module.lc_dvr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.StringRequest;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.base.VorangeType;
import com.example.administrator.lc_dvr.bean.BannerPicture;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.constant.Constant;
import com.example.administrator.lc_dvr.common.retrofit.Api;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.XmlToJson;
import com.example.administrator.lc_dvr.module.LocalVideoFileActivity;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.VorangeDvrFiles;
import com.example.administrator.lc_dvr.module.lc_dvr_recording.VorangeDvrRecording;
import com.example.administrator.lc_dvr.module.lc_dvr_setting.VorangeDvrSetting;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.videolan.libvlc.VLCApplication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by yangboru on 2017/11/2.
 */

public class VorangeDVR extends BaseFragment {

    private SwipeMenuListView lc_dvr_wifi_list;
    private CommonAdapter adapter;
    private ArrayList<Object> wifiNameList;
    private List<DvrWifiName> allWifiName;
    private RadioButton rb_local_file;
    private RadioButton rb_wifi_disconnect; // 断开wifi
    private NormalDialog dialog;
    private String[] mStringBts;
    private KProgressHUD kProgressHUD;
    private Banner banner;
    private List<String> adsImageArray;
    private RelativeLayout dvrTip;
    private RelativeLayout rl_turn_left;

    private IntentFilter intentFilter;
    private WIFIReceiver wifiReceiver;
    private List<String> adsTitTleArray;
    private List<String> adsLinkArray;
    private ListDataSave dataSave;
    private Timer wifiTimer;
    private TextView tv_setting;
    private AsyncTask imageTask;

    private int current;
    private boolean isFreshImage = true;//是否实时刷新图片

    private static boolean isSyncTime = true;//是否同步时间

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        //如果是连接上了dvr的wifi就开启录像,并开启定时
        if (getDvrWifiName(getActivity().getApplicationContext()) != null) {
            //开始录像
            startRec();

            if (wifiTimer == null) {
                wifiTimer = new Timer();
                wifiTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //如果断开了dvr的wifi就跳转到失败的页面
                        if (getDvrWifiName(getActivity().getApplicationContext()) == null) {
                            wifiHandler.sendEmptyMessage(0);
                        }
                    }
                }, 1000, 1000);
            }
        }
        //是否已加载了视频数据
        PreferenceUtil.commitInt("isVideoData", 0);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        if (imageTask != null) {
            imageTask.cancel(true);
            imageTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageTask != null) {
            imageTask.cancel(true);
            imageTask = null;
        }
        if (wifiReceiver != null) {
            getActivity().unregisterReceiver(wifiReceiver);
        }

    }

    @Override
    protected int setViewId() {
        return R.layout.vorange_dvr;
    }

    @Override
    protected void findView(View view) {
        lc_dvr_wifi_list = (SwipeMenuListView) view.findViewById(R.id.lc_dvr_wifi_list);
        rb_wifi_disconnect = (RadioButton) view.findViewById(R.id.rb_wifi_disconnect);
        rb_local_file = (RadioButton) view.findViewById(R.id.rb_local_file);
        banner = (Banner) view.findViewById(R.id.banner);
        dvrTip = (RelativeLayout) view.findViewById(R.id.dvrTip);

        tv_setting = (TextView) view.findViewById(R.id.tv_setting);
        rl_turn_left = (RelativeLayout) view.findViewById(R.id.rl_turn_left);
    }

    @Override
    protected void init() {

        tv_setting.setText("3.点击“");
        SpannableString clickString = new SpannableString("连接行车记录设备");
        clickString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                Intent intent = new Intent();
                intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                intent.putExtra("extra_prefs_show_button_bar", true);
                intent.putExtra("wifi_enable_next_on_connect", true);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getActivity(), R.color.primary_dark));//设置颜色
                ds.setUnderlineText(false);
            }
        }, 0, clickString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_setting.append(clickString);
        tv_setting.append("”，在系统设置中与以“VORANGE”开头的Wifi建立连接，密码：12345678");
        tv_setting.setMovementMethod(LinkMovementMethod.getInstance());

        tv_setting.setLongClickable(false);

        dataSave = new ListDataSave(getContext(), "baiyu");

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        wifiReceiver = new WIFIReceiver();
        getActivity().registerReceiver(wifiReceiver, intentFilter);

        //用来保存广告图的数组
        adsImageArray = new ArrayList<>();

        //用来保存广告标题的数组
        adsTitTleArray = new ArrayList<>();

        //用来保存广告链接的数组
        adsLinkArray = new ArrayList<>();

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        //保存设备wifi的名字
        wifiNameList = new ArrayList<>();

        adapter = new CommonAdapter(this.getActivity(), wifiNameList, R.layout.vorange_dvr_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {
                helper.setText(R.id.wifi_name, wifiNameList.get(position).toString());
                if (VLCApplication.getDvrWifiName() != null) {
                    if (VLCApplication.getDvrWifiName().contains("\"")) {
                        if (VLCApplication.getDvrWifiName().replace("\"", "").replace("\"", "").equals(wifiNameList.get(position).toString())) {
                            helper.setImageResource(R.id.wifi_icon, R.mipmap.wifi_blue);
                        }
                    } else {

                    }
                } else {
                    helper.setImageResource(R.id.wifi_icon, R.mipmap.wifi_gray);
                }
            }
        };

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // Create different menus depending on the view type
                switch (menu.getViewType()) {
                    case 0:
                        createMenu1(menu);
                        break;
                    case 1:
                        createMenu2(menu);
                        break;
                    case 2:
                        createMenu3(menu);
                        break;
                }
            }
        };
        //为列表设置创建器
        lc_dvr_wifi_list.setMenuCreator(creator);

        lc_dvr_wifi_list.setAdapter(adapter);

        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());

        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.RIGHT);

        //如果是连接上了dvr的wifi就显示wifi列表，否则显示广告图
        if (getDvrWifiName() != null) {

            //同步dvr的状态
            getDvrState();

            rb_wifi_disconnect.setVisibility(View.VISIBLE);

            banner.setVisibility(View.GONE);
            dvrTip.setVisibility(View.GONE);
            lc_dvr_wifi_list.setVisibility(View.VISIBLE);

            //刷新wifi列表
            refreshWifi();
            // 只在第一次出现气泡提示
            if (PreferenceUtil.getBoolean("isFirstTip", true)) {
                rl_turn_left.setVisibility(View.VISIBLE);
            }

        } else {
            lc_dvr_wifi_list.setVisibility(View.GONE);
            banner.setVisibility(View.VISIBLE);
            dvrTip.setVisibility(View.VISIBLE);

            rb_wifi_disconnect.setVisibility(View.GONE);
            isFreshImage = true;
            getAdsImageByAsyc();

            if (rl_turn_left.getVisibility() == View.VISIBLE) {
                rl_turn_left.setVisibility(View.GONE);
            }
        }

        //获得服务器的所有字典
        List<Map<String, String>> configsList = dataSave.getDataList("configsList");
        if (!configsList.isEmpty()) {
            VLCApplication.configsDictionary = configsList.get(0);
        }
    }

    /**
     * 同步dvr的状态
     */
    private void getDvrState() {

        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.DVR_STATE, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    JSONArray status = function.getJSONArray("Status");
                    if (VLCApplication.getVorangeType() == VorangeType.C1) {
                        //获得画质的状态
                        int qualityStatus = status.getInt(2);
                        if (qualityStatus == 1) {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("picture", "720");
                        } else {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("picture", "1080");
                        }

                        //获得声音的状态
                        int voiceStatus = status.getInt(7);
                        if (voiceStatus == 1) {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("audio", "on");
                        } else {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("audio", "off");
                        }
                    } else {
                        //获得画质的状态
                        int qualityStatus = status.getInt(3);
                        if (qualityStatus == 1) {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("picture", "720");
                        } else {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("picture", "1080");
                        }

                        //获得声音的状态
                        int voiceStatus = status.getInt(13);
                        if (voiceStatus == 1) {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("audio", "on");
                        } else {
                            //保存当前的声音状态
                            PreferenceUtil.commitString("audio", "off");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    class WIFIReceiver extends BroadcastReceiver {
        //通知处理函数
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果是连接上了dvr的wifi就显示wifi列表，否则显示广告图
            if (getDvrWifiName() != null) {

                //同步dvr的状态
                getDvrState();

                banner.setVisibility(View.GONE);
                dvrTip.setVisibility(View.GONE);
                lc_dvr_wifi_list.setVisibility(View.VISIBLE);
                rb_wifi_disconnect.setVisibility(View.VISIBLE);
                //刷新wifi列表
                refreshWifi();
                // 如果连接的是小C或小D
                if (isSyncTime && (VLCApplication.getVorangeType() == VorangeType.D1 || VLCApplication.getVorangeType() == VorangeType.C1)) {
                    setDVRData();
                }
                // 只有第一次连接的时候才会有提示
                if (PreferenceUtil.getBoolean("isFirstTip", true)) {
                    rl_turn_left.setVisibility(View.VISIBLE);
                }
            } else {
                lc_dvr_wifi_list.setVisibility(View.GONE);
                rb_wifi_disconnect.setVisibility(View.GONE);
                banner.setVisibility(View.VISIBLE);
                dvrTip.setVisibility(View.VISIBLE);

                isFreshImage = false;
                getAdsImageByAsyc();

                if (rl_turn_left.getVisibility() == View.VISIBLE) {
                    rl_turn_left.setVisibility(View.GONE);
                }
            }
        }
    }

    private void createMenu2(SwipeMenu menu) {
        // 创建“删除”项
        SwipeMenuItem deleteItem = new SwipeMenuItem(
                VorangeDVR.this.getContext().getApplicationContext());
        deleteItem.setBackground(new ColorDrawable(Color.RED));
        deleteItem.setWidth(dp2px(70));
        deleteItem.setTitle(R.string.lc_delete);
        deleteItem.setTitleSize(18);
        deleteItem.setTitleColor(Color.WHITE);
        // 将创建的菜单项添加进菜单中
        menu.addMenuItem(deleteItem);
    }

    private void createMenu1(SwipeMenu menu) {
        // 创建“预览”项
        SwipeMenuItem previewItem = new SwipeMenuItem(
                VorangeDVR.this.getContext().getApplicationContext());
        previewItem.setBackground(new ColorDrawable(Color.GRAY));
        previewItem.setWidth(dp2px(70));
        previewItem.setTitle(R.string.lc_preview);
        previewItem.setTitleSize(18);
        previewItem.setTitleColor(Color.BLACK);
        // 将创建的菜单项添加进菜单中
        menu.addMenuItem(previewItem);

        // 创建“设置”项
        SwipeMenuItem settingItem = new SwipeMenuItem(
                VorangeDVR.this.getContext().getApplicationContext());
        settingItem.setBackground(new ColorDrawable(Color.LTGRAY));
        settingItem.setWidth(dp2px(70));
        settingItem.setTitle(R.string.lc_setting);
        settingItem.setTitleSize(18);
        settingItem.setTitleColor(Color.BLACK);
        // 将创建的菜单项添加进菜单中
        menu.addMenuItem(settingItem);

    }

    private void createMenu3(SwipeMenu menu) {
        // 创建“设置”项
        SwipeMenuItem settingItem = new SwipeMenuItem(
                VorangeDVR.this.getContext().getApplicationContext());
        settingItem.setBackground(new ColorDrawable(Color.LTGRAY));
        settingItem.setWidth(dp2px(70));
        settingItem.setTitle(R.string.lc_setting);
        settingItem.setTitleSize(18);
        settingItem.setTitleColor(Color.BLACK);
        // 将创建的菜单项添加进菜单中
        menu.addMenuItem(settingItem);
    }

    /**
     * 将dp转换为px
     *
     * @param value
     * @return
     */
    private int dp2px(int value) {
        // 第一个参数为我们待转的数据的单位，此处为 dp（dip）
        // 第二个参数为我们待转的数据的值的大小
        // 第三个参数为此次转换使用的显示量度（Metrics），它提供屏幕显示密度（density）和缩放信息
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    /**
     * 设置wifi列表
     */
    private void setWifiList() {
        //先清空一下数据

        if (VLCApplication.getDvrWifiName() != null) {
            wifiNameList.clear();
            allWifiName = DataSupport.findAll(DvrWifiName.class);

            for (int i = 0; i < allWifiName.size(); i++) {

                if (allWifiName.get(i).getWifiName() != null && allWifiName.get(i).getWifiName().equals(VLCApplication.getDvrWifiName())) {
                    return;
                }
            }
            //如果当前连接的wifi不存在数据库当中就保存到数据库
            DvrWifiName wifiName = new DvrWifiName();
            wifiName.setWifiName(VLCApplication.getDvrWifiName());
            wifiName.save();
        }
    }

    @Override
    protected void initEvents() {
        //点击轮播图时的响应
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent intent = new Intent(getContext(), Advertisement.class);
                //传送当前广告图的标题和链接
                intent.putExtra("adsTitle", adsTitTleArray.get(position));
                intent.putExtra("adsLink", adsLinkArray.get(position));
                startActivity(intent);
            }
        });
        //点击本地文件
        rb_local_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
                Intent intent = new Intent(VorangeDVR.this.getContext(), LocalVideoFileActivity.class);
                startActivity(intent);

            }
        });
        rb_wifi_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 断开wifi

                showTipDialog("执行本操作，设备wifi将会断开，确认继续吗？");

                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                VLCApplication.wifiManager.setWifiEnabled(false);
                            }
                        },
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        });

            }
        });
        //为ListView设置菜单项点击监听器，监听菜单项的点击事件
        lc_dvr_wifi_list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        if (menu.getViewType() == 0) {
                            if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()) != null) {
                                System.out.println("VLCApplication.getDvrWifiName()" + VLCApplication.getDvrWifiName());
                                if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()).replace("\"", "").replace("\"", "").equals(wifiNameList.get(position).toString())) {
                                    Intent intent = new Intent(VorangeDVR.this.getContext(), VorangeDvrRecording.class);
                                    intent.putExtra("isPreview", true);
                                    startActivity(intent);
                                } else {

                                    Toast.makeText(VorangeDVR.this.getContext(), R.string.device_no_connected, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(VorangeDVR.this.getContext(), R.string.device_no_connected, Toast.LENGTH_SHORT).show();
                            }
                        } else if (menu.getViewType() == 2) {
                            //跳转到设置界面
                            Intent intent2 = new Intent(VorangeDVR.this.getContext(), VorangeDvrSetting.class);
                            startActivity(intent2);
                        } else {
                            if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()) != null) {
                                if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()).replace("\"", "").replace("\"", "").equals(wifiNameList.get(position).toString())) {
                                    Toast.makeText(VorangeDVR.this.getContext(), R.string.cannot_delete_device, Toast.LENGTH_SHORT).show();
                                } else {
                                    allWifiName = DataSupport.findAll(DvrWifiName.class);
                                    for (int i = 0; i < allWifiName.size(); i++) {
                                        if (allWifiName.get(i).getWifiName() != null && allWifiName.get(i).getWifiName().replace("\"", "").replace("\"", "").equals(wifiNameList.get(position))) {
                                            DataSupport.delete(DvrWifiName.class, allWifiName.get(i).getId());
                                            break;
                                        }
                                    }
                                    //刷新wifi列表
                                    refreshWifi();
                                }
                            } else {
                                allWifiName = DataSupport.findAll(DvrWifiName.class);
                                for (int i = 0; i < allWifiName.size(); i++) {
                                    if (allWifiName.get(i).getWifiName() != null && allWifiName.get(i).getWifiName().replace("\"", "").replace("\"", "").equals(wifiNameList.get(position))) {
                                        DataSupport.delete(DvrWifiName.class, allWifiName.get(i).getId());
                                        break;
                                    }
                                }
                                //刷新wifi列表
                                refreshWifi();

                            }
                        }
                        break;
                    case 1:

                        if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()) != null) {
                            if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()).replace("\"", "").replace("\"", "").equals(wifiNameList.get(position).toString())) {

                                if (VLCApplication.configsDictionary.get("app-a-005") != null) {
                                    showTipDialog(VLCApplication.configsDictionary.get("app-a-005"));
                                } else {
                                    showTipDialog("执行本操作，设备录像将被暂停，确认继续吗？");
                                }

                                dialog.setOnBtnClickL(
                                        new OnBtnClickL() {
                                            @Override
                                            public void onBtnClick() {
                                                dialog.dismiss();
                                                //弹出加载对话框
                                                showProgress("请稍候，正在打开设置界面...");
                                                //停止录像
                                                stopRec(2);
                                            }
                                        },
                                        new OnBtnClickL() {
                                            @Override
                                            public void onBtnClick() {
                                                dialog.dismiss();
                                            }
                                        });
                            } else {
                                Toast.makeText(VorangeDVR.this.getContext(), R.string.device_no_connected, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(VorangeDVR.this.getContext(), R.string.device_no_connected, Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 2:
                        current = position;
                        showTipDialog("您确认要删除所选设备吗？");
                        dialog.setOnBtnClickL(
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();

                                        allWifiName = DataSupport.findAll(DvrWifiName.class);
                                        for (int i = 0; i < allWifiName.size(); i++) {
                                            if (allWifiName.get(i).getWifiName() != null && allWifiName.get(i).getWifiName().replace("\"", "").replace("\"", "").equals(wifiNameList.get(current))) {
                                                DataSupport.delete(DvrWifiName.class, allWifiName.get(i).getId());
                                                break;
                                            }
                                        }
                                        //刷新wifi列表
                                        refreshWifi();
                                    }
                                },
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                    }
                                });

                        break;
                }
                return false;
            }
        });

        lc_dvr_wifi_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
                if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()) != null) {
                    if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()).replace("\"", "").replace("\"", "").equals(wifiNameList.get(position).toString())) {
                        if (VLCApplication.getVorangeType() == VorangeType.B1) {
                            //小B===========
                            if (VLCApplication.getDvrWifiName(getActivity().getApplicationContext()).replace("\"", "").replace("\"", "").startsWith("VORANGE_B1")) {

                                Intent intent = new Intent(VorangeDVR.this.getContext(), VorangeDvrFiles.class);
                                PreferenceUtil.commitBoolean("isReport", false);
                                startActivity(intent);
                            }
                        } else if (VLCApplication.getVorangeType() == VorangeType.C1 || VLCApplication.getVorangeType() == VorangeType.D1) {

                            if (VLCApplication.configsDictionary.get("app-a-000") != null) {
                                showTipDialog(VLCApplication.configsDictionary.get("app-a-000"));
                            } else {
                                showTipDialog("执行本操作，设备录像将被暂停，确认继续吗？");
                            }
                            dialog.setOnBtnClickL(
                                    new OnBtnClickL() {
                                        @Override
                                        public void onBtnClick() {
                                            dialog.dismiss();
                                            //弹出加载对话框
                                            showProgress(getString(R.string.filesManagerNow));
                                            //检测SD状态并停止录像
                                            checkSDAndStopRec();
                                        }
                                    },
                                    new OnBtnClickL() {
                                        @Override
                                        public void onBtnClick() {
                                            dialog.dismiss();
                                        }
                                    });
                        }

                    } else {
                        if (VLCApplication.configsDictionary.get("app-a-010") != null) {
                            showTipDialog(VLCApplication.configsDictionary.get("app-a-010"));
                        } else {
                            showTipDialog("当前未连接此设备，现在就去建立连接吗？");
                        }
                        dialog.setOnBtnClickL(
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                        //进入到系统的wifi设置页面
                                        VLCApplication.wifiManager.setWifiEnabled(true);//打开wifi
                                        Intent intent = new Intent();
                                        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                                        intent.putExtra("extra_prefs_show_button_bar", true);
                                        intent.putExtra("wifi_enable_next_on_connect", true);
                                        startActivity(intent);
                                    }
                                },
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                    }
                                });
                    }
                } else {
                    if (VLCApplication.configsDictionary.get("app-a-010") != null) {
                        showTipDialog(VLCApplication.configsDictionary.get("app-a-010"));
                    } else {
                        showTipDialog("当前未连接此设备，现在就去建立连接吗？");
                    }
                    dialog.setOnBtnClickL(
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                    //进入到系统的wifi设置页面
                                    VLCApplication.wifiManager.setWifiEnabled(true);//打开wifi
                                    Intent intent = new Intent();
                                    intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                                    intent.putExtra("extra_prefs_show_button_bar", true);
                                    intent.putExtra("wifi_enable_next_on_connect", true);
                                    startActivity(intent);
                                }
                            },
                            new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                }
                            });
                }

            }
        });
        rl_turn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rl_turn_left.getVisibility() == View.VISIBLE) {
                    rl_turn_left.setVisibility(View.GONE);
                    PreferenceUtil.commitBoolean("isFirstTip", false);
                }

            }
        });
    }

    private void refreshWifi() {
        //设置wifi列表
        setWifiList();
        wifiNameList.clear();
        if (VLCApplication.getDvrWifiName() != null) {
            //获得数据库中保存的wifi列表
            allWifiName = DataSupport.findAll(DvrWifiName.class);
            //当前连接的wifi显示在首位
            if (VLCApplication.getDvrWifiName().contains("\"")) {
                wifiNameList.add(VLCApplication.getDvrWifiName().replace("\"", "").replace("\"", ""));
            }

            for (int i = 0; i < allWifiName.size(); i++) {
                if (VLCApplication.getDvrWifiName() != null && allWifiName.get(i).getWifiName() != null
                        && VLCApplication.getDvrWifiName().contains("\"") && allWifiName.get(i).getWifiName().contains("\"")) {
                    if (!VLCApplication.getDvrWifiName().replace("\"", "").replace("\"", "").equals(allWifiName.get(i).getWifiName().replace("\"", "").replace("\"", ""))) {
                        wifiNameList.add(allWifiName.get(i).getWifiName().replace("\"", "").replace("\"", ""));
                    }
                }

            }
        } else {
            //获得数据库中保存的wifi列表
            allWifiName = DataSupport.findAll(DvrWifiName.class);
            for (int i = 0; i < allWifiName.size(); i++) {
                if (allWifiName.get(i).getWifiName() != null && allWifiName.get(i).getWifiName().contains("\"")) {
                    wifiNameList.add(allWifiName.get(i).getWifiName().replace("\"", "").replace("\"", ""));
                }
            }
        }
        lc_dvr_wifi_list.setAdapter(adapter);
    }

    /**
     * 检测sd卡，如果有sd卡，就停止录像
     */
    private void checkSDAndStopRec() {
        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.CARD_STATE, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int value = function.getInt("Value");
                    if (value == 1) {
                        //停止录像
                        stopRec(1);
                    } else {
                        //关闭加载对话框
                        if (kProgressHUD != null) {
                            kProgressHUD.dismiss();
                        }
                        Toast.makeText(VorangeDVR.this.getContext(), R.string.noSDCard, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
//                if (VLCApplication.configsDictionary.get("app-z-010") != null) {
//                    Toast.makeText(VorangeDVR.this.getContext(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(VorangeDVR.this.getContext(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
//                }
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    /**
     * 开始录像
     */
    private void startRec() {
        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.START_MOVIE_RECORD, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int status = function.getInt("Status");
                    if (status == 0) {
                        //保存录像状态
                        PreferenceUtil.commitInt("automatic_rec", 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    /**
     * 停止录像
     */
    private void stopRec(final int action) {
        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.STOP_MOVIE_RECORD, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int status = function.getInt("Status");
                    if (status == 0) {
                        //保存录像状态
                        PreferenceUtil.commitInt("automatic_rec", 0);
                        //关闭加载对话框
                        kProgressHUD.dismiss();

                        switch (action) {
                            case 1:
                                //跳转到文件管理界面
                                Intent intent = new Intent(VorangeDVR.this.getContext(), VorangeDvrFiles.class);
                                PreferenceUtil.commitBoolean("isReport", false);
                                startActivity(intent);
                                break;
                            case 2:
                                //跳转到设置界面
                                Intent intent2 = new Intent(VorangeDVR.this.getContext(), VorangeDvrSetting.class);
                                startActivity(intent2);
                                break;
                        }

                    } else {
                        //关闭加载对话框
                        if (kProgressHUD != null) {
                            kProgressHUD.dismiss();
                        }
                        Toast.makeText(VorangeDVR.this.getContext(), R.string.stopRecFail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
//                if (VLCApplication.configsDictionary.get("app-z-010") != null) {
//                    Toast.makeText(VorangeDVR.this.getContext(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(VorangeDVR.this.getContext(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
//                }
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    //wifi断开之后要用到的Handler
    Handler wifiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //开始录像
            startRec();
            //显示广告图
            lc_dvr_wifi_list.setVisibility(View.GONE);
            banner.setVisibility(View.VISIBLE);
            dvrTip.setVisibility(View.VISIBLE);
            if (wifiTimer != null) {
                wifiTimer.cancel();
                wifiTimer = null;
            }
            isFreshImage = false;
            getAdsImageByAsyc();

        }
    };

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(VorangeDVR.this.getContext());
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(VorangeDVR.this.getContext());
        dialog.title(getString(R.string.tip));
        dialog.isTitleShow(true)//
                .cornerRadius(5)//
                .content(lable)//
                .contentGravity(Gravity.CENTER)//
                .btnTextSize(15.5f, 15.5f)//
                .widthScale(0.85f)//
                .btnText(mStringBts)
                .btnTextColor(new int[]{ContextCompat.getColor(getActivity(), R.color.primary), ContextCompat.getColor(getActivity(), R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void loadData() {

    }

    // 开启异步任务加载图片
    private void getAdsImageByAsyc() {


        //先清空一下数据
        adsImageArray.clear();
        adsTitTleArray.clear();
        adsLinkArray.clear();
        if (null == VLCApplication.bannerPictureList || 0 == VLCApplication.bannerPictureList.size()) {
            imageTask = new AdsImageTask();
            imageTask.execute();
        } else {
            for (int i = 0; i < VLCApplication.bannerPictureList.size(); i++) {
                adsImageArray.add(VLCApplication.bannerPictureList.get(i).getPiclink());
                adsTitTleArray.add(VLCApplication.bannerPictureList.get(i).getTitle());
                adsLinkArray.add(VLCApplication.bannerPictureList.get(i).getUrl());
            }
            //设置图片集合
            if (adsImageArray != null && adsImageArray.size() != 0) {
                banner.setImages(adsImageArray);
                //banner设置方法全部调用完毕时最后调用
                banner.start();
            }
        }


    }

    /**
     * 获得服务器的广告图
     */
    private void getAdsImage() {

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
                    //先清空一下数据
                    adsImageArray.clear();
                    adsTitTleArray.clear();
                    adsLinkArray.clear();
                    VLCApplication.bannerPictureList = new ArrayList<>();
                    JSONObject datas = jsonObject.getJSONObject("datas");
                    JSONArray ads = datas.getJSONArray("ads");
                    for (int i = 0; i < ads.length(); i++) {
                        JSONObject value = (JSONObject) ads.get(i);
                        String piclink = value.getString("piclink");
                        String title = value.getString("title");
                        String url = value.getString("url");
                        adsImageArray.add(piclink);
                        adsTitTleArray.add(title);
                        adsLinkArray.add(url);

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

    private class AdsImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            if (!isFreshImage) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            getAdsImage();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //设置图片集合
            if (adsImageArray != null && adsImageArray.size() != 0) {
                banner.setImages(adsImageArray);
                //banner设置方法全部调用完毕时最后调用
                banner.start();
            }

        }
    }

    /**
     * 设置记录仪的data
     */
    private void setDVRData() {
        String date = getPhoneDate();
        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.SET_DATE + date, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int status = function.getInt("Status");
                    if (status == 0) {
                        setDVRTime();
                    } else {
                        //关闭加载对话框
//                        kProgressHUD.dismiss();
                        Toast.makeText(getActivity(), R.string.synchronizationTimeFail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
//                kProgressHUD.dismiss();
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    /**
     * 设置记录仪的Time
     */
    private void setDVRTime() {
        String time = getPhoneTime();
        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.SET_TIME + time, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int status = function.getInt("Status");
                    if (status == 0) {
                        if (isSyncTime) {
                            isSyncTime = false;
                            Toast.makeText(getActivity(), R.string.synchronizationTimeSuccess, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        //关闭加载对话框
//                        kProgressHUD.dismiss();
                        Toast.makeText(getActivity(), R.string.synchronizationTimeFail, Toast.LENGTH_SHORT).show();
                        isSyncTime = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
//                kProgressHUD.dismiss();
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    /**
     * 获得当前的日期
     *
     * @return
     */
    private String getPhoneDate() {
        SimpleDateFormat formatterdate = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());
        return formatterdate.format(curDate);
    }

    /**
     * 获得当前的时间
     *
     * @return
     */
    private String getPhoneTime() {
        SimpleDateFormat formattertime = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formattertime.format(curDate);
    }

}