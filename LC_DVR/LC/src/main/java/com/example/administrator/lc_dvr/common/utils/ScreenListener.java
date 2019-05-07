package com.example.administrator.lc_dvr.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/08/13
 *   desc   :
 *  version :
 * </pre>
 */
public class ScreenListener {
    private Context mContext;
    private ScreenBroadcastReceiver receiver;
    private ScreenStateListener mScreenStateListener;

    public ScreenListener(Context context) {
        mContext = context;
        receiver = new ScreenBroadcastReceiver();
    }

    public void register(ScreenStateListener screenStateListener) {
        if (screenStateListener != null) {
            mScreenStateListener = screenStateListener;
        }
        if (receiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            mContext.registerReceiver(receiver, filter);
        }
    }

    public void unregister() {
        if (receiver != null) {
            try{
                mContext.unregisterReceiver(receiver);
            }catch (Exception e){}

        }
    }


    private class ScreenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    if (mScreenStateListener != null) {
                        mScreenStateListener.onScreenOn();
                        Log.e("ScreenListener","........onScreenOn.......");
                    }
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    if (mScreenStateListener != null) {
                        mScreenStateListener.onScreenOff();
                        Log.e("ScreenListener","........onScreenOff.......");
                    }
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    if (mScreenStateListener != null) {
                        mScreenStateListener.onUserPresent();
                        Log.e("ScreenListener","........onUserPresent.......");
                    }
                }
            }
        }
    }

    public interface ScreenStateListener {
        void onScreenOn();

        void onScreenOff();

        void onUserPresent();
    }
}