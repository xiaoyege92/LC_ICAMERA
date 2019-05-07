package com.dxing.udriver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDexApplication;

import com.dxing.wifi.api.DxtWiFi;
import com.dxing.wifi.api.UdriverFileInfo;
import com.dxing.wifi.debug.DXTdebug;
import com.example.administrator.lc_dvr.common.utils.PreferenceUtil;
import com.varma.android.aws.webserver.ProgressiveStreamHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.waldheinz.fs.FsDirectory;
import de.waldheinz.fs.FsDirectoryEntry;


/**
 * Created by shun on 2017/12/25.
 */

public class Udriver extends MultiDexApplication implements WiFiSDConfiguration {

    public static Udriver udriver;

    private static final String[] SUPPORTED_IMAGE_TYPE = {"JPG", "BMP", "PNG", "RAW"};
    private static final String[] SUPPORTED_MUSIC_TYPE = {"MP3", "WAV"};
    private static final String[] SUPPORTED_VIDEO_TYPE = {"AVI", "RMVB", "MKV", "FLV", "MOV", "MP4", "WMV", "M4V", "3GP", "3G2", "M2T", "MTS", "M2TS", "M2V", "TS", "264", "h264"}; // m2ts, m2v
    private static final String[] SUPPORTED_STREAMING_VIDEO = {"AVI", "RMVB", "MKV", "FLV", "MOV", "MP4", "WMV", "M4V", "3GP", "3G2", "M2T", "MTS", "M2TS", "M2V", "TS", "264", "h264"};
    private static final String[] SUPPORTED_MISC_TYPE = {"DOC", "DOCX", "TXT", "PDF", "PPT", "PPTX", "XLS", "XLSX"};
    public static final String[] SUPPORTED_IMAGE_RAW_TYPE = {"RAW", "NRW", "CR2", "CRW", "NEF", "SR2", "ARW", "RAF", "RW2", "RWL"};
    private static final String[] SUPPORTED_TS_VIDEO = {"TS"};

    FsDirectory rootDirectory;
    FsDirectory tempDirectory;

    Handler loginHandler;
    Handler directoryHandler;
    Handler fileEventHandler;
    private static List<FsDirectoryEntry> udriverFiles = new ArrayList<FsDirectoryEntry>();
    private ArrayList<UdriverFileInfo> udriverFileInfos = new ArrayList<UdriverFileInfo>();

    private ArrayList<LoginResultListener> loginResultListeners = new ArrayList<LoginResultListener>();
    private ArrayList<DirectoryListener> directoryListeners = new ArrayList<DirectoryListener>();
    private ArrayList<FileEventListener> fileEventListeners = new ArrayList<FileEventListener>();
    private ArrayList<FileListener> fileListeners = new ArrayList<FileListener>();
    private ArrayList<FormatListener> formatListeners = new ArrayList<FormatListener>();
    private ArrayList<DeleteListener> deleteListeners = new ArrayList<DeleteListener>();

    private boolean getAllFileProcess = false;
    private Object waitDirGet = new Object();
    private final Lock lock = new ReentrantLock();
    private final Condition getFileContidion = lock.newCondition();
    private Handler fileHandler;
    private FileOutputStream fileOutputStream;
    private boolean downloadComplete = true;
    private File downloadFile;

    private Handler formatHandler;
    private Handler deleteHandler;

    private IntentFilter networkFilter;
    private BroadcastReceiver networkReceiver;
    private Object activityNetworkChanging = new Object();
    private int prevActivityNetworkType;
    private ArrayList<UdriverFileInfo> delUdriverFileInfos = new ArrayList<UdriverFileInfo>();

    @Override
    public void onCreate() {
        super.onCreate();
        udriver = this;
//        initialUdriver(getApplicationContext());
//        loginHandler = new LoginResultHandler();
//        DxtWiFi.sdCard.addLoginHandler(loginHandler);
//        directoryHandler = new DirectoryHandler();
//        DxtWiFi.sdCard.addDirectoryHandler(directoryHandler);
//        fileEventHandler = new FileEventHandler();
//        DxtWiFi.sdCard.addFileHandler(fileEventHandler);
//        fileHandler = new FileHandler();
    }

    public void registerNetworkReceive() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            prevActivityNetworkType = networkInfo.getType();
        } else {
            prevActivityNetworkType = -1;
        }

        networkFilter = new IntentFilter();
        networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        NetworkInfo networkInfo;
                        networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (networkInfo != null) {
                            int currentActivityNetworkType = networkInfo.getType();
                            synchronized (activityNetworkChanging) {
                                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (prevActivityNetworkType != currentActivityNetworkType) {
                                    if ((currentActivityNetworkType == ConnectivityManager.TYPE_MOBILE) && wifiManager.isWifiEnabled()) {
                                        DxtWiFi.sdCard.enableWifiWithCallularIsActivity();  //for Android One
                                    } else if ((currentActivityNetworkType == ConnectivityManager.TYPE_WIFI) && (prevActivityNetworkType == ConnectivityManager.TYPE_MOBILE)) {
                                    }
                                    prevActivityNetworkType = currentActivityNetworkType;
                                }
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(networkReceiver, networkFilter);
    }

    public ArrayList<String> getFileList() {
        ArrayList<String> files = new ArrayList<String>();
        for (UdriverFileInfo udriverFileInfo : udriverFileInfos) {
            files.add(udriverFileInfo.getFilePath());
        }
        return files;
    }

    public ArrayList<UdriverFileInfo> getFileListWithinfo() {
        return udriverFileInfos;
    }

    public void rootDirectoryReady(final FsDirectory root) {
        udriver.rootDirectory = root;
        FileParser();
    }

    public void FileParser() {
        Thread thread = new Thread() {
            public void run() {
                udriverFiles.clear();
                udriverFileInfos.clear();

                if (rootDirectory == null) {
                    return;
                }
                getFileInDirectory(rootDirectory, "/");
                Message msg = fileHandler.obtainMessage(0);
                fileHandler.sendMessage(msg);
            }
        };
        thread.start();
    }

    public void getDirectory(FsDirectoryEntry directoryEntry, String path) {
        FsDirectory fsDirectory;

        DxtWiFi.sdCard.getDirectory(directoryEntry);
        lock.lock();
        try {
            getFileContidion.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        fsDirectory = tempDirectory;
        String filePath = path + directoryEntry.getName() + "/";
        getFileInDirectory(fsDirectory, filePath);
    }

    public void getFileInDirectory(FsDirectory directory, String path) {

        Iterator<FsDirectoryEntry> entryIterator = directory.iterator();
        for (; entryIterator.hasNext(); ) {
            FsDirectoryEntry directoryEntry = entryIterator.next();
            if (!directoryEntry.getName().startsWith(".") && !directoryEntry.isHidden()) {
                if (directoryEntry.isFile()) {
                    UdriverFileInfo info = null;
                    String filePath = path + directoryEntry.getName();
                    try {
                        info = new UdriverFileInfo(directoryEntry.getName(), filePath, directoryEntry.getFile().getLength(), directoryEntry.getCreated());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (info != null)
                        udriverFileInfos.add(info);
                } else if (directoryEntry.isDirectory()) {
                    getDirectory(directoryEntry, path);
                }
            }
        }
        //sort with file create time
        Comparator<UdriverFileInfo> comparator = new UdriverFileInfoCompare();
        Collections.sort(udriverFileInfos, comparator);
    }

    class UdriverFileInfoCompare implements Comparator<UdriverFileInfo> {

        @Override
        public int compare(UdriverFileInfo fileInfo1, UdriverFileInfo fileInfo2) {
            if (fileInfo1 == null || fileInfo2 == null) {
                return 0;
            }
            long createTime1 = fileInfo1.getFileCreateDate();
            long createTime2 = fileInfo2.getFileCreateDate();
            if (createTime1 > createTime2) {
                return -1;
            } else if (createTime1 < createTime2) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public FsDirectoryEntry fileToFsEntry(UdriverFileInfo udriverFileInfo) {
        FsDirectoryEntry entry = null;
        String path = udriverFileInfo.getFilePath();
        FsDirectory tempDirectory = rootDirectory;
        String playFileName;
        //get entry
        while (true) {
            int rootPos = path.indexOf("/");
            int lastPos = path.lastIndexOf("/");
            if (rootPos == lastPos) {   //files
                playFileName = path.substring(rootPos + 1);
                Iterator<FsDirectoryEntry> entryIterator = tempDirectory.iterator();
                for (; entryIterator.hasNext(); ) {
                    FsDirectoryEntry directoryEntry = entryIterator.next();
                    if (directoryEntry.isFile()) {
                        if (directoryEntry.getName().equals(udriverFileInfo.getFileName())) {
                            entry = directoryEntry;
                            break;
                        }
                    }
                }
                break;
            } else {
                path = path.substring(rootPos + 1);
                DXTdebug.debug_Format("path:" + path);
                int pos = path.indexOf("/");
                String dirName = path.substring(0, pos);
                Iterator<FsDirectoryEntry> entryIterator = tempDirectory.iterator();
                for (; entryIterator.hasNext(); ) {
                    FsDirectoryEntry directoryEntry = entryIterator.next();
                    if (directoryEntry.isDirectory()) {
                        if (directoryEntry.getName().equals(dirName)) {
                            try {
                                tempDirectory = directoryEntry.getDirectory();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
        return entry;
    }

    public void getJpegThumbnail(UdriverFileInfo udriverFileInfo) {
        FsDirectoryEntry jpgThumbnail = fileToFsEntry(udriverFileInfo);
        DxtWiFi.sdCard.loadThumb(jpgThumbnail);
    }

    public void getTsPlayTime_Thumbnail(UdriverFileInfo udriverFileInfo) {
        FsDirectoryEntry tsFile = fileToFsEntry(udriverFileInfo);
        DxtWiFi.sdCard.loadTsPlayTime_Thumbnail(tsFile, udriverFileInfo);
    }

    public void playVideo(UdriverFileInfo udriverFileInfo) {    //use Http server
        FsDirectoryEntry video = fileToFsEntry(udriverFileInfo);
        if (video != null) {
            DxtWiFi.sdCard.cancelFileLoading(null);
            ProgressiveStreamHandler.processingFile = video;
            Message message = fileEventHandler.obtainMessage(DxtWiFi.FILE_MSG_VIDEO_STREAMING_CAPABILITY, 1, 0, video.getName());
            // 保存文件创建日期，用于截屏时候创建文件名使用
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MMdd_HHmmss");

            PreferenceUtil.commitString("playFileTime", simpleDateFormat.format(udriverFileInfo.getFileCreateDate()));
            fileEventHandler.sendMessage(message);
        }
    }

    public void downloadFile(final UdriverFileInfo udriverFileInfo, File destFile) {
        final FsDirectoryEntry srcFileEntry = fileToFsEntry(udriverFileInfo);
        downloadFile = destFile;
        Thread downloadThread = new Thread() {
            public void run() {
                try {
                    DxtWiFi.sdCard.stopGetTsThumbnail();
                    downloadComplete = false;
                    fileOutputStream = new FileOutputStream(downloadFile);
                    DxtWiFi.sdCard.writeFileTo(fileOutputStream, srcFileEntry, 0, srcFileEntry.getFile().getLength());
                    fileOutputStream.close();
                    fileOutputStream = null;
                    downloadComplete = true;
                    DxtWiFi.sdCard.continueGetTsThumbnail();
                } catch (FileNotFoundException e) {
                    DxtWiFi.sdCard.continueGetTsThumbnail();
                    e.printStackTrace();
                } catch (IOException e) {
                    if (downloadFile != null && downloadFile.length() != udriverFileInfo.getFileLength()) {
                        if (downloadFile.delete()) {

                        }
                    }
//                    DxtWiFi.sdCard.continueGetTsThumbnail();
                    e.printStackTrace();
                }
            }
        };
        downloadThread.start();
    }

    public boolean downloadFileStop() throws Exception {
        if (!downloadComplete) {
            downloadComplete = true;
            if (fileOutputStream != null) {
                try {
                    if (downloadFile != null && downloadFile.exists()) {
                        downloadFile.delete();
                    }
                    fileOutputStream.close();
                    fileOutputStream = null;
                    //if(downloadFile.delete())   {
                    //}
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void formatSDCard() {
        formatHandler = new FormatHandler();
        DxtWiFi.sdCard.setFormatHandler(formatHandler);
        DxtWiFi.sdCard.formatSDCard();
    }

    public void deleteFile(UdriverFileInfo udriverFileInfo) {
        deleteHandler = new DeleteHandler();
        DxtWiFi.sdCard.setdeleteHandler(deleteHandler);
        final FsDirectoryEntry deleteFileEntry = fileToFsEntry(udriverFileInfo);
        DxtWiFi.sdCard.deleteFile(deleteFileEntry);
        synchronized (delUdriverFileInfos) {
            delUdriverFileInfos.add(udriverFileInfo);
        }
    }

    public String getStoragePath_AndroidM(Context mcontext, boolean isRemovable) {
        return DxtWiFi.sdCard.getStoragePath_AndroidM(mcontext, isRemovable);
    }

    public void initialUdriver(Context applicationContext) {
        DxtWiFi.startSDCard(applicationContext, GET_SD_CAPACITY, GET_TS_THUMBNAIL, TS_THUMBNAIL_PRELOAD_SIZE, GET_TS_THUMBNAIL_MODE);
        // 视频缩略图长款除以4
        DxtWiFi.sdCard.enableTsThumbnailScale(20);
        DxtWiFi.sdCard.setHeartBeatPacketInterval(3000);
        loginHandler = new LoginResultHandler();
        DxtWiFi.sdCard.addLoginHandler(loginHandler);
        directoryHandler = new DirectoryHandler();
        DxtWiFi.sdCard.addDirectoryHandler(directoryHandler);
        fileEventHandler = new FileEventHandler();
        DxtWiFi.sdCard.addFileHandler(fileEventHandler);
        fileHandler = new FileHandler();
    }

    public boolean isConnected() {
        try {
            return DxtWiFi.sdCard.isCardConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public void setMyIp(String Addr) {
        DxtWiFi.sdCard.setMyIp(Addr);
    }

    public String getIP() {
        return DxtWiFi.sdCard.getIP();
    }

    public long getFreeSpace() {
        return DxtWiFi.sdCard.getFreeSpace();
    }

    public long getCapacity() {
        return DxtWiFi.sdCard.getCapacity();
    }

    public void start(String username, String password, String folder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (wifiManager.isWifiEnabled() && (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)) {
                    DxtWiFi.sdCard.enableWifiWithCallularIsActivity();  //for Android One
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        DxtWiFi.sdCard.start(username, password, folder);
    }

    public static boolean isSupportedImageType(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_IMAGE_TYPE.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_IMAGE_TYPE[i])) {
                return true;
            }
        }
        return isSupportedRAWType(filename);
    }

    public static boolean isSupportedRAWType(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_IMAGE_TYPE.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_IMAGE_TYPE[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedMusicType(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_MUSIC_TYPE.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_MUSIC_TYPE[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedVideoType(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_VIDEO_TYPE.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_VIDEO_TYPE[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedStreamingVideo(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_STREAMING_VIDEO.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_STREAMING_VIDEO[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedMiscType(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_MISC_TYPE.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_MISC_TYPE[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedTsVideo(String filename) {
        int pos = filename.lastIndexOf('.');
        String extension = filename.substring(pos + 1);
        for (int i = 0; i < SUPPORTED_TS_VIDEO.length; i++) {
            if (extension.equalsIgnoreCase(SUPPORTED_TS_VIDEO[i])) {
                return true;
            }
        }
        return false;
    }

    public void stopGetTsThumbnail() {
        DxtWiFi.sdCard.stopGetTsThumbnail();
    }

    public void continueGetTsThumbnail() {
        DxtWiFi.sdCard.continueGetTsThumbnail();
    }

    public void setOnDeviceFound() {
        DxtWiFi.sdCard.onDeviceFound(null);
    }

    public void addLoginResultListener(LoginResultListener loginResultListener) {
        this.loginResultListeners.add(loginResultListener);
    }

    public void removeLoginResultListener(LoginResultListener loginResultListener) {
        this.loginResultListeners.remove(loginResultListener);
    }

    public void addDirectoryListener(DirectoryListener directoryListener) {
        this.directoryListeners.add(directoryListener);
    }

    public void removeDirectoryListener(DirectoryListener directoryListener) {
        this.directoryListeners.remove(directoryListener);
    }

    public void addFileEventListener(FileEventListener mediaEventListener) {
        this.fileEventListeners.add(mediaEventListener);
    }

    public void removeFileEventListener(FileEventListener mediaEventListener) {
        this.fileEventListeners.remove(mediaEventListener);
    }

    public void addFileListener(FileListener fileListener) {
        this.fileListeners.add(fileListener);
    }

    public void removeFileListener(FileListener fileListener) {
        this.fileListeners.remove(fileListener);
    }

    public void addFormatListener(FormatListener formatListener) {
        this.formatListeners.add(formatListener);
    }

    public void removeFormatListener(FormatListener formatListener) {
        this.formatListeners.remove(formatListener);
    }

    public void addDeleteFileListener(DeleteListener deleteListener) {
        this.deleteListeners.add(deleteListener);
    }

    public void removeDeleteFileListener(DeleteListener deleteListener) {
        this.deleteListeners.remove(deleteListener);
    }

    //handler
    static class LoginResultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DxtWiFi.LOGIN_RESULT:
                    DXTdebug.debug_Format("on Login Result, udriver.loginResultListeners size:" + udriver.loginResultListeners.size());
                    for (LoginResultListener loginResultListener : udriver.loginResultListeners) {
                        loginResultListener.onLoginResultListener(msg.arg1);
                    }
                    break;
                case DxtWiFi.DEVICE_FOUND:
                    DXTdebug.debug_Format("on Device Found, udriver.loginResultListeners size:" + udriver.loginResultListeners.size());
                    for (LoginResultListener loginResultListener : udriver.loginResultListeners) {
                        loginResultListener.onDeviceFoundListener();
                    }
                    break;
                case DxtWiFi.DEVICE_ERROR:
                    String str = (String) msg.obj;
                    for (LoginResultListener loginResultListener : udriver.loginResultListeners) {
                        if (str != null) {
                            if (str.compareTo("file already exists") == 0) {
                                loginResultListener.onDeviceErrorDuplicateFileListener();
                            } else {
                                loginResultListener.onDeviceErrorUnknownFormatListener();
                            }
                        }
                    }
                    break;
                case DxtWiFi.UNFORMATTED_ERROR:
                    for (LoginResultListener loginResultListener : udriver.loginResultListeners) {
                        loginResultListener.onDeviceErrorUnFormatListener();
                    }
                    break;
                case DxtWiFi.DEVICE_READY:
                    DXTdebug.debug_Format("on Device ready, udriver.loginResultListeners size:" + udriver.loginResultListeners.size());
                    for (LoginResultListener loginResultListener : udriver.loginResultListeners) {
                        loginResultListener.onDeviceReadyListener();
                    }
                    break;

            }
        }
    }

    static class DirectoryHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DxtWiFi.DIRECTORY_MSG_READY:
                    FsDirectoryEntry msgFile = (FsDirectoryEntry) msg.obj;
                    try {
                        udriver.tempDirectory = msgFile.getDirectory();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    udriver.lock.lock();
                    udriver.getFileContidion.signal();
                    udriver.lock.unlock();
                    //for(DirectoryListener directoryListener : udriver.directoryListeners)   {
                    //    directoryListener.onDirectoryReady(msg.obj);
                    //}
                    break;
                case DxtWiFi.DIRECTORY_MSG_THUMBNAIL:    // thumbnail ready
                    for (DirectoryListener directoryListener : udriver.directoryListeners) {
                        directoryListener.onDirectoryThumbnail(msg.obj);
                    }
                    break;
                case DxtWiFi.DIRECTORY_MSG_ROOT_READY:    // root directory ready
                    /*for(DirectoryListener directoryListener : udriver.directoryListeners)   {
                                                    directoryListener.onDirectortRootReady(msg.obj);
                                               }*/
                    FsDirectory root = (FsDirectory) msg.obj;
                    udriver.rootDirectoryReady(root);
                    break;
                case DxtWiFi.DIRECTORY_MSG_ADDFILE_RESULT:    // result of addnew file
                    for (DirectoryListener directoryListener : udriver.directoryListeners) {
                        directoryListener.onDirectoryAddFileResult(msg.obj);
                    }
                    break;
                case DxtWiFi.DIRECTORY_MSG_WRITE_FILE_DONE:    // write file complete
                    for (DirectoryListener directoryListener : udriver.directoryListeners) {
                        directoryListener.onDirectoryWriteFileDone(msg.obj);
                    }
                    break;

                case DxtWiFi.DIRECTORY_MSG_DIRECTORY_ADDED:
                    for (DirectoryListener directoryListener : udriver.directoryListeners) {
                        directoryListener.onDirectoryDirAdded();
                    }
                    break;
                case DxtWiFi.DIRECTORY_MSG_FILE_UPLOADED:
                    for (DirectoryListener directoryListener : udriver.directoryListeners) {
                        directoryListener.onDirectoryFileUpload();
                    }
                    break;
            }
        }
    }

    static class FileEventHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DxtWiFi.FILE_MSG_LOADING_COMPLETE:
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onFileLoadingComplete();
                    }
                    break;
                case DxtWiFi.FILE_MSG_LOADING_PROGRESS:
                    long position = ((FsDirectoryEntry) msg.obj).getProgress();
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onFileLoadingProgress((int) position);
                    }
                    break;
                case DxtWiFi.FILE_MSG_LOADING_ERROR:
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onFileLoadingError();
                    }
                    break;
                case DxtWiFi.FILE_MSG_VIDEO_STREAMING_CAPABILITY:
                    boolean isStreamable = msg.arg1 == 1;
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onFileVideoStreamCapability(isStreamable, msg.obj);
                    }
                    break;
                case DxtWiFi.FILE_MSG_DATA_UPDATED:
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onDataUpdate();
                    }
                    break;
                case DxtWiFi.FILE_MSG_FILE_SCAN:
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onFileScan(msg.obj);
                    }
                    break;
                case DxtWiFi.FILE_MSG_TS_FILE_PLAY_TIME:
                    UdriverFileInfo udriverFileInfo = (UdriverFileInfo) msg.obj;
                    for (FileEventListener fileEventListener : udriver.fileEventListeners) {
                        fileEventListener.onGetTsFilePlayTime(udriverFileInfo);
                    }
                    break;
            }
        }
    }

    static class FileHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    for (FileListener fileListener : udriver.fileListeners) {
                        fileListener.onFileScanDone();
                    }
            }
        }
    }

    static class FormatHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    for (FormatListener formatListener : udriver.formatListeners) {
                        formatListener.onFormatWriteBusy();
                    }
                    break;
                case 1:
                    if ((msg.arg1 > 0) && (msg.arg1 == msg.arg2)) {
                        udriver.formatHandler = null;
                        DxtWiFi.sdCard.clrFormatHandler();
                    }
                    for (FormatListener formatListener : udriver.formatListeners) {
                        formatListener.onFormatProgress(msg.arg1, msg.arg2);
                    }
                    break;
            }
        }
    }

    static class DeleteHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    for (DeleteListener deleteListener : udriver.deleteListeners) {
                        deleteListener.onDeleteFileBusy();
                    }
                    break;
                case 1:
                    synchronized (udriver.delUdriverFileInfos) {
                        for (UdriverFileInfo delUdriverFileInfo : udriver.delUdriverFileInfos) {
                            for (DeleteListener deleteListener : udriver.deleteListeners) {
                                deleteListener.onDeleteFileDone(delUdriverFileInfo);
                            }
                            udriver.udriverFileInfos.remove(delUdriverFileInfo);
                        }
                        udriver.delUdriverFileInfos.clear();
                    }

                    //udriver.rootDirectoryReady(udriver.rootDirectory);
                    break;
                case 2:
                    for (DeleteListener deleteListener : udriver.deleteListeners) {
                        deleteListener.onDeleteFileError();
                    }
                    break;
            }
        }
    }

    //interface
    public interface LoginResultListener {
        public void onLoginResultListener(int result);

        public void onDeviceFoundListener();

        public void onDeviceErrorDuplicateFileListener();

        public void onDeviceErrorUnknownFormatListener();

        public void onDeviceErrorUnFormatListener();

        public void onDeviceReadyListener();
    }

    public interface DirectoryListener {
        public void onDirectoryReady(Object object);

        public void onDirectoryThumbnail(Object object);

        public void onDirectortRootReady(Object object);

        public void onDirectoryAddFileResult(Object object);

        public void onDirectoryWriteFileDone(Object object);

        public void onDirectoryDirAdded();

        public void onDirectoryFileUpload();
    }

    public interface FileEventListener {
        public void onFileLoadingComplete();

        public void onFileLoadingProgress(int position);

        public void onFileLoadingError();

        public void onFileVideoStreamCapability(boolean isStreamable, Object object);

        public void onDataUpdate();

        public void onFileScan(Object object);

        public void onGetTsFilePlayTime(UdriverFileInfo udriverFileInfo);
    }

    public interface FileListener {
        public void onFileScanDone();
    }

    public interface FormatListener {
        public void onFormatWriteBusy();

        public void onFormatProgress(int currentProgress, int maxProgress);
    }

    public interface DeleteListener {
        public void onDeleteFileBusy();

        public void onDeleteFileDone(UdriverFileInfo delUdriverFileInfo);

        public void onDeleteFileError();
    }

    public void turnOnUsb() {
        DxtWiFi.sdCard.turnOnUsb();
    }

    public void turnOffUsb() {
        DxtWiFi.sdCard.turnOffUsb();
    }

    public void closeDevice(boolean turnOnUsbFirst) {

        DxtWiFi.sdCard.removeLoginHandler(loginHandler);
        DxtWiFi.sdCard.removeDirectoryHandler(directoryHandler);
        DxtWiFi.sdCard.removeFileHandler(fileEventHandler);
//        DxtWiFi.sdCard.removeFileHandler(fileHandler);
        loginHandler = null;
        directoryHandler = null;
        fileEventHandler = null;
        fileHandler = null;

        try {
            DxtWiFi.sdCard.closeDevice(turnOnUsbFirst);
        } catch (Exception e) {
            // FIXME 这个地方该做什么处理呢
//            ToastUtils.showNomalShortToast(getApplicationContext(),"Wifi已断开连接！");
        }

    }

}
