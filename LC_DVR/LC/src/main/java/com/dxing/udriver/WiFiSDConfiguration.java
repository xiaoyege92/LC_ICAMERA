package com.dxing.udriver;

public interface WiFiSDConfiguration {

	// Prefference keys
	public static final String PREF_LOCAL_PATH = "localPath";

	public static final String DEFAULT_LOCAL_PATH = "WSD";

	public static final int MAX_CACHE_THUMB = 100;

	public static final int PAGE_PHOTO = 1;
	public static final int PAGE_MUSIC = 2;
	public static final int PAGE_VIDEO = 3;
	public static final int PAGE_MISC = 4;

	public static final boolean GET_SD_CAPACITY = false;	//set to false, improve initial read performance
	public static final boolean GET_TS_THUMBNAIL = true;
	//public static final int TS_THUMBNAIL_PRELOAD_SIZE = 150000;
	public static final int TS_THUMBNAIL_PRELOAD_SIZE = 200000; //invalid from V1.08
	//public static final int TS_THUMBNAIL_PRELOAD_SIZE = 50000;
	public static final int GET_TS_THUMBNAIL_MODE_LOAD_FIX_SIZE = 1;    //invalid from V1.08
	public static final int GET_TS_THUMBNAIL_MODE_LOAD_FULL = 2;	//get thumbnail speed slow
	public static final int GET_TS_THUMBNAIL_MODE = GET_TS_THUMBNAIL_MODE_LOAD_FIX_SIZE;    //invalid from V1.08
	//public static final int GET_TS_THUMBNAIL_MODE = GET_TS_THUMBNAIL_MODE_LOAD_FULL;
}
