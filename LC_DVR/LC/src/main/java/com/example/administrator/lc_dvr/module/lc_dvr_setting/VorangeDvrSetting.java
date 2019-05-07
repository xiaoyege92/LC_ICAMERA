package com.example.administrator.lc_dvr.module.lc_dvr_setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.StringRequest;
import com.dxing.udriver.Udriver;
import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.base.VorangeType;
import com.example.administrator.lc_dvr.common.constant.Constant;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.XmlToJson;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.dxing.udriver.MainMenuFragment.getWifiConfiguration;
import static com.dxing.udriver.WiFiSDConfiguration.DEFAULT_LOCAL_PATH;
import static com.dxing.udriver.WiFiSDConfiguration.PREF_LOCAL_PATH;
import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by yangboru on 2017/11/4.
 */

public class VorangeDvrSetting extends BaseActivity implements Udriver.FormatListener {
    public final int SCAN_WSD_INTERVAL = 400;

    private KProgressHUD kProgressHUD;
    private NormalDialog dialog;
    private String[] mStringBts;
    private ImageView lc_video_p;
    private ImageView lc_voice;
    private TextView lc_video_tip;
    private TextView wifiName;
    private Timer wifiTimer;
    private RadioButton rb_wifi_disconnect;

    private LinearLayout recodeisLint;
    private LinearLayout recodetypeLint;

    private boolean isLoginResult = true;//与硬件建立socket连接循环
    private WifiManager wifiManager;
    public static String ssid = "";
    public static String ipStr = "";

    @Override
    protected int setViewId() {
        return R.layout.vorange_dvr_setting;
    }

    @Override
    protected void findView() {
        lc_video_p = (ImageView) findViewById(R.id.lc_video_p);
        lc_voice = (ImageView) findViewById(R.id.lc_voice);
        lc_video_tip = (TextView) findViewById(R.id.lc_video_tip);
        wifiName = (TextView) findViewById(R.id.wifiName);
        recodeisLint = (LinearLayout) findViewById(R.id.recodeis_lint);
        recodetypeLint = (LinearLayout) findViewById(R.id.recodetype_lint);

        rb_wifi_disconnect = (RadioButton) findViewById(R.id.rb_wifi_disconnect);
    }

    @Override
    protected void init() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        //根据录像声音的状态设置对应的状态
        String audioState = PreferenceUtil.getString("audio", "off");
        if (audioState.equals("on")) {
            //设置对应的图片
            lc_voice.setImageResource(R.mipmap.voice_on);
        } else {
            //设置对应的图片
            lc_voice.setImageResource(R.mipmap.voice_off);
        }

        //根据画质设置的状态设置对应的图标
        String pictureState = PreferenceUtil.getString("picture", "1080");
        if (pictureState.equals("1080")) {
            //设置对应的图标
            lc_video_p.setImageResource(R.mipmap.video_1080p);
        } else {
            //设置对应的图标
            lc_video_p.setImageResource(R.mipmap.video_720p);
        }

        //给wifi名字赋值
        wifiName.setText(VLCApplication.getDvrWifiName().replace("\"", ""));
        //判断是否为B1
        if (VLCApplication.getVorangeType() == VorangeType.B1) {
            recodeisLint.setVisibility(View.GONE);
            recodetypeLint.setVisibility(View.GONE);

            Udriver.udriver.initialUdriver(VorangeDvrSetting.this.getApplicationContext());

            if (!Udriver.udriver.isConnected()) {
                scanWSD();
            } else if (wifiManager.isWifiEnabled()) {
                scanWSD();
            }
        }
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
                    if (getDvrWifiName() == null) {
                        //移除所有的activity
                        VLCApplication.removeALLActivity();
                        Intent intent = new Intent(VorangeDvrSetting.this, MainActivity.class);
                        intent.putExtra("selectIndex", "lv_dvr");
                        startActivity(intent);
                        wifiTimer.cancel();
                        wifiTimer = null;
                    }
                }
            }, 1000, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        startRec();//开始录像

        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        // 如果断开wifi就closeDevice(false)
        if(VLCApplication.getVorangeType() == VorangeType.B1 && VLCApplication.getDvrWifiName(VorangeDvrSetting.this) == null){
            Udriver.udriver.closeDevice(false);
        }else if(VLCApplication.getVorangeType() == VorangeType.B1) {
            Udriver.udriver.closeDevice(true);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 格式化sd卡
     *
     * @param view
     */
    public void formatSD(View view) {
        if (VLCApplication.configsDictionary.get("app-a-060") != null) {
            showTipDialog(VLCApplication.configsDictionary.get("app-a-060"));
        } else {
            showTipDialog("您确认要删除TF卡上的全部数据吗？");
        }
        dialog.setOnBtnClickL(
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                        if (VLCApplication.getVorangeType() == VorangeType.B1) {

                            //弹出加载对话框
                            if (VLCApplication.configsDictionary.get("app-a-050") != null) {
                                showProgress(VLCApplication.configsDictionary.get("app-a-050"));
                            } else {
                                showProgress("正在为您格式化TF卡");
                            }
                            Udriver.udriver.addFormatListener(VorangeDvrSetting.this);
                            Udriver.udriver.formatSDCard();
                        } else {
                            //弹出加载对话框
                            if (VLCApplication.configsDictionary.get("app-a-050") != null) {
                                showProgress(VLCApplication.configsDictionary.get("app-a-050"));
                            } else {
                                showProgress("正在为您格式化TF卡");
                            }
                            //检查sd卡并格式化sd卡
                            StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.CARD_STATE, new Listener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    JSONObject json = XmlToJson.convertXml2Json(s.toString());
                                    try {
                                        JSONObject function = json.getJSONObject("Function");
                                        int value = function.getInt("Value");
                                        if (value == 1) {
                                            //开始格式化
                                            formatNow();
                                        } else {
                                            //关闭加载对话框
                                            kProgressHUD.dismiss();
                                            if (VLCApplication.configsDictionary.get("app-a-056") != null) {
                                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-056"), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(VorangeDvrSetting.this, "未格式化成功，请稍后再试", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(VolleyError volleyError) {
                                    super.onError(volleyError);
                                    //关闭加载对话框
                                    kProgressHUD.dismiss();
                                }
                            });
                            VLCApplication.queue.add(stringrequest);
                        }
                    }
                },
                new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                });
    }

    /**
     * 开始格式化sd卡
     */
    private void formatNow() {

        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.FORMAT_CARD, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int status = function.getInt("Status");
                    if (status == 0) {
                        //关闭加载对话框
                        kProgressHUD.dismiss();
                        if (VLCApplication.configsDictionary.get("app-a-055") != null) {
                            Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-055"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VorangeDvrSetting.this, "已成功为您格式化TF卡", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //关闭加载对话框
                        kProgressHUD.dismiss();
                        if (VLCApplication.configsDictionary.get("app-a-056") != null) {
                            Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-056"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VorangeDvrSetting.this, "未格式化成功，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
                kProgressHUD.dismiss();
            }
        });
        VLCApplication.queue.add(stringrequest);


    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(VorangeDvrSetting.this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
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
                        kProgressHUD.dismiss();
                        Toast.makeText(VorangeDvrSetting.this, R.string.synchronizationTimeFail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
                kProgressHUD.dismiss();
            }
        });
        VLCApplication.queue.add(stringrequest);
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
                        //关闭加载对话框
                        kProgressHUD.dismiss();
                        Toast.makeText(VorangeDvrSetting.this, R.string.synchronizationTimeSuccess, Toast.LENGTH_SHORT).show();
                    } else {
                        //关闭加载对话框
                        kProgressHUD.dismiss();
                        Toast.makeText(VorangeDvrSetting.this, R.string.synchronizationTimeFail, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                //关闭加载对话框
                kProgressHUD.dismiss();
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    @Override
    protected void initEvents() {
        rb_wifi_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭wifi
                VLCApplication.wifiManager.setWifiEnabled(false);
            }
        });
    }

    @Override
    protected void loadData() {

    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(VorangeDvrSetting.this);
        dialog.title(getString(R.string.tip));
        dialog.isTitleShow(true)//
                .cornerRadius(5)//
                .content(lable)//
                .contentGravity(Gravity.CENTER)//
                .btnTextSize(15.5f, 15.5f)//
                .widthScale(0.85f)//
                .btnText(mStringBts)
                .btnTextColor(new int[]{ContextCompat.getColor(this, R.color.primary), ContextCompat.getColor(this, R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 退出当前的页面
     *
     * @param view
     */
    public void closeVorangeDvrSetting(View view) {
        finish();
    }

    /**
     * 设置声音
     *
     * @param view
     */
    public void setLcVoice(View view) {
        if (PreferenceUtil.getString("audio", "off").equals("off")) {
            //弹出加载对话框
            showProgress(getString(R.string.vioceNowOpen));
            StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.AUDIO_ON, new Listener<String>() {
                @Override
                public void onSuccess(String s) {
                    JSONObject json = XmlToJson.convertXml2Json(s.toString());
                    try {
                        JSONObject function = json.getJSONObject("Function");
                        int status = function.getInt("Status");
                        if (status == 0) {
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            //保存当前的声音状态
                            PreferenceUtil.commitString("audio", "on");
                            //设置对应的图片
                            lc_voice.setImageResource(R.mipmap.voice_on);
                            if (VLCApplication.configsDictionary.get("app-a-041") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-041"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "已成功为您开启录像声音", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            if (VLCApplication.configsDictionary.get("app-a-047") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-047"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "未设置成功，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VolleyError volleyError) {
                    super.onError(volleyError);
                    //关闭加载对话框
                    kProgressHUD.dismiss();
                }
            });
            VLCApplication.queue.add(stringrequest);
        } else {
            //弹出加载对话框
            showProgress(getString(R.string.vioceNowClose));
            StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.AUDIO_OFF, new Listener<String>() {
                @Override
                public void onSuccess(String s) {
                    JSONObject json = XmlToJson.convertXml2Json(s.toString());
                    try {
                        JSONObject function = json.getJSONObject("Function");
                        int status = function.getInt("Status");
                        if (status == 0) {
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            if (VLCApplication.configsDictionary.get("app-a-040") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-040"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "已成功为您关闭录像声音", Toast.LENGTH_SHORT).show();
                            }
                            //保存当前的声音状态
                            PreferenceUtil.commitString("audio", "off");
                            //设置对应的图片
                            lc_voice.setImageResource(R.mipmap.voice_off);
                        } else {
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            if (VLCApplication.configsDictionary.get("app-a-047") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-047"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "未设置成功，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VolleyError volleyError) {
                    super.onError(volleyError);
                    //关闭加载对话框
                    kProgressHUD.dismiss();
                }
            });
            VLCApplication.queue.add(stringrequest);
        }

    }

    /**
     * 设置画质
     *
     * @param view
     */
    public void setLcVideo(View view) {
        if (PreferenceUtil.getString("picture", "1080").equals("1080")) {
            //弹出加载对话框
            showProgress(getString(R.string.settingNow));
            StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.VIDEO_720, new Listener<String>() {
                @Override
                public void onSuccess(String s) {
                    JSONObject json = XmlToJson.convertXml2Json(s.toString());
                    try {
                        JSONObject function = json.getJSONObject("Function");
                        int status = function.getInt("Status");
                        if (status == 0) {
                            //设置对应的图标
                            lc_video_p.setImageResource(R.mipmap.video_720p);
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            //保存当前的声音状态
                            PreferenceUtil.commitString("picture", "720");
                            //隐藏画质提示
                            lc_video_tip.setVisibility(View.GONE);
                            if (VLCApplication.configsDictionary.get("app-a-045") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-045"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "已成功为您将清晰度设为720P", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            if (VLCApplication.configsDictionary.get("app-a-047") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-047"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "未设置成功，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VolleyError volleyError) {
                    super.onError(volleyError);
                    //关闭加载对话框
                    kProgressHUD.dismiss();
                }
            });
            VLCApplication.queue.add(stringrequest);
        } else {
            //弹出加载对话框
            showProgress(getString(R.string.settingNow));
            StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.VIDEO_1080, new Listener<String>() {
                @Override
                public void onSuccess(String s) {
                    JSONObject json = XmlToJson.convertXml2Json(s.toString());
                    try {
                        JSONObject function = json.getJSONObject("Function");
                        int status = function.getInt("Status");
                        if (status == 0) {
                            //设置对应的图标
                            lc_video_p.setImageResource(R.mipmap.video_1080p);
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            //保存当前的声音状态
                            PreferenceUtil.commitString("picture", "1080");
                            //显示画质提示
                            lc_video_tip.setVisibility(View.VISIBLE);
                            if (VLCApplication.configsDictionary.get("app-a-046") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-046"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "已成功为您将清晰度设为1080P", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //关闭加载对话框
                            kProgressHUD.dismiss();
                            if (VLCApplication.configsDictionary.get("app-a-047") != null) {
                                Toast.makeText(VorangeDvrSetting.this, VLCApplication.configsDictionary.get("app-a-047"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VorangeDvrSetting.this, "未设置成功，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VolleyError volleyError) {
                    super.onError(volleyError);
                    //关闭加载对话框
                    kProgressHUD.dismiss();
                }
            });
            VLCApplication.queue.add(stringrequest);
        }
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

    public void scanWSD() {
        Thread monitorThread = new Thread("Monitor") {
            public void run() {
                while (isLoginResult) {
                    try {
                        Thread.sleep(SCAN_WSD_INTERVAL);
                    } catch (Exception e) {
                    }
                    if (!Udriver.udriver.isConnected()) {

                    } else {
                        linkSetting();
                        Udriver.udriver.turnOffUsb();
                        loginResult(0);
                        Udriver.udriver.setOnDeviceFound();
                        break;
                    }
                }
            }
        };
        monitorThread.start();
    }

    @Override
    public void onFormatWriteBusy() {
        Toast.makeText(VorangeDvrSetting.this, "TF卡格式化失败，请稍后再试", Toast.LENGTH_SHORT).show();
        Udriver.udriver.removeFormatListener(this);
        if (kProgressHUD != null) {
            kProgressHUD.dismiss();
        }
    }

    @Override
    public void onFormatProgress(int currentProgress, int maxProgress) {

        if ((currentProgress > 0) && (currentProgress == maxProgress)) {
            Udriver.udriver.removeFormatListener(this);
            if (kProgressHUD != null) {
                kProgressHUD.dismiss();
            }
            Toast.makeText(VorangeDvrSetting.this, "已成功为您格式化TF卡", Toast.LENGTH_SHORT).show();
        } else {
            Log.v("shun", "format progress:" + currentProgress + "/" + maxProgress);
        }
    }

    private void loginResult(int result) {
        if (result == 1) {    //success
            return;
        }
        if (VorangeDvrSetting.this != null) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(VorangeDvrSetting.this);
            String folder = pref.getString(PREF_LOCAL_PATH, DEFAULT_LOCAL_PATH);

            String dirString = Environment.getExternalStorageDirectory().toString();
            File prefFolder = new File(dirString, folder);
            prefFolder.mkdirs();
            try {
                Udriver.udriver.start("", "", folder);
            } catch (Exception e) {
                // FIXME 这都是什么错，乱七八糟
            }

        }

    }

    private void linkSetting() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            ssid = wifiInfo.getSSID();
            if (ssid.matches("..:..:..:..:..:.. ")) {
                List<WifiConfiguration> wifiConfiguration = getWifiConfiguration();
                for (WifiConfiguration wc : wifiConfiguration) {
                    if (wc.status == WifiConfiguration.Status.CURRENT) {
                        if (wc.SSID.indexOf("\"") == 0) {
                            ssid = wc.SSID.substring(1, wc.SSID.length() - 1);
                        } else {
                            ssid = wc.SSID;
                        }
                    }
                }
            } else if (ssid.substring(0, 1).equals("\"") && ssid.substring(ssid.length() - 1).equals("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            int ip = wifiInfo.getIpAddress();
            ipStr = String.format("%d.%d.%d.%d", (ip & 0xFF), (ip >> 8 & 0xFF), (ip >> 16 & 0xFF), (ip >> 24 & 0xFF));
            try {
                Udriver.udriver.setMyIp(ipStr);    //for new data update  notify
            } catch (Exception e) {

            }
        }
    }

}
