package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.load_video_thumbnail;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyVideoThumbLoader {
    private LruCache<String, Bitmap> lruCache;

    @SuppressLint("NewApi")
    public MyVideoThumbLoader() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();//��ȡ���������ڴ�
        int maxSize = maxMemory / 4;
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //�����������ÿ�δ��뻺���ʱ�����
                return value.getByteCount();
            }
        };
    }

    public void addVideoThumbToCache(String path, Bitmap bitmap) {
        if (getVideoThumbToCache(path) == null) {
            //��ǰ��ַû�л���ʱ�������
            lruCache.put(path, bitmap);
        }
    }

    public Bitmap getVideoThumbToCache(String path) {

        return lruCache.get(path);

    }

    public void showThumbByAsynctack(String path, ImageView imgview, String videoName) {

        if (getVideoThumbToCache(path) == null) {
            if (new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + videoName + ".png").exists()) {
                ImageLoader.getInstance().displayImage("file://" + BitmapUtils.getSDPath() + "/VOC/Cache/" + videoName + ".png", imgview);
            } else {
                //�첽����
                new MyBobAsynctack(imgview, path, videoName).execute(path);
            }
        } else {
            imgview.setImageBitmap(getVideoThumbToCache(path));
        }

    }

    /**
     * 保存bitmap到SD卡
     *
     * @param bmp
     * @param bitName
     * @return
     * @throws IOException
     */
    public boolean saveMyBitmap(Bitmap bmp, String bitName) throws IOException {
        File dirFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File f = new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + bitName + ".png");
        boolean flag = false;
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
            flag = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }


    class MyBobAsynctack extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private String path;
        private String videoName;

        public MyBobAsynctack(ImageView imageView, String path, String videoName) {
            this.imgView = imageView;
            this.path = path;
            this.videoName = videoName;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = VideoUtil.createVideoThumbnail(params[0], 70, 50, MediaStore.Video.Thumbnails.MICRO_KIND);
            //���뻺����
            if (getVideoThumbToCache(params[0]) == null) {
                if (path != null && bitmap != null) {
                    addVideoThumbToCache(path, bitmap);
                    try {
                        saveMyBitmap(bitmap, videoName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imgView.getTag().equals(path)) {
                if (bitmap != null) {
                    imgView.setImageBitmap(bitmap);
                }
            }
        }
    }

}
