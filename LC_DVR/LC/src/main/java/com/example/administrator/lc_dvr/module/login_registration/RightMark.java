package com.example.administrator.lc_dvr.module.login_registration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.example.administrator.lc_dvr.MainActivity;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.base.BaseActivity;
import com.example.administrator.lc_dvr.common.customview.RightMarkView;

import org.videolan.libvlc.VLCApplication;

import io.vov.vitamio.utils.Log;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/01
 *   desc   :
 *  version :
 * </pre>
 */
public class RightMark extends Activity{

    private RightMarkView rightMarkView;

    private String selectIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.right_mark_activity);

        rightMarkView = (RightMarkView) findViewById(R.id.rightMarkView);

        Intent intent = getIntent();
        selectIndex = intent.getStringExtra("selectIndex");
        Log.e("right selectIndex:"+selectIndex);
        rightMarkView.setColor(ContextCompat.getColor(this,R.color.right_mark_size), ContextCompat.getColor(this,R.color.right_mark_size));
        rightMarkView.setStrokeWidth(7f);

        rightMarkView.startAnimator();

        new MyAsyncTask().execute();
    }

    private class MyAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            finish();
            //进入主界面
            Intent intent = new Intent(RightMark.this, MainActivity.class);
            intent.putExtra("selectIndex", selectIndex);
            Log.e("right SELECTINDEX:"+selectIndex);
            startActivity(intent);

            //移除所有的activity
            VLCApplication.removeALLActivity();
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}
