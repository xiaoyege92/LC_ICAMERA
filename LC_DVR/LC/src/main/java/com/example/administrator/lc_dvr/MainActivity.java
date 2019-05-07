package com.example.administrator.lc_dvr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BottomMenu;
import com.example.administrator.lc_dvr.common.utils.ChangeFragmentCallBack;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.lc_dvr.VorangeDVR;
import com.example.administrator.lc_dvr.module.lc_help.OneKeyHelp;
import com.example.administrator.lc_dvr.module.lc_help.OneKeyHelpFragment;
import com.example.administrator.lc_dvr.module.lc_report.MyVorange;
import com.example.administrator.lc_dvr.module.lc_report.Upgrade2;
import com.example.administrator.lc_dvr.module.login_registration.Landing;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;

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

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by yangboru on 2017/11/2.
 * <p>
 * 主页面，包含三个下图标menu
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ChangeFragmentCallBack {
    private BottomMenu lc_dvr;
    private BottomMenu lc_report;
    private BottomMenu lc_help;
    private FragmentManager v4FragmentManager;
    private BaseFragment lastFragment;
    private BottomMenu mLastMenu;
    private long lastTime;
    private boolean isFirst = true;

    private String[] mStringBts;
    private NormalDialog dialog;
    private KProgressHUD kProgressHUD;
    private BottomMenu seleteMenu;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //跳转到更新详情界面
                    Intent intent = new Intent(MainActivity.this, Upgrade2.class);
                    intent.putExtra("forcedUpdate", true);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);// 重新创建的时候不保存上次一次的内容
        setContentView(setViewId());
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findView();
        init();

        // 判断是否需要升级
        if (VLCApplication.isUpdate) {
            VLCApplication.isUpdate = false;
            isUpgrade();
        }

        Intent intent = getIntent();
        String selectIndex = intent.getStringExtra("selectIndex");

        if (selectIndex != null) {
            if (selectIndex.equals("lc_report")) {
                lc_report.performClick();//默认点中这项，相当于手动点击
            } else {
                lc_dvr.onSelete();
                BaseFragment show = null;
                FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
                show = new VorangeDVR();
                if (lastFragment != null) {
                    v4Transaction.hide(lastFragment);
                }
                v4Transaction.add(R.id.lc_neirong, show);
                lastFragment = show;
                v4Transaction.commit();//一个事务被提交了，就不能再用了
                mLastMenu = lc_dvr;
                isSignIn3();
            }
        } else {
            lc_dvr.onSelete();
            BaseFragment show = null;
            FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
            show = new VorangeDVR();
            if (lastFragment != null) {
                v4Transaction.hide(lastFragment);
            }
            v4Transaction.add(R.id.lc_neirong, show);
            lastFragment = show;
            v4Transaction.commit();//一个事务被提交了，就不能再用了
            mLastMenu = lc_dvr;
            isSignIn3();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取消息信息
        getMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public int setViewId() {
        return R.layout.main_activity;
    }

    public void findView() {
        lc_dvr = (BottomMenu) findViewById(R.id.lc_dvr);
        lc_report = (BottomMenu) findViewById(R.id.lc_report);
        lc_help = (BottomMenu) findViewById(R.id.lc_help);
    }

    public void init() {
        //在Android 6.0及以上，如果检查没有权限，需要主动请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE);
            if (permission == PackageManager.PERMISSION_GRANTED) {
                //表示已经授权
            } else {
                //请求wifi权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);
            }
        }

        VLCApplication.addActivity(this);
        Utils.setChangeFragmentCallBack(this);
        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        v4FragmentManager = getSupportFragmentManager();

        lc_dvr.setOnClickListener(this);
        lc_report.setOnClickListener(this);

    }

    /**
     * 作用是：按两次返回键退出
     */
    public void onBackPressed() {
        if (isFirst) {
            Toast.makeText(this, R.string.zaiantuichu, Toast.LENGTH_SHORT).show();
            lastTime = System.currentTimeMillis();
            isFirst = false;
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime <= 2000) {
                System.exit(1);//用这个相当于系统把后台完全杀死了，finish()做不到这个效果
            } else {
                Toast.makeText(this, R.string.zaiantuichu, Toast.LENGTH_SHORT).show();
                lastTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(MainActivity.this);
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
     * 进入到系统wifi页面
     */
    private void disConectWifi() {
        VLCApplication.wifiManager.setWifiEnabled(false);
    }

    /**
     * 点击一键求助按钮时的响应
     *
     * @param view
     */
    public void seekHelp(final View view) {
        if (mLastMenu == lc_help) {
            return;
        }
        if (TimeUtils.isFastClick()) {
            return;
        }
        //判断是否连接上了dvr的wifi
        if (getDvrWifiName() != null) {

            if (VLCApplication.configsDictionary.get("APP-A-035") != null) {
                showTipDialog(VLCApplication.configsDictionary.get("APP-A-035"));
            } else {
                showTipDialog("一键求助功能需要断开当前VORANGE行车记录设备哦，确认吗？");
            }

            dialog.setOnBtnClickL(
                    new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            dialog.dismiss();//为什么执行后没有消失
                            showProgress("拼命加载中");
                            //断开当前的wifi连接
                            disConectWifi();
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            //判断是否登录
                            if (null == PreferenceUtil.getString("tokenid", "") || "".equals(PreferenceUtil.getString("tokenid", ""))) {
                                //移除所有的activity
                                VLCApplication.removeALLActivity();
                                //跳转到登录页面
                                Intent intent = new Intent(MainActivity.this, Landing.class);
                                intent.putExtra("selectIndex", "lc_help");
                                startActivity(intent);
                            } else {
                                seleteMenu = (BottomMenu) view;
                                BottomMenu selete = (BottomMenu) seleteMenu;
                                selete.onSelete();
                                BaseFragment show = null;
                                FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
                                if (lastFragment != null) {
                                    v4Transaction.hide(lastFragment);
                                }
                                show = new OneKeyHelpFragment();
                                v4Transaction.add(R.id.lc_neirong, show);
                                lastFragment = show;
                                v4Transaction.commit();//一个事务被提交了，就不能再用了
                                if (mLastMenu != null && !mLastMenu.equals(selete)) {
                                    mLastMenu.onSuoFang();
                                }
                                mLastMenu = (BottomMenu) seleteMenu;//保存上一次的点击
                            }
                            if(kProgressHUD != null) {
                                kProgressHUD.dismiss();
                            }

                        }
                    },
                    new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            dialog.dismiss();
                        }
                    });

        } else {

            if (!NetUtils.isNetworkConnected(MainActivity.this)) {
                ToastUtils.showNomalShortToast(MainActivity.this, getString(R.string.network_off));
                return;
            }

            //判断是否登录
            if (null == PreferenceUtil.getString("tokenid", "") || "".equals(PreferenceUtil.getString("tokenid", ""))) {
                //移除所有的activity
                VLCApplication.removeALLActivity();
                //跳转到登录页面
                Intent intent = new Intent(MainActivity.this, Landing.class);
                intent.putExtra("selectIndex", "lc_help");
                startActivity(intent);
            } else {
//                showProgress("拼命加载中");
//                isSignIn(view);
                seleteMenu = (BottomMenu) view;
                BottomMenu selete = (BottomMenu) seleteMenu;
                selete.onSelete();
                BaseFragment show = null;
                FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
                if (lastFragment != null) {
                    v4Transaction.hide(lastFragment);
                }
                show = new OneKeyHelpFragment();
                v4Transaction.add(R.id.lc_neirong, show);
                lastFragment = show;
                v4Transaction.commit();//一个事务被提交了，就不能再用了
                if (mLastMenu != null && !mLastMenu.equals(selete)) {
                    mLastMenu.onSuoFang();
                }
                mLastMenu = (BottomMenu) seleteMenu;//保存上一次的点击
            }
        }
    }

    /**
     * 判断登录有没有过期了
     */
    private void isSignIn3() {
        if (PreferenceUtil.getString("tokenid", "") == null
                || "".equals(PreferenceUtil.getString("tokenid", ""))) {
            //移除所有的activity
            VLCApplication.removeALLActivity();
            //跳转到登录页面
            Intent intent = new Intent(MainActivity.this, Landing.class);
            startActivity(intent);
        }
    }

    /**
     * 判断是否要升级
     */
    private void isUpgrade() {
        Map<String, String> map = new HashMap<>();
        //post参数
        map.put("kind", "android");
        Call<ResponseBody> call = RetrofitManager.getInstance().create().updateAPP(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        String version = jsonObject.getJSONObject("datas").getJSONObject("clientinfo").getString("version");
                        String mustupdate = jsonObject.getJSONObject("datas").getJSONObject("clientinfo").getString("mustupdate");
                        //保存当前服务器上的版本号
                        PreferenceUtil.commitString("version", version);
                        try {
                            String versionName = MainActivity.this.getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0).versionName;
                            if (!version.equals(versionName)) {
                                //判断是否要升级
                                PreferenceUtil.commitBoolean("isUpgrade", true);
                                lc_report.showRedCircle();
                                if (mustupdate.equals("yes")) {
                                    Message message = new Message();
                                    message.what = 1;
                                    mHandler.sendMessage(message);
                                }
                            } else {
                                //判断是否要升级
                                PreferenceUtil.commitBoolean("isUpgrade", false);
                                lc_report.setVisibility(View.GONE);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
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

    /**
     * 从服务器获得单位消息数
     */
    private void getMessage() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getUnitMessage(NetUtils.getHeaders(), PreferenceUtil.getString("personcode", ""));
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    String msg = jsonObject.getString("msg");
                    if (msg.equals("操作成功")) {
                        JSONArray jsonArray = jsonObject.getJSONObject("datas").getJSONArray("msgs");
                        int status;
                        int unRead = 0;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            status = jsonArray.getJSONObject(i).getInt("status");
                            if (status == 0) {
                                unRead++;
                            }
                        }
                        if (unRead > 0) {
                            lc_help.showMsg(unRead);
                        } else {
                            lc_help.hideMsg();
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
                lc_help.hideMsg();
            }
        });
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(MainActivity.this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }

    @Override
    public void onClick(final View v) {
        //判断点击的是否是报案页面
        if (v.getId() == R.id.lc_report) {// 如果上次选中的事报案快赔界面就直接返回
            if (TimeUtils.isFastClick()) {
                return;
            }
            if (mLastMenu == lc_report) {
                return;
            }
            //判断是否连接上了dvr的wifi
            if (getDvrWifiName() != null) {

                if (VLCApplication.configsDictionary.get("APP-A-036") != null) {
                    showTipDialog(VLCApplication.configsDictionary.get("APP-A-036"));
                } else {
                    showTipDialog("报案快赔功能需要断开当前VORANGE行车记录设备哦，确认吗？");
                }

                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();

                                showProgress("拼命加载中");
                                //断开当前的wifi连接
                                disConectWifi();
                                try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                seleteMenu = (BottomMenu) v;
                                //判断是否登录
                                if (null == PreferenceUtil.getString("tokenid", "") || "".equals(PreferenceUtil.getString("tokenid", ""))) {
                                    //移除所有的activity
                                    VLCApplication.removeALLActivity();

                                    //跳转到登录页面
                                    Intent intent = new Intent(MainActivity.this, Landing.class);
                                    intent.putExtra("selectIndex", "lc_report");
                                    startActivity(intent);
                                } else {
                                    BottomMenu selete = (BottomMenu) seleteMenu;
                                    selete.onSelete();
                                    BaseFragment show = null;
                                    FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
                                    if (lastFragment != null) {
                                        v4Transaction.hide(lastFragment);
                                    }

                                    show = new MyVorange();
                                    v4Transaction.add(R.id.lc_neirong, show);
                                    lastFragment = show;
                                    v4Transaction.commit();//一个事务被提交了，就不能再用了
                                    if (mLastMenu != null && !mLastMenu.equals(selete)) {
                                        mLastMenu.onSuoFang();
                                    }
                                    mLastMenu = (BottomMenu) seleteMenu;//保存上一次的点击
                                }
                                if(kProgressHUD != null) {
                                    kProgressHUD.dismiss();
                                }

                            }
                        },
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        });

            } else {
                //检查网络是否连接
                if (!NetUtils.isNetworkConnected(MainActivity.this)) {
                    ToastUtils.showNomalShortToast(MainActivity.this, "连接失败，请检查您的网络连接");
                    return;
                }

                seleteMenu = (BottomMenu) v;
                //判断是否登录
                if (null == PreferenceUtil.getString("tokenid", "") || "".equals(PreferenceUtil.getString("tokenid", ""))) {
                    //移除所有的activity
                    VLCApplication.removeALLActivity();
                    //跳转到登录页面
                    Intent intent = new Intent(MainActivity.this, Landing.class);
                    intent.putExtra("selectIndex", "lc_report");
                    startActivity(intent);
                } else {
//                    showProgress("拼命加载中");
//                    isSignIn2();
                    BottomMenu selete = (BottomMenu) seleteMenu;
                    selete.onSelete();
                    BaseFragment show = null;
                    FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
                    if (lastFragment != null) {
                        v4Transaction.hide(lastFragment);
                    }

                    show = new MyVorange();
                    v4Transaction.add(R.id.lc_neirong, show);
                    lastFragment = show;
                    v4Transaction.commit();//一个事务被提交了，就不能再用了
                    if (mLastMenu != null && !mLastMenu.equals(selete)) {
                        mLastMenu.onSuoFang();
                    }
                    mLastMenu = (BottomMenu) seleteMenu;//保存上一次的点击
                }
            }

        } else if (v.getId() == R.id.lc_dvr) {
            if (TimeUtils.isFastClick()) {
                return;
            }
            if (mLastMenu == lc_dvr) {// 如果上次选中的事行车记录界面就直接返回
                return;
            }

            //判断是否登录
            if (null == PreferenceUtil.getString("tokenid", "") || "".equals(PreferenceUtil.getString("tokenid", ""))) {
                //移除所有的activity
                VLCApplication.removeALLActivity();
                //跳转到登录页面
                Intent intent = new Intent(MainActivity.this, Landing.class);
                intent.putExtra("selectIndex", "lc_dvr");
                startActivity(intent);
            } else {
                BottomMenu selete = (BottomMenu) v;
                selete.onSelete();
                BaseFragment show = null;
                FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();

                show = new VorangeDVR();

                v4Transaction.add(R.id.lc_neirong, show);
                if (lastFragment != null) {
                    v4Transaction.hide(lastFragment);
                }
                lastFragment = show;
                v4Transaction.commit();//一个事务被提交了，就不能再用了
                if (mLastMenu != null && !mLastMenu.equals(selete)) {
                    mLastMenu.onSuoFang();
                }
                mLastMenu = (BottomMenu) v;//保存上一次的点击
            }
        }
    }

    @Override
    public void changeFragment() {
        BaseFragment show = null;
        FragmentTransaction v4Transaction = v4FragmentManager.beginTransaction();
        show = new OneKeyHelp();

        v4Transaction.replace(R.id.lc_neirong, show);
        lastFragment = show;
        v4Transaction.commit();//一个事务被提交了，就不能再用了
    }
}
