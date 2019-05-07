package com.example.administrator.lc_dvr.module.lc_dvr;

import org.litepal.crud.DataSupport;

/**
 * Created by yangboru on 2017/11/2.
 */

public class DvrWifiName extends DataSupport {

    private String wifiName;

    //Litepal数据库自动生成的自增的ID
    private long id;

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
