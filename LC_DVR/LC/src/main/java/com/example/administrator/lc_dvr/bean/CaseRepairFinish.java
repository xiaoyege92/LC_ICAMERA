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
public class CaseRepairFinish  implements Parcelable {

    private int id;
    private String  caseid;
    private String createtime ;
    private String finishdate;
    private String reason ;
    private String idea ;
    private String usercode ;
    private String userkind ;
    private String username ;

    public CaseRepairFinish() {}

    public CaseRepairFinish(int id, String caseid, String createtime, String finishdate, String reason, String idea, String usercode, String userkind, String username) {
        this.id = id;
        this.caseid = caseid;
        this.createtime = createtime;
        this.finishdate = finishdate;
        this.reason = reason;
        this.idea = idea;
        this.usercode = usercode;
        this.userkind = userkind;
        this.username = username;
    }

    protected CaseRepairFinish(Parcel in) {
        id = in.readInt();
        caseid = in.readString();
        createtime = in.readString();
        finishdate = in.readString();
        reason = in.readString();
        idea = in.readString();
        usercode = in.readString();
        userkind = in.readString();
        username = in.readString();
    }

    public static final Creator<CaseRepairFinish> CREATOR = new Creator<CaseRepairFinish>() {
        @Override
        public CaseRepairFinish createFromParcel(Parcel in) {
            return new CaseRepairFinish(in);
        }

        @Override
        public CaseRepairFinish[] newArray(int size) {
            return new CaseRepairFinish[size];
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

    public String getFinishdate() {
        return finishdate;
    }

    public void setFinishdate(String finishdate) {
        this.finishdate = finishdate;
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
        dest.writeString(finishdate);
        dest.writeString(reason);
        dest.writeString(idea);
        dest.writeString(usercode);
        dest.writeString(userkind);
        dest.writeString(username);
    }
}
