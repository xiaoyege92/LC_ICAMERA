package com.varma.android.aws.webserver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.varma.android.aws.constants.Constants;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class WebServer extends Thread {
	private static final String SERVER_NAME = "AndWebServer";
	private static final String ALL_PATTERN = "*";
//	private static final String MESSAGE_PATTERN = "/message*";
//  private static final String FOLDER_PATTERN = "/dir*";
	
	private boolean isRunning = false;
	private Context context = null;
	private int serverPort = 0;

	private BasicHttpProcessor httpproc = null;
	private BasicHttpContext httpContext = null;
	private HttpService httpService = null;
	private HttpRequestHandlerRegistry registry = null;
	private NotificationManager notifyManager = null;
	private ArrayList<SingleServer> webServers = new ArrayList<SingleServer>();
	private int serverId=0;
	
	public WebServer(Context context, NotificationManager notifyManager){
		super(SERVER_NAME);
		
		this.setContext(context);
		this.setNotifyManager(notifyManager);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		
		serverPort = Integer.parseInt(pref.getString(Constants.PREF_SERVER_PORT, "" + Constants.DEFAULT_SERVER_PORT));
		httpproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();
		
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());

        httpService = new HttpService(httpproc,
        									new DefaultConnectionReuseStrategy(),
        									new DefaultHttpResponseFactory());

		
        registry = new HttpRequestHandlerRegistry();
        
        registry.register(ALL_PATTERN, new ProgressiveStreamHandler(context));
        
        httpService.setHandlerResolver(registry);
	}
	
	@Override
	public void run() {
		super.run();
		
		try {
			ServerSocket serverSocket = new ServerSocket(serverPort);
			serverSocket.setReuseAddress(true);
			while(isRunning) {
				//while (!isInterrupted())
				{
					if(serverSocket==null){
						return;
					}
					SingleServer singleServer = new SingleServer(serverSocket.accept(), ++serverId);
					for(SingleServer tempServer : webServers)	{
						if(!tempServer.getServerState())	{
							tempServer.stopThread();
							while(true)	{
								if(tempServer.getServerState())	{
									break;
								}
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							webServers.remove(tempServer);
							break;
						}
						else	{
							webServers.remove(tempServer);
							break;
						}
					}
					webServers.add(singleServer);
					singleServer.start();
				}
			}
			serverSocket.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void startThread() {
		isRunning = true;
		try {
			super.start();
		}catch (Exception e){
			e.printStackTrace();
		}

	}
	
	public synchronized void stopThread(){
		isRunning = false;
	}

	public void setNotifyManager(NotificationManager notifyManager) {
		this.notifyManager = notifyManager;
	}

	public NotificationManager getNotifyManager() {
		return notifyManager;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public class SingleServer extends Thread {
		private Socket socket;
		private int serverId;
		private boolean serverProcessDone = true;
		public SingleServer(Socket socket, int serverId)	{
			this.socket = socket;
			this.serverId = serverId;
		}

		public void run()	{
			try {
				serverProcessDone = false;
				DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
				serverConnection.bind(socket, new BasicHttpParams());
				httpService.handleRequest(serverConnection, httpContext);
				serverConnection.shutdown();
				serverProcessDone = true;
			} catch (IOException e) {
				serverProcessDone = true;
				e.printStackTrace();
			} catch (HttpException e) {
				serverProcessDone = true;
				e.printStackTrace();
			}catch (Exception e) {
				serverProcessDone = true;
				e.printStackTrace();
			}
		}

		public void stopThread()	{
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public boolean getServerState()	{
			return this.serverProcessDone;
		}
	}
}



