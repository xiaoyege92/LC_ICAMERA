package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.load_video_thumbnail;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;

/**
 * Created by Administrator on 2015/7/17.
 */
public class VideoUtil {
	public static Bitmap createVideoThumbnail(String vidioPath, int width,
			int height, int kind) {
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(vidioPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	public static ArrayList<VideoBean> scanVidoSdCord() {
		ArrayList<VideoBean> videoList = new ArrayList<VideoBean>();
		String strPath = Environment.getExternalStorageDirectory()
				+ File.separator + "phonetest1";// �ļ�·�����Զ���
		File rootFile = new File(strPath);
		File[] files = rootFile.listFiles();
		if (files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					File[] videoFiles = files[i].listFiles();
					if (videoFiles.length > 0) {// ��ͼƬ
						for (int j = 0; j < videoFiles.length; j++) {
							if (videoFiles[j].getName().endsWith(".mp4")
									|| videoFiles[j].getName().endsWith(".MP4")
									|| videoFiles[j].getName().endsWith(".3gp")
									|| videoFiles[j].getName().endsWith(".avi")) {
								VideoBean video = new VideoBean();
								video.setFileParentVideo(files[i].getName());
								video.setVidioName(videoFiles[j].getName());
								video.setLocationPath(videoFiles[j]
										.getAbsolutePath());
								videoList.add(video);
							}
						}
					}
				}
			}
		}
		return videoList;
	}
}
