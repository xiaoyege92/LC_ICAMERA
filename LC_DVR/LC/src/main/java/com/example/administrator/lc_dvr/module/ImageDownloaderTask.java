package com.example.administrator.lc_dvr.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/09/11
 *   desc   :
 *  version :
 * </pre>
 */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

    private final WeakReference<ImageView> imageViewReference;

    public ImageDownloaderTask(ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        Utils.checkLocalFolder();

        return downloadBitmap(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.ic_file_default_video);
                    imageView.setImageDrawable(placeholder);
                }
            }

        }
    }

    private Bitmap downloadBitmap(String url, String name) {

        File file = new File(Utils.local_thumbnail_path, name);
        OutputStream fOut = null;

        if (checkThumbnailGetAble(url)) {

            HttpURLConnection urlConnection = null;
            try {
                URL uri = new URL(url);
                urlConnection = (HttpURLConnection) uri.openConnection();

                int statusCode = urlConnection.getResponseCode();
                if (statusCode != HttpStatus.SC_OK) {
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    fOut = new FileOutputStream(file);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();

                    return bitmap;
                }
            } catch (Exception e) {
                urlConnection.disconnect();
                Log.e("ImageDownloader", "Error downloading image from " + url);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        else {
            HttpGet httpget = new HttpGet(url  + "?custom=1&cmd=4001");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;

            try {
                response = client.execute(httpget);
                HttpEntity resEntityGet = response.getEntity();
                InputStream instream = resEntityGet.getContent();

                fOut = new FileOutputStream(file);

                if (instream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(instream);
                    if(bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    }
                    fOut.flush();
                    fOut.close();
                    return bitmap;
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;
    }

    private boolean checkThumbnailGetAble(String url) {
        HttpGet httpget1 = new HttpGet(url  + "?custom=1&cmd=4001");
        HttpClient client1 = new DefaultHttpClient();
        HttpResponse response1;
        boolean is = false;
        try {
            response1 = client1.execute(httpget1);
            HttpEntity resEntityGet1 = response1.getEntity();
            is =  EntityUtils.toString(resEntityGet1, "UTF-8").contains("xml");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return is;
    }
}