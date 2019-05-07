package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/01/22
 *   desc   :
 *  version :
 * </pre>
 */
public class CaseRepair implements Parcelable{

    private int id;
    private String  caseid;
    private String createtime ;
    private String reason ;
    private String idea ;
    private String predate ;
    private String usercode ;
    private String userkind ;
    private String username ;

    public CaseRepair() {}

    public CaseRepair(int id, String caseid, String createtime, String reason, String idea, String predate, String usercode, String userkind, String username) {
        this.id = id;
        this.caseid = caseid;
        this.createtime = createtime;
        this.reason = reason;
        this.idea = idea;
        this.predate = predate;
        this.usercode = usercode;
        this.userkind = userkind;
        this.username = username;
    }

    protected CaseRepair(Parcel in) {
        id = in.readInt();
        caseid = in.readString();
        createtime = in.readString();
        reason = in.readString();
        idea = in.readString();
        predate = in.readString();
        usercode = in.readString();
        userkind = in.readString();
        username = in.readString();
    }

    public static final Creator<CaseRepair> CREATOR = new Creator<CaseRepair>() {
        @Override
        public CaseRepair createFromParcel(Parcel in) {
            return new CaseRepair(in);
        }

        @Override
        public CaseRepair[] newArray(int size) {
            return new CaseRepair[size];
        }
    };

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

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getPredate() {
        return predate;
    }

    public void setPredate(String predate) {
        this.predate = predate;
    }

    public String getUsercode() {
        return usercode;
    }

    public void setUsercode(String usercode) {
        this.usercode = usercode;
    }

    public String getUserkind() {
        return userkind;
    }

    public void setUserkind(String userkind) {
        this.userkind = userkind;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(caseid);
        dest.writeString(createtime);
        dest.writeString(reason);
        dest.writeString(idea);
        dest.writeString(predate);
        dest.writeString(usercode);
        dest.writeString(userkind);
        dest.writeString(username);
    }
}
