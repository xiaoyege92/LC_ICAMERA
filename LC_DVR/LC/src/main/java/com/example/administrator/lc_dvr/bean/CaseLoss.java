package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/25
 *   desc   :
 *  version :
 * </pre>
 */
public class CaseLoss implements Parcelable {
    private Integer id;
    private String caseid;
    private String createtime;
    private String idea;
    private String lossdate;
    private String losssum;
    private String lossstatus;
    private String username;
    private String usermobile;
    private String failreason;
    private String failreasondetial;
    private String usercode;
    private String userkind;

    public CaseLoss(){}

    public CaseLoss(Integer id, String caseid, String createtime, String idea, String lossdate, String losssum, String lossstatus, String username, String usermobile, String failreason, String failreasondetial, String usercode, String userkind) {
        this.id = id;
        this.caseid = caseid;
        this.createtime = createtime;
        this.idea = idea;
        this.lossdate = lossdate;
        this.losssum = losssum;
        this.lossstatus = lossstatus;
        this.username = username;
        this.usermobile = usermobile;
        this.failreason = failreason;
        this.failreasondetial = failreasondetial;
        this.usercode = usercode;
        this.userkind = userkind;
    }

    protected CaseLoss(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        caseid = in.readString();
        createtime = in.readString();
        idea = in.readString();
        lossdate = in.readString();
        losssum = in.readString();
        lossstatus = in.readString();
        username = in.readString();
        usermobile = in.readString();
        failreason = in.readString();
        failreasondetial = in.readString();
        usercode = in.readString();
        userkind = in.readString();
    }

    public static final Creator<CaseLoss> CREATOR = new Creator<CaseLoss>() {
        @Override
        public CaseLoss createFromParcel(Parcel in) {
            return new CaseLoss(in);
        }

        @Override
        public CaseLoss[] newArray(int size) {
            return new CaseLoss[size];
        }
    };

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

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getIdea() {
        return idea;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public String getLossdate() {
        return lossdate;
    }

    public void setLossdate(String lossdate) {
        this.lossdate = lossdate;
    }

    public String getLosssum() {
        return losssum;
    }

    public void setLosssum(String losssum) {
        this.losssum = losssum;
    }

    public String getLossstatus() {
        return lossstatus;
    }

    public void setLossstatus(String lossstatus) {
        this.lossstatus = lossstatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsermobile() {
        return usermobile;
    }

    public void setUsermobile(String usermobile) {
        this.usermobile = usermobile;
    }

    public String getFailreason() {
        return failreason;
    }

    public void setFailreason(String failreason) {
        this.failreason = failreason;
    }

    public String getFailreasondetial() {
        return failreasondetial;
    }

    public void setFailreasondetial(String failreasondetial) {
        this.failreasondetial = failreasondetial;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(caseid);
        dest.writeString(createtime);
        dest.writeString(idea);
        dest.writeString(lossdate);
        dest.writeString(losssum);
        dest.writeString(lossstatus);
        dest.writeString(username);
        dest.writeString(usermobile);
        dest.writeString(failreason);
        dest.writeString(failreasondetial);
        dest.writeString(usercode);
        dest.writeString(userkind);
    }
}
