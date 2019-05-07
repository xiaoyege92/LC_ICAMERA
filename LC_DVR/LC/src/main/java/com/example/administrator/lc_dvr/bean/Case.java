package com.example.administrator.lc_dvr.bean;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/04/02
 *   desc   :
 *  version :
 * </pre>
 */
public class Case {

    private String caseid;
    private String casedate;
    private String accidentdate;
    private String accidentaddress;
    private String accidentscene;
    private String carnumber;
    private String mobile;
    private String username;
    private String accidentkind;
    private String accident;
    private String personinjure;
    private String carcanmove;

    private String inscode;
    private String casetype;
    private String clientType;
    private String threeCarnumber;
    private String threeCartype;
    private String threeUsername;
    private String threeMobile;
    private String goodinjure;
    private String cartype;

    private int isdemo;
    private Map<String, List<Map<String, String>>> casepictures; // 关联的报案图片附件
    private Map<String, List<Map<String, String>>>  compensatepictures;// 理赔图片

    public Case() {}
    public Case(String caseid, String casedate, String accidentdate, String accidentaddress, String accidentscene, String carnumber, String mobile, String username, String accidentkind, String accident, String personinjure, String carcanmove, String inscode, String casetype, String clientType, String threeCarnumber, String threeCartype, String threeUsername, String threeMobile, String goodinjure, String cartype, int isdemo, Map<String, List<Map<String, String>>> casepictures, Map<String, List<Map<String, String>>> compensatepictures) {
        this.caseid = caseid;
        this.casedate = casedate;
        this.accidentdate = accidentdate;
        this.accidentaddress = accidentaddress;
        this.accidentscene = accidentscene;
        this.carnumber = carnumber;
        this.mobile = mobile;
        this.username = username;
        this.accidentkind = accidentkind;
        this.accident = accident;
        this.personinjure = personinjure;
        this.carcanmove = carcanmove;
        this.inscode = inscode;
        this.casetype = casetype;
        this.clientType = clientType;
        this.threeCarnumber = threeCarnumber;
        this.threeCartype = threeCartype;
        this.threeUsername = threeUsername;
        this.threeMobile = threeMobile;
        this.goodinjure = goodinjure;
        this.cartype = cartype;
        this.isdemo = isdemo;
        this.casepictures = casepictures;
        this.compensatepictures = compensatepictures;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public String getCasedate() {
        return casedate;
    }

    public void setCasedate(String casedate) {
        this.casedate = casedate;
    }

    public String getAccidentdate() {
        return accidentdate;
    }

    public void setAccidentdate(String accidentdate) {
        this.accidentdate = accidentdate;
    }

    public String getAccidentaddress() {
        return accidentaddress;
    }

    public void setAccidentaddress(String accidentaddress) {
        this.accidentaddress = accidentaddress;
    }

    public String getAccidentscene() {
        return accidentscene;
    }

    public void setAccidentscene(String accidentscene) {
        this.accidentscene = accidentscene;
    }

    public String getCarnumber() {
        return carnumber;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccidentkind() {
        return accidentkind;
    }

    public void setAccidentkind(String accidentkind) {
        this.accidentkind = accidentkind;
    }

    public String getAccident() {
        return accident;
    }

    public void setAccident(String accident) {
        this.accident = accident;
    }

    public String getPersoninjure() {
        return personinjure;
    }

    public void setPersoninjure(String personinjure) {
        this.personinjure = personinjure;
    }

    public String getCarcanmove() {
        return carcanmove;
    }

    public void setCarcanmove(String carcanmove) {
        this.carcanmove = carcanmove;
    }

    public String getInscode() {
        return inscode;
    }

    public void setInscode(String inscode) {
        this.inscode = inscode;
    }

    public String getCasetype() {
        return casetype;
    }

    public void setCasetype(String casetype) {
        this.casetype = casetype;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getThreeCarnumber() {
        return threeCarnumber;
    }

    public void setThreeCarnumber(String threeCarnumber) {
        this.threeCarnumber = threeCarnumber;
    }

    public String getThreeCartype() {
        return threeCartype;
    }

    public void setThreeCartype(String threeCartype) {
        this.threeCartype = threeCartype;
    }

    public String getThreeUsername() {
        return threeUsername;
    }

    public void setThreeUsername(String threeUsername) {
        this.threeUsername = threeUsername;
    }

    public String getThreeMobile() {
        return threeMobile;
    }

    public void setThreeMobile(String threeMobile) {
        this.threeMobile = threeMobile;
    }

    public String getGoodinjure() {
        return goodinjure;
    }

    public void setGoodinjure(String goodinjure) {
        this.goodinjure = goodinjure;
    }

    public String getCartype() {
        return cartype;
    }

    public void setCartype(String cartype) {
        this.cartype = cartype;
    }

    public int getIsdemo() {
        return isdemo;
    }

    public void setIsdemo(int isdemo) {
        this.isdemo = isdemo;
    }

    public Map<String, List<Map<String, String>>> getCasepictures() {
        return casepictures;
    }

    public void setCasepictures(Map<String, List<Map<String, String>>> casepictures) {
        this.casepictures = casepictures;
    }

    public Map<String, List<Map<String, String>>> getCompensatepictures() {
        return compensatepictures;
    }

    public void setCompensatepictures(Map<String, List<Map<String, String>>> compensatepictures) {
        this.compensatepictures = compensatepictures;
    }
}
