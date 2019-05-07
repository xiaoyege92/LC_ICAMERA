/*****************************************************************************
 * VLCApplication.java
 * ****************************************************************************
 * Copyright © 2010-2013 VLC authors and VideoLAN
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/
package org.videolan.libvlc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.Config;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.listener.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dxing.udriver.Udriver;
import com.example.administrator.lc_dvr.base.VorangeType;
import com.example.administrator.lc_dvr.bean.BannerPicture;
import com.example.administrator.lc_dvr.common.constant.Constant;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.storage.UploadManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreater;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class VLCApplication extends Udriver {
    public final static String TAG = "VLC/VLCApplication";
    private static VLCApplication instance;
    public static boolean isUpdate = true;//是否提示升级
    public static RequestQueue queue;
    public static WifiManager wifiManager;
    //不同行车记录仪的直播URL
    public static String VIDEO_URL;
    public static ArrayList<Object> dvrVideoList;
    public static ArrayList<Object> dvrPhotoList;
    public static ArrayList<Object> lockVideoList;
    public static HashMap<String, String> insuranceCompanyDictionary;
    public static ArrayList<BannerPicture> bannerPictureList;

    public static String unitName; // 单位名称
    public static String unitIconUrl; //单位icon
    public static String unitAddress; //单位地址
    public static String unitServiceTime; //单位服务时间
    public static String unitHelpTip; //单位提示
    public static String unitHelpURL; //单位提示连接


    public static boolean noOneFile = true;//是否不是只有一个文件

    //用来判断软件是否进入了后台
    public int count = 0;

    private static VLCApplication mApp;
    private MediaProjectionManager mMpMgr;

    private static List<Activity> oList;//用于存放所有启动的Activity的集合

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                //指定为经典Header，默认是 贝塞尔雷达Header
                return new ClassicsHeader(context);
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreater(new DefaultRefreshFooterCreater() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    public static IWXAPI wxapi;

    public static Map<String, List<Map<String, String>>> compensatepictures;
    public static List<Map<String, String>> license_b;
    public static List<Map<String, String>> frame;
    public static List<Map<String, String>> license_a;
    public static List<Map<String, String>> permit;
    public static List<Map<String, String>> card;
    public static List<Map<String, String>> drivelicense;


    public static Map<String, List<Map<String, String>>> casepictures;
    public static List<Map<String, String>> one_a;
    public static List<Map<String, String>> one_b;
    public static List<Map<String, String>> one_c;
    public static List<Map<String, String>> more_a;
    public static List<Map<String, String>> more_b;
    public static List<Map<String, String>> more_c;
    public static List<Map<String, String>> more_d;
    public static List<Map<String, String>> more_e;
    public static List<Map<String, String>> video;
    private Timer terminationTimer;
    public static Map<String, String> configsDictionary;

    private com.qiniu.android.storage.Configuration config = new com.qiniu.android.storage.Configuration.Builder()
            .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
            .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
            .connectTimeout(10)           // 链接超时。默认10秒
            .useHttps(true)               // 是否使用https上传域名
            .responseTimeout(60)          // 服务器响应超时。默认60秒
            .zone(FixedZone.zone0)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
            .build();
    // 重用uploadManager。一般地，只需要创建一个uploadManager对象
    public static UploadManager uploadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        //在这里初始化
        uploadManager = new UploadManager(config);

        //casepictures集合
        casepictures = new HashMap<>();

        //one_a集合
        one_a = new ArrayList<>();

        //one_b集合
        one_b = new ArrayList<>();

        //one_c集合
        one_c = new ArrayList<>();

        //more_a集合
        more_a = new ArrayList<>();

        //more_b集合
        more_b = new ArrayList<>();

        //more_c集合
        more_c = new ArrayList<>();

        //more_d集合
        more_d = new ArrayList<>();

        //more_e集合
        more_e = new ArrayList<>();

        //video集合
        video = new ArrayList<>();

        //compensatepictures集合
        compensatepictures = new HashMap<>();

        //保存服务器的所有字典
        configsDictionary = new HashMap<>();

        //license_b集合
        license_b = new ArrayList<>();

        //frame集合
        frame = new ArrayList<>();

        //license_a集合
        license_a = new ArrayList<>();

        //permit集合
        permit = new ArrayList<>();

        //card集合
        card = new ArrayList<>();

        //drivelicense集合
        drivelicense = new ArrayList<>();

        //向微信注册
        wxapi = WXAPIFactory.createWXAPI(this, Config.WEIXIN_APPID, true);
        wxapi.registerApp(Config.WEIXIN_APPID);

        //初始化Activity的集合
        oList = new ArrayList<Activity>();

        mApp = this;

        /*
        初始化数据库
         */
        LitePal.initialize(this);

        //保存保险公司的字典
        insuranceCompanyDictionary = new HashMap<String, String>();
        bannerPictureList = new ArrayList<>();

        //保存dvr视频列表数据
        dvrVideoList = new ArrayList<>();
        //保存dvr图片列表数据
        dvrPhotoList = new ArrayList<>();
        //保存dvrJ加锁视频列表数据
        lockVideoList = new ArrayList<>();

        /*
        初始化wifi管理器
         */
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        /*
        初始化ImageLoader
         */
        DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder() //
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)//设置下载的图片下载前是否重置，复位
                .cacheInMemory(true)//设置下载图片是否缓存到内存
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片解码类型
                .displayer(new FadeInBitmapDisplayer(300))//设置用户加载图片的task(这里是渐现)
                .build(); //

        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultDisplayImageOptions)
                .memoryCacheExtraOptions(480, 800)
                .discCacheSize(100 * 1024 * 1024)//在sdcard缓存100MB
                .threadPoolSize(5)
                .build();

        ImageLoader.getInstance().init(imageLoaderConfiguration);
        /*
        初始化Volley
         */
        queue = Volley.newRequestQueue(this);

        /*
        初始化PreferenceUtil
         */
        PreferenceUtil.init(this);

        /*
        显示上一次设置的语言
         */
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        String language = PreferenceUtil.getString("language", "chinese");
        if (language.equals("english")) {
            configuration.locale = Locale.ENGLISH;
            resources.updateConfiguration(configuration, displayMetrics);
        } else if (language.equals("chinese")) {
            configuration.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(configuration, displayMetrics);
        } else {
            //设置为德文
            configuration.locale = Locale.GERMAN;
            resources.updateConfiguration(configuration, displayMetrics);
        }

        //每10秒钟发送一次命令，当没有命令发送时，记录仪60秒后自动开启录像
        terminationTap();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String p = pref.getString("set_locale", "");
        if (p != null && !p.equals("")) {
            Locale locale;
            // workaround due to region code
            if (p.equals("zh-TW")) {
                locale = Locale.TRADITIONAL_CHINESE;
            } else if (p.startsWith("zh")) {
                locale = Locale.CHINA;
            } else if (p.equals("pt-BR")) {
                locale = new Locale("pt", "BR");
            } else if (p.equals("bn-IN") || p.startsWith("bn")) {
                locale = new Locale("bn", "IN");
            } else {
                /**
                 * Avoid a crash of
                 * java.lang.AssertionError: couldn't initialize LocaleData for locale
                 * if the user enters nonsensical region codes.
                 */
                if (p.contains("-"))
                    p = p.substring(0, p.indexOf('-'));
                locale = new Locale(p);
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        instance = this;

    }

    /**
     * 每10秒钟发送一次命令，当没有命令发送时，记录仪60秒后自动开启录像
     */
    private void terminationTap() {
        terminationTimer = new Timer();
        terminationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                StringRequest stringrequest = new StringRequest(Request.Method.GET, Constant.TERMINATION, new Listener<String>() {
                    @Override
                    public void onSuccess(String s) {
                    }

                    @Override
                    public void onError(VolleyError volleyError) {
                        super.onError(volleyError);
                    }
                });
                VLCApplication.queue.add(stringrequest);
            }
        }, 10000, 10000);
    }

    /**
     * Called when the overall system is running low on memory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext() {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources() {
        if (instance == null) return null;
        return instance.getResources();
    }

    public static VLCApplication getInstance() {
        return mApp;
    }

    /**
     * 添加Activity
     */
    public static void addActivity(Activity activity) {
        // 判断当前集合中不存在该Activity
        if (!oList.contains(activity)) {
            oList.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁所有的Activity
     */
    public static void removeALLActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (Activity activity : oList) {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    /**
     * 获取wifi的名字并判断是否连接上了记录仪的wifi
     *
     * @return
     */
    public static int getWifiSsid() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        if (ssid.contains("ACV_DVR") || ssid.contains("DAZA DVR") || ssid.contains("Z900_") || ssid.contains("CAR_DVR") || ssid.contains("CAR-DVR")) {
            return 1;
        } else if (ssid.contains("HAVAL_DVR") || ssid.contains("DAZA_CARDV") || ssid.contains("NVT_CARDV") || ssid.contains("VORANGE")) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * 获得dvr的wifi名字
     *
     * @return
     */
    public static String getDvrWifiName() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {//如果WiFi状态时关的就直接返回空
            return null;
        }
        String ssid = wifiInfo.getSSID();
        if (ssid.contains("ACV_DVR") || ssid.contains("DAZA DVR") || ssid.contains("Z900_") || ssid.contains("CAR_DVR") || ssid.contains("CAR-DVR")) {
            return ssid;
        } else if (ssid.contains("HAVAL_DVR") || ssid.contains("DAZA_CARDV") || ssid.contains("NVT_CARDV") || ssid.contains("VORANGE")) {
            return ssid;
        } else {
            return null;
        }
    }

    /**
     * 获得dvr的wifi名字
     *
     * @return
     */
    public static String getDvrWifiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {//如果WiFi状态时关的就直接返回空
            return null;
        }
        String ssid = wifiInfo.getSSID();
        if (ssid.contains("ACV_DVR") || ssid.contains("DAZA DVR") || ssid.contains("Z900_") || ssid.contains("CAR_DVR") || ssid.contains("CAR-DVR")) {
            return ssid;
        } else if (ssid.contains("HAVAL_DVR") || ssid.contains("DAZA_CARDV") || ssid.contains("NVT_CARDV") || ssid.contains("VORANGE")) {
            return ssid;
        } else {
            return null;
        }
    }

    /**
     * 获得dvr的wifi名字
     *
     * @return
     */
    public static VorangeType getVorangeType() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        if (ssid != null && (ssid.contains("VORANGE_C1") || ssid.contains("CAR_DVR")) || ssid.contains("CAR-DVR")) {
            return VorangeType.C1;
        } else if (ssid.contains("VORANGE_B1")) {
            return VorangeType.B1;
        } else if (ssid != null && ssid.contains("VORANGE_D1")) {
            return VorangeType.D1;
        } else {
            return null;
        }
    }

}
