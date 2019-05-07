package com.example.administrator.lc_dvr.bean;

/**
 * <pre>
 *   author : zch
 *   e-mail : xxx@xx
 *   time   : 2019/03/25
 *   desc   :
 *  version :
 * </pre>
 */
public class RevokeReason {
    private String number;
    private String reason;
    private boolean isChecked;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
