package com.example.administrator.lc_dvr.bean;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.Date;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2018/11/30
 *   desc   :
 *  version :
 * </pre>
 */
public class Car implements Parcelable{
    private int id;
    private String carbrand;
    private String carermobile;
    private String carername;
    private String carnumber;
    private String cartype;
    private String msg;
    private String personcode;
    private int isdefault;
    private String unitcode;
    private String servicecode;
    private String inscode;
    private String carframe;
    private String permit;
    private String drivelicense;
    private String card;
    private String licenseA;
    private String licenseB;

    protected Car(Parcel in) {
        id = in.readInt();
        carbrand = in.readString();
        carermobile = in.readString();
        carername = in.readString();
        carnumber = in.readString();
        cartype = in.readString();
        msg = in.readString();
        personcode = in.readString();
        isdefault = in.readInt();
        unitcode = in.readString();
        servicecode = in.readString();
        inscode = in.readString();
        carframe = in.readString();
        permit = in.readString();
        drivelicense = in.readString();
        card = in.readString();
        licenseA = in.readString();
        licenseB = in.readString();
    }

    public Car(){

    }

    public Car(int id, String carbrand, String carermobile, String carername, String carnumber, String cartype, String msg, String personcode, int isdefault, String unitcode, String servicecode, String inscode, String carframe, String permit, String drivelicense, String card, String licenseA, String licenseB) {
        this.id = id;
        this.carbrand = carbrand;
        this.carermobile = carermobile;
        this.carername = carername;
        this.carnumber = carnumber;
        this.cartype = cartype;
        this.msg = msg;
        this.personcode = personcode;
        this.isdefault = isdefault;
        this.unitcode = unitcode;
        this.servicecode = servicecode;
        this.inscode = inscode;
        this.carframe = carframe;
        this.permit = permit;
        this.drivelicense = drivelicense;
        this.card = card;
        this.licenseA = licenseA;
        this.licenseB = licenseB;
    }

    public static final Creator<Car> CREATOR = new Creator<Car>() {
        @Override
        public Car createFromParcel(Parcel in) {
            return new Car(in);
        }

        @Override
        public Car[] newArray(int size) {
            return new Car[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersoncode() {
        return personcode;
    }

    public void setPersoncode(String personcode) {
        this.personcode = personcode;
    }

    public String getCarnumber() {
        return carnumber;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public String getCarername() {
        return carername;
    }

    public void setCarername(String carername) {
        this.carername = carername;
    }

    public String getCarermobile() {
        return carermobile;
    }

    public void setCarermobile(String carermobile) {
        this.carermobile = carermobile;
    }

    public String getCarbrand() {
        return carbrand;
    }

    public void setCarbrand(String carbrand) {
        this.carbrand = carbrand;
    }

    public String getCartype() {
        return cartype;
    }

    public void setCartype(String cartype) {
        this.cartype = cartype;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIsDefault() {
        return isdefault;
    }

    public void setIsdefault(Integer isdefault) {
        this.isdefault = isdefault;
    }

    public String getUnitcode() {
        return unitcode;
    }

    public void setUnitcode(String unitcode) {
        this.unitcode = unitcode;
    }

    public String getServicecode() {
        return servicecode;
    }

    public void setServicecode(String servicecode) {
        this.servicecode = servicecode;
    }

    public String getInscode() {
        return inscode;
    }

    public void setInscode(String inscode) {
        this.inscode = inscode;
    }

    public String getCarframe() {
        return carframe;
    }

    public void setCarframe(String carframe) {
        this.carframe = carframe;
    }

    public String getPermit() {
        return permit;
    }

    public void setPermit(String permit) {
        this.permit = permit;
    }

    public String getDrivelicense() {
        return drivelicense;
    }

    public void setDrivelicense(String drivelicense) {
        this.drivelicense = drivelicense;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getLicenseA() {
        return licenseA;
    }

    public void setLicenseA(String licenseA) {
        this.licenseA = licenseA;
    }

    public String getLicenseB() {
        return licenseB;
    }

    public void setLicenseB(String licenseB) {
        this.licenseB = licenseB;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(carbrand);
        dest.writeString(carermobile);
        dest.writeString(carername);
        dest.writeString(carnumber);
        dest.writeString(cartype);
        dest.writeString(msg);
        dest.writeString(personcode);
        dest.writeInt(isdefault);
        dest.writeString(unitcode);
        dest.writeString(servicecode);
        dest.writeString(inscode);
        dest.writeString(carframe);
        dest.writeString(permit);
        dest.writeString(drivelicense);
        dest.writeString(card);
        dest.writeString(licenseA);
        dest.writeString(licenseB);
    }
}
