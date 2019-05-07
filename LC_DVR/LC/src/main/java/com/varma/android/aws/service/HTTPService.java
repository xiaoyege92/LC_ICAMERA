package com.varma.android.aws.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.varma.android.aws.webserver.WebServer;

public class HTTPService extends Service {
	
	public static boolean serviceRunning = false;
	public static Service instance;
	
	private static final int NOTIFICATION_STARTED_ID = 1;
	
	private NotificationManager notifyManager = null;
	private WebServer server = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		server = new WebServer(this, notifyManager);
		instance = this;
	}

	@Override
	public void onDestroy() {
		server.stopThread();
		notifyManager.cancel(NOTIFICATION_STARTED_ID);
		
		notifyManager = null;
		
		serviceRunning = false;
		instance = null;
		
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		serviceRunning = true;
		server.startThread();
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
