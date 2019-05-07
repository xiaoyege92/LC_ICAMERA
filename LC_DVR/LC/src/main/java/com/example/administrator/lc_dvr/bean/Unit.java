package com.example.administrator.lc_dvr.bean;    //

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *   author  zch
 *   e-mail  xxx@xx
 *   time    2019/01/23
 *   desc   单位
 *  version
 * </pre>
 */
public class Unit implements Parcelable {
    private String unitcode;    // 201802221349010524,
    private String address;    // 同济路305号,
    private int canreportcase;    // 0,
    private String createtime;    // 2018-02-22T05;    //49;    //02.000+0000,
    private int deleteflag;    // 0,
    private String demo;    // 13258741235,
    private String icon;    // f3d900065c3549e4a5c887e29b192435,
    private String inscode;    // ,
    private String inskind;    // 单车,
    private String needpic;    // 1,
    private String notice;    // 有任何疑问，欢迎您及时联系我们。,
    private String owner;    // 张三,
    private String ownermobile;    // 18018011801,
    private String post;    // 站,
    private String reportcasekind;    // ,
    private String servicephone;    // 021-80270399,
    private String servicetime;    // 周一~周五 9;    //00--18;    //00,
    private String shortcode;    // 4014,
    private int status;    // 1,
    private int unitkind;    // 1,
    private String unitname;    // 上海西上海宝山别克,
    private String password;    // df2894fc405edfee0e0d506629cd9dfc,
    private String helptip;    // ,
    private String helpurl;    // ,
    private String unitpic;    // 136cf8e7f5b34e3186af47effb01e537,4424b01030714c159cfadcf56837110b,
    private String smscode;    // null,
    private String amount;    // null,
    private String showlogo;    // null,
    private int newworktime;    // null,
    private int newworkover;    // null

    protected Unit(Parcel in) {
        unitcode = in.readString();
        address = in.readString();
        canreportcase = in.readInt();
        createtime = in.readString();
        deleteflag = in.readInt();
        demo = in.readString();
        icon = in.readString();
        inscode = in.readString();
        inskind = in.readString();
        needpic = in.readString();
        notice = in.readString();
        owner = in.readString();
        ownermobile = in.readString();
        post = in.readString();
        reportcasekind = in.readString();
        servicephone = in.readString();
        servicetime = in.readString();
        shortcode = in.readString();
        status = in.readInt();
        unitkind = in.readInt();
        unitname = in.readString();
        password = in.readString();
        helptip = in.readString();
        helpurl = in.readString();
        unitpic = in.readString();
        smscode = in.readString();
        amount = in.readString();
        showlogo = in.readString();
        newworktime = in.readInt();
        newworkover = in.readInt();
    }

    public Unit() {

    }

    public Unit(String unitcode, String address, int canreportcase, String createtime, int deleteflag, String demo, String icon, String inscode, String inskind, String needpic, String notice, String owner, String ownermobile, String post, String reportcasekind, String servicephone, String servicetime, String shortcode, int status, int unitkind, String unitname, String password, String helptip, String helpurl, String unitpic, String smscode, String amount, String showlogo, int newworktime, int newworkover) {
        this.unitcode = unitcode;
        this.address = address;
        this.canreportcase = canreportcase;
        this.createtime = createtime;
        this.deleteflag = deleteflag;
        this.demo = demo;
        this.icon = icon;
        this.inscode = inscode;
        this.inskind = inskind;
        this.needpic = needpic;
        this.notice = notice;
        this.owner = owner;
        this.ownermobile = ownermobile;
        this.post = post;
        this.reportcasekind = reportcasekind;
        this.servicephone = servicephone;
        this.servicetime = servicetime;
        this.shortcode = shortcode;
        this.status = status;
        this.unitkind = unitkind;
        this.unitname = unitname;
        this.password = password;
        this.helptip = helptip;
        this.helpurl = helpurl;
        this.unitpic = unitpic;
        this.smscode = smscode;
        this.amount = amount;
        this.showlogo = showlogo;
        this.newworktime = newworktime;
        this.newworkover = newworkover;
    }

    public static final Creator<Unit> CREATOR = new Creator<Unit>() {
        @Override
        public Unit createFromParcel(Parcel in) {
            return new Unit(in);
        }

        @Override
        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };

    public String getUnitcode() {
        return unitcode;
    }

    public void setUnitcode(String unitcode) {
        this.unitcode = unitcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCanreportcase() {
        return canreportcase;
    }

    public void setCanreportcase(int canreportcase) {
        this.canreportcase = canreportcase;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getInscode() {
        return inscode;
    }

    public void setInscode(String inscode) {
        this.inscode = inscode;
    }

    public String getInskind() {
        return inskind;
    }

    public void setInskind(String inskind) {
        this.inskind = inskind;
    }

    public String getNeedpic() {
        return needpic;
    }

    public void setNeedpic(String needpic) {
        this.needpic = needpic;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnermobile() {
        return ownermobile;
    }

    public void setOwnermobile(String ownermobile) {
        this.ownermobile = ownermobile;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getReportcasekind() {
        return reportcasekind;
    }

    public void setReportcasekind(String reportcasekind) {
        this.reportcasekind = reportcasekind;
    }

    public String getServicephone() {
        return servicephone;
    }

    public void setServicephone(String servicephone) {
        this.servicephone = servicephone;
    }

    public String getServicetime() {
        return servicetime;
    }

    public void setServicetime(String servicetime) {
        this.servicetime = servicetime;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUnitkind() {
        return unitkind;
    }

    public void setUnitkind(int unitkind) {
        this.unitkind = unitkind;
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHelptip() {
        return helptip;
    }

    public void setHelptip(String helptip) {
        this.helptip = helptip;
    }

    public String getHelpurl() {
        return helpurl;
    }

    public void setHelpurl(String helpurl) {
        this.helpurl = helpurl;
    }

    public String getUnitpic() {
        return unitpic;
    }

    public void setUnitpic(String unitpic) {
        this.unitpic = unitpic;
    }

    public String getSmscode() {
        return smscode;
    }

    public void setSmscode(String smscode) {
        this.smscode = smscode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getShowlogo() {
        return showlogo;
    }

    public void setShowlogo(String showlogo) {
        this.showlogo = showlogo;
    }

    public int getNewworktime() {
        return newworktime;
    }

    public void setNewworktime(int newworktime) {
        this.newworktime = newworktime;
    }

    public int getNewworkover() {
        return newworkover;
    }

    public void setNewworkover(int newworkover) {
        this.newworkover = newworkover;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(unitcode);
        dest.writeString(address);
        dest.writeInt(canreportcase);
        dest.writeString(createtime);
        dest.writeInt(deleteflag);
        dest.writeString(demo);
        dest.writeString(icon);
        dest.writeString(inscode);
        dest.writeString(inskind);
        dest.writeString(needpic);
        dest.writeString(notice);
        dest.writeString(owner);
        dest.writeString(ownermobile);
        dest.writeString(post);
        dest.writeString(reportcasekind);
        dest.writeString(servicephone);
        dest.writeString(servicetime);
        dest.writeString(shortcode);
        dest.writeInt(status);
        dest.writeInt(unitkind);
        dest.writeString(unitname);
        dest.writeString(password);
        dest.writeString(helptip);
        dest.writeString(helpurl);
        dest.writeString(unitpic);
        dest.writeString(smscode);
        dest.writeString(amount);
        dest.writeString(showlogo);
        dest.writeInt(newworktime);
        dest.writeInt(newworkover);
    }
}
