package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/08
 *   desc   :
 *  version :
 * </pre>
 */
public class UnitMessage  implements Parcelable {
    private String msgtime;
    private int msgId;
    private int id;
    private String msgurl;
    private String title;
    private String msgpic;
    private String content;
    private int status;

    public UnitMessage () {

    }

    public UnitMessage(String msgtime, int msgId, int id, String msgurl, String title, String msgpic, String content, int status) {
        this.msgtime = msgtime;
        this.msgId = msgId;
        this.id = id;
        this.msgurl = msgurl;
        this.title = title;
        this.msgpic = msgpic;
        this.content = content;
        this.status = status;
    }

    protected UnitMessage(Parcel in) {
        msgtime = in.readString();
        msgId = in.readInt();
        id = in.readInt();
        msgurl = in.readString();
        title = in.readString();
        msgpic = in.readString();
        content = in.readString();
        status = in.readInt();
    }

    public static final Creator<UnitMessage> CREATOR = new Creator<UnitMessage>() {
        @Override
        public UnitMessage createFromParcel(Parcel in) {
            return new UnitMessage(in);
        }

        @Override
        public UnitMessage[] newArray(int size) {
            return new UnitMessage[size];
        }
    };

    public String getMsgtime() {
        return msgtime;
    }

    public void setMsgtime(String msgtime) {
        this.msgtime = msgtime;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsgurl() {
        return msgurl;
    }

    public void setMsgurl(String msgurl) {
        this.msgurl = msgurl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsgpic() {
        return msgpic;
    }

    public void setMsgpic(String msgpic) {
        this.msgpic = msgpic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgtime);
        dest.writeInt(msgId);
        dest.writeInt(id);
        dest.writeString(msgurl);
        dest.writeString(title);
        dest.writeString(msgpic);
        dest.writeString(content);
        dest.writeInt(status);
    }
}
