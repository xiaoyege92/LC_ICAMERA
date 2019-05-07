package com.example.administrator.lc_dvr.module.lc_dvr_files_manager;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by yangboru on 2017/11/13.
 */

public class GetLoadVideoName {
    private LruCache<String, String> lruCache;

    @SuppressLint("NewApi")
    public GetLoadVideoName() {
        lruCache = new LruCache<String, String>(1 * 1024 * 1024);
    }

    public void addVideoThumbToCache(String path, String bitmap) {
        if (getVideoThumbToCache(path) == null) {
            lruCache.put(path, bitmap);
        }
    }

    public String getVideoThumbToCache(String path) {

        return lruCache.get(path);

    }

    public void showThumbByAsynctack(String path, TextView imgview) {

        if (getVideoThumbToCache(path) == null) {
            new GetLoadVideoName.MyBobAsynctack(imgview, path).execute(path);
        } else {
            long minute = Integer.parseInt(getVideoThumbToCache(path)) / 60000;
            long seconds = Integer.parseInt(getVideoThumbToCache(path)) % 60000;
            long second = Math.round((float) seconds / 1000);
            if(minute < 10 && second < 10) {
                imgview.setText( "0"+minute+":"+"0"+ second);
            }else if(minute < 10 && second > 9) {
                imgview.setText( "0" + minute+":" + second);
            }else if(minute > 9 && second < 10){
                imgview.setText(minute+":"+"0" + second);
            }else {
                imgview.setText( minute+":" + second);
            }
        }
    }
    public void showTimeByAsynctack(String path, TextView imgview) {

        if (getVideoThumbToCache(path) == null) {
            new GetLoadVideoName.MyBobAsynctack(imgview, path).execute(path);
        } else {
            long minute = Integer.parseInt(getVideoThumbToCache(path)) / 60000;
            long seconds = Integer.parseInt(getVideoThumbToCache(path)) % 60000;
            long second = Math.round((float) seconds / 1000);
            if(minute == 0 && second<10) {
                imgview.setText( "0"+minute+":"+"0"+ second);
            }else if(minute == 0 && second>9) {
                imgview.setText( "0" + minute+":" + second);
            }else if(minute > 0 && second<10){
                imgview.setText( "0"+minute+":" + second);
            }else{
                imgview.setText( minute+":" + second);
            }


        }
    }

    class MyBobAsynctack extends AsyncTask<String, Void, String> {
        private TextView imgView;
        private String path;

        public MyBobAsynctack(TextView imageView, String path) {
            this.imgView = imageView;
            this.path = path;
        }

        @Override
        protected String doInBackground(String... params) {
            MediaPlayer md = new MediaPlayer();
            md.reset();
            try {
                md.setDataSource(params[0]);
                md.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (getVideoThumbToCache(params[0]) == null) {
                if (path != null && md != null) {
                    addVideoThumbToCache(path, md.getDuration() + "");
                }
            }
            return md.getDuration() + "";
        }

        @Override
        protected void onPostExecute(String bitmap) {
            if (imgView.getTag().equals(path)) {
                if (bitmap != null) {
                    long minute = Integer.parseInt(bitmap) / 60000;
                    long seconds = Integer.parseInt(bitmap) % 60000;
                    long second = Math.round((float) seconds / 1000);
                    if(minute == 0 && second<10) {
                        imgView.setText( "0"+minute+":"+"0"+ second);
                    }else if(minute == 0 && second>9) {
                        imgView.setText( "0" + minute+":" + second);
                    }else if(minute > 0 && minute<10 && second > 9){
                        imgView.setText( "0"+minute+":" + second);
                    }else{
                        imgView.setText( minute+":" + second);
                    }
                }
            }
        }
    }
    public static String getRingDuring(String mUri){
        String duration=null;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();

        try {
            if (mUri != null) {
                HashMap<String, String> headers=null;
                if (headers == null) {
                    headers = new HashMap<String, String>();
                    headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
                }
                mmr.setDataSource(mUri, headers);
            }

            duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
        } finally {
            mmr.release();
        }
        return duration;
    }
}
