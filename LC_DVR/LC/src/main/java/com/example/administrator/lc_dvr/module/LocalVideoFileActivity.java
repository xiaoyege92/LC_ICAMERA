package com.example.administrator.lc_dvr.module;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.GetLoadVideoName;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.PlayerActivity;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.LocalFileInfo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.load_video_thumbnail.MyVideoThumbLoader;
import com.example.administrator.lc_dvr.module.lc_report.ImmediatelyReport;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/09/03
 *   desc   :
 *  version :
 * </pre>
 */
public class LocalVideoFileActivity extends BaseActivity {
    private SwipeMenuListView videoListView;
    private CommonAdapter adapter;
    private List<String> list = new ArrayList<>();
    private String loadVideoName;
    private String loadVideoPath;
    private MyVideoThumbLoader mVideoThumbLoader;
    private GetLoadVideoName getLoadVideoName;
    private String deleteName;
    private NormalDialog dialog;
    private String[] mStringBts;
    private ArrayList<LocalFileInfo> tempList = new ArrayList();
    private SortTask sortTask;
    private KProgressHUD kProgressHUD;
    private RadioButton rb_wifi_disconnect;

    private String shareFileURL;// 分享视频链接
    private String qiniuToken; // 七牛token
    private String strShare = "";

    Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            videoListView.setAdapter(adapter);
            if (list.size() == 0) {
                ToastUtils.showNomalShortToast(LocalVideoFileActivity.this, "本地文件为空");
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // 如果连接行车记录仪wifi，则显示断开连接图标
        if(getDvrWifiName() != null) {
            rb_wifi_disconnect.setVisibility(View.VISIBLE);
        }else {
            rb_wifi_disconnect.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sortTask != null) {
            sortTask.cancel(true);
            sortTask = null;

            if (kProgressHUD != null) {
                kProgressHUD.dismiss();
            }
        }

    }

    @Override
    protected int setViewId() {
        return R.layout.local_video_file_activity;
    }

    @Override
    protected void findView() {
        videoListView = (SwipeMenuListView) findViewById(R.id.phone_video_list);
        rb_wifi_disconnect = (RadioButton) findViewById(R.id.rb_wifi_disconnect);
    }

    @Override
    protected void init() {

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        mVideoThumbLoader = new MyVideoThumbLoader();// 初始化缩略图载入方法

        getLoadVideoName = new GetLoadVideoName();// 初始化本地视频名称载入方法

        File videoDir = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");
        if (!videoDir.exists()) {
            videoDir.mkdirs();//创建目录
        }
        File vocDir = new File(BitmapUtils.getSDPath() + "/VOC/");
        if (!vocDir.exists()) {
            vocDir.mkdirs();//创建目录
        }

        adapter = new CommonAdapter(this, list, R.layout.phone_video_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {
                System.out.println("convert==");
                loadVideoName = list.get(position);

                helper.setText(R.id.phone_video_name, loadVideoName);

//                loadVideoPath = BitmapUtils.getSDPath() + "/B1/FILES/" + loadVideoName;

//                if (loadVideoName.contains(".ts") || loadVideoName.contains(".TS") || loadVideoName.contains(".MP4") || loadVideoName.contains(".MOV") || loadVideoName.contains(".mov")) {
                if (loadVideoName.contains(".mp4")) {
                    loadVideoPath = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + loadVideoName;
                } else {
                    loadVideoPath = BitmapUtils.getSDPath() + "/VOC/" + loadVideoName;
                }
                //获得文件的大小
                File file = new File(loadVideoPath);
                String size = "";
                java.text.SimpleDateFormat datedf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                if (file.exists() && file.isFile()) {
                    long fileS = file.length();
                    DecimalFormat df = new DecimalFormat("#.0");
                    if (fileS < 1024) {
                        size = df.format((double) fileS) + "BT";
                    } else if (fileS < 1048576) {
                        size = df.format((double) fileS / 1024) + "KB";
                    } else if (fileS < 1073741824) {
                        size = df.format((double) fileS / 1048576) + "MB";
                    } else {
                        size = df.format((double) fileS / 1073741824) + "GB";
                    }
                } else if (file.exists() && file.isDirectory()) {
                    size = "";
                } else {
                    size = "0BT";
                }

                //设置对应的视频类型图片标志
                if (loadVideoName.contains(".mp4") && !loadVideoName.contains("demo.mp4")) {
                    helper.setImageResource(R.id.load_video_tag, R.mipmap.recorder_video);
                } else {
                    helper.setImageResource(R.id.load_video_tag, R.mipmap.dvr_video);
                }

                //设置视频的时长
                TextView load_video_time_long = helper.getView(R.id.load_video_time_long);
//                load_video_time_long.setText(getLoadVideoName.getRingDuring(loadVideoPath));
                load_video_time_long.setText("00:00");
                load_video_time_long.setTag(loadVideoPath);
                getLoadVideoName.showThumbByAsynctack(loadVideoPath, load_video_time_long);

                //设置文件的大小
                helper.setText(R.id.phone_video_size, size);
                //设置文件的时间
                helper.setText(R.id.load_video_date, datedf.format(new Date(file.lastModified())));
//                helper.setText(R.id.load_video_date, loadVideoName.substring(0, 4) + "-" + loadVideoName.substring(5, 7) + "-" + loadVideoName.substring(7, 9) + " " + loadVideoName.substring(10, 12) + ":" + loadVideoName.substring(12, 14));

                //设置缩略图
                ImageView phone_video_img = helper.getView(R.id.phone_video_img);
                // FIXME 使用视频第一帧作为视频缩略图
                phone_video_img.setImageResource(R.drawable.nophotos);
                phone_video_img.setTag(loadVideoPath);
//                phone_video_img.setImageBitmap(Utils.getVideoThumbnail(loadVideoPath,480,360,1));
                mVideoThumbLoader.showThumbByAsynctack(loadVideoPath, phone_video_img, loadVideoName);

            }
        };

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // 创建“删除”项
                SwipeMenuItem deleteItem = new SwipeMenuItem(LocalVideoFileActivity.this);
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                deleteItem.setWidth(dp2px(70));
                deleteItem.setTitle(R.string.lc_delete);
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(deleteItem);
                // 创建“分享”项
                SwipeMenuItem shareItem = new SwipeMenuItem(LocalVideoFileActivity.this);
                shareItem.setBackground(new ColorDrawable(Color.LTGRAY));
                shareItem.setWidth(dp2px(70));
                shareItem.setTitle("分享");
                shareItem.setTitleSize(18);
                shareItem.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(shareItem);
                // 创建“立即报案”项
                SwipeMenuItem reportItem = new SwipeMenuItem(LocalVideoFileActivity.this);
                reportItem.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.color.orange));
                reportItem.setWidth(dp2px(100));
                reportItem.setTitle("立即报案");
                reportItem.setTitleSize(18);
                reportItem.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(reportItem);
            }
        };
        //为列表设置创建器
        videoListView.setMenuCreator(creator);

        videoListView.setAdapter(adapter);

        showProgress("拼命加载中...");
        sortTask = new SortTask();
        sortTask.execute();

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
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(LocalVideoFileActivity.this);
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

    @Override
    protected void initEvents() {

        rb_wifi_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showTipDialog("您要断开当前VORANGE行车记录设备哦，确认吗？");
                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                //断开当前的wifi连接
                                disConectWifi();
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
        videoListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:

                        if (VLCApplication.configsDictionary.get("app-a-025") != null) {
                            showTipDialog(VLCApplication.configsDictionary.get("app-a-025"));
                        } else {
                            showTipDialog("您确认要删除所选文件吗？");
                        }
                        dialog.setOnBtnClickL(
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();

                                        if (list.get(position).contains(".mp4")) {
                                            deleteName = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + list.get(position);
                                        } else {
                                            deleteName = BitmapUtils.getSDPath() + "/VOC/" + list.get(position);
                                        }

                                        File removeFile = new File(deleteName);
                                        if (removeFile.isFile()) {
                                            removeFile.delete();
                                        }

                                        if (VLCApplication.configsDictionary.get("APP-A-030") != null) {
                                            ToastUtils.showNomalShortToast(LocalVideoFileActivity.this, VLCApplication.configsDictionary.get("APP-A-030"));
                                        } else {
                                            ToastUtils.showNomalShortToast(LocalVideoFileActivity.this, "文件已成功删除！");
                                        }
                                        sortTask = new SortTask();
                                        sortTask.execute();

                                    }
                                },
                                new OnBtnClickL() {
                                    @Override
                                    public void onBtnClick() {
                                        dialog.dismiss();
                                    }
                                });

                        break;
                    case 1:
                        //判断是否连接上了dvr的wifi
                        if (getDvrWifiName() != null) {
                            showTipDialog("视频分享功能需要断开当前VORANGE行车记录设备哦，确认吗？");
                            dialog.setOnBtnClickL(
                                    new OnBtnClickL() {
                                        @Override
                                        public void onBtnClick() {
                                            dialog.dismiss();
                                            showProgress("拼命加载中...");
                                            //断开当前的wifi连接
                                            disConectWifi();
                                            try {
                                                Thread.sleep(1500);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if (list.get(position).contains(".mp4")) {
                                                strShare = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + list.get(position);
                                            } else {
                                                strShare = BitmapUtils.getSDPath() + "/VOC/" + list.get(position);
                                            }
                                            getQiniuToken();

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
                            if (!NetUtils.isNetworkConnected(LocalVideoFileActivity.this)) {
                                ToastUtils.showNomalShortToast(LocalVideoFileActivity.this, "连接失败，请检查您的网络连接");
                                break;
                            }
                            showProgress("拼命加载中...");
                            if (list.get(position).contains(".mp4")) {
                                strShare = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + list.get(position);
                            } else {
                                strShare = BitmapUtils.getSDPath() + "/VOC/" + list.get(position);
                            }
                            getQiniuToken();
                        }

                        break;
                    case 2:

                        //判断是否连接上了dvr的wifi
                        if (getDvrWifiName() != null) {

                            if (VLCApplication.configsDictionary.get("APP-A-036") != null) {
                                showTipDialog(VLCApplication.configsDictionary.get("APP-A-036"));
                            } else {
                                showTipDialog("立即报案功能需要断开当前VORANGE行车记录设备哦，确认吗？");
                            }

                            dialog.setOnBtnClickL(
                                    new OnBtnClickL() {
                                        @Override
                                        public void onBtnClick() {
                                            dialog.dismiss();
                                            //断开当前的wifi连接
                                            disConectWifi();
                                            try {
                                                Thread.sleep(1500);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if (list.get(position).contains(".mp4")) {
                                                strShare = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + list.get(position);
                                            } else {
                                                strShare = BitmapUtils.getSDPath() + "/VOC/" + list.get(position);
                                            }
                                            Intent intent = new Intent(LocalVideoFileActivity.this, ImmediatelyReport.class);
                                            PreferenceUtil.commitString("isdemo", "0"); // 正式报案
                                            startActivity(intent);
                                            finish();
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
                            if (!NetUtils.isNetworkConnected(LocalVideoFileActivity.this)) {
                                ToastUtils.showNomalShortToast(LocalVideoFileActivity.this, "连接失败，请检查您的网络连接");
                                break;
                            }
                            // 关闭本页面，直接跳转到立即报案页
                            Intent intent = new Intent(LocalVideoFileActivity.this, ImmediatelyReport.class);
                            PreferenceUtil.commitString("isdemo", "0"); // 正式报案
                            startActivity(intent);
                            finish();
                        }

                        break;
                }
                return false;
            }
        });

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int itemPosition, long id) {
                Intent intent = new Intent(LocalVideoFileActivity.this, PlayerActivity.class);
                intent.putExtra("phone", 1);
                intent.putExtra("video_play", list.get(itemPosition));
                if (list.get(itemPosition).contains(".mp4")) {
                    intent.putExtra("is_recorder", 1);
                } else {
                    intent.putExtra("is_recorder", 0);
                }
                PreferenceUtil.commitBoolean("isReport", true);
                startActivity(intent);
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
        finish();
    }

    /**
     * 进入到系统wifi页面
     */
    private void disConectWifi() {
        VLCApplication.wifiManager.setWifiEnabled(false);
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(LocalVideoFileActivity.this);
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }

    private class SortTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            File file = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");
            File file2 = new File(BitmapUtils.getSDPath() + "/VOC/");
            String[] fileList = file.list();//得到该目录下所有的文件名
            String[] fileList2 = file2.list();//得到该目录下所有的文件名
            java.text.SimpleDateFormat datedf = new java.text.SimpleDateFormat("yyyy_MMdd_HHmmss");
            if (tempList != null) {
                try {
                    for (int i = 0; i < fileList.length; i++) {
                        if ((fileList[i].contains(".TS") || fileList[i].contains(".ts") || fileList[i].contains(".mov") || fileList[i].contains(".MOV") || fileList[i].contains(".mp4") || fileList[i].contains(".MP4")) && !fileList[i].equals("456.mp4")) {
                            LocalFileInfo localFileInfo = new LocalFileInfo();
                            localFileInfo.setFileName(fileList[i]);
                            localFileInfo.setFileCreateTime(datedf.format(new Date((new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + fileList[i])).lastModified())));
                            tempList.add(localFileInfo);
                        }
                    }
                    for (int i = 0; i < fileList2.length; i++) {
                        if (fileList2[i].contains(".TS") || fileList2[i].contains(".ts") || fileList2[i].contains(".mp4") || fileList2[i].contains(".MP4") || fileList2[i].contains(".mov") || fileList2[i].contains(".MOV")) {
                            LocalFileInfo localFileInfo = new LocalFileInfo();
                            localFileInfo.setFileName(fileList2[i]);
                            localFileInfo.setFileCreateTime(datedf.format(new Date((new File(BitmapUtils.getSDPath() + "/VOC/" + fileList2[i])).lastModified())));
                            tempList.add(localFileInfo);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //下面的代码是实现时间的排序
            //注意这里有一个坑：要转化的字符串必须要与SimpleDateFormat的格式一模一样
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
            Date d1;
            Date d2;
            //做一个冒泡排序，大的在数组的前列
            for (int i = 0; i < tempList.size() - 1; i++) {
                for (int j = i + 1; j < tempList.size(); j++) {

                    String mov1 = tempList.get(i).getFileCreateTime();
                    String mov2 = tempList.get(j).getFileCreateTime();
                    try {
                        d1 = sdf.parse(mov1);
                        d2 = sdf.parse(mov2);
                        if (d1 != null && d2 != null) {
                            if (d1.before(d2)) {//如果队前日期靠前，调换顺序
                                Collections.swap(tempList, i, j);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            list.clear();
            for (LocalFileInfo s : tempList) {
                list.add(s.getFileName());
            }
            tempList.clear();

            if (kProgressHUD != null) {
                kProgressHUD.dismiss();
            }

            myhandler.sendEmptyMessageDelayed(0, 300);
        }


    }

    /**
     * 获取七牛token
     */
    private void getQiniuToken() {
        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(),1);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (200 == jsonObject.getInt("code")) {
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");
                        shareFileURL = (jsonArray.get(0).toString());

                        uploadShareVideo(shareFileURL + ".mp4");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (kProgressHUD != null) {
                    kProgressHUD.dismiss();
                }
                Toast.makeText(LocalVideoFileActivity.this, "视频分享失败，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 上传分享视频
     *
     * @param fileName
     */
    private void uploadShareVideo(String fileName) {

        //上传分享视频到七牛云
        File file = new File(strShare);
        if (file.exists()) {
            VLCApplication.uploadManager.put(file, fileName, qiniuToken,
                    new UpCompletionHandler() {
                        @Override
                        public void complete(String key, ResponseInfo info, JSONObject res) {
                            //res包含hash、key等信息，具体字段取决于上传策略的设置
                            if (info.isOK()) {
                                try {
                                    String attachid = res.getString("key");
                                    // 获取图片的url
                                    shareFileURL = Config.QINIU_BASE_URL + attachid;
                                    if (kProgressHUD != null) {
                                        kProgressHUD.dismiss();
                                    }
                                    WXVideoShare(shareFileURL, LocalVideoFileActivity.this);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(LocalVideoFileActivity.this, "视频分享失败，请稍后再试", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, null);
        } else {
            if (kProgressHUD != null) {
                kProgressHUD.dismiss();
            }
            Toast.makeText(LocalVideoFileActivity.this, "视频文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 微信分享视频
     *
     * @param videoUrl
     * @param context
     */
    public void WXVideoShare(String videoUrl, Context context) {
        //初始化一个WXVideoObject对象,填写url
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = videoUrl;
        //用WXVideoObject初始化一个WXMediaMessage对象,填写标题、描述
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = video;
        msg.title = "旅橙iCamera";
        msg.description = "事故小视频在线分享。";
        //设置缩略图
        Bitmap thumbBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.wx_icon);
        msg.thumbData = bmpToByteArray(thumbBmp, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");//transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        //调用api接口发送数据到微信
        VLCApplication.wxapi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
