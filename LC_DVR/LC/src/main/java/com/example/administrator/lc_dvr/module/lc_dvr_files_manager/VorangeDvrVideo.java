package com.example.administrator.lc_dvr.module.lc_dvr_files_manager;

import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.MessageEvent;
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
import com.example.administrator.lc_dvr.common.adapter.VorangeDvrVideoAdapter;
import com.example.administrator.lc_dvr.common.constant.Constant;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.XmlToJson;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.OneVideoListInfo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.VideoListInfo;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by yangboru on 2017/11/6.
 */

public class VorangeDvrVideo extends BaseFragment {

    private final int INT_REFRESH_COUNT = 20;// 一次读取文件个数
    private int curCount = 20;//当前数据个数

    private SwipeMenuListView videoListView;
    private VorangeDvrVideoAdapter adapter;
    private VideoListInfo.LISTBean.ALLFileBean.FileBean fileBean;
    private OneVideoListInfo.LISTBean.ALLFileBean.FileBean oneFileBean;
    private String name;
    private VideoListInfo.LISTBean.ALLFileBean.FileBean fileBean_List;
    private OneVideoListInfo.LISTBean.ALLFileBean.FileBean oneFileBean_List;
    private String videoName;
    private int size;
    private String thumbnail_url;//缩略图的url
    private NormalDialog dialog;
    private String[] mStringBts;
    private KProgressHUD kProgressHUD;
    private String delete_url;
    private String video_hour;
    private String video_minute;
    private String time_tag;
    //    public static HashMap<Integer, Boolean> timeStateMap;
    private HttpUtils downloadManager;
    private String filePath;
    private TextView video_download_progress;
    private ImageView cancel_video_download;
    private LinearLayout download_action;
    private WaveProgressView wave_progress;
    private RefreshLayout refreshLayout;
    private int visiblePosition;
    //    public static int isDownloading = -1;
    private StringRequest stringrequest;
    public static long downloadCurrent;// 当前下载量
    public static long downloadTotal;// 总共下载量

    private HttpHandler<File> downloadHandler;
    private boolean isDeleteNow;

    private RankTask rankTask;
    // 显示的列表数据
    private ArrayList<Object> dvrVideoList;

//    private long startArrDate; // 开始加载列表时间
//    private long endArrDate; // 结束家在列表时间

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (kProgressHUD != null) {
                //关闭加载对话框
                kProgressHUD.dismiss();
            }
            if (msg.what == 2) {
                ToastUtils.showNomalShortToast(getContext(), "文件列表读取失败，请稍后再试！");
            }
        }
    };

    @Override
    protected int setViewId() {
        return R.layout.video_list_layout;
    }

    @Override
    protected void findView(View view) {
        videoListView = (SwipeMenuListView) view.findViewById(R.id.video_list);
        refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
        // 禁止下拉刷新
        refreshLayout.setEnableRefresh(false);
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        // 显示的数据
        dvrVideoList = new ArrayList<>();
        //是否正在删除
        isDeleteNow = false;

        //初始化下载管理器
        downloadManager = new HttpUtils();

        time_tag = "-1";

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        adapter = new VorangeDvrVideoAdapter(getActivity(), R.layout.video_item, dvrVideoList);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // 创建“下载”项
                SwipeMenuItem downloadItem = new SwipeMenuItem(
                        VorangeDvrVideo.this.getContext());
                downloadItem.setBackground(new ColorDrawable(Color.LTGRAY));
                downloadItem.setWidth(dp2px(70));
                downloadItem.setTitle(R.string.xiazai);
                downloadItem.setTitleSize(18);
                downloadItem.setTitleColor(Color.BLACK);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(downloadItem);

                // 创建“删除”项
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        VorangeDvrVideo.this.getContext());
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                deleteItem.setWidth(dp2px(70));
                deleteItem.setTitle(R.string.lc_delete);
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(deleteItem);
            }
        };
        //为列表设置创建器
        videoListView.setMenuCreator(creator);
        videoListView.setAdapter(adapter);
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

    @Override
    public void onStop() {
        super.onStop();
        if (stringrequest != null) {
            stringrequest.cancel();
            stringrequest = null;
        }
        //如果还有下载任务就取消下载任务
        if (adapter.isDownloading != -1) {
            //设置为-1表示当前没有下载任务
            adapter.isDownloading = -1;
            //设置列表能左滑
            adapter.isLeftSlip = false;
            adapter.notifyDataSetChanged();
            //隐藏下载信息
            download_action.setVisibility(View.GONE);
            downloadHandler.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        // 如果正在加载就取消加载
        if (stringrequest != null) {
            stringrequest.cancel();
            stringrequest = null;
        }
        //如果还有下载任务就取消下载任务
        if (adapter.isDownloading != -1) {
            //设置为-1表示当前没有下载任务
            adapter.isDownloading = -1;
            //设置列表能左滑
            adapter.isLeftSlip = false;
            adapter.notifyDataSetChanged();
            //隐藏下载信息
            download_action.setVisibility(View.GONE);
            downloadHandler.cancel();
        }
        if (dvrVideoList != null) {
            dvrVideoList.clear();
            dvrVideoList = null;
        }
        if (VLCApplication.dvrVideoList != null) {
            VLCApplication.dvrVideoList.clear();
        }
        if (rankTask != null) {
            rankTask.cancel(true);
            rankTask = null;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if (messageEvent != null && Config.FILE_DOWNLOAD_EXIT_C1.equals(messageEvent.getMessage())) {
            //如果还有下载任务就取消下载任务
            if (adapter.isDownloading != -1) {
                //设置为-1表示当前没有下载任务
                adapter.isDownloading = -1;
                //设置列表能左滑
                adapter.isLeftSlip = false;
                adapter.notifyDataSetChanged();
                //隐藏下载信息
                download_action.setVisibility(View.GONE);
                downloadHandler.cancel();
            }
        }
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(VorangeDvrVideo.this.getContext());
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

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(getContext());
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        kProgressHUD.show();
    }

    @Override
    protected void initEvents() {

        //为ListView设置菜单项点击监听器，监听菜单项的点击事件
        videoListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, final SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        if (adapter.isDownloading == -1) {
                            if (VLCApplication.noOneFile) {
                                fileBean = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
                                name = fileBean.getNAME();
                                size = fileBean.getSIZE();
                            } else {
                                oneFileBean = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
                                name = oneFileBean.getNAME();
                                size = oneFileBean.getSIZE();
                            }
                            //下载的地址
                            filePath = "http://192.168.1.254/CARDV/MOVIE/" + name;

                            video_download_progress = (TextView) videoListView.findViewWithTag(name + "download_progress");
                            cancel_video_download = (ImageView) videoListView.findViewWithTag(name + "download");
                            download_action = (LinearLayout) videoListView.findViewWithTag(name + "action");
                            wave_progress = (WaveProgressView) videoListView.findViewWithTag(name + "progress");

                            //判断文件是否存在，存在就获取它的大小
                            final File dvrFile = new File(BitmapUtils.getSDPath() + "/VOC/" + name);
                            double loadValue = 0;
                            double dvrValue = 1;
                            if (dvrFile.exists() && dvrFile.isFile()) {
                                long fileS = dvrFile.length();
                                double loadSize = (double) (fileS / 1024) / 1024;
                                BigDecimal loadBigDecimal = new BigDecimal(loadSize);
                                loadValue = loadBigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
                                double dvrSize = (double) (size / 1024) / 1024;
                                BigDecimal dvrDecimal = new BigDecimal(dvrSize);
                                dvrValue = dvrDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
                            }

                            //判断文件是否存在
                            if (loadValue == dvrValue) {
                                if (VLCApplication.configsDictionary.get("app-a-020") != null) {
                                    showTipDialog(VLCApplication.configsDictionary.get("app-a-020"));
                                } else {
                                    showTipDialog("文件已存在，您是否要重新下载？");
                                }
                                dialog.setOnBtnClickL(
                                        new OnBtnClickL() {
                                            @Override
                                            public void onBtnClick() {
                                                dialog.dismiss();

                                                //重新下载后就删除之前的文件
                                                File removeFile = new File(dvrFile.getPath());
                                                if (removeFile.isFile()) {
                                                    removeFile.delete();
                                                }

                                                //保存那一项正在下载
                                                adapter.isDownloading = position;

                                                //设置列表不能左滑
                                                adapter.isLeftSlip = true;
                                                adapter.notifyDataSetChanged();

                                                //显示下载信息
                                                download_action.setVisibility(View.VISIBLE);
                                                //设置为-1表示当前没有下载任务
//隐藏下载信息
//设置为-1表示当前没有下载任务
//隐藏下载信息
//只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
//显示下载进度
                                                downloadHandler = downloadManager.download(filePath, BitmapUtils.getSDPath() + "/VOC/" + name,
                                                        new RequestCallBack<File>() {

                                                            @Override
                                                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                                                System.out.println("EventBus.getDefault().post=========");
                                                                EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_FINISH_C1));

                                                                //设置为-1表示当前没有下载任务
                                                                adapter.isDownloading = -1;
                                                                //设置列表能左滑
                                                                adapter.isLeftSlip = false;
                                                                adapter.notifyDataSetChanged();
                                                                //隐藏下载信息
                                                                download_action.setVisibility(View.GONE);
                                                                if (VLCApplication.configsDictionary.get("app-a-015") != null) {
                                                                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-a-015"), Toast.LENGTH_SHORT).show();
                                                                } else {

                                                                    Toast.makeText(getContext(), "下载已完成，您可至本地文件查看！", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(HttpException error, String msg) {
                                                                //设置为-1表示当前没有下载任务
                                                                adapter.isDownloading = -1;
                                                                //设置列表能左滑
                                                                adapter.isLeftSlip = false;
                                                                adapter.notifyDataSetChanged();
                                                                //隐藏下载信息
                                                                download_action.setVisibility(View.GONE);
                                                                Toast.makeText(getContext(), R.string.downloadFail, Toast.LENGTH_SHORT).show();
                                                            }

                                                            @Override
                                                            public void onLoading(long total, long current,
                                                                                  boolean isUploading) {
                                                                downloadCurrent = current;
                                                                downloadTotal = total;
                                                                //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
//                                                                if (position - visiblePosition >= 0) {
                                                                //显示下载进度
                                                                TextView video_download_progress = (TextView) videoListView.findViewWithTag(name + "download_progress");
                                                                WaveProgressView wave_progress = (WaveProgressView) videoListView.findViewWithTag(name + "progress");
                                                                if (wave_progress != null) {
                                                                    wave_progress.setProgress((int) (((double) (current) / (double) (total)) * 100));
                                                                    wave_progress.hideProgressText(true);
                                                                }
                                                                if (video_download_progress != null) {
                                                                    video_download_progress.setText((int) (((double) (current) / (double) (total)) * 100) + "%");
                                                                }


//                                                                }
//                                                                super.onLoading(total, current, isUploading);
                                                            }

                                                        });
                                                //点击取消下载时的响应
                                                cancel_video_download.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //设置为-1表示当前没有下载任务
                                                        adapter.isDownloading = -1;
                                                        //设置列表能左滑
                                                        adapter.isLeftSlip = false;
                                                        adapter.notifyDataSetChanged();
                                                        //隐藏下载信息
                                                        download_action.setVisibility(View.GONE);
                                                        downloadHandler.cancel();
                                                    }
                                                });
                                            }
                                        },
                                        new OnBtnClickL() {
                                            @Override
                                            public void onBtnClick() {
                                                dialog.dismiss();
                                            }
                                        });
                            } else {
                                //保存那一项正在下载
                                adapter.isDownloading = position;

                                //设置列表不能左滑
                                adapter.isLeftSlip = true;
                                adapter.notifyDataSetChanged();

                                //显示下载信息
                                download_action.setVisibility(View.VISIBLE);
                                //设置为-1表示当前没有下载任务
                                //隐藏下载信息
                                //设置为-1表示当前没有下载任务
                                //隐藏下载信息
                                //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
                                //显示下载进度
                                downloadHandler = downloadManager.download(filePath, BitmapUtils.getSDPath() + "/VOC/" + name,
                                        new RequestCallBack<File>() {

                                            @Override
                                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                                System.out.println("EventBus.getDefault().post=========");
                                                EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_FINISH_C1));
                                                //设置为-1表示当前没有下载任务
                                                adapter.isDownloading = -1;
                                                //设置列表能左滑
                                                adapter.isLeftSlip = false;
                                                adapter.notifyDataSetChanged();
                                                //隐藏下载信息
                                                download_action.setVisibility(View.GONE);
                                                if (VLCApplication.configsDictionary.get("app-a-015") != null) {
                                                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-a-015"), Toast.LENGTH_SHORT).show();
                                                } else {

                                                    Toast.makeText(getContext(), "下载已完成，您可至本地文件查看！", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(HttpException error, String msg) {
                                                //设置为-1表示当前没有下载任务
                                                adapter.isDownloading = -1;
                                                //设置列表能左滑
                                                adapter.isLeftSlip = false;
                                                adapter.notifyDataSetChanged();
                                                //隐藏下载信息
                                                download_action.setVisibility(View.GONE);
                                                Toast.makeText(getContext(), R.string.downloadFail, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onLoading(long total, long current,
                                                                  boolean isUploading) {
                                                downloadCurrent = current;
                                                downloadTotal = total;
                                                //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
//                                                if (position - visiblePosition >= 0) {
                                                TextView video_download_progress = (TextView) videoListView.findViewWithTag(name + "download_progress");
                                                WaveProgressView wave_progress = (WaveProgressView) videoListView.findViewWithTag(name + "progress");
                                                if (wave_progress != null) {
                                                    wave_progress.setProgress((int) (((double) (current) / (double) (total)) * 100));
                                                    wave_progress.hideProgressText(true);
                                                }
                                                if (video_download_progress != null) {
                                                    video_download_progress.setText((int) (((double) (current) / (double) (total)) * 100) + "%");
                                                }
//                                                }

//                                                super.onLoading(total, current, isUploading);
                                            }

                                        });
                                //点击取消下载时的响应
                                cancel_video_download.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //设置为-1表示当前没有下载任务
                                        adapter.isDownloading = -1;
                                        //设置列表能左滑
                                        adapter.isLeftSlip = false;
                                        adapter.notifyDataSetChanged();
                                        //隐藏下载信息
                                        download_action.setVisibility(View.GONE);
                                        downloadHandler.cancel();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.notFinishDownload, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (adapter.isDownloading != -1) {
                            Toast.makeText(getContext(), R.string.notFinishDownload, Toast.LENGTH_SHORT).show();
                        } else {

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
                                            //弹出一个等待框
                                            showProgress("正在删除中");
                                            if (VLCApplication.noOneFile) {
                                                fileBean = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
                                                name = fileBean.getNAME();
                                            } else {
                                                oneFileBean = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
                                                name = oneFileBean.getNAME();
                                            }
                                            delete_url = "http://192.168.1.254/CARDV/MOVIE/" + name + "?del=1";

                                            StringRequest stringrequest = new StringRequest(Request.Method.GET, delete_url, new Listener<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    //是否正在删除
                                                    isDeleteNow = true;

//                                                    if (VLCApplication.configsDictionary.get("APP-A-030") != null) {
//                                                        ToastUtils.showNomalShortToast(getActivity(), VLCApplication.configsDictionary.get("APP-A-030"));
//                                                    } else {
//                                                        ToastUtils.showNomalShortToast(getActivity(), "文件已成功删除！");
//                                                    }
                                                    //重新加载数据
                                                    changeModePlayBack();
                                                }

                                                @Override
                                                public void onError(VolleyError volleyError) {
                                                    super.onError(volleyError);
                                                    if (kProgressHUD != null) {
                                                        //关闭加载对话框
                                                        kProgressHUD.dismiss();
                                                    }
//                                                    if (VLCApplication.configsDictionary.get("app-z-010") != null) {
//                                                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
//                                                    } else {
//                                                        Toast.makeText(getContext(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
//                                                    }
                                                }
                                            });
                                            VLCApplication.queue.add(stringrequest);
                                        }
                                    },
                                    new OnBtnClickL() {
                                        @Override
                                        public void onBtnClick() {
                                            dialog.dismiss();
                                        }
                                    });

                        }
                        break;
                }
                return false;
            }
        });

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int itemPosition, long id) {

                //通过isLeftSlip来控制下载完成才能跳转界面
                if (!adapter.isLeftSlip) {

                    if (VLCApplication.noOneFile) {
                        fileBean = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(itemPosition);
                        name = fileBean.getNAME();
                    } else {
                        oneFileBean = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(itemPosition);
                        name = oneFileBean.getNAME();
                    }
//                    Intent intent = new Intent(VorangeDvrVideo.this.getActivity(), RecorderPlayerActivity.class);
//                    Intent intent = new Intent(VorangeDvrVideo.this.getActivity(), IjkPlayer.class);
                    Intent intent = new Intent(VorangeDvrVideo.this.getActivity(), SimplePlayer.class);
                    intent.putExtra("video_play", name);
                    intent.putExtra("phone", 0);

                    SimpleDateFormat dateteFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//19/07/2016 15:56:36
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MMdd_HHmmss");
                    Date resultDate = null;
                    try {
                        resultDate = dateteFormat.parse(fileBean.getTIME());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String Time = simpleDateFormat.format(resultDate);
                    PreferenceUtil.commitString("playFileTime", Time);

                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "如需点击其他操作请等待下载完毕或取消下载", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // 上拉加载
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                refreshData();
            }

        });
    }

    /**
     * 上拉加载数据
     */
    private void refreshData() {
        // 如果数据加载完毕
        if (curCount == VLCApplication.dvrVideoList.size()) {
            ToastUtils.showNomalShortToast(getActivity(), "数据已经加载完毕！");
            refreshLayout.finishLoadMore();
            return;
        }
        try {
            if (curCount + INT_REFRESH_COUNT < VLCApplication.dvrVideoList.size()) {
                for (int i = curCount; i < curCount + INT_REFRESH_COUNT; i++) {
                    dvrVideoList.add(VLCApplication.dvrVideoList.get(i));
                }
                curCount += INT_REFRESH_COUNT;
            } else {
                for (int i = curCount; i < VLCApplication.dvrVideoList.size(); i++) {
                    dvrVideoList.add(VLCApplication.dvrVideoList.get(i));
                }
                curCount = VLCApplication.dvrVideoList.size();
            }
            refreshLayout.finishLoadMore();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            refreshLayout.finishLoadMore();
            Toast.makeText(getContext(), "刷新失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void loadData() {
        if (getDvrWifiName() != null) {
            if (PreferenceUtil.getInt("isVideoData", 0) == 0) {

                //弹出加载对话框
                if (VLCApplication.configsDictionary.get("app-z-000") != null) {
                    showProgress(VLCApplication.configsDictionary.get("app-z-000"));
                } else {
                    showProgress("正在玩命加载中");
                }
                changeModePlayBack();
                //是否已加载了视频数据
                PreferenceUtil.commitInt("isVideoData", 1);
            } else {
                //获得视频文件的名字
                if (VLCApplication.noOneFile) {
                    for (int i = 0; i < VLCApplication.dvrVideoList.size(); i++) {
                        VideoListInfo.LISTBean.ALLFileBean.FileBean fileBeanItem = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                        String fileBeanItemNAME = fileBeanItem.getTIME();
                        if (!time_tag.equals(fileBeanItemNAME.substring(11, 13))) {
                            adapter.timeStateMap.put(i, true);
                            time_tag = fileBeanItemNAME.substring(11, 13);
                        } else {
                            adapter.timeStateMap.put(i, false);
                        }
                    }
                } else {
                    for (int i = 0; i < VLCApplication.dvrVideoList.size(); i++) {
                        OneVideoListInfo.LISTBean.ALLFileBean.FileBean oneFileBeanItem = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                        String oneFileBeanItemNAME = oneFileBeanItem.getTIME();
                        if (!time_tag.equals(oneFileBeanItemNAME.substring(11, 13))) {
                            adapter.timeStateMap.put(i, true);
                            time_tag = oneFileBeanItemNAME.substring(11, 13);
                        } else {
                            adapter.timeStateMap.put(i, false);
                        }
                    }
                }
            }
        } else {

        }
    }

    /**
     * 改变模式为playback
     */
    private void changeModePlayBack() {
        StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.DV_MODE_PLAYBACK, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());
                try {
                    JSONObject function = json.getJSONObject("Function");
                    int value = function.getInt("Status");
                    if (value == 0) {
                        geiVideoData();
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

    /**
     * 获得视频的数据
     */
    private void geiVideoData() {
        //加载数据前先清除数据
        VLCApplication.dvrVideoList.clear();
        time_tag = "-1";
        adapter.timeStateMap.clear();
//        startArrDate = System.currentTimeMillis();;
        stringrequest = new StringRequest(Request.Method.GET, Constant.VIDEO_LIST, new Listener<String>() {
            @Override
            public void onSuccess(String s) {
                JSONObject json = XmlToJson.convertXml2Json(s.toString());

                String videoStr = json.toString();
                if (videoStr.contains("MOV") || videoStr.contains("mov") || videoStr.contains("MP4")) {
                    if (videoStr.contains("[{\"File\"")) {
                        VLCApplication.noOneFile = true;
                        Gson gson = new Gson();
                        VideoListInfo videoListInfo = gson.fromJson(videoStr, VideoListInfo.class);
                        System.out.println("videoStr:" + videoStr);
                        List<VideoListInfo.LISTBean.ALLFileBean> allFile = videoListInfo.getLIST().getALLFile();
                        for (int i = 0; i < allFile.size(); i++) {
                            VideoListInfo.LISTBean.ALLFileBean.FileBean file = allFile.get(i).getFile();
                            String name = file.getNAME();
                            String fpath = file.getFPATH();
                            if (!fpath.contains("RO")) {
                                boolean jpg = name.contains("JPG") || name.contains("jpg");
                                if (!jpg) {
                                    VLCApplication.dvrVideoList.add(file);
                                }
                            }
                        }
//                        endArrDate = System.currentTimeMillis();
//                        ToastUtils.showNomalShortToast(getActivity(),"加载列表时间 = "+( endArrDate- startArrDate));
                        // 开始排序
                        rankTask = new RankTask();
                        rankTask.execute();
                    } else {
                        VLCApplication.noOneFile = false;
                        Gson gs = new Gson();
                        OneVideoListInfo oneVideoListInfo = gs.fromJson(videoStr, OneVideoListInfo.class);
                        OneVideoListInfo.LISTBean.ALLFileBean.FileBean oneFile = oneVideoListInfo.getLIST().getALLFile().getFile();
                        String oneName = oneFile.getNAME();
                        String oneFileFPATH = oneFile.getFPATH();
                        if (!oneFileFPATH.contains("RO")) {
                            boolean jpg = oneName.contains("JPG") || oneName.contains("jpg");
                            if (!jpg) {
                                VLCApplication.dvrVideoList.add(oneFile);
                            }
                        }
                        for (int i = 0; i < VLCApplication.dvrVideoList.size(); i++) {
                            OneVideoListInfo.LISTBean.ALLFileBean.FileBean oneFileBeanItem = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                            String oneFileBeanItemNAME = oneFileBeanItem.getNAME();
                            if (!time_tag.equals(oneFileBeanItemNAME.substring(10, 12))) {
                                adapter.timeStateMap.put(i, true);
                                time_tag = oneFileBeanItemNAME.substring(10, 12);
                            } else {
                                adapter.timeStateMap.put(i, false);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        Message message = new Message();
                        mHandler.sendMessage(message);
                    }
                }

            }

            @Override
            public void onError(VolleyError volleyError) {
                super.onError(volleyError);
                if (isDeleteNow) {
                    //是否正在删除
                    isDeleteNow = false;
                }
                Message message = new Message();
                message.what = 2;
                mHandler.sendMessage(message);
                adapter.notifyDataSetChanged();
            }
        });
        VLCApplication.queue.add(stringrequest);
    }

    private class RankTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            //下面的代码是实现时间的排序
            //注意这里有一个坑：要转化的字符串必须要与SimpleDateFormat的格式一模一样
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date d1;
            Date d2;
            VideoListInfo.LISTBean.ALLFileBean.FileBean temp_r;

            //做一个冒泡排序,TIME字段（文件创建日期）格式“2018/10/15 10:54:49”，大的在数组的前列
            for (int i = 0; i < VLCApplication.dvrVideoList.size() - 1; i++) {
                for (int j = i + 1; j < VLCApplication.dvrVideoList.size(); j++) {
                    ParsePosition pos1 = new ParsePosition(0);
                    ParsePosition pos2 = new ParsePosition(0);
                    VideoListInfo.LISTBean.ALLFileBean.FileBean dvrFile1 = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                    String substring1 = dvrFile1.getTIME();
                    d1 = sdf.parse(substring1, pos1);
                    VideoListInfo.LISTBean.ALLFileBean.FileBean dvrFile2 = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(j);
                    String substring2 = dvrFile2.getTIME();
                    d2 = sdf.parse(substring2, pos2);
                    if (d1 != null && d2 != null) {
                        if (d1.before(d2)) {//如果队前日期靠前，调换顺序
                            temp_r = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                            VLCApplication.dvrVideoList.set(i, VLCApplication.dvrVideoList.get(j));
                            VLCApplication.dvrVideoList.set(j, temp_r);
                        }
                    }
                }
            }

            //下面的代码就是实现时间轴
            for (int i = 0; i < VLCApplication.dvrVideoList.size(); i++) {
                VideoListInfo.LISTBean.ALLFileBean.FileBean fileBeanItem = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                String fileBeanItemNAME = fileBeanItem.getTIME();
                if (!time_tag.equals(fileBeanItemNAME.substring(11, 13))) {
                    adapter.timeStateMap.put(i, true);
                    time_tag = fileBeanItemNAME.substring(11, 13);
                } else {
                    adapter.timeStateMap.put(i, false);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            // 清空list数据
            dvrVideoList.clear();
            // 如果数据多于curCount，就加载curCount个，否则加载VLCApplication.dvrVideoList数据大小
            if (curCount < VLCApplication.dvrVideoList.size()) {
                for (int k = 0; k < curCount; k++) {
                    dvrVideoList.add(VLCApplication.dvrVideoList.get(k));
                }
            } else {
                for (int k = 0; k < VLCApplication.dvrVideoList.size(); k++) {
                    dvrVideoList.add(VLCApplication.dvrVideoList.get(k));
                }
                curCount = VLCApplication.dvrVideoList.size();
            }
//            ToastUtils.showNomalShortToast(getActivity(),"排序列表时间 = "+(System.currentTimeMillis() - endArrDate));
            if (isDeleteNow) {
                //是否正在删除
                isDeleteNow = false;
                if (VLCApplication.configsDictionary.get("app-a-030") != null) {
                    Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-a-030"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "文件已成功删除！", Toast.LENGTH_SHORT).show();
                }
            }

            adapter.notifyDataSetChanged();
            Message message = new Message();
            mHandler.sendMessage(message);

        }
    }

}
