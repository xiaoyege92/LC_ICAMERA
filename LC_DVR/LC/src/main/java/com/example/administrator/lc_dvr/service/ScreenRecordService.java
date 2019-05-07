package com.example.administrator.lc_dvr.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;

import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.screen_recording.CommonUtils;

import java.io.File;
import java.io.IOException;


/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/09/16
 *   desc   :
 *  version :
 * </pre>
 */
public class ScreenRecordService extends Service implements Handler.Callback {

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    private boolean mIsRunning;
    private int mScreenDpi = CommonUtils.getScreenDpi();
    private int mRecordWidth = 1280;
    private int mRecordHeight = 720;
    private int mResultCode;
    private Intent mResultData;

    //录屏文件的保存地址
    private String mRecordFilePath;

    //    private Handler mHandler;
    //已经录制多少秒了
    private int mRecordSeconds = 0;

    private static final int MSG_TYPE_COUNT_DOWN = 110;

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mIsRunning = false;
        mMediaRecorder = new MediaRecorder();
//        mHandler = new Handler(Looper.getMainLooper(), this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean isReady() {
        return mMediaProjection != null && mResultData != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void clearRecordElement() {
        clearAll();
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mResultData = null;
        mIsRunning = false;
    }

    public boolean ismIsRunning() {
        return mIsRunning;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setResultData(int resultCode, Intent resultData) {
        mResultCode = resultCode;
        mResultData = resultData;

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null) {
            mMediaProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean startRecord() {
        if (mIsRunning) {
            return false;
        }
        if (mMediaProjection == null) {
            mMediaProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);

        }

        setUpMediaRecorder();
        createVirtualDisplay();
        mMediaRecorder.start();

        ScreenUtil.startRecord();

        mIsRunning = true;

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean stopRecord(String tip) {
//        Log.w("lala","stopRecord: first ");

        if (!mIsRunning) {
            return false;
        }
        mIsRunning = false;
        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder = null;
            mVirtualDisplay.release();
            mMediaProjection.stop();
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mMediaProjection = null;

//      mHandler.removeMessages(MSG_TYPE_COUNT_DOWN);
        ScreenUtil.stopRecord(tip);

//        if (mRecordSeconds <= 2) {

//            FileUtil.deleteSDFile(mRecordFilePath);
//        } else {
        //通知系统图库更新
        FileUtil.fileScanVideo(this, mRecordFilePath, mRecordWidth, mRecordHeight, mRecordSeconds);
//        }

        mRecordSeconds = 0;

        return true;
    }

    public void pauseRecord() {
        if (mMediaRecorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.pause();
            }
        }
    }

    public void resumeRecord() {
        if (mMediaRecorder != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.resume();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("MainScreen", mRecordWidth, mRecordHeight, mScreenDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaRecorder() {
        int curNameCount = 1;//记录包含这个文件创建日期的文件有多少个
        String Time = PreferenceUtil.getString("playFileTime", "");

        File file = new File(Environment.getExternalStorageDirectory() + "/VOC/ACCIDENT");
        File[] subFile = file.listFiles();

        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {//遍历文件夹获取包含文件创建日期的个数

            if (!subFile[iFileLength].isDirectory()) {  // 判断是否为文件夹
                String filename = subFile[iFileLength].getName();
                if (filename.contains(Time)) {
                    curNameCount++;
                }
            }
        }
        String hecheng;
        if (curNameCount < 10) {//如果一个视频截屏小视频小于10个
            hecheng = Environment.getExternalStorageDirectory() + "/VOC/ACCIDENT/" + Time + "_00" + curNameCount + ".mp4";
            PreferenceUtil.commitString("playVideoName", Time + "_00" + curNameCount + ".mp4");
        } else {
            hecheng = Environment.getExternalStorageDirectory() + "/VOC/ACCIDENT/" + Time + "_0" + curNameCount + ".mp4";
            PreferenceUtil.commitString("playVideoName", Time + "_0" + curNameCount + ".mp4");
        }

        //保存录屏文件的名称
        PreferenceUtil.commitString("recorder_name", hecheng);

        mRecordFilePath = hecheng;
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setOutputFile(mRecordFilePath);
        mMediaRecorder.setVideoSize(mRecordWidth, mRecordHeight);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncodingBitRate(1800000);
        mMediaRecorder.setVideoFrameRate(20);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void clearAll() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public String getRecordFilePath() {
        return mRecordFilePath;
    }

    public String getSaveDirectory() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case MSG_TYPE_COUNT_DOWN: {

                boolean enough = FileUtil.getSDFreeMemory() / (1024 * 1024) < 4;
                if (enough) {
                    //空间不足，停止录屏
                    stopRecord("空间不足，停止录制");
                    mRecordSeconds = 0;
                    break;
                }

                mRecordSeconds++;
                int minute = 0, second = 0;
                if (mRecordSeconds >= 60) {
                    minute = mRecordSeconds / 60;
                    second = mRecordSeconds % 60;
                } else {
                    second = mRecordSeconds;
                }
                ScreenUtil.onRecording("0" + minute + ":" + (second < 10 ? "0" + second : second + ""));

                if (mRecordSeconds < 3 * 60) {
//                    mHandler.sendEmptyMessageDelayed(MSG_TYPE_COUNT_DOWN, 1000);
                } else if (mRecordSeconds == 3 * 60) {
//                    str = getString(R.string.record_time_end_tip);
                    stopRecord("停止录制");
                    mRecordSeconds = 0;
                }

                break;
            }
        }
        return true;
    }

    public class RecordBinder extends Binder {
        public ScreenRecordService getRecordService() {
            return ScreenRecordService.this;
        }
    }


}
