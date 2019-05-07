package com.dxing.udriver;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dxing.wifi.api.DxtWiFi;
import com.dxing.wifi.api.UdriverFileInfo;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.BitmapUtils;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.WaveProgressView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UdriverFileListViewAdapter extends ArrayAdapter<UdriverFileInfo> implements WiFiSDConfiguration {

    private final Context context;
    private final List<UdriverFileInfo> fileList;
    static final LinkedHashMap<UdriverFileInfo, Bitmap> fsToBmp = new LinkedHashMap<UdriverFileInfo, Bitmap>();
    private Handler sizeHandler;
    private OnRightDownloadClickListener downloadClickListener;
    private OnRightDeleteClickListener deleteClickListener;

    public HashMap<Integer, Boolean> timeStateMap;
    public static int isDownloading = -1;

    public interface OnRightDownloadClickListener {
        void onDownloadClick(int position);
    }

    public interface OnRightDeleteClickListener {
        void onDeleteClick(ViewHolder viewHolder, int position);
    }

    public UdriverFileListViewAdapter(Context context, List<UdriverFileInfo> fileList, OnRightDownloadClickListener downloadClickListener, OnRightDeleteClickListener deleteClickListener) {
        super(context, R.layout.file_list, fileList);
        this.context = context;
        this.fileList = fileList;
        this.downloadClickListener = downloadClickListener;
        this.deleteClickListener = deleteClickListener;

//        getLoadVideoName = new GetLoadVideoName();
        //判断是否要显示时间轴
        timeStateMap = new HashMap<Integer, Boolean>();

        //displaymetrics = DxtWiFi.sdCard.getDisplayMetrics();
        sizeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                double size = msg.arg1 / (1024.0);
                ((TextView) msg.obj).setText(new DecimalFormat("#.##").format(size) + "MB");
            }
        };
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final UdriverFileInfo udriverFileInfo = fileList.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.file_list, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.videoName = (TextView) convertView.findViewById(R.id.video_name);
            viewHolder.size = (TextView) convertView.findViewById(R.id.video_size);
            viewHolder.videoHour = (TextView) convertView.findViewById(R.id.video_hour);
            viewHolder.video_date = (TextView) convertView.findViewById(R.id.video_date);
            viewHolder.video_time1 = (TextView) convertView.findViewById(R.id.video_time1);
            viewHolder.video_time2 = (TextView) convertView.findViewById(R.id.video_time2);
            viewHolder.video_year = (TextView) convertView.findViewById(R.id.video_year);
            viewHolder.video_month = (TextView) convertView.findViewById(R.id.video_month);
            viewHolder.video_day = (TextView) convertView.findViewById(R.id.video_day);
            viewHolder.video_data = convertView.findViewById(R.id.video_data);
            viewHolder.video_data_seat = convertView.findViewById(R.id.video_data_seat);
            viewHolder.download_action = (LinearLayout) convertView.findViewById(R.id.download_action);
            viewHolder.video_download_progress = (TextView) convertView.findViewById(R.id.video_download_progress);
            viewHolder.cancel_video_download = (ImageView) convertView.findViewById(R.id.cancel_video_download);
            viewHolder.wave_progress = (WaveProgressView) convertView.findViewById(R.id.wave_progress);

            viewHolder.leftThumbnailImage = (ImageView) convertView.findViewById(R.id.video_img);
            viewHolder.leftFilePathTextView = (TextView) convertView.findViewById(R.id.filepath);
            viewHolder.leftFileDateTextView = (TextView) convertView.findViewById(R.id.filedate);
            viewHolder.leftFileLengthTextView = (TextView) convertView.findViewById(R.id.filesize);

            viewHolder.leftFilePlayTimeTextView = (TextView) convertView.findViewById(R.id.filePlayTime);
            viewHolder.rightDownloadImage = (ImageView) convertView.findViewById(R.id.download_image);
            viewHolder.rightDeleteImage = (ImageView) convertView.findViewById(R.id.delete_image);
            viewHolder.leftDownloadProgressBar = (ProgressBar) convertView.findViewById(R.id.download_progressBar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (udriverFileInfo.getFileLength() > 0) {
            viewHolder.rightDownloadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (downloadClickListener != null) {
                        downloadClickListener.onDownloadClick(position);
                    }
                }
            });
        } else {
            viewHolder.rightDownloadImage.setVisibility(View.GONE);
        }

        viewHolder.rightDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(viewHolder, position);
                }
            }
        });

        // 文件名
        viewHolder.videoName.setText(udriverFileInfo.getFileName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        Date resultDate = new Date(udriverFileInfo.getFileCreateDate());
        String Time = simpleDateFormat.format(resultDate);

        viewHolder.videoHour.setTag(udriverFileInfo.getFilePath()+"0");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(udriverFileInfo.getFileCreateDate());

        viewHolder.video_date.setText(dateFormat.format(date));

        if (isDownloading == position) {
            viewHolder.download_action.setVisibility(View.VISIBLE);
        } else {
            viewHolder.download_action.setVisibility(View.GONE);
        }
        viewHolder.wave_progress.setTag(udriverFileInfo.getFilePath()+"01");
        viewHolder.video_download_progress.setTag(udriverFileInfo.getFilePath()+"02");

        if(isDownloading != -1) {
            viewHolder.wave_progress.setProgress((int) (((double) (MainMenuFragment.proPosition) / (double) (MainMenuFragment.downloadLen)) * 100));
            viewHolder.wave_progress.hideProgressText(true);
            viewHolder.video_download_progress.setText((int) (((double) (MainMenuFragment.proPosition) / (double) (MainMenuFragment.downloadLen)) * 100) + "%");
        }


        if (timeStateMap.get(position) != null && timeStateMap.get(position)) {
            viewHolder.video_year.setText(Time.substring(0, 4));
            viewHolder.video_month.setText(Time.substring(5, 7));
            viewHolder.video_day.setText(Time.substring(8, 10));
            viewHolder.video_data_seat.setVisibility(View.GONE);
            viewHolder.video_data.setVisibility(View.VISIBLE);
            viewHolder.video_time1.setTextColor(ContextCompat.getColor(context, R.color.orange));
            viewHolder.video_time1.setText(Time.substring(11, 13));
            viewHolder.video_time2.setTextColor(ContextCompat.getColor(context, R.color.orange));

        } else {
            viewHolder.video_data_seat.setVisibility(View.VISIBLE);
            viewHolder.video_data.setVisibility(View.GONE);
            viewHolder.video_time1.setTextColor(ContextCompat.getColor(context, R.color.white));
//			viewHolder.video_time1.setText(Time.substring(14,16));
            viewHolder.video_time2.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        if (udriverFileInfo.isPlayTimeValid()) {
            int time = udriverFileInfo.getFilePlayTime();
            viewHolder.videoHour.setText(sec2time(time));
        }
        int rootPos = udriverFileInfo.getFilePath().indexOf("/");
        int lastPos = udriverFileInfo.getFilePath().lastIndexOf("/");
        if (rootPos == lastPos) {
            //root
            viewHolder.leftFilePathTextView.setText("path : /");
        } else {
            viewHolder.leftFilePathTextView.setText("path : " + udriverFileInfo.getFilePath().substring(rootPos + 1, lastPos - rootPos + 1));
        }

        final long lastWriteDateTime = DxtWiFi.sdCard.getLastWriteDateTime(true);

        Thread sizeThread = new Thread() {
            public void run() {
                double size = udriverFileInfo.getFileLength() / (1024.0);
                long time = udriverFileInfo.getFileCreateDate();

                if (time >= lastWriteDateTime) {
                    //is new file
                } else {
                    //not new file
                }
                Message msg = sizeHandler.obtainMessage(0, (int) size, 0, viewHolder.size);
                sizeHandler.sendMessage(msg);
            }
        };
        sizeThread.start();

        viewHolder.leftThumbnailImage.setTag(udriverFileInfo.getFilePath());
        Bitmap bmp = fsToBmp.get(udriverFileInfo);
        if (bmp == null) {
            switch (MainMenuFragment.getFileType(udriverFileInfo.getFileName())) {
                case PAGE_PHOTO:
                    if (Udriver.udriver.isSupportedRAWType(udriverFileInfo.getFileName())) {
                        viewHolder.leftThumbnailImage.setImageResource(R.drawable.browse_btn_raw);
                    } else {
                        //Udriver.udriver.getJpegThumbnail(udriverFileInfo);
                    }
                    break;
                case PAGE_MUSIC:
                    viewHolder.leftThumbnailImage.setImageResource(R.drawable.browse_btn2);
                    break;
                case PAGE_VIDEO:
                    viewHolder.leftThumbnailImage.setImageResource(R.drawable.browse_btn3);
                    if (Udriver.udriver.isSupportedTsVideo(udriverFileInfo.getFileName())) {
                        Udriver.udriver.getTsPlayTime_Thumbnail(udriverFileInfo);
                    }
                    break;
                case PAGE_MISC:
                    viewHolder.leftThumbnailImage.setImageResource(R.drawable.browse_btn5);
                    break;
            }
        } else {
            viewHolder.leftThumbnailImage.setImageBitmap(bmp);
        }

        return convertView;
    }

    void putBitmap(UdriverFileInfo udriverFileInfo, Bitmap bmp) {
        fsToBmp.put(udriverFileInfo, bmp);
        if (fsToBmp.size() > MAX_CACHE_THUMB) {
            for (Map.Entry<UdriverFileInfo, Bitmap> entry : fsToBmp.entrySet()) {
                fsToBmp.entrySet().remove(entry);
                break;
            }
        }
    }

    public void saveBitmap(UdriverFileInfo udriverFileInfo) {
        if (fsToBmp.get(udriverFileInfo) != null) {
            saveImage(fsToBmp.get(udriverFileInfo), udriverFileInfo.getFileName());
        }
    }

    private void saveImage(Bitmap bmp, String filename) {

        File appDir = new File(BitmapUtils.getSDPath(), "/VOC/Cache/");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = filename + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 秒转化为常见格式
     *
     * @param time
     * @return
     */
    private String sec2time(long time) {
        int totalSeconds = (int) time;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public class ViewHolder {

        TextView videoName;
        TextView size;
        TextView videoHour;
        //        TextView videoMinute;
        ImageView leftThumbnailImage;
        TextView video_time1;
        TextView video_time2;
        TextView video_year;
        TextView video_month;
        TextView video_day;
        View video_data_seat;
        View video_data;
        LinearLayout download_action;
        TextView video_download_progress;
        ImageView cancel_video_download;
        WaveProgressView wave_progress;
        TextView video_date;
        //		TextView leftFilenameTextView;
        TextView leftFilePathTextView;
        TextView leftFileDateTextView;
        TextView leftFileLengthTextView;
        TextView leftFilePlayTimeTextView;
        ProgressBar leftDownloadProgressBar;

        ImageView rightDownloadImage;
        ImageView rightDeleteImage;

    }

}
