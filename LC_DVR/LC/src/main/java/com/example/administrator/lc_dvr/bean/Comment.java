package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/12/25
 *   desc   : 评论的model
 *  version :
 * </pre>
 */
public class Comment implements Parcelable{
    private Integer id;
    private String caseid;
    private int usertype;
    private String userid;
    private Date ctime;
    private int star1;
    private int star2;
    private String pic;
    private String n;
    private String msg;

    public Comment() {}

    public Comment(Integer id, String caseid, int usertype, String userid, Date ctime, int star1, int star2, String pic, String n, String msg) {
        this.id = id;
        this.caseid = caseid;
        this.usertype = usertype;
        this.userid = userid;
        this.ctime = ctime;
        this.star1 = star1;
        this.star2 = star2;
        this.pic = pic;
        this.n = n;
        this.msg = msg;
    }

    protected Comment(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        caseid = in.readString();
        usertype = in.readInt();
        userid = in.readString();
        star1 = in.readInt();
        star2 = in.readInt();
        pic = in.readString();
        n = in.readString();
        msg = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
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

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public int getStar1() {
        return star1;
    }

    public void setStar1(int star1) {
        this.star1 = star1;
    }

    public int getStar2() {
        return star2;
    }

    public void setStar2(int star2) {
        this.star2 = star2;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
        dest.writeInt(usertype);
        dest.writeString(userid);
        dest.writeInt(star1);
        dest.writeInt(star2);
        dest.writeString(pic);
        dest.writeString(n);
        dest.writeString(msg);
    }
}
