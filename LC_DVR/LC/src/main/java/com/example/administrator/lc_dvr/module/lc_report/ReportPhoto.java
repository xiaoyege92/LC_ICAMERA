package com.example.administrator.lc_dvr.module.lc_report;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.bean.Case;
import com.example.administrator.lc_dvr.bean.Casemsg;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.CommonRecyclerAdapter;
import com.example.administrator.lc_dvr.common.adapter.RecyclerViewHolder;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.retrofit.RetrofitManager;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.HorizontalListView;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.NetUtils;
import com.example.administrator.lc_dvr.common.utils.OneMoreSwitchCallBack;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.TimeUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.AppCaseData;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vov.vitamio.utils.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangboru on 2018/1/24.
 * <p>
 * 拍照界面
 */

public class ReportPhoto extends BaseFragment implements OneMoreSwitchCallBack {

    private final int HANDLER_MESSAGE_UPLOAD_SUCCESS = 1;
    private final int HANDLER_MESSAGE_REWOKE_SUCCESS = 2;
    private final int HANDLER_MESSAGE_UPLOAD_FAIL = 3;
    private final int HANDLER_MESSAGE_UPLOAD_NET_FAIL = 4;

    private HorizontalListView reportPhotoList3;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;
    private RecyclerView recyclerView4;
    private RecyclerView recyclerView5;
    private CommonRecyclerAdapter recyclerViewAdapter1;
    private CommonRecyclerAdapter recyclerViewAdapter2;
    private CommonRecyclerAdapter recyclerViewAdapter4;
    private CommonRecyclerAdapter recyclerViewAdapter5;
    private LinearLayout oneCarLayout;
    private LinearLayout moreCarLayout;
    private LinearLayoutManager layoutManager1;
    private LinearLayoutManager layoutManager2;
    private LinearLayoutManager layoutManager4;
    private LinearLayoutManager layoutManager5;
    private ImageView wholeCarPhotoSwitch;
    private ImageView wholeCarPhoto;
    private ImageView videoPhoto;
    private Button oneKeyReport;

    private List<String> reportPhotoArr1;
    private List<String> reportPhotoArr2;
    private List<String> reportPhotoArr3;
    private List<String> reportPhotoArr4;
    private List<String> reportPhotoArr5;

    private CommonAdapter adapter3;
    private LocalBroadcastManager localBroadcastManager;

    private String reportBitmap;
    private KProgressHUD kProgressHUD;
    private int currentProgress;
    private List<String> realVideoNameArr;
    private boolean isEditReport;
    private int whichImageList;
    private boolean isTakePhoto;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private ListDataSave dataSave;
    private List<String> remain_image;
    private RelativeLayout report_action;
    private int unitkind;
    private List<String> wholeCarArr;
    private RelativeLayout rl_wholeCar;
    private int isSwitchListImage;
    private List<String> reportPhotoUrlArr1;
    private List<String> reportPhotoUrlArr2;
    private List<String> reportPhotoUrlArr4;
    private List<String> reportPhotoUrlArr5;
    private List<String> wholeCarUrlArr;
    private List<String> realVideoUrlArr;
    private ImageView wholeCar_line;
    private TextView report_video_text;
    private RelativeLayout report_video_view;
    private View report_video_line;

    private TextView photo1Title;
    private TextView photo2Title;
    private TextView photo3Title;
    private TextView photo4Title;
    private TextView photo5Title;
    private View manyCarsView1;
    private RelativeLayout manyCarsView2;
    private View manyCarsView4;
    private RelativeLayout manyCarsView5;

    private NormalDialog dialog;
    private String[] mStringBts;

    private boolean isShowVideo;

    private String baseUrl;
    private String qiniuToken;
    private int onCarCount;
    private int moreCarCount;

    private ArrayList<String> arrReport1;
    private ArrayList<String> arrReport2;
    private ArrayList<String> arrReport4;
    private ArrayList<String> arrReport5;
    private String strWholeCar;
    private String strVideo;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_UPLOAD_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    oneKeyReport.setEnabled(true);
                    break;
                case HANDLER_MESSAGE_REWOKE_SUCCESS:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    oneKeyReport.setEnabled(true);
                    if (VLCApplication.configsDictionary.get("app-c-171") != null) {
                        Toast.makeText(getContext(), VLCApplication.configsDictionary.get("app-c-171"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "未上传成功，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case HANDLER_MESSAGE_UPLOAD_NET_FAIL:
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                        currentProgress = 0;
                    }
                    oneKeyReport.setEnabled(true);
                    if (VLCApplication.configsDictionary.get("app-z-010") != null) {
                        Toast.makeText(getActivity(), VLCApplication.configsDictionary.get("app-z-010"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "连接失败，请检查您的网络连接", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public ReportPhoto() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    @Override
    protected int setViewId() {
        return R.layout.reportphoto_layout;
    }

    @Override
    protected void findView(View view) {
        // 单车照片布局
        oneCarLayout = (LinearLayout) view.findViewById(R.id.oneCarLayout);
        // 多车照片布局
        moreCarLayout = (LinearLayout) view.findViewById(R.id.moreCarLayout);
        //横向列表3
        reportPhotoList3 = (HorizontalListView) view.findViewById(R.id.reportPhotoList3);

        recyclerView1 = (RecyclerView) view.findViewById(R.id.recyclerView1);
        recyclerView2 = (RecyclerView) view.findViewById(R.id.recyclerView2);
        recyclerView4 = (RecyclerView) view.findViewById(R.id.recyclerView4);
        recyclerView5 = (RecyclerView) view.findViewById(R.id.recyclerView5);
        //多车图片view
        manyCarsView1 = (View) view.findViewById(R.id.manyCarsView1);
        manyCarsView2 = (RelativeLayout) view.findViewById(R.id.manyCarsView2);
        manyCarsView4 = (View) view.findViewById(R.id.manyCarsView4);
        manyCarsView5 = (RelativeLayout) view.findViewById(R.id.manyCarsView5);
        // 全车照片切换按钮
        wholeCarPhotoSwitch = (ImageView) view.findViewById(R.id.wholeCarPhotoSwitch);
        //全车照片
        wholeCarPhoto = (ImageView) view.findViewById(R.id.wholeCarPhoto);
        rl_wholeCar = (RelativeLayout) view.findViewById(R.id.rl_wholeCar);
        //全车照片的线
        wholeCar_line = (ImageView) view.findViewById(R.id.wholeCar_line);
        //一键报案按钮
        oneKeyReport = (Button) view.findViewById(R.id.oneKeyReport);
        //照片title
        photo1Title = (TextView) view.findViewById(R.id.photo1Title);
        photo2Title = (TextView) view.findViewById(R.id.photo2Title);
        photo3Title = (TextView) view.findViewById(R.id.photo3Title);
        photo4Title = (TextView) view.findViewById(R.id.photo4Title);
        photo5Title = (TextView) view.findViewById(R.id.photo5Title);
        //最底下的layout
        report_action = (RelativeLayout) view.findViewById(R.id.report_action);
        //视频的提示文字
        report_video_text = (TextView) view.findViewById(R.id.report_video_text);
        //视频的标题view
        report_video_view = (RelativeLayout) view.findViewById(R.id.report_video_view);
        //视频的分割线
        report_video_line = (View) view.findViewById(R.id.report_video_line);
    }

    @Override
    protected void init() {

        Utils.setOneMoreSwitchCallBack(ReportPhoto.this);

        arrReport1 = new ArrayList<>();
        arrReport2 = new ArrayList<>();
        arrReport4 = new ArrayList<>();
        arrReport5 = new ArrayList<>();

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.queding);
        mStringBts[1] = getString(R.string.quxiao);
        dialog = new NormalDialog(getActivity());
        //是否是切换列表中的图片，当等于-1时就表示不是
        isSwitchListImage = -1;
        //全车照片url数组
        wholeCarArr = new ArrayList<>();
        //用来保存list到本地
        dataSave = new ListDataSave(getContext(), "baiyu");
        //判断是否要在图片查看界面隐藏删除按钮
        PreferenceUtil.commitBoolean("isEditReport", true);

        //判断是否进入了拍照界面
        isTakePhoto = true;

        //判断是哪一个图片列表
        whichImageList = 0;

        //录屏视频的真实名字
        realVideoNameArr = new ArrayList();

        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        //动态注册本地广播接收器
        intentFilter = new IntentFilter();
        intentFilter.addAction("ReportPhoto");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        //横向列表1的图片数组
        reportPhotoArr1 = new ArrayList<>();
        //横向列表2的图片数组
        reportPhotoArr2 = new ArrayList<>();
        //横向列表3的图片数组
        reportPhotoArr3 = new ArrayList<>();
        //横向列表4的图片数组
        reportPhotoArr4 = new ArrayList<>();
        //横向列表5的图片数组
        reportPhotoArr5 = new ArrayList<>();

        //初始化列表
        reportPhotoUrlArr1 = new ArrayList<>();
        reportPhotoUrlArr2 = new ArrayList<>();
        reportPhotoUrlArr4 = new ArrayList<>();
        reportPhotoUrlArr5 = new ArrayList<>();
        wholeCarUrlArr = new ArrayList<>();
        realVideoUrlArr = new ArrayList<>();
        //给图片数组赋初始值
        reportBitmap = "file://" + BitmapUtils.getSDPath() + "/VOC/Cache/report_image.png";

        recyclerViewAdapter1 = new CommonRecyclerAdapter(getActivity(), R.layout.reportphoto1_item, reportPhotoArr1) {
            @Override
            public void convert(RecyclerViewHolder helper, Object item, final int position) {
                //获得item中的控件
                ImageView report_image1 = helper.getView(R.id.report_image1);
                ImageView delete_image1 = helper.getView(R.id.delete_image1);

                //如果是最后一个cell，就把deleteImage隐藏
                if ((position == reportPhotoArr1.size() - 1) && isEditReport) {
                    delete_image1.setVisibility(View.GONE);
                    helper.setImageResource(R.id.report_image1, R.mipmap.report_image);
                } else {
                    delete_image1.setVisibility(View.VISIBLE);
                    helper.setImageByUrl(R.id.report_image1, reportPhotoArr1.get(position), getContext());
                }

                //点击列表1中reportImage时的响应方法
                report_image1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TimeUtils.isFastClick()) {
                            return;
                        }
                        //判断是那个list
                        whichImageList = 1;
                        PreferenceUtil.commitInt("whichList", 1);
                        if (position == reportPhotoArr1.size() - 1) {
                            //判断是否要禁止编辑
                            if (!isEditReport) {
                                isTakePhoto = false;
                                //保存当前的reportPhotoArr1
                                dataSave.setDataList("LookImageList", reportPhotoArr1);
                                //从当前的界面跳转到Identifier为"look_image_list"的界面
                                Intent intent = new Intent(getContext(), LookImageList.class);
                                intent.putExtra("current", position);
                                startActivity(intent);
                                PreferenceUtil.commitInt("numOfPages", reportPhotoArr1.size() - 1);
                            } else {
                                //是否是切换列表中的图片
                                isSwitchListImage = -1;
                                isTakePhoto = true;
                                //从当前的界面跳转到Identifier为"take_report_photo"的界面
                                Intent intent = new Intent(getContext(), TakeReportPhoto.class);
                                startActivity(intent);
                            }
                        } else {
                            isTakePhoto = false;
                            //保存当前的reportPhotoArr1
                            dataSave.setDataList("LookImageList", reportPhotoArr1);
                            //从当前的界面跳转到Identifier为"look_image_list"的界面
                            Intent intent = new Intent(getContext(), LookImageList.class);
                            intent.putExtra("current", position);
                            startActivity(intent);
                            PreferenceUtil.commitInt("numOfPages", reportPhotoArr1.size() - 1);
                        }
                    }
                });

                //点击列表1中deleteImage时的响应方法
                delete_image1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除对应项
                        reportPhotoArr1.remove(position);
                        dataSave.setDataList("reportPhotoArr1", reportPhotoArr1);
                        //删除缓存图片
                        File hitImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/hitImage" + position + ".png");
                        if (hitImageFile.exists()) {
                            hitImageFile.delete();
                        }
                        //更新数据
                        recyclerViewAdapter1.notifyDataSetChanged();
                    }
                });

                //判断是否要禁止编辑
                if (!isEditReport) {
                    delete_image1.setVisibility(View.GONE);
                }
            }
        };
        layoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        //设置RecyclerView管理器
        recyclerView1.setLayoutManager(layoutManager1);
        recyclerView1.setAdapter(recyclerViewAdapter1);

        layoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerViewAdapter2 = new CommonRecyclerAdapter(getActivity(), R.layout.reportphoto2_item, reportPhotoArr2) {
            @Override
            public void convert(RecyclerViewHolder helper, Object t, final int position) {

                //获得item中的控件
                ImageView report_image2 = helper.getView(R.id.report_image2);
                ImageView delete_image2 = helper.getView(R.id.delete_image2);

                //如果是最后一个cell，就把deleteImage隐藏
                if ((position == reportPhotoArr2.size() - 1) && isEditReport) {
                    delete_image2.setVisibility(View.GONE);
                    helper.setImageResource(R.id.report_image2, R.mipmap.report_image);
                } else {
                    delete_image2.setVisibility(View.VISIBLE);
                    helper.setImageByUrl(R.id.report_image2, reportPhotoArr2.get(position), getContext());
                }

                //点击列表2中reportImage时的响应方法
                report_image2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TimeUtils.isFastClick()) {
                            return;
                        }
                        //判断是那个list
                        whichImageList = 2;
                        PreferenceUtil.commitInt("whichList", 2);
                        if (position == reportPhotoArr2.size() - 1) {
                            //判断是否要禁止编辑
                            if (!isEditReport) {
                                isTakePhoto = false;
                                //保存当前的reportPhotoArr2
                                dataSave.setDataList("LookImageList", reportPhotoArr2);
                                //从当前的界面跳转到Identifier为"look_image_list"的界面
                                Intent intent = new Intent(getContext(), LookImageList.class);
                                intent.putExtra("current", position);
                                startActivity(intent);
                                PreferenceUtil.commitInt("numOfPages", reportPhotoArr2.size() - 1);
                            } else {
                                //是否是切换列表中的图片
                                isSwitchListImage = -1;
                                isTakePhoto = true;
                                //从当前的界面跳转到Identifier为"take_report_photo"的界面
                                Intent intent = new Intent(getContext(), TakeReportPhoto.class);
                                startActivity(intent);
                            }
                        } else {
                            isTakePhoto = false;
                            //保存当前的reportPhotoArr2
                            dataSave.setDataList("LookImageList", reportPhotoArr2);
                            //从当前的界面跳转到Identifier为"look_image_list"的界面
                            Intent intent = new Intent(getContext(), LookImageList.class);
                            intent.putExtra("current", position);
                            startActivity(intent);
                            PreferenceUtil.commitInt("numOfPages", reportPhotoArr2.size() - 1);
                        }
                    }
                });

                //点击列表2中deleteImage时的响应方法
                delete_image2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除对应项
                        reportPhotoArr2.remove(position);
                        dataSave.setDataList("reportPhotoArr2", reportPhotoArr2);
                        //删除缓存图片
                        File hitImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/damagedImage" + position + ".png");
                        if (hitImageFile.exists()) {
                            hitImageFile.delete();
                        }
                        //更新数据
                        recyclerViewAdapter2.notifyDataSetChanged();
                    }
                });

                //判断是否要禁止编辑
                if (!isEditReport) {
                    delete_image2.setVisibility(View.GONE);
                }
            }
        };
        recyclerView2.setAdapter(recyclerViewAdapter2);
        //列表3的适配器
        adapter3 = new CommonAdapter(this.getActivity(), reportPhotoArr3, R.layout.reportphoto3_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {

                //获得item中的控件
                final ImageView report_image3 = helper.getView(R.id.report_image3);
                ImageView delete_image3 = helper.getView(R.id.delete_image3);

                if (reportPhotoArr3.get(position).contains("https")) {
                    //判断本地缓存中是否存在图片
                    if (new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + reportPhotoArr3.get(position).substring(57) + ".png").exists()) {
                        //从缓存中获得图片
                        report_image3.setImageURI(Uri.fromFile(new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + reportPhotoArr3.get(position).substring(57) + ".png")));

                    } else {
                        //获得网络缩略图
                        helper.setVideoImageByUrl(R.id.report_image3, reportPhotoArr3.get(position), getContext());
                    }
                } else {
                    if (!realVideoNameArr.isEmpty()) {
                        helper.setImageByUrl(R.id.report_image3, reportPhotoArr3.get(position), getContext());
                    }
                }

                if (realVideoNameArr != null && realVideoNameArr.size() != 0 && isEditReport) {
                    delete_image3.setVisibility(View.VISIBLE);
                } else {
                    delete_image3.setVisibility(View.GONE);
                }
                //点击列表3中deleteImage时的响应方法
                delete_image3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除对应项
                        reportPhotoArr3.remove(position);
                        realVideoNameArr.remove(position);
                        dataSave.setDataList("reportPhotoArr3", reportPhotoArr3);
                        dataSave.setDataList("realVideoNameArr", realVideoNameArr);
                        //更新数据
                        reportPhotoArr3.add(reportBitmap);
                        isShowVideo = true;
                        adapter3.notifyDataSetChanged();
                    }
                });

                report_image3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TimeUtils.isFastClick()) {
                            return;
                        }
                        //判断是那个list
                        whichImageList = 3;
                        if (position == reportPhotoArr3.size() - 1) {
                            //判断是否要禁止编辑
                            if (!isEditReport || !isShowVideo) {
                                PreferenceUtil.commitBoolean("isReport", true);
                                //跳转到视频播放界面
                                Intent intent = new Intent(getActivity(), ReportIjkPlayerActivity.class);
                                if (realVideoNameArr.get(position).contains("mp4")) {
                                    intent.putExtra("is_recorder", 1);
                                }
                                intent.putExtra("phone", 1);
                                intent.putExtra("video_play", realVideoNameArr.get(position));
                                startActivity(intent);
                            } else {
                                isTakePhoto = false;
                                //从当前的界面跳转到Identifier为"to_recording_files"的界面
                                Intent intent = new Intent(getContext(), RecordingFiles.class);
                                startActivity(intent);
                                PreferenceUtil.commitInt("whichList", 3);
                                dataSave.setDataList("alreadyChosenFile", realVideoNameArr);
                            }
                        } else {
                            PreferenceUtil.commitBoolean("isReport", true);
                            //跳转到视频播放界面
                            Intent intent = new Intent(getActivity(), ReportIjkPlayerActivity.class);
                            if (realVideoNameArr.get(position).contains("mp4")) {
                                intent.putExtra("is_recorder", 1);
                            }
                            intent.putExtra("phone", 1);
                            intent.putExtra("video_play", realVideoNameArr.get(position));
                            startActivity(intent);
                        }
                    }
                });
            }
        };
        reportPhotoList3.setAdapter(adapter3);

        layoutManager4 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView4.setLayoutManager(layoutManager4);
        recyclerViewAdapter4 = new CommonRecyclerAdapter(this.getActivity(), R.layout.reportphoto4_item, reportPhotoArr4) {
            @Override
            public void convert(RecyclerViewHolder helper, Object t, final int position) {
                //获得item中的控件
                ImageView report_image4 = helper.getView(R.id.report_image4);
                ImageView delete_image4 = helper.getView(R.id.delete_image4);

                //如果是最后一个cell，就把deleteImage隐藏
                if ((position == reportPhotoArr4.size() - 1) && isEditReport) {
                    delete_image4.setVisibility(View.GONE);
                    helper.setImageResource(R.id.report_image4, R.mipmap.report_image);
                } else {
                    delete_image4.setVisibility(View.VISIBLE);
                    helper.setImageByUrl(R.id.report_image4, reportPhotoArr4.get(position), getContext());
                }

                //点击列表4中reportImage时的响应方法
                report_image4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TimeUtils.isFastClick()) {
                            return;
                        }
                        //判断是那个list
                        whichImageList = 4;
                        PreferenceUtil.commitInt("whichList", 4);
                        if (position == reportPhotoArr4.size() - 1) {
                            //判断是否要禁止编辑
                            if (!isEditReport) {
                                isTakePhoto = false;
                                //保存当前的reportPhotoArr4
                                dataSave.setDataList("LookImageList", reportPhotoArr4);
                                //从当前的界面跳转到Identifier为"look_image_list"的界面
                                Intent intent = new Intent(getContext(), LookImageList.class);
                                intent.putExtra("current", position);
                                startActivity(intent);
                                PreferenceUtil.commitInt("numOfPages", reportPhotoArr4.size() - 1);
                            } else {
                                //是否是切换列表中的图片
                                isSwitchListImage = -1;
                                isTakePhoto = true;
                                //从当前的界面跳转到Identifier为"take_report_photo"的界面
                                Intent intent = new Intent(getContext(), TakeReportPhoto.class);
                                startActivity(intent);
                            }
                        } else {
                            isTakePhoto = false;
                            //保存当前的reportPhotoArr4
                            dataSave.setDataList("LookImageList", reportPhotoArr4);
                            //从当前的界面跳转到Identifier为"look_image_list"的界面
                            Intent intent = new Intent(getContext(), LookImageList.class);
                            intent.putExtra("current", position);
                            startActivity(intent);
                            PreferenceUtil.commitInt("numOfPages", reportPhotoArr4.size() - 1);
                        }
                    }
                });

                //点击列表4中deleteImage时的响应方法
                delete_image4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除对应项
                        reportPhotoArr4.remove(position);
                        dataSave.setDataList("reportPhotoArr4", reportPhotoArr4);
                        //删除缓存图片
                        File hitImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/damaged4Image" + position + ".png");
                        if (hitImageFile.exists()) {
                            hitImageFile.delete();
                        }
                        //更新数据
                        recyclerViewAdapter4.notifyDataSetChanged();
                    }
                });

                //判断是否要禁止编辑
                if (!isEditReport) {
                    delete_image4.setVisibility(View.GONE);
                }
            }
        };
        recyclerView4.setAdapter(recyclerViewAdapter4);

        layoutManager5 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView5.setLayoutManager(layoutManager5);
        recyclerViewAdapter5 = new CommonRecyclerAdapter(this.getActivity(), R.layout.reportphoto5_item, reportPhotoArr5) {
            @Override
            public void convert(RecyclerViewHolder helper, Object t, final int position) {
                //获得item中的控件
                ImageView report_image5 = helper.getView(R.id.report_image5);
                ImageView delete_image5 = helper.getView(R.id.delete_image5);

                //如果是最后一个cell，就把deleteImage隐藏
                if ((position == reportPhotoArr5.size() - 1) && isEditReport) {
                    delete_image5.setVisibility(View.GONE);
                    helper.setImageResource(R.id.report_image5, R.mipmap.report_image);
                } else {
                    delete_image5.setVisibility(View.VISIBLE);
                    helper.setImageByUrl(R.id.report_image5, reportPhotoArr5.get(position), getContext());
                }

                //点击列表5中reportImage时的响应方法
                report_image5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TimeUtils.isFastClick()) {
                            return;
                        }
                        //判断是那个list
                        whichImageList = 5;
                        PreferenceUtil.commitInt("whichList", 5);
                        if (position == reportPhotoArr5.size() - 1) {
                            //判断是否要禁止编辑
                            if (!isEditReport) {
                                isTakePhoto = false;
                                //保存当前的reportPhotoArr5
                                dataSave.setDataList("LookImageList", reportPhotoArr5);
                                //从当前的界面跳转到Identifier为"look_image_list"的界面
                                Intent intent = new Intent(getContext(), LookImageList.class);
                                intent.putExtra("current", position);
                                startActivity(intent);
                                PreferenceUtil.commitInt("numOfPages", reportPhotoArr5.size() - 1);
                            } else {
                                //是否是切换列表中的图片
                                isSwitchListImage = -1;
                                isTakePhoto = true;
                                //从当前的界面跳转到Identifier为"take_report_photo"的界面
                                Intent intent = new Intent(getContext(), TakeReportPhoto.class);
                                startActivity(intent);
                            }
                        } else {
                            isTakePhoto = false;
                            //保存当前的reportPhotoArr5
                            dataSave.setDataList("LookImageList", reportPhotoArr5);
                            //从当前的界面跳转到Identifier为"look_image_list"的界面
                            Intent intent = new Intent(getContext(), LookImageList.class);
                            intent.putExtra("current", position);
                            startActivity(intent);
                            PreferenceUtil.commitInt("numOfPages", reportPhotoArr5.size() - 1);
                        }
                    }
                });

                //点击列表5中deleteImage时的响应方法
                delete_image5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //删除对应项
                        reportPhotoArr5.remove(position);
                        dataSave.setDataList("reportPhotoArr5", reportPhotoArr5);
                        //删除缓存图片
                        File hitImageFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/damaged5Image" + position + ".png");
                        if (hitImageFile.exists()) {
                            hitImageFile.delete();
                        }
                        //更新数据
                        recyclerViewAdapter5.notifyDataSetChanged();
                    }
                });

                //判断是否要禁止编辑
                if (!isEditReport) {
                    delete_image5.setVisibility(View.GONE);
                }
            }
        };
        recyclerView5.setAdapter(recyclerViewAdapter5);

        //多车和单车是两种不同的界面显示方式
        if (AppCaseData.carCount == 2) {
            //显示多车图片view
            manyCarsView1.setVisibility(View.VISIBLE);
            manyCarsView2.setVisibility(View.VISIBLE);
            manyCarsView4.setVisibility(View.VISIBLE);
            manyCarsView5.setVisibility(View.VISIBLE);

            recyclerView4.setVisibility(View.VISIBLE);
            recyclerView5.setVisibility(View.VISIBLE);
            //照片1
            photo1Title.setText("撞击部位45度照片：");
            //照片2
            photo2Title.setText("自己车带牌照照片：");
            //照片3
            photo3Title.setText("对方车带牌照照片：");
            //照片4
            photo4Title.setText("自己车受损部位照片：");
            //照片5
            photo5Title.setText("对方车受损部位照片：");
        } else {
            //隐藏多车图片view
            manyCarsView1.setVisibility(View.GONE);
            manyCarsView2.setVisibility(View.GONE);
            manyCarsView4.setVisibility(View.GONE);
            manyCarsView5.setVisibility(View.GONE);
            recyclerView4.setVisibility(View.GONE);
            recyclerView5.setVisibility(View.GONE);
            //照片1
            photo1Title.setText("被撞物体照片：");
            //照片2
            photo2Title.setText("全车45度带车牌照片：");
            //照片3
            photo3Title.setText("受损部位照片：");
        }

        getCasedetail();
    }

    /**
     * 获得事故ID的具体内容
     */
    private void getCasedetail() {
        Map<String, String> map = new HashMap<>();
        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getCaseDetail(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        // 如果是正式报案
                        if ("0".equals(PreferenceUtil.getString("isdemo", "0"))) {
                            JSONObject datas = jsonObject.getJSONObject("datas");

                            //报案时间
                            AppCaseData.reportTime = Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("casedate"));
                            // 事故时间
                            AppCaseData.accidentTime = (Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("accidentdate")));
                            // 报案人姓名
                            AppCaseData.informantName = (Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("username")));
                            // 报案人电话
                            AppCaseData.informantPhone = (Utils.parseStr(jsonObject.getJSONObject("datas").getJSONObject("case").getString("mobile")));

                            // 出险地点
                            AppCaseData.geographicalPosition = Utils.parseStr(datas.getJSONObject("case").getString("accidentaddress"));
                            // 车牌号
                            AppCaseData.plateNumber = Utils.parseStr(datas.getJSONObject("case").getString("carnumber"));
                            // label_models 厂型车牌
                            AppCaseData.label_models = Utils.parseStr(datas.getJSONObject("case").getString("cartype"));
                            // 对方车牌
                            AppCaseData.other_plate_number = Utils.parseStr(datas.getJSONObject("case").getString("threeCarnumber"));
                            // 对方厂牌车型
                            AppCaseData.other_label_models = Utils.parseStr(datas.getJSONObject("case").getString("threeCartype"));
                            // 对方姓名
                            AppCaseData.other_name = Utils.parseStr(datas.getJSONObject("case").getString("threeUsername"));
                            // 对方手机号
                            AppCaseData.other_phone_number = Utils.parseStr(datas.getJSONObject("case").getString("threeMobile"));

                            // 事故信息
                            //事故类型
                            if ("单车".equals(datas.getJSONObject("case").getString("accidentkind"))) {
                                AppCaseData.carCount = 1;
                            } else {
                                AppCaseData.carCount = 2;
                            }
                            // 事故责任类型
                            if ("全部责任".equals(datas.getJSONObject("case").getString("accident"))) {
                                AppCaseData.accidentResponsibility = 1;
                            } else if ("没有责任".equals(datas.getJSONObject("case").getString("accident"))) {
                                AppCaseData.accidentResponsibility = 2;
                            } else if ("主要责任".equals(datas.getJSONObject("case").getString("accident"))) {
                                AppCaseData.accidentResponsibility = 3;
                            } else if ("同等责任".equals(datas.getJSONObject("case").getString("accident"))) {
                                AppCaseData.accidentResponsibility = 4;
                            } else if ("次要责任".equals(datas.getJSONObject("case").getString("accident"))) {
                                AppCaseData.accidentResponsibility = 5;
                            }
                            // 是否有物损
                            if ("是".equals(datas.getJSONObject("case").getString("goodinjure"))) {
                                AppCaseData.isPhysicalDamage = 1;
                            } else {
                                AppCaseData.isPhysicalDamage = 2;
                            }
                            // 是否有人伤
                            if ("是".equals(datas.getJSONObject("case").getString("personinjure"))) {
                                AppCaseData.isWounded = 1;
                            } else {
                                AppCaseData.isWounded = 2;
                            }
                            //是否事故现场
                            if ("是".equals(datas.getJSONObject("case").getString("accidentscene"))) {
                                AppCaseData.isScene = 1;
                            } else {
                                AppCaseData.isScene = 2;
                            }
                            // 是否能正常行驶
                            if ("是".equals(datas.getJSONObject("case").getString("carcanmove"))) {
                                AppCaseData.isNormalDriving = 1;
                            } else {
                                AppCaseData.isNormalDriving = 2;
                            }

                            JSONObject casepictures = datas.getJSONObject("casepictures");
                            //分多车和单车两种情况
                            if (AppCaseData.carCount == 2) {
                                //保存从服务器获得已上传的图片url
                                JSONArray more_a = casepictures.getJSONArray("more_a");
                                for (int i = 0; i < more_a.length(); i++) {
                                    JSONObject value = (JSONObject) more_a.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> more_aMap = new HashMap<>();
                                    more_aMap.put("attachid", piclink);
                                    VLCApplication.more_a.add(more_aMap);
                                    reportPhotoArr1.add(Config.QINIU_BASE_URL + piclink);
                                }
                                JSONArray more_b = casepictures.getJSONArray("more_b");
                                for (int i = 0; i < more_b.length(); i++) {
                                    JSONObject value = (JSONObject) more_b.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> more_bMap = new HashMap<>();
                                    more_bMap.put("attachid", piclink);
                                    VLCApplication.more_b.add(more_bMap);

                                    wholeCarArr.add(Config.QINIU_BASE_URL + piclink);
                                }
                                if (!wholeCarArr.isEmpty()) {
                                    //从网络中获得图片
                                    ImageLoader.getInstance().displayImage(wholeCarArr.get(0), wholeCarPhoto);
                                }

                                JSONArray more_c = casepictures.getJSONArray("more_c");
                                for (int i = 0; i < more_c.length(); i++) {
                                    JSONObject value = (JSONObject) more_c.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> more_cMap = new HashMap<>();
                                    more_cMap.put("attachid", piclink);
                                    VLCApplication.more_c.add(more_cMap);
                                    reportPhotoArr2.add(Config.QINIU_BASE_URL + piclink);
                                }

                                JSONArray more_d = casepictures.getJSONArray("more_d");
                                for (int i = 0; i < more_d.length(); i++) {
                                    JSONObject value = (JSONObject) more_d.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> more_dMap = new HashMap<>();
                                    more_dMap.put("attachid", piclink);
                                    VLCApplication.more_d.add(more_dMap);
                                    reportPhotoArr4.add(Config.QINIU_BASE_URL + piclink);
                                }

                                JSONArray more_e = casepictures.getJSONArray("more_e");
                                for (int i = 0; i < more_e.length(); i++) {
                                    JSONObject value = (JSONObject) more_e.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> more_eMap = new HashMap<>();
                                    more_eMap.put("attachid", piclink);
                                    VLCApplication.more_e.add(more_eMap);
                                    reportPhotoArr5.add(Config.QINIU_BASE_URL + piclink);
                                }

                                //保存从服务器获得已上传的视频url
                                JSONArray video = casepictures.getJSONArray("video");
                                for (int i = 0; i < video.length(); i++) {
                                    JSONObject value = (JSONObject) video.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> videoMap = new HashMap<>();
                                    videoMap.put("attachid", piclink);
                                    VLCApplication.video.add(videoMap);
                                    if (piclink != null && !piclink.equals("") && !"null".equals(piclink)) {
                                        realVideoNameArr.add(Config.QINIU_BASE_URL + piclink);
                                        reportPhotoArr3.add(Config.QINIU_BASE_URL + piclink + Config.QINIU_VIDEO_THUMB);
                                    }
                                }
                                if (!reportPhotoArr3.isEmpty()) {
                                    ImageLoader.getInstance().displayImage(reportPhotoArr3.get(0), videoPhoto);
                                }

                            } else if (AppCaseData.carCount == 1) {

                                //保存从服务器获得已上传的图片url
                                JSONArray one_a = casepictures.getJSONArray("one_a");
                                for (int i = 0; i < one_a.length(); i++) {
                                    JSONObject value = (JSONObject) one_a.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> one_aMap = new HashMap<>();
                                    one_aMap.put("attachid", piclink);
                                    VLCApplication.one_a.add(one_aMap);

                                    wholeCarArr.add(Config.QINIU_BASE_URL + piclink);
                                }
                                if (!wholeCarArr.isEmpty()) {
                                    //从网络中获得图片
                                    ImageLoader.getInstance().displayImage(wholeCarArr.get(0), wholeCarPhoto);
                                }
                                JSONArray one_b = casepictures.getJSONArray("one_b");
                                for (int i = 0; i < one_b.length(); i++) {
                                    JSONObject value = (JSONObject) one_b.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> one_bMap = new HashMap<>();
                                    one_bMap.put("attachid", piclink);
                                    VLCApplication.one_b.add(one_bMap);
                                    reportPhotoArr1.add(Config.QINIU_BASE_URL + piclink);
                                }

                                JSONArray one_c = casepictures.getJSONArray("one_c");
                                for (int i = 0; i < one_c.length(); i++) {
                                    JSONObject value = (JSONObject) one_c.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> one_cMap = new HashMap<>();
                                    one_cMap.put("attachid", piclink);
                                    VLCApplication.one_c.add(one_cMap);
                                    reportPhotoArr2.add(Config.QINIU_BASE_URL + piclink);
                                }

                                //保存从服务器获得已上传的视频url
                                JSONArray video = casepictures.getJSONArray("video");
                                for (int i = 0; i < video.length(); i++) {
                                    JSONObject value = (JSONObject) video.get(i);
                                    String piclink = value.getString("attachid");
                                    //保存attachid，上传图片时有用
                                    Map<String, String> videoMap = new HashMap<>();
                                    videoMap.put("attachid", piclink);
                                    VLCApplication.video.add(videoMap);
                                    if (piclink != null && !piclink.equals("") && !"null".equals(piclink)) {
                                        realVideoNameArr.add(Config.QINIU_BASE_URL + piclink);
                                        reportPhotoArr3.add(Config.QINIU_BASE_URL + piclink + Config.QINIU_VIDEO_THUMB);
                                    }
                                }
                                if (!reportPhotoArr3.isEmpty()) {
                                    ImageLoader.getInstance().displayImage(reportPhotoArr3.get(0), videoPhoto);
                                }
                                //刷新数据
                                adapter3.notifyDataSetChanged();
                            }

                            // 定损和报案 状态
                            String lossStatus = datas.getJSONObject("case").getString("losestatus");
                            String reportStatus = datas.getJSONObject("case").getString("reportstatus");
                            // 理赔状态
                            String paylossStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("paylossstatus");
                            // ((|报案状态|非空 || <>已销案) || (|定损状态|非空 || <>已销案)) &&  |理赔状态|<>已结案 ，才显示并可操作
                            if (((!"0".equals(lossStatus) && !"9".equals(lossStatus)) || (!"0".equals(reportStatus) && !"9".equals(reportStatus)))
                                    && !"2".equals(paylossStatus)) {
                                // 是否可编辑
                                isEditReport = true;
                                // 在拍照页是否显示删除照片
                                PreferenceUtil.commitBoolean("isEditReport", true);
                                report_action.setVisibility(View.VISIBLE);

                                //给图片数组赋初始值
                                reportPhotoArr1.add(reportBitmap);
                                reportPhotoArr2.add(reportBitmap);
                                reportPhotoArr4.add(reportBitmap);
                                reportPhotoArr5.add(reportBitmap);

                                if (!wholeCarArr.isEmpty()) {
                                    wholeCarPhotoSwitch.setVisibility(View.VISIBLE);
                                } else {
                                    wholeCarArr.add(reportBitmap);
                                    wholeCarPhotoSwitch.setVisibility(View.GONE);
                                }
                                if (reportPhotoArr3.size() == 0) {
                                    reportPhotoArr3.add(reportBitmap);
                                    isShowVideo = true;
                                } else {
                                    isShowVideo = false;
                                }
                            } else {
                                isEditReport = false;
                                PreferenceUtil.commitBoolean("isEditReport", false);
                                report_action.setVisibility(View.GONE);
                                if (wholeCarArr.isEmpty()) {
                                    rl_wholeCar.setVisibility(View.GONE);
                                }
                                wholeCarPhotoSwitch.setVisibility(View.GONE);

                                if (0 == reportPhotoArr1.size()) {
                                    recyclerView1.setVisibility(View.GONE);
                                }
                                if (0 == reportPhotoArr2.size()) {
                                    recyclerView2.setVisibility(View.GONE);
                                }
                                if (0 == reportPhotoArr4.size()) {
                                    recyclerView4.setVisibility(View.GONE);
                                }
                                if (0 == reportPhotoArr5.size()) {
                                    recyclerView5.setVisibility(View.GONE);
                                }

                                if (reportPhotoArr3.size() == 0) {
                                    reportPhotoList3.setVisibility(View.GONE);
                                    isShowVideo = true;
                                } else {
                                    isShowVideo = false;
                                }
                            }
                            //刷新数据
                            recyclerViewAdapter1.notifyDataSetChanged();
                            recyclerViewAdapter2.notifyDataSetChanged();
                            recyclerViewAdapter4.notifyDataSetChanged();
                            recyclerViewAdapter5.notifyDataSetChanged();
                            adapter3.notifyDataSetChanged();

                            dataSave.setDataList("reportPhotoArr1", reportPhotoArr1);
                            dataSave.setDataList("reportPhotoArr2", reportPhotoArr2);
                            dataSave.setDataList("reportPhotoArr4", reportPhotoArr4);
                            dataSave.setDataList("reportPhotoArr5", reportPhotoArr5);
                            dataSave.setDataList("wholeCarArr", wholeCarArr);
                            dataSave.setDataList("realVideoNameArr", realVideoNameArr);
                        } else {
                            // 是否可编辑
                            isEditReport = true;
                            // 在拍照页是否显示删除照片
                            PreferenceUtil.commitBoolean("isEditReport", true);
                            report_action.setVisibility(View.VISIBLE);

                            //给图片数组赋初始值
                            reportPhotoArr1.add(reportBitmap);
                            reportPhotoArr2.add(reportBitmap);
                            reportPhotoArr4.add(reportBitmap);
                            reportPhotoArr5.add(reportBitmap);

                            if (!wholeCarArr.isEmpty()) {
                                wholeCarPhotoSwitch.setVisibility(View.VISIBLE);
                            } else {
                                wholeCarArr.add(reportBitmap);
                                wholeCarPhotoSwitch.setVisibility(View.GONE);
                            }
                            if (reportPhotoArr3.size() == 0) {
                                reportPhotoArr3.add(reportBitmap);
                                isShowVideo = true;
                            } else {
                                isShowVideo = false;
                            }

                            //刷新数据
                            recyclerViewAdapter1.notifyDataSetChanged();
                            recyclerViewAdapter2.notifyDataSetChanged();
                            recyclerViewAdapter4.notifyDataSetChanged();
                            recyclerViewAdapter5.notifyDataSetChanged();
                            adapter3.notifyDataSetChanged();

                            dataSave.setDataList("reportPhotoArr1", reportPhotoArr1);
                            dataSave.setDataList("reportPhotoArr2", reportPhotoArr2);
                            dataSave.setDataList("reportPhotoArr4", reportPhotoArr4);
                            dataSave.setDataList("reportPhotoArr5", reportPhotoArr5);
                            dataSave.setDataList("wholeCarArr", wholeCarArr);
                            dataSave.setDataList("realVideoNameArr", realVideoNameArr);
                        }

                    } else {
                        Toast.makeText(getContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                    }
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
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
            }
        });

    }

    @Override
    protected void initEvents() {
        //注意：不要把最后一张图片也上传了
        oneKeyReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetUtils.isNetworkConnected(getActivity())) {//判断是否联网
                    ToastUtils.showNomalShortToast(getActivity(), getString(R.string.network_off));
                    return;
                }
                if (!Utils.isMatchered(AppCaseData.informantPhone)) {
                    ToastUtils.showNomalShortToast(getActivity(), "请在报案页面输入正确的11手机号码");
                    return;
                }

                //获得保存在本地的事故图片url数组
                reportPhotoUrlArr1 = dataSave.getDataList("reportPhotoArr1");
                reportPhotoUrlArr2 = dataSave.getDataList("reportPhotoArr2");
                reportPhotoUrlArr4 = dataSave.getDataList("reportPhotoArr4");
                reportPhotoUrlArr5 = dataSave.getDataList("reportPhotoArr5");
                wholeCarUrlArr = dataSave.getDataList("wholeCarArr");
                realVideoUrlArr = dataSave.getDataList("realVideoNameArr");

                onCarCount = (reportPhotoUrlArr1.size() - 1) + (reportPhotoUrlArr2.size() - 1)
                        + wholeCarUrlArr.size() + realVideoUrlArr.size();

                moreCarCount = (reportPhotoUrlArr1.size() - 1) + (reportPhotoUrlArr2.size() - 1)
                        + (reportPhotoUrlArr4.size() - 1) + (reportPhotoUrlArr5.size() - 1)
                        + wholeCarUrlArr.size() + realVideoUrlArr.size();

                showTipDialog("请再次确认您所有的报案信息，继续吗？");
                dialog.setOnBtnClickL(
                        new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();

                                if (AppCaseData.carCount == 2) {
                                    showProgress("拼命上传中", moreCarCount + 3);
                                } else {
                                    showProgress("拼命上传中", onCarCount + 3);
                                }
                                uploadField();
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
        //点击全车照片切换按钮时的响应方法
        wholeCarPhotoSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
                //判断是那个list
                whichImageList = 0;
                PreferenceUtil.commitInt("whichList", 0);
                isTakePhoto = true;
                //跳转到拍照页面
                Intent intent = new Intent(getActivity(), TakeReportPhoto.class);
                startActivity(intent);
            }
        });
        //点击全车照片时的响应方法
        wholeCarPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimeUtils.isFastClick()) {
                    return;
                }
                //判断是那个list
                whichImageList = 0;
                PreferenceUtil.commitInt("whichList", 0);
                if (wholeCarPhotoSwitch.getVisibility() == View.GONE) {
                    //判断是否要禁止编辑
                    if (!isEditReport) {
                        isTakePhoto = false;
                        //保存当前的wholeCarArr
                        dataSave.setDataList("LookImageList", wholeCarArr);
                        //跳转到照片查看界面
                        Intent intent = new Intent(getActivity(), LookImageList.class);

                        startActivity(intent);

                        if (PreferenceUtil.getBoolean("isEditReport", true)) {
                            PreferenceUtil.commitInt("numOfPages", 1);
                        } else {
                            PreferenceUtil.commitInt("numOfPages", 0);
                        }
                    } else {
                        isTakePhoto = true;
                        //跳转到拍照页面
                        Intent intent = new Intent(getActivity(), TakeReportPhoto.class);
                        startActivity(intent);
                    }
                } else {
                    isTakePhoto = false;
                    //保存当前的wholeCarArr
                    dataSave.setDataList("LookImageList", wholeCarArr);
                    //跳转到照片查看界面
                    Intent intent = new Intent(getActivity(), LookImageList.class);
                    startActivity(intent);

                    if (PreferenceUtil.getBoolean("isEditReport", true)) {
                        PreferenceUtil.commitInt("numOfPages", 1);
                    } else {
                        PreferenceUtil.commitInt("numOfPages", 0);
                    }

                }
            }
        });
    }

    /**
     * 上传字段
     */
    private void uploadField() {
        Map<String, String> map = new HashMap<>();

        //设置事故ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));

        //post参数
        map.put("casetype", Config.ICAMERA_A);
        // 案子ID
        map.put("caseid", PreferenceUtil.getString("caseid", null));
        // 报案时间
        map.put("casedate", AppCaseData.reportTime);
        // 事故时间
        map.put("accidentdate", AppCaseData.accidentTime);
        //报案人联系方式
        map.put("mobile", AppCaseData.informantPhone);
        //报案人姓名
        map.put("username", AppCaseData.informantName);
        // 投保公司
        map.put("inscode", AppCaseData.inscode);
        // 客户类型
        map.put("clientType", "0");
        /***   报案信息  ***/
        //出险地点
        map.put("accidentaddress", AppCaseData.geographicalPosition);
        //车牌号
        map.put("carnumber", AppCaseData.plateNumber);
        // 厂牌车型
        map.put("cartype", AppCaseData.label_models);
        // 三者车牌号
        map.put("threeCarnumber", AppCaseData.other_plate_number);
        // 三者厂牌车型
        map.put("threeCartype", AppCaseData.other_label_models);
        // 三者姓名
        map.put("threeUsername", AppCaseData.other_name);
        // 三者手机号
        map.put("threeMobile", AppCaseData.other_phone_number);

        /***   事故信息  ***/
        //事故类型
        if (AppCaseData.carCount == 1) {
            map.put("accidentkind", "单车");
        } else {
            map.put("accidentkind", "多车");
        }
        // 事故责任类型
        if (AppCaseData.accidentResponsibility == 1) {
            map.put("accident", "全部责任");
        } else if (AppCaseData.accidentResponsibility == 2) {
            map.put("accident", "没有责任");
        } else if (AppCaseData.accidentResponsibility == 3) {
            map.put("accident", "主要责任");
        } else if (AppCaseData.accidentResponsibility == 4) {
            map.put("accident", "同等责任");
        } else if (AppCaseData.accidentResponsibility == 5) {
            map.put("accident", "次要责任");
        }
        // 是否有物损
        if (AppCaseData.isPhysicalDamage == 1) {
            map.put("goodinjure", "是");
        } else {
            map.put("goodinjure", "否");
        }
        // 是否有人伤
        if (AppCaseData.isWounded == 1) {
            map.put("personinjure", "是");
        } else {
            map.put("personinjure", "否");
        }
        //是否事故现场
        if (AppCaseData.isScene == 1) {
            map.put("accidentscene", "是");
        } else {
            map.put("accidentscene", "否");
        }
        // 是否能正常行驶
        if (AppCaseData.isNormalDriving == 1) {
            map.put("carcanmove", "是");
        } else {
            map.put("carcanmove", "否");
        }
        //是否为模拟数据
        map.put("isdemo", PreferenceUtil.getString("isdemo", "0"));

        Call<ResponseBody> call = RetrofitManager.getInstance().create().stepOne(NetUtils.getHeaders(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {

                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        // 把备注内容上传
                        if (!"".equals(AppCaseData.new_remark) && AppCaseData.new_remark != null) { // 只有备注内容不为空才添加备注
                            addCaseMsg(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"));
                        }
                        if (AppCaseData.carCount == 2) {
                            getQiniuTokenForMoreCar();
                        } else {
                            getQiniuTokenForOneCar();
                        }

                    } else {
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //设置报案按钮能点击
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 上传步骤2
     */
    private void uploadTap2() {

        Case caseDetail = new Case();
        caseDetail.setCaseid(PreferenceUtil.getString("caseid", null));
        caseDetail.setCasepictures(VLCApplication.casepictures);

        Call<ResponseBody> call = RetrofitManager.getInstance().create().stepTwo(NetUtils.getHeaders(), caseDetail);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (jsonObject.getBoolean(Config.SUCCESS)) {
                        currentProgress += 1;
                        kProgressHUD.setProgress(currentProgress);

                        // 一健报案结束后，调用发送短信接口
                        sendSMS(jsonObject.getJSONObject("datas").getJSONObject("case").getString("caseid"), 2);

                        if (realVideoUrlArr.size() == 1) {
                            PreferenceUtil.commitBoolean("isHadVideo", true);
                        } else {
                            PreferenceUtil.commitBoolean("isHadVideo", false);
                        }
                        AppCaseData.paymentStatus = jsonObject.getJSONObject("datas").getJSONObject("case").getString("paylossstatus");

                        Intent intent = new Intent("OnlineSurvey2");
                        localBroadcastManager.sendBroadcast(intent);

                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                        handler.sendMessage(message);
                    } else {
                        //设置报案按钮能点击
                        Message message = new Message();
                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                        handler.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //设置报案按钮能点击
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 单车的一键上传
     */
    private void getQiniuTokenForOneCar() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), onCarCount);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");
                        arrReport1.clear();
                        arrReport2.clear();
                        if (jsonArray.length() <= 0) {
                            upLoadReportArr1ForOneCar(arrReport1);
                            currentProgress += 1;
                            kProgressHUD.setProgress(currentProgress);
                        } else {
                            for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                                arrReport1.add(jsonArray.get(i).toString());
                            }
                            for (int j = reportPhotoUrlArr1.size() - 1; j < reportPhotoUrlArr2.size() - 1 + reportPhotoUrlArr1.size() - 1; j++) {
                                arrReport2.add(jsonArray.get(j).toString());
                            }
                            if (realVideoUrlArr.size() == 0) {
                                strWholeCar = jsonArray.get(jsonArray.length() - 1).toString();
                                strVideo = "";
                            } else {
                                strWholeCar = jsonArray.get(jsonArray.length() - 2).toString();
                                strVideo = jsonArray.get(jsonArray.length() - 1).toString();
                            }
                            currentProgress += 1;
                            kProgressHUD.setProgress(currentProgress);
                            upLoadReportArr1ForOneCar(arrReport1);
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
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 上传单车定损图1
     *
     * @param list
     */
    private void upLoadReportArr1ForOneCar(final ArrayList<String> list) {
        VLCApplication.casepictures.clear();
        VLCApplication.one_b.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
            uploadReportArr2ForOneCar(arrReport2);
        } else {
            for (int i = 0; i < list.size(); i++) {

                File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> one_bMap = new HashMap<>();
                                            one_bMap.put("attachid", attachid);
                                            VLCApplication.one_b.add(one_bMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.one_b.size() == list.size()) {//one_b上传完毕
                                                VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                                                uploadReportArr2ForOneCar(arrReport2);
                                            }
                                        } catch (JSONException e) {
                                            Message message = new Message();
                                            message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        Message msg = new Message();
                                        msg.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(msg);
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> one_bMap = new HashMap<>();
                    one_bMap.put("attachid", reportPhotoUrlArr1.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.one_b.add(one_bMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.one_b.size() == list.size()) {//one_b上传完毕
                        VLCApplication.casepictures.put("one_b", VLCApplication.one_b);
                        uploadReportArr2ForOneCar(arrReport2);
                    }
                }
            }
        }
    }

    /**
     * 上传单车定损图2
     *
     * @param list
     */
    private void uploadReportArr2ForOneCar(final ArrayList<String> list) {
        VLCApplication.one_c.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
            uploadWholeForOneCar(strWholeCar);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr2.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> one_cMap = new HashMap<>();
                                            one_cMap.put("attachid", attachid);
                                            VLCApplication.one_c.add(one_cMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.one_c.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                                                uploadWholeForOneCar(strWholeCar);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Message message = new Message();
                                            message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> one_cMap = new HashMap<>();
                    one_cMap.put("attachid", reportPhotoUrlArr2.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.one_c.add(one_cMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.one_c.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("one_c", VLCApplication.one_c);
                        uploadWholeForOneCar(strWholeCar);
                    }
                }
            }
        }
    }

    /**
     * 上传全车照
     *
     * @param fileName
     */
    private void uploadWholeForOneCar(String fileName) {
        VLCApplication.one_a.clear();
        if (null == fileName || "".equals(fileName)) {
            VLCApplication.casepictures.put("one_a", VLCApplication.one_a);
            uploadVieo(strVideo);
        } else {
            File takeWholeCarPhoto0File = new File(wholeCarArr.get(0).substring(7));
            if (takeWholeCarPhoto0File.exists() && wholeCarUrlArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.casepictures.put("one_a", VLCApplication.one_a);
                uploadVieo(strVideo);
            } else if (takeWholeCarPhoto0File.exists() && !wholeCarUrlArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.uploadManager.put(takeWholeCarPhoto0File, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;

                                        Map<String, String> one_aMap = new HashMap<>();
                                        one_aMap.put("attachid", attachid);

                                        VLCApplication.one_a.add(one_aMap);
                                        VLCApplication.casepictures.put("one_a", VLCApplication.one_a);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadVieo(strVideo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> one_aMap = new HashMap<>();
                one_aMap.put("attachid", wholeCarUrlArr.get(0).replace(Config.QINIU_BASE_URL, ""));
                VLCApplication.one_a.add(one_aMap);
                VLCApplication.casepictures.put("one_a", VLCApplication.one_a);

                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);

                uploadVieo(strVideo);
            }
        }
    }

    /**
     * 一键上传
     */
    private void getQiniuTokenForMoreCar() {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().getQiniuToken(NetUtils.getHeaders(), moreCarCount);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());

                    if (200 == jsonObject.getInt("code")) {
                        baseUrl = jsonObject.getJSONObject("data").getString("baseUrl");
                        qiniuToken = jsonObject.getJSONObject("data").getString("token");
                        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("fileName");
                        arrReport1.clear();
                        arrReport2.clear();
                        arrReport4.clear();
                        arrReport5.clear();
                        if (jsonArray.length() <= 0) {
                            upLoadReportArr1(arrReport1);

                            currentProgress += 1;
                            kProgressHUD.setProgress(currentProgress);
                        } else {
                            for (int i = 0; i < reportPhotoUrlArr1.size() - 1; i++) {
                                arrReport1.add(jsonArray.get(i).toString());
                            }
                            for (int j = reportPhotoUrlArr1.size() - 1; j < reportPhotoUrlArr2.size() - 1 + reportPhotoUrlArr1.size() - 1; j++) {
                                arrReport2.add(jsonArray.get(j).toString());
                            }
                            for (int k = reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() - 2; k < reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() - 3; k++) {
                                arrReport4.add(jsonArray.get(k).toString());
                            }

                            for (int l = reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() - 3;
                                 l < reportPhotoUrlArr2.size() + reportPhotoUrlArr1.size() + reportPhotoUrlArr4.size() + reportPhotoUrlArr5.size() - 4; l++) {
                                arrReport5.add(jsonArray.get(l).toString());
                            }
                            if (realVideoUrlArr.size() == 0) {
                                strWholeCar = jsonArray.get(jsonArray.length() - 1).toString();
                                strVideo = "";
                            } else {
                                strWholeCar = jsonArray.get(jsonArray.length() - 2).toString();
                                strVideo = jsonArray.get(jsonArray.length() - 1).toString();
                            }
                            upLoadReportArr1(arrReport1);

                            currentProgress += 1;
                            kProgressHUD.setProgress(currentProgress);
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
                Message message = new Message();
                message.what = HANDLER_MESSAGE_UPLOAD_NET_FAIL;
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 上传多车定损图片 Arr1
     */
    private void upLoadReportArr1(final ArrayList<String> list) {
        VLCApplication.casepictures.clear();
        VLCApplication.more_a.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
            uploadReportArr2(arrReport2);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr1.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {

                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_aMap = new HashMap<>();
                                            more_aMap.put("attachid", attachid);
                                            VLCApplication.more_a.add(more_aMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_a.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                                                uploadReportArr2(arrReport2);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Message message = new Message();
                                            message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> more_aMap = new HashMap<>();
                    more_aMap.put("attachid", reportPhotoUrlArr1.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_a.add(more_aMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_a.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("more_a", VLCApplication.more_a);
                        uploadReportArr2(arrReport2);
                    }
                }
            }
        }
    }

    /**
     * 上传多车定损图片 Arr2
     */
    private void uploadReportArr2(final ArrayList<String> list) {
        VLCApplication.more_c.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
            uploadReportArr4(arrReport4);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr2.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_cMap = new HashMap<>();
                                            more_cMap.put("attachid", attachid);
                                            VLCApplication.more_c.add(more_cMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_c.size() == list.size()) {//more_a上传完毕
                                                VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                                                uploadReportArr4(arrReport4);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Message message = new Message();
                                            message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);

                } else {
                    Map<String, String> more_cMap = new HashMap<>();
                    more_cMap.put("attachid", reportPhotoUrlArr2.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_c.add(more_cMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_c.size() == list.size()) {//more_a上传完毕
                        VLCApplication.casepictures.put("more_c", VLCApplication.more_c);
                        uploadReportArr4(arrReport4);
                    }
                }
            }
        }
    }

    /**
     * 上传多车定损图片 Arr4
     */
    private void uploadReportArr4(final ArrayList<String> list) {
        VLCApplication.more_d.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
            uploadReportArr5(arrReport5);
        } else {

            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr4.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_dMap = new HashMap<>();
                                            more_dMap.put("attachid", attachid);
                                            VLCApplication.more_d.add(more_dMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_d.size() == list.size()) {//more_d上传完毕
                                                VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                                                uploadReportArr5(arrReport5);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Message message = new Message();
                                            message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> more_dMap = new HashMap<>();
                    more_dMap.put("attachid", reportPhotoUrlArr4.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_d.add(more_dMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_d.size() == list.size()) {//more_d上传完毕
                        VLCApplication.casepictures.put("more_d", VLCApplication.more_d);
                        uploadReportArr5(arrReport5);
                    }
                }
            }
        }

    }

    /**
     * 上传多车定损图片 Arr5
     */
    private void uploadReportArr5(final ArrayList<String> list) {
        VLCApplication.more_e.clear();
        if (null == list || 0 == list.size()) {
            VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
            uploadWholeCar(strWholeCar);
        } else {
            for (int i = 0; i < list.size(); i++) {
                File reportPhotoArr1File = new File(reportPhotoUrlArr5.get(i).substring(7));
                if (reportPhotoArr1File.exists()) {
                    VLCApplication.uploadManager.put(reportPhotoArr1File, list.get(i), qiniuToken,
                            new UpCompletionHandler() {
                                @Override
                                public void complete(String key, ResponseInfo info, JSONObject res) {
                                    //res包含hash、key等信息，具体字段取决于上传策略的设置
                                    if (info.isOK()) {
                                        Log.i("qiniu", "Upload Success");
                                        try {
                                            String attachid = res.getString("key");
                                            // 获取图片的url
                                            String url = Config.QINIU_BASE_URL + attachid;
                                            Map<String, String> more_eMap = new HashMap<>();
                                            more_eMap.put("attachid", attachid);
                                            VLCApplication.more_e.add(more_eMap);

                                            currentProgress += 1;
                                            kProgressHUD.setProgress(currentProgress);

                                            if (VLCApplication.more_e.size() == list.size()) {//more_e上传完毕
                                                VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                                                uploadWholeCar(strWholeCar);

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Message message = new Message();
                                            message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                            handler.sendMessage(message);
                                        }
                                    } else {
                                        Log.i("qiniu", "Upload Fail");
                                        //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                    Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                                }
                            }, null);
                } else {
                    Map<String, String> more_eMap = new HashMap<>();
                    more_eMap.put("attachid", reportPhotoUrlArr5.get(i).replace(Config.QINIU_BASE_URL, ""));
                    VLCApplication.more_e.add(more_eMap);

                    currentProgress += 1;
                    kProgressHUD.setProgress(currentProgress);

                    if (VLCApplication.more_e.size() == list.size()) {//more_e上传完毕
                        VLCApplication.casepictures.put("more_e", VLCApplication.more_e);
                        uploadWholeCar(strWholeCar);
                    }
                }
            }
        }

    }

    /**
     * 上传全车照
     *
     * @param fileName
     */
    private void uploadWholeCar(String fileName) {
        VLCApplication.more_b.clear();
        if (null == fileName || "".equals(fileName)) {
            VLCApplication.casepictures.put("more_b", VLCApplication.more_b);
            uploadVieo(strVideo);
        } else {
            File takeWholeCarPhoto0File = new File(wholeCarArr.get(0).substring(7));
            if (takeWholeCarPhoto0File.exists() && wholeCarArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.casepictures.put("more_b", VLCApplication.more_b);
                uploadVieo(strVideo);
            } else if (takeWholeCarPhoto0File.exists() && !wholeCarArr.get(0).substring(7).contains("report_image")) {
                VLCApplication.uploadManager.put(takeWholeCarPhoto0File, fileName, qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;

                                        Map<String, String> license_bMap = new HashMap<>();
                                        license_bMap.put("attachid", attachid);

                                        VLCApplication.more_b.add(license_bMap);
                                        VLCApplication.casepictures.put("more_b", VLCApplication.more_b);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadVieo(strVideo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> more_bMap = new HashMap<>();
                more_bMap.put("attachid", wholeCarUrlArr.get(0).replace(Config.QINIU_BASE_URL, ""));
                VLCApplication.more_b.add(more_bMap);
                VLCApplication.casepictures.put("more_b", VLCApplication.more_b);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                uploadVieo(strVideo);
            }
        }

    }

    /**
     * 上传小视频
     *
     * @param fileName
     */
    private void uploadVieo(String fileName) {
        VLCApplication.video.clear();
        if (realVideoUrlArr == null || realVideoUrlArr.size() == 0) {
            VLCApplication.casepictures.put("video", VLCApplication.video);
            currentProgress += 1;
            kProgressHUD.setProgress(currentProgress);
            uploadTap2();
        } else {
            File realVideoNameArrFile = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + realVideoUrlArr.get(0));
            if (realVideoNameArrFile.exists()) {
                VLCApplication.uploadManager.put(realVideoNameArrFile, fileName + ".mp4", qiniuToken,
                        new UpCompletionHandler() {
                            @Override
                            public void complete(String key, ResponseInfo info, JSONObject res) {
                                //res包含hash、key等信息，具体字段取决于上传策略的设置
                                if (info.isOK()) {
                                    Log.i("qiniu", "Upload Success");
                                    try {
                                        String attachid = res.getString("key");
                                        // 获取图片的url
                                        String url = Config.QINIU_BASE_URL + attachid;

                                        Map<String, String> videoMap = new HashMap<>();
                                        videoMap.put("attachid", attachid);

                                        VLCApplication.video.add(videoMap);
                                        VLCApplication.casepictures.put("video", VLCApplication.video);

                                        currentProgress += 1;
                                        kProgressHUD.setProgress(currentProgress);

                                        uploadTap2();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Message message = new Message();
                                        message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                        handler.sendMessage(message);
                                    }
                                } else {
                                    Log.i("qiniu", "Upload Fail");
                                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                                    Message message = new Message();
                                    message.what = HANDLER_MESSAGE_UPLOAD_FAIL;
                                    handler.sendMessage(message);
                                }
                                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                            }
                        }, null);
            } else {
                Map<String, String> videoMap = new HashMap<>();
                videoMap.put("attachid", realVideoUrlArr.get(0).replace(Config.QINIU_BASE_URL, ""));

                VLCApplication.video.add(videoMap);
                VLCApplication.casepictures.put("video", VLCApplication.video);
                currentProgress += 1;
                kProgressHUD.setProgress(currentProgress);
                uploadTap2();
            }
        }
    }

    @Override
    protected void loadData() {

    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label, int maxProgress) {

        kProgressHUD = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.ANNULAR_DETERMINATE)
                .setLabel(label)
                .setMaxProgress(maxProgress)
                .show();
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {

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
    }

    /**
     * 单车多车切换时的回调
     */
    @Override
    public void oneMoreSwitch() {
        //多车和单车是两种不同的界面显示方式
        if (AppCaseData.carCount == 2) {
            //显示多车图片view
            manyCarsView1.setVisibility(View.VISIBLE);
            manyCarsView2.setVisibility(View.VISIBLE);
            manyCarsView4.setVisibility(View.VISIBLE);
            manyCarsView5.setVisibility(View.VISIBLE);

            recyclerView4.setVisibility(View.VISIBLE);
            recyclerView5.setVisibility(View.VISIBLE);
            //照片1
            photo1Title.setText("撞击部位45度照片：");
            //照片2
            photo2Title.setText("自己车带牌照照片：");
            //照片3
            photo3Title.setText("对方车带牌照照片：");
            //照片4
            photo4Title.setText("自己车受损部位照片：");
            //照片5
            photo5Title.setText("对方车受损部位照片：");
        } else {
            //隐藏多车图片view
            manyCarsView1.setVisibility(View.GONE);
            manyCarsView2.setVisibility(View.GONE);
            manyCarsView4.setVisibility(View.GONE);
            manyCarsView5.setVisibility(View.GONE);
            recyclerView4.setVisibility(View.GONE);
            recyclerView5.setVisibility(View.GONE);
            //照片1
            photo1Title.setText("被撞物体照片：");
            //照片2
            photo2Title.setText("全车45度带车牌照片：");
            //照片3
            photo3Title.setText("受损部位照片：");
        }

        reportPhotoArr1.clear();
        reportPhotoArr2.clear();
        reportPhotoArr4.clear();
        reportPhotoArr5.clear();
        wholeCarArr.clear();
        reportPhotoArr3.clear();
        realVideoNameArr.clear();
        //给图片数组赋初始值
        reportPhotoArr1.add(reportBitmap);
        reportPhotoArr2.add(reportBitmap);
        reportPhotoArr4.add(reportBitmap);
        reportPhotoArr5.add(reportBitmap);
        wholeCarPhoto.setImageResource(R.mipmap.report_image);
        wholeCarPhotoSwitch.setVisibility(View.GONE);
        reportPhotoArr3.add(reportBitmap);
        isShowVideo = true;
        //刷新数据
        recyclerViewAdapter1.notifyDataSetChanged();
        recyclerViewAdapter2.notifyDataSetChanged();
        recyclerViewAdapter4.notifyDataSetChanged();
        recyclerViewAdapter5.notifyDataSetChanged();
        adapter3.notifyDataSetChanged();

        dataSave.setDataList("reportPhotoArr1", reportPhotoArr1);
        dataSave.setDataList("reportPhotoArr2", reportPhotoArr2);
        dataSave.setDataList("reportPhotoArr4", reportPhotoArr4);
        dataSave.setDataList("reportPhotoArr5", reportPhotoArr5);
        dataSave.setDataList("wholeCarArr", wholeCarArr);
        dataSave.setDataList("realVideoNameArr", realVideoNameArr);
    }

    class LocalReceiver extends BroadcastReceiver {
        //通知处理函数
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isTakePhoto) {
                //获得用户选择了多少张图片
                final int selectCount = PreferenceUtil.getInt("selectCount", -1);
                switch (whichImageList) {
                    case 1:
                        if (isSwitchListImage == -1) {
                            //删除最后一张图片
                            reportPhotoArr1.remove(reportPhotoArr1.size() - 1);
                            //从缓存中获得图片地址
                            final List<String> takeHitPhoto = dataSave.getDataList("takeHitPhoto");
                            //获取选择的原图
                            for (int i = 0; i < takeHitPhoto.size(); i++) {
                                reportPhotoArr1.add(takeHitPhoto.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeHitPhoto.size() - 1) {
                                    reportPhotoArr1.add(reportBitmap);
                                    //更新数据
                                    recyclerViewAdapter1.notifyDataSetChanged();
                                }
                            }
                        } else {
                            //分多车和单车两种情况
                            //删除对应已存在的attachid
                            if (PreferenceUtil.getString("accidentkind", "").equals("多车")) {
                                if (reportPhotoArr1.get(isSwitchListImage).contains("http")) {
                                    VLCApplication.more_a.remove(isSwitchListImage);
                                }
                            } else {
                                if (reportPhotoArr1.get(isSwitchListImage).contains("http")) {
                                    VLCApplication.one_b.remove(isSwitchListImage);
                                }
                            }
                            //从缓存中获得图片地址
                            final List<String> takeHitPhoto = dataSave.getDataList("takeHitPhoto");
                            //获取选择的原图
                            for (int i = 0; i < takeHitPhoto.size(); i++) {
                                reportPhotoArr1.set(isSwitchListImage, takeHitPhoto.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeHitPhoto.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter1.notifyDataSetChanged();
                                }
                            }
                        }
                        layoutManager1.smoothScrollToPosition(recyclerView1, new RecyclerView.State(), reportPhotoArr1.size());
                        //保存数组到本地，当状态为事故照片有问题，需要您重新提交，上传图片时有用
                        dataSave.setDataList("reportPhotoArr1", reportPhotoArr1);

                        break;
                    case 2:

                        if (isSwitchListImage == -1) {
                            //删除最后一张图片
                            reportPhotoArr2.remove(reportPhotoArr2.size() - 1);
                            //从缓存中获得图片地址
                            List<String> takeDamagedPhoto = dataSave.getDataList("takeDamagedPhoto");
                            //获取选择的原图
                            for (int i = 0; i < takeDamagedPhoto.size(); i++) {
                                reportPhotoArr2.add(takeDamagedPhoto.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeDamagedPhoto.size() - 1) {
                                    reportPhotoArr2.add(reportBitmap);
                                    //更新数据
                                    recyclerViewAdapter2.notifyDataSetChanged();
                                }
                            }
                        } else {
                            //分多车和单车两种情况
                            //删除对应已存在的attachid
                            if (PreferenceUtil.getString("accidentkind", "").equals("多车")) {
                                if (reportPhotoArr2.get(isSwitchListImage).contains("http")) {
                                    VLCApplication.more_c.remove(isSwitchListImage);
                                }
                            } else {
                                if (reportPhotoArr2.get(isSwitchListImage).contains("http")) {
                                    VLCApplication.one_c.remove(isSwitchListImage);
                                }
                            }
                            //从缓存中获得图片地址
                            final List<String> takeDamagedPhoto = dataSave.getDataList("takeDamagedPhoto");
                            //获取选择的原图
                            for (int i = 0; i < takeDamagedPhoto.size(); i++) {
                                reportPhotoArr2.set(isSwitchListImage, takeDamagedPhoto.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeDamagedPhoto.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter2.notifyDataSetChanged();
                                }
                            }
                        }
                        layoutManager2.smoothScrollToPosition(recyclerView2, new RecyclerView.State(), reportPhotoArr2.size());
                        //保存数组到本地，当状态为事故照片有问题，需要您重新提交，上传图片时有用
                        dataSave.setDataList("reportPhotoArr2", reportPhotoArr2);

                        break;
                    case 4:

                        if (isSwitchListImage == -1) {
                            //删除最后一张图片
                            reportPhotoArr4.remove(reportPhotoArr4.size() - 1);
                            //从缓存中获得图片地址
                            List<String> takeDamaged4Photo = dataSave.getDataList("takeDamaged4Photo");
                            //获取选择的原图
                            for (int i = 0; i < takeDamaged4Photo.size(); i++) {
                                reportPhotoArr4.add(takeDamaged4Photo.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeDamaged4Photo.size() - 1) {
                                    reportPhotoArr4.add(reportBitmap);
                                    //更新数据
                                    recyclerViewAdapter4.notifyDataSetChanged();
                                }
                            }
                        } else {
                            if (reportPhotoArr4.get(isSwitchListImage).contains("http")) {
                                //删除对应已存在的attachid
                                VLCApplication.more_d.remove(isSwitchListImage);
                            }
                            //从缓存中获得图片地址
                            final List<String> takeDamaged4Photo = dataSave.getDataList("takeDamaged4Photo");
                            //获取选择的原图
                            for (int i = 0; i < takeDamaged4Photo.size(); i++) {
                                reportPhotoArr4.set(isSwitchListImage, takeDamaged4Photo.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeDamaged4Photo.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter4.notifyDataSetChanged();
                                }
                            }
                        }
                        layoutManager4.smoothScrollToPosition(recyclerView4, new RecyclerView.State(), reportPhotoArr4.size());
                        //保存数组到本地，当状态为事故照片有问题，需要您重新提交，上传图片时有用
                        dataSave.setDataList("reportPhotoArr4", reportPhotoArr4);

                        break;
                    case 5:

                        if (isSwitchListImage == -1) {
                            //删除最后一张图片
                            reportPhotoArr5.remove(reportPhotoArr5.size() - 1);
                            //从缓存中获得图片地址
                            List<String> takeDamaged5Photo = dataSave.getDataList("takeDamaged5Photo");
                            //获取选择的原图
                            for (int i = 0; i < takeDamaged5Photo.size(); i++) {
                                reportPhotoArr5.add(takeDamaged5Photo.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeDamaged5Photo.size() - 1) {
                                    reportPhotoArr5.add(reportBitmap);
                                    //更新数据
                                    recyclerViewAdapter5.notifyDataSetChanged();
                                }
                            }
                        } else {
                            if (reportPhotoArr5.get(isSwitchListImage).contains("http")) {
                                //删除对应已存在的attachid
                                VLCApplication.more_e.remove(isSwitchListImage);
                            }
                            //从缓存中获得图片地址
                            final List<String> takeDamaged5Photo = dataSave.getDataList("takeDamaged5Photo");
                            //获取选择的原图
                            for (int i = 0; i < takeDamaged5Photo.size(); i++) {
                                reportPhotoArr5.set(isSwitchListImage, takeDamaged5Photo.get(i));
                                //如果是最后一个就刷新列表
                                if (i == takeDamaged5Photo.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter5.notifyDataSetChanged();
                                }
                            }
                        }
                        layoutManager5.smoothScrollToPosition(recyclerView5, new RecyclerView.State(), reportPhotoArr5.size());
                        //保存数组到本地，当状态为事故照片有问题，需要您重新提交，上传图片时有用
                        dataSave.setDataList("reportPhotoArr5", reportPhotoArr5);

                        break;
                    case 0:
                        //分多车和单车两种情况
                        //删除对应已存在的attachid
                        if (PreferenceUtil.getString("accidentkind", "").equals("多车")) {
                            if (wholeCarArr.size() > 0) {
                                if (wholeCarArr.get(0).contains("http")) {
                                    VLCApplication.more_b.clear();
                                }
                            }
                        } else {
                            if (wholeCarArr.size() > 0) {
                                if (wholeCarArr.get(0).contains("http")) {
                                    VLCApplication.one_a.clear();
                                }
                            }
                        }
                        //从缓存中获得图片
                        ImageLoader.getInstance().displayImage(PreferenceUtil.getString("takeWholeCarPhoto0", ""), wholeCarPhoto);
                        //显示图片切换按钮
                        wholeCarPhotoSwitch.setVisibility(View.VISIBLE);
                        //清空一下数组
                        wholeCarArr.clear();
                        wholeCarArr.add(PreferenceUtil.getString("takeWholeCarPhoto0", ""));
                        //保存数组到本地，当状态为事故照片有问题，需要您重新提交，上传图片时有用
                        dataSave.setDataList("wholeCarArr", wholeCarArr);
                        break;
                    default:
                        break;
                }
            } else {
                //获得页面传过来的值
                final int numOfPages = PreferenceUtil.getInt("numOfPages", -1);

                if (whichImageList == 3) {
                    //判断是否要禁止编辑
                    if (isEditReport) {
                        //保存录屏文件的名字
                        realVideoNameArr.clear();
                        realVideoNameArr.add(PreferenceUtil.getString("selectRecordingFile", ""));
                        //删除最后一张图片
                        reportPhotoArr3.clear();
                        //从缓存中获得图片地址
                        reportPhotoArr3.add("file://" + BitmapUtils.getSDPath() + "/VOC/Cache/" + PreferenceUtil.getString("selectRecordingFile", "") + ".png");

                        //保存数组到本地，当状态为事故照片有问题，需要您重新提交，上传图片时有用
                        dataSave.setDataList("realVideoNameArr", realVideoNameArr);
                        isShowVideo = false;
                        //更新数据
                        adapter3.notifyDataSetChanged();
                    }

                } else {
                    if (numOfPages != 0) {
                        if (whichImageList == 1) {
                            //先清空所有的数据
                            reportPhotoArr1.clear();
                            //给reportPhotoArr1赋值
                            //注意：不能直接让数组直接等于某一个数组，这样会出错，要获得某一个数组的数值就要一个一个添加它的子元素
                            remain_image = dataSave.getDataList("remain_image");
                            //获取选择的原图
                            for (int i = 0; i < remain_image.size(); i++) {
                                reportPhotoArr1.add(remain_image.get(i));
                                //如果是最后一个就刷新列表
                                if (i == remain_image.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter1.notifyDataSetChanged();
                                }
                            }
                        } else if (whichImageList == 2) {
                            //先清空所有的数据
                            reportPhotoArr2.clear();
                            //给reportPhotoArr2赋值
                            //注意：不能直接让数组直接等于某一个数组，这样会出错，要获得某一个数组的数值就要一个一个添加它的子元素
                            remain_image = dataSave.getDataList("remain_image");
                            //获取选择的原图
                            for (int i = 0; i < remain_image.size(); i++) {
                                reportPhotoArr2.add(remain_image.get(i));
                                //如果是最后一个就刷新列表
                                if (i == remain_image.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter2.notifyDataSetChanged();
                                }
                            }
                        } else if (whichImageList == 4) {
                            //先清空所有的数据
                            reportPhotoArr4.clear();
                            //给reportPhotoArr4赋值
                            //注意：不能直接让数组直接等于某一个数组，这样会出错，要获得某一个数组的数值就要一个一个添加它的子元素
                            remain_image = dataSave.getDataList("remain_image");
                            //获取选择的原图
                            for (int i = 0; i < remain_image.size(); i++) {
                                reportPhotoArr4.add(remain_image.get(i));
                                //如果是最后一个就刷新列表
                                if (i == remain_image.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter4.notifyDataSetChanged();
                                }
                            }
                        } else if (whichImageList == 5) {
                            //先清空所有的数据
                            reportPhotoArr5.clear();
                            //给reportPhotoArr5赋值
                            //注意：不能直接让数组直接等于某一个数组，这样会出错，要获得某一个数组的数值就要一个一个添加它的子元素
                            remain_image = dataSave.getDataList("remain_image");
                            //获取选择的原图
                            for (int i = 0; i < remain_image.size(); i++) {
                                reportPhotoArr5.add(remain_image.get(i));
                                //如果是最后一个就刷新列表
                                if (i == remain_image.size() - 1) {
                                    //更新数据
                                    recyclerViewAdapter5.notifyDataSetChanged();
                                }
                            }
                        }
                    } else {
                        if (whichImageList == 1) {
                            //先清空所有的数据
                            reportPhotoArr1.clear();
                            reportPhotoArr1.add(reportBitmap);
                            //更新数据
                            recyclerViewAdapter1.notifyDataSetChanged();
                        } else if (whichImageList == 2) {
                            //先清空所有的数据
                            reportPhotoArr2.clear();
                            reportPhotoArr2.add(reportBitmap);
                            //更新数据
                            recyclerViewAdapter2.notifyDataSetChanged();
                        } else if (whichImageList == 4) {
                            //先清空所有的数据
                            reportPhotoArr4.clear();
                            reportPhotoArr4.add(reportBitmap);
                            //更新数据
                            recyclerViewAdapter4.notifyDataSetChanged();
                        } else if (whichImageList == 5) {
                            //先清空所有的数据
                            reportPhotoArr5.clear();
                            reportPhotoArr5.add(reportBitmap);
                            //更新数据
                            recyclerViewAdapter5.notifyDataSetChanged();
                        } else {
                            //设置为默认图片
                            wholeCarPhoto.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.report_image));
                            //隐藏图片切换按钮
                            wholeCarPhotoSwitch.setVisibility(View.GONE);
                            dataSave.setDataList("wholeCarArr", new ArrayList<>());
                        }
                    }
                }
            }
        }
    }

    /**
     * 调用发送短信接口
     *
     * @param caseid
     */
    private void sendSMS(String caseid, int type) {

        Call<ResponseBody> call = RetrofitManager.getInstance().create().sendSMS(NetUtils.getHeaders(), caseid, type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 新增报案备注信息
     */
    private void addCaseMsg(String caseid) {

        Casemsg casemsg = new Casemsg();
        casemsg.setCaseid(caseid);
        casemsg.setUsercode(PreferenceUtil.getString("personcode", ""));
        casemsg.setContent(AppCaseData.new_remark);
        casemsg.setUserkind("2");

        Call<ResponseBody> call = RetrofitManager.getInstance().create().addCaseMsg(NetUtils.getHeaders(), casemsg);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
