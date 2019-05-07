package com.example.administrator.lc_dvr.bean;

import org.json.JSONObject;

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
public class CasePayment {
    private String caseid;
    private Map<String, List<Map<String, String>>> compensatepictures;

    public CasePayment() {}

    public CasePayment(String caseid, Map<String, List<Map<String, String>>> compensatepictures) {
        this.caseid = caseid;
        this.compensatepictures = compensatepictures;
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public Map<String, List<Map<String, String>>> getCompensatepictures() {
        return compensatepictures;
    }

    public void setCompensatepictures(Map<String, List<Map<String, String>>> compensatepictures) {
        this.compensatepictures = compensatepictures;
    }
}
