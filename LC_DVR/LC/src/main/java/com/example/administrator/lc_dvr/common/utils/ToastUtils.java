package com.example.administrator.lc_dvr.common.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.administrator.lc_dvr.R;
import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * Created by zch on 2018/6/14.
 */

public class ToastUtils {

    /**
     * 正常短提示
     * @param context
     * @param string
     */
    public static void showNomalShortToast(Context context,String string) {
        if(context != null) {
            Toast.makeText(context, string,Toast.LENGTH_SHORT).show();
        }
    }

}
