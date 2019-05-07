package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.screen_recording;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.coremedia.iso.boxes.Container;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    private static String TAG = "CommonUtils";
    private static AudioRecorder audioRecorder;
    private static int mScreenWidth;
    private static int mScreenHeight;

    private static int mScreenDpi;

    public static void init(Activity activity){

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDpi = metrics.densityDpi;
    }

    public static int getScreenWidth(){
        return mScreenWidth;
    }

    public static int getScreenHeight(){
        return mScreenHeight;
    }

    public static int getScreenDpi(){
        return mScreenDpi;
    }
    public static void startCollectionFile() {
        if (Environment.isExternalStorageEmulated()) {
            String video = Environment.getExternalStorageDirectory() + "/VOC/ACCIDENT/456.mp4";
            String audio = Environment.getExternalStorageDirectory() + "/VOC/ACCIDENT/123.mp3";
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
            if (new File(audio).exists()) {
                if (new File(video).exists()) {
                    initFile(hecheng);
                    try {
                        Movie countVideo = MovieCreator.build(video);
                        Movie countAudio = MovieCreator.build(audio);
                        countVideo.addTrack(countAudio.getTracks().get(0));
                        Container container = new DefaultMp4Builder().build(countVideo);
                        FileOutputStream fos = new FileOutputStream(new File(hecheng));
                        container.writeContainer(fos.getChannel());
                        Log.d(TAG, "合并成功文件为:" + hecheng);
                        fos.close();

                        //合成成功后就删除合成之前的文件
                        File videoFile = new File(video);
                        if (videoFile.exists()) {
                            videoFile.delete();
                        }
                        File audioFile = new File(audio);
                        if (audioFile.exists()) {
                            audioFile.delete();
                        }
                        Log.e("CommonUtil", ".........成功合成......." + PreferenceUtil.getBoolean("isDeleteRecorderFile", false));
                        File recorderFile = new File(hecheng);
                        if (PreferenceUtil.getBoolean("isDeleteRecorderFile", false) && recorderFile.exists()) {
                            recorderFile.delete();
                            PreferenceUtil.commitBoolean("isDeleteRecorderFile", false);
                        }
                        //判断是否已合并完成
                        PreferenceUtil.commitInt("isFinishRecorder", 1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "视频文件:" + video + "不存在");
                }
            } else {
                Log.d(TAG, "音频文件:" + audio + "不存在");
            }
        } else {
            Log.d(TAG, "未挂载SD卡");
        }
    }

    /**
     * 获得当前的日期
     *
     * @return
     */
    private static String getPhoneDate() {
        SimpleDateFormat formatterdate = new SimpleDateFormat("yyyy_MMdd");
        Date curDate = new Date(System.currentTimeMillis());
        return formatterdate.format(curDate);
    }

    /**
     * 获得当前的时间
     *
     * @return
     */
    private static String getPhoneTime() {
        SimpleDateFormat formattertime = new SimpleDateFormat("HHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        return formattertime.format(curDate);
    }

    //AudioRecorder录音
    public static void startAudioRecorder() throws IOException {
        String audio = Environment.getExternalStorageDirectory() + "/VOC/ACCIDENT/123.mp3";
        initFile(audio);
        audioRecorder = AudioRecorder.getInstance(new File(audio));
        audioRecorder.setAudioEncoder(new AudioEncoder());
        audioRecorder.startAudioRecording();
    }

    public static void stopAudioRecoder() {
        if (audioRecorder != null) {
            audioRecorder.stopAudioRecording();
        }
    }

    //MediaRecorder录音
    private static boolean isRecording = true;
    private static MediaRecorder mRecorder;

    public static void startMediaRecoder() {
        isRecording = true;
        String audio = Environment.getExternalStorageState() + "/VOC/ACCIDENT/123.mp3";
        initFile(audio);
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setOutputFile(audio);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }

    public static void stopMediaRecoder() {
        if (isRecording) {
            mRecorder.stop();
            isRecording = false;
        }
    }

    public static void initFile(String video) {
        File videoFile = new File(video);
        if (videoFile.exists()) {
            videoFile.delete();
        }
        try {
            videoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
