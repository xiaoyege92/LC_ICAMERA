package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/09/03
 *   desc   :
 *  version :
 * </pre>
 */
public class LocalFileInfo {
    private String filePath;
    private String fileName;
    private String fileCreateTime;

    public LocalFileInfo() {

    }

    public LocalFileInfo(String filePath, String fileName, String fileCreateTime) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileCreateTime = fileCreateTime;
    }

    public LocalFileInfo(String fileName, String fileCreateTime) {
        this.fileName = fileName;
        this.fileCreateTime = fileCreateTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileCreateTime() {
        return fileCreateTime;
    }

    public void setFileCreateTime(String fileCreateTime) {
        this.fileCreateTime = fileCreateTime;
    }
}
