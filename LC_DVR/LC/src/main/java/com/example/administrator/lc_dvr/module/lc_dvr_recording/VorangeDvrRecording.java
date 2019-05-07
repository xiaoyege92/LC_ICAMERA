package com.example.administrator.lc_dvr.module.lc_dvr_recording;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.StringRequest;
import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.constant.Constant;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.XmlToJson;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by yangboru on 2017/11/3.
 */

public class VorangeDvrRecording extends Activity {

    private SurfaceView lc_http_player;
    private static final int CONNECT_TIMEOUT = 10000;
    private TextView lc_load_data;
    private TextView lc_load_time;
    private Timer loadTimer;
    private RelativeLayout lc_rec_layout;
    private Button lc_fullscreen;
    private Button lc_exit_fullscreen;
    private Resources resources;
    private Configuration config;
    private DisplayMetrics dm;
    private Timer refreshTimer;
    private Timer wifiTimer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.vorange_dvr_recording);// 设置布局内容

        lc_http_player = (SurfaceView) findViewById(R.id.lc_http_player);
        lc_load_data = (TextView) findViewById(R.id.lc_load_data);
        lc_load_time = (TextView) findViewById(R.id.lc_load_time);
        lc_rec_layout = (RelativeLayout) findViewById(R.id.lc_rec_layout);
        lc_fullscreen = (Button) findViewById(R.id.lc_fullscreen);
        lc_exit_fullscreen = (Button) findViewById(R.id.lc_exit_fullscreen);

        resources = getResources();
        config = resources.getConfiguration();
        dm = resources.getDisplayMetrics();

        //用来显示本地时间的Timer
        loadTimer = new Timer();
        loadTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.sendEmptyMessage(1);
            }
        }, 1000, 1000);

        setVlc();
        lc_http_player.setKeepScreenOn(true);//让手机屏幕常亮
        //开始播放http流
        playHttp();

        //每10秒刷新一次画面
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.sendEmptyMessage(2);
            }
        }, 10000, 10000);

        //默认设置为全屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 默认设置为半屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //添加当前的activity
        VLCApplication.addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (wifiTimer == null) {
            wifiTimer = new Timer();
            wifiTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //如果断开了dvr的wifi就跳转到失败的页面
                    if (getDvrWifiName(getApplicationContext()) == null) {
                        //移除所有的activity
                        VLCApplication.removeALLActivity();
                        Intent intent = new Intent(VorangeDvrRecording.this, MainActivity.class);
                        intent.putExtra("selectIndex", "lv_dvr");
                        startActivity(intent);
                        wifiTimer.cancel();
                        wifiTimer = null;
//                        Udriver.udriver.closeDevice();
                    }
                }
            }, 1000, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }

        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.DV_MODE_MOIVE, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());

            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    /**
     * 开始播放http流
     */
    private void playHttp() {

        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.MODE_PHOTO, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int value = function.getInt("Status");
                    if (value == 0) {


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

    //用来显示本地时间的Handler
    Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 1:
                    //显示本地的时间
                    lc_load_data.setText(getPhoneDate());
                    lc_load_time.setText(getPhoneTime());
                    break;
                case 2:
                    //每10秒刷新一次Http流播放器
                    playHttp();
                    break;
            }
        }
    };

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

    /**
     * 退出当前的页面
     *
     * @param view
     */
    public void closeVorangeDvrRecording(View view) {
        if (loadTimer != null) {
            loadTimer.cancel();
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
//        if (lc_http_player != null && lc_http_player()) {
//            lc_http_player.stopPlayback();//停止播放
//        }
        lc_http_player.setKeepScreenOn(false);//不再让手机屏幕常亮
        //退出当前的页面
        finish();
    }

    /**
     * 捕获用户按返回键的动作，设置为用户按返回键直接退出当前的页面
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (loadTimer != null) {
                loadTimer.cancel();
            }
            if (refreshTimer != null) {
                refreshTimer.cancel();
            }
//            if (lc_http_player != null && lc_http_player.isPlaying()) {
//                lc_http_player.stopPlayback();//停止播放
//            }
            lc_http_player.setKeepScreenOn(false);//不再让手机屏幕常亮
            //退出当前的页面
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 全屏设置
     *
     * @param view
     */
    public void setVorangeFullScreen(View view) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 作用是屏幕发生变化时，activity不会重新启动，只会执行这个方法，前提条件是：必须先在清单文件中声明ConfigurationChanged属性
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        String language = PreferenceUtil.getString("language", "cn");
        if (language.equals("english")) {
            config.locale = Locale.ENGLISH;
            resources.updateConfiguration(config, dm);
        } else if (language.equals("german")) {
            //设置语言为德文
            config.locale = Locale.GERMAN;
            resources.updateConfiguration(config, dm);
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lc_rec_layout.setPadding(0, 0, 0, 0);
            lc_exit_fullscreen.setVisibility(View.VISIBLE);
            lc_fullscreen.setVisibility(View.GONE);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            lc_rec_layout.setPadding(dp2px(20), dp2px(100), dp2px(20), dp2px(280));
            lc_exit_fullscreen.setVisibility(View.GONE);
            lc_fullscreen.setVisibility(View.VISIBLE);
        }
        super.onConfigurationChanged(newConfig);
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

    public void setVlc() {


    }

}
