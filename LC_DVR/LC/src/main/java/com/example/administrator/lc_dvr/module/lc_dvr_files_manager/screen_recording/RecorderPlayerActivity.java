package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.screen_recording;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Config;
import com.MessageEvent;
import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.VorangeType;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.service.FileUtil;
import com.example.administrator.lc_dvr.service.ScreenRecordService;
import com.example.administrator.lc_dvr.service.ScreenUtil;
import com.flyco.dialog.widget.NormalDialog;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.greenrobot.eventbus.EventBus;
import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.ScreenResolution;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.VideoView;

import static org.videolan.libvlc.VLCApplication.getDvrWifiName;

/**
 * Created by recker on 16/8/6.
 */
public class RecorderPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int UPDATE_PALY_TIME = 0x01;//更新播放时间
    public static final int UPDATE_TIME = 800;
    public static final int HIDE_CONTROL_BAR = 0x02;//隐藏控制条
    public static final int HIDE_TIME = 3000;//隐藏控制条时间
    public static final int SHOW_CENTER_CONTROL = 0x03;//显示中间控制
    public static final int SHOW_CONTROL_TIME = 1000;

    public final static int ADD_FLAG = 1;
    public final static int SUB_FLAG = -1;

    private RelativeLayout mVideoLayout;
    private VideoView mVideoView;
    private RelativeLayout mControlTop;//顶部控制栏
    private RelativeLayout mControlBottom;//底部控制栏
    private ImageView mIvBack;//返回
    private ImageView mIvPlay;//播放/暂停
    private TextView mTvTime;//时间显示
    private SeekBar mSeekBar;//进度条
    private ImageView mIvIsFullScreen;//是否全屏
    private RelativeLayout mProgressBar;//缓冲提示
    private LinearLayout mControlCenter;
    private ImageView mIvControl;
    private TextView mTvControl;
    private TextView mTvFast;//快进

    private int mScreenWidth = 0;//屏幕宽度
    private boolean mIsFullScreen = false;//是否为全屏
    private long mVideoTotalTime = 0;//视频总时间
    //    private boolean mIntoSeek = false;//是否 快进/快退
    private long mSeek = 0;//快进的进度
    private boolean mIsFastFinish = false;

    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener;

    private AudioManager mAudioManager;
    private int mMaxVolume;//最大声音
    private int mShowVolume;//声音
    private int mShowLightness;//亮度

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PALY_TIME:
                    long currentPosition = mVideoView.getCurrentPosition();
                    if (currentPosition <= mVideoTotalTime) {
                        //更新时间显示
                        mTvTime.setText(sec2time(currentPosition));
                        mTvTime2.setText(sec2time(mVideoTotalTime));
                        //更新进度条
                        int progress = (int) ((currentPosition * 1.0 / mVideoTotalTime) * 100);
                        mSeekBar.setProgress(progress);
                        mHandler.sendEmptyMessageDelayed(UPDATE_PALY_TIME, UPDATE_TIME);
                    }
                    break;
                case HIDE_CONTROL_BAR:
                    isShowControl = false;
                    hideControlBar();
                    break;
                case SHOW_CENTER_CONTROL:
                    mControlCenter.setVisibility(View.GONE);
                    break;
            }
        }
    };
    private int tag;
    private int phone;
    private int isLock;
    private String playOnline;

    private List<String> videoLists;
    private TextView videoTitle;
    private ImageView screen_recording;
    private ImageView screen_recording_open;
    private MediaProjectionManager mMediaProjectionManager;
    private ScreenRecorder mRecorder;
    private static final int REQUEST_CODE = 1;
    private TextView rec_minute;
    private TextView rec_second;
    private int minute;
    private int second;
    private Timer recTimer;
    private LinearLayout recording_time;
    private NormalDialog dialog;
    private String[] mStringBts;
    private TextView mTvTime2;
    private Timer recorderTimer;
    private KProgressHUD kProgressHUD;
    private LinearLayout recorder_layout;
    private Timer wifiTimer;
    private Timer backTimer;

    private ScaleAnimation scaleAni;
    //用来控制应用前后台切换的逻辑
    private boolean isCurrentRunningForeground = true;
    // 控制在录屏是进度条是否显示隐藏的逻辑
    private boolean isShowControl = true;
    // 旋转动画
//    private Animation operatingAnim;
    // 开启录像服务
    private ServiceConnection mServiceConnection;


    /**
     * 秒转化为常见格式
     *
     * @param time
     * @return
     */
    private String sec2time(long time) {
        String hms = StringUtils.generateTime(time);
        return hms;
    }

    /**
     * 自定义对话框
     *
     * @param value
     */
    private void showMyDialog(String value) {
        EventBus.getDefault().post(new MessageEvent(""));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.dialog_hint_stop_rec, null);

        final AlertDialog myDialog = builder.create();
        myDialog.setView(view, 0, 0, 0, 0);

        Button bt_ok = (Button) view.findViewById(R.id.btn_queding);
        Button btn_quxiao = (Button) view.findViewById(R.id.btn_quxiao);
        TextView tv_hint = (TextView) view.findViewById(R.id.tv_hint);
        tv_hint.setText(value);

        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否点击了查看录屏文件按钮
                PreferenceUtil.commitInt("is_recorder", 1);
                //关闭对话框
                myDialog.dismiss();
                // 刷新小C小B的视频列表
                EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_FINISH_C1));
                EventBus.getDefault().post(new MessageEvent(Config.FILE_DOWNLOAD_FINISH_B1));

                if (wifiTimer != null) {
                    wifiTimer.cancel();
                    wifiTimer = null;
                }

                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                }

                if (mRecorder != null) {
                    mRecorder.quit();
                    mRecorder = null;
                }

                //释放资源
                if (mVideoView != null) {
                    mVideoView.destroyDrawingCache();

                    mVideoView.stopPlayback();
                    mVideoView.setKeepScreenOn(false);
                }

                File accidentDir = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");
                if (!accidentDir.exists()) {
                    accidentDir.mkdirs();//创建目录
                }
                finish();


            }
        });
        btn_quxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //恢复播放
//                if (!mVideoView.isPlaying()) {
//                    mVideoView.start();
//                }
                timerHandler.sendEmptyMessage(4);
                //关闭对话框
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_fullscreen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mVideoView = (VideoView) findViewById(R.id.videoview);
        mControlTop = (RelativeLayout) findViewById(R.id.control_top);
        mControlBottom = (RelativeLayout) findViewById(R.id.control_bottom);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        screen_recording = (ImageView) findViewById(R.id.screen_recording);
        screen_recording_open = (ImageView) findViewById(R.id.screen_recording_open);
        mIvPlay = (ImageView) findViewById(R.id.iv_play);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvTime2 = (TextView) findViewById(R.id.tv_time2);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mIvIsFullScreen = (ImageView) findViewById(R.id.iv_is_fullscreen);
        mProgressBar = (RelativeLayout) findViewById(R.id.progressbar);
        mControlCenter = (LinearLayout) findViewById(R.id.control_center);
        mIvControl = (ImageView) findViewById(R.id.iv_control_img);
        mTvControl = (TextView) findViewById(R.id.tv_control);
        mTvFast = (TextView) findViewById(R.id.tv_fast);
        videoTitle = (TextView) findViewById(R.id.video_title);
        rec_minute = (TextView) findViewById(R.id.recording_minute);
        rec_second = (TextView) findViewById(R.id.recording_second);
        recording_time = (LinearLayout) findViewById(R.id.recording_time);
        recorder_layout = (LinearLayout) findViewById(R.id.recorder_layout);

        mStringBts = new String[2];
        mStringBts[0] = getString(R.string.re_recording);
        mStringBts[1] = getString(R.string.view_recording_files);

        //作用是当调节手机音量时，调的是媒体音量，而不是其他音量
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Intent intent = getIntent();
        //用来判断是多选操作还是单选操作
        tag = intent.getIntExtra("tag", 0);
        //用来判断是本地还是在线视频
        phone = intent.getIntExtra("phone", 0);
        //用来判断是否是加锁视频
        isLock = intent.getIntExtra("isLock", 0);
        if (tag == 1) {
            videoLists = (List<String>) intent.getSerializableExtra("video_list_play");
            if (phone == 1) {//phone为1时表示的是本地
                playOnline = BitmapUtils.getSDPath() + "/VOC/" + videoLists.get(0);
            } else {
                if (VLCApplication.getWifiSsid() == 1) {
                    playOnline = "http://192.168.1.254/CARDV/MOVIE/" + videoLists.get(0);
                } else if (VLCApplication.getWifiSsid() == 2) {
                    if (videoLists.get(0).contains("SOS")) {
                        playOnline = "http://192.168.1.254:8080/mnt/extsd/sos/" + videoLists.get(0);
                    } else {
                        playOnline = "http://192.168.1.254:8080/mnt/extsd/video/" + videoLists.get(0);
                    }
                }
            }
            videoTitle.setText(playOnline);
            mIvBack.setOnClickListener(this);
            mIvPlay.setOnClickListener(this);
            mIvIsFullScreen.setOnClickListener(this);
            init(playOnline);

        } else {
            String video_play = intent.getStringExtra("video_play");
            if (phone == 1) {//phone为1时表示的是本地
                playOnline = BitmapUtils.getSDPath() + "/VOC/" + video_play;
            } else {
                if (VLCApplication.getVorangeType() == VorangeType.B1) {
                    System.out.println("video_play:" + video_play);
                    playOnline = video_play;
                } else {
                    if (VLCApplication.getWifiSsid() == 1) {
                        if (isLock == 1) {
                            playOnline = "http://192.168.1.254/CARDV/RO/" + video_play;
                        } else {
                            playOnline = "http://192.168.1.254/CARDV/MOVIE/" + video_play;
                        }
                    } else if (VLCApplication.getWifiSsid() == 2) {
                        if (isLock == 1) {
                            playOnline = "http://192.168.1.254/CARDV/MOVIE/RO/" + video_play;
                        } else {
                            playOnline = "http://192.168.1.254/CARDV/MOVIE/" + video_play;
                        }
                    }
                }
            }
            videoTitle.setText(video_play);
            mIvBack.setOnClickListener(this);
            mIvPlay.setOnClickListener(this);
            mIvIsFullScreen.setOnClickListener(this);
            init(playOnline);
        }

        File destDir = new File(BitmapUtils.getSDPath() + "/VOC/ACCIDENT/");

        if (!destDir.exists()) {
            destDir.mkdirs();//创建目录
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection ResourceType
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        }

        dialog = new NormalDialog(RecorderPlayerActivity.this);

        //添加当前的activity
        VLCApplication.addActivity(this);

        CommonUtils.init(this);
        // 开启平路录制服务
        startScreenRecordService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                countRecTime(0, 20);
                ScreenUtil.setUpData(resultCode, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    private void init(String url) {

        Pair<Integer, Integer> screenPair = ScreenResolution.getResolution(this);
        mScreenWidth = screenPair.first;
        //播放网络资源
        if (url != null) {
            mVideoView.setVideoPath(url);
        }
        //不要设置缓冲，不然会发生错误
        //在这里设置缓冲大小，设置为10240，不然播放会卡顿
        mVideoView.setBufferSize(10240);

        initVolumeWithLight();
        addVideoViewListener();
        addSeekBarListener();
        addTouchListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //恢复播放
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
            mVideoView.setKeepScreenOn(true);//让屏幕不休眠
        }
        if (wifiTimer == null) {
            wifiTimer = new Timer();
            wifiTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //如果断开了dvr的wifi就跳转到失败的页面
                    if (getDvrWifiName(getApplicationContext()) == null) {

                        VLCApplication.removeALLActivity();
                        Intent intent = new Intent(RecorderPlayerActivity.this, MainActivity.class);
                        intent.putExtra("selectIndex", "lv_dvr");
                        startActivity(intent);
                    }
                }
            }, 1000, 1000);
        }
        if (backTimer == null) {
            backTimer = new Timer();
            backTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    // 如果应用切换到后台则关闭当前页面
                    isCurrentRunningForeground = Utils.isRunningForeground(RecorderPlayerActivity.this);
                    if (!isCurrentRunningForeground) {

                        finish();
                    }
                }
            }, 100, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //如果还在播放，则暂停
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        //释放资源
        if (mVideoView != null) {
            mVideoView.destroyDrawingCache();

            mVideoView.stopPlayback();
            mVideoView.setKeepScreenOn(false);
        }
//        if (operatingAnim != null) {
//            screen_recording.clearAnimation();
//        }

        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        if (backTimer != null) {
            backTimer.cancel();
            backTimer = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (ScreenUtil.isRecording) {// 如果Activity在销毁的时候，还在录屏就删除合并之后的文件
//            PreferenceUtil.commitBoolean("isDeleteRecorderFile", true);
            FileUtil.isDeleteFile = true;
            ScreenUtil.stopScreenRecord(this);
            ScreenUtil.isRecording = false;
        }

        if (recTimer != null) {
            recTimer.cancel();
            recTimer = null;
        }
        if (recorderTimer != null) {
            recorderTimer.cancel();
            recorderTimer = null;
        }
        if (timerHandler != null) {
            timerHandler.removeMessages(1);
            timerHandler.removeMessages(2);
            timerHandler.removeMessages(3);
            timerHandler = null;
        }

        //释放资源
        if (mVideoView != null) {
            mVideoView.destroyDrawingCache();

            mVideoView.stopPlayback();
            mVideoView.setKeepScreenOn(false);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (wifiTimer != null) {
            wifiTimer.cancel();
            wifiTimer = null;
        }
        if (backTimer != null) {
            backTimer.cancel();
            backTimer = null;
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (recTimer != null) {
            recTimer.cancel();
            recTimer = null;
        }
        if (recorderTimer != null) {
            recorderTimer.cancel();
            recorderTimer = null;
        }
        if (timerHandler != null) {
            timerHandler.removeMessages(1);
            timerHandler.removeMessages(2);
            timerHandler.removeMessages(3);
            timerHandler = null;
        }

        //释放资源
        if (mVideoView != null) {
            mVideoView.destroyDrawingCache();

            mVideoView.stopPlayback();
            mVideoView.setKeepScreenOn(false);
        }
    }

    /**
     * 初始化声音和亮度
     */
    private void initVolumeWithLight() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mShowVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / mMaxVolume;

        mShowLightness = getScreenBrightness();
    }

    /**
     * 获得当前屏幕亮度值 0--255
     */
    private int getScreenBrightness() {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenBrightness;
    }

    /**
     * 为VideoView添加监听
     */
    private void addVideoViewListener() {
        //准备播放完成
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //获取播放总时长
                mVideoTotalTime = mVideoView.getDuration();

            }
        });

        //正在缓冲
        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
//                if (!mIntoSeek)
                mProgressBar.setVisibility(View.VISIBLE);

                mHandler.removeMessages(UPDATE_PALY_TIME);
                mHandler.removeMessages(HIDE_TIME);
                mIvPlay.setImageResource(R.drawable.video_play);

                if (mVideoView.isPlaying())
                    mVideoView.pause();
            }
        });

        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {

                switch (what) {
                    //缓冲完成
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        mIvPlay.setImageResource(R.drawable.video_pause);
                        mHandler.removeMessages(UPDATE_PALY_TIME);
                        mHandler.removeMessages(HIDE_CONTROL_BAR);
                        mHandler.sendEmptyMessage(UPDATE_PALY_TIME);
                        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                        mProgressBar.setVisibility(View.GONE);

                        if (!mVideoView.isPlaying())
                            mVideoView.start();
                        break;
                }

                return true;
            }
        });

        //视频播放出错
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                    finish();
                    Toast.makeText(RecorderPlayerActivity.this, R.string.video_error, Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        //视频播放完成
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //播放完成后就停止播放和重置视频进度
                mVideoView.pause();
                mIvPlay.setImageResource(R.drawable.video_play);
                mHandler.removeMessages(UPDATE_PALY_TIME);
                mHandler.removeMessages(HIDE_CONTROL_BAR);
                showControlBar();
//                mIntoSeek = true;
                mVideoView.seekTo(0);
            }
        });
    }

    /**
     * 为SeekBar添加监听
     */
    private void addSeekBarListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                long progress = (long) (seekBar.getProgress() * 1.0 / 100 * mVideoView.getDuration());
                mTvTime.setText(sec2time(progress));
                mTvTime2.setText(sec2time(mVideoTotalTime));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //快进或者快退时先停止播放
                mVideoView.pause();
                mIvPlay.setImageResource(R.drawable.video_play);
                mHandler.removeMessages(UPDATE_PALY_TIME);
                mHandler.removeMessages(HIDE_CONTROL_BAR);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                long progress = (long) (seekBar.getProgress() * 1.0 / 100 * mVideoView.getDuration());
                mVideoView.seekTo(progress);
                mHandler.sendEmptyMessage(UPDATE_PALY_TIME);


            }
        });
    }

    /**
     * 添加手势操作
     */
    private void addTouchListener() {
        mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

            //滑动操作
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
//                if (!mIsFullScreen)//非全屏不进行手势操作
//                    return false;

                float x1 = e1.getX();
                float y1 = e1.getY();
                float x2 = e2.getX();
                float y2 = e2.getY();
                float absX = Math.abs(x1 - x2);
                float absY = Math.abs(y1 - y2);

                float absDistanceX = Math.abs(distanceX);// distanceX < 0 从左到右
                float absDistanceY = Math.abs(distanceY);// distanceY < 0 从上到下

                // Y方向的距离比X方向的大，即 上下 滑动
//                if (absDistanceX < absDistanceY && !mIntoSeek) {
                if (absDistanceX < absDistanceY) {
                    if (distanceY > 0) {//向上滑动
                        if (x1 >= mScreenWidth * 0.65) {//右边调节声音
                            changeVolume(ADD_FLAG);
                        } else {//调节亮度
                            changeLightness(ADD_FLAG);
                        }
                    } else {//向下滑动
                        if (x1 >= mScreenWidth * 0.65) {
                            changeVolume(SUB_FLAG);
                        } else {
                            changeLightness(SUB_FLAG);
                        }
                    }

                } else {// X方向的距离比Y方向的大，即 左右 滑动
                    if (absX > absY) {
//                        mIntoSeek = true;
                        onSeekChange(x1, x2);
                        return true;
                    }
                }

                return false;
            }

            //双击事件，有的视频播放器支持双击播放暂停，可从这实现
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }

            //单击事件
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                if (ScreenUtil.isRecording) {

                    mHandler.removeMessages(HIDE_CONTROL_BAR);
                    hideControlBar();

                    if (playOnline != null && (playOnline.contains("CH1") || playOnline.contains("QLY1"))) {

                        if (scaleAni != null) {
                            mVideoView.clearAnimation();
                            scaleAni = null;
                        } else {
                            // 如果在左上角
                            if (e.getX() < mVideoView.getWidth() / 2 && e.getY() < mVideoView.getHeight() / 2) {

                                scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                        0, 0, 0, 0);
                                // 如果在右上角
                            } else if (e.getX() > mVideoView.getWidth() / 2 && e.getY() < mVideoView.getHeight() / 2) {

                                scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                        0, mVideoView.getWidth(), 0, 0);

                                // 如果在左下角
                            } else if (e.getX() < mVideoView.getWidth() / 2 && e.getY() > mVideoView.getHeight() / 2) {

                                scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                        0, 0, 0, mVideoView.getHeight());
                                // 如果在右下角
                            } else if (e.getX() > mVideoView.getWidth() / 2 && e.getY() > mVideoView.getHeight() / 2) {

                                scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                        0, mVideoView.getWidth(), 0, mVideoView.getHeight());
                            }
                            scaleAni.setDuration(500);
                            scaleAni.setFillAfter(true);
                            mVideoView.startAnimation(scaleAni);
                        }
                    } else { // 如果非四目

                    }
                } else {
                    // 如果播放视频包含CH1_（四目视频）
                    if (playOnline != null && (playOnline.contains("CH1") || playOnline.contains("QLY1"))) {
                        // 1.先判断动画有没有在执行，如果执行了动画，就关闭动画，如果没执行动画，再判断点击屏幕的位置
                        if (scaleAni != null) {
                            // 如果在单目情况下，进度条隐藏，单击显示进度条。否则单击退回四目播放
                            if (mControlBottom.getVisibility() == View.VISIBLE || isShowControl) {

                                mVideoView.clearAnimation();
                                scaleAni = null;

                            } else {
                                showControlBar();
                                mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                            }

                        } else {
                            // 如果进度条显示就显示单目动画，否则就显示进度条
                            if (mControlBottom.getVisibility() == View.VISIBLE || isShowControl) {
                                // 如果在左上角
                                if (e.getX() < mVideoView.getWidth() / 2 && e.getY() < mVideoView.getHeight() / 2) {

                                    scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                            0, 0, 0, 0);
                                    // 如果在右上角
                                } else if (e.getX() > mVideoView.getWidth() / 2 && e.getY() < mVideoView.getHeight() / 2) {

                                    scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                            0, mVideoView.getWidth(), 0, 0);

                                    // 如果在左下角
                                } else if (e.getX() < mVideoView.getWidth() / 2 && e.getY() > mVideoView.getHeight() / 2) {

                                    scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                            0, 0, 0, mVideoView.getHeight());
                                    // 如果在右下角
                                } else if (e.getX() > mVideoView.getWidth() / 2 && e.getY() > mVideoView.getHeight() / 2) {

                                    scaleAni = new ScaleAnimation(1f, 2f, 1f, 2f,
                                            0, mVideoView.getWidth(), 0, mVideoView.getHeight());
                                }
                                scaleAni.setDuration(500);
                                scaleAni.setFillAfter(true);
                                mVideoView.startAnimation(scaleAni);

                            } else {
                                showControlBar();
                                mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                            }
                        }
                    } else { // 如果非四目就正常点击显示进度条隐藏
                        if (mControlBottom.getVisibility() == View.VISIBLE) {
                            mHandler.removeMessages(HIDE_CONTROL_BAR);
                            hideControlBar();
                        } else {
                            showControlBar();
                            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                        }
                    }
                }
                return true;
            }
        };
        mGestureDetector = new GestureDetector(this, mSimpleOnGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null)
            mGestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {//手指抬起
            mTvFast.setVisibility(View.GONE);
//            mIntoSeek = false;
            if (mIsFastFinish) {
                mVideoView.seekTo(mSeek);
                mIsFastFinish = false;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                if (mIsFullScreen) {
                    if (mVideoView.isPlaying()) {
                        mHandler.removeMessages(HIDE_CONTROL_BAR);
                        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                    }
                    setupUnFullScreen();

                    finish();
                } else {

                    finish();
                }
                break;

            case R.id.iv_play:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    mIvPlay.setImageResource(R.drawable.video_play);
                    mHandler.removeMessages(UPDATE_PALY_TIME);
                    mHandler.removeMessages(HIDE_CONTROL_BAR);
                    showControlBar();
                } else {
                    mVideoView.start();
                    mIvPlay.setImageResource(R.drawable.video_pause);
                    mHandler.sendEmptyMessage(UPDATE_PALY_TIME);
                    mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                }
                break;

            case R.id.iv_is_fullscreen:
                if (mIsFullScreen) {
                    setupUnFullScreen();
                } else {
                    setupFullScreen();
                }
                if (mVideoView.isPlaying()) {
                    mHandler.removeMessages(HIDE_CONTROL_BAR);
                    mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                }
                break;
        }
    }

    /**
     * 左右滑动距离计算快进/快退时间
     */
    private void onSeekChange(float x1, float x2) {

        long currentPosition = mVideoView.getCurrentPosition();
        long seek = 0;

        if (x1 - x2 > 200) {//向左滑
//            if (currentPosition < 10000) {
//                currentPosition = 0;
//                seek = 0;
//                setFashText(seek);
//                mVideoView.seekTo(currentPosition);
//            } else {
                float ducation = (x1 - x2);
                mVideoView.seekTo(currentPosition - (long) ducation * 10);
                seek = currentPosition - (long) ducation * 10;
                setFashText(seek);
//            }
        } else if (x2 - x1 > 200) { //向右滑动
//            if (currentPosition + 10000 > mVideoView.getDuration()) {
//                currentPosition = mVideoView.getDuration();
//                mVideoView.seekTo(currentPosition);
//                seek = currentPosition;
//                setFashText(seek);
//            } else {
                float ducation = x2 - x1;
                mVideoView.seekTo(currentPosition + (long) ducation * 10);
                seek = currentPosition + (long) ducation * 10;
                setFashText(seek);

                Log.e(".........","current = "+currentPosition+",ducation = "+ducation*10+",total = "+mVideoTotalTime);
//            }
        }

    }

    private void setFashText(long seek) {
        String showTime = StringUtils.generateTime(seek) +
                "/" + StringUtils.generateTime(mVideoView.getDuration());
        mTvFast.setText(showTime);
        mSeek = seek;
        mIsFastFinish = true;

        if (mTvFast.getVisibility() != View.VISIBLE) {
            mTvFast.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 改变声音
     */
    private void changeVolume(int flag) {
        mShowVolume += flag;
        if (mShowVolume > 100) {
            mShowVolume = 100;
        } else if (mShowVolume < 0) {
            mShowVolume = 0;
        }
        mIvControl.setImageResource(R.drawable.volume_icon);
        mTvControl.setText(mShowVolume + "%");
        int tagVolume = mShowVolume * mMaxVolume / 100;
        //tagVolume:音量绝对值
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, tagVolume, 0);

        mHandler.removeMessages(SHOW_CENTER_CONTROL);
        mControlCenter.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(SHOW_CENTER_CONTROL, SHOW_CONTROL_TIME);
    }

    /**
     * 改变亮度
     */
    private void changeLightness(int flag) {
        mShowLightness += flag;
        if (mShowLightness > 255) {
            mShowLightness = 255;
        } else if (mShowLightness <= 0) {
            mShowLightness = 0;
        }
        mIvControl.setImageResource(R.drawable.lightness_icon);
        mTvControl.setText(mShowLightness * 100 / 255 + "%");
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = mShowLightness / 255f;
        getWindow().setAttributes(lp);

        mHandler.removeMessages(SHOW_CENTER_CONTROL);
        mControlCenter.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(SHOW_CENTER_CONTROL, SHOW_CONTROL_TIME);
    }

    /**
     * 隐藏控制条
     */
    private void hideControlBar() {
        mControlBottom.setVisibility(View.GONE);
        mControlTop.setVisibility(View.GONE);
    }

    /**
     * 显示控制条
     */
    private void showControlBar() {
        isShowControl = true;
        mControlBottom.setVisibility(View.VISIBLE);
        mControlTop.setVisibility(View.VISIBLE);
    }

    /**
     * 设置为全屏
     */
    private void setupFullScreen() {
        //设置窗口模式
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //获取屏幕尺寸
        WindowManager manager = this.getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);

        //设置Video布局尺寸
        mVideoLayout.getLayoutParams().width = metrics.widthPixels;
        mVideoLayout.getLayoutParams().height = metrics.heightPixels;

        //设置为全屏拉伸
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        mIvIsFullScreen.setImageResource(R.drawable.not_fullscreen);

        mIsFullScreen = true;
    }

    /**
     * 设置为非全屏
     */
    private void setupUnFullScreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        float width = getResources().getDisplayMetrics().heightPixels;
        float height = getResources().getDisplayMetrics().widthPixels;
        mVideoLayout.getLayoutParams().width = (int) width;
        mVideoLayout.getLayoutParams().height = (int) height;

        //设置为全屏
        mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        mIvIsFullScreen.setImageResource(R.drawable.play_fullscreen);

        mIsFullScreen = false;
    }

    @Override
    public void onBackPressed() {
        if (mIsFullScreen) {
            setupUnFullScreen();
        } else {
            super.onBackPressed();
        }


    }

    /**
     * 弹出加载对话框
     *
     * @param label
     */
    private void showProgress(String label) {
        if (kProgressHUD == null) {
            kProgressHUD = KProgressHUD.create(RecorderPlayerActivity.this);
        }
        kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        kProgressHUD.setLabel(label);
        kProgressHUD.setCancellable(true);
        kProgressHUD.setAnimationSpeed(2);
        kProgressHUD.setDimAmount(0.5f);
        if (!kProgressHUD.isShowing()) {
            kProgressHUD.show();
        }
    }

    /**
     * 录屏
     *
     * @param view
     */
    public void screenRecording(View view) {


        if (ScreenUtil.isRecording) {
            //隐藏录屏时间
            recording_time.setVisibility(View.GONE);
            ScreenUtil.stopScreenRecord(this);
            //停止计算录屏时间
            if (recTimer != null) {
                recTimer.cancel();
            }
            screen_recording_open.clearAnimation();
            screen_recording_open.setImageResource(R.mipmap.screen_recording_stop1);

            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            }

            //弹出加载对话框
            showProgress(getString(R.string.saveRecorderFileNow));
            timerHandler.sendEmptyMessage(1);

        } else {
            PackageManager pkgManager = getPackageManager();

            boolean audioSatePermission =
                    pkgManager.checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName()) == PackageManager.PERMISSION_GRANTED;
            if (Build.VERSION.SDK_INT >= 23 && !audioSatePermission) {
                requestPermission();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //如果视频暂停了就恢复播放
                if (!mVideoView.isPlaying()) {
                    mVideoView.start();
                    mIvPlay.setImageResource(R.drawable.video_pause);
                    mHandler.sendEmptyMessage(UPDATE_PALY_TIME);
                    mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
                }
                //隐藏控制条
                hideControlBar();
                screen_recording_open.setImageResource(R.mipmap.screen_recording_start1);
                //  开始录屏
                ScreenUtil.startScreenRecord(this, REQUEST_CODE);
                // 隐藏虚拟按键
//                hideBottomUIMenu();
                FileUtil.isDeleteFile = false;
                final Animation animation = new AlphaAnimation(1, 0);
                animation.setDuration(750);//闪烁时间间隔
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                animation.setRepeatCount(Animation.INFINITE);
                animation.setRepeatMode(Animation.REVERSE);

                screen_recording_open.setAnimation(animation);
                screen_recording_open.startAnimation(animation);

            } else {
                Toast.makeText(this, R.string.recording_tip, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
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
                .show();
        dialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 开启录制 Service
     */
    private void startScreenRecordService() {

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ScreenRecordService.RecordBinder recordBinder = (ScreenRecordService.RecordBinder) service;
                ScreenRecordService screenRecordService = recordBinder.getRecordService();
                ScreenUtil.setScreenService(screenRecordService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(this, ScreenRecordService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        ScreenUtil.addRecordListener(recordListener);
    }

    private ScreenUtil.RecordListener recordListener = new ScreenUtil.RecordListener() {
        @Override
        public void onStartRecord() {

        }

        @Override
        public void onPauseRecord() {

        }

        @Override
        public void onResumeRecord() {

        }

        @Override
        public void onStopRecord(String stopTip) {
//            ToastUtil.show(ScreenRecordActivity.this,stopTip);
        }

        @Override
        public void onRecording(String timeTip) {
//            mTvTime.setText(timeTip);
        }
    };

    /**
     * 计算录屏时间
     *
     * @param min
     * @param sec
     */
    private void countRecTime(int min, int sec) {
        minute = min;
        second = sec;

        //用来计算录像时间的Timer
        recTimer = new Timer();
        recTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.sendEmptyMessage(2);
            }
        }, 1000, 1000);
    }

    private String deleteName;
    //用来显示本地时间的Handler
    Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 1:
                    //先设置为能点击
                    screen_recording.setEnabled(true);
                    //关闭加载对话框
                    if (kProgressHUD != null) {
                        kProgressHUD.dismiss();
                    }
                    //判断是否已合并完成
                    PreferenceUtil.commitInt("isFinishRecorder", 0);
                    showMyDialog("你录制的事故片段‘" + PreferenceUtil.getString("playVideoName", "") + "’已下载到本地文件目录中，请根据需要选择以下操作");
                    break;
                case 2:
                    //显示计算录屏时间的面板
                    recording_time.setVisibility(View.VISIBLE);
                    second -= 1;
                    if (second < 10) {
                        rec_second.setText("0" + second);
                    } else {
                        rec_second.setText(second + "");
                    }
                    if (second <= 0) {

                        if (ScreenUtil.isRecording) {
                            //隐藏录屏时间
                            recording_time.setVisibility(View.GONE);
                            //停止计算录屏时间
                            if (recTimer != null) {
                                recTimer.cancel();
                            }
                            screen_recording_open.setImageResource(R.mipmap.screen_recording_stop1);
                            ScreenUtil.stopScreenRecord(RecorderPlayerActivity.this);
                            //如果还在播放，则暂停
                            if (mVideoView.isPlaying()) {
                                mVideoView.pause();
                            }
                            //弹出加载对话框
                            showProgress(getString(R.string.saveRecorderFileNow));
                            recorderTimer = new Timer();
                            recorderTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    timerHandler.sendEmptyMessage(1);
                                    //停止计时器
                                    recorderTimer.cancel();
                                    //                    }
                                }
                            }, 1000, 1000);
                        }
                    }
                    break;
                case 3:
                    //隐藏录屏时间
                    recording_time.setVisibility(View.GONE);
                    //停止计算录屏时间
                    if (recTimer != null) {
                        recTimer.cancel();
                    }
                    screen_recording_open.setImageResource(R.mipmap.screen_recording_stop1);
                    mRecorder.quit();
                    mRecorder = null;
                    //如果还在播放，则暂停
                    if (mVideoView.isPlaying()) {
                        mVideoView.pause();
                    }
                    //弹出加载对话框
                    showProgress(getString(R.string.saveRecorderFileNow));
                    //每1秒判断一次是否录屏文件是否已合成
                    recorderTimer = new Timer();
                    recorderTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (PreferenceUtil.getInt("isFinishRecorder", 0) == 1) {
                                timerHandler.sendEmptyMessage(1);
                                //停止计时器
                                recorderTimer.cancel();

                                if (mRecorder != null) {
                                    //删除刚刚的录屏文件
                                    if (VLCApplication.getVorangeType() == VorangeType.B1) {
                                        deleteName = PreferenceUtil.getString("recorder_name", "");
                                    } else {
                                        deleteName = PreferenceUtil.getString("recorder_name", "");
                                    }
                                    File removeFile = new File(deleteName);
                                    if (removeFile.isFile()) {
                                        removeFile.delete();
                                    }
                                }
                                //移除所有的activity
                                VLCApplication.removeALLActivity();
                                Intent intent = new Intent(RecorderPlayerActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    }, 1000, 1000);
                    break;
                case 4:

                    //删除刚刚的录屏文件
                    if (VLCApplication.getVorangeType() == VorangeType.B1) {
                        deleteName = PreferenceUtil.getString("recorder_name", "");
                    } else {
                        deleteName = PreferenceUtil.getString("recorder_name", "");
                    }
                    File removeFile = new File(deleteName);
                    if (removeFile.isFile()) {
                        removeFile.delete();
                    }
                    screen_recording_open.clearAnimation();

                    mIvPlay.setImageResource(R.drawable.video_play);
                    mHandler.removeMessages(UPDATE_PALY_TIME);
                    mHandler.removeMessages(HIDE_CONTROL_BAR);
                    showControlBar();
                    break;
            }
        }
    };

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}
