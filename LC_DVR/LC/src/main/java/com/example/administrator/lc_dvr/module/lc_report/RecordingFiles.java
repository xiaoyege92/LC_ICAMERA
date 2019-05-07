package com.example.administrator.lc_dvr.module.lc_report;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.adapter.CommonAdapter;
import com.example.administrator.lc_dvr.common.adapter.ViewHolder;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.ListDataSave;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ToastUtils;
import com.example.administrator.lc_dvr.module.LocalVideoFileActivity;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.GetLoadVideoName;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.PlayerActivity;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.LocalFileInfo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.load_video_thumbnail.MyVideoThumbLoader;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by yangboru on 2018/2/1.
 *
 *  选择本地文件列表
 */

public class RecordingFiles extends BaseActivity {

    private ListView recordingFilesTableView;
    private ListDataSave dataSave;
    private List<String> alreadyChosenFile;
    private MyVideoThumbLoader mVideoThumbLoader;
    private GetLoadVideoName getLoadVideoName;
    private List<String> list;
    private CommonAdapter adapter;
    private String loadVideoName;
    private String loadVideoPath;
    private LocalBroadcastManager localBroadcastManager;
    private KProgressHUD kProgressHUD;
    private ArrayList<LocalFileInfo> tempList = new ArrayList();
    private SortTask sortTask;

    Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            recordingFilesTableView.setAdapter(adapter);
            if(list.size() == 0){
                ToastUtils.showNomalShortToast(RecordingFiles.this,"本地文件为空");
            }
        }
    };

    @Override
    protected int setViewId() {
        return R.layout.recordingfiles_layout;
    }

    @Override
    protected void findView() {
        recordingFilesTableView = (ListView) findViewById(R.id.recordingFilesTableView);
    }

    @Override
    protected void init() {

        //得到本地广播管理器的实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        //用来保存list到本地
        dataSave = new ListDataSave(this, "baiyu");
        //获得页面传过来的已选择录屏文件数组
        alreadyChosenFile = dataSave.getDataList("alreadyChosenFile");

        mVideoThumbLoader = new MyVideoThumbLoader();// 初始化缩略图载入方法

        getLoadVideoName = new GetLoadVideoName();// 初始化本地视频名称载入方法

        list = new ArrayList<>();

        File videoDir = new File(BitmapUtils.getSDPath() + "/VOC/");
        if (!videoDir.exists()) {
            videoDir.mkdirs();//创建目录
        }

        File accidentDir = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");
        if (!accidentDir.exists()) {
            accidentDir.mkdirs();//创建目录
        }

        adapter = new CommonAdapter(this, list, R.layout.recordingfiles_item) {
            @Override
            public void convert(ViewHolder helper, final int position, Object item) {

                loadVideoName = list.get(position);

                helper.setText(R.id.recordingFileName, loadVideoName);

                loadVideoPath = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + loadVideoName;

                //获得文件的大小
                File file = new File(loadVideoPath);
                String size = "";
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

                //设置视频的时长
                TextView recordingFileDuration = helper.getView(R.id.recordingFileDuration);
                recordingFileDuration.setText("00:00");
                recordingFileDuration.setTag(loadVideoPath);
                getLoadVideoName.showThumbByAsynctack(loadVideoPath, recordingFileDuration);
                // 设置视频时长
                helper.setText(R.id.recordingFileDuration,"");
                //设置文件的大小
                helper.setText(R.id.recordingFileSize, size);
                //设置文件的时间
                SimpleDateFormat datedf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                helper.setText(R.id.recordingFileTime, datedf.format(new Date(file.lastModified())));
                //设置缩略图
                final ImageView phone_video_img = helper.getView(R.id.recordingFileImage);
                phone_video_img.setImageResource(R.drawable.nophotos);
                phone_video_img.setTag(loadVideoPath);
                mVideoThumbLoader.showThumbByAsynctack(loadVideoPath, phone_video_img, loadVideoName);
                //点击选择按钮时的响应
                Button selectBtn = helper.getView(R.id.selectBtn);
                selectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //保存当前选择的录屏文件和缩略图
                        PreferenceUtil.commitString("selectRecordingFile", list.get(position));
                        //发送本地广播
                        Intent intent = new Intent("ReportPhoto");
                        localBroadcastManager.sendBroadcast(intent);
                        //隐藏当前的视图
                        finish();
                    }
                });

                for (int i = 0; i < alreadyChosenFile.size(); i++) {
                    if (alreadyChosenFile.get(i).equals(list.get(position))) {
                        selectBtn.setText("已选择");
                        //设置为不能点击
                        selectBtn.setEnabled(false);
                    }
                }
            }
        };
        //设置适配器
        recordingFilesTableView.setAdapter(adapter);
        //获得本地视频数据
//        getFileName();
        showProgress("拼命加载中...");
        sortTask = new SortTask();
        sortTask.execute();
    }

    private void getFileName() {
        //先清空一下数据
        list.clear();

        File recorderFile = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");
        String[] recorderFileList = recorderFile.list();//得到该目录下所有的文件名
        for (int i = 0; i < recorderFileList.length; i++) {
            if (recorderFileList[i].contains("mp4")) {
                list.add(recorderFileList[i]);
            }
        }
        //下面的代码是实现时间的排序
        //注意这里有一个坑：要转化的字符串必须要与SimpleDateFormat的格式一模一样
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
        Date d1;
        Date d2;
        String temp_r;
        //做一个冒泡排序，大的在数组的前列
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                ParsePosition pos1 = new ParsePosition(0);
                ParsePosition pos2 = new ParsePosition(0);
                if (list.get(i).contains("MOV")||list.get(i).contains("MP4")) {
                    String mov1 = list.get(i).substring(0, list.get(i).length() - 8);
                    Log.e("ddd", mov1);
                    d1 = sdf.parse(mov1, pos1);
                } else {
                    String mov2 = list.get(i).substring(0, list.get(i).length() - 4);
                    Log.e("ddd", mov2);
                    d1 = sdf.parse(mov2, pos1);
                }

                if (list.get(j).contains("MOV")||list.get(j).contains("MP4")) {
                    String mov1 = list.get(j).substring(0, list.get(j).length() - 8);
                    Log.e("ddd", mov1);
                    d2 = sdf.parse(mov1, pos2);
                } else {
                    String mov2 = list.get(j).substring(0, list.get(j).length() - 4);
                    Log.e("ddd", mov2);
                    d2 = sdf.parse(mov2, pos2);
                }

                if (d1 != null && d2 != null) {
                    if (d1.before(d2)) {//如果队前日期靠前，调换顺序
                        temp_r = list.get(i);
                        list.set(i, list.get(j));
                        list.set(j, temp_r);
                    }
                }

            }
        }

        //刷新数据
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void initEvents() {
        //点击recordingFilesTableView的item时的响应
        recordingFilesTableView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PreferenceUtil.commitBoolean("isReport", true);
                //跳转到视频播放界面
                Intent intent = new Intent(RecordingFiles.this, PlayerActivity.class);
                if (list.get(position).contains("mp4")) {
                    intent.putExtra("is_recorder", 1);
                }
                intent.putExtra("phone", 1);
                intent.putExtra("video_play", list.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void loadData() {

    }

    /**
     * 关闭当前的页面
     * @param view
     */
    public void closeRecordingFiles(View view) {
        finish();
    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        kProgressHUD = KProgressHUD.create(RecordingFiles.this);
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
//            File file2 = new File(BitmapUtils.getSDPath() + "/VOC/");
            String[] fileList = file.list();//得到该目录下所有的文件名
//            String[] fileList2 = file2.list();//得到该目录下所有的文件名
            java.text.SimpleDateFormat datedf = new java.text.SimpleDateFormat("yyyy_MMdd_HHmmss");
            if (tempList != null) {
                try {
                    for (int i = 0; i < fileList.length; i++) {
                        if ((fileList[i].contains(".TS") || fileList[i].contains(".ts") || fileList[i].contains(".mov")|| fileList[i].contains(".MOV")|| fileList[i].contains(".mp4") || fileList[i].contains(".MP4")) && !fileList[i].equals("456.mp4")) {
                            LocalFileInfo localFileInfo = new LocalFileInfo();
                            localFileInfo.setFileName(fileList[i]);
                            localFileInfo.setFileCreateTime(datedf.format(new Date((new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/"+fileList[i])).lastModified())));
                            tempList.add(localFileInfo);
                        }
                    }
//                    for (int i = 0; i < fileList2.length; i++) {
//                        if (fileList2[i].contains(".TS") || fileList2[i].contains(".ts") || fileList2[i].contains(".mp4") || fileList2[i].contains(".MP4") || fileList2[i].contains(".mov")|| fileList2[i].contains(".MOV")) {
//                            LocalFileInfo localFileInfo = new LocalFileInfo();
//                            localFileInfo.setFileName(fileList2[i]);
//                            localFileInfo.setFileCreateTime(datedf.format(new Date((new File(BitmapUtils.getSDPath() + "/VOC/"+fileList2[i])).lastModified())));
//                            tempList.add(localFileInfo);
//                        }
//                    }
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
                                Collections.swap(tempList,i,j);
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

            if(kProgressHUD != null) {
                kProgressHUD.dismiss();
            }

            myhandler.sendEmptyMessageDelayed(0, 100);
        }


    }
}
