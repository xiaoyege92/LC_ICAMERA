package com.example.administrator.lc_dvr.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *   author : zch
 *   e-mail : 815801028@qq.com
 *   time   : 2018/07/18
 *   desc   :
 *  version :
 * </pre>
 */
public class Utils {

    // 判断是否为11手机号的正则表达式规则
    public final static String PHONE_PATTERN = "^((1[3-9]))\\d{9}$";
    private static MyCallBack mMyCallBack;
    private static OneMoreSwitchCallBack oneMoreSwitchCallBack;
    private static ChangeFragmentCallBack changeFragmentCallBack;

    public static String root_path = Environment.getExternalStorageDirectory().toString();
    public static String local_thumbnail_path = root_path + "/IPCAM/THUMBNAIL";

    public static boolean checkLocalFolder() {

        File nvt_dir = new File(root_path + "/IPCAM");
        if (!nvt_dir.exists())
            nvt_dir.mkdir();

        File tn_dir = new File(root_path + "/IPCAM/THUMBNAIL");
        if (!tn_dir.exists())
            tn_dir.mkdir();

        File dtn_dir = new File(root_path + "/IPCAM/DEVICE_THUMBNAIL");
        if (!dtn_dir.exists())
            dtn_dir.mkdir();

        File ph_dir = new File(root_path + "/IPCAM/PHOTO");
        if (!ph_dir.exists())
            ph_dir.mkdir();

        File mv_dir = new File(root_path + "/IPCAM/MOVIE");
        if (!mv_dir.exists())
            mv_dir.mkdir();

        File log_dir = new File(root_path + "/IPCAM/LOG");
        if (!log_dir.exists())
            log_dir.mkdir();

        File devices_dir = new File(root_path + "/IPCAM/DEVICES");
        if (!devices_dir.exists())
            devices_dir.mkdir();

        return (tn_dir.exists() && ph_dir.exists() && mv_dir.exists() && log_dir.exists());
    }

    /**
     * 正则表达式匹配判断 是否为11位手机号
     *
     * @param input 需要做匹配操作的字符串
     * @return true if matched, else false
     */
    public static boolean isMatchered(CharSequence input) {
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    public static void setCallBack(MyCallBack myCallBack) {
        mMyCallBack = myCallBack;
    }

    public static void doCallBackMethod() {
        if (mMyCallBack != null) {
            mMyCallBack.doSomeThing();
        }
    }

    // 单车多车切换回调函数
    public static void setOneMoreSwitchCallBack(OneMoreSwitchCallBack callBack) {
        oneMoreSwitchCallBack = callBack;
    }
    public static void doOneMoreSwitch() {
        if(oneMoreSwitchCallBack != null) {
            oneMoreSwitchCallBack.oneMoreSwitch();
        }
    }

    // MainActivity中fragment切换
    public static void setChangeFragmentCallBack(ChangeFragmentCallBack callBack){
        changeFragmentCallBack = callBack;
    }
    public static void doChangeFragment(){
        if(changeFragmentCallBack != null) {
            changeFragmentCallBack.changeFragment();
        }
    }
    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();

        }

    }

    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context  Context 使用CopyFiles类的Activity
     * @param fileName String  文件名
     * @param filePath String  /xxx/xxx
     */
    public static void copyFilesFassets(Context context, String fileName, String filePath) {
        InputStream inputStream;
        try {
            inputStream = context.getResources().getAssets().open(fileName);// assets文件夹下的文件
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(filePath + "/" + fileName);// 保存到本地的文件夹下的文件
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getAndroiodScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）

        return width;
    }

    public static int getAndroiodScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）

        return height;
    }

    /**
     * 判断当前进程是否是本程序
     *
     * @param context
     * @return true表示当前进程为本程序，false表示当前进程非本程序
     */
    public static boolean isRunningForeground(Context context) {
        if (context == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        if (appProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
                if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind); //調用ThumbnailUtils類的靜態方法createVideoThumbnail獲取視頻的截圖；
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);//調用ThumbnailUtils類的靜態方法extractThumbnail將原圖片（即上方截取的圖片）轉化為指定大小；
        }
        return bitmap;
    }

    /**
     * 判断是否安装了某个应用
     *
     * @param context
     * @param packName 包名
     * @return t
     */
    public static boolean isInstallApp(Context context, String packName) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName.toLowerCase(Locale.ENGLISH);
                if (pn.equals(packName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断该字符串是否为空 / 空的null
     *
     * @param str
     * @return
     */
    public static String parseStr(String str) {
        if (null == str || "null".equals(str) || "NULL".equals(str)) {
            return "";
        } else {
            return str;
        }
    }

}
