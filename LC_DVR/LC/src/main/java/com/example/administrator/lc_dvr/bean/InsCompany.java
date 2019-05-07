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
public class InsCompany implements Parcelable{

    private String companycode;
    private int canreportcase; // 是否开通一键报案
    private String companyname; // 投保公司名称
    private String demo; // 备注
    private String reportcasekind;
    private String companymobile; // 投保公司电话
    private int sort;
    private String smssign;
    private String logo;



    public InsCompany() {}

    public InsCompany(String companycode, int canreportcase, String companyname, String demo, String reportcasekind, String companymobile, int sort, String smssign, String logo) {
        this.companycode = companycode;
        this.canreportcase = canreportcase;
        this.companyname = companyname;
        this.demo = demo;
        this.reportcasekind = reportcasekind;
        this.companymobile = companymobile;
        this.sort = sort;
        this.smssign = smssign;
        this.logo = logo;
    }

    protected InsCompany(Parcel in) {
        companycode = in.readString();
        canreportcase = in.readInt();
        companyname = in.readString();
        demo = in.readString();
        reportcasekind = in.readString();
        companymobile = in.readString();
        sort = in.readInt();
        smssign = in.readString();
        logo = in.readString();
    }

    public static final Creator<InsCompany> CREATOR = new Creator<InsCompany>() {
        @Override
        public InsCompany createFromParcel(Parcel in) {
            return new InsCompany(in);
        }

        @Override
        public InsCompany[] newArray(int size) {
            return new InsCompany[size];
        }
    };

    public String getCompanycode() {
        return companycode;
    }

    public void setCompanycode(String companycode) {
        this.companycode = companycode;
    }

    public int getCanreportcase() {
        return canreportcase;
    }

    public void setCanreportcase(int canreportcase) {
        this.canreportcase = canreportcase;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getReportcasekind() {
        return reportcasekind;
    }

    public void setReportcasekind(String reportcasekind) {
        this.reportcasekind = reportcasekind;
    }

    public String getCompanymobile() {
        return companymobile;
    }

    public void setCompanymobile(String companymobile) {
        this.companymobile = companymobile;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getSmssign() {
        return smssign;
    }

    public void setSmssign(String smssign) {
        this.smssign = smssign;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(companycode);
        dest.writeInt(canreportcase);
        dest.writeString(companyname);
        dest.writeString(demo);
        dest.writeString(reportcasekind);
        dest.writeString(companymobile);
        dest.writeInt(sort);
        dest.writeString(smssign);
        dest.writeString(logo);
    }
}
