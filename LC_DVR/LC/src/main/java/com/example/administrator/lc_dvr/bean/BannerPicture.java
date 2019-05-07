package com.example.administrator.lc_dvr.bean;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/28
 *   desc   :
 *  version :
 * </pre>
 */
public class BannerPicture {
    private String title;
    private String piclink;
    private String url;

    public BannerPicture () {}

    public BannerPicture(String title, String piclink, String url) {
        this.title = title;
        this.piclink = piclink;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPiclink() {
        return piclink;
    }

    public void setPiclink(String piclink) {
        this.piclink = piclink;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
