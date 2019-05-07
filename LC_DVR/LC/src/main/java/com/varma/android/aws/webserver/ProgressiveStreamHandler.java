package com.varma.android.aws.webserver;

import android.content.Context;
import android.os.Handler;

import com.dxing.wifi.api.DxtWiFi;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.OutputStream;

import de.waldheinz.fs.FsDirectoryEntry;

public class ProgressiveStreamHandler implements HttpRequestHandler {
	private Context context = null;
	
	public static boolean complete = false;
	
	public static FsDirectoryEntry processingFile;
	public static Handler processingHandler;
	
	public ProgressiveStreamHandler(Context context){
		this.context = context;
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) throws HttpException, IOException {
		
		final boolean isHttpHead = request.getRequestLine().getMethod().equalsIgnoreCase("head");

		long startPosition = 0;
		long endPosition = processingFile.getFile().getLength() - 1;
		Header rangeHearder = request.getFirstHeader("Range");
		if( rangeHearder != null && rangeHearder.getValue() != "") {
			String strRange = rangeHearder.getValue().toLowerCase();
			int pos1 = strRange.indexOf("bytes=");
			int pos2 = strRange.indexOf("-");
			int pos3 = strRange.length();
			if( pos1 != -1 && pos2 != -1) {
				try {
					startPosition = Long.parseLong(strRange.substring(pos1 + "bytes=".length(),pos2));
					if (pos3 > (pos2 + 1)) {
						endPosition	= Long.parseLong(strRange.substring(pos2 + "-".length(),pos3));
					}
				}
				catch( Exception e) {
					e.printStackTrace();
				}
			}
		}
		final long start = startPosition;
		long length_temp = endPosition - startPosition + 1;
		final long length = length_temp;

		String contentType = "video/mp4";
		
		String filename = processingFile.getName().toLowerCase();
		if( filename.endsWith("mp4") || filename.endsWith("m4v")) {
			contentType = "video/mp4";
		}
		else if( filename.endsWith("3gp") || filename.endsWith("3g2")) {
			contentType = "video/3gpp";
		}
		else if( filename.endsWith("mp3")) {
			contentType = "audio/mp3";
		}
		else if(filename.endsWith("ts"))	{
			//contentType = "application/octet-stream";
			contentType = "video/mp2t";
		}

		BasicHttpEntity entity = new BasicHttpEntity() {
    		public void writeTo(final OutputStream outstream) throws IOException {
    			try {
    				if(length > 0 && !isHttpHead) {
        				DxtWiFi.sdCard.writeFileTo(outstream, processingFile, start, length);
    				}
				}
				catch( Exception e) {
					e.printStackTrace();
				}
				
			}
		};

		if(rangeHearder != null) {
			String strValue;
			entity.setContentType(contentType);
			entity.setContentLength(length);
			strValue = "bytes " + start + "-" + endPosition + "/" + processingFile.getFile().getLength();
			response.addHeader("Content-Range",strValue);
			response.setStatusCode(206);
			response.setEntity(entity);
		}
		else	{
			entity.setContentLength(length);
			entity.setContentType(contentType);
			response.setEntity(entity);
		}
	}

}
