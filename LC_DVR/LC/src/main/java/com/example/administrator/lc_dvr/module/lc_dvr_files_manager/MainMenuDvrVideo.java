package com.example.administrator.lc_dvr.module.lc_dvr_files_manager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.dxing.udriver.Udriver;
import com.dxing.wifi.api.UdriverFileInfo;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.adapter.DvrCommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.OneVideoListInfo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.VideoListInfo;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yangboru on 2017/11/6.
 */

public class MainMenuDvrVideo extends BaseFragment {
    private SwipeMenuListView videoListView;
    private DvrCommonAdapter adapter;
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
    private HashMap<Integer, Boolean> timeStateMap;
    private HttpUtils downloadManager;
    private TextView video_download_progress;
    private String filePath;
    private ImageView cancel_video_download;
    private LinearLayout download_action;
    private int visiblePosition;
    private int isDownloading = -1;
    private WaveProgressView wave_progress;
    private HttpHandler<File> downloadHandler;
    private boolean isDeleteNow;

    @Override
    protected int setViewId() {
        return R.layout.video_list_layout;
    }

    @Override
    protected void findView(View view) {
        videoListView = (SwipeMenuListView) view.findViewById(R.id.video_list);
    }

    @Override
    protected void init() {

        //是否正在删除
        isDeleteNow = false;

        //初始化下载管理器
        downloadManager = new HttpUtils();

        //判断是否要显示时间轴
        timeStateMap = new HashMap<Integer, Boolean>();

        //用来判断是否是在同一小时段
        time_tag = "-1";

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);

        adapter = new DvrCommonAdapter(this.getActivity(), VLCApplication.dvrVideoList, R.layout.file_list) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {
                if (VLCApplication.noOneFile) {
                    fileBean_List = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
                    videoName = fileBean_List.getNAME();
                    size = fileBean_List.getSIZE();
                    video_hour = videoName.substring(10, 12);
                    video_minute = videoName.substring(12, 14);
                } else {
                    oneFileBean_List = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
                    videoName = oneFileBean_List.getNAME();
                    size = oneFileBean_List.getSIZE();
                    video_hour = videoName.substring(10, 12);
                    video_minute = videoName.substring(12, 14);
                }
                thumbnail_url = "http://192.168.1.254/CARDV/MOVIE/" + videoName + "?custom=1&cmd=4001";

                helper.setImageByUrl2(R.id.video_img, thumbnail_url, MainMenuDvrVideo.this.getActivity());
                helper.setText(R.id.video_name, videoName);
                helper.setText(R.id.video_hour, video_hour);
//                helper.setText(R.id.video_minute, video_minute);
                double videoSize = (double) (size / 1024) / 1024;
                BigDecimal bigDecimal = new BigDecimal(videoSize);
                double value = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                helper.setText(R.id.video_size, value + "M");

                //设置下载信息为隐藏还是显示
                LinearLayout download_action = helper.getView(R.id.download_action);
                if (isDownloading == position) {
                    download_action.setVisibility(View.VISIBLE);
                } else {
                    download_action.setVisibility(View.GONE);
                }

                if (timeStateMap.get(position)) {
                    helper.setText(R.id.video_year, videoName.substring(0, 4));
                    helper.setText(R.id.video_month, videoName.substring(5, 7));
                    helper.setText(R.id.video_day, videoName.substring(7, 9));
                    helper.getView(R.id.video_data_seat).setVisibility(View.GONE);
                    helper.getView(R.id.video_data).setVisibility(View.VISIBLE);
                    TextView video_time1 = helper.getView(R.id.video_time1);
                    video_time1.setTextColor(getResources().getColor(R.color.orange));
                    video_time1.setText(video_hour);
                    TextView video_time2 = helper.getView(R.id.video_time2);
                    video_time2.setTextColor(getResources().getColor(R.color.orange));
                } else {
                    helper.getView(R.id.video_data_seat).setVisibility(View.VISIBLE);
                    helper.getView(R.id.video_data).setVisibility(View.GONE);
                    TextView video_time1 = helper.getView(R.id.video_time1);
                    video_time1.setTextColor(getResources().getColor(R.color.white));
                    TextView video_time2 = helper.getView(R.id.video_time2);
                    video_time2.setTextColor(getResources().getColor(R.color.white));
                }

            }
        };

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                // 创建“下载”项
                SwipeMenuItem downloadItem = new SwipeMenuItem(
                        MainMenuDvrVideo.this.getContext());
                downloadItem.setBackground(new ColorDrawable(Color.LTGRAY));
                downloadItem.setWidth(dp2px(70));
                downloadItem.setTitle(R.string.xiazai);
                downloadItem.setTitleSize(18);
                downloadItem.setTitleColor(Color.BLACK);
                // 将创建的菜单项添加进菜单中
                menu.addMenuItem(downloadItem);

                // 创建“删除”项
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        MainMenuDvrVideo.this.getContext());
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
        //如果还有下载任务就取消下载任务
        if (isDownloading != -1) {
            //设置为-1表示当前没有下载任务
            isDownloading = -1;
            //设置列表能左滑
            adapter.isLeftSlip = false;
            adapter.notifyDataSetChanged();
            //隐藏下载信息
            download_action.setVisibility(View.GONE);
            downloadHandler.cancel();
        }
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(MainMenuDvrVideo.this.getContext());
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
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        if (isDownloading == -1) {
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

                            //得到第一个可显示控件的位置，
                            visiblePosition = videoListView.getFirstVisiblePosition();
                            video_download_progress = (TextView) videoListView.getChildAt(position - visiblePosition).findViewById(R.id.video_download_progress);
                            cancel_video_download = (ImageView) videoListView.getChildAt(position - visiblePosition).findViewById(R.id.cancel_video_download);
                            download_action = (LinearLayout) videoListView.getChildAt(position - visiblePosition).findViewById(R.id.download_action);
                            wave_progress = (WaveProgressView) videoListView.getChildAt(position - visiblePosition).findViewById(R.id.wave_progress);

                            //判断文件是否存在，存在就获取它的大小
                            final File dvrFile = new File(BitmapUtils.getSDPath() + "/VOC/VIDEO/" + name);
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
                                                isDownloading = position;

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
                                                downloadHandler = downloadManager.download(filePath, BitmapUtils.getSDPath() + "/VOC/VIDEO/" + name,
                                                        new RequestCallBack<File>() {

                                                            @Override
                                                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                                                //设置为-1表示当前没有下载任务
                                                                isDownloading = -1;
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
                                                                isDownloading = -1;
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
                                                                //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
                                                                if (position - visiblePosition >= 0) {
                                                                    //显示下载进度
                                                                    wave_progress.setProgress((int) (((double) (current) / (double) (total)) * 100));
                                                                    wave_progress.hideProgressText(true);
                                                                    video_download_progress.setText((int) (((double) (current) / (double) (total)) * 100) + "%");
                                                                }
                                                                super.onLoading(total, current, isUploading);
                                                            }

                                                        });
                                                //点击取消下载时的响应
                                                cancel_video_download.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        //设置为-1表示当前没有下载任务
                                                        isDownloading = -1;
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
                                isDownloading = position;

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
                                downloadHandler = downloadManager.download(filePath, BitmapUtils.getSDPath() + "/VOC/VIDEO/" + name,
                                        new RequestCallBack<File>() {

                                            @Override
                                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                                //设置为-1表示当前没有下载任务
                                                isDownloading = -1;
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
                                                isDownloading = -1;
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
                                                //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
                                                if (position - visiblePosition >= 0) {
                                                    //显示下载进度
                                                    wave_progress.setProgress((int) (((double) (current) / (double) (total)) * 100));
                                                    wave_progress.hideProgressText(true);
                                                    video_download_progress.setText((int) (((double) (current) / (double) (total)) * 100) + "%");
                                                }
                                                super.onLoading(total, current, isUploading);
                                            }

                                        });
                                //点击取消下载时的响应
                                cancel_video_download.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //设置为-1表示当前没有下载任务
                                        isDownloading = -1;
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
                        if (isDownloading != -1) {
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
                                                    //重新加载数据
                                                    geiVideoData();
                                                }

                                                @Override
                                                public void onError(VolleyError volleyError) {
                                                    super.onError(volleyError);
                                                    if (kProgressHUD != null) {
                                                        //关闭加载对话框
                                                        kProgressHUD.dismiss();
                                                    }
//                                                    if (VLCApplication.configsDictionary.get("app-z-010") != null){
//                                                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
//                                                    }else {
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
                if (VLCApplication.noOneFile) {
                    fileBean = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(itemPosition);
                    name = fileBean.getNAME();
                } else {
                    oneFileBean = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(itemPosition);
                    name = oneFileBean.getNAME();
                }

                //通过isLeftSlip来控制下载完成才能跳转界面
                if (!adapter.isLeftSlip) {
//                    Intent intent = new Intent(MainMenuDvrVideo.this.getActivity(), RecorderPlayerActivity.class);
//                    Intent intent = new Intent(MainMenuDvrVideo.this.getActivity(), IjkPlayer.class);
                    Intent intent = new Intent(MainMenuDvrVideo.this.getActivity(), SimplePlayer.class);
                    intent.putExtra("video_play", name);
                    intent.putExtra("phone", 0);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "如需点击其他操作请等待下载完毕或取消下载", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void loadData() {
        if (PreferenceUtil.getInt("isVideoData", 0) == 0) {
            //弹出加载对话框
            //弹出加载对话框
            if (VLCApplication.configsDictionary.get("app-z-000") != null) {
                showProgress(VLCApplication.configsDictionary.get("app-z-000"));
            } else {
                showProgress("正在玩命加载中");
            }
            geiVideoData();
            //是否已加载了视频数据
            PreferenceUtil.commitInt("isVideoData", 1);
        } else {
            //获得视频文件的名字
            if (VLCApplication.noOneFile) {
                for (int i = 0; i < VLCApplication.dvrVideoList.size(); i++) {
                    VideoListInfo.LISTBean.ALLFileBean.FileBean fileBeanItem = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                    String fileBeanItemNAME = fileBeanItem.getNAME();
                    if (!time_tag.equals(fileBeanItemNAME.substring(10, 12))) {
                        timeStateMap.put(i, true);
                        time_tag = fileBeanItemNAME.substring(10, 12);
                    } else {
                        timeStateMap.put(i, false);
                    }
                }
            } else {
                for (int i = 0; i < VLCApplication.dvrVideoList.size(); i++) {
                    OneVideoListInfo.LISTBean.ALLFileBean.FileBean oneFileBeanItem = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                    String oneFileBeanItemNAME = oneFileBeanItem.getNAME();
                    if (!time_tag.equals(oneFileBeanItemNAME.substring(10, 12))) {
                        timeStateMap.put(i, true);
                        time_tag = oneFileBeanItemNAME.substring(10, 12);
                    } else {
                        timeStateMap.put(i, false);
                    }
                }
            }
        }
    }

    /**
     * 获得视频的数据
     */
    private void geiVideoData() {
        //加载数据前先清除数据
        VLCApplication.dvrVideoList.clear();
        time_tag = "-1";
        timeStateMap.clear();


        List<UdriverFileInfo> udriverFileInfos = Udriver.udriver.getFileListWithinfo();
        System.out.println("udriverFileInfos:" + udriverFileInfos.size());
        if (udriverFileInfos != null && udriverFileInfos.size() > 0) {
            for (UdriverFileInfo info : udriverFileInfos) {
                VideoListInfo.LISTBean.ALLFileBean.FileBean fileBean = new VideoListInfo.LISTBean.ALLFileBean.FileBean();

                fileBean.setNAME(info.getFileName());
                fileBean.setFPATH(info.getFilePath());
                fileBean.setSIZE(Integer.valueOf(info.getFileLength() + ""));
                fileBean.setTIME(info.getFileCreateDate() + "");
                System.out.println("info:" + info);
                VLCApplication.dvrVideoList.add(fileBean);
            }
        }
        //下面的代码是实现时间的排序
        //注意这里有一个坑：要转化的字符串必须要与SimpleDateFormat的格式一模一样
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
        Date d1;
        Date d2;
        VideoListInfo.LISTBean.ALLFileBean.FileBean temp_r;
        //做一个冒泡排序，大的在数组的前列
        for (int i = 0; i < VLCApplication.dvrVideoList.size() - 1; i++) {
            for (int j = i + 1; j < VLCApplication.dvrVideoList.size(); j++) {
                ParsePosition pos1 = new ParsePosition(0);
                ParsePosition pos2 = new ParsePosition(0);
                VideoListInfo.LISTBean.ALLFileBean.FileBean dvrFile1 = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(i);
                String substring1 = dvrFile1.getNAME().substring(0, dvrFile1.getNAME().length() - 8);
                d1 = sdf.parse(substring1, pos1);
                VideoListInfo.LISTBean.ALLFileBean.FileBean dvrFile2 = (VideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(j);
                String substring2 = dvrFile2.getNAME().substring(0, dvrFile2.getNAME().length() - 8);
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
            String fileBeanItemNAME = fileBeanItem.getNAME();
            if (!time_tag.equals(fileBeanItemNAME.substring(10, 12))) {
                timeStateMap.put(i, true);
                time_tag = fileBeanItemNAME.substring(10, 12);
            } else {
                timeStateMap.put(i, false);
            }
        }
        adapter.notifyDataSetChanged();
    }

}
