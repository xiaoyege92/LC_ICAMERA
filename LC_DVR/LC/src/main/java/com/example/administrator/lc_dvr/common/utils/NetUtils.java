package com.example.administrator.lc_dvr.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zch on 2018/6/14.
 */

public class NetUtils {



    /**
     * 判断是否联网
     * @param context
     * @return true有网   false没网
     */
    public static boolean isNetworkConnected(Context context) {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            //mNetworkInfo.isAvailable();
            return true;//有网
        }else{
            return false;//没有网
        }
    }

    /**
     * 获取当前网络连接状态
     * @param context
     * @return
     */
    public static boolean getNetworkState(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 接口调用请求头
     *
     * @return
     */
    public static Map getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(Config.HEADER_ACCEPT_KEY, Config.HEADER_ACCEPT_VALUE);
        headers.put(Config.HEADER_CONTENT_KEY, Config.HEADER_CONTENT_VALUE);
        //上传授权码
        headers.put(Config.HEADER_TOKENID_KEY, PreferenceUtil.getString("tokenid", ""));

        return headers;
    }
}

