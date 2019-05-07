package com.example.administrator.lc_dvr.common.constant;

import org.videolan.libvlc.VLCApplication;

/**
 * ant compile // conditioal compile
 * 常量都放在这个类
 * 注意：下载接口、缩略图接口、删除单一项接口没有写在这里
 * Created by Administrator on 2016/4/5.
 */
public class Constant {
    //打开直播流
    public static final String OPEN_VIDEO = "http://192.168.1.254:8010/command/63/1";
    //关闭直播流
    public static final String CLOSE_VIDEO = "http://192.168.1.254:8010/command/63/0";
    //连接行车记录仪的摄像头
    public static String VIDEO_URL = VLCApplication.VIDEO_URL;
    //判断是否在录像和获得录像了多长时间都是这个接口
    public static final String CHECK_STATE = "http://192.168.1.254/?custom=1&cmd=2016";
    //停止录像
    public static final String STOP_MOVIE_RECORD = "http://192.168.1.254:80/?custom=1&cmd=2001&par=0";
    //设置日期
    public static final String SET_DATE = "http://192.168.1.254:80/?custom=1&cmd=3005&str=";
    //设置时间
    public static final String SET_TIME = "http://192.168.1.254:80/?custom=1&cmd=3006&str=";
    //设置视频画质为1080p
    public static final String VIDEO_1080 = "http://192.168.1.254:80/?custom=1&cmd=2002&par=0";
    //设置视频画质为720p
    public static final String VIDEO_720 = "http://192.168.1.254:80/?custom=1&cmd=2002&par=1";
    //开始录像
    public static final String START_MOVIE_RECORD = "http://192.168.1.254:80/?custom=1&cmd=2001&par=1";
    //设置视频分段为1分钟
    public static final String SUBSECTION_1 = "http://192.168.1.254:80/?custom=1&cmd=2003&par=0";
    //设置视频分段为2分钟
    public static final String SUBSECTION_2 = "http://192.168.1.254:80/?custom=1&cmd=2003&par=1";
    //设置视频分段为3分钟
    public static final String SUBSECTION_3 = "http://192.168.1.254:80/?custom=1&cmd=2003&par=2";
    //设置视频分段为5分钟
    public static final String SUBSECTION_5 = "http://192.168.1.254:80/?custom=1&cmd=2003&par=3";
    //设置视频分段为10分钟
    public static final String SUBSECTION_10 = "http://192.168.1.254:80/?custom=1&cmd=2003&par=4";
    //恢复默认设置
    public static final String DEFAULT_SET = "http://192.168.1.254:80/?custom=1&cmd=3011";
    //格式化sd
    public static final String FORMAT_CARD = "http://192.168.1.254:80/?custom=1&cmd=3010&par=1";
    //得到录像的列表
    public static final String VIDEO_LIST = "http://192.168.1.254/?custom=1&cmd=3015";
    //得到图片的列表
    public static final String PHOTO_LIST = "http://192.168.1.254/?custom=1&cmd=3030";
    //设置HDR为关闭
    public static final String HDR_OFF = "http://192.168.1.254:80/?custom=1&cmd=2004&par=0";
    //设置HDR为开启
    public static final String HDR_ON = "http://192.168.1.254:80/?custom=1&cmd=2004&par=1";

    //设置ADAS为关闭
    public static final String ADAS_OFF = "http://192.168.1.254/?custom=1&cmd=2088&par=2";
    //设置ADAS为开启
    public static final String ADAS_ON = "http://192.168.1.254/?custom=1&cmd=2088&par=1";

    //设置停车监控为关闭
    public static final String PARKINGMONITORING_OFF = "http://192.168.1.254/?custom=1&cmd=2089&par=0";
    //设置停车监控为开启
    public static final String PARKINGMONITORING_ON = "http://192.168.1.254/?custom=1&cmd=2089&par=1";

    //移动侦测
    public static final String MOTION_OFF = "http://192.168.1.254:80/?custom=1&cmd=2006&par=0";
    public static final String MOTION_ON = "http://192.168.1.254:80/?custom=1&cmd=2006&par=1";
    //录像音频
    public static final String AUDIO_OFF = "http://192.168.1.254:80/?custom=1&cmd=2007&par=0";
    public static final String AUDIO_ON = "http://192.168.1.254:80/?custom=1&cmd=2007&par=1";
    //日期记录
    public static final String DATE_OFF = "http://192.168.1.254:80/?custom=1&cmd=2008&par=0";
    public static final String DATE_ON = "http://192.168.1.254:80/?custom=1&cmd=2008&par=1";
    //重力感应
    public static final String GSENSOR_OFF = "http://192.168.1.254:80/?custom=1&cmd=2011&par=0";
    public static final String GSENSOR_LOW = "http://192.168.1.254:80/?custom=1&cmd=2011&par=1";
    public static final String GSENSOR_MID = "http://192.168.1.254:80/?custom=1&cmd=2011&par=2";
    public static final String GSENSOR_HIGH = "http://192.168.1.254:80/?custom=1&cmd=2011&par=3";

    //音频音量
    public static final String VOLUME_LOW = "http://192.168.1.254/?custom=1&cmd=2090&par=2";
    public static final String VOLUME_MID = "http://192.168.1.254/?custom=1&cmd=2090&par=1";
    public static final String VOLUME_HIGH = "http://192.168.1.254/?custom=1&cmd=2090&par=0";

    //切换到照相模式
    public static final String MODE_PHOTO = "http://192.168.1.254:80/?custom=1&cmd=3001&par=0";
    //切换到录像模式
    public static final String MODE_VIDEO = "http://192.168.1.254:80/?custom=1&cmd=3001&par=1";
    //判断sd卡是否插入
    public static final String CARD_STATE = "http://192.168.1.254:80/?custom=1&cmd=3024";
    //拍照
    public static final String TAKE_PHOTO = "http://192.168.1.254/?custom=1&cmd=1001";
    //版本号信息
    public static final String BAN_BEN = "http://192.168.1.254/?custom=1&cmd=3012";
    //程序终止时自动开始录像要用到
    public static final String TERMINATION = "http://192.168.1.254/?custom=1&cmd=3016";
    //同步dvr的状态
    public static final String DVR_STATE = "http://192.168.1.254:80?custom=1&cmd=3014";
    // Make sure Car DV is in photo mode.
    public static final String DV_MODE_PHOTO = "http://192.168.1.254/?custom=1&cmd=3001&par=0";
    // Make sure Car DV is in movie mode.
    public static final String DV_MODE_MOIVE = "http://192.168.1.254/?custom=1&cmd=3001&par=1";
    // Make sure Car DV is in playback mode.
    public static final String DV_MODE_PLAYBACK = "http://192.168.1.254/?custom=1&cmd=3001&par=2";
    // HTTP MJPG streaming
    public static final String MJPEG_STREAM_URL_HTTP  = "http://192.168.1.254:8192";


}
