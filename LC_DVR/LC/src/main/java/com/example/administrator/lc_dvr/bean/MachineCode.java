package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/17
 *   desc   :
 *  version :
 * </pre>
 */
public class MachineCode implements Parcelable{
    private Integer id;
    private String personcode;
    private String carid;
    private String ctime;
    private String machinecode;
    private int status;
    private String msg;

    public MachineCode() {
    }

    public MachineCode(Integer id, String personcode, String carid, String ctime, String machinecode, int status, String msg) {
        this.id = id;
        this.personcode = personcode;
        this.carid = carid;
        this.ctime = ctime;
        this.machinecode = machinecode;
        this.status = status;
        this.msg = msg;
    }

    protected MachineCode(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        personcode = in.readString();
        carid = in.readString();
        ctime = in.readString();
        machinecode = in.readString();
        status = in.readInt();
        msg = in.readString();
    }

    public static final Creator<MachineCode> CREATOR = new Creator<MachineCode>() {
        @Override
        public MachineCode createFromParcel(Parcel in) {
            return new MachineCode(in);
        }

        @Override
        public MachineCode[] newArray(int size) {
            return new MachineCode[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPersoncode() {
        return personcode;
    }

    public void setPersoncode(String personcode) {
        this.personcode = personcode;
    }

    public String getCarid() {
        return carid;
    }

    public void setCarid(String carid) {
        this.carid = carid;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getMachinecode() {
        return machinecode;
    }

    public void setMachinecode(String machinecode) {
        this.machinecode = machinecode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
        dest.writeString(personcode);
        dest.writeString(carid);
        dest.writeString(ctime);
        dest.writeString(machinecode);
        dest.writeInt(status);
        dest.writeString(msg);
    }
}
