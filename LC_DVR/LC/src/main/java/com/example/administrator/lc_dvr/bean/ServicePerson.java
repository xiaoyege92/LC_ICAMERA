package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/13
 *   desc   :
 *  version :
 * </pre>
 */
public class ServicePerson implements Parcelable{

    private String servicecode;
    private String createtime;
    private int deleteflag;
    private String demo;
    private String loginid;
    private String logoid;
    private String mobile;
    private String openid;
    private String password;
    private int status;
    private String username;
    private int workstatus;
    private String nickname;
    private String avatarurl;

    public ServicePerson() {

    }

    public ServicePerson(String servicecode, String createtime, int deleteflag, String demo, String loginid, String logoid, String mobile, String openid, String password, int status, String username, int workstatus, String nickname, String avatarurl) {
        this.servicecode = servicecode;
        this.createtime = createtime;
        this.deleteflag = deleteflag;
        this.demo = demo;
        this.loginid = loginid;
        this.logoid = logoid;
        this.mobile = mobile;
        this.openid = openid;
        this.password = password;
        this.status = status;
        this.username = username;
        this.workstatus = workstatus;
        this.nickname = nickname;
        this.avatarurl = avatarurl;
    }

    protected ServicePerson(Parcel in) {
        servicecode = in.readString();
        createtime = in.readString();
        deleteflag = in.readInt();
        demo = in.readString();
        loginid = in.readString();
        logoid = in.readString();
        mobile = in.readString();
        openid = in.readString();
        password = in.readString();
        status = in.readInt();
        username = in.readString();
        workstatus = in.readInt();
        nickname = in.readString();
        avatarurl = in.readString();
    }

    public static final Creator<ServicePerson> CREATOR = new Creator<ServicePerson>() {
        @Override
        public ServicePerson createFromParcel(Parcel in) {
            return new ServicePerson(in);
        }

        @Override
        public ServicePerson[] newArray(int size) {
            return new ServicePerson[size];
        }
    };

    public String getServicecode() {
        return servicecode;
    }

    public void setServicecode(String servicecode) {
        this.servicecode = servicecode;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public int getDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(int deleteflag) {
        this.deleteflag = deleteflag;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public String getLogoid() {
        return logoid;
    }

    public void setLogoid(String logoid) {
        this.logoid = logoid;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getWorkstatus() {
        return workstatus;
    }

    public void setWorkstatus(int workstatus) {
        this.workstatus = workstatus;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(servicecode);
        dest.writeString(createtime);
        dest.writeInt(deleteflag);
        dest.writeString(demo);
        dest.writeString(loginid);
        dest.writeString(logoid);
        dest.writeString(mobile);
        dest.writeString(openid);
        dest.writeString(password);
        dest.writeInt(status);
        dest.writeString(username);
        dest.writeInt(workstatus);
        dest.writeString(nickname);
        dest.writeString(avatarurl);
    }
}
