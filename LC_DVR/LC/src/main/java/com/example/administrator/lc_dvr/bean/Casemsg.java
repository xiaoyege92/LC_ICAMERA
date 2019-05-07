package com.example.administrator.lc_dvr.bean;

import java.util.Date;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/12/20
 *   desc   : 备注 信息
 *  version :
 * </pre>
 */
public class Casemsg {

    private int id;

    private String caseid;

    private String userkind;

    private String usercode;

    private String ctime;

    private String content;

    private String username;

    private String logoid;

    public String getUsername(){
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public String getUserkind() {
        return userkind;
    }

    public void setUserkind(String userkind) {
        this.userkind = userkind;
    }

    public String getUsercode() {
        return usercode;
    }

    public void setUsercode(String usercode) {
        this.usercode = usercode;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLogoid() {
        return logoid;
    }

    public void setLogoid(String logoid) {
        this.logoid = logoid;
    }
}
