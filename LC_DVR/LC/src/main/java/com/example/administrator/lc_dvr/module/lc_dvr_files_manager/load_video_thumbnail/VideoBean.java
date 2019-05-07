package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.load_video_thumbnail;

import android.graphics.Bitmap;

public class VideoBean {
	private String LocationPath;//��Ƶ����·��
	private String vidioName;//��Ƶ����
	private String fileParentVideo;//���ļ���
	private Bitmap bitmap;
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getLocationPath() {
		return LocationPath;
	}
	public void setLocationPath(String locationPath) {
		LocationPath = locationPath;
	}
	public String getVidioName() {
		return vidioName;
	}
	public void setVidioName(String vidioName) {
		this.vidioName = vidioName;
	}
	public String getFileParentVideo() {
		return fileParentVideo;
	}
	public void setFileParentVideo(String fileParentVideo) {
		this.fileParentVideo = fileParentVideo;
	}
}
