package com.example.administrator.lc_dvr.module.lc_dvr_files_manager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioButton;

import com.android.volley.Request;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.StringRequest;
import com.dxing.udriver.MainMenuFragment;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.base.VorangeType;
import com.example.administrator.lc_dvr.common.constant.Constant;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ScreenListener;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.common.utils.XmlToJson;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by yangboru on 2017/11/4.
 */

public class VorangeDvrFiles extends BaseActivity {

    //    private View dvr_selector;
//    private View load_selector;
    private FragmentManager v4FragmentManager;
    //    private LinearLayout lc_dvr_video;
//    private LinearLayout lc_load_video;
    private Timer wifiTimer;
    private Timer backTimer;// 监听程序是否被放置后台
    MainMenuFragment mainMenuFragment = new MainMenuFragment();
    //    MainMeanLoadVideo mainMeanLoadVideo = new MainMeanLoadVideo();
    VorangeDvrVideo vorangeDvrVideo = new VorangeDvrVideo();
    //    VorangeLoadVideo vorangeLoadVideo = new VorangeLoadVideo();
    FragmentTransaction v4Transaction;
    HashMap<Integer, Fragment> hashMapFragment = new HashMap<>();
    Fragment mCurrentFrgment;
    private ScreenListener listener;// 用于监听屏幕是否锁屏
    private boolean isCurrentRunningForeground = true;//用来控制应用前后台切换的逻辑

    private String[] mStringBts;
    private NormalDialog dialog;

    private RadioButton rb_wifi_disconnect; // 断开WiFi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //如果录屏完成后点击了查看按钮就跳到本地文件
        if (PreferenceUtil.getInt("is_recorder", 0) == 1) {
//            lc_load_video.performClick();
            //判断是否点击了查看录屏文件按钮
            PreferenceUtil.commitInt("is_recorder", 0);
        }

        Intent intent = getIntent();
        if (intent != null && "is_local_files".equals(intent.getStringExtra("is_local_files"))) {
//            lc_load_video.performClick();
        } else {
            if (wifiTimer == null) {
                wifiTimer = new Timer();
                wifiTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //如果断开了dvr的wifi就跳转到失败的页面
                        if (getDvrWifiName(getApplicationContext()) == null) {
                            //移除所有的activity
//                            VLCApplication.removeALLActivity();
//                            Intent intent = new Intent(VorangeDvrFiles.this, MainActivity.class);
//                            intent.putExtra("selectIndex", "lv_dvr");
//                            startActivity(intent);
                            finish();
                            if (wifiTimer != null) {
                                wifiTimer.cancel();
                                wifiTimer = null;
                            }
                            if (backTimer != null) {
                                backTimer.cancel();
                                backTimer = null;
                            }
//                            Udriver.udriver.closeDevice();
                        }
                    }
                }, 500, 500);
            }
        }
        if (backTimer == null) {
            backTimer = new Timer();
            backTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isCurrentRunningForeground = Utils.isRunningForeground(VorangeDvrFiles.this);
                    if (!isCurrentRunningForeground) {
                        if (hashMapFragment != null) {
                            hashMapFragment.clear();
                        }
                        finish();

                        if (wifiTimer != null) {
                            wifiTimer.cancel();
                            wifiTimer = null;
                        }
                        if (backTimer != null) {
                            backTimer.cancel();
                            backTimer = null;
                        }
                    }
                }
            }, 100, 500);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hashMapFragment.clear();
        if (listener != null) {
            listener.unregister();
        }
        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        if (backTimer != null) {
            backTimer.cancel();
            backTimer = null;
        }
        startRec();//列表页面结束就发送开始录像指令
    }

    @Override
    protected int setViewId() {
        return R.layout.vorange_dvr_files;
    }

    @Override
    protected void findView() {
        rb_wifi_disconnect = (RadioButton) findViewById(R.id.rb_wifi_disconnect);
    }

    @Override
    protected void init() {

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);
        //添加当前的activity
        VLCApplication.addActivity(this);

        v4FragmentManager = getSupportFragmentManager();
        PreferenceUtil.commitInt("is_recorder", 0);

        initFragment();

        listener = new ScreenListener(this);
        listener.register(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                Log.e("ScreenListener", ".....回调...onScreenOn.......");
            }

            @Override
            public void onScreenOff() {
                finish();
                Log.e("ScreenListener", ".....回调...onScreenOff.......");
            }

            @Override
            public void onUserPresent() {
                Log.e("ScreenListener", ".....回调...onUserPresent.......");
            }
        });
    }

    @Override
    protected void initEvents() {
        rb_wifi_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }

    @Override
    protected void loadData() {

    }

    /**
     * 退出当前的页面
     *
     * @param view
     */
    public void closeVorangeDvrFiles(View view) {
        hashMapFragment.clear();
        finish();
    }


//    public void fileSelector(View v) {
//        v4Transaction = v4FragmentManager.beginTransaction();
//        switch (v.getId()) {
//            case R.id.lc_dvr_video://此处也可以和id比较的
//                dvr_selector.setVisibility(View.VISIBLE);
//                load_selector.setVisibility(View.GONE);
//                changeTab(R.id.lc_dvr_video);
//
//                break;
//            case R.id.lc_load_video:
//                //FIXME  当连接小B并且正在下载文件的时候
//                if (VLCApplication.getVorangeType() == VorangeType.B1 && UdriverFileListViewAdapter.isDownloading != -1) {
//
//                    showTipDialog("进入本地文件会停止下载，继续吗？");
//
//                    dialog.setOnBtnClickL(
//                            new OnBtnClickL() {
//                                @Override
//                                public void onBtnClick() {
//                                    dialog.dismiss();
//
//                                    EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_EXIT_B1));
//
//                                    dvr_selector.setVisibility(View.GONE);
//                                    load_selector.setVisibility(View.VISIBLE);
//                                    changeTab(R.id.lc_load_video);
//                                }
//                            },
//                            new OnBtnClickL() {
//                                @Override
//                                public void onBtnClick() {
//                                    dialog.dismiss();
//                                }
//                            });
//                } else if ((VLCApplication.getVorangeType() == VorangeType.C1 && VorangeDvrVideoAdapter.isDownloading != -1)||(VLCApplication.getVorangeType() == VorangeType.D1 && VorangeDvrVideoAdapter.isDownloading != -1)) {
//                    showTipDialog("进入本地文件会停止下载，继续吗？");
//
//                    dialog.setOnBtnClickL(
//                            new OnBtnClickL() {
//                                @Override
//                                public void onBtnClick() {
//                                    dialog.dismiss();
//
//                                    EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_EXIT_C1));
//
//                                    dvr_selector.setVisibility(View.GONE);
//                                    load_selector.setVisibility(View.VISIBLE);
//                                    changeTab(R.id.lc_load_video);
//                                }
//                            },
//                            new OnBtnClickL() {
//                                @Override
//                                public void onBtnClick() {
//                                    dialog.dismiss();
//                                }
//                            });
//                } else {
//                    dvr_selector.setVisibility(View.GONE);
//                    load_selector.setVisibility(View.VISIBLE);
//                    changeTab(R.id.lc_load_video);
//                }
//
//                break;
//        }
//
//        v4Transaction.commit();//一个事务被提交了，就不能再用了
//    }

    private void initFragment() {
        v4Transaction = v4FragmentManager.beginTransaction();
        if (VLCApplication.getVorangeType() == VorangeType.B1) {
//            hashMapFragment.put(R.id.lc_dvr_video, mainMenuFragment);
            v4Transaction.add(R.id.lc_file_layout, mainMenuFragment);
        } else {
//            hashMapFragment.put(R.id.lc_dvr_video, vorangeDvrVideo);
            v4Transaction.add(R.id.lc_file_layout, vorangeDvrVideo);
        }

        v4Transaction.commit();
//        if (VLCApplication.getVorangeType() == VorangeType.B1) {
//            hashMapFragment.put(R.id.lc_load_video, mainMeanLoadVideo);
//        } else {
//            hashMapFragment.put(R.id.lc_load_video, vorangeLoadVideo);
//        }
//        changeTab(R.id.lc_dvr_video);
    }

//    private void changeTab(int index) {
//
//        FragmentTransaction ft = v4FragmentManager.beginTransaction();
//        //判断当前的Fragment是否为空，不为空则隐藏
//        if (null != mCurrentFrgment) {
//            ft.hide(mCurrentFrgment);
//        }
//        //先根据Tag从FragmentTransaction事物获取之前添加的Fragment
//        Fragment fragment = null;
//
//        if (null == fragment) {
//            //如fragment为空，则之前未添加此Fragment。便从集合中取出
//            fragment = hashMapFragment.get(index);
//        }
//        //判断此Fragment是否已经添加到FragmentTransaction事物中
//        if (!fragment.isAdded()) {
//            ft.add(R.id.lc_file_layout, fragment, fragment.getClass().getName());
//        } else {
//            ft.show(fragment);
//
//        }
//        mCurrentFrgment = fragment;
//        ft.commit();
//    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(VorangeDvrFiles.this);
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
}
