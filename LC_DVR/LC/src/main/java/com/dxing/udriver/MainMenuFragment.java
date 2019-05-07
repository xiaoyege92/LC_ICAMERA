package com.dxing.udriver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.MessageEvent;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.dxing.wifi.api.UdriverFileInfo;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.base.VorangeType;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.SimplePlayer;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.WaveProgressView;
import com.example.administrator.lc_dvr.module.lc_dvr_setting.VorangeDvrSetting;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.varma.android.aws.app.AppSettings;
import com.varma.android.aws.service.HTTPService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vov.vitamio.utils.Log;

import static com.scwang.smartrefresh.layout.util.DensityUtil.dp2px;
import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

public class MainMenuFragment extends BaseFragment implements WiFiSDConfiguration,
        Udriver.LoginResultListener,
        Udriver.FileListener,
        Udriver.FileEventListener,
        Udriver.FormatListener,
        Udriver.DeleteListener,
        Udriver.DirectoryListener,
        UdriverFileListViewAdapter.OnRightDownloadClickListener,
        UdriverFileListViewAdapter.OnRightDeleteClickListener {
    public final int SCAN_WSD_INTERVAL = 200;
    private static Intent httpServerIntent = null;

    private final int INT_REFRESH_COUNT = 30;// 一次读取文件个数
    private int curCount = 30;//当前数据个数
    public static MainMenuFragment mainMenu;
    public static Context context;

    private WifiManager wifiManager;
    public static String ssid = "";
    public static String ipStr = "";
    public Activity runningActivity;

    public ArrayList<SSID> staSSIDs = new ArrayList<SSID>();
    public static Handler autoConnectHandler;
    public static boolean stayMenu, isBusy;
    public SwipeMenuListView listView;
    private UdriverFileListViewAdapter udriverFileListViewAdapter;
    public static List<UdriverFileInfo> udriverFileInfos;
    public static int downloadLen = 0;
    private ArrayList<DownloadQueue> downloadQueues = new ArrayList<DownloadQueue>();
    private boolean singleDownloadProcessing = false;
    private boolean udriverProcessing = false;
    //    private UdriverFileListViewAdapter.ViewHolder currentProcessViewHolder;
    private boolean isFormatting = false;
    private boolean prevActionIsFormat = false;
    private boolean prevActionIsDelete = false;
    private static boolean listenerRemove = false;
    private ArrayList<View> deleteViews = new ArrayList<View>();
    private AtomicBoolean delItem = new AtomicBoolean(false);
    private Handler deleteHandler;
    View view;
    private UdriverFileListViewAdapter.ViewHolder deleteViewHolder;
    private int deletePosition;
    private NormalDialog dialog;
    private String[] mStringBts;
    // 判断是否在一个小时段
    private String time_tag;

    private TextView video_download_progress;
    private ImageView cancel_video_download;
    private LinearLayout download_action;
    private WaveProgressView wave_progress;
    private int visiblePosition;
    private String name;//下载文件名
    private long size;// 下载文件大小
    private UdriverFileInfo udriverFileInfo;// 正在下载的文件bean
    private KProgressHUD kProgressHUD;

    private String filePath;
    private boolean isLoginResult = true;
    private RefreshLayout refreshLayout;
    public static int proPosition;

    @Override
    public void onResume() {
        super.onResume();
        runningActivity = getActivity();
        if (listenerRemove) {
            time_tag = "-1";
            listenerRemove = false;
            Udriver.udriver.addLoginResultListener(this);
            Udriver.udriver.addFileListener(this);
            Udriver.udriver.addFileEventListener(this);
            Udriver.udriver.addDirectoryListener(this);

            udriverFileInfos.clear();
            udriverFileListViewAdapter.timeStateMap.clear();

            for (int i = 0; i < curCount; i++) {

                UdriverFileInfo udriverFileInfo = (UdriverFileInfo) Udriver.udriver.getFileListWithinfo().get(i);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
                String Time = simpleDateFormat.format(resultDate);

                if(udriverFileInfo.getFileName().contains(".ts") || udriverFileInfo.getFileName().contains(".TS")
                        || udriverFileInfo.getFileName().contains(".MP4")|| udriverFileInfo.getFileName().contains(".mp4")
                        || udriverFileInfo.getFileName().contains(".mov")|| udriverFileInfo.getFileName().contains(".MOV")
                        || udriverFileInfo.getFileName().contains(".avi")|| udriverFileInfo.getFileName().contains(".AVI")) {

                    if(udriverFileInfo.getFileLength() > 0) {

                        udriverFileInfos.add(Udriver.udriver.getFileListWithinfo().get(i));

                        if (!time_tag.equals(Time.substring(11, 13))) {
                            udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, true);
                            time_tag = Time.substring(11, 13);
                        } else {
                            udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, false);
                        }
                    }

                }
            }
            Log.e(udriverFileListViewAdapter.timeStateMap.toString());
            udriverFileListViewAdapter.notifyDataSetChanged();
        }
        Udriver.udriver.continueGetTsThumbnail();
    }

    @Override
    public void onPause() {
        super.onPause();
        Udriver.udriver.removeLoginResultListener(this);
        Udriver.udriver.removeFileListener(this);
        Udriver.udriver.removeFileEventListener(this);
        Udriver.udriver.removeDirectoryListener(this);
        listenerRemove = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        //如果还有下载任务就取消下载任务
        if (udriverFileListViewAdapter.isDownloading != -1) {
            //设置为-1表示当前没有下载任务
            udriverFileListViewAdapter.isDownloading = -1;
            udriverProcessing = false;
            udriverFileListViewAdapter.notifyDataSetChanged();
            //隐藏下载信息
            download_action.setVisibility(View.GONE);
            try {
                Udriver.udriver.downloadFileStop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        // 如果有下载任务就停止下载任务，并
        if (udriverFileListViewAdapter.isDownloading != -1) {
            udriverFileListViewAdapter.isDownloading = -1;//设置为-1表示当前没有下载任务

            udriverFileListViewAdapter.notifyDataSetChanged();
            //隐藏下载信息
            download_action.setVisibility(View.GONE);
        }
        try {
            Udriver.udriver.downloadFileStop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果断开wifi就closeDevice(false)
        if(VLCApplication.getDvrWifiName(getActivity()) == null){
            Udriver.udriver.closeDevice(false);
        }else if(VLCApplication.getVorangeType() == VorangeType.B1) {
            Udriver.udriver.closeDevice(true);
        }
        if (udriverFileInfos != null) {
            udriverFileInfos.clear();
            udriverFileInfos = null;
        }
        isLoginResult = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if (messageEvent != null && Config.FILE_DOWNLOAD_EXIT_B1.equals(messageEvent.getMessage())) {
            //如果还有下载任务就取消下载任务
            if (udriverFileListViewAdapter.isDownloading != -1) {
                //设置为-1表示当前没有下载任务
                udriverFileListViewAdapter.isDownloading = -1;
                udriverProcessing = false;
                udriverFileListViewAdapter.notifyDataSetChanged();
                //隐藏下载信息
                download_action.setVisibility(View.GONE);
                try {
                    Udriver.udriver.downloadFileStop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected int setViewId() {
        return R.layout.activity_menu_udriver;
    }

    @Override
    protected void findView(View view) {
        this.view = view;
        refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
        // 禁用下拉刷新
        refreshLayout.setEnableRefresh(false);
    }

    @Override
    protected void init() {
        if (getDvrWifiName(getActivity().getApplicationContext()) != null) {
            showProgress("拼命加载中...");
        }
        EventBus.getDefault().register(this);

        listenerRemove = false;

        time_tag = "-1";

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        System.out.println("init-------------------");
        mainMenu = this;
        runningActivity = getActivity();
        context = getContext();
        wifiManager = (WifiManager) getActivity().getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 初始化视频列表
        if (udriverFileInfos == null) {
            udriverFileInfos = new ArrayList<UdriverFileInfo>();
        }
        Udriver.udriver.addLoginResultListener(this);
        Udriver.udriver.addFileListener(this);
        Udriver.udriver.addFileEventListener(this);
        Udriver.udriver.addDirectoryListener(this);

        // start http service
        if (httpServerIntent == null && !HTTPService.serviceRunning) {
            httpServerIntent = new Intent(getActivity(), HTTPService.class);
            getActivity().startService(httpServerIntent);
            AppSettings.setServiceStarted(getActivity(), true);
        }

        if (getDvrWifiName(getActivity().getApplicationContext()) != null) {
            try {
                Udriver.udriver.initialUdriver(getContext().getApplicationContext());
            } catch (Exception e) {
                // FIXME 这个问题怎么说
//                ToastUtils.showNomalShortToast(getActivity(),"这特么到底是什么玩意。");
            }

        }

        if (!Udriver.udriver.isConnected()) {
            scanWSD();
        } else if (wifiManager.isWifiEnabled()) {
            scanWSD();
        }

        // create temp directory
        String path = getActivity().getApplicationContext().getFilesDir() + "/temp/";
        File file = new File(path);
        file.mkdirs();

        setFileListView();
    }

    @Override
    protected void initEvents() {
        //为ListView设置菜单项点击监听器，监听菜单项的点击事件
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        if (udriverFileListViewAdapter.isDownloading == -1) {

                            udriverFileInfo = (UdriverFileInfo) udriverFileInfos.get(position);
                            name = udriverFileInfo.getFileName();
                            size = udriverFileInfo.getFileLength();

                            //得到第一个可显示控件的位置，
                            visiblePosition = listView.getFirstVisiblePosition();
                            video_download_progress = (TextView) listView.findViewWithTag(udriverFileInfo.getFilePath() + "02");
                            cancel_video_download = (ImageView) listView.getChildAt(position - visiblePosition).findViewById(R.id.cancel_video_download);
                            download_action = (LinearLayout) listView.getChildAt(position - visiblePosition).findViewById(R.id.download_action);
                            wave_progress = (WaveProgressView) listView.findViewWithTag(udriverFileInfo.getFilePath() + "01");

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
                            filePath = "http://127.0.0.1:8080/" + name;
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
                                                udriverFileListViewAdapter.isDownloading = position;

                                                udriverFileListViewAdapter.notifyDataSetChanged();

                                                //显示下载信息
                                                download_action.setVisibility(View.VISIBLE);

                                                downloadProcessor(udriverFileInfo);
                                                //点击取消下载时的响应
                                                cancel_video_download.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //设置为-1表示当前没有下载任务
                                                        udriverFileListViewAdapter.isDownloading = -1;
                                                        udriverProcessing = false;
                                                        //设置列表能左滑
//                                                        udriverFileListViewAdapter.isLeftSlip = false;
                                                        udriverFileListViewAdapter.notifyDataSetChanged();
                                                        //隐藏下载信息
                                                        download_action.setVisibility(View.GONE);
                                                        try {
                                                            Udriver.udriver.downloadFileStop();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
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
                                udriverFileListViewAdapter.isDownloading = position;

                                //设置列表不能左滑
//                                udriverFileListViewAdapter.isLeftSlip = true;
                                udriverFileListViewAdapter.notifyDataSetChanged();

                                //显示下载信息
                                download_action.setVisibility(View.VISIBLE);

                                downloadProcessor(udriverFileInfo);

                                cancel_video_download.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //设置为-1表示当前没有下载任务
                                        udriverFileListViewAdapter.isDownloading = -1;
                                        udriverProcessing = false;

                                        udriverFileListViewAdapter.notifyDataSetChanged();
                                        //隐藏下载信息
                                        download_action.setVisibility(View.GONE);
//                                        downloadHandler.cancel();
                                        try {
                                            Udriver.udriver.downloadFileStop();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.notFinishDownload, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (udriverFileListViewAdapter.isDownloading != -1) {
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
                                            udriverFileInfo = (UdriverFileInfo) udriverFileInfos.get(position);
                                            name = udriverFileInfo.getFileName();

                                            if (!udriverProcessing && !isFormatting && !prevActionIsDelete) {
//                                                deleteViewHolder = viewHolder;
                                                deletePosition = position;

                                                dialog.dismiss();
//                                              listView.removeView(listView.getChildAt(position));
                                                Udriver.udriver.addDeleteFileListener(MainMenuFragment.this);
                                                final UdriverFileInfo udriverFileInfo = udriverFileInfos.get(position);
                                                delItem.set(false);
                                                Udriver.udriver.deleteFile(udriverFileInfo);
                                                prevActionIsDelete = true;

                                                if (deleteHandler != null)
                                                    deleteHandler = null;
                                                deleteHandler = new DeleteItemHandler();
                                                Thread delThread = new Thread() {
                                                    public void run() {
                                                        try {
                                                            Thread.sleep(800);

                                                            synchronized (delItem) {
                                                                if (delItem.get()) {
                                                                    Message message = deleteHandler.obtainMessage(0, udriverFileInfo);
                                                                    deleteHandler.sendMessage(message);
                                                                } else {
                                                                    delItem.set(true);
                                                                }
                                                            }
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                };
                                                delThread.start();

                                                if (VLCApplication.configsDictionary.get("APP-A-030") != null) {
                                                    ToastUtils.showNomalShortToast(getActivity(), VLCApplication.configsDictionary.get("APP-A-030"));
                                                } else {
                                                    ToastUtils.showNomalShortToast(getActivity(), "文件已成功删除！");
                                                }
                                            } else {
                                                String strMsg = "system process, please wait";
                                                Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
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
                        break;
                }
                return false;
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

    @Override
    protected void loadData() {

    }

    private void setFileListView() {
        listView = (SwipeMenuListView) view.findViewById(R.id.udriver_fileListView);

        udriverFileListViewAdapter = new UdriverFileListViewAdapter(getActivity(), udriverFileInfos, this, this);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // 创建“下载”项
                SwipeMenuItem downloadItem = new SwipeMenuItem(
                        MainMenuFragment.this.getContext());
                downloadItem.setBackground(new ColorDrawable(Color.LTGRAY));
                downloadItem.setWidth(dp2px(70));
                downloadItem.setTitle(R.string.xiazai);
                downloadItem.setTitleSize(18);
                downloadItem.setTitleColor(Color.BLACK);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(downloadItem);

                // 创建“删除”项
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        MainMenuFragment.this.getContext());
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
        listView.setMenuCreator(creator);
        listView.setAdapter(udriverFileListViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!udriverProcessing && !isFormatting) {
                    UdriverFileInfo udriverFileInfo = udriverFileInfos.get(position);
                    switch (getFileType(udriverFileInfo.getFileName())) {
                        case PAGE_PHOTO:
                            break;
                        case PAGE_VIDEO:
                            Udriver.udriver.playVideo(udriverFileInfo);
                            break;
                    }
                }
            }
        });

        refreshLayout.autoRefresh();

    }

    /**
     * 刷新数据
     */
    private void refreshData() {

        if (curCount == Udriver.udriver.getFileListWithinfo().size()) {
            ToastUtils.showNomalShortToast(getActivity(), "数据已经加载完毕！");
            refreshLayout.finishLoadMore();
            udriverFileListViewAdapter.notifyDataSetChanged();
            return;
        }
        try {
            if (curCount + INT_REFRESH_COUNT < Udriver.udriver.getFileListWithinfo().size()) {
                for (int i = curCount; i < curCount + INT_REFRESH_COUNT; i++) {
                    UdriverFileInfo udriverFileInfo = (UdriverFileInfo) Udriver.udriver.getFileListWithinfo().get(i);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                    Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
                    String Time = simpleDateFormat.format(resultDate);

                    if(udriverFileInfo.getFileName().contains(".ts") || udriverFileInfo.getFileName().contains(".TS")
                            || udriverFileInfo.getFileName().contains(".MP4")|| udriverFileInfo.getFileName().contains(".mp4")
                            || udriverFileInfo.getFileName().contains(".mov")|| udriverFileInfo.getFileName().contains(".MOV")
                            || udriverFileInfo.getFileName().contains(".avi")|| udriverFileInfo.getFileName().contains(".AVI")) {

                        if(udriverFileInfo.getFileLength() > 0) {

                            // 从udriver中拿到需要刷新的数据
                            udriverFileInfos.add(Udriver.udriver.getFileListWithinfo().get(i));

                            if (!time_tag.equals(Time.substring(11, 13))) {
                                udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, true);
                                time_tag = Time.substring(11, 13);
                            } else {
                                udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, false);
                            }
                        }
                    }
                }
                // 刷新数据增加
                curCount += INT_REFRESH_COUNT;
            } else {
                for (int i = curCount; i < Udriver.udriver.getFileListWithinfo().size(); i++) {
                    UdriverFileInfo udriverFileInfo = (UdriverFileInfo) Udriver.udriver.getFileListWithinfo().get(i);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                    Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
                    String Time = simpleDateFormat.format(resultDate);

                    if(udriverFileInfo.getFileName().contains(".ts") || udriverFileInfo.getFileName().contains(".TS")
                            || udriverFileInfo.getFileName().contains(".MP4")|| udriverFileInfo.getFileName().contains(".mp4")
                            || udriverFileInfo.getFileName().contains(".mov")|| udriverFileInfo.getFileName().contains(".MOV")
                            || udriverFileInfo.getFileName().contains(".avi")|| udriverFileInfo.getFileName().contains(".AVI")) {

                        if(udriverFileInfo.getFileLength() > 0) {

                            // 从udriver中拿到需要刷新的数据
                            udriverFileInfos.add(Udriver.udriver.getFileListWithinfo().get(i));

                            if (!time_tag.equals(Time.substring(11, 13))) {
                                udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, true);
                                time_tag = Time.substring(11, 13);
                            } else {
                                udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, false);
                            }
                        }
                    }
                }
                curCount = Udriver.udriver.getFileListWithinfo().size();
            }

            refreshLayout.finishLoadMore();
            udriverFileListViewAdapter.notifyDataSetChanged();
            // 加载缩略图
            Udriver.udriver.continueGetTsThumbnail();
        } catch (Exception e) {
            refreshLayout.finishLoadMore();
            Toast.makeText(getContext(), "刷新失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    protected void showWiFiDisconnectedDialog(boolean value) {
        String msg = value ? getString(R.string.wifi_reconnected) : getString(R.string.wifi_disconnected);
        msg += "\n" + getString(R.string.alert_shutdown);
        AlertDialog disconnectedDialog = new AlertDialog.Builder(runningActivity)
                .setTitle(R.string.wifi_state)
                .setMessage(msg)
                .setPositiveButton(R.string.confirm_shutdown, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        exit();
                    }
                })
                .create();
        disconnectedDialog.setCanceledOnTouchOutside(false);
        disconnectedDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void loginResult(int result) {
        if (result == 1) {    //success
            return;
        }
        if (getActivity() != null) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String folder = pref.getString(PREF_LOCAL_PATH, DEFAULT_LOCAL_PATH);

            String dirString = Environment.getExternalStorageDirectory().toString();
            File prefFolder = new File(dirString, folder);
            prefFolder.mkdirs();
            try {
                Udriver.udriver.start("", "", folder);
            } catch (Exception e) {
            }

        }

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
//                        Udriver.udriver.initialUdriver(getActivity().getApplicationContext());
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

    public static void exit() {
        if (HTTPService.serviceRunning) {
            try {
                HTTPService.instance.stopSelf();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String path = context.getApplicationContext().getFilesDir() + "/temp/";
            File tmpDirectory = new File(path);
            for (File file : tmpDirectory.listFiles()) {
                file.delete();
            }
            tmpDirectory.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //	mainMenu.finish();
        System.exit(0);
    }

    private void showNoDeviceFoundDialog() {
        String msg = this.getString(R.string.no_shared_disc);
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.shared_disc_connection_title)
                .setMessage(msg)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        dialog.show();
    }

    void playVideo(String videoName) {
        if (videoName == null || "".equals(videoName)) {
            return;
        }
        String name_change = videoName.replace(" ", "0");

        String videoClip = "http://127.0.0.1:8080/" + name_change;

        if (isAdded()) {
//            Intent intent = new Intent(runningActivity, RecorderPlayerActivity.class);
//            Intent intent = new Intent(runningActivity, IjkPlayer.class);
            Intent intent = new Intent(runningActivity, SimplePlayer.class);
            intent.putExtra("phone", 0);
            intent.putExtra("video_play", videoClip);
            startActivity(intent);
        }
    }

    private String getExternalMemoryPath() {
        return Environment.getExternalStorageDirectory().getPath();
        //return "/mnt/sdcard";
    }

    private StatFs getStatFs(String path) {
        try {
            return new StatFs(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getFileType(String filename) {
        if (Udriver.isSupportedImageType(filename)) {
            return PAGE_PHOTO;
        } else if (Udriver.isSupportedMusicType(filename)) {
            return PAGE_MUSIC;
        } else if (Udriver.isSupportedVideoType(filename)) {
            return PAGE_VIDEO;
        } else {
            return PAGE_MISC;
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

    @SuppressLint("NewApi")
    private long calculateSizeInMB(StatFs stat) {
        if (stat != null) {
            if (Build.VERSION.SDK_INT >= 18) {
                return stat.getAvailableBytes();
            } else {
                return (long) stat.getAvailableBlocks() * stat.getBlockSize();
            }
        }
        return 0;
    }

    private long getAvailableExternalMemorySize() {
        String path = getExternalMemoryPath();
        StatFs stat = getStatFs(path);
        return calculateSizeInMB(stat);
    }

    private int downloadProcessor(UdriverFileInfo udriverFileInfo) {
        long fileLen = udriverFileInfo.getFileLength();
        File destFile;

//        if (singleDownloadProcessing) {
//            synchronized (downloadQueues) {
        DownloadQueue downloadQueue = new DownloadQueue(udriverFileInfo);
//                viewHolder.leftItem.setBackgroundColor(0xFF86F303);
//                downloadQueues.add(downloadQueue);
//				TextView downloadText = (TextView)view.findViewById(R.id.download_text);
//				String queueSize = Integer.toString(downloadQueues.size() - 1);
//				downloadText.setText("file downloading, download queue : " + queueSize);
//                return 1;    //put to queue
//            }
//        } else {
        if (fileLen > getAvailableExternalMemorySize()) {
            String str = "not enough space";
            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
            return -1;
        }
        singleDownloadProcessing = true;
        udriverProcessing = true;
//            currentProcessViewHolder = viewHolder;

        String internalStoragePath;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            internalStoragePath = System.getenv("EXTERNAL_STORAGE");
        } else {
            internalStoragePath = Udriver.udriver.getStoragePath_AndroidM(context, false);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String localPath = pref.getString(PREF_LOCAL_PATH, DEFAULT_LOCAL_PATH);

        String path = BitmapUtils.getSDPath() + "/VOC/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filename = udriverFileInfo.getFileName();
        destFile = new File(dir, filename);
        System.out.println("destFile:" + destFile);
        // FIXME 下载时同时下载缩略图，以便与SD卡缩略图保持一致
        udriverFileListViewAdapter.saveBitmap(udriverFileInfo);
        if (destFile.exists()) {
//				int extPos = filename.lastIndexOf(".");
//				String realname = extPos != -1 ? filename.substring(0, extPos) : filename;
//				String extension = extPos != -1 ? filename.substring(extPos) : "";
//				int counter = 1;
//				filename = realname + "_" + (counter++) + extension;
//				destFile = new File(dir, filename);
//				while(destFile.exists()) {
//					filename = realname + "_" + (counter++) + extension;
//					destFile = new File(dir, filename);
//				}
        }

//            boolean findinQueue = false;
//            synchronized (downloadQueues) {
//                for (DownloadQueue downloadQueue : downloadQueues) {
//                    if (downloadQueue.getViewHolder() == viewHolder) {
//                        findinQueue = true;
//                        break;
//                    }
//                }
//                if (!findinQueue) {
//                    DownloadQueue downloadQueue = new DownloadQueue(udriverFileInfo);
//                    downloadQueues.add(downloadQueue);
//                }
//            }
//            String queueSize = Integer.toString(downloadQueues.size() - 1);
//            viewHolder.rightDownloadImage.setImageResource(R.drawable.cancel_black_60dp);
//            viewHolder.rightDownloadImage.setTag("loading");

//			TextView downloadText = (TextView)view.findViewById(R.id.download_text);
//			downloadText.setVisibility(View.VISIBLE);
//			downloadText.setText("file downloading, download queue : " + queueSize);
//            viewHolder.leftItem.setBackgroundColor(0xFFE8F303);
//            viewHolder.leftDownloadProgressBar.setVisibility(View.VISIBLE);
        downloadLen = (int) udriverFileInfo.getFileLength();
//            viewHolder.leftDownloadProgressBar.setMax(downloadLen);
//            viewHolder.leftDownloadProgressBar.setProgress(0);

        Udriver.udriver.downloadFile(udriverFileInfo, destFile);
        //listener  onFileLoadingComplete() for download complete and onFileLoadingProgress for progress
        return 0;
//        }
    }

    @Override
    public void onFileLoadingComplete() {
        //设置为-1表示当前没有下载任务
        udriverFileListViewAdapter.isDownloading = -1;
        udriverProcessing = false;
        //设置列表能左滑
//      udriverFileListViewAdapter.isLeftSlip = false;
        udriverFileListViewAdapter.notifyDataSetChanged();
        //隐藏下载信息
        if (download_action != null) {
            download_action.setVisibility(View.GONE);
            if (VLCApplication.configsDictionary.get("app-a-015") != null) {
                Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-a-015"), Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(getContext(), "下载已完成，您可至本地文件查看！", Toast.LENGTH_SHORT).show();
            }
        }
        EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_FINISH_B1));
        Udriver.udriver.continueGetTsThumbnail();
//        if (singleDownloadProcessing) {
//            boolean hasDownloadQueue = false;
//            DownloadQueue downloadQueue = null;
//
//            singleDownloadProcessing = false;
//            synchronized (downloadQueues) {
//                if (downloadQueues.size() > 0) {
//                    downloadQueue = downloadQueues.get(0);
//                    downloadQueue.getViewHolder().rightDownloadImage.setImageResource(R.drawable.cloud_download_black_60dp);
//                    downloadQueue.getViewHolder().rightDownloadImage.setTag("wait");
//                    downloadQueue.getViewHolder().leftDownloadProgressBar.setVisibility(View.GONE);
//                    downloadQueue.getViewHolder().leftItem.setBackgroundColor(Color.TRANSPARENT);
//                    String str = downloadQueue.getUdriverFileInfo().getFileName() + "  download complete!!";
//                    Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
//                    EventBus.getDefault().post(new MessageEvent(""));
//                    downloadQueues.remove(0);
//                    if (downloadQueues.size() > 0) {
//                        hasDownloadQueue = true;
//                        downloadQueue = downloadQueues.get(0);
//                    }
//                }
//            }
//
//            if (!hasDownloadQueue) {
//                udriverProcessing = false;
//				TextView downloadText = (TextView) view.findViewById(R.id.download_text);
//				downloadText.setVisibility(View.GONE);
//            } else {
//                downloadProcessor(downloadQueue.getUdriverFileInfo());
//            }
//        }
    }

    @Override
    public void onFileLoadingProgress(int position) {

        proPosition = position;
        if (listView != null && udriverFileInfo != null) {
            WaveProgressView wpv = (WaveProgressView) listView.findViewWithTag(udriverFileInfo.getFilePath() + "01");
            TextView textView = (TextView) listView.findViewWithTag(udriverFileInfo.getFilePath() + "02");
            if (wpv != null) {
                wpv.setProgress((int) (((double) (position) / (double) (downloadLen)) * 100));
                wpv.hideProgressText(true);
            }
            if (textView != null) {
                textView.setText((int) (((double) (position) / (double) (downloadLen)) * 100) + "%");
            }
        }
    }

    @Override
    public void onFileLoadingError() {

    }

    @Override
    public void onLoginResultListener(int result) {
        loginResult(result);
    }

    @Override
    public void onDeviceFoundListener() {
    }

    @Override
    public void onDeviceErrorDuplicateFileListener() {
//        showDeviceErrorDialog(R.string.device_error_file_err);
        ToastUtils.showNomalShortToast(getActivity(), "您TF卡格式不正确，请格式化为FAT32格式");
        if (kProgressHUD != null) {
            kProgressHUD.dismiss();
        }
    }

    @Override
    public void onDeviceErrorUnknownFormatListener() {
//        showDeviceErrorDialog(R.string.device_error);
        ToastUtils.showNomalShortToast(getActivity(), "您TF卡格式不正确，请格式化为FAT32格式");
        if (kProgressHUD != null) {
            kProgressHUD.dismiss();
        }
    }

    @Override
    public void onDeviceErrorUnFormatListener() {
//        showDeviceErrorDialog(R.string.device_error_unformatted);
        ToastUtils.showNomalShortToast(getActivity(), "您TF卡格式不正确，请格式化为FAT32格式");
        if (kProgressHUD != null) {
            kProgressHUD.dismiss();
        }
    }

    @Override
    public void onDeviceReadyListener() {
        this.isBusy = false;
    }

    public void showDeviceErrorDialog(int msgId) {
        if (getActivity() == null) {
            return;
        }
        String msg = getActivity().getString(msgId) + "\n" + this.getString(R.string.ask_format);
        Dialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.device_error_title)
                .setMessage(msg)
                .setPositiveButton(R.string.confirm_format, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //exit();
                        Udriver.udriver.addFormatListener(mainMenu);
                        isFormatting = true;
                        prevActionIsFormat = true;
                        try {
                            Udriver.udriver.formatSDCard();
                        } catch (Exception e) {
                            // FIXME 无奈
                        }
                    }
                })
                .setNegativeButton(R.string.confirm_shutdown, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        exit();
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onFileScanDone() {

        if (kProgressHUD != null) {
            kProgressHUD.dismiss();
        }
        udriverFileInfos.clear();
        time_tag = "-1";
        udriverFileListViewAdapter.timeStateMap.clear();

        if (curCount < Udriver.udriver.getFileListWithinfo().size()) {
            for (int i = 0; i < curCount; i++) {
                UdriverFileInfo udriverFileInfo = (UdriverFileInfo) Udriver.udriver.getFileListWithinfo().get(i);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
                String Time = simpleDateFormat.format(resultDate);

                if(udriverFileInfo.getFileName().contains(".ts") || udriverFileInfo.getFileName().contains(".TS")
                        || udriverFileInfo.getFileName().contains(".MP4")|| udriverFileInfo.getFileName().contains(".mp4")
                        || udriverFileInfo.getFileName().contains(".mov")|| udriverFileInfo.getFileName().contains(".MOV")
                        || udriverFileInfo.getFileName().contains(".avi")|| udriverFileInfo.getFileName().contains(".AVI")) {

                    if(udriverFileInfo.getFileLength() > 0) {

                        // 从udriver中拿到需要刷新的数据
                        udriverFileInfos.add(Udriver.udriver.getFileListWithinfo().get(i));

                        if (!time_tag.equals(Time.substring(11, 13))) {
                            udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, true);
                            time_tag = Time.substring(11, 13);
                        } else {
                            udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, false);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < Udriver.udriver.getFileListWithinfo().size(); i++) {
                UdriverFileInfo udriverFileInfo = (UdriverFileInfo) Udriver.udriver.getFileListWithinfo().get(i);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
                String Time = simpleDateFormat.format(resultDate);

                if(udriverFileInfo.getFileName().contains(".ts") || udriverFileInfo.getFileName().contains(".TS")
                        || udriverFileInfo.getFileName().contains(".MP4")|| udriverFileInfo.getFileName().contains(".mp4")
                        || udriverFileInfo.getFileName().contains(".mov")|| udriverFileInfo.getFileName().contains(".MOV")
                        || udriverFileInfo.getFileName().contains(".avi")|| udriverFileInfo.getFileName().contains(".AVI")) {

                    if(udriverFileInfo.getFileLength() > 0) {

                        // 从udriver中拿到需要刷新的数据
                        udriverFileInfos.add(Udriver.udriver.getFileListWithinfo().get(i));

                        if (!time_tag.equals(Time.substring(11, 13))) {
                            udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, true);
                            time_tag = Time.substring(11, 13);
                        } else {
                            udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, false);
                        }
                    }
                }
            }
            curCount = Udriver.udriver.getFileListWithinfo().size();
        }
//        udriverFileInfos.addAll(Udriver.udriver.getFileListWithinfo());

        udriverFileListViewAdapter.notifyDataSetChanged();
        if (prevActionIsFormat || prevActionIsDelete) {
            prevActionIsFormat = false;
            prevActionIsDelete = false;
        }
        if (Udriver.udriver.getFileListWithinfo().size() == 0) {
            ToastUtils.showNomalShortToast(getActivity(), "文件列表为空，请先录制");
        }
    }

    @Override
    public void onFileVideoStreamCapability(boolean isStreamable, Object object) {
        if (isStreamable) {
            playVideo((String) object);
        }
    }

    @Override
    public void onDataUpdate() {

    }

    @Override
    public void onFileScan(Object object) {

    }

    // 播放时长回传方法
    @Override
    public void onGetTsFilePlayTime(UdriverFileInfo udriverFileInfo) {
        TextView playTimeTextView = (TextView) listView.findViewWithTag(udriverFileInfo.getFilePath() + "0");
        if (playTimeTextView != null) {
            if (udriverFileInfo.isPlayTimeValid()) {
                int time = udriverFileInfo.getFilePlayTime();
                playTimeTextView.setText(sec2time(time));
            }
        }
    }

    @Override
    public void onDownloadClick(int position) {
//        ImageView imageView = viewHolder.rightDownloadImage;
        if (isFormatting) {
            String strMsg = "Formatting!! can not Download!!";
            Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
            return;
        } else if (prevActionIsDelete) {
            String strMsg = "File delete, please wait!!";
            Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
            return;
        }

//        if (viewHolder.rightDownloadImage.getTag().equals("loading")) {
//            //cancel download
//            viewHolder.rightDownloadImage.setImageResource(R.drawable.cloud_download_black_60dp);
//            viewHolder.rightDownloadImage.setTag("wait");
        //cancel download
        try {
            Udriver.udriver.downloadFileStop();
        } catch (Exception e) {
            e.printStackTrace();
        }
//            this.onFileLoadingComplete();
//        } else if (viewHolder.rightDownloadImage.getTag().equals("queue")) {
//            viewHolder.rightDownloadImage.setImageResource(R.drawable.cloud_download_black_60dp);
//            viewHolder.rightDownloadImage.setTag("wait");
//            viewHolder.leftItem.setBackgroundColor(Color.TRANSPARENT);
        synchronized (downloadQueues) {
            for (DownloadQueue downloadQueue : downloadQueues) {
//                    if (downloadQueue.getViewHolder().rightDownloadImage == viewHolder.rightDownloadImage) {
                downloadQueues.remove(downloadQueue);
//                        break;
//                    }
            }
//				TextView downloadText = (TextView)view.findViewById(R.id.download_text);
//				String queueSize = Integer.toString(downloadQueues.size() - 1);
//				downloadText.setText("file downloading, download queue : " + queueSize);
        }
//        } else {
        UdriverFileInfo udriverFileInfo = udriverFileInfos.get(position);
        int sts = downloadProcessor(udriverFileInfo);
//            if (sts == 0) {
//                viewHolder.rightDownloadImage.setImageResource(R.drawable.cancel_black_60dp);
//                viewHolder.rightDownloadImage.setTag("loading");
//            } else if (sts == 1) {
//                viewHolder.rightDownloadImage.setImageResource(R.drawable.queue_black_60dp);
//                viewHolder.rightDownloadImage.setTag("queue");
//            }
//        }
    }

    @Override
    public void onDeleteClick(UdriverFileListViewAdapter.ViewHolder viewHolder, int position) {
        if (!udriverProcessing && !isFormatting && !prevActionIsDelete) {
            deleteViewHolder = viewHolder;
            deletePosition = position;
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
                            View deleteView = (View) listView.findViewWithTag(deleteViewHolder);
                            listView.removeView(deleteView);
                            Udriver.udriver.addDeleteFileListener(MainMenuFragment.this);
                            final UdriverFileInfo udriverFileInfo = udriverFileInfos.get(deletePosition);
                            delItem.set(false);
                            Udriver.udriver.deleteFile(udriverFileInfo);
                            prevActionIsDelete = true;
                            if (deleteView != null)
                                deleteViews.add(deleteView);
                            if (deleteHandler != null)
                                deleteHandler = null;
                            deleteHandler = new DeleteItemHandler();
                            Thread delThread = new Thread() {
                                public void run() {
                                    try {
                                        Thread.sleep(800);

                                        synchronized (delItem) {
                                            if (delItem.get()) {
                                                Message message = deleteHandler.obtainMessage(0, udriverFileInfo);
                                                deleteHandler.sendMessage(message);
                                            } else {
                                                delItem.set(true);
                                            }
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            delThread.start();

                            if (VLCApplication.configsDictionary.get("APP-A-030") != null) {
                                ToastUtils.showNomalShortToast(getActivity(), VLCApplication.configsDictionary.get("APP-A-030"));
                            } else {
                                ToastUtils.showNomalShortToast(getActivity(), "文件已成功删除！");
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
            String strMsg = "system process, please wait";
            Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
        }
    }

    static class DeleteItemHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    UdriverFileInfo delUdriverFileInfo = (UdriverFileInfo) msg.obj;
                    udriverFileInfos.remove(delUdriverFileInfo);

                    udriverFileInfos.clear();
                    mainMenu.time_tag= "-1";
                    mainMenu.udriverFileListViewAdapter.timeStateMap.clear();
                    mainMenu.curCount--;

                    for (int i = 0; i < mainMenu.curCount; i++) {
                        UdriverFileInfo udriverFileInfo = (UdriverFileInfo) Udriver.udriver.getFileListWithinfo().get(i);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                        Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
                        String Time = simpleDateFormat.format(resultDate);

                        if(udriverFileInfo.getFileName().contains(".ts") || udriverFileInfo.getFileName().contains(".TS")
                                || udriverFileInfo.getFileName().contains(".MP4")|| udriverFileInfo.getFileName().contains(".mp4")
                                || udriverFileInfo.getFileName().contains(".mov")|| udriverFileInfo.getFileName().contains(".MOV")
                                || udriverFileInfo.getFileName().contains(".avi")|| udriverFileInfo.getFileName().contains(".AVI")) {

                            if(udriverFileInfo.getFileLength() > 0) {

                                udriverFileInfos.add(Udriver.udriver.getFileListWithinfo().get(i));

                                if (!mainMenu.time_tag.equals(Time.substring(11, 13))) {
                                    mainMenu.udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, true);
                                    mainMenu.time_tag = Time.substring(11, 13);
                                } else {
                                    mainMenu.udriverFileListViewAdapter.timeStateMap.put(udriverFileInfos.size()-1, false);
                                }
                            }
                        }
                    }

                    mainMenu.udriverFileListViewAdapter.notifyDataSetChanged();
                    mainMenu.prevActionIsDelete = false;

                    break;
            }
        }
    }

    @Override
    public void onFormatWriteBusy() {
        isFormatting = false;
        String strMsg = "card is Busy, please Wait";
        Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFormatProgress(int currentProgress, int maxProgress) {

    }

    @Override
    public void onDeleteFileBusy() {
        String strMsg = "card is Busy, please Wait";
        Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteFileDone(UdriverFileInfo delUdriverFileInfo) {
        //String strMsg = "File delete success!!";

        Udriver.udriver.removeDeleteFileListener(this);

        synchronized (delItem) {
            if (delItem.get()) {
                Message message = deleteHandler.obtainMessage(0, delUdriverFileInfo);
                deleteHandler.sendMessage(message);
            } else {
                delItem.set(true);
            }
        }
    }

    @Override
    public void onDeleteFileError() {
        String strMsg = "File delete Fail!!";
        Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDirectoryReady(Object object) {

    }

    // 缩略图回传方法
    @Override
    public void onDirectoryThumbnail(Object object) {
        UdriverFileInfo udriverFileInfo = (UdriverFileInfo) ((HashMap<String, Object>) object).get("file");
        Bitmap bitmap = (Bitmap) ((HashMap<String, Object>) object).get("thumbnail");

        ImageView imageView = (ImageView) listView.findViewWithTag(udriverFileInfo.getFilePath());
        if (bitmap != null && imageView != null) {
            imageView.setImageBitmap(bitmap);
            udriverFileListViewAdapter.putBitmap(udriverFileInfo, bitmap);
        }
    }

    @Override
    public void onDirectortRootReady(Object object) {

    }

    @Override
    public void onDirectoryAddFileResult(Object object) {

    }

    @Override
    public void onDirectoryWriteFileDone(Object object) {

    }

    @Override
    public void onDirectoryDirAdded() {

    }

    @Override
    public void onDirectoryFileUpload() {

    }

    public static List<WifiConfiguration> getWifiConfiguration() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);    //re get
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        return list;
    }

    public static int addWifiConfig(boolean newConfig, WifiConfiguration config) {
        int id = 0;
        if (newConfig)
            id = mainMenu.wifiManager.addNetwork(config);
        else
            id = mainMenu.wifiManager.updateNetwork(config);
        boolean save = mainMenu.wifiManager.saveConfiguration();
        return id;
    }

    public static void deleteWifiConfig(WifiConfiguration config) {
        boolean res = mainMenu.wifiManager.removeNetwork(config.networkId);
        boolean save = mainMenu.wifiManager.saveConfiguration();
    }

    public void autoConnectForm() {

    }

    public static WifiInfo getWifiInfo() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    public static void enableLocalWiFi() {
        Thread waitWifiEnableThread = new Thread() {
            public void run() {
                //while(true)	{
                try {
                    Thread.sleep(200);
                    if (!mainMenu.wifiManager.isWifiEnabled())
                        mainMenu.wifiManager.setWifiEnabled(true);
                    //else	{
                    //	break;
                    //}
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //}
            }
        };
        waitWifiEnableThread.start();
    }

    public static void disableLocalWiFi() {
        if (mainMenu.wifiManager.isWifiEnabled())
            mainMenu.wifiManager.setWifiEnabled(false);
    }

    public static boolean isLocalWiFiEnabled() {
        return mainMenu.wifiManager.isWifiEnabled();
    }

    public static void connectWifi(WifiConfiguration config) {
        boolean sts;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.disconnect();    //for Hauwei Meta10
        sts = wifiManager.enableNetwork(config.networkId, true);
    }

    public void reScanWSD() {
        showNoDeviceFoundDialog();
        scanWSD();
    }

    public static void turnOnAutoConnectHandler() {
        autoConnectHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        mainMenu.scanWSD();
                        break;
                    case 1:    //can't connect
                        String strMsg = mainMenu.getString(R.string.wifiConnectFail);
                        Toast.makeText(context, strMsg, Toast.LENGTH_LONG).show();
                        break;

                }

            }
        };
    }

    public static boolean isSameConfig(WifiConfiguration wifiConfiguration, String capabilities) {
        boolean sameConfig = false;

        if (capabilities.indexOf("WPA") >= 0) {
            if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK) &&
                    !wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                if (wifiConfiguration.preSharedKey != null) {
                    if (wifiConfiguration.preSharedKey.equals("*") || wifiConfiguration.preSharedKey.length() >= 8) {
                        sameConfig = true;
                    }
                }
            }
        } else if (capabilities.indexOf("WEP") >= 0) {
            if (!wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK) &&
                    wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                if (wifiConfiguration.wepKeys[0] != null) {
                    if (wifiConfiguration.wepKeys[0].equals("*")) {
                        sameConfig = true;
                    }
                }
            }
        } else {
            if (!wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK) &&
                    wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                if (wifiConfiguration.wepKeys[0] != null && wifiConfiguration.preSharedKey != null) {
                    if (!wifiConfiguration.wepKeys[0].equals("*") && !wifiConfiguration.preSharedKey.equals("*")) {
                        sameConfig = true;
                    }
                }
            }
        }
        return sameConfig;
    }

    private class SSID {
        String name;
        String key;

        public SSID(String name, String key) {
            this.name = name;
            this.key = key;
        }

        public String getName() {
            return this.name;
        }

        public String getKey() {
            return this.key;
        }
    }

    public void clrSSID() {
        staSSIDs.clear();

    }

    public void setSSID(String name, String key) {
        SSID recSSID = new SSID(name, key);
        staSSIDs.add(recSSID);
    }

    boolean StartShowSpace = true;

    public class DownloadQueue {
        //        private final UdriverFileListViewAdapter.ViewHolder viewHolder;
        private final UdriverFileInfo udriverFileInfo;

        public DownloadQueue(UdriverFileInfo udriverFileInfo) {
//            public DownloadQueue(UdriverFileListViewAdapter.ViewHolder viewHolder, UdriverFileInfo udriverFileInfo) {
//            this.viewHolder = viewHolder;
            this.udriverFileInfo = udriverFileInfo;
        }

//        public UdriverFileListViewAdapter.ViewHolder getViewHolder() {
//            return this.viewHolder;
//        }

        public UdriverFileInfo getUdriverFileInfo() {
            return this.udriverFileInfo;
        }
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(MainMenuFragment.this.getContext());
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

    /**
     * 秒转化为常见格式
     *
     * @param time
     * @return
     */
    private String sec2time(long time) {
        int totalSeconds = (int) time;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

}
