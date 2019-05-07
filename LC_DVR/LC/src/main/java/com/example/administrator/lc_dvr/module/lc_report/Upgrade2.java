package com.example.administrator.lc_dvr.module.lc_report;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2018/3/22.
 * <p>
 * 版本开始升级界面
 */

public class Upgrade2 extends Activity {

    private TextView update_content;

    //软件下载的地址
    private String downloadUrl = "";
    private ProgressBar iv_image_clip_left;
    private TextView progress_label;
    private Button confirm_btn;
    private HttpUtils downloadManager;
    private HttpHandler<File> downloadHandler;
    private TextView progress_tip;
    private RelativeLayout progress_layout;
    private View finishView1;
    private View finishView2;
    private boolean forcedUpdate;
    private RelativeLayout closeUpgrade2;
    private TextView tv_version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //表示设置当前的Activity无Title并且全屏,调用这个方法有个限制,即必须在setContentView(R.layout.main)之前调用,否则会抛出异常
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.upgrade2_layout);// 设置布局内容

        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //更新的内容
        update_content = (TextView) findViewById(R.id.update_content);
        //下载提示文字
        progress_tip = (TextView) findViewById(R.id.progress_tip);
        //进度条的百分比
        progress_label = (TextView) findViewById(R.id.progress_label);
        //自定义的进度条
        iv_image_clip_left = (ProgressBar) findViewById(R.id.iv_image_clip_left);
        //进度的layout
        progress_layout = (RelativeLayout) findViewById(R.id.progress_layout);
        //底下确定的按钮
        confirm_btn = (Button) findViewById(R.id.confirm_btn);
        //点击空白处关闭当前页面的view
        finishView1 = (View) findViewById(R.id.finishView1);
        finishView2 = (View) findViewById(R.id.finishView2);
        closeUpgrade2 = (RelativeLayout) findViewById(R.id.closeUpgrade2);
        tv_version = (TextView) findViewById(R.id.tv_version);
        //初始化下载管理器
        downloadManager = new HttpUtils();

        //设置进度的最大值
        iv_image_clip_left.setMax(100);

        Intent intent = getIntent();
        //判断是否是强制升级
        forcedUpdate = intent.getBooleanExtra("forcedUpdate", false);

        //判断是否要升级
        isUpgrade();

        closeUpgrade2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消下载
                if (downloadHandler != null) {
                    downloadHandler.cancel();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消下载
        if (downloadHandler != null) {
            downloadHandler.cancel();
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
                        String mustupdate = jsonObject.getJSONObject("datas").getJSONObject("clientinfo").getString("mustupdate");
                        if ("no".equals(mustupdate)) {
                            closeUpgrade2.setVisibility(View.VISIBLE);
                        }
                        String version = jsonObject.getJSONObject("datas").getJSONObject("clientinfo").getString("version");
                        tv_version.setText("V" + version);
                        //获得更新的内容
                        String content = jsonObject.getJSONObject("datas").getJSONObject("clientinfo").getString("demo");
                        update_content.setText(content);
                        //获得下载的地址
                        downloadUrl = jsonObject.getJSONObject("datas").getJSONObject("clientinfo").getString("url");

                        //设置为可点击
                        confirm_btn.setEnabled(true);
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
     * 关闭当前的页面
     *
     * @param view
     */
    public void finish(View view) {
        if (!forcedUpdate) {
            finish();
        }
    }

    public void finish2(View view) {
        if (!forcedUpdate) {
            finish();
        }
    }


    /**
     * 点击确定按钮时的响应
     *
     * @param view
     */
    public void confirmTap(View view) {

        //设置为不可点击
        finishView1.setEnabled(false);
        finishView2.setEnabled(false);

        //更新的内容
        update_content.setVisibility(View.VISIBLE);
        //下载提示文字
        progress_tip.setVisibility(View.VISIBLE);
        //进度条的百分比
        progress_label.setVisibility(View.VISIBLE);
        //自定义的进度条
        iv_image_clip_left.setVisibility(View.VISIBLE);
        //进度的layout
        progress_layout.setVisibility(View.VISIBLE);
        //底下确定的按钮
        confirm_btn.setVisibility(View.GONE);

        downloadHandler = downloadManager.download(downloadUrl, BitmapUtils.getSDPath() + "/VOC/Cache/旅橙.apk",
                new RequestCallBack<File>() {

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        //设置为可点击
                        finishView1.setEnabled(true);
                        finishView2.setEnabled(true);
                        //下载完成就开始安装apk
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Intent.ACTION_VIEW);
                        String type = "application/vnd.android.package-archive";
                        intent.setDataAndType(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/旅橙.apk")), type);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Toast.makeText(Upgrade2.this, R.string.downloadFail, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLoading(long total, long current,
                                          boolean isUploading) {
                        //显示下载进度
                        iv_image_clip_left.setProgress((int) (((double) (current) / (double) (total)) * 100));
                        progress_label.setText((int) (((double) (current) / (double) (total)) * 100) + "%");
                        super.onLoading(total, current, isUploading);
                    }

                });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //do something.
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

}
