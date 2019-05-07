package com.example.administrator.lc_dvr.common.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ViewHolder {
    private final SparseArray<View> mViews;

    private  int mPosition;

    private View mConvertView;
    private ImageView videoImage;

    private ViewHolder(Context context, ViewGroup parent, int layoutId,
                       int position) {
        this.mPosition = position;
        this.mViews = new SparseArray<View>();
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        // setTag
        mConvertView.setTag(this);
    }

    /**
     * 拿到一个ViewHolder对象
     *
     * @param context
     * @param convertView
     * @param parent
     * @param layoutId
     * @param position
     * @return
     */
    public static ViewHolder get(Context context, View convertView,
                                 ViewGroup parent, int layoutId, int position) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder(context, parent, layoutId, position);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
//        viewHolder = new ViewHolder(context, parent, layoutId, position);
        return viewHolder;
    }

    public View getConvertView() {
        return mConvertView;
    }

    /**
     * 通过控件的Id获取对于的控件，如果没有则加入views
     *
     * @param viewId
     * @return
     */
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param string
     * @return
     */
    public ViewHolder setText(int viewId, String string) {
        TextView view = getView(viewId);
        view.setText(string);
        return this;
    }

    public void setStar(int viewId, String rating) {
        RatingBar rb = getView(viewId);
        float i = Float.parseFloat(rating);
        rb.setRating(i / 2);
    }

    public void setState(int viewId, boolean flag) {
        CheckBox checkBox = getView(viewId);
        checkBox.setChecked(flag);
    }

    /**
     * 获取edit文本
     *
     * @param viewId
     * @return
     */
    public String getEditText(int viewId) {
        EditText ed = getView(viewId);
        String str = ed.getText().toString();
        return str;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public ViewHolder setImageResource(int viewId, int drawableId) {
        ImageView view = getView(viewId);
        view.setImageResource(drawableId);
        return this;
    }

    public ViewHolder showImageView(int viewId) {
        ImageView view = getView(viewId);
        view.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @return
     */
    public ViewHolder setImageBitmap(int viewId, Bitmap bm) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }

    /**
     * 设置网络视频缩略图
     *
     * @param viewId
     * @param url
     * @param context
     * @return
     */
    public ViewHolder setVideoImageByUrl(int viewId, String url, Context context) {

        videoImage = getView(viewId);
        videoImage.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.nophotos));
        new DownTask().execute(url);
        return this;
    }

    /**
     * 保存bitmap到SD卡
     *
     * @param bmp
     * @param bitName
     * @return
     * @throws IOException
     */
    public void saveMyBitmap(final Bitmap bmp, final String bitName) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (bmp != null) {
                    File dirFile = new File(BitmapUtils.getSDPath() + "/VOC/Cache/");
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    File f = new File(BitmapUtils.getSDPath() + "/VOC/Cache/" + bitName + ".png");
                    if (f.exists()) {
                        f.delete();
                    }
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    FileOutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(f);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
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
                }
            }
        });
        thread.start();
    }

    /**
     * 异步任务获取网络视频缩略图
     *
     */
    public class DownTask extends AsyncTask<String, Void, Bitmap> {
        //上面的方法中，第一个参数：网络图片的路径，第二个参数的包装类：进度的刻度，第三个参数：任务执行的返回结果
        @Override
        //在界面上显示进度条
        protected void onPreExecute() {
        }

        protected Bitmap doInBackground(String... params) {  //三个点，代表可变参数

            Bitmap bitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            int kind = MediaStore.Video.Thumbnails.MINI_KIND;
            try {
                if (Build.VERSION.SDK_INT >= 14) {
                    retriever.setDataSource(params[0], new HashMap<String, String>());
                } else {
                    retriever.setDataSource(params[0]);
                }
                bitmap = retriever.getFrameAtTime();
            } catch (IllegalArgumentException ex) {
                // Assume this is a corrupt video file
            } catch (RuntimeException ex) {
                // Assume this is a corrupt video file.
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException ex) {
                    // Ignore failures while cleaning up.
                }
            }
            if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, 90, 58, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            }

            //缓存图片
            try {
                saveMyBitmap(bitmap, params[0].substring(57));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;

        }

        //主要是更新UI
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            videoImage.setImageBitmap(result);//更新UI
        }
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @return
     */
    public ViewHolder setImageByUrl(int viewId, String url, Context context) {
        ImageView iv = getView(viewId);
        ImageLoader.getInstance().displayImage(url, iv);
        return this;
    }

    /**
     * 为ImageView设置图片2
     *
     * @param viewId
     * @return
     */
    public ViewHolder setImageByUrl2(int viewId, String url, Context context) {
        ImageView iv = getView(viewId);
        DisplayImageOptions defaultDisplayImageOptions = new DisplayImageOptions.Builder() //
                .showImageOnLoading(R.drawable.ic_file_default_video)
                .showImageOnFail(R.drawable.ic_file_default_video)
                .showImageForEmptyUri(R.drawable.ic_file_default_video)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)//设置下载的图片下载前是否重置，复位
                .cacheInMemory(true)//设置下载图片是否缓存到内存
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片解码类型
                .displayer(new FadeInBitmapDisplayer(300))//设置用户加载图片的task(这里是渐现)
                .build();
        ImageLoader.getInstance().displayImage(url, iv, defaultDisplayImageOptions);
        return this;
    }

    /**
     * 给view设置背景色
     *
     * @param viewId
     * @param color
     * @return
     */
    public ViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    public int getPosition() {
        return mPosition;
    }

}
