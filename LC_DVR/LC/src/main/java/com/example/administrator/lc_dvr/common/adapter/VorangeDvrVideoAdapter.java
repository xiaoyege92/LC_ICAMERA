package com.example.administrator.lc_dvr.common.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.BaseSwipListAdapter;
import com.example.administrator.lc_dvr.R;
import com.example.administrator.lc_dvr.common.utils.Utils;
import com.example.administrator.lc_dvr.module.ImageDownloaderTask;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.VorangeDvrVideo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.WaveProgressView;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.OneVideoListInfo;
import com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean.VideoListInfo;

import org.videolan.libvlc.VLCApplication;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/09/06
 *   desc   :
 *  version :
 * </pre>
 */
public class VorangeDvrVideoAdapter extends BaseSwipListAdapter {

    private final Context context;
    private final ArrayList<Object> fileList;
    public boolean isLeftSlip = false;
    private String thumbnail_url;//缩略图的url

    public static HashMap<Integer, Boolean> timeStateMap;
    public static int isDownloading = -1;
    private String video_hour;
    private String video_minute;
    private String videoName;
    private int size;

    public VorangeDvrVideoAdapter(@NonNull Context context, int resource, ArrayList<Object> fileList) {

        this.context = context;
        this.fileList = fileList;
        timeStateMap = new HashMap<Integer, Boolean>();
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 在这个回调方法设置那一项不能左滑
     *
     * @param position
     * @return
     */
    @Override
    public boolean getSwipEnableByPosition(int position) {
        //判断是否要禁止dvr列表的左滑
        if (isLeftSlip) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;


        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
            viewHolder = new VorangeDvrVideoAdapter.ViewHolder();

            viewHolder.videoName = (TextView) convertView.findViewById(R.id.video_name);
            viewHolder.size = (TextView) convertView.findViewById(R.id.video_size);
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VorangeDvrVideoAdapter.ViewHolder) convertView.getTag();
        }

        if (VLCApplication.noOneFile) {
            final VideoListInfo.LISTBean.ALLFileBean.FileBean fileBean = (VideoListInfo.LISTBean.ALLFileBean.FileBean) fileList.get(position);

            videoName = fileBean.getNAME();
            size = fileBean.getSIZE();
            video_hour = videoName.substring(12, 13);
            video_minute = videoName.substring(15, 16);
            viewHolder.video_date.setText(fileBean.getTIME());

            String thumbnail_url = "http://192.168.1.254/CARDV/MOVIE/" + videoName + "?custom=1&cmd=4001";

            File f = new File(Utils.local_thumbnail_path + "/" + videoName);

            if (f.exists() == false) {
                new ImageDownloaderTask(viewHolder.leftThumbnailImage).execute(thumbnail_url, videoName);
            } else {
                Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                viewHolder.leftThumbnailImage.setImageBitmap(myBitmap);
            }

            //   2018/10/15 10:54:49
            if (timeStateMap.get(position)) {
                viewHolder.video_year.setText(fileBean.getTIME().substring(0, 4));
                viewHolder.video_month.setText(fileBean.getTIME().substring(5, 7));
                viewHolder.video_day.setText(fileBean.getTIME().substring(8, 10));
                viewHolder.video_data_seat.setVisibility(View.GONE);
                viewHolder.video_data.setVisibility(View.VISIBLE);
                viewHolder.video_time1.setTextColor(context.getResources().getColor(R.color.orange));
                viewHolder.video_time1.setText(fileBean.getTIME().substring(11, 13));

                viewHolder.video_time2.setTextColor(context.getResources().getColor(R.color.orange));
            } else {
                viewHolder.video_data_seat.setVisibility(View.VISIBLE);
                viewHolder.video_data.setVisibility(View.GONE);
                viewHolder.video_time1.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.video_time2.setTextColor(context.getResources().getColor(R.color.white));
            }
        } else {
            final OneVideoListInfo.LISTBean.ALLFileBean.FileBean fileBean = (OneVideoListInfo.LISTBean.ALLFileBean.FileBean) VLCApplication.dvrVideoList.get(position);
            videoName = fileBean.getNAME();
            size = fileBean.getSIZE();
            video_hour = videoName.substring(12, 13);
            video_minute = videoName.substring(15, 16);
            viewHolder.video_date.setText(fileBean.getTIME());

            String thumbnail_url = "http://192.168.1.254/CARDV/MOVIE/" + videoName + "?custom=1&cmd=4001";
            File f = new File(Utils.local_thumbnail_path + "/" + videoName);

            if (f.exists() == false) {
                new ImageDownloaderTask(viewHolder.leftThumbnailImage).execute(thumbnail_url, videoName);
            } else {
                Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                viewHolder.leftThumbnailImage.setImageBitmap(myBitmap);
            }
            // fileBean.getTIME()  2018/10/15 10:54:49
            if (timeStateMap.get(position)) {
                viewHolder.video_year.setText(fileBean.getTIME().substring(0, 4));
                viewHolder.video_month.setText(fileBean.getTIME().substring(5, 7));
                viewHolder.video_day.setText(fileBean.getTIME().substring(8, 10));
                viewHolder.video_data_seat.setVisibility(View.GONE);
                viewHolder.video_data.setVisibility(View.VISIBLE);
                viewHolder.video_time1.setTextColor(context.getResources().getColor(R.color.orange));
                viewHolder.video_time1.setText(fileBean.getTIME().substring(11, 13));

                viewHolder.video_time2.setTextColor(context.getResources().getColor(R.color.orange));
            } else {
                viewHolder.video_data_seat.setVisibility(View.VISIBLE);
                viewHolder.video_data.setVisibility(View.GONE);
                viewHolder.video_time1.setTextColor(context.getResources().getColor(R.color.white));
                viewHolder.video_time2.setTextColor(context.getResources().getColor(R.color.white));
            }
        }

        viewHolder.videoName.setText(videoName);
        double videoSize = (double) (size / 1024) / 1024;
        BigDecimal bigDecimal = new BigDecimal(videoSize);
        double value = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        viewHolder.size.setText(value + "M");

        if (isDownloading == position) {
            viewHolder.download_action.setVisibility(View.VISIBLE);
            //显示下载进度
            viewHolder.wave_progress.setProgress((int) (((double) (VorangeDvrVideo.downloadCurrent) / (double) (VorangeDvrVideo.downloadTotal)) * 100));
            viewHolder.wave_progress.hideProgressText(true);
            viewHolder.video_download_progress.setText((int) (((double) (VorangeDvrVideo.downloadCurrent) / (double) (VorangeDvrVideo.downloadTotal)) * 100) + "%");
        } else {
            viewHolder.download_action.setVisibility(View.GONE);
        }


        viewHolder.download_action.setTag(videoName + "action");
        viewHolder.wave_progress.setTag(videoName + "progress");
        viewHolder.video_download_progress.setTag(videoName + "download_progress");
        viewHolder.cancel_video_download.setTag(videoName + "download");

        return convertView;
    }

    public class ViewHolder {

        TextView videoName;
        TextView size;
//        TextView videoHour;
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

}
