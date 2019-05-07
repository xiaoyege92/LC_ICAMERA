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
import android.widget.TextView;

import com.Config;
import com.MessageEvent;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseFragment;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.LocalFileInfo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.load_video_thumbnail.MyVideoThumbLoader;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by yangboru on 2017/11/6.
 * <p>
 * 连接小B时的本地文件
 */

public class MainMeanLoadVideo extends BaseFragment {
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
    Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            videoListView.setAdapter(adapter);
        }
    };

    @Override
    protected int setViewId() {
        return R.layout.phone_video_layout;
    }

    @Override
    protected void findView(View view) {
        videoListView = (SwipeMenuListView) view.findViewById(R.id.phone_video_list);
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);

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

        adapter = new CommonAdapter(this.getActivity(), list, R.layout.phone_video_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {
                System.out.println("convert==");
                loadVideoName = list.get(position);

                helper.setText(R.id.phone_video_name, loadVideoName);

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
                if (loadVideoName.contains(".mp4") && !loadVideoName.equals("demo.mp4")) {
                    helper.setImageResource(R.id.load_video_tag, R.mipmap.recorder_video);
                } else {
                    helper.setImageResource(R.id.load_video_tag, R.mipmap.dvr_video);
                }

                //设置视频的时长
                TextView load_video_time_long = helper.getView(R.id.load_video_time_long);
                load_video_time_long.setText(getLoadVideoName.getRingDuring(loadVideoPath));
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
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        MainMeanLoadVideo.this.getContext());
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
    public void onResume() {
        super.onResume();
        sortTask = new SortTask();
        sortTask.execute();
    }

    /**
     * 弹出提示框
     *
     * @param lable
     */
    private void showTipDialog(String lable) {
        dialog = new NormalDialog(MainMeanLoadVideo.this.getContext());
        dialog.title(getString(R.string.tip));
        dialog.isTitleShow(true)//
                .cornerRadius(5)//
                .content(lable)//
                .contentGravity(Gravity.CENTER)//
                .btnTextSize(15.5f, 15.5f)//
                .widthScale(0.85f)//
                .btnText(mStringBts)
                .btnTextColor(new int[]{ContextCompat.getColor(getActivity(),R.color.primary),ContextCompat.getColor(getActivity(),R.color.alphablack)})
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void initEvents() {

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
                                            ToastUtils.showNomalShortToast(getActivity(), VLCApplication.configsDictionary.get("APP-A-030"));
                                        } else {
                                            ToastUtils.showNomalShortToast(getActivity(), "文件已成功删除！");
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
                }
                return false;
            }
        });

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int itemPosition, long id) {
                Intent intent = new Intent(MainMeanLoadVideo.this.getActivity(), PlayerActivity.class);
                intent.putExtra("phone", 1);
                intent.putExtra("video_play", list.get(itemPosition));
                if(list.get(itemPosition).contains(".mp4")) {
                    intent.putExtra("is_recorder",1);
                }else {
                    intent.putExtra("is_recorder",0);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void loadData() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if (messageEvent != null && Config.FILE_DOWNLOAD_FINISH_B1.equals(messageEvent.getMessage())) {
            sortTask = new SortTask();
            sortTask.execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (sortTask != null) {
            sortTask.cancel(true);
            sortTask = null;
        }
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
                        if ((fileList[i].contains(".TS") || fileList[i].contains(".ts") || fileList[i].contains(".mp4") || fileList[i].contains(".MP4") || fileList[i].contains(".mov") || fileList[i].contains(".MOV")) && !fileList[i].contains("456.mp4")) {
                            LocalFileInfo localFileInfo = new LocalFileInfo();
                            localFileInfo.setFileName(fileList[i]);
                            localFileInfo.setFileCreateTime(datedf.format(new Date((new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + fileList[i])).lastModified())));
                            tempList.add(localFileInfo);
                        }
                    }
                    for (int i = 0; i < fileList2.length; i++) {
                        if (fileList2[i].contains(".TS") || fileList2[i].contains(".ts") || fileList2[i].contains(".mov")  || fileList2[i].contains(".MOV")  || fileList2[i].contains(".mp4") || fileList2[i].contains(".MP4")) {
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
            myhandler.sendEmptyMessageDelayed(0, 300);
        }


    }

}
