package com.example.administrator.lc_dvr.module.lc_dvr_files_manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.example.administrator.lc_dvr.common.utils.ScreenListener;
import com.example.administrator.lc_dvr.common.utils.Utils;

import org.videolan.libvlc.VLCApplication;

import java.util.List;
import java.util.Locale;
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
public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {

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
    private TextView videoTitle;

    private int mScreenWidth = 0;//屏幕宽度
    private boolean mIsFullScreen = false;//是否为全屏
    private int mVideoTotalTime = 0;//视频总时间
    private boolean mIntoSeek = false;//是否 快进/快退
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
                    int currentPosition = (int) mVideoView.getCurrentPosition();
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
    private List<String> videoLists;
    private String playOnline;
    private int videoOrder;
    private int isLock;
    private int is_recorder;
    private Resources resources;
    private Configuration config;
    private DisplayMetrics dm;
    private TextView mTvTime2;
    private Timer wifiTimer;
    private Timer backTimer;

    private ScreenListener listener;//用来监听是否锁屏
    //用来控制应用前后台切换的逻辑
    private boolean isCurrentRunningForeground = true;

    /**
     * 作用是屏幕发生变化时，activity不会重新启动，只会执行这个方法，前提条件是：必须先在清单文件中声明ConfigurationChanged属性
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        String language = PreferenceUtil.getString("language", "cn");
        if (language.equals("english")) {
            config.locale = Locale.ENGLISH;
            resources.updateConfiguration(config, dm);
        } else if (language.equals("german")) {
            //设置语言为德文
            config.locale = Locale.GERMAN;
            resources.updateConfiguration(config, dm);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(this);
        setContentView(R.layout.activity_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mVideoView = (VideoView) findViewById(R.id.videoview);
        mControlTop = (RelativeLayout) findViewById(R.id.control_top);
        mControlBottom = (RelativeLayout) findViewById(R.id.control_bottom);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
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

        resources = getResources();
        config = resources.getConfiguration();
        dm = resources.getDisplayMetrics();

        //作用是当调节手机音量时，调的是媒体音量，而不是其他音量
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Intent intent = getIntent();
        //用来判断是多选操作还是单选操作
        tag = intent.getIntExtra("tag", 0);
        //用来判断是本地还是在线视频
        phone = intent.getIntExtra("phone", 0);
        //用来判断是录屏还是dvr视频
        is_recorder = intent.getIntExtra("is_recorder", 0);
        //用来判断是否是加锁视频
        isLock = intent.getIntExtra("isLock", 0);
        if (tag == 1) {
            videoLists = (List<String>) intent.getSerializableExtra("video_list_play");
            if (phone == 1) {//phone为1时表示的是本地
                playOnline = BitmapUtils.getSDPath() + "/DVRvideo/" + videoLists.get(0);
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
            videoOrder = 1;//视频的播放顺序

        } else {
            String video_play = intent.getStringExtra("video_play");
            if (phone == 1) {//phone为1时表示的是本地
                if (video_play.contains("http")) {
                    playOnline = video_play;
                } else {
                    if (is_recorder == 1) {
                        playOnline = BitmapUtils.getSDPath() + "/VOC/ACCIDENT/" + video_play;
                    } else {
                        playOnline = BitmapUtils.getSDPath() + "/VOC/" + video_play;
                    }
                }
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
            videoTitle.setText(video_play);
            mIvBack.setOnClickListener(this);
            mIvPlay.setOnClickListener(this);
            mIvIsFullScreen.setOnClickListener(this);
            init(playOnline);
        }
        //添加当前的activity
        VLCApplication.addActivity(this);
    }

    private void init(String url) {
        //获取屏幕宽度
        Pair<Integer, Integer> screenPair = ScreenResolution.getResolution(this);
        mScreenWidth = screenPair.first;
        //播放网络资源
        mVideoView.setVideoPath(url);

        listener = new ScreenListener(this);
        listener.register(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {

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
                finish();
            }

            @Override
            public void onScreenOff() {

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
                finish();
            }

            @Override
            public void onUserPresent() {
            }
        });
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
        }
        mVideoView.setKeepScreenOn(true);//让屏幕不休眠

        //判断是不是从报案页面进来的
        if (!PreferenceUtil.getBoolean("isReport", false)) {
            if (!playOnline.contains("http")) {
                if (wifiTimer == null) {
                    wifiTimer = new Timer();
                    wifiTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //如果断开了dvr的wifi就跳转到失败的页面
                            if (getDvrWifiName(getApplicationContext()) == null) {
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
                                //释放资源
                                if (mVideoView != null) {
                                    mVideoView.destroyDrawingCache();
                                    mVideoView.stopPlayback();
                                    mVideoView.setKeepScreenOn(false);
                                    mVideoView = null;
                                }
                                //移除所有的activity
                                VLCApplication.removeALLActivity();
                                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                                intent.putExtra("selectIndex", "lv_dvr");
                                startActivity(intent);
//                                Udriver.udriver.closeDevice();
                            }
                        }
                    }, 1000, 1000);
                }
            }
        }
        if (backTimer == null) {
            backTimer = new Timer();
            backTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // 如果应用切换到后台则关闭当前页面
                    isCurrentRunningForeground = Utils.isRunningForeground(PlayerActivity.this);

                    if (!isCurrentRunningForeground) {
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
                        //释放资源
                        if (mVideoView != null) {
                            mVideoView.destroyDrawingCache();
                            mVideoView.stopPlayback();
                            mVideoView.setKeepScreenOn(false);
                            mVideoView = null;
                        }
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
        // 屏幕广播的注销
        if (listener != null) {
            listener.unregister();
        }
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
        //释放资源
        if (mVideoView != null) {
            mVideoView.pause();
            mVideoView.destroyDrawingCache();
            mVideoView.stopPlayback();
            mVideoView.setKeepScreenOn(false);
            mVideoView = null;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 屏幕广播的注销
        if (listener != null) {
            listener.unregister();
        }
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
        //释放资源
        if (mVideoView != null) {
            mVideoView.destroyDrawingCache();
            mVideoView.stopPlayback();
            mVideoView.setKeepScreenOn(false);
            mVideoView = null;
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
                mVideoTotalTime = (int) mVideoView.getDuration();
            }
        });

        //正在缓冲
        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (!mIntoSeek)
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
                    Toast.makeText(PlayerActivity.this, R.string.video_error, Toast.LENGTH_SHORT).show();
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
                mIntoSeek = true;
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

                if (!mVideoView.isPlaying()) {
                    mVideoView.start();
                    mIvPlay.setImageResource(R.drawable.video_pause);
                }
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
                if (!mIsFullScreen)//非全屏不进行手势操作
                    return false;

                float x1 = e1.getX();
                float y1 = e1.getY();
                float x2 = e2.getX();
                float y2 = e2.getY();
                float absX = Math.abs(x1 - x2);
                float absY = Math.abs(y1 - y2);

                float absDistanceX = Math.abs(distanceX);// distanceX < 0 从左到右
                float absDistanceY = Math.abs(distanceY);// distanceY < 0 从上到下

                // Y方向的距离比X方向的大，即 上下 滑动
                if (absDistanceX < absDistanceY && !mIntoSeek) {
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
                        mIntoSeek = true;
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

                if (!mVideoView.isPlaying())
                    return false;

                if (mControlBottom.getVisibility() == View.VISIBLE) {
                    mHandler.removeMessages(HIDE_CONTROL_BAR);
                    hideControlBar();
                } else {
                    showControlBar();
                    mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_BAR, HIDE_TIME);
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
            mIntoSeek = false;
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
            if (currentPosition < 10000) {
                currentPosition = 0;
                seek = 0;
                setFashText(seek);
                mVideoView.seekTo(currentPosition);
            } else {
                float ducation = (x1 - x2);
                mVideoView.seekTo(currentPosition - (long) ducation * 10);
                seek = currentPosition - (long) ducation * 10;
                setFashText(seek);
            }
        } else if (x2 - x1 > 200) { //向右滑动
            if (currentPosition + 10000 > mVideoView.getDuration()) {
                currentPosition = mVideoView.getDuration();
                mVideoView.seekTo(currentPosition);
                seek = currentPosition;
                setFashText(seek);
            } else {
                float ducation = x2 - x1;
                mVideoView.seekTo(currentPosition + (long) ducation * 10);
                seek = currentPosition + (long) ducation * 10;
                setFashText(seek);
            }
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
     * dp转px
     *
     * @param dpValue
     * @return
     */
    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

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
}
