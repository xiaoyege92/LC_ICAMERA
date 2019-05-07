package com.example.administrator.lc_dvr.bean;

import java.util.Date;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/12/20
 *   desc   : 联络记录model
 *  version :
 * </pre>
 */
public class Contact {

    private Integer id;

    private String caseid;

    private int usertype; //0-系统，1-服务单位，2-车主

    private String userid;

    private int contacttype; // 1-电话，2-VIP电话，3-短信

    private Date ctime;

    private int contactTime;

    private String smsContent;

    private String msg;

    private String mobile;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public int getUsertype() {
        return usertype;
    }

    public void setUsertype(int usertype) {
        this.usertype = usertype;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getContacttype() {
        return contacttype;
    }

    public void setContacttype(int contacttype) {
        this.contacttype = contacttype;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Integer getContactTime() {
        return contactTime;
    }

    public void setContactTime(Integer contactTime) {
        this.contactTime = contactTime;
    }

    public String getSmsContent() {
        return smsContent;
    }

    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
